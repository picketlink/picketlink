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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Composite key used in AES/CBC/PKCS5Padding/HMAC-SHA2 encryption.
 *
 * <p>
 * See draft-ietf-jose-json-web-algorithms-26, section 5.2.
 *
 * @author Giriraj Sharma
 */
public class CompositeKey {

    /**
     * The input key.
     */
    private final SecretKey inputKey;

    /**
     * The extracted MAC key.
     */
    private final SecretKey macKey;

    /**
     * The extracted AES key.
     */
    private final SecretKey encKey;

    /**
     * The expected truncated MAC output length.
     */
    private final int truncatedMacLength;

    /**
     * Creates a new composite key from the specified secret key.
     *
     * @param inputKey The input key. Must be 256, 384 or 512 bits long. Must not be {@code null}.
     *
     * @throws RuntimeException If the input key length is not supported.
     */
    public CompositeKey(final SecretKey inputKey) {

        this.inputKey = inputKey;
        byte[] secretKeyBytes = inputKey.getEncoded();

        if (secretKeyBytes.length == 32) {
            // AES_128_CBC_HMAC_SHA_256
            // 256 bit key -> 128 bit MAC key + 128 bit AES key
            macKey = new SecretKeySpec(secretKeyBytes, 0, 16, "HMACSHA256");
            encKey = new SecretKeySpec(secretKeyBytes, 16, 16, "AES");
            truncatedMacLength = 16;

        } else if (secretKeyBytes.length == 48) {
            // AES_192_CBC_HMAC_SHA_384
            // 384 bit key -> 129 bit MAC key + 192 bit AES key
            macKey = new SecretKeySpec(secretKeyBytes, 0, 24, "HMACSHA384");
            encKey = new SecretKeySpec(secretKeyBytes, 24, 24, "AES");
            truncatedMacLength = 24;

        } else if (secretKeyBytes.length == 64) {
            // AES_256_CBC_HMAC_SHA_512
            // 512 bit key -> 256 bit MAC key + 256 bit AES key
            macKey = new SecretKeySpec(secretKeyBytes, 0, 32, "HMACSHA512");
            encKey = new SecretKeySpec(secretKeyBytes, 32, 32, "AES");
            truncatedMacLength = 32;

        } else {
            throw new RuntimeException("Unsupported AES/CBC/PKCS5Padding/HMAC-SHA2 key length, must be 256, 384 or 512 bits");
        }
    }

    /**
     * Gets the input key.
     *
     * @return The input key.
     */
    public SecretKey getInputKey() {
        return inputKey;
    }

    /**
     * Gets the extracted MAC key.
     *
     * @return The extracted MAC key.
     */
    public SecretKey getMACKey() {
        return macKey;
    }

    /**
     * Gets the expected truncated MAC length.
     *
     * @return The expected truncated MAC length, in bytes.
     */
    public int getTruncatedMACByteLength() {
        return truncatedMacLength;
    }

    /**
     * Gets the extracted encryption key.
     *
     * @return The extracted encryption key.
     */
    public SecretKey getAESKey() {
        return encKey;
    }
}