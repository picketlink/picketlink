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

import static org.picketlink.json.JsonConstants.JWE.ALG_RSA1_5;
import static org.picketlink.json.JsonConstants.JWE.ALG_RSA_OAEP;
import static org.picketlink.json.JsonConstants.JWE.ALG_RSA_OAEP_256;
import static org.picketlink.json.JsonConstants.JWE.ENC_A128CBC_HS256;
import static org.picketlink.json.JsonConstants.JWE.ENC_A128GCM;
import static org.picketlink.json.JsonConstants.JWE.ENC_A192CBC_HS384;
import static org.picketlink.json.JsonConstants.JWE.ENC_A192GCM;
import static org.picketlink.json.JsonConstants.JWE.ENC_A256CBC_HS512;
import static org.picketlink.json.JsonConstants.JWE.ENC_A256GCM;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.SecretKey;

import org.picketlink.json.jose.JWE;
import org.picketlink.json.util.JsonUtil;

/**
 * JWE Encrypter of for JSON Web Encryption.
 *
 * <p>
 * Supports the following JWE algorithms:
 *
 * <ul>
 * <li>{@link org.picketlink.json.JsonConstants.JWE.RSA1_5}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.RSA_OAEP}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.RSA_OAEP_256}
 * </ul>
 *
 * <p>
 * Supports the following encryption algorithms:
 *
 * <ul>
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A128CBC_HS256}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A192CBC_HS384}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A256CBC_HS512}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A128GCM}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A192GCM}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A256GCM}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A128CBC_HS256_DEPRECATED}
 * <li>{@link org.picketlink.json.JsonConstants.JWE.A256CBC_HS512_DEPRECATED}
 * </ul>
 *
 * @author Giriraj Sharma
 */
public class JWEEncrypter {

    /**
     * The public RSA key.
     */
    private final RSAPublicKey publicKey;

    /**
     * Creates a new JWE encrypter.
     *
     * @param publicKey The public RSA key. Must not be {@code null}.
     *
     * @throws RuntimeException If the underlying secure random generator couldn't be instantiated.
     */
    public JWEEncrypter(final RSAPublicKey publicKey) {

        if (publicKey == null) {
            throw new IllegalArgumentException("The public RSA key must not be null");
        }
        this.publicKey = publicKey;
    }

    /**
     * Gets the public RSA key.
     *
     * @return The public RSA key.
     */
    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Creates a JWE compact serialization.This string is BASE64URL(UTF8(JWE Protected Header)) || '.' || BASE64URL(JWEEncrypted
     * Key) || '.' || BASE64URL(JWE Initialization Vector) || '.' || BASE64URL(JWE Ciphertext) || '.' ||
     * BASE64URL(JWEAuthentication Tag).
     *
     * @param jweHeader The JWE Header. Must not be {@code null}.
     * @param bytes The byte array of message to be encrypted. Must not be {@code null}.
     *
     * @return The compact serialization string.
     *
     * @throws RuntimeException If any of the algorithm is unsupported.
     */
    public String encrypt(JWE jweHeader, final byte[] bytes) {

        final String alg = jweHeader.getAlgorithm();
        final String enc = jweHeader.getEncryptionAlgorithm();

        // Generate and encrypt the CEK according to the enc method
        final SecureRandom randomGen = new SecureRandom();
        final SecretKey cek = AES.generateKey(Integer.parseInt(jweHeader.getCEKBitLength()), randomGen);

        byte[] encryptedKey;

        if (alg.equals(ALG_RSA1_5)) {
            encryptedKey = RSA1_5.encryptCEK(publicKey, cek);
        } else if (alg.equals(ALG_RSA_OAEP)) {
            encryptedKey = RSA_OAEP.encryptCEK(publicKey, cek);
        } else if (alg.equals(ALG_RSA_OAEP_256)) {
            encryptedKey = RSA_OAEP_256.encryptCEK(publicKey, cek);
        } else {
            throw new RuntimeException("Unsupported JWE algorithm, must be RSA1_5, RSA-OAEP, or RSA-OAEP-256");
        }

        // Apply compression if instructed
        byte[] plainText = bytes;
        if (jweHeader.getCompressionAlgorithm().equals("DEF")) {
            try {
                plainText = DeflateUtils.compress(bytes);
            } catch (Exception e) {
                throw new RuntimeException("Failed to compress plainText");
            }
        }

        // Compose the AAD
        byte[] aad = JsonUtil.b64Encode(jweHeader.toString()).getBytes(Charset.forName("UTF-8"));

        // Encrypt the plain text according to the JWE enc
        byte[] iv;
        AuthenticatedCipherText authCipherText;

        if (enc.equals(ENC_A128CBC_HS256) ||
            enc.equals(ENC_A192CBC_HS384) ||
            enc.equals(ENC_A256CBC_HS512)) {

            iv = AESCBC.generateIV(randomGen);
            authCipherText = AESCBC.encryptAuthenticated(cek, iv, plainText, aad);

        } else if (enc.equals(ENC_A128GCM) ||
            enc.equals(ENC_A192GCM) ||
            enc.equals(ENC_A256GCM)) {

            iv = AESGCM.generateIV(randomGen);
            authCipherText = AESGCM.encrypt(cek, iv, plainText, aad);

        } else {
            throw new RuntimeException("Unsupported encryption method, must be A128CBC_HS256, A192CBC_HS384, A256CBC_HS512, A128GCM, A192GCM or A256GCM");
        }

        String serializedJWE = JsonUtil.b64Encode(jweHeader.toString()) + "." +
            JsonUtil.b64Encode(encryptedKey) + "." +
            JsonUtil.b64Encode(iv) + "." +
            JsonUtil.b64Encode(authCipherText.getCipherText()) + "." +
            JsonUtil.b64Encode(authCipherText.getAuthenticationTag());

        return serializedJWE;

    }
}