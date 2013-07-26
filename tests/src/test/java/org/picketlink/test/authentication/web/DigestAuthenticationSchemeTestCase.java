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
import org.picketlink.authentication.web.support.HTTPDigestUtil;
import org.picketlink.idm.credential.Digest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class DigestAuthenticationSchemeTestCase extends AbstractAuthenticationSchemeTestCase {

    @Deployment (testable = false)
    public static Archive<?> deploy() {
        return deploy("authc-filter-digest-web.xml");
    }

    @Override
    void doPrepareForAuthentication(WebRequestSettings request, WebResponse response) {
        addAuthorizationHeader(request, response, "john", "passwd");
    }

    @Override
    void doPrepareForInvalidAuthentication(WebRequestSettings request, WebResponse response) {
        addAuthorizationHeader(request, response, "john", "bad_passwd");
    }

    private void addAuthorizationHeader(WebRequestSettings request, WebResponse response, String john, String passwd) {
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());

        String authenticateHeader = response.getResponseHeaderValue("WWW-Authenticate");

        assertTrue(authenticateHeader.contains("Digest realm=\"Test Realm\""));

        String[] challengeTokens = HTTPDigestUtil.quoteTokenize(authenticateHeader.toString().replace("Digest ", ""));
        Digest clientDigest = HTTPDigestUtil.digest(challengeTokens);

        request.addAdditionalHeader("Authorization", buildAuthorizationHeader(clientDigest, john, passwd));
    }

    private String buildAuthorizationHeader(Digest digest, String userName, String password) {
        String clientResponse = null;

        digest.setUsername(userName);
        digest.setMethod("GET");
        digest.setUri("/test/protected/");
        digest.setNonce(digest.getNonce());
        digest.setClientNonce(digest.getNonce());
        digest.setNonceCount("00001");

        clientResponse = HTTPDigestUtil.clientResponseValue(digest, password.toCharArray());

        StringBuilder str = new StringBuilder();

        str.append("Digest ")
                .append("username=\"").append(digest.getUsername()).append("\",")
                .append("realm=\"").append(digest.getRealm()).append("\",")
                .append("nonce=\"").append(digest.getNonce()).append("\",")
                .append("cnonce=\"").append(digest.getClientNonce()).append("\",")
                .append("uri=\"").append(digest.getUri()).append("\",")
                .append("qop=").append(digest.getQop()).append(",")
                .append("nc=").append(digest.getNonceCount()).append(",")
                .append("response=\"").append(clientResponse).append("\"");

        return str.toString();
    }

}