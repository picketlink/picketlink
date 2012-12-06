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
package org.picketlink.idm.credential;

import java.security.MessageDigest;

/**
 * Utility class to support Digest Credentials
 *
 * @author anil saldhana
 * @since July 5, 2012
 */
public class DigestUtil {

    private static final String UTF8 = "UTF-8";
    private static final String MD5_ALGORITHM = "MD5";

    /**
     * Determine the message digest
     *
     * @param str
     * @return
     * @throws FormatException
     */
    public static byte[] md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            return md.digest(str.getBytes(UTF8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given the digest, construct the client response value
     *
     * @param digest
     * @param password
     * @return
     * @throws FormatException
     */
    public static String clientResponseValue(Digest digest, char[] password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] ha1;
            // A1 digest
            messageDigest.update(digest.getUsername().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getRealm().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(new String(password).getBytes(UTF8));
            ha1 = messageDigest.digest();

            // A2 digest
            messageDigest.reset();
            messageDigest.update(digest.getMethod().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getUri().getBytes(UTF8));
            byte[] ha2 = messageDigest.digest();

            messageDigest.update(convertBytesToHex(ha1).getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getNonce().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getNc().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getCnonce().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getQop().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(convertBytesToHex(ha2).getBytes(UTF8));
            byte[] digestedValue = messageDigest.digest();

            return convertBytesToHex(digestedValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Match the Client Response value with a generated digest based on the password
     *
     * @param digest
     * @param password
     * @return
     * @throws FormatException
     */
    public static boolean matchCredential(Digest digest, char[] password) {
        return clientResponseValue(digest, password).equalsIgnoreCase(digest.getClientResponse());
    }

    /**
     * Convert a byte array to hex
     *
     * @param bytes
     * @return
     */
    public static String convertBytesToHex(byte[] bytes) {
        int base = 16;

        int ALL_ON = 0xff;

        StringBuilder buf = new StringBuilder();
        for (byte byteValue : bytes) {
            int bit = ALL_ON & byteValue;
            int c = '0' + (bit / base) % base;
            if (c > '9')
                c = 'a' + (c - '0' - 10);
            buf.append((char) c);
            c = '0' + bit % base;
            if (c > '9')
                c = 'a' + (c - '0' - 10);
            buf.append((char) c);
        }
        return buf.toString();
    }
}