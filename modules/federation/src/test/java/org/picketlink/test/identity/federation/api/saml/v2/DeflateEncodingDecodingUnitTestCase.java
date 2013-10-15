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
package org.picketlink.test.identity.federation.api.saml.v2;

import junit.framework.TestCase;
import org.picketlink.common.util.Base64;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.util.DeflateUtil;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Unit test the DEFLATE compression encoding/decoding cycles
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 11, 2008
 */
public class DeflateEncodingDecodingUnitTestCase extends TestCase {

    public void testDeflateEncoding() throws Exception {
        AuthnRequestType authnRequest = (new SAML2Request()).createAuthnRequestType(IDGenerator.create("ID_"), "http://sp",
                "http://localhost:8080/idp", "http://sp");

        StringWriter sw = new StringWriter();
        SAML2Request request = new SAML2Request();
        request.marshall(authnRequest, sw);
        byte[] deflatedMsg = DeflateUtil.encode(sw.toString());

        String base64Request = Base64.encodeBytes(deflatedMsg, Base64.DONT_BREAK_LINES);

        base64Request = URLEncoder.encode(base64Request, "UTF-8");

        // Decode
        String urlDecodedMsg = URLDecoder.decode(base64Request, "UTF-8");
        byte[] decodedMessage = Base64.decode(urlDecodedMsg);
        InputStream is = DeflateUtil.decode(decodedMessage);
        AuthnRequestType decodedRequestType = request.getAuthnRequestType(is);

        assertNotNull(decodedRequestType);
    }
}