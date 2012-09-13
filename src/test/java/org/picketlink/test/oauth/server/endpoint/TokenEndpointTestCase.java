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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.Test;
import org.picketbox.test.http.jetty.EmbeddedWebServerBase;
import org.picketlink.oauth.PicketLinkOAuthApplication;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
public class TokenEndpointTestCase extends EmbeddedWebServerBase {

    @Override
    protected void establishUserApps() {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        if (tcl == null) {
            tcl = getClass().getClassLoader();
        }

        final String WEBAPPDIR = "oauth";

        final String CONTEXTPATH = "/*";

        // for localhost:port/admin/index.html and whatever else is in the webapp directory
        final URL warUrl = tcl.getResource(WEBAPPDIR);
        final String warUrlString = warUrl.toExternalForm();

        // WebAppContext context = new WebAppContext(warUrlString, CONTEXTPATH);

        WebAppContext context = createWebApp(CONTEXTPATH, warUrlString);

        context.setContextPath("/");
        ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
        servletHolder.setInitParameter("javax.ws.rs.Application", PicketLinkOAuthApplication.class.getName());
        context.addServlet(servletHolder, "/*");

        // context.setParentLoaderPriority(true);
        server.setHandler(context);

    }

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