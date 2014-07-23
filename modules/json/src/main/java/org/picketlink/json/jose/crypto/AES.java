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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * AES encryption, decryption and key generation methods.
 *
 * @author Giriraj Sharma
 */
public class AES {

    /**
     * Returns a new AES key generator instance.
     *
     * @return The AES key generator.
     *
     * @throws RuntimeException If an AES key generator couldn't be instantiated.
     */
    public static KeyGenerator createKeyGenerator() {
        try {
            return KeyGenerator.getInstance("AES", new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Generates an AES key of the specified length.
     *
     * @param keyBitLength The key length, in bits.
     *
     * @return The AES key.
     *
     * @throws RuntimeException If an AES key couldn't be generated.
     */
    public static SecretKey generateKey(final int keyBitLength,
        final SecureRandom random) {

        KeyGenerator aesKeyGenerator = createKeyGenerator();
        aesKeyGenerator.init(keyBitLength, random);
        return aesKeyGenerator.generateKey();
    }

    /**
     * Creates a new AES cipher.
     *
     * @param secretKey The AES key. Must not be {@code null}.
     * @param forEncryption If {@code true} creates an AES encryption cipher, else creates an AES decryption cipher.
     *
     * @return The AES cipher.
     */
    public static AESEngine createCipher(final SecretKey secretKey,
        final boolean forEncryption) {

        AESEngine cipher = new AESEngine();
        CipherParameters cipherParams = new KeyParameter(secretKey.getEncoded());
        cipher.init(forEncryption, cipherParams);
        return cipher;
    }

    /**
     * Prevents public instantiation.
     */
    private AES() {
    }
}