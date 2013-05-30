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
package org.picketlink.test.oauth.workflow;

import org.junit.Test;
import org.picketlink.oauth.OAuthUtils;
import org.picketlink.oauth.client.ClientOAuth;
import org.picketlink.oauth.client.ClientOAuth.AccessTokenClient;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationClient;
import org.picketlink.oauth.client.ClientOAuth.RegistrationClient;
import org.picketlink.oauth.client.ClientOAuth.ResourceClient;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.messages.AccessTokenResponse;
import org.picketlink.oauth.messages.AuthorizationResponse;
import org.picketlink.oauth.messages.RegistrationResponse;
import org.picketlink.test.oauth.server.endpoint.EndpointTestBase;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test OAuth Workflow : Registration, Authorization Code and Access Token
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class OAuthWorkflowTestCase extends EndpointTestBase {
    private String registrationEndpoint = "http://localhost:11080/oauth/register";

    private String appName = "Sample Application";
    private String appURL = "http://www.example.com";
    private String appIcon = "http://www.example.com/app.ico";
    private String appDescription = "Description of a Sample App";
    private String appRedirectURL = "http://www.example.com/redirect";

    private ClientOAuth client = new ClientOAuth();

    @Test
    public void testWorkflow() throws Exception {
        // Step 1: Perform the registration
        RegistrationClient registration = client.registrationClient();
        RegistrationResponse registrationResponse = registration.setLocation(registrationEndpoint).setAppName(appName)
                .setAppURL(appURL).setAppDescription(appDescription).setAppIcon(appIcon).setAppRedirectURL(appRedirectURL)
                .build().execute();

        String clientID = registrationResponse.getClientID();
        assertNotNull(clientID);
        String clientSecret = registrationResponse.getClientSecret();
        assertNotNull(clientSecret);
        if (registrationResponse.getExpiresIn() != 3600L) {
            fail("expires");
        }
        long parsedIssuedAt = Long.parseLong(registrationResponse.getIssued());
        assertTrue(parsedIssuedAt - (new Date()).getTime() < 50L);

        String authorizationEndpoint = "http://localhost:11080/oauth/authz";
        String authzRedirectURL = "http://localhost:11080/oauth/redirect";

        AuthorizationClient authorization = client.authorizationClient();
        AuthorizationResponse authorizationResponse = authorization.setAuthorizationEndpoint(authorizationEndpoint)
                .setClientID(clientID).setAuthCodeRedirectURL(authzRedirectURL).build().execute();

        String msg = authorizationResponse.getResponseMessage();

        // Msg will contain something like http://localhost:11080/oauth/redirect?code=3c80bf2325fc6e9ef5b84ea4edc6a2ac
        int index = msg.indexOf("http");
        String subString = msg.substring(index + authzRedirectURL.length() + 1);
        Map<String, Object> map = OAuthUtils.decodeForm(subString);

        String authorizationCode = (String) map.get(OAuthConstants.CODE);
        assertNotNull(authorizationCode);

        String tokenEndpoint = "http://localhost:11080/oauth/token";
        String authCodeRedirectURL = "http://localhost:11080/oauth/register";

        // Step 3: Get Access Token on behalf of an User.
        AccessTokenClient tokenClient = client.tokenClient();
        AccessTokenResponse tokenResponse = tokenClient.setTokenEndpoint(tokenEndpoint).setAuthorizationCode(authorizationCode)
                .setAuthCodeRedirectURL(authCodeRedirectURL).setClientID(clientID).setClientSecret(clientSecret).build()
                .execute();

        String accessToken = tokenResponse.getAccessToken();
        long expiresIn = tokenResponse.getExpires();

        assertNotNull("Validate access token is null?", accessToken);
        assertNotNull("Validate expires is null?", expiresIn);

        // Now attempt the resource
        String resourceURL = "http://localhost:11080/oauth/resource";

        ResourceClient resourceClient = client.resourceClient(accessToken).setResourceURL(resourceURL);

        InputStream inputStream = resourceClient.execute();
        String responseBody = OAuthUtils.saveStreamAsString(inputStream);
        assertEquals("I am a Resource", responseBody);
    }
}