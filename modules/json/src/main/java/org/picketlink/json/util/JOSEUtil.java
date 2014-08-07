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

import java.text.ParseException;

public class JOSEUtil {

    /**
     * Checks the specified arrays for equality in constant time. Intended to mitigate timing attacks.
     *
     * @param a The first array. Must not be {@code null}.
     * @param b The second array. Must not be {@code null}.
     *
     * @return {@code true} if the two arrays are equal, else {@code false}.
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
     * Splits a serialized JOSE object into its Base64URL-encoded parts.
     *
     * @param s The serialized JOSE object to split. Must not be {@code null}.
     *
     * @return The JOSE Base64URL-encoded parts (three for plain text and JWS objects, five for JWE objects).
     *
     * @throws ParseException If the specified string couldn't be split into three or five Base64URL-encoded parts.
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
}
