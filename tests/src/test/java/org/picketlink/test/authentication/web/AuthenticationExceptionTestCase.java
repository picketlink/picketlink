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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.picketlink.test.authentication.web.FormAuthenticationSchemeTestCase.prepareAuthenticationRequest;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_DISABLED_USERNAME;
import static org.picketlink.test.authentication.web.Resources.DEFAULT_USER_PASSWD;

/**
 * @author pedroigor
 */
public class AuthenticationExceptionTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment(testable = false)
    public static Archive<?> deploy() {
        WebArchive archive = create("teste.war", "authc-filter-form-web.xml");

        archive.add(new StringAsset("Login Page"), "login.jsp");
        archive.add(new StringAsset("Login Error Page"), "loginError.jsp");

        return archive;
    }

    @Test
    public void testLockedAccountException() throws Exception {
        WebClient client = new WebClient();
        WebRequestSettings request = new WebRequestSettings(getProtectedResourceURL());
        WebResponse response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Login Page", response.getContentAsString());

        prepareAuthenticationRequest(request, getProtectedResourceURL(), DEFAULT_DISABLED_USERNAME, DEFAULT_USER_PASSWD);

        response = client.loadWebResponse(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        assertEquals("Login Page", response.getContentAsString());
    }
}