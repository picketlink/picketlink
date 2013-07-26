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

import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.picketlink.common.util.Base64;
import static org.junit.Assert.assertEquals;

/**
 * @author pedroigor
 */
public class BasicAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (testable = false)
    public static Archive<?> deploy() {
        return deploy("authc-filter-basic-web.xml");
    }

    void doPrepareForAuthentication(WebRequestSettings request, WebResponse response) {
        addAuthorizationHeader(request, response, "john", "passwd");
    }

    @Override
    void doPrepareForInvalidAuthentication(WebRequestSettings request, WebResponse response) {
        addAuthorizationHeader(request, response, "john", "bad_passwd");
    }

    private void addAuthorizationHeader(WebRequestSettings request, WebResponse response, String john, String passwd) {
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
        assertEquals("Basic realm=\"Test Realm\"", response.getResponseHeaderValue("WWW-Authenticate"));

        request.addAdditionalHeader("Authorization", new String("Basic " + Base64.encodeBytes(String.valueOf(john + ":" + passwd).getBytes())));
    }
}