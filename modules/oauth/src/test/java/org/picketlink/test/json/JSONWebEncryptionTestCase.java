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

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.junit.Test;
import org.picketlink.json.enc.JSONWebEncryption;
import org.picketlink.json.enc.JSONWebEncryptionHeader;

/**
 * Unit test {@link JSONWebEncryption}
 *
 * @author anil saldhana
 * @since Jul 27, 2012
 */
public class JSONWebEncryptionTestCase {
    byte[] contentMasterKey = { (byte) 4, (byte) 211, (byte) 31, (byte) 197, (byte) 84, (byte) 157, (byte) 252, (byte) 254,
            (byte) 11, (byte) 100, (byte) 157, (byte) 250, (byte) 63, (byte) 170, (byte) 106, (byte) 206, (byte) 107,
            (byte) 124, (byte) 212, (byte) 45, (byte) 111, (byte) 107, (byte) 9, (byte) 219, (byte) 200, (byte) 177, (byte) 0,
            (byte) 240, (byte) 143, (byte) 156, (byte) 44, (byte) 207 };

    byte[] n = { (byte) 177, (byte) 119, (byte) 33, (byte) 13, (byte) 164, (byte) 30, (byte) 108, (byte) 121, (byte) 207,
            (byte) 136, (byte) 107, (byte) (byte) 242, (byte) 12, (byte) 224, (byte) 19, (byte) 226, (byte) 198, (byte) 134,
            (byte) 17, (byte) 71, (byte) 173, (byte) 75, (byte) 42, (byte) (byte) 61, (byte) 48, (byte) 162, (byte) 206,
            (byte) 161, (byte) 97, (byte) 108, (byte) 185, (byte) 234, (byte) 226, (byte) 219, (byte) (byte) 118, (byte) 206,
            (byte) 118, (byte) 5, (byte) 169, (byte) 224, (byte) 60, (byte) 181, (byte) 90, (byte) 85, (byte) 51, (byte) 123,
            (byte) (byte) 6, (byte) 224, (byte) 4, (byte) 122, (byte) 29, (byte) 230, (byte) 151, (byte) 12, (byte) 244,
            (byte) 127, (byte) 121, (byte) 25, (byte) (byte) 4, (byte) 85, (byte) 220, (byte) 144, (byte) 215, (byte) 110,
            (byte) 130, (byte) 17, (byte) 68, (byte) 228, (byte) 129, (byte) (byte) 138, (byte) 7, (byte) 130, (byte) 231,
            (byte) 40, (byte) 212, (byte) 214, (byte) 17, (byte) 179, (byte) 28, (byte) 124, (byte) (byte) 151, (byte) 178,
            (byte) 207, (byte) 20, (byte) 14, (byte) 154, (byte) 222, (byte) 113, (byte) 176, (byte) 24, (byte) 198,
            (byte) (byte) 73, (byte) 211, (byte) 113, (byte) 9, (byte) 33, (byte) 178, (byte) 80, (byte) 13, (byte) 25,
            (byte) 21, (byte) 25, (byte) 153, (byte) (byte) 212, (byte) 206, (byte) 67, (byte) 154, (byte) 147, (byte) 70,
            (byte) 194, (byte) 192, (byte) 183, (byte) 160, (byte) 83, (byte) (byte) 98, (byte) 236, (byte) 175, (byte) 85,
            (byte) 23, (byte) 97, (byte) 75, (byte) 199, (byte) 177, (byte) 73, (byte) 145, (byte) 50, (byte) (byte) 253,
            (byte) 206, (byte) 32, (byte) 179, (byte) 254, (byte) 236, (byte) 190, (byte) 82, (byte) 73, (byte) 67, (byte) 129,
            (byte) (byte) 253, (byte) 252, (byte) 220, (byte) 108, (byte) 136, (byte) 138, (byte) 11, (byte) 192, (byte) 1,
            (byte) 36, (byte) 239, (byte) (byte) 228, (byte) 55, (byte) 81, (byte) 113, (byte) 17, (byte) 25, (byte) 140,
            (byte) 63, (byte) 239, (byte) 146, (byte) 3, (byte) 172, (byte) (byte) 96, (byte) 60, (byte) 227, (byte) 233,
            (byte) 64, (byte) 255, (byte) 224, (byte) 173, (byte) 225, (byte) 228, (byte) 229, (byte) (byte) 92, (byte) 112,
            (byte) 72, (byte) 99, (byte) 97, (byte) 26, (byte) 87, (byte) 187, (byte) 123, (byte) 46, (byte) 50, (byte) 90,
            (byte) (byte) 202, (byte) 117, (byte) 73, (byte) 10, (byte) 153, (byte) 47, (byte) 224, (byte) 178, (byte) 163,
            (byte) 77, (byte) 48, (byte) 46, (byte) (byte) 154, (byte) 33, (byte) 148, (byte) 34, (byte) 228, (byte) 33,
            (byte) 172, (byte) 216, (byte) 89, (byte) 46, (byte) 225, (byte) (byte) 127, (byte) 68, (byte) 146, (byte) 234,
            (byte) 30, (byte) 147, (byte) 54, (byte) 146, (byte) 5, (byte) 133, (byte) 45, (byte) 78, (byte) (byte) 254,
            (byte) 85, (byte) 55, (byte) 75, (byte) 213, (byte) 86, (byte) 194, (byte) 218, (byte) 215, (byte) 163, (byte) 189,
            (byte) (byte) 194, (byte) 54, (byte) 6, (byte) 83, (byte) 36, (byte) 18, (byte) 153, (byte) 53, (byte) 7,
            (byte) 48, (byte) 89, (byte) 35, (byte) 66, (byte) (byte) 144, (byte) 7, (byte) 65, (byte) 154, (byte) 13,
            (byte) 97, (byte) 75, (byte) 55, (byte) 230, (byte) 132, (byte) 3, (byte) 13, (byte) (byte) 239, (byte) 71 };

    byte[] e = { 1, 0, 1 };

    byte[] d = { (byte) 84, (byte) 80, (byte) 150, (byte) 58, (byte) 165, (byte) 235, (byte) 242, (byte) 123, (byte) 217,
            (byte) 55, (byte) 38, (byte) (byte) 154, (byte) 36, (byte) 181, (byte) 221, (byte) 156, (byte) 211, (byte) 215,
            (byte) 100, (byte) 164, (byte) 90, (byte) 88, (byte) (byte) 40, (byte) 228, (byte) 83, (byte) 148, (byte) 54,
            (byte) 122, (byte) 4, (byte) 16, (byte) 165, (byte) 48, (byte) 76, (byte) 194, (byte) (byte) 26, (byte) 107,
            (byte) 51, (byte) 53, (byte) 179, (byte) 165, (byte) 31, (byte) 18, (byte) 198, (byte) 173, (byte) 78, (byte) 61,
            (byte) (byte) 56, (byte) 97, (byte) 252, (byte) 158, (byte) 140, (byte) 80, (byte) 63, (byte) 25, (byte) 223,
            (byte) 156, (byte) 36, (byte) 203, (byte) (byte) 214, (byte) 252, (byte) 120, (byte) 67, (byte) 180, (byte) 167,
            (byte) 3, (byte) 82, (byte) 243, (byte) 25, (byte) 97, (byte) 214, (byte) (byte) 83, (byte) 133, (byte) 69,
            (byte) 16, (byte) 104, (byte) 54, (byte) 160, (byte) 200, (byte) 41, (byte) 83, (byte) 164, (byte) 187,
            (byte) (byte) 70, (byte) 153, (byte) 111, (byte) 234, (byte) 242, (byte) 158, (byte) 175, (byte) 28, (byte) 198,
            (byte) 48, (byte) 211, (byte) (byte) 45, (byte) 148, (byte) 58, (byte) 23, (byte) 62, (byte) 227, (byte) 74,
            (byte) 52, (byte) 117, (byte) 42, (byte) 90, (byte) 41, (byte) (byte) 249, (byte) 130, (byte) 154, (byte) 80,
            (byte) 119, (byte) 61, (byte) 26, (byte) 193, (byte) 40, (byte) 125, (byte) 10, (byte) (byte) 152, (byte) 174,
            (byte) 227, (byte) 225, (byte) 205, (byte) 32, (byte) 62, (byte) 66, (byte) 6, (byte) 163, (byte) 100, (byte) 99,
            (byte) (byte) 219, (byte) 19, (byte) 253, (byte) 25, (byte) 105, (byte) 80, (byte) 201, (byte) 29, (byte) 252,
            (byte) 157, (byte) 237, (byte) (byte) 69, (byte) 1, (byte) 80, (byte) 171, (byte) 167, (byte) 20, (byte) 196,
            (byte) 156, (byte) 109, (byte) 249, (byte) 88, (byte) 0, (byte) (byte) 3, (byte) 152, (byte) 38, (byte) 165,
            (byte) 72, (byte) 87, (byte) 6, (byte) 152, (byte) 71, (byte) 156, (byte) 214, (byte) 16, (byte) (byte) 71,
            (byte) 30, (byte) 82, (byte) 51, (byte) 103, (byte) 76, (byte) 218, (byte) 63, (byte) 9, (byte) 84, (byte) 163,
            (byte) 249, (byte) (byte) 91, (byte) 215, (byte) 44, (byte) 238, (byte) 85, (byte) 101, (byte) 240, (byte) 148,
            (byte) 1, (byte) 82, (byte) 224, (byte) 91, (byte) (byte) 135, (byte) 105, (byte) 127, (byte) 84, (byte) 171,
            (byte) 181, (byte) 152, (byte) 210, (byte) 183, (byte) 126, (byte) 24, (byte) (byte) 46, (byte) 196, (byte) 90,
            (byte) 173, (byte) 38, (byte) 245, (byte) 219, (byte) 186, (byte) 222, (byte) 27, (byte) 240, (byte) (byte) 212,
            (byte) 194, (byte) 15, (byte) 66, (byte) 135, (byte) 226, (byte) 178, (byte) 190, (byte) 52, (byte) 245, (byte) 74,
            (byte) (byte) 65, (byte) 224, (byte) 81, (byte) 100, (byte) 85, (byte) 25, (byte) 204, (byte) 165, (byte) 203,
            (byte) 187, (byte) 175, (byte) (byte) 84, (byte) 100, (byte) 82, (byte) 15, (byte) 11, (byte) 23, (byte) 202,
            (byte) 151, (byte) 107, (byte) 54, (byte) 41, (byte) 207, (byte) (byte) 3, (byte) 136, (byte) 229, (byte) 134,
            (byte) 131, (byte) 93, (byte) 139, (byte) 50, (byte) 182, (byte) 204, (byte) 93, (byte) (byte) 130, (byte) 89 };

    /**
     * Test JSON Web Encryption using the use case from the draft.
     *
     * The Content Master Key will be encrypted using RSA 1.5 Plain Text will be encrypted using AES CBC 128 byte keys
     *
     * (Algorithm: RSA1.5 Encryption: AES CBC 128 byte Keys)
     *
     * @throws Exception
     */
    @Test
    public void testJWEUsingRSA15AES128CBC() throws Exception {
        String plainText = "Now is the time for all good men to come to the aid of their country.";

        String headerStr = "{\"alg\":\"RSA1_5\",\"enc\":\"A128CBC\",\"int\":\"HS256\",\"iv\":\"48V1_ALb6US04U3b\"}";

        JSONWebEncryptionHeader header = new JSONWebEncryptionHeader();
        header.load(headerStr);

        RSAPublicKey rsaPublicKey = getPublicKey();
        RSAPrivateKey rsaPrivateKey = getPrivateKey();

        JSONWebEncryption json = new JSONWebEncryption();
        json.setJsonWebEncryptionHeader(header);

        String encrypted = json.encrypt(plainText, rsaPublicKey, contentMasterKey);

        System.out.println("Encrypted:" + encrypted);

        String cleartext = json.decrypt(encrypted, rsaPrivateKey);
        assertEquals(plainText, cleartext);
    }

    public RSAPrivateKey getPrivateKey() throws Exception {
        BigInteger N = new BigInteger(1, n);
        BigInteger D = new BigInteger(1, d);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(N, D);
        return (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);
    }

    public RSAPublicKey getPublicKey() throws Exception {
        BigInteger N = new BigInteger(1, n);
        BigInteger E = new BigInteger(1, e);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(N, E);
        return (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
    }
}
