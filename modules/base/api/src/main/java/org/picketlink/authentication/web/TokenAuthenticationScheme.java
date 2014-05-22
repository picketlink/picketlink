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
package org.picketlink.authentication.web;

import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.picketlink.Identity.Stateless;

/**
 * <p>A custom {@link org.picketlink.authentication.web.HTTPAuthenticationScheme} that knows how to extract a header from
 * the request containing a token to authenticate/re-authenticate an user.</p>
 *
 * <p>Tokens are issued by providing specific credentials for the <b>primary authentication scheme</b>. This scheme will be used
 * to validate user's credentials (eg.: username/password over BASIC) and if successful, issue a token.</p>
 *
 * <p>By default, the primary authentication scheme is {@link org.picketlink.authentication.web.BasicAuthenticationScheme}. In order to
 * change it, subclasses may override the <code>getPrimaryAuthenticationScheme</code> method.</p>
 *
 * <p>Once a token is issued, it will be written to the {@link javax.servlet.http.HttpServletResponse} using a JSON format. In order to
 * change how tokens are returned to clients, subclasses may override the <code>issueToken</code> method.</p>
 *
 * <p>The authentication is stateless, which means that security state is discarded once the request finishes. The token must be always
 * provided in order to create the security context for a request and provide access to protected resources.</p>
 *
 * <p>This scheme is used by the {@link org.picketlink.authentication.web.AuthenticationFilter}, which is configured in the web application
 * deployment descriptor(web.xml).</p>
 *
 * @author Pedro Igor
 */
@ApplicationScoped
public class TokenAuthenticationScheme implements HTTPAuthenticationScheme {

    public static final String AUTHORIZATION_TOKEN_HEADER_NAME = "Authorization";
    public static final String AUTHENTICATION_SCHEME_NAME = "Token";
    public static final String REQUIRES_AUTHENTICATION_HEADER_NAME = "WWW-Authenticate";

    @Inject
    @Stateless
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<DefaultLoginCredentials> credentialsInstance;

    @Inject
    private BasicAuthenticationScheme basicAuthenticationScheme;

    @Inject
    private Instance<Token.Provider> tokenProvider;

    @Override
    public void initialize(FilterConfig config) {
        String statelessAuthentication = config.getInitParameter(AuthenticationFilter.STATELESS_AUTHENTICATION_INIT_PARAM);

        if (statelessAuthentication == null || !Boolean.valueOf(statelessAuthentication)) {
            throw new SecurityConfigurationException("TokenAuthenticationScheme only supports a stateless authentication model. Did you forget to provide the " + AuthenticationFilter.STATELESS_AUTHENTICATION_INIT_PARAM + " init parameter to the " + AuthenticationFilter.class.getName() + "?");
        }
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        // we first try to extract the credentials using the primary authentication scheme
        getPrimaryAuthenticationScheme().extractCredential(request, creds);

        // if credentials are not present, we try to extract the token from the request.
        if (creds.getCredential() == null) {
            extractTokenFromRequest(request, creds);
        }
    }

    /**
     * <p>We use a 401 http status code to sinalize to clients that authentication is required.</p>
     *
     * <p>We only challenge clients if the authentication failed. In other words, if there is a token in the request bu it is
     * invalid.</p>
     *
     * @param request
     * @param response
     *
     * @throws java.io.IOException
     */
    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isPrimaryAuthenticationRequest()) {
            getPrimaryAuthenticationScheme().challengeClient(request, response);
        } else {
            response.setHeader(REQUIRES_AUTHENTICATION_HEADER_NAME, AUTHENTICATION_SCHEME_NAME);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isPrimaryAuthenticationRequest() && getIdentity().isLoggedIn()) {
            issueToken(request, response);
            return false;
        }

        return true;
    }

    /**
     * <p>We only initiate the authentication process if any credential is present in the request.</p>
     *
     * @param request
     * @return
     */
    @Override
    public boolean isProtected(HttpServletRequest request) {
        return true;
    }

    /**
     * <p>Returns the current {@link org.picketlink.credential.DefaultLoginCredentials} associated with the request.</p>
     *
     * @return
     */
    protected DefaultLoginCredentials getCredentials() {
        return this.credentialsInstance.get();
    }

    /**
     * <p>Returns the current {@link org.picketlink.Identity} associated with the request.</p>
     *
     * @return
     */
    protected Identity getIdentity() {
        return this.identityInstance.get();
    }

    /**
     * <p>Returns the primary {@link org.picketlink.authentication.web.HTTPAuthenticationScheme} used to validate user's credential
     * before issuing a new token..</p>
     *
     * @return
     */
    protected HTTPAuthenticationScheme getPrimaryAuthenticationScheme() {
        return this.basicAuthenticationScheme;
    }

    /**
     * <p>Extracts the token from the {@link javax.servlet.http.HttpServletRequest} and populates the given {@link org.picketlink.credential.DefaultLoginCredentials}
     * with the proper credentials.</p>
     *
     * <p>Subclasses can override this method to customize how tokens are extracted from the request and how a {@link org.picketlink.idm.credential.TokenCredential}
     * is built.</p>
     *
     * @param request
     * @param creds
     */
    protected void extractTokenFromRequest(HttpServletRequest request, DefaultLoginCredentials creds) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_TOKEN_HEADER_NAME);

        if (authorizationHeader != null && authorizationHeader.contains(AUTHENTICATION_SCHEME_NAME)) {
            String tokenValue = authorizationHeader.substring(AUTHENTICATION_SCHEME_NAME.length() + 1);

            if (tokenValue != null) {
                Token token = getTokenProvider().create(tokenValue);

                creds.setCredential(new TokenCredential(token));
            }
        }
    }

    /**
     * <p>Writes to the response the token after a successful authentication.</p>
     *
     * <p>Subclasses can override this method in order to customize how tokens are written to the response.</p>
     *
     * @param request
     * @param response
     */
    protected void issueToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = getTokenProvider().issue(getIdentity().getAccount()).getToken();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("{\"authctoken\":\"" + token + "\"}");
        } catch (Exception e) {
            throw new AuthenticationException("Could not issue token.", e);
        }
    }

    private boolean isPrimaryAuthenticationRequest() {
        return getCredentials().getCredential() != null && !TokenCredential.class.isInstance(getCredentials().getCredential());
    }

    private Token.Provider getTokenProvider() {
        if (this.tokenProvider.isAmbiguous() || this.tokenProvider.isUnsatisfied()) {
            throw new AuthenticationException("You must provide exactly one " + Token.Provider.class.getName() + " implementation.");
        }

        return this.tokenProvider.get();
    }
}
