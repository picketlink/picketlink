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

import java.security.AlgorithmParameters;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

/**
 * RSAES OAEP (SHA-256) methods for Content Encryption Key (CEK) encryption and decryption.
 *
 * @author Giriraj Sharma
 */
public class RSA_OAEP_256 {

    /**
     * Encrypts the specified Content Encryption Key (CEK).
     *
     * @param pub The public RSA key. Must not be {@code null}.
     * @param cek The Content Encryption Key (CEK) to encrypt. Must not be {@code null}.
     *
     * @return The encrypted Content Encryption Key (CEK).
     *
     * @throws RuntimeException If encryption failed.
     */
    public static byte[] encryptCEK(final RSAPublicKey pub, final SecretKey cek) {

        try {
            AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP");
            AlgorithmParameterSpec paramSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            algp.init(paramSpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pub, algp);
            return cipher.doFinal(cek.getEncoded());

        } catch (Exception e) {
            // java.security.NoSuchAlgorithmException
            // java.security.NoSuchPaddingException
            // java.security.InvalidKeyException
            // javax.crypto.IllegalBlockSizeException
            // javax.crypto.BadPaddingException
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Decrypts the specified encrypted Content Encryption Key (CEK).
     *
     * @param priv The private RSA key. Must not be {@code null}.
     * @param encryptedCEK The encrypted Content Encryption Key (CEK) to decrypt. Must not be {@code null}.
     *
     * @return The decrypted Content Encryption Key (CEK).
     *
     * @throws RuntimeException If decryption failed.
     */
    public static SecretKey decryptCEK(final RSAPrivateKey priv,
        final byte[] encryptedCEK) {

        try {
            AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP");
            AlgorithmParameterSpec paramSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            algp.init(paramSpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, priv, algp);
            return new SecretKeySpec(cipher.doFinal(encryptedCEK), "AES");

        } catch (Exception e) {
            // java.security.NoSuchAlgorithmException
            // java.security.NoSuchPaddingException
            // java.security.InvalidKeyException
            // javax.crypto.IllegalBlockSizeException
            // javax.crypto.BadPaddingException
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Prevents public instantiation.
     */
    private RSA_OAEP_256() {
    }
}