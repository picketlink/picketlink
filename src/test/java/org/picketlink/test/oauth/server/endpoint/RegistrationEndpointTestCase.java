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

import java.net.URL;
import java.util.Date;

import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.ext.dynamicreg.client.OAuthRegistrationClient;
import org.apache.amber.oauth2.ext.dynamicreg.client.request.OAuthClientRegistrationRequest;
import org.apache.amber.oauth2.ext.dynamicreg.client.response.OAuthClientRegistrationResponse;
import org.apache.amber.oauth2.ext.dynamicreg.common.OAuthRegistration;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.junit.After;
import org.junit.Test;
import org.picketbox.test.http.jetty.EmbeddedWebServerBase;
import org.picketbox.test.ldap.LDAPTestUtil;
import org.picketlink.oauth.PicketLinkOAuthApplication;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;

/**
 * Unit test the {@link AuthorizationEndpoint}
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
public class RegistrationEndpointTestCase extends EmbeddedWebServerBase {
    protected LDAPTestUtil testUtil = null;

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (testUtil != null) {
            testUtil.tearDown();
        }
    }

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

        // Deal with LDAP Server
        try {
            testUtil = new LDAPTestUtil();
            testUtil.setup();
            testUtil.createBaseDN("jboss", "dc=jboss,dc=org");
            testUtil.importLDIF("ldap/users.ldif");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    String registrationEndpoint = "http://localhost:11080/oauth/register";
    String redirectURL = "http://localhost:11080/oauth/register";
    String clientID = "test_id";

    String APP_NAME = "Sample Application";
    String APP_URL = "http://www.example.com";
    String APP_ICON = "http://www.example.com/app.ico";
    String APP_DESCRIPTION = "Description of a Sample App";
    String APP_REDIRECT_URI = "http://www.example.com/redirect";

    String CLIENT_ID = "someclientid";
    String CLIENT_SECRET = "someclientsecret";
    String ISSUED_AT = "0123456789";
    Long EXPIRES_IN = 987654321l;

    @Test
    public void testRegistration() throws Exception {

        OAuthClientRequest request = OAuthClientRegistrationRequest.location(registrationEndpoint, OAuthRegistration.Type.PUSH)
                .setName(APP_NAME).setUrl(APP_URL).setDescription(APP_DESCRIPTION).setIcon(APP_ICON)
                .setRedirectURL(APP_REDIRECT_URI).buildJSONMessage();

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
                .setName(APP_NAME).setUrl(APP_URL).setDescription(APP_DESCRIPTION).setIcon(APP_ICON)
                .setRedirectURL(APP_REDIRECT_URI).buildBodyMessage();

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