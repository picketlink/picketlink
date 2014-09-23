/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.http.internal;

import org.jboss.logging.Logger;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.config.SecurityConfiguration;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.config.http.AuthenticationConfiguration;
import org.picketlink.config.http.AuthenticationSchemeConfiguration;
import org.picketlink.config.http.AuthorizationConfiguration;
import org.picketlink.config.http.BasicAuthenticationConfiguration;
import org.picketlink.config.http.CustomAuthenticationConfiguration;
import org.picketlink.config.http.DigestAuthenticationConfiguration;
import org.picketlink.config.http.FormAuthenticationConfiguration;
import org.picketlink.config.http.HttpSecurityConfiguration;
import org.picketlink.config.http.HttpSecurityConfigurationException;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.config.http.TokenAuthenticationConfiguration;
import org.picketlink.config.http.X509AuthenticationConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.extension.PicketLinkExtension;
import org.picketlink.http.AccessDeniedException;
import org.picketlink.http.AuthenticationRequiredException;
import org.picketlink.http.HttpMethod;
import org.picketlink.http.MethodNotAllowedException;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.authorization.PathAuthorizer;
import org.picketlink.http.internal.authentication.schemes.BasicAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.DigestAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.FormAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.TokenAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.X509AuthenticationScheme;
import org.picketlink.http.internal.authorization.ExpressionPathAuthorizer;
import org.picketlink.http.internal.authorization.GroupPathAuthorizer;
import org.picketlink.http.internal.authorization.RealmPathAuthorizer;
import org.picketlink.http.internal.authorization.RolePathAuthorizer;
import org.picketlink.http.internal.util.RequestUtil;
import org.picketlink.idm.PartitionManager;
import org.picketlink.internal.el.ELProcessor;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.ERROR;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.FORBIDDEN;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.OK;
import static org.picketlink.log.BaseLog.HTTP_LOGGER;

/**
 * @author Pedro Igor
 */
public class SecurityFilter implements Filter {

    public static final String AUTHENTICATION_ORIGINAL_PATH = SecurityFilter.class.getName() + ".authc.original.path";

    @Inject
    private PicketLinkExtension picketLinkExtension;

    @Inject
    private Instance<PartitionManager> partitionManager;

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<DefaultLoginCredentials> credentialsInstance;

    @Inject
    @Any
    private Instance<HttpAuthenticationScheme> authenticationSchemesInstance;

    @Inject
    @Any
    private Instance<PathAuthorizer> pathAuthorizerInstance;

    @Inject
    @PicketLink
    private Instance<HttpServletRequest> picketLinkHttpServletRequest;

    @Inject
    private ELProcessor elProcessor;

    private HttpSecurityConfiguration configuration;
    private Map<PathConfiguration, HttpAuthenticationScheme> authenticationSchemes = new HashMap<PathConfiguration, HttpAuthenticationScheme>();
    private PathMatcher pathMatcher;
    private Map<PathConfiguration, List<PathAuthorizer>> pathAuthorizers = new HashMap<PathConfiguration, List<PathAuthorizer>>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        SecurityConfigurationBuilder configurationBuilder = this.picketLinkExtension.getSecurityConfigurationBuilder();
        SecurityConfiguration securityConfiguration = configurationBuilder.build();

        this.configuration = securityConfiguration.getHttpSecurityConfiguration();

        if (this.configuration == null) {
            throw new HttpSecurityConfigurationException("No configuration provided.");
        }

        initializePathMatcher();
        initializeAuthenticationSchemes();
        initializePathAuthorizers();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
        ServletException {
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            throw new ServletException("This filter can only process HttpServletRequest requests.");
        }

        PathConfiguration pathConfiguration = null;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        try {
            request = this.picketLinkHttpServletRequest.get();

            if (HTTP_LOGGER.isDebugEnabled()) {
                HTTP_LOGGER.debugf("Processing request to path [%s].", request.getRequestURI());
            }

            pathConfiguration = this.pathMatcher.matches(request);

            performAuthenticationIfRequired(pathConfiguration, request, response);

            if (isSecured(pathConfiguration)) {
                if (!isMethodAllowed(pathConfiguration, request)) {
                    throw new MethodNotAllowedException("The given method is not allowed [" + request.getMethod() + "] for path [" + pathConfiguration.getUri() + "].");
                }

                if (!response.isCommitted()) {
                    Identity identity = getIdentity();

                    if (!identity.isLoggedIn()) {
                        challengeClientForCredentials(pathConfiguration, request, response);
                    } else if (isLogoutPath(pathConfiguration)) {
                        performLogout(request, response, identity, pathConfiguration);
                    } else {
                        if (!isAuthorized(pathConfiguration, request, response)) {
                            throw new AccessDeniedException("The request for the given path [" + pathConfiguration.getUri() + "] was forbidden.");
                        }
                    }
                }
            }

            performOutboundProcessing(pathConfiguration, request, response, chain);
        } catch (Exception e) {
            handleException(pathConfiguration, request, response, e);
        }
    }

    private boolean isSecured(PathConfiguration pathConfiguration) {
        return pathConfiguration != null && pathConfiguration.isSecured();
    }

    private boolean isMethodAllowed(PathConfiguration pathConfiguration, HttpServletRequest request) {
        Set<HttpMethod> methods = pathConfiguration.getMethods();

        return methods.contains(HttpMethod.valueOf(request.getMethod().toUpperCase()));
    }

    private void performOutboundProcessing(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response.isCommitted()) {
            if (HTTP_LOGGER.isDebugEnabled()) {
                HTTP_LOGGER.debugf("Response already commited. Ignoring outbound processing for path [%s].", pathConfiguration);
            }
            return;
        }

        if (isSecured(pathConfiguration)) {
            String redirectUrl = pathConfiguration.getRedirectUrl(OK);

            if (isLogoutPath(pathConfiguration)) {
                if (RequestUtil.isAjaxRequest(request)) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    if (redirectUrl == null) {
                        redirectUrl = request.getContextPath();
                    }
                }
            }

            if (redirectUrl != null) {
                redirect(redirectUrl, request, response);
            } else {
                processRequest(pathConfiguration, request, response, chain);
            }
        } else {
            if (this.configuration.isPermissive()) {
                processRequest(pathConfiguration, request, response, chain);
            } else if (pathConfiguration == null) {
                response.sendError(SC_FORBIDDEN, "No configuration found for the given path [" + request.getRequestURI() + "] ");
            }
        }
    }

    private void redirect(String redirectUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
        redirectUrl = formatRedirectUrl(request, redirectUrl);

        if (HTTP_LOGGER.isDebugEnabled()) {
            HTTP_LOGGER.debugf("Redirecting to [%s].", redirectUrl);
        }

        response.sendRedirect(redirectUrl);
    }

    private void handleException(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response, Throwable exception) throws IOException {
        String redirectUrl = null;
        int statusCode;

        if (HTTP_LOGGER.isDebugEnabled()) {
            HTTP_LOGGER.debugf("Handling exception [%s] for path [%s].", exception, request.getRequestURI());
        }

        if (AuthenticationRequiredException.class.isInstance(exception)) {
            statusCode = SC_UNAUTHORIZED;
        } else if (AccessDeniedException.class.isInstance(exception)) {
            statusCode = SC_FORBIDDEN;

            if (isSecured(pathConfiguration)) {
                redirectUrl = pathConfiguration.getRedirectUrl(FORBIDDEN);
            }
        } else if (MethodNotAllowedException.class.isInstance(exception)) {
            statusCode = SC_METHOD_NOT_ALLOWED;
        } else {
            statusCode = SC_INTERNAL_SERVER_ERROR;

            if (isSecured(pathConfiguration)) {
                redirectUrl = pathConfiguration.getRedirectUrl(ERROR);
            }
        }

        if (redirectUrl != null) {
            redirect(redirectUrl, request, response);
        } else {
            String message = exception.getMessage();

            if (message == null) {
                message = "The server could not process your request.";
            }

            if (HTTP_LOGGER.isEnabled(Logger.Level.ERROR)) {
                HTTP_LOGGER.errorf(exception, "Exception thrown during processing for path [%s]. Sending error with status code [%s].", request.getRequestURI(), statusCode);
            }

            response.sendError(statusCode, message);
        }
    }

    private String formatRedirectUrl(HttpServletRequest request, String redirectUrl) {
        if (redirectUrl.startsWith("/")) {
            if (!redirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = request.getContextPath() + redirectUrl;
            }
        }

        return redirectUrl;
    }

    private void performLogout(HttpServletRequest request, HttpServletResponse response, Identity identity, PathConfiguration pathConfiguration) throws IOException {
        if (identity.isLoggedIn()) {
            identity.logout();
        }
    }

    private boolean isLogoutPath(PathConfiguration pathConfiguration) {
        return pathConfiguration != null && pathConfiguration.getLogoutConfiguration() != null;
    }

    private boolean isAuthorized(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
        List<PathAuthorizer> authorizers = this.pathAuthorizers.get(pathConfiguration);

        if (authorizers != null) {
            for (PathAuthorizer authorizer : authorizers) {
                if (!authorizer.authorize(pathConfiguration, request, response)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void processRequest(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            if (HTTP_LOGGER.isDebugEnabled()) {
                HTTP_LOGGER.debugf("Continuing to process request for path [%s].", request.getRequestURI());
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException("Could not process request.", e);
        }
    }

    private void challengeClientForCredentials(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
        HttpAuthenticationScheme authenticationScheme = getAuthenticationScheme(pathConfiguration, request);

        if (authenticationScheme != null) {
            if (HTTP_LOGGER.isDebugEnabled()) {
                HTTP_LOGGER
                    .debugf("Challenging client using authentication scheme [%s].", authenticationScheme);
            }

            try {
                authenticationScheme.challengeClient(request, response);
            } catch (Exception e) {
                throw new RuntimeException("Could not challenge client for credentials.", e);
            }

            HttpSession session = request.getSession(false);
            PathConfiguration authenticationOriginalPath;

            if (session != null) {
                authenticationOriginalPath = (PathConfiguration) session.getAttribute(AUTHENTICATION_ORIGINAL_PATH);

                if (authenticationOriginalPath == null || !authenticationOriginalPath.equals(pathConfiguration)) {
                    session.setAttribute(AUTHENTICATION_ORIGINAL_PATH, pathConfiguration);
                }
            }
        }
    }

    private void performAuthenticationIfRequired(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Identity identity = getIdentity();
        HttpAuthenticationScheme authenticationScheme = getAuthenticationScheme(pathConfiguration, request);

        if (authenticationScheme != null) {
            DefaultLoginCredentials creds = extractCredentials(request, authenticationScheme);

            if (HTTP_LOGGER.isDebugEnabled()) {
                HTTP_LOGGER.debugf("Credentials extracted from request [%s]", creds.getCredential());
            }

            if (creds.getCredential() != null) {
                if (identity.isLoggedIn()) {
                    if (HTTP_LOGGER.isDebugEnabled()) {
                        HTTP_LOGGER
                            .debugf("Forcing re-authentication. Logging out current user [%s]", identity.getAccount());
                    }

                    identity.logout();
                }

                creds = extractCredentials(request, authenticationScheme);
            }

            if (creds.getCredential() != null) {
                try {
                    if (HTTP_LOGGER.isDebugEnabled()) {
                        HTTP_LOGGER.debugf("Authenticating using credentials [%s]", creds.getCredential());
                    }

                    identity.login();

                    authenticationScheme.onPostAuthentication(request, response);
                } catch (AuthenticationException ae) {
                    HTTP_LOGGER.authenticationFailed(creds.getUserId(), ae);
                }
            }
        } else {
            if (!identity.isLoggedIn()) {
                if (pathConfiguration != null && pathConfiguration.getAuthorizationConfiguration() != null) {
                    throw new AuthenticationRequiredException("The given path [" + pathConfiguration.getUri() + "] requires authentication.");
                }
            }
        }
    }

    private HttpAuthenticationScheme getAuthenticationScheme(PathConfiguration pathConfiguration, HttpServletRequest request) {
        HttpAuthenticationScheme authenticationScheme = null;

        if (pathConfiguration != null) {
            AuthenticationConfiguration authcConfiguration = pathConfiguration.getAuthenticationConfiguration();

            if (authcConfiguration != null) {
                AuthenticationSchemeConfiguration authSchemeConfiguration = authcConfiguration.getAuthenticationSchemeConfiguration();

                authenticationScheme = this.authenticationSchemes.get(pathConfiguration);

                if (authenticationScheme == null) {
                    Class<? extends HttpAuthenticationScheme> authcSchemeType;

                    if (FormAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authcSchemeType = FormAuthenticationScheme.class;
                    } else if (DigestAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authcSchemeType = DigestAuthenticationScheme.class;
                    } else if (BasicAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authcSchemeType = BasicAuthenticationScheme.class;
                    } else if (X509AuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authcSchemeType = X509AuthenticationScheme.class;
                    } else if (TokenAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authcSchemeType = TokenAuthenticationScheme.class;
                    } else if (CustomAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        CustomAuthenticationConfiguration customAuthcConfig = (CustomAuthenticationConfiguration) authSchemeConfiguration;
                        authcSchemeType = customAuthcConfig.getSchemeType();
                    } else {
                        throw new HttpSecurityConfigurationException("Unexpected Authentication Scheme configuration [" + authSchemeConfiguration + "].");
                    }

                    authenticationScheme = resolveInstance(this.authenticationSchemesInstance, authcSchemeType);

                    this.authenticationSchemes.put(pathConfiguration, authenticationScheme);
                }
            }
        } else {
            authenticationScheme = restorePreviousAuthenticationScheme(request);
        }

        return authenticationScheme;
    }

    @Override
    public void destroy() {

    }

    private HttpAuthenticationScheme restorePreviousAuthenticationScheme(HttpServletRequest request) {
        for (Map.Entry<PathConfiguration, HttpAuthenticationScheme> entry : this.authenticationSchemes.entrySet()) {
            DefaultLoginCredentials creds = extractCredentials(request, entry.getValue());

            if (creds.getCredential() != null) {
                HttpSession session = request.getSession(false);

                if (session != null) {
                    PathConfiguration originalAuthcPath = (PathConfiguration) session.getAttribute(AUTHENTICATION_ORIGINAL_PATH);

                    if (originalAuthcPath != null && originalAuthcPath.equals(entry.getKey())) {
                        session.removeAttribute(AUTHENTICATION_ORIGINAL_PATH);
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }

    private DefaultLoginCredentials extractCredentials(HttpServletRequest request, HttpAuthenticationScheme authenticationScheme) {
        DefaultLoginCredentials creds = getCredentials();

        creds.invalidate();

        authenticationScheme.extractCredential(request, creds);

        return creds;
    }

    private DefaultLoginCredentials getCredentials() {
        return resolveInstance(this.credentialsInstance);
    }

    private Identity getIdentity() {
        return resolveInstance(this.identityInstance);
    }

    private <I> I resolveInstance(Instance<I> instance) {
        return resolveInstance(instance, null);
    }

    private <I> I resolveInstance(Instance<I> fromInstance, Class<? extends I> type) {
        try {
            Instance<? extends I> instance;

            if (type != null) {
                instance = fromInstance.select(type);
            } else {
                instance = fromInstance;
            }

            if (instance.isUnsatisfied()) {
                throw new IllegalStateException("Instance [" + instance + "] not found.");
            } else if (instance.isAmbiguous()) {
                throw new IllegalStateException("Instance [" + instance + "] is ambiguous.");
            }

            return instance.get();
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve fromInstance [" + fromInstance + "].", e);
        }
    }

    private void initializeAuthenticationSchemes() {
        for (List<PathConfiguration> configurations : this.configuration.getPaths().values()) {
            for (PathConfiguration pathConfiguration : configurations) {
                if (pathConfiguration.isSecured()) {
                    HttpAuthenticationScheme authenticationScheme = getAuthenticationScheme(pathConfiguration, null);

                    if (authenticationScheme != null) {
                        AuthenticationConfiguration authcConfig = pathConfiguration.getAuthenticationConfiguration();
                        AuthenticationSchemeConfiguration authcSchemeConfig = authcConfig.getAuthenticationSchemeConfiguration();

                        if (!CustomAuthenticationConfiguration.class.isInstance(authcSchemeConfig)) {
                            try {
                                authenticationScheme.initialize(authcSchemeConfig);
                            } catch (Exception e) {
                                throw new HttpSecurityConfigurationException("Could not initialize Http Authentication Scheme [" + authenticationScheme + "].", e);
                            }
                        }
                    }
                }
            }
        }
    }

    private void initializePathAuthorizers() {
        for (List<PathConfiguration> configurations : this.configuration.getPaths().values()) {
            for (PathConfiguration pathConfiguration : configurations) {
                if (pathConfiguration.isSecured()) {
                    AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();

                    if (authorizationConfiguration != null) {
                        List<PathAuthorizer> pathAuthorizers = new ArrayList<PathAuthorizer>();
                        List<Class<? extends PathAuthorizer>> pathAuthorizerTypes = new ArrayList<Class<? extends PathAuthorizer>>(authorizationConfiguration.getAuthorizers());

                        pathAuthorizerTypes.addAll(getDefaultPathAuthorizers());

                        for (Class<? extends PathAuthorizer> authorizerType : pathAuthorizerTypes) {
                            try {
                                pathAuthorizers.add(resolveInstance(this.pathAuthorizerInstance, authorizerType));
                            } catch (Exception e) {
                                throw new HttpSecurityConfigurationException("Could not resolve PathAuthorizer [" + authorizerType + "].", e);
                            }
                        }

                        this.pathAuthorizers.put(pathConfiguration, pathAuthorizers);
                    }
                }
            }
        }
    }

    private Set<Class<? extends PathAuthorizer>> getDefaultPathAuthorizers() {
        Set<Class<? extends PathAuthorizer>> defaultAuthorizers = new HashSet<Class<? extends PathAuthorizer>>();

        defaultAuthorizers.add(RolePathAuthorizer.class);
        defaultAuthorizers.add(GroupPathAuthorizer.class);
        defaultAuthorizers.add(RealmPathAuthorizer.class);
        defaultAuthorizers.add(ExpressionPathAuthorizer.class);

        return defaultAuthorizers;
    }

    private void initializePathMatcher() {
        this.pathMatcher = new PathMatcher(this.configuration.getPaths(), this.elProcessor);
    }
}
