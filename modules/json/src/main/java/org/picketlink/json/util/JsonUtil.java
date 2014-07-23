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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import static org.picketlink.json.JsonMessages.MESSAGES;

// TODO: Auto-generated Javadoc
/**
 * PicketLink JSON Utility Class.
 *
 * @author Anil Saldhana
 * @since March 07, 2014
 */
public class JsonUtil {

    // FIXME: need to review JWE and support JSR-353
    // public static final String AES = JsonConstants.JWE.AES;
    // public static final String AES_CBC = "AES/CBC/PKCS5Padding";
    // public static final String SHA_256 = "SHA-256";

    /**
     * Base64 Encode without breaking lines.
     *
     * @param str the str
     * @return the string
     */
    public static String b64Encode(String str) {
        try {
            return b64Encode(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw MESSAGES.failEncodeToken(e);
        }
    }

    /**
     * Base64 Encode without breaking lines.
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String b64Encode(byte[] bytes) {
        String s = Base64.encodeBytes(bytes);

        s = s.split("=")[0]; // Remove any trailing '='s
        s = s.replace('+', '-'); // 62nd char of encoding
        s = s.replace('/', '_'); // 63rd char of encoding

        return s;
    }

    /**
     * B64 decode.
     *
     * @param s the string to be decoded
     * @return the byte[]
     */
    public static byte[] b64Decode(String s) {
        s = s.replace('-', '+'); // 62nd char of encoding
        s = s.replace('_', '/'); // 63rd char of encoding
        switch (s.length() % 4) { // Pad with trailing '='s
            case 0:
                break; // No pad chars in this case
            case 2:
                s += "==";
                break; // Two pad chars
            case 3:
                s += "=";
                break; // One pad char
            default:
                throw new RuntimeException("Illegal base64url string!");
        }

        try {
            return Base64.decode(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constant time are equal.
     *
     * @param a the a
     * @param b the b
     * @return true, if successful
     */
    public static boolean constantTimeAreEqual(final byte[] a, final byte[] b) {
        // From http://codahale.com/a-lesson-in-timing-attacks/
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Split.
     *
     * @param s the s
     * @return the string[]
     * @throws ParseException the parse exception
     */
    public static String[] split(final String s)
        throws ParseException {
        // We must have 2 (JWS) or 4 dots (JWE)
        // String.split() cannot handle empty parts
        final int dot1 = s.indexOf(".");

        if (dot1 == -1) {
            throw new ParseException("Invalid serialized plain/JWS/JWE object: Missing part delimiters", 0);
        }

        final int dot2 = s.indexOf(".", dot1 + 1);
        if (dot2 == -1) {
            throw new ParseException("Invalid serialized plain/JWS/JWE object: Missing second delimiter", 0);
        }

        // Third dot for JWE only
        final int dot3 = s.indexOf(".", dot2 + 1);
        if (dot3 == -1) {

            // Two dots only? -> We have a JWS
            String[] parts = new String[3];
            parts[0] = new String(s.substring(0, dot1));
            parts[1] = new String(s.substring(dot1 + 1, dot2));
            parts[2] = new String(s.substring(dot2 + 1));
            return parts;
        }

        // Fourth final dot for JWE
        final int dot4 = s.indexOf(".", dot3 + 1);
        if (dot4 == -1) {
            throw new ParseException("Invalid serialized JWE object: Missing fourth delimiter", 0);
        }

        if (dot4 != -1 && s.indexOf(".", dot4 + 1) != -1) {
            throw new ParseException("Invalid serialized plain/JWS/JWE object: Too many part delimiters", 0);
        }
        // Four dots -> five parts
        String[] parts = new String[5];
        parts[0] = new String(s.substring(0, dot1));
        parts[1] = new String(s.substring(dot1 + 1, dot2));
        parts[2] = new String(s.substring(dot2 + 1, dot3));
        parts[3] = new String(s.substring(dot3 + 1, dot4));
        parts[4] = new String(s.substring(dot4 + 1));
        return parts;
    }

    // FIXME: need to review JWE and support JSR-353
    // public static byte[] encryptUsingAES_CBC(String plainText, byte[] key, IvParameterSpec parameters)
    // throws ProcessingException {
    // if (key == null || key.length == 0) {
    // throw JsonMessages.MESSAGES.invalidNullArgument("key");
    // }
    // Cipher cipher = null;
    // try {
    // cipher = Cipher.getInstance(AES_CBC);
    // SecretKeySpec keyspec = new SecretKeySpec(key, AES);
    // cipher.init(Cipher.ENCRYPT_MODE, keyspec, parameters);
    // return cipher.doFinal(plainText.getBytes());
    // } catch (Exception e) {
    // throw JsonMessages.MESSAGES.processingException(e);
    // }
    // }
    //
    // public static byte[] decryptUsingAES_CBC(byte[] encryptedPlainText, byte[] key, IvParameterSpec parameters)
    // throws ProcessingException {
    // if (key == null || key.length == 0) {
    // throw JsonMessages.MESSAGES.invalidNullArgument("key");
    // }
    // Cipher cipher = null;
    // try {
    // cipher = Cipher.getInstance(AES_CBC);
    // SecretKeySpec keyspec = new SecretKeySpec(key, AES);
    // cipher.init(Cipher.DECRYPT_MODE, keyspec, parameters);
    // return cipher.doFinal(encryptedPlainText);
    // } catch (Exception e) {
    // throw JsonMessages.MESSAGES.processingException(e);
    // }
    // }
}
