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
package org.picketlink.test.oauth.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Map;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.junit.Test;
import org.picketlink.oauth.client.ClientOAuth;
import org.picketlink.oauth.client.ClientOAuth.AccessTokenClient;
import org.picketlink.oauth.client.ClientOAuth.AccessTokenResponse;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationClient;
import org.picketlink.oauth.client.ClientOAuth.AuthorizationResponse;
import org.picketlink.oauth.client.ClientOAuth.RegistrationClient;
import org.picketlink.oauth.client.ClientOAuth.RegistrationResponse;
import org.picketlink.test.oauth.server.endpoint.EndpointTestBase;

/**
 * Unit test OAuth Workflow : Registration, Authorization Code and Access Token
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class OAuthWorkflowTestCase extends EndpointTestBase {

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
    public void testWorkflow() throws Exception {
        // Step 1: Perform the registration
        RegistrationClient registration = client.registrationClient();
        RegistrationResponse registrationResponse = registration.setLocation(registrationEndpoint).setAppName(appName)
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

        AuthorizationClient authorization = client.authorizationClient();
        AuthorizationResponse authorizationResponse = authorization.setAuthorizationEndpoint(authorizationEndpoint)
                .setClientID(clientID).setAuthCodeRedirectURL(authzRedirectURL).build().execute();

        String msg = authorizationResponse.getResponseMessage();

        // Msg will contain something like http://localhost:11080/oauth/redirect?code=3c80bf2325fc6e9ef5b84ea4edc6a2ac
        int index = msg.indexOf("http");
        String subString = msg.substring(index + authzRedirectURL.length() + 1);
        Map<String, Object> map = OAuthUtils.decodeForm(subString);

        String authorizationCode = (String) map.get(OAuth.OAUTH_CODE);
        assertNotNull(authorizationCode);

        String tokenEndpoint = "http://localhost:11080/oauth/token";
        String authCodeRedirectURL = "http://localhost:11080/oauth/register";

        // Step 3: Get Access Token on behalf of an User.
        AccessTokenClient tokenClient = client.tokenClient();
        AccessTokenResponse tokenResponse = tokenClient.setTokenEndpoint(tokenEndpoint).setAuthorizationCode(authorizationCode)
                .setAuthCodeRedirectURL(authCodeRedirectURL).setClientID(clientID).setClientSecret(clientSecret).build()
                .execute();

        String accessToken = tokenResponse.getAccessToken();
        long expiresIn = tokenResponse.getExpiresIn();

        assertNotNull("Validate access token is null?", accessToken);
        assertNotNull("Validate expires is null?", expiresIn);

        // Now attempt the resource
        String resourceURL = "http://localhost:11080/oauth/resource";
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

            InputStream inputStream = null;
            if (httpURLConnection.getResponseCode() == 400) {
                inputStream = httpURLConnection.getErrorStream();
            } else {
                inputStream = httpURLConnection.getInputStream();
            }
            String responseBody = OAuthUtils.saveStreamAsString(inputStream);
            assertEquals("I am a Resource", responseBody);
        } else {
            throw new RuntimeException("Wrong url conn");
        }
    }
}