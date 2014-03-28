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
package org.picketlink.test.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.picketlink.json.PicketLinkJSONConstants;
import org.picketlink.json.key.JSONWebKey;
import org.picketlink.json.key.RSAKey;
import org.picketlink.json.sig.JSONWebSignature;
import org.picketlink.json.sig.JSONWebSignatureHeader;

/**
 * Unit test for JSON Security
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class JSONSecurityTestCase {
    @Test
    public void parseJSONWebKey() throws Exception {
        URL url = getClass().getClassLoader().getResource("json/jsonWebKey.json");
        assertNotNull(url);
        File file = new File(url.toURI());

        FileReader reader = new FileReader(file);
        JSONTokener tokener = new JSONTokener(reader);
        JSONObject json = new JSONObject(tokener);
        assertNotNull(json);
        System.out.println(json.toString());

        JSONWebKey webKey = new JSONWebKey();
        webKey.parse(json);
        assertEquals(2, webKey.getKeys().length());
        JSONObject keyObj = webKey.getKey("2011-04-29");
        RSAKey rsa = new RSAKey();
        rsa.parse(keyObj);

        RSAPublicKey publicKey = rsa.convertToPublicKey();
        assertNotNull(publicKey);

        RSAKey convertedKey = RSAKey.convert(publicKey);
        assertNotNull(convertedKey);
    }

    @Test
    public void testJSONWebSignature() throws Exception {
        JSONWebSignature sig = new JSONWebSignature();
        String text = "{\"iss\":\"joe\", \"exp\":1300819380, \"http://example.com/is_root\":true}";

        JSONObject payload = new JSONObject(text);

        sig.setPayload(payload);
        JSONWebSignatureHeader header = new JSONWebSignatureHeader(PicketLinkJSONConstants.COMMON.HMAC_SHA_256);
        sig.setHeader(header);

        String encodedString = sig.encode();

        System.out.println(encodedString);
        JSONWebSignature decodedString = JSONWebSignature.decode(encodedString);
        assertNotNull(decodedString);
    }
}
