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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author pedroigor
 */
public class FormAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (testable = false)
    public static Archive<?> deploy() {
        WebArchive archive = deploy("authc-filter-form-web.xml");

        archive.add(new StringAsset("Login Page"), "login.jsp");
        archive.add(new StringAsset("Login Error Page"), "loginError.jsp");

        return archive;
    }

    @Test
    public void testNotProtectedResource() throws Exception {
        WebClient client = new WebClient();
        WebResponse response = client.loadWebResponse(new WebRequestSettings(getContextPath()));

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Index Page", response.getContentAsString());
    }

    @Test
    public void testUnprotectedMethod() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());

        request.setHttpMethod(HttpMethod.OPTIONS);

        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Login Page", response.getContentAsString());

        prepareAuthenticationRequest(request, response, "john", "passwd");

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
    public void testUnsuccessfulAuthentication() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals("Login Page", response.getContentAsString());

        prepareAuthenticationRequest(request, response, "john", "bad_passwd");

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

    }

    private void prepareAuthenticationRequest(WebRequestSettings request, WebResponse response, String userName, String password) {
        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new NameValuePair("j_username", userName));
        parameters.add(new NameValuePair("j_password", password));

        request.setHttpMethod(HttpMethod.POST);
        request.setRequestParameters(parameters);

        try {
            request.setUrl(new URL(getProtectedResourceURL() + "/j_security_check"));
        } catch (MalformedURLException e) {
            fail(e.getMessage());
        }
    }

}