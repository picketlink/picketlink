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

import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.authorization.util.AuthorizationUtil;
import org.picketlink.common.reflection.Reflections;
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
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.internal.schemes.BasicAuthenticationScheme;
import org.picketlink.http.internal.schemes.DigestAuthenticationScheme;
import org.picketlink.http.internal.schemes.FormAuthenticationScheme;
import org.picketlink.http.internal.schemes.TokenAuthenticationScheme;
import org.picketlink.http.internal.schemes.X509AuthenticationScheme;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Account;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.ERROR;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.FORBIDDEN;
import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition.OK;
import static org.picketlink.log.BaseLog.AUTHENTICATION_LOGGER;

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
    private Instance<HttpAuthenticationScheme> authenticationSchemesInstances;

    @Inject
    @PicketLink
    private Instance<HttpServletRequest> picketLinkHttpServletRequest;

    @Inject
    private ELProcessor elProcessor;

    private HttpSecurityConfiguration configuration;
    private Map<PathConfiguration, HttpAuthenticationScheme> authenticationSchemes = new HashMap<PathConfiguration, HttpAuthenticationScheme>();
    private PathMatcher pathMatcher;

    @Override
    public void init(FilterConfig config) throws ServletException {
        SecurityConfigurationBuilder configurationBuilder = this.picketLinkExtension.getSecurityConfigurationBuilder();
        SecurityConfiguration securityConfiguration = configurationBuilder.build();

        this.configuration = securityConfiguration.getHttpSecurityConfiguration();

        if (this.configuration == null) {
            throw new HttpSecurityConfigurationException("No configuration provided.");
        }

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

        this.pathMatcher = new PathMatcher(this.configuration.getPaths(), this.elProcessor);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
        ServletException {
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            throw new ServletException("This filter can only process HttpServletRequest requests.");
        }

        ContextualHttpServletRequest request = null;
        HttpServletResponse response = null;
        PathConfiguration pathConfiguration = null;

        try {
            request = new ContextualHttpServletRequest(this.picketLinkHttpServletRequest.get());

            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER.debugf("Processing request to path [%s].", request.getRequestURI());
            }

            response = (HttpServletResponse) servletResponse;
            pathConfiguration = resolvePathConfiguration(request);

            if (pathConfiguration != null) {
                Set<String> methods = pathConfiguration.getMethods();

                if (!methods.contains(request.getMethod())) {
                    request.invalidate(SC_METHOD_NOT_ALLOWED);
                    return;
                }

                if (!pathConfiguration.isSecured()) {
                    chain.doFilter(request, response);
                    return;
                }
            }

            Identity identity = getIdentity();

            performAuthenticationIfRequired(pathConfiguration, request, response);

            if (!identity.isLoggedIn()) {
                if (pathConfiguration != null && !response.isCommitted()) {
                    challengeClientForCredentials(pathConfiguration, request, response);
                }
            } else {
                if (pathConfiguration != null) {
                    if (!response.isCommitted()) {
                        if (!isLogoutPath(pathConfiguration)) {
                            boolean authorized = performAuthorization(pathConfiguration, request, response);

                            if (authorized) {
                                processRequest(pathConfiguration, request, response, chain);
                                return;
                            }
                        } else {
                            performLogout(request, response, identity, pathConfiguration);
                        }
                    }
                }
            }
        } catch (Exception e) {
            request.invalidate(SC_INTERNAL_SERVER_ERROR);

            if (pathConfiguration != null) {
                if (pathConfiguration.hasRedirectWhen(ERROR)) {
                    return;
                }

                throw new RuntimeException("Could not process path [" + pathConfiguration.getUri() + "].", e);
            }

            throw new RuntimeException("Unexpected error while processing requested path [" + request.getRequestURI() + "].", e);
        } finally {
            performOutboundProcessing(pathConfiguration, request, response, chain);
        }
    }

    private void performOutboundProcessing(PathConfiguration pathConfiguration, ContextualHttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (pathConfiguration != null && pathConfiguration.isSecured()) {
            String redirectUrl = null;
            if (request.isForbidden()) {
                redirectUrl = pathConfiguration.getRedirectUrl(FORBIDDEN);
            }

            if (redirectUrl == null) {
                if (request.isValid()) {
                    redirectUrl = pathConfiguration.getRedirectUrl(OK);
                } else {
                    redirectUrl = pathConfiguration.getRedirectUrl(ERROR);
                }
            }

            if (redirectUrl == null && isLogoutPath(pathConfiguration)) {
                redirectUrl = request.getContextPath();
            }

            if (redirectUrl != null) {
                if (redirectUrl.startsWith("/")) {
                    if (!redirectUrl.startsWith(request.getContextPath())) {
                        redirectUrl = request.getContextPath() + redirectUrl;
                    }
                }

                if (!response.isCommitted()) {
                    response.sendRedirect(redirectUrl);
                }
            } else {
                if (!request.isValid()) {
                    if (!response.isCommitted()) {
                        response.sendError(request.getInvalidationStatusCode());
                    }
                } else {
                    if (!response.isCommitted()) {
                        chain.doFilter(request, response);
                    }
                }
            }
        } else {
            if (!response.isCommitted()) {
                if (this.configuration.isPermissive()) {
                    chain.doFilter(request, response);
                }
            }
        }
    }

    private void performLogout(HttpServletRequest request, HttpServletResponse response, Identity identity, PathConfiguration pathConfiguration) throws IOException {
        if (identity.isLoggedIn()) {
            identity.logout();
        }
    }

    private boolean isLogoutPath(PathConfiguration pathConfiguration) {
        if (pathConfiguration != null) {
            return pathConfiguration.getLogoutConfiguration() != null;
        }

        return false;
    }

    private boolean performAuthorization(PathConfiguration pathConfiguration, ContextualHttpServletRequest request, HttpServletResponse response) {
        boolean isAuthorized = true;

        AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();

        if (authorizationConfiguration == null) {
            return true;
        }

        Identity identity = getIdentity();
        String[] allowedRoles = authorizationConfiguration.getAllowedRoles();

        if (allowedRoles != null) {
            for (String roneName : allowedRoles) {
                if (!AuthorizationUtil.hasRole(identity, this.partitionManager.get(), roneName)) {
                    isAuthorized = false;
                    break;
                }
            }
        }

        String[] allowedGroups = authorizationConfiguration.getAllowedGroups();

        if (allowedGroups != null) {
            for (String groupName : allowedGroups) {
                if (!AuthorizationUtil.isMember(identity, this.partitionManager.get(), groupName)) {
                    isAuthorized = false;
                    break;
                }
            }
        }

        String[] allowedRealms = authorizationConfiguration.getAllowedRealms();

        if (allowedRealms != null) {
            for (String realmName : allowedRealms) {
                Account validatedAccount = identity.getAccount();

                if (!validatedAccount.getPartition().getName().equals(realmName)) {
                    try {
                        Class<Object> partitionType = Reflections.classForName(realmName);

                        isAuthorized = AuthorizationUtil.hasPartition(identity, partitionType, null);
                    } catch (Exception ignore) {
                        isAuthorized = false;
                    }

                    if (isAuthorized) {
                        break;
                    }

                    isAuthorized = false;
                }
            }
        }

        String protectedUri = request.getContextPath() + pathConfiguration.getUri();
        int startRegex = protectedUri.indexOf("{");

        if (startRegex == -1) {
            String[] expressions = authorizationConfiguration.getExpressions();

            if (expressions != null) {
                for (String expression : expressions) {
                    try {
                        Object eval = this.elProcessor.eval(expression);

                        if (eval == null || !Boolean.class.isInstance(eval)) {
                            throw new RuntimeException("Authorization expressions [" + expression + "] must evaluate to a boolean.");
                        }

                        if (!Boolean.valueOf(eval.toString())) {
                            isAuthorized = false;
                            break;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process authorization expression [" + expression + "] for path [" + protectedUri + "].", e);
                    }
                }
            }
        } else {
            String[] expressions = authorizationConfiguration.getExpressions();
            String formattedProtectedUri = protectedUri;

            if (expressions != null) {
                for (String expression : expressions) {
                    try {
                        Object eval = this.elProcessor.eval(expression);

                        if (eval == null) {
                            throw new RuntimeException("Authorization expressions [" + expression + "] must evaluate to a not null value.");
                        }

                        String expressionPattern = expression.substring(1);

                        if (formattedProtectedUri.indexOf(expressionPattern) == -1) {
                            isAuthorized = false;
                            break;
                        }

                        formattedProtectedUri = formattedProtectedUri.replace(expressionPattern, eval.toString());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to process authorization expression [" + expression + "] for path [" + protectedUri + "].", e);
                    }
                }

                if (!request.getRequestURI().equals(formattedProtectedUri)) {
                    int prefixEnd = formattedProtectedUri.lastIndexOf('/');

                    if (prefixEnd != -1) {
                        String prefix = formattedProtectedUri.substring(0, prefixEnd);

                        if (!request.getRequestURI().startsWith(prefix)) {
                            isAuthorized = false;
                        }
                    } else {
                        isAuthorized = false;
                    }
                }
            }
        }

        if (!isAuthorized) {
            request.invalidate(HttpServletResponse.SC_FORBIDDEN);
        }

        return isAuthorized;
    }

    private void processRequest(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            throw new RuntimeException("Could not process request.", e);
        }
    }

    private void challengeClientForCredentials(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
        HttpAuthenticationScheme authenticationScheme = getAuthenticationScheme(pathConfiguration, request);

        if (authenticationScheme != null) {
            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER
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

    private void performAuthenticationIfRequired(PathConfiguration pathConfiguration, ContextualHttpServletRequest request, HttpServletResponse response) throws IOException {
        Identity identity = getIdentity();
        HttpAuthenticationScheme authenticationScheme = getAuthenticationScheme(pathConfiguration, request);

        if (authenticationScheme != null) {
            DefaultLoginCredentials creds = extractCredentials(request, authenticationScheme);

            if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                AUTHENTICATION_LOGGER.debugf("Credentials extracted from request [%s]", creds.getCredential());
            }

            if (creds.getCredential() != null) {
                if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                    AUTHENTICATION_LOGGER
                        .debugf("Forcing re-authentication. Logging out current user [%s]", identity.getAccount());
                }

                if (identity.isLoggedIn()) {
                    identity.logout();
                }

                creds = extractCredentials(request, authenticationScheme);
            }

            if (creds.getCredential() != null) {
                try {
                    if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
                        AUTHENTICATION_LOGGER.debugf("Authenticating using credentials [%s]", creds.getCredential());
                    }

                    identity.login();

                    authenticationScheme.onPostAuthentication(request, response);
                } catch (AuthenticationException ae) {
                    AUTHENTICATION_LOGGER.authenticationFailed(creds.getUserId(), ae);
                }
            }
        } else {
            if (pathConfiguration != null) {
                if (pathConfiguration.getAuthorizationConfiguration() != null) {
                    request.invalidate(SC_UNAUTHORIZED);
                }
            }
        }
    }

    private PathConfiguration resolvePathConfiguration(HttpServletRequest request) {
        return this.pathMatcher.matches(request);
    }

    private HttpAuthenticationScheme getAuthenticationScheme(PathConfiguration pathConfiguration, HttpServletRequest request) {
        HttpAuthenticationScheme authenticationScheme = null;

        if (pathConfiguration != null) {
            AuthenticationConfiguration authcConfiguration = pathConfiguration.getAuthenticationConfiguration();

            if (authcConfiguration != null) {
                AuthenticationSchemeConfiguration authSchemeConfiguration = authcConfiguration.getAuthenticationSchemeConfiguration();

                authenticationScheme = this.authenticationSchemes.get(pathConfiguration);

                if (authenticationScheme == null) {
                    if (FormAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authenticationScheme = resolveAuthenticationScheme(FormAuthenticationScheme.class);
                    } else if (DigestAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authenticationScheme = resolveAuthenticationScheme(DigestAuthenticationScheme.class);
                    } else if (BasicAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authenticationScheme = resolveAuthenticationScheme(BasicAuthenticationScheme.class);
                    } else if (X509AuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authenticationScheme = resolveAuthenticationScheme(X509AuthenticationScheme.class);
                    } else if (TokenAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        authenticationScheme = resolveAuthenticationScheme(TokenAuthenticationScheme.class);
                    } else if (CustomAuthenticationConfiguration.class.isInstance(authSchemeConfiguration)) {
                        CustomAuthenticationConfiguration customAuthcConfig = (CustomAuthenticationConfiguration) authSchemeConfiguration;
                        authenticationScheme = resolveAuthenticationScheme(customAuthcConfig.getSchemeType());
                    }

                    this.authenticationSchemes.put(pathConfiguration, authenticationScheme);
                }
            }
        } else if (request != null) {
            for (Map.Entry<PathConfiguration, HttpAuthenticationScheme> entry : this.authenticationSchemes.entrySet()) {
                DefaultLoginCredentials creds = extractCredentials(request, entry.getValue());

                if (creds.getCredential() != null) {
                    HttpSession session = request.getSession(false);

                    if (session != null) {
                        PathConfiguration originalAuthcPath = (PathConfiguration) session
                            .getAttribute(AUTHENTICATION_ORIGINAL_PATH);

                        if (originalAuthcPath != null && originalAuthcPath.equals(entry.getKey())) {
                            session.removeAttribute(AUTHENTICATION_ORIGINAL_PATH);
                            return entry.getValue();
                        }
                    }
                }
            }
        }

        return authenticationScheme;
    }

    @Override
    public void destroy() {

    }

    private DefaultLoginCredentials extractCredentials(HttpServletRequest request, HttpAuthenticationScheme authenticationScheme) {
        DefaultLoginCredentials creds = getCredentials();

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
        if (instance.isUnsatisfied()) {
            throw new IllegalStateException("Instance [" + instance + "] not found.");
        } else if (instance.isAmbiguous()) {
            throw new IllegalStateException("Instance [" + instance + "] is ambiguous.");
        }

        try {
            return (I) instance.get();
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve instance [" + instance + "].", e);
        }
    }

    private HttpAuthenticationScheme resolveAuthenticationScheme(Class<? extends HttpAuthenticationScheme> authSchemeType) {
        Instance<? extends HttpAuthenticationScheme> configuredAuthScheme = this.authenticationSchemesInstances.select(authSchemeType);

        if (configuredAuthScheme.isAmbiguous()) {
            throw new IllegalStateException("Ambiguous beans found for Http Authentication Scheme type [" + authSchemeType + "].");
        }

        if (configuredAuthScheme.isUnsatisfied()) {
            throw new IllegalStateException("No bean found for Http Authentication Scheme with type [" + authSchemeType + "].");
        }

        return configuredAuthScheme.get();
    }
}
