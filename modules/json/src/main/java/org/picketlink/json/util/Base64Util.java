/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.json.util;

import static org.picketlink.json.JsonMessages.MESSAGES;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Base64Util {

    /**
     * Base64 Encode without breaking lines.
     *
     * @param str the string to be encoded
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
     * Base64 Encode the byte array without breaking lines.
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
     * Base64 decode the string.
     *
     * @param s the string to be decoded
     * @return the decoded byte[] array
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
}
