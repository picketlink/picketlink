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

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.picketlink.json.util.JsonUtil;

/**
 * AES/CBC/PKCS5Padding and AES/CBC/PKCS5Padding/HMAC-SHA2 encryption and decryption methods.
 *
 * <p>
 * Also supports the deprecated AES/CBC/HMAC encryption using a custom concat KDF (JOSE draft suite 08).
 *
 * <p>
 * See draft-ietf-jose-json-web-algorithms-26, section 5.2.
 *
 * @author Giriraj Sharma
 */
public class AESCBC {

    /**
     * The standard Initialization Vector (IV) length (128 bits).
     */
    public static final int IV_BIT_LENGTH = 128;

    /**
     * Generates a random 128 bit (16 byte) Initialization Vector(IV) for use in AES-CBC encryption.
     *
     * @param randomGen The secure random generator to use. Must be correctly initialized and not {@code null}.
     *
     * @return The random 128 bit IV, as 16 byte array.
     */
    public static byte[] generateIV(final SecureRandom randomGen) {

        byte[] bytes = new byte[IV_BIT_LENGTH / 8];
        randomGen.nextBytes(bytes);
        return bytes;
    }

    /**
     * Creates a new AES/CBC/PKCS5Padding cipher.
     *
     * @param secretKey The AES key. Must not be {@code null}.
     * @param forEncryption If {@code true} creates an encryption cipher, else creates a decryption cipher.
     * @param iv The initialization vector (IV). Must not be {@code null}.
     *
     * @return The AES/CBC/PKCS5Padding cipher.
     */
    private static Cipher createAESCBCCipher(final SecretKey secretKey,
        final boolean forEncryption,
        final byte[] iv) {

        Cipher cipher;

        // http://stackoverflow.com/questions/3425766/how-would-i-use-maven-to-install-the-jce-unlimited-strength-policy-files?rq=1
        // Simple hack for using policies of jce-unlimited-strength-policy-files
        try {
            Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            field.set(null, java.lang.Boolean.FALSE);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }

        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keyspec = new SecretKeySpec(secretKey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            if (forEncryption) {
                cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivSpec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, keyspec, ivSpec);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return cipher;
    }

    /**
     * Encrypts the specified plain text using AES/CBC/PKCS5Padding.
     *
     * @param secretKey The AES key. Must not be {@code null}.
     * @param iv The initialization vector (IV). Must not be {@code null}.
     * @param plainText The plain text. Must not be {@code null}.
     *
     * @return The cipher text.
     *
     * @throws RuntimeException If encryption failed.
     */
    public static byte[] encrypt(final SecretKey secretKey,
        final byte[] iv,
        final byte[] plainText) {

        Cipher cipher = createAESCBCCipher(secretKey, true, iv);
        try {
            return cipher.doFinal(plainText);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Computes the bit length of the specified Additional Authenticated Data (AAD). Used in AES/CBC/PKCS5Padding/HMAC-SHA2
     * encryption.
     *
     * @param aad The Additional Authenticated Data (AAD). Must not be {@code null}.
     *
     * @return The computed AAD bit length, as a 64 bit big-ending representation (8 byte array).
     */
    public static byte[] computeAADLength(final byte[] aad) {

        final int bitLength = aad.length * 8;
        return ByteBuffer.allocate(8).putLong(bitLength).array();
    }

    /**
     * Encrypts the specified plain text using AES/CBC/PKCS5Padding/ HMAC-SHA2.
     *
     * <p>
     * See draft-ietf-jose-json-web-algorithms-26, section 5.2.
     *
     * <p>
     * See draft-mcgrew-aead-aes-cbc-hmac-sha2-01
     *
     * @param secretKey The secret key. Must be 256 or 512 bits long. Must not be {@code null}.
     * @param iv The initialisation vector (IV). Must not be {@code null}.
     * @param plainText The plain text. Must not be {@code null}.
     * @param aad The additional authenticated data. Must not be {@code null}.
     *
     * @return The authenticated cipher text.
     *
     * @throws RuntimeException If encryption failed.
     */
    public static AuthenticatedCipherText encryptAuthenticated(final SecretKey secretKey,
        final byte[] iv,
        final byte[] plainText,
        final byte[] aad) {

        // Extract MAC + AES/CBC keys from input secret key
        CompositeKey compositeKey = new CompositeKey(secretKey);

        // Encrypt plain text
        byte[] cipherText = encrypt(compositeKey.getAESKey(), iv, plainText);

        // AAD length to 8 byte array
        byte[] al = computeAADLength(aad);

        // Do MAC
        int hmacInputLength = aad.length + iv.length + cipherText.length + al.length;
        byte[] hmacInput = ByteBuffer.allocate(hmacInputLength).put(aad).put(iv).put(cipherText).put(al).array();
        byte[] hmac = HMAC.compute(compositeKey.getMACKey(), hmacInput);
        byte[] authTag = Arrays.copyOf(hmac, compositeKey.getTruncatedMACByteLength());

        return new AuthenticatedCipherText(cipherText, authTag);
    }

    /**
     * Decrypts the specified cipher text using AES/CBC/PKCS5Padding.
     *
     * @param secretKey The AES key. Must not be {@code null}.
     * @param iv The initialization vector (IV). Must not be {@code null}.
     * @param cipherText The cipher text. Must not be {@code null}.
     *
     * @return The decrypted plain text.
     *
     * @throws RuntimeException If decryption failed.
     */
    public static byte[] decrypt(final SecretKey secretKey,
        final byte[] iv,
        final byte[] cipherText) {

        Cipher cipher = createAESCBCCipher(secretKey, false, iv);
        try {
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Decrypts the specified cipher text using AES/CBC/PKCS5Padding/ HMAC-SHA2.
     *
     * <p>
     * See draft-ietf-jose-json-web-algorithms-26, section 5.2.
     *
     * @param secretKey The secret key. Must be 256 or 512 bits long. Must not be {@code null}.
     * @param iv The initialization vector (IV). Must not be {@code null}.
     * @param cipherText The cipher text. Must not be {@code null}.
     * @param aad The additional authenticated data. Must not be {@code null}.
     * @param authTag The authentication tag. Must not be {@code null}.
     *
     * @return The decrypted plain text.
     *
     * @throws RuntimeException If decryption failed.
     */
    public static byte[] decryptAuthenticated(final SecretKey secretKey,
        final byte[] iv,
        final byte[] cipherText,
        final byte[] aad,
        final byte[] authTag) {

        // Extract MAC + AES/CBC keys from input secret key
        CompositeKey compositeKey = new CompositeKey(secretKey);

        // AAD length to 8 byte array
        byte[] al = computeAADLength(aad);

        // Check MAC
        int hmacInputLength = aad.length + iv.length + cipherText.length + al.length;
        byte[] hmacInput = ByteBuffer.allocate(hmacInputLength).put(aad).put(iv).put(cipherText).put(al).array();
        byte[] hmac = HMAC.compute(compositeKey.getMACKey(), hmacInput);

        byte[] expectedAuthTag = Arrays.copyOf(hmac, compositeKey.getTruncatedMACByteLength());
        boolean macCheckPassed = true;

        if (!JsonUtil.constantTimeAreEqual(expectedAuthTag, authTag)) {
            // Thwart timing attacks by delaying exception until after decryption
            macCheckPassed = false;
        }

        byte[] plainText = decrypt(compositeKey.getAESKey(), iv, cipherText);
        if (!macCheckPassed) {
            throw new RuntimeException("MAC check failed");
        }
        return plainText;
    }

    /**
     * Prevents public instantiation.
     */
    private AESCBC() {
    }
}