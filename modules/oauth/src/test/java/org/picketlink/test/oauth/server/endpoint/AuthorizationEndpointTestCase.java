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

import org.junit.Test;
import org.picketlink.oauth.OAuthUtils;
import org.picketlink.oauth.client.ClientOAuth;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationClient;
import org.picketlink.oauth.client.ClientOAuth.RegistrationClient;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.messages.AuthorizationResponse;
import org.picketlink.oauth.messages.RegistrationResponse;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
public class AuthorizationEndpointTestCase extends EndpointTestBase {

    private String registrationEndpoint = "http://localhost:11080/oauth/register";

    private String appName = "Sample Application";
    private String appURL = "http://www.example.com";
    private String appIcon = "http://www.example.com/app.ico";
    private String appDescription = "Description of a Sample App";
    private String appRedirectURL = "http://www.example.com/redirect";

    private ClientOAuth client = new ClientOAuth();

    @Test
    public void testEndUserAuthorization() throws Exception {
        // Step 1: Perform the registration
        RegistrationClient registrationClient = client.registrationClient();
        RegistrationResponse registrationResponse = registrationClient.setLocation(registrationEndpoint).setAppName(appName)
                .setAppURL(appURL).setAppDescription(appDescription).setAppIcon(appIcon).setAppRedirectURL(appRedirectURL)
                .build().registerAsJSON();

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
        String redirectURL = "http://localhost:11080/oauth/redirect";

        AuthorizationClient authorizationClient = client.authorizationClient();
        AuthorizationResponse authorizationResponse = authorizationClient.setAuthorizationEndpoint(authorizationEndpoint)
                .setClientID(clientID).setAuthCodeRedirectURL(authzRedirectURL).build().execute();

        String msg = authorizationResponse.getResponseMessage();
        // Msg will contain http://localhost:11080/oauth/redirect?code=3c80bf2325fc6e9ef5b84ea4edc6a2ac
        System.out.println(msg);
        int index = msg.indexOf("http");
        System.out.println("Received message=" + msg);
        String subString = msg.substring(index + redirectURL.length() + 1);
        Map<String, Object> map = OAuthUtils.decodeForm(subString);

        assertNotNull(map.get(OAuthConstants.CODE));
    }
}