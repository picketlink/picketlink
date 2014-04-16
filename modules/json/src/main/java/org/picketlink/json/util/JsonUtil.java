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
import org.picketlink.common.util.Base64;

import java.io.UnsupportedEncodingException;

import static org.picketlink.json.JsonMessages.MESSAGES;

/**
 * PicketLink JSON Utility Class
 * @author Anil Saldhana
 * @since March 07, 2014
 */
public class JsonUtil {

//FIXME: need to review JWE and support JSR-353
//    public static final String AES = JsonConstants.JWE.AES;
//    public static final String AES_CBC = "AES/CBC/PKCS5Padding";
//    public static final String SHA_256 = "SHA-256";

    /**
     * Base64 Encode without breaking lines
     *
     * @param str
     * @return
     * @throws ProcessingException
     */
    public static String b64Encode(String str) {
        try {
            return Base64.encodeBytes(str.getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
        } catch (UnsupportedEncodingException e) {
            throw MESSAGES.failEncodeToken(e);
        }
    }

    /**
     * Base64 Encode without breaking lines
     *
     * @param str
     * @return
     */
    public static String b64Encode(byte[] str) {
        return Base64.encodeBytes(str, Base64.DONT_BREAK_LINES);
    }

    public static byte[] b64Decode(String str) {
        return Base64.decode(str);
    }

//FIXME: need to review JWE and support JSR-353
//    public static byte[] encryptUsingAES_CBC(String plainText, byte[] key, IvParameterSpec parameters)
//            throws ProcessingException {
//        if (key == null || key.length == 0) {
//            throw JsonMessages.MESSAGES.invalidNullArgument("key");
//        }
//        Cipher cipher = null;
//        try {
//            cipher = Cipher.getInstance(AES_CBC);
//            SecretKeySpec keyspec = new SecretKeySpec(key, AES);
//            cipher.init(Cipher.ENCRYPT_MODE, keyspec, parameters);
//            return cipher.doFinal(plainText.getBytes());
//        } catch (Exception e) {
//            throw JsonMessages.MESSAGES.processingException(e);
//        }
//    }
//
//    public static byte[] decryptUsingAES_CBC(byte[] encryptedPlainText, byte[] key, IvParameterSpec parameters)
//            throws ProcessingException {
//        if (key == null || key.length == 0) {
//            throw JsonMessages.MESSAGES.invalidNullArgument("key");
//        }
//        Cipher cipher = null;
//        try {
//            cipher = Cipher.getInstance(AES_CBC);
//            SecretKeySpec keyspec = new SecretKeySpec(key, AES);
//            cipher.init(Cipher.DECRYPT_MODE, keyspec, parameters);
//            return cipher.doFinal(encryptedPlainText);
//        } catch (Exception e) {
//            throw JsonMessages.MESSAGES.processingException(e);
//        }
//    }
}
