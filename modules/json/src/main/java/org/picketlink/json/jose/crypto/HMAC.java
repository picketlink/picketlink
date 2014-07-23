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
package org.picketlink.json.jose.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Static methods for Hash-based Message Authentication Codes (HMAC).
 *
 * @author Giriraj Sharma
 */
public class HMAC {

    /**
     * Computes a Hash-based Message Authentication Code (HMAC) for the specified (shared) secret and message.
     *
     * @param alg The Java Cryptography Architecture (JCA) HMAC algorithm name. Must not be {@code null}.
     * @param secret The (shared) secret. Must not be {@code null}.
     * @param message The message. Must not be {@code null}.
     *
     * @return A MAC service instance.
     *
     * @throws RuntimeException If the algorithm is not supported or the MAC secret key is invalid.
     */
    public static byte[] compute(final String alg,
        final byte[] secret,
        final byte[] message) {

        return compute(new SecretKeySpec(secret, alg), message);
    }

    /**
     * Computes a Hash-based Message Authentication Code (HMAC) for the specified (shared) secret key and message.
     *
     * @param secretKey The (shared) secret key, with the appropriate HMAC algorithm. Must not be {@code null}.
     * @param message The message. Must not be {@code null}.
     *
     * @return A MAC service instance.
     *
     * @throws RuntimeException If the algorithm is not supported or the MAC secret key is invalid.
     */
    public static byte[] compute(final SecretKey secretKey,
        final byte[] message) {

        Mac mac;

        try {
            mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unsupported HMAC algorithm: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid HMAC key: " + e.getMessage(), e);
        }
        mac.update(message);
        return mac.doFinal();
    }
}
