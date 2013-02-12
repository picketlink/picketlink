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
package org.picketlink.test.oauth.server.endpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.picketlink.oauth.amber.oauth2.client.OAuthClient;
import org.picketlink.oauth.amber.oauth2.client.URLConnectionClient;
import org.picketlink.oauth.amber.oauth2.client.request.OAuthClientRequest;
import org.picketlink.oauth.amber.oauth2.client.response.OAuthAccessTokenResponse;
import org.picketlink.oauth.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.error.OAuthError;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.message.types.GrantType;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
@Ignore
public class TokenEndpointTestCase extends EndpointTestBase {

    String tokenEndpoint = "http://localhost:11080/oauth/token";
    String redirectURL = "http://localhost:11080/oauth/register";
    String clientID = "test_id";

    String appName = "Sample Application";
    String appURL = "http://www.example.com";
    String appIcon = "http://www.example.com/app.ico";
    String appDescription = "Description of a Sample App";
    String appRedirectURL = "http://www.example.com/redirect";

    String clientSecret = "someclientsecret";
    String issuedAt = "0123456789";
    Long expiresIn = 987654321l;

    String authorizationCode = "xyz";
    String username = "anil";
    String password = "test";

    @Test
    public void testSuccessfullAccessToken() throws Exception {

        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                .setCode(authorizationCode).setRedirectURI(redirectURL).setClientId(clientID).setClientSecret(clientSecret)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse response = oAuthClient.accessToken(request);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getExpiresIn());

    }

    @Test
    public void testSuccessfullAccessTokenGETMethod() throws Exception {

        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                .setCode(authorizationCode).setRedirectURI(redirectURL).setClientId(clientID).setClientSecret(clientSecret)
                .buildQueryMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthAccessTokenResponse response = oAuthClient.accessToken(request, OAuth.HttpMethod.GET);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getExpiresIn());

    }

    @Test
    public void testNoneGrantType() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(null).setClientId(clientID)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }
    }

    @Test
    public void testInvalidRequest() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setClientId(clientID).buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }
    }

    @Test
    public void testInvalidClient() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                .setCode(authorizationCode).setClientId("unknownid").setRedirectURI(redirectURL).buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }
    }

    @Test
    public void testInvalidGrantType() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint)
                .setParameter(OAuth.OAUTH_GRANT_TYPE, "unknown_grant_type").setCode(authorizationCode)
                .setRedirectURI(redirectURL).setClientId(clientID).buildBodyMessage();

        OAuthClient oAuthclient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthclient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }

    }

    @Test
    public void testInvalidCode() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.AUTHORIZATION_CODE)
                .setRedirectURI(redirectURL).setCode("unknown_code").setClientId(clientID).buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }
    }

    @Test
    public void testPasswordSuccessfullAccessToken() throws Exception {

        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.PASSWORD)
                .setClientId(clientID).setClientSecret(clientSecret).setUsername(username).setPassword(password)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request);

        assertNotNull(response.getAccessToken());
    }

    @Test
    public void testPasswordInvalidRequest() throws Exception {

        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.PASSWORD)
                .setClientId(clientID).buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_REQUEST, e.getError());
        }

    }

    @Test
    public void testPasswordInvalidClient() throws Exception {
        OAuthClientRequest request = OAuthClientRequest.tokenLocation(tokenEndpoint).setGrantType(GrantType.PASSWORD)
                .setClientId("wrong_client_id").setClientSecret(clientSecret).setUsername(username).setPassword(password)
                .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        try {
            oAuthClient.accessToken(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertEquals(OAuthError.TokenResponse.INVALID_CLIENT, e.getError());
        }
    }
}