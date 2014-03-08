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

import java.security.PublicKey;

import org.json.JSONObject;
import org.junit.Test;
import org.picketlink.json.token.JSONWebToken;

/**
 * Unit test the API for JWT
 *
 * @author anil saldhana
 * @since Jul 31, 2012
 */
public class JSONWebTokenAPITestCase {

    /**
     * Test the JWT API for Plain Text usecase
     *
     * @throws Exception
     */
    @Test
    public void testPlainTextJWTAPI() throws Exception {
        String plainText = "{\"data\":\"Welcome to the world of AES\"}";

        JSONWebToken jwt = new JSONWebToken();
        jwt.setData(new JSONObject(plainText));

        // Let us create the header
        JSONObject header = new JSONObject();
        header.put("alg", "none");

        jwt.setHeader(header);

        String encodedJWT = jwt.encode();
        System.out.println(encodedJWT);

        // Let us decode
        jwt = new JSONWebToken();
        jwt.load(encodedJWT);

        assertEquals(plainText, jwt.getData().toString());
    }

    /**
     * Test the JWT API for signature use case
     *
     * @throws Exception
     */
    @Test
    public void testJWSAPI() throws Exception {
        String headerStr = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
        String text = "{\"iss\":\"joe\",\"exp\":1300819380,\"http://example.com/is_root\":true}";

        JSONWebToken jwt = new JSONWebToken();
        jwt.setData(new JSONObject(text));

        // Let us create the header
        JSONObject header = new JSONObject(headerStr);
        jwt.setHeader(header);

        String encodedJWT = jwt.encode();
        System.out.println(encodedJWT);

        // Let us decode
        jwt = new JSONWebToken();
        jwt.load(encodedJWT);

        assertEquals("joe", jwt.getData().getString("iss"));
    }

    /**
     * Test the JWT API for encryption use case
     *
     * @throws Exception
     */
    @Test
    public void testJWEAPI() throws Exception {
        String headerStr = "{\"alg\":\"RSA1_5\",\"enc\":\"A128CBC\",\"int\":\"HS256\",\"iv\":\"48V1_ALb6US04U3b\"}";
        String text = "{\"iss\":\"joe\",\"exp\":1300819380,\"http://example.com/is_root\":true}";

        JSONWebEncryptionTestCase jweTest = new JSONWebEncryptionTestCase();
        PublicKey publicKey = jweTest.getPublicKey();

        JSONWebToken jwt = new JSONWebToken();
        jwt.setData(new JSONObject(text));
        jwt.setPublicKey(publicKey);

        // Let us create the header
        JSONObject header = new JSONObject(headerStr);
        jwt.setHeader(header);

        String encodedJWT = jwt.encode();
        System.out.println(encodedJWT);

        // Let us decode
        jwt = new JSONWebToken();
        jwt.setPrivateKey(jweTest.getPrivateKey());
        jwt.load(encodedJWT);

        assertEquals("joe", jwt.getData().getString("iss"));
    }
}
