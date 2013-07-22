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
package org.picketlink.idm.credential.util;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestValidationException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
     * Determine the message digest
     *
     * @param str
     * @return
     * @throws FormatException
     */
    public static byte[] md5(String str) {
        try {
            MessageDigest md = getMessageDigest();
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
    public static String calculate(Digest digest, char[] password) {
        try {
            MessageDigest messageDigest = getMessageDigest();

            byte[] ha1 = calculateA1(digest.getUsername(), digest.getRealm(), password);

            byte[] ha2 = calculateA2(digest.getMethod(), digest.getUri());

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

    public static String calculateDigest(Digest digest, byte[] ha1, byte[] ha2) {
        try {
            MessageDigest messageDigest = getMessageDigest();

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

            return convertBytesToHex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(MD5_ALGORITHM);
    }

    public static byte[] calculateA1(String userName, String realm, char[] password) {
        // A1 digest
        MessageDigest messageDigest = null;

        try {
            messageDigest = getMessageDigest();

            messageDigest.update(userName.getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(realm.getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(String.valueOf(password).getBytes(UTF8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding.", e);
        }

        return messageDigest.digest();
    }

    public static byte[] calculateA2(String method, String uri) {
        // A1 digest
        MessageDigest messageDigest = null;

        try {
            messageDigest = getMessageDigest();

            messageDigest.update(method.getBytes(UTF8));
            messageDigest.update((byte) ':');
            messageDigest.update(uri.getBytes(UTF8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid algorithm.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding.", e);
        }

        return messageDigest.digest();
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
        return calculate(digest, password).equalsIgnoreCase(digest.getDigest());
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

    public void validate(Digest digest, String systemRealm, String key) throws DigestValidationException {
        // Check all required parameters were supplied (ie RFC 2069)
        if (digest.getRealm() == null)
            throw new DigestValidationException("Mandatory field 'realm' not specified");
        if (digest.getNonce() == null)
            throw new DigestValidationException("Mandatory field 'nonce' not specified");
        if (digest.getUri() == null)
            throw new DigestValidationException("Mandatory field 'uri' not specified");
        if (digest.getClientNonce() == null)
            throw new DigestValidationException("Mandatory field 'response' not specified");

        // Check all required parameters for an "auth" qop were supplied (ie RFC
        // 2617)
        if ("auth".equals(digest.getQop())) {
            if (digest.getNonceCount() == null) {
                throw new DigestValidationException("Mandatory field 'nc' not specified");
            }

            if (digest.getClientNonce() == null) {
                throw new DigestValidationException("Mandatory field 'cnonce' not specified");
            }
        }

        String nonceAsText = new String(Base64.decode(digest.getNonce()));

        String[] nonceTokens = nonceAsText.split(":");
        if (nonceTokens.length != 2) {
            throw new DigestValidationException("Nonce should provide two tokens - nonce received: " + digest.getNonce());
        }

        // Check realm name equals what we expected
        if (!systemRealm.equals(digest.getRealm())) {
            throw new DigestValidationException("Realm name [" + digest.getRealm() + "] does not match system realm name ["
                    + systemRealm + "]");
        }

        long nonceExpiry = 0;
        try {
            nonceExpiry = new Long(nonceTokens[0]).longValue();
        } catch (NumberFormatException nfe) {
            throw new DigestValidationException("First nonce token should be numeric, but was: " + nonceTokens[0]);
        }

        // To get this far, the digest must have been valid
        // Check the nonce has not expired
        // We do this last so we can direct the user agent its nonce is stale
        // but the request was otherwise appearing to be valid
        if (nonceExpiry < System.currentTimeMillis()) {
            throw new DigestValidationException("Nonce has expired", true);
        }

        String expectedNonceSignature = new String(md5(nonceExpiry + ":" + key));

        if (!expectedNonceSignature.equals(nonceTokens[1])) {
            throw new DigestValidationException("Nonce token invalid: " + nonceAsText);
        }
    }
}