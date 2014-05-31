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
package org.picketlink.test.authentication.web.token;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.common.util.Base64;
import org.picketlink.test.authentication.web.AbstractAuthenticationSchemeTestCase;
import org.picketlink.test.util.ArchiveUtils;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_USERNAME;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_USER_PASSWD;

/**
 * @author pedroigor
 */
public class TokenAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (name = "default", testable = false)
    public static Archive<?> deployDefault() {
        WebArchive webArchive = create("default.war", "authc-filter-basic-token-web.xml",
            SimpleToken.class,
            SimpleTokenIDMConfiguration.class,
            SimpleTokenAuthenticationConfiguration.class,
            ProtectedServlet.class);

        ArchiveUtils.addBeansXml(webArchive, "stateless-identity-beans.xml");

        return webArchive;
    }

    @Test
    @OperateOnDeployment("default")
    public void testSuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        URL protectedServletUrl = new URL(getProtectedResourceURL().toString() + "/servlet");
        WebRequestSettings request = new WebRequestSettings(protectedServletUrl);
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Token"));

        // we first use BASIC, which is the primary authc scheme, to issue a token
        request.addAdditionalHeader("Authorization", new String("Basic " + Base64.encodeBytes(String.valueOf(DEFAULT_USERNAME + ":" + DEFAULT_USER_PASSWD).getBytes())));

        response = client.loadWebResponse(request);

        String responseData = response.getContentAsString();

        assertNotNull(responseData);
        assertNull(client.getCookieManager().getCookie(SESSION_HEADER_NAME.toUpperCase()));

        String token = responseData.substring(responseData.indexOf(":") + 2, responseData.length() - 2);

        // now we can use the token to authenticate every single request
        request.addAdditionalHeader("Authorization", new String("Token " + token));

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertTrue(response.getContentAsString().contains("User is john"));
        assertNull(client.getCookieManager().getCookie(SESSION_HEADER_NAME.toUpperCase()));

        request = new WebRequestSettings(protectedServletUrl);
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());

        authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Token"));
    }
}