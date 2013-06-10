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

package org.picketlink.social.standalone.google;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import org.apache.log4j.Logger;
import org.picketlink.social.standalone.oauth.OAuthConstants;
import org.picketlink.social.standalone.oauth.SocialException;
import org.picketlink.social.standalone.oauth.SocialExceptionCode;

/**
 * Processor to perform Google+ interaction with usage of OAuth2
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GoogleProcessor {

    protected static Logger log = Logger.getLogger(GoogleProcessor.class);

    private final String returnURL;
    private final String clientID;
    private final String clientSecret;
    private final Set<String> scopes = new HashSet<String>();
    private final String accessType;
    private final String applicationName;


    /** Default HTTP transport to use to make HTTP requests. */
    private final HttpTransport TRANSPORT = new NetHttpTransport();


    /** Default JSON factory to use to deserialize JSON. */
    private final JacksonFactory JSON_FACTORY = new JacksonFactory();


    /** Secure random to generate random states */
    private final SecureRandom secureRandom;


    public GoogleProcessor(String clientID,
                           String clientSecret,
                           String returnURL,
                           String accessType,
                           String applicationName,
                           String randomAlgorithm,
                           String scope) {
        checkNotNullParam("clientID", clientID);
        checkNotNullParam("clientSecret", clientSecret);
        checkNotNullParam("returnURL", returnURL);
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.returnURL = returnURL;
        this.accessType = accessType != null ? accessType : "offline";
        this.applicationName = applicationName != null ? applicationName : "someApp";
        if (randomAlgorithm == null) {
            randomAlgorithm = "SHA1PRNG";
        }
        try {
            this.secureRandom = SecureRandom.getInstance(randomAlgorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalArgumentException("Can't create secureRandom", nsae);
        }

        if (scope == null) {
            scope = "https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";
        }
        addScopesFromString(scope, this.scopes);

        if (log.isTraceEnabled()) {
            log.trace("configuration: clientId=" + clientID +
                    ", clientSecret=" + clientSecret +
                    ", returnURL=" + returnURL +
                    ", scope=" + scopes +
                    ", accessType=" + accessType +
                    ", applicationName=" + applicationName +
                    ", randomAlgorithm=" + randomAlgorithm);
        }
    }


    public InteractionState processOAuthInteraction(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, SocialException {
        return processOAuthInteractionImpl(httpRequest, httpResponse, this.scopes);
    }


    protected InteractionState processOAuthInteractionImpl(HttpServletRequest request, HttpServletResponse response, Set<String> scopes)
            throws IOException, SocialException {
        HttpSession session = request.getSession();
        String state = (String) session.getAttribute(GoogleConstants.ATTRIBUTE_AUTH_STATE);

        // Very initial request to portal
        if (state == null || state.isEmpty()) {

            return initialInteraction(request, response, scopes);
        } else if (state.equals(InteractionState.State.AUTH.name())) {
            GoogleTokenResponse tokenResponse = obtainAccessToken(request);
            GoogleAccessTokenContext accessTokenContext = validateTokenAndUpdateScopes(new GoogleAccessTokenContext(tokenResponse, ""));

            // Clear session attributes
            session.removeAttribute(GoogleConstants.ATTRIBUTE_AUTH_STATE);
            session.removeAttribute(GoogleConstants.ATTRIBUTE_VERIFICATION_STATE);

            return new InteractionState(InteractionState.State.FINISH, accessTokenContext);
        }

        // Likely shouldn't happen...
        return new InteractionState(InteractionState.State.valueOf(state), null);
    }


    protected InteractionState initialInteraction(HttpServletRequest request, HttpServletResponse response, Set<String> scopes) throws IOException {
        String verificationState = generateSecureString();
        String authorizeUrl = new GoogleAuthorizationCodeRequestUrl(clientID, returnURL, scopes).
                setState(verificationState).setAccessType(accessType).build();
        if (log.isTraceEnabled()) {
            log.trace("Starting OAuth2 interaction with Google+");
            log.trace("URL to send to Google+: " + authorizeUrl);
        }

        HttpSession session = request.getSession();
        session.setAttribute(GoogleConstants.ATTRIBUTE_VERIFICATION_STATE, verificationState);
        session.setAttribute(GoogleConstants.ATTRIBUTE_AUTH_STATE, InteractionState.State.AUTH.name());
        response.sendRedirect(authorizeUrl);
        return new InteractionState(InteractionState.State.AUTH, null);
    }


    protected GoogleTokenResponse obtainAccessToken(HttpServletRequest request) throws SocialException {
        HttpSession session = request.getSession();
        String stateFromSession = (String)session.getAttribute(GoogleConstants.ATTRIBUTE_VERIFICATION_STATE);
        String stateFromRequest = request.getParameter(OAuthConstants.STATE_PARAMETER);
        if (stateFromSession == null || stateFromRequest == null || !stateFromSession.equals(stateFromRequest)) {
            throw new SocialException(SocialExceptionCode.INVALID_STATE, "Validation of state parameter failed. stateFromSession="
                    + stateFromSession + ", stateFromRequest=" + stateFromRequest);
        }

        // Check if user didn't permit scope
        String error = request.getParameter(OAuthConstants.ERROR_PARAMETER);
        if (error != null) {
            if (OAuthConstants.ERROR_ACCESS_DENIED.equals(error)) {
                throw new SocialException(SocialExceptionCode.USER_DENIED_SCOPE, error);
            } else {
                throw new SocialException(SocialExceptionCode.UNKNOWN_ERROR, error);
            }
        } else {
            String code = request.getParameter(OAuthConstants.CODE_PARAMETER);

            GoogleTokenResponse tokenResponse;
            try {
                tokenResponse = new GoogleAuthorizationCodeTokenRequest(TRANSPORT, JSON_FACTORY, clientID,
                    clientSecret, code, returnURL).execute();
            } catch (IOException ioe) {
                throw new SocialException(SocialExceptionCode.INVALID_CLIENT, "Error when obtaining access token from Google: " + ioe.getMessage(), ioe);
            }

            if (log.isTraceEnabled()) {
                log.trace("Successfully obtained accessToken from google: " + tokenResponse);
            }

            return tokenResponse;
        }
    }


    public GoogleAccessTokenContext validateTokenAndUpdateScopes(GoogleAccessTokenContext accessTokenContext) throws SocialException {
        GoogleRequest<Tokeninfo> googleRequest = new GoogleRequest<Tokeninfo>() {

            @Override
            protected Tokeninfo invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
                GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
                Oauth2 oauth2 = getOAuth2Instance(accessTokenContext);
                GoogleCredential credential = getGoogleCredential(tokenData);
                return oauth2.tokeninfo().setAccessToken(credential.getAccessToken()).execute();
            }

            @Override
            protected SocialException createException(IOException cause) {
                if (cause instanceof HttpResponseException) {
                    return new SocialException(SocialExceptionCode.ACCESS_TOKEN_ERROR,
                            "Error when obtaining tokenInfo: " + cause.getMessage(), cause);
                } else {
                    return new SocialException(SocialExceptionCode.IO_ERROR,
                            "IO Error when obtaining tokenInfo: " + cause.getMessage(), cause);
                }
            }

        };
        Tokeninfo tokenInfo = googleRequest.executeRequest(accessTokenContext, this);

        // If there was an error in the token info, abort.
        if (tokenInfo.containsKey(OAuthConstants.ERROR_PARAMETER)) {
            throw new SocialException(SocialExceptionCode.ACCESS_TOKEN_ERROR, "Error during token validation: " + tokenInfo.get("error").toString());
        }

        if (!tokenInfo.getIssuedTo().equals(clientID)) {
            throw new SocialException(SocialExceptionCode.ACCESS_TOKEN_ERROR, "Token's client ID does not match app's. clientID from tokenINFO: " + tokenInfo.getIssuedTo());
        }

        if (log.isTraceEnabled()) {
            log.trace("Successfully validated accessToken from google: " + tokenInfo);
        }

        return new GoogleAccessTokenContext(accessTokenContext.getTokenData(), tokenInfo.getScope());
    }


    public Userinfo obtainUserInfo(GoogleAccessTokenContext accessTokenContext) throws SocialException {
        final Oauth2 oauth2 = getOAuth2Instance(accessTokenContext);

        GoogleRequest<Userinfo> googleRequest = new GoogleRequest<Userinfo>() {

            @Override
            protected Userinfo invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
                return oauth2.userinfo().v2().me().get().execute();
            }

            @Override
            protected SocialException createException(IOException cause) {
                if (cause instanceof HttpResponseException) {
                    return new SocialException(SocialExceptionCode.ACCESS_TOKEN_ERROR,
                            "Error when obtaining userInfo: " + cause.getMessage(), cause);
                } else {
                    return new SocialException(SocialExceptionCode.IO_ERROR,
                            "IO Error when obtaining userInfo: " + cause.getMessage(), cause);
                }
            }

        };
        Userinfo uinfo = googleRequest.executeRequest(accessTokenContext, this);

        if (log.isTraceEnabled()) {
            log.trace("Successfully obtained userInfo from google: " + uinfo);
        }

        return uinfo;
    }


    public Oauth2 getOAuth2Instance(GoogleAccessTokenContext accessTokenContext) {
        GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
        GoogleCredential credential = getGoogleCredential(tokenData);
        return new Oauth2.Builder(TRANSPORT, JSON_FACTORY, credential).setApplicationName(applicationName).build();
    }


    private GoogleCredential getGoogleCredential(GoogleTokenResponse tokenResponse) {
        return new GoogleCredential.Builder()
                .setJsonFactory(JSON_FACTORY)
                .setTransport(TRANSPORT)
                .setClientSecrets(clientID, clientSecret).build()
                .setFromTokenResponse(tokenResponse);
    }

    /**
     * Revoke existing access token, so it won't be valid anymore. Application will be removed from list of existing apps of this user
     * on Google
     *
     * @param accessTokenContext
     * @throws SocialException
     */
    public void revokeToken(GoogleAccessTokenContext accessTokenContext) throws SocialException {
        GoogleRequest<Void> googleRequest = new GoogleRequest<Void>() {

            @Override
            protected Void invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException {
                GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
                TRANSPORT.createRequestFactory()
                        .buildGetRequest(new GenericUrl("https://accounts.google.com/o/oauth2/revoke?token=" + tokenData.getAccessToken())).execute();
                if (log.isTraceEnabled()) {
                    log.trace("Revoked token " + tokenData);
                }
                return null;
            }

            @Override
            protected SocialException createException(IOException cause) {
                return new SocialException(SocialExceptionCode.TOKEN_REVOCATION_FAILED, "Error when revoking token", cause);
            }

        };
        googleRequest.executeRequest(accessTokenContext, this);
    }

    /**
     * Refresh existing access token. Parameter must have attached refreshToken. New refreshed accessToken will be updated to this
     * instance of accessTokenContext
     *
     * @param accessTokenContext with refreshToken attached
     */
    public void refreshToken(GoogleAccessTokenContext accessTokenContext) {
        GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
        if (tokenData.getRefreshToken() == null) {
            throw new SocialException(SocialExceptionCode.GOOGLE_ERROR, "Given GoogleTokenResponse does not contain refreshToken");
        }

        try {
            GoogleRefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(TRANSPORT, JSON_FACTORY, tokenData.getRefreshToken(),
                    this.clientID, this.clientSecret);
            GoogleTokenResponse refreshed = refreshTokenRequest.execute();

            // Update only 'accessToken' with new value
            tokenData.setAccessToken(refreshed.getAccessToken());

            if (log.isTraceEnabled()) {
                log.trace("AccessToken refreshed successfully with value " + refreshed.getAccessToken());
            }
        } catch (IOException ioe) {
            throw new SocialException(SocialExceptionCode.GOOGLE_ERROR, ioe.getMessage(), ioe);
        }
    }


    private void addScopesFromString(String scope, Set<String> scopes) {
        String[] scopes2 = scope.split(" ");
        for (String current : scopes2) {
            scopes.add(current);
        }
    }

    private void checkNotNullParam(String paramName, String paramValue) {
        if (paramValue == null) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' must be not null");
        }
    }

    protected String generateSecureString() {
        return String.valueOf(secureRandom.nextLong());
    }
}
