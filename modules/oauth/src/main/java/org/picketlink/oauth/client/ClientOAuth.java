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

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.messages.AccessTokenRequest;
import org.picketlink.oauth.messages.AccessTokenResponse;
import org.picketlink.oauth.messages.AuthorizationRequest;
import org.picketlink.oauth.messages.AuthorizationResponse;
import org.picketlink.oauth.messages.OAuthRequest;
import org.picketlink.oauth.messages.RegistrationRequest;
import org.picketlink.oauth.messages.RegistrationResponse;
import org.picketlink.oauth.messages.ResourceAccessRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * OAuth Client
 *
 * @author anil saldhana
 * @since Sep 23, 2012
 */
public class ClientOAuth {
    protected OAuthRequest request;
    protected ObjectMapper objectMapper;

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
        objectMapper = null;
    }

    /**
     * Create a client that can make authorization code grant requests
     *
     * @author anil saldhana
     */
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
            AuthorizationRequest authorizationRequest = new AuthorizationRequest();
            authorizationRequest.setLocation(authorizationEndpoint).setClientId(clientID).setRedirectUri(authCodeRedirectURL)
                    .setResponseType(OAuthConstants.CODE);

            request = authorizationRequest;
            return this;
        }

        public AuthorizationResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }
            AuthorizationResponse response = new AuthorizationResponse();
            try {
                AuthorizationRequest authorizationRequest = (AuthorizationRequest) request;
                String locationURL = authorizationRequest.getLocation() + "?" + authorizationRequest.asQueryParams();
                URL url = new URL(locationURL);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setInstanceFollowRedirects(true);
                c.connect();
                response.setStatusCode(c.getResponseCode());
                response.setResponseMessage(c.getResponseMessage());
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
            return response;
        }
    }

    /**
     * Create a client that can make access token requests
     *
     * @author anil saldhana
     */
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
            AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
            accessTokenRequest.setLocation(tokenEndpoint).setGrantType(OAuthConstants.AUTHORIZATION_CODE)
                    .setCode(authorizationCode).setRedirectUri(authCodeRedirectURL).setClientId(clientID);
            request = accessTokenRequest;

            return this;
        }

        public AccessTokenResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }
            AccessTokenRequest accessTokenRequest = (AccessTokenRequest) request;

            String url = accessTokenRequest.getLocation();
            String body = accessTokenRequest.asQueryParams();
            InputStream is = executePost(url, body, false);

            ObjectMapper mapper = getObjectMapper();
            try {
                return mapper.readValue(is, AccessTokenResponse.class);
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
        }
    }

    /**
     * Create a client that can make client registration requests
     *
     * @author anil saldhana
     */
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
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setLocation(location);
            registrationRequest.setClientName(appName).setClientUrl(appURL).setClientDescription(appDescription)
                    .setClientRedirecturl(appRedirectURL).setClient_Icon(appIcon);

            request = registrationRequest;

            return this;
        }

        public RegistrationResponse execute() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }

            RegistrationRequest registrationRequest = (RegistrationRequest) request;
            String url = registrationRequest.getLocation();
            String body = registrationRequest.asQueryParams();

            InputStream is = executePost(url, body, false);

            ObjectMapper mapper = getObjectMapper();
            try {
                return mapper.readValue(is, RegistrationResponse.class);
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
        }

        public RegistrationResponse registerAsJSON() throws OAuthClientException {
            if (request == null) {
                throw new OAuthClientException("Request has not been built. Use build() method");
            }

            RegistrationRequest registrationRequest = (RegistrationRequest) request;
            String url = registrationRequest.getLocation();
            String body = registrationRequest.asJSON();

            InputStream is = executePost(url, body, true);

            ObjectMapper mapper = getObjectMapper();
            try {
                return mapper.readValue(is, RegistrationResponse.class);
            } catch (Exception e) {
                throw new OAuthClientException(e);
            }
        }
    }

    /**
     * Create a client that can make access requests for OAuth Resources
     *
     * @author anil saldhana
     */
    public class ResourceClient {
        private String resourceURL;

        public ResourceClient(String token) {
            ResourceAccessRequest resourceAccessRequest = new ResourceAccessRequest();
            resourceAccessRequest.setAccessToken(token);
            request = resourceAccessRequest;
        }

        public ResourceClient setResourceURL(String resourceURL) {
            this.resourceURL = resourceURL;
            return this;
        }

        public InputStream execute() throws OAuthClientException {
            ResourceAccessRequest resourceAccessRequest = (ResourceAccessRequest) request;
            String body = resourceAccessRequest.asQueryParams();
            return executePost(resourceURL, body, false);
        }
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        }
        return objectMapper;
    }

    private InputStream executePost(String endpointURL, String body, boolean isJSON) throws OAuthClientException {
        InputStream inputStream = null;
        try {
            URL resUrl = new URL(endpointURL);
            URLConnection urlConnection = resUrl.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setAllowUserInteraction(false);
                if (isJSON) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                } else {
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
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