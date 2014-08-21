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
package org.picketlink.http.internal.authentication.schemes;

import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.config.http.TokenAuthenticationConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.picketlink.idm.credential.Token.Builder.create;
import static org.picketlink.idm.credential.Token.Consumer;
import static org.picketlink.idm.credential.Token.Provider;

/**
 * <p>A custom {@link org.picketlink.http.authentication.HttpAuthenticationScheme} that knows how to extract a header from
 * the request containing a token to authenticate/re-authenticate an user.</p>
 *
 * <p>Tokens are issued by providing specific credentials for the <b>primary authentication scheme</b>. This scheme will be used
 * to validate user's credentials (eg.: username/password over BASIC) and if successful, issue a token.</p>
 *
 * <p>By default, the primary authentication scheme is {@link org.picketlink.http.internal.authentication.schemes.BasicAuthenticationScheme}. In order to
 * change it, subclasses may override the <code>getPrimaryAuthenticationScheme</code> method.</p>
 *
 * <p>Once a token is issued, it will be written to the {@link javax.servlet.http.HttpServletResponse} using a JSON format. In order to
 * change how tokens are returned to clients, subclasses may override the {@link org.picketlink.http.internal.authentication.schemes.TokenAuthenticationScheme#writeToken(String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.</p>
 *
 * <p>This scheme is used by the {@link org.picketlink.http.internal.SecurityFilter}, which is configured in the web application
 * deployment descriptor (web.xml).</p>
 *
 * @author Pedro Igor
 */
public class TokenAuthenticationScheme implements HttpAuthenticationScheme<TokenAuthenticationConfiguration> {

    public static final String AUTHORIZATION_TOKEN_HEADER_NAME = "Authorization";
    public static final String AUTHENTICATION_SCHEME_NAME = "Token";
    public static final String REQUIRES_AUTHENTICATION_HEADER_NAME = "WWW-Authenticate";

    @Inject
    private Instance<Identity> identityInstance;

    @Inject
    private Instance<DefaultLoginCredentials> credentialsInstance;

    @Inject
    private BasicAuthenticationScheme basicAuthenticationScheme;

    @Inject
    private Instance<Provider<?>> tokenProvider;

    @Inject
    private Instance<Consumer<?>> tokenConsumer;

    @Override
    public void initialize(TokenAuthenticationConfiguration config) {
    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        // we first try to extract the credentials using the primary authentication scheme
        getPrimaryAuthenticationScheme().extractCredential(request, creds);

        // if credentials are not present, we try to extract the token from the request.
        if (creds.getCredential() == null) {
            String extractedToken = extractTokenFromRequest(request);

            if (extractedToken != null) {
                creds.setCredential(createCredential(extractedToken));
            }
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
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (isPrimaryAuthenticationRequest()) {
                getPrimaryAuthenticationScheme().challengeClient(request, response);
            } else {
                response.setHeader(REQUIRES_AUTHENTICATION_HEADER_NAME, AUTHENTICATION_SCHEME_NAME);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not challeng client credentials.", e);
        }
    }

    @Override
    public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (isPrimaryAuthenticationRequest() && getIdentity().isLoggedIn()) {
            String issuedToken = issueToken(request, response);

            writeToken(issuedToken, request, response);
        }
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
     * <p>Returns the primary {@link org.picketlink.http.authentication.HttpAuthenticationScheme} that will be used to validate user's
     * credential before issuing a new token.</p>
     *
     * <p>Default authentication scheme is {@link org.picketlink.http.internal.authentication.schemes.BasicAuthenticationScheme}.</p>
     *
     * @return
     */
    protected HttpAuthenticationScheme getPrimaryAuthenticationScheme() {
        return this.basicAuthenticationScheme;
    }

    /**
     * <p>Extracts the token from the {@link javax.servlet.http.HttpServletRequest}.</p>
     * <p>Subclasses can override this method to customize how tokens are extracted from the request.</p>
     *
     *  @param request
     *  @return A String representing the token extracted from the request.
     */
    protected String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_TOKEN_HEADER_NAME);

        if (authorizationHeader != null && authorizationHeader.contains(AUTHENTICATION_SCHEME_NAME)) {
            return authorizationHeader.substring(AUTHENTICATION_SCHEME_NAME.length() + 1);
        }

        return null;
    }

    /**
     * <p>Creates a {@link org.picketlink.idm.credential.TokenCredential} using the token previously extracted from the request.</p>
     *
     * <p>Subclasses can override this method to customize how the credential is created. Defaults to an instance of {@link org.picketlink.idm.credential.TokenCredential}.</p>
     *
     * @param extractedToken The token previously extracted from the request.
     * @return
     */
    protected TokenCredential createCredential(String extractedToken) {
        Token token;
        Provider tokenProvider = getTokenProvider();

        if (tokenProvider != null) {
            token = create(getTokenProvider().getTokenType().getName(), extractedToken);
        } else {
            Consumer tokenConsumer = getTokenConsumer();

            if (tokenConsumer == null) {
                throw new AuthenticationException("You must provide a " + Provider.class.getName() + " or " + Consumer.class.getName() + ".");
            }

            token = create(getTokenConsumer().getTokenType().getName(), extractedToken);
        }

        return new TokenCredential(token);
    }

    /**
     * <p>Issues a token for a previously authenticated {@link org.picketlink.idm.model.Account} using the
     * configured {@link org.picketlink.idm.credential.Token.Provider}.</p>
     *
     * @param request
     * @param response
     */
    protected String issueToken(HttpServletRequest request, HttpServletResponse response) {
        Provider tokenProvider = getTokenProvider();

        if (tokenProvider == null) {
            throw new AuthenticationException("No " + Provider.class.getName() + " was found.");
        }

        return tokenProvider.issue(getIdentity().getAccount()).getToken();
    }

    /**
     * <p>Writes the <code>issuedToken</code> to the {@link javax.servlet.http.HttpServletResponse}.</p>
     *
     * @param issuedToken
     * @param request
     * @param response
     */
    protected void writeToken(String issuedToken, HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_OK);

            PrintWriter writer = response.getWriter();

            writer.print("{\"authctoken\":\"" + issuedToken + "\"}");

            writer.flush();
        } catch (Exception e) {
            throw new AuthenticationException("Could not write token to response.", e);
        }
    }

    protected Provider getTokenProvider() {
        if (this.tokenProvider.isAmbiguous()) {
            throw new AuthenticationException("You must provide exactly one " + Provider.class.getName() + " implementation.");
        }

        if (!this.tokenProvider.isUnsatisfied()) {
            return this.tokenProvider.get();
        }

        return null;
    }

    protected Consumer getTokenConsumer() {
        if (this.tokenConsumer.isAmbiguous()) {
            throw new AuthenticationException("You must provide exactly one " + Consumer.class.getName() + " implementation.");
        }

        if (!this.tokenConsumer.isUnsatisfied()) {
            return this.tokenConsumer.get();
        }

        return null;
    }

    private boolean isPrimaryAuthenticationRequest() {
        return getCredentials().getCredential() != null && !TokenCredential.class.isInstance(getCredentials().getCredential());
    }
}
