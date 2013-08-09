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
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.common.util.Base64;
import static org.junit.Assert.assertEquals;

/**
 * @author pedroigor
 */
public class ClientCertAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        WebArchive archive = deploy("authc-filter-client-cert-web.xml", MockClientCertAuthenticationFilter.class);

        archive.addAsResource(new File(ClientCertAuthenticationSchemeTestCase.class.getResource("/cert/servercert.txt").getFile()));

        return archive;
    }

    @Test
    public void testNotProtectedResource() throws Exception {
        WebClient client = new WebClient();
        WebResponse webResponse = client.loadWebResponse(new WebRequestSettings(getContextPath()));

        assertEquals(HttpStatus.SC_OK, webResponse.getStatusCode());
        assertEquals("Index Page", webResponse.getContentAsString());
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

        assertEquals(HttpStatus.SC_FORBIDDEN, response.getStatusCode());

        prepareAuthenticationRequest(request);

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

    private void prepareAuthenticationRequest(WebRequestSettings request) {
        X509Certificate certificate = MockClientCertAuthenticationFilter.getTestingCertificate(getClass().getClassLoader(), "cert/servercert.txt");

        request.setHttpMethod(HttpMethod.POST);

        ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new NameValuePair("x-client-cert", Base64.encodeObject(certificate)));

        request.setRequestParameters(parameters);
    }

}