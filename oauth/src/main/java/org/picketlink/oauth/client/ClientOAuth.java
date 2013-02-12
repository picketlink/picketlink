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
package org.picketlink.oauth.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.picketlink.oauth.amber.oauth2.client.OAuthClient;
import org.picketlink.oauth.amber.oauth2.client.URLConnectionClient;
import org.picketlink.oauth.amber.oauth2.client.request.OAuthClientRequest;
import org.picketlink.oauth.amber.oauth2.client.response.OAuthAccessTokenResponse;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.types.GrantType;
import org.picketlink.oauth.amber.oauth2.common.message.types.ResponseType;
import org.picketlink.oauth.amber.oauth2.common.token.OAuthToken;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.OAuthRegistrationClient;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.request.OAuthClientRegistrationRequest;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.response.OAuthClientRegistrationResponse;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.common.OAuthRegistration;

/**
 * OAuth Client
 *
 * @author anil saldhana
 * @since Sep 23, 2012
 */
public class ClientOAuth {

    protected OAuthClientRequest request;

    /**
     * Create a client for making Authorization Code Requests
     *
     * @return
     */
    public AuthorizationClient authorizationClient() {
        clear();
        return new AuthorizationClient();
    }

    /**
     * Create a client for making Registration Requests
     *
     * @return
     */
    public RegistrationClient registrationClient() {
        clear();
        return new RegistrationClient();
    }

    /**
     * Create a client for making Access Token Requests
     *
     * @return
     */
    public AccessTokenClient tokenClient() {
        clear();
        return new AccessTokenClient();
    }

    /**
     * Create a client for making resource requests
     *
     * @param accessToken
     * @return
     */
    public ResourceClient resourceClient(String accessToken) {
        clear();
        return new ResourceClient(accessToken);
    }

    private void clear() {
        request = null;
    }

    public class AuthorizationClient {
        private String authorizationEndpoint, clientID, authCodeRedirectURL;

        public String getAuthorizationEndpoint() {
            return authorizationEndpoint;
        }

        public AuthorizationClient setAuthorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
            return this;
        }

        public String getClientID() {
            return clientID;
        }

        public AuthorizationClient setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public String getAuthCodeRedirectURL() {
            return authCodeRedirectURL;
        }

        public AuthorizationClient setAuthCodeRedirectURL(String authCodeRedirectURL) {
            this.authCodeRedirectURL = authCodeRedirectURL;
            return this;
        }

        public AuthorizationClient build() throws OAuthClientException {
            try {
                request = OAuthClientRequest.authorizationLocation(authorizationEndpoint).setClientId(clientID)
                        .setRedirectURI(authCodeRedirectURL).setResponseType(ResponseType.CODE.toString()).buildQueryMessage();
            } catch (OAuthSystemException e) {
                throw new OAuthClientException(e);
            }
            return this;
        }

        public AuthorizationResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }
            AuthorizationResponse response = new AuthorizationResponse();
            try {
                URL url = new URL(request.getLocationUri());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setInstanceFollowRedirects(true);
                c.connect();
                response.setResponseCode(c.getResponseCode());
                response.setResponseMessage(c.getResponseMessage());
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
            return response;
        }
    }

    public class AuthorizationResponse {
        private int responseCode;
        private String responseMessage;

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        public String getResponseMessage() {
            return responseMessage;
        }

        public void setResponseMessage(String responseMessage) {
            this.responseMessage = responseMessage;
        }
    }

    public class AccessTokenClient {
        private String tokenEndpoint, authorizationCode, authCodeRedirectURL, clientID, clientSecret;

        public String getTokenEndpoint() {
            return tokenEndpoint;
        }

        public AccessTokenClient setTokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        public String getAuthorizationCode() {
            return authorizationCode;
        }

        public AccessTokenClient setAuthorizationCode(String authorizationCode) {
            this.authorizationCode = authorizationCode;
            return this;
        }

        public String getAuthCodeRedirectURL() {
            return authCodeRedirectURL;
        }

        public AccessTokenClient setAuthCodeRedirectURL(String authCodeRedirectURL) {
            this.authCodeRedirectURL = authCodeRedirectURL;
            return this;
        }

        public String getClientID() {
            return clientID;
        }

        public AccessTokenClient setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public AccessTokenClient setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public AccessTokenClient build() throws OAuthClientException {
            try {
                request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                        .setCode(authorizationCode).setRedirectURI(authCodeRedirectURL).setClientId(clientID)
                        .setClientSecret(clientSecret).buildBodyMessage();
            } catch (OAuthSystemException e) {
                throw new OAuthClientException(e);
            }
            return this;
        }

        public AccessTokenResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }
            try {
                OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
                OAuthAccessTokenResponse oauthTokenresponse = oAuthClient.accessToken(request);
                return new AccessTokenResponse(oauthTokenresponse);
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
        }
    }

    public class AccessTokenResponse {
        private OAuthAccessTokenResponse delegate;

        public AccessTokenResponse(OAuthAccessTokenResponse delegate) {
            this.delegate = delegate;
        }

        public String getAccessToken() {
            return delegate.getAccessToken();
        }

        public Long getExpiresIn() {
            return delegate.getExpiresIn();
        }

        public String getRefreshToken() {
            return delegate.getRefreshToken();
        }

        public String getScope() {
            return delegate.getScope();
        }

        public CommonOAuthToken getOAuthToken() {
            CommonOAuthToken token = new CommonOAuthToken(delegate.getOAuthToken());
            return token;
        }
    }

    public class RegistrationClient {
        String location, appName, appURL, appDescription, appIcon, appRedirectURL;

        public String getLocation() {
            return location;
        }

        public RegistrationClient setLocation(String location) {
            this.location = location;
            return this;
        }

        public String getAppName() {
            return appName;
        }

        public RegistrationClient setAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public String getAppURL() {
            return appURL;
        }

        public RegistrationClient setAppURL(String appURL) {
            this.appURL = appURL;
            return this;
        }

        public String getAppDescription() {
            return appDescription;
        }

        public RegistrationClient setAppDescription(String appDescription) {
            this.appDescription = appDescription;
            return this;
        }

        public String getAppIcon() {
            return appIcon;
        }

        public RegistrationClient setAppIcon(String appIcon) {
            this.appIcon = appIcon;
            return this;
        }

        public String getAppRedirectURL() {
            return appRedirectURL;
        }

        public RegistrationClient setAppRedirectURL(String appRedirectURL) {
            this.appRedirectURL = appRedirectURL;
            return this;
        }

        public RegistrationClient build() throws OAuthClientException {
            try {
                request = OAuthClientRegistrationRequest.location(location, OAuthRegistration.Type.PUSH).setName(appName)
                        .setUrl(appURL).setDescription(appDescription).setIcon(appIcon).setRedirectURL(appRedirectURL)
                        .buildJSONMessage();
            } catch (OAuthSystemException e) {
                throw new OAuthClientException(e);
            }
            return this;
        }

        public RegistrationResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }
            try {
                OAuthRegistrationClient oauthclient = new OAuthRegistrationClient(new URLConnectionClient());
                OAuthClientRegistrationResponse response = oauthclient.clientInfo(request);
                return new RegistrationResponse(response);
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
        }
    }

    public class RegistrationResponse {
        private OAuthClientRegistrationResponse delegate;

        public RegistrationResponse(OAuthClientRegistrationResponse delegate) {
            this.delegate = delegate;
        }

        public String getClientId() {
            return delegate.getClientId();
        }

        public String getClientSecret() {
            return delegate.getClientSecret();
        }

        public String getIssuedAt() {
            return delegate.getIssuedAt();
        }

        public Long getExpiresIn() {
            return delegate.getExpiresIn();
        }
    }

    public class CommonOAuthToken {
        private OAuthToken delegate;

        public CommonOAuthToken(OAuthToken delegate) {
            this.delegate = delegate;
        }

        public String getAccessToken() {
            return delegate.getAccessToken();
        }

        public Long getExpiresIn() {
            return delegate.getExpiresIn();
        }

        public String getRefreshToken() {
            return delegate.getRefreshToken();
        }

        public String getScope() {
            return delegate.getScope();
        }
    }

    public class ResourceClient {
        private String accessToken;

        public ResourceClient(String token) {
            this.accessToken = token;
        }

        public InputStream execute(String resourceURL) throws OAuthClientException {
            InputStream inputStream = null;
            try {
                URL resUrl = new URL(resourceURL);
                URLConnection urlConnection = resUrl.openConnection();
                if (urlConnection instanceof HttpURLConnection) {
                    String body = "access_token=" + accessToken;

                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setAllowUserInteraction(false);
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpURLConnection.setRequestProperty("Content-Length", Integer.toString(body.length()));
                    OutputStream ost = httpURLConnection.getOutputStream();
                    PrintWriter pw = new PrintWriter(ost);
                    pw.print(body);
                    pw.flush();
                    pw.close();

                    if (httpURLConnection.getResponseCode() == 400) {
                        inputStream = httpURLConnection.getErrorStream();
                    } else {
                        inputStream = httpURLConnection.getInputStream();
                    }
                } else {
                    throw new RuntimeException("Wrong url conn");
                }
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
            return inputStream;
        }
    }
}