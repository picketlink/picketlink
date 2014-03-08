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
package org.picketlink.json.util;

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.json.PicketLinkJSONMessages;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC SHA 256 Algorithm
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class HmacSha256Util {

    /**
     * Encode a payload using HMAC SHA 256 algorithm
     *
     * @param payload
     * @return
     * @throws ProcessingException
     */
    public static String encode(String payload) throws ProcessingException {
        final Charset charSet = Charset.forName("UTF-8");
        Mac sha256_HMAC = null;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e1) {
            throw PicketLinkJSONMessages.MESSAGES.noSuchAlgorithm(e1);
        }

        final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(charSet.encode("key").array(), "HmacSHA256");
        try {
            sha256_HMAC.init(secret_key);
        } catch (InvalidKeyException e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
        final byte[] mac_data = sha256_HMAC.doFinal(charSet.encode(payload).array());
        String result = "";
        for (final byte element : mac_data) {
            result += Integer.toString((element & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
