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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Test;
import org.picketlink.oauth.amber.oauth2.client.URLConnectionClient;
import org.picketlink.oauth.amber.oauth2.client.request.OAuthClientRequest;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.OAuthRegistrationClient;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.request.OAuthClientRegistrationRequest;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.client.response.OAuthClientRegistrationResponse;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.common.OAuthRegistration;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
public class RegistrationEndpointTestCase extends EndpointTestBase {

    /*
     * @Override protected boolean needLDAP() { return true; }
     */

    private String registrationEndpoint = "http://localhost:11080/oauth/register";

    private String appName = "Sample Application";
    private String appURL = "http://www.example.com";
    private String appIcon = "http://www.example.com/app.ico";
    private String appDescription = "Description of a Sample App";
    private String appRedirectURL = "http://www.example.com/redirect";

    @Test
    public void testRegistration() throws Exception {
        OAuthClientRequest request = OAuthClientRegistrationRequest.location(registrationEndpoint, OAuthRegistration.Type.PUSH)
                .setName(appName).setUrl(appURL).setDescription(appDescription).setIcon(appIcon).setRedirectURL(appRedirectURL)
                .buildJSONMessage();

        OAuthRegistrationClient oauthclient = new OAuthRegistrationClient(new URLConnectionClient());
        OAuthClientRegistrationResponse response = oauthclient.clientInfo(request);

        String clientID = response.getClientId();
        assertNotNull(clientID);
        String clientSecret = response.getClientSecret();
        assertNotNull(clientSecret);
        if (response.getExpiresIn() != 3600L) {
            fail("expires");
        }
        long parsedIssuedAt = Long.parseLong(response.getIssuedAt());
        assertTrue(parsedIssuedAt - (new Date()).getTime() < 50L);
    }

    @Test
    public void testInvalidType() throws Exception {
        OAuthClientRequest request = OAuthClientRegistrationRequest.location(registrationEndpoint, "unknown_type")
                .setName(appName).setUrl(appURL).setDescription(appDescription).setIcon(appIcon).setRedirectURL(appRedirectURL)
                .buildBodyMessage();

        OAuthRegistrationClient oauthclient = new OAuthRegistrationClient(new URLConnectionClient());
        try {
            @SuppressWarnings("unused")
            OAuthClientRegistrationResponse response = oauthclient.clientInfo(request);
            fail("exception expected");
        } catch (OAuthProblemException e) {
            assertNotNull(e.getError());
        }
    }
}