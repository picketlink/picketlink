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
package org.picketlink.test.oauth.server.endpoint;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Map;

import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.utils.OAuthUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.oauth.client.ClientOAuth;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationClient;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationResponse;
import org.picketlink.oauth.client.ClientOAuth.RegistrationClient;
import org.picketlink.oauth.client.ClientOAuth.RegistrationResponse;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
@Ignore
public class AuthorizationEndpointTestCase extends EndpointTestBase {

    @Override
    protected boolean needLDAP() {
        return true;
    }

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
                .build().execute();

        String clientID = registrationResponse.getClientId();
        assertNotNull(clientID);
        String clientSecret = registrationResponse.getClientSecret();
        assertNotNull(clientSecret);
        if (registrationResponse.getExpiresIn() != 3600L) {
            fail("expires");
        }
        long parsedIssuedAt = Long.parseLong(registrationResponse.getIssuedAt());
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
        String subString = msg.substring(index + redirectURL.length() + 1);
        Map<String, Object> map = OAuthUtils.decodeForm(subString);

        assertNotNull(map.get(OAuth.OAUTH_CODE));
    }
}