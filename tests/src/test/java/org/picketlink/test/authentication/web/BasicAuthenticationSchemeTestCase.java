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
package org.picketlink.test.authentication.web;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.picketlink.common.util.Base64;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class BasicAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (name = "default", testable = false)
    public static Archive<?> deployDefault() {
        return deploy("default.war", "authc-filter-basic-web.xml");
    }

    @Deployment (name = "force-reauthentication", testable = false)
    public static Archive<?> deployWithReauthentication() {
        return deploy("force-reauthentication.war", "authc-filter-basic-reauthc-web.xml");
    }

    @Test
    @OperateOnDeployment("default")
    public void testNotProtectedResource() throws Exception {
        WebClient client = new WebClient();
        WebResponse response = client.loadWebResponse(new WebRequestSettings(getContextPath()));

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());
    }

    @Test
    @OperateOnDeployment("default")
    public void testUnprotectedMethod() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());

        request.setHttpMethod(HttpMethod.OPTIONS);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    @OperateOnDeployment("default")
    public void testSuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Basic realm=\"Test Realm\""));

        prepareAuthenticationRequest(request, "john", "passwd");

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());

        request.setUrl(getContextPath());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());

        request.setUrl(getProtectedResourceURL());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());
    }

    @Test
    @OperateOnDeployment("default")
    public void testUnsuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        prepareAuthenticationRequest(request, "john", "bad_passwd");

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @OperateOnDeployment("force-reauthentication")
    public void testReAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertNotNull(authenticateHeader);
        assertTrue(authenticateHeader.contains("Basic realm=\"Test Realm\""));

        prepareAuthenticationRequest(request, "john", "passwd");

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Protected Page", response.getContentAsString());

        request.setUrl(getContextPath());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());

        prepareAuthenticationRequest(request, "john", "bad_passwd");

        request.setUrl(getProtectedResourceURL());
        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
    }

    private void prepareAuthenticationRequest(WebRequestSettings request, String john, String passwd) {
        request.addAdditionalHeader("Authorization", new String("Basic " + Base64.encodeBytes(String.valueOf(john + ":" + passwd).getBytes())));
    }
}