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

import java.security.PrivateKey;

import org.json.JSONObject;
import org.junit.Test;
import org.picketlink.json.sig.JSONWebSignature;
import org.picketlink.json.sig.JSONWebSignatureHeader;
import org.picketlink.json.token.JSONWebToken;

/**
 * Unit test the {@link JSONWebToken}
 *
 * @author anil saldhana
 * @since Jul 30, 2012
 */
public class JSONWebTokenTestCase {

    /**
     * Test the Plaintext JWT usecase
     *
     * @throws Exception
     */
    @Test
    public void testPlainTextJWT() throws Exception {
        String token = "eyJhbGciOiJub25lIn0=.eyJpc3MiOiJqb2UiLA0KICJleHAiOjEzMDA4MTkzODAsDQogImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ==.";
        JSONWebToken jwt = new JSONWebToken();
        jwt.load(token);
        assertEquals("{\"alg\":\"none\"}", jwt.getHeader().toString());

        JSONObject data = jwt.getData();
        assertEquals("joe", data.getString("iss"));
        assertEquals("1300819380", data.getString("exp"));
        assertEquals("true", data.getString("http://example.com/is_root"));
    }

    /**
     * Test the JWT with MAC usecase
     *
     * @throws Exception
     */
    @Test
    public void testJWTWithMAC() throws Exception {

        String headerStr = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";
        String text = "{\"iss\":\"joe\",\"exp\":1300819380,\"http://example.com/is_root\":true}";

        JSONWebSignature sig = new JSONWebSignature();
        JSONObject payload = new JSONObject(text);

        sig.setPayload(payload);
        JSONWebSignatureHeader header = JSONWebSignatureHeader.create(headerStr);
        sig.setHeader(header);

        String tokenValue = sig.encode();

        JSONWebToken jwt = new JSONWebToken();
        jwt.load(tokenValue);

        JSONObject headerObj = new JSONObject(header);
        JSONObject textObj = new JSONObject(text);

        JSONObject jwtHeader = jwt.getHeader();
        JSONObject jwtData = jwt.getData();

        assertEquals(headerObj.getString("alg"), jwtHeader.getString("alg"));

        assertEquals(textObj.getString("iss"), jwtData.getString("iss"));
        assertEquals(textObj.getString("exp"), jwtData.getString("exp"));
    }

    @Test
    public void testJWTWithEnc() throws Exception {
        String header = "{\"alg\":\"RSA1_5\",\"enc\":\"A128CBC\",\"int\":\"HS256\",\"iv\":\"AxY8DCtDaGlsbGljb3RoZQ\"}";

        String token = "eyJhbGciOiJSU0ExXzUiLCJpdiI6IjQ4VjFfQUxiNlVTMDRVM2IiLCJpbnQiOiJIUzI1NiIsImVuYyI6IkExMjhDQkMifQ==."
                + "C6s7/YmGL6P6Cp4ylJIuMo41vHs/OBrmVmuZYQepeq/e8JsE4ffe7g29mvA1BtDUFQwuRDb1BHAPMZaoC8al/4mq4lpeOhxriY1gp"
                + "lKthw1O9/GfiBPP0Yf/Tiyqe9nFQscA00awfV0zLq9qhdZWI3AZJeIZJ8D1JA0rFkbnS/HFHey8iI9KhNIc1zLatnMVjB+vywpK0Lmxv"
                + "maXXlE59o7khAF1MRwL4e+XTTRm02Q1Ye06HVLbq0dzVmQyPyrnWzoTPduLMxTb/MafS9BN5WdtL8q8DkadQUmA65sOSCcBPaGdxNdWoa"
                + "OPe8ERYKAqJGtLGRyafZaxd9ldI57GNg==.xpurSbWBpEGuGt4huHJ5ZHhggDV7PASWAz06rgCwCDvc+IgVM6HucUHSCvvvqn5/NVRNS2la2"
                + "Kva9+7dT1zUPB+HcmgxN7VJs0NKiWS8iZc=.PHEnSewy1m7BwZnQPYPkxTv+bv3/o5Rpf1ToevCaZiU=";

        JSONWebEncryptionTestCase test = new JSONWebEncryptionTestCase();
        PrivateKey privateKey = test.getPrivateKey();

        JSONWebToken jwt = new JSONWebToken();
        jwt.setPrivateKey(privateKey);
        jwt.load(token);

        JSONObject headerObj = new JSONObject(header);

        JSONObject jwtHeader = jwt.getHeader();

        assertEquals(headerObj.getString("alg"), jwtHeader.getString("alg"));

        assertEquals("Now is the time for all good men to come to the aid of their country.", jwt.getPlainText());
    }
}