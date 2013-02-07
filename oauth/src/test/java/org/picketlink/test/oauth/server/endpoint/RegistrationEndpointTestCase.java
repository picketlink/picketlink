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

    /*@Override
    protected boolean needLDAP() {
        return true;
    }*/

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