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
package org.picketlink.authentication.web.support;

import java.security.MessageDigest;

import org.picketlink.idm.credential.Digest;

/**
 * Utility class to support HTTP Digest Authentication
 *
 * @author anil saldhana
 * @since July 5, 2012
 */
public class HTTPDigestUtil {

    private static final String UTF8 = "UTF-8";
    private static final String MD5_ALGORITHM = "MD5";

    /**
     * Given the standard client response in HTTP/Digest mechanism, generate a set of string tokens that retains the quotes
     *
     * @param clientResponse
     * @return
     */
    public static String[] quoteTokenize(String clientResponse) {
        if (clientResponse == null)
            throw new IllegalArgumentException("Null client response.");

        // Derived from http://issues.apache.org/bugzilla/show_bug.cgi?id=37132
        return clientResponse.split(",(?=(?:[^\"]*\"[^\"]*\")+$)");
    }

    /**
     * @param token
     * @return
     */
    public static String userName(String token) {
        if (token.startsWith("Digest")) {
            token = token.substring(7).trim();
        }

        return extract(token, "username=");
    }

    /**
     * Given a digest token, extract the value
     *
     * @param token
     * @param key
     * @return
     */
    public static String extract(String token, String key) {
        String result = null;
        if (token.startsWith(key)) {

            int eq = token.indexOf("=");
            result = token.substring(eq + 1);
            if (result.startsWith("\"")) {
                result = result.substring(1);
            }
            if (result.endsWith("\"")) {
                int len = result.length();
                result = result.substring(0, len - 1);
            }
        }
        return result;
    }

    /**
     * Construct a {@link DigestHolder} from the tokens
     *
     * @param tokens
     * @return
     */
    public static Digest digest(String[] tokens) {
        String username = null, realm = null, nonce = null, uri = null, qop = null, nc = null, cnonce = null, clientResponse = null, opaque = null, domain = null, stale = "false";

        int len = tokens.length;

        for (int i = 0; i < len; i++) {
            String token = tokens[i].trim();

            if (token.startsWith("Digest") || token.startsWith("username=")) {
                username = HTTPDigestUtil.userName(token);
            } else if (token.startsWith("realm")) {
                realm = HTTPDigestUtil.extract(token, "realm=");
            } else if (token.startsWith("nonce")) {
                nonce = HTTPDigestUtil.extract(token, "nonce=");
            } else if (token.startsWith("uri")) {
                uri = HTTPDigestUtil.extract(token, "uri=");
            } else if (token.startsWith("qop")) {
                qop = HTTPDigestUtil.extract(token, "qop=");
            } else if (token.startsWith("nc")) {
                nc = HTTPDigestUtil.extract(token, "nc=");
            } else if (token.startsWith("cnonce")) {
                cnonce = HTTPDigestUtil.extract(token, "cnonce=");
            } else if (token.startsWith("response")) {
                clientResponse = HTTPDigestUtil.extract(token, "response=");
            } else if (token.startsWith("opaque")) {
                opaque = HTTPDigestUtil.extract(token, "opaque=");
            } else if (token.startsWith("domain")) {
                domain = HTTPDigestUtil.extract(token, "domain=");
            } else if (token.startsWith("stale")) {
                stale = HTTPDigestUtil.extract(token, "stale=");
            }
        }
        // Construct a digest holder
        Digest digestHolder = new Digest();

        digestHolder.setUsername(username).setRealm(realm).setNonce(nonce).setUri(uri).setQop(qop).setNonceCount(nc).setClientNonce(cnonce)
                .setDigest(clientResponse).setOpaque(opaque);

        digestHolder.setStale(stale).setDomain(domain);

        return digestHolder;
    }

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
            messageDigest.update(digest.getNonceCount().getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(digest.getClientNonce().getBytes(UTF8));
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
        return clientResponseValue(digest, password).equalsIgnoreCase(digest.getDigest());
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