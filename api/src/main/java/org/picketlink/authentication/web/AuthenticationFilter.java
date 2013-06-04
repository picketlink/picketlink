/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.authentication.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
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
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;

/**
 * @author Shane Bryzak
 * @author Pedro Igor
 */
@ApplicationScoped
public class AuthenticationFilter implements Filter {

    public static final String DEFAULT_REALM_NAME = "PicketLink Default Realm";

    public static final String REALM_NAME_INIT_PARAM = "realmName";
    public static final String AUTH_TYPE_INIT_PARAM = "authType";
    public static final String UNPROTECTED_METHODS_INIT_PARAM = "unprotectedMethods";

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<DefaultLoginCredentials> credentialsInstance;

    private Map<AuthType, HTTPAuthenticationScheme> authenticationSchemes = new HashMap<AuthType, HTTPAuthenticationScheme>();

    private Set<String> unprotectedMethods = new HashSet<String>();

    public enum AuthType {
        BASIC, DIGEST
    }

    private AuthType authType = AuthType.BASIC;
    private String realm = DEFAULT_REALM_NAME;

    @Override
    public void init(FilterConfig config) throws ServletException {
        String providedRealm = config.getInitParameter(REALM_NAME_INIT_PARAM);

        if (providedRealm != null) {
            this.realm = providedRealm;
        }

        setAuthType(config.getInitParameter(AUTH_TYPE_INIT_PARAM));

        this.authenticationSchemes.put(AuthType.DIGEST, new DigestAuthenticationScheme(this.realm));
        this.authenticationSchemes.put(AuthType.BASIC, new BasicAuthenticationScheme(this.realm));

        String unprotectedMethodsInitParam = config.getInitParameter(UNPROTECTED_METHODS_INIT_PARAM);

        if (unprotectedMethodsInitParam != null) {
            if (unprotectedMethodsInitParam.contains(",")) {
                for (String method : unprotectedMethodsInitParam.split(",")) {
                    this.unprotectedMethods.add(method.trim().toUpperCase());
                }
            } else {
                this.unprotectedMethods.add(unprotectedMethodsInitParam.trim().toUpperCase());
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException,
            ServletException {
        if (!HttpServletRequest.class.isInstance(servletRequest)) {
            throw new ServletException("This filter can only process HttpServletRequest requests.");
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isProtected(request)) {
            // Force session creation
            request.getSession();

            Identity identity;

            try {
                identity = identityInstance.get();
            } catch (Exception e) {
                throw new ServletException("Identity not found - please ensure that the Identity component is created on startup.",
                        e);
            }

            DefaultLoginCredentials creds;

            try {
                creds = credentialsInstance.get();
            } catch (Exception e) {
                throw new ServletException(
                        "DefaultLoginCredentials not found - please ensure that the DefaultLoginCredentials component is created on startup.",
                        e);
            }

            HTTPAuthenticationScheme authenticationScheme = this.authenticationSchemes.get(this.authType);

            if (!identity.isLoggedIn()) {
                authenticationScheme.extractCredential(request, creds);

                if (creds.getCredential() != null) {
                    identity.login();
                }
            }

            if (identity.isLoggedIn()) {
                chain.doFilter(servletRequest, servletResponse);
            } else {
                authenticationScheme.challengeClient(request, response);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private void setAuthType(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null authentication type provided.");
        }

        try {
            this.authType = AuthType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported authentication type. Possible values are: BASIC and DIGEST.", e);
        }
    }

    private boolean isProtected(HttpServletRequest request) {
        return !this.unprotectedMethods.contains(request.getMethod().toUpperCase());
    }

}
