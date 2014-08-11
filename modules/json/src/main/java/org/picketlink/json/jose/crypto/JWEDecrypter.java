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

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.SecretKey;

import org.picketlink.json.jose.JWE;
import org.picketlink.json.util.Base64Util;

/**
 * JWE Decrypter for JSON Web Decryption.
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
 * </ul>
 *
 * @author Giriraj Sharma
 */
public class JWEDecrypter {

    /**
     * The private RSA key.
     */
    private final RSAPrivateKey privateKey;

    /**
     * Creates a new RSA decrypter.
     *
     * @param privateKey The private RSA key. Must not be {@code null}.
     */
    public JWEDecrypter(final RSAPrivateKey privateKey) {

        if (privateKey == null) {
            throw new IllegalArgumentException("The private RSA key must not be null");
        }
        this.privateKey = privateKey;
    }

    /**
     * Gets the private RSA key.
     *
     * @return The private RSA key.
     */
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public byte[] decrypt(final JWE jweHeader,
        final String encryptedKey,
        final String iv,
        final String cipherText,
        final String authTag) {

        // Validate required JWE parts
        if (encryptedKey == null) {
            throw new RuntimeException("The encrypted key must not be null");
        }
        if (iv == null) {
            throw new RuntimeException("The initialization vector (IV) must not be null");
        }
        if (authTag == null) {
            throw new RuntimeException("The authentication tag must not be null");
        }

        // Derive the content encryption key
        String alg = jweHeader.getAlgorithm();
        SecretKey cek;

        if (alg.equals(ALG_RSA1_5)) {
            int keyLength = Integer.parseInt(jweHeader.getCEKBitLength());

            // Protect against MMA attack by generating random CEK on failure,
            // see http://www.ietf.org/mail-archive/web/jose/current/msg01832.html
            SecureRandom randomGen = new SecureRandom();
            SecretKey randomCEK = AES.generateKey(keyLength, randomGen);

            try {
                cek = RSA1_5.decryptCEK(privateKey, Base64Util.b64Decode(encryptedKey), keyLength);

                if (cek == null) {
                    // CEK length mismatch, signalled by null instead of
                    // exception to prevent MMA attack
                    cek = randomCEK;
                }

            } catch (Exception e) {
                // continue
                cek = randomCEK;
            }

        } else if (alg.equals(ALG_RSA_OAEP)) {
            cek = RSA_OAEP.decryptCEK(privateKey, Base64Util.b64Decode(encryptedKey));
        } else if (alg.equals(ALG_RSA_OAEP_256)) {
            cek = RSA_OAEP_256.decryptCEK(privateKey, Base64Util.b64Decode(encryptedKey));
        } else {
            throw new RuntimeException("Unsupported JWE algorithm, must be RSA1_5 or RSA_OAEP");
        }

        // Compose the AAD
        byte[] aad = Base64Util.b64Encode(jweHeader.toString()).getBytes(Charset.forName("UTF-8"));

        // Decrypt the cipher text according to the JWE enc
        String enc = jweHeader.getEncryptionAlgorithm();

        byte[] plainText;

        if (enc.equals(ENC_A128CBC_HS256) ||
            enc.equals(ENC_A192CBC_HS384) ||
            enc.equals(ENC_A256CBC_HS512)) {

            plainText = AESCBC.decryptAuthenticated(
                cek,
                Base64Util.b64Decode(iv),
                Base64Util.b64Decode(cipherText),
                aad,
                Base64Util.b64Decode(authTag));

        } else if (enc.equals(ENC_A128GCM) ||
            enc.equals(ENC_A192GCM) ||
            enc.equals(ENC_A256GCM)) {

            plainText = AESGCM.decrypt(
                cek,
                Base64Util.b64Decode(iv),
                Base64Util.b64Decode(cipherText),
                aad,
                Base64Util.b64Decode(authTag));

        } else {
            throw new RuntimeException("Unsupported encryption method, must be A128CBC_HS256, A192CBC_HS384, A256CBC_HS512, A128GCM, A192GCM or A256GCM");
        }

        // Apply decompression if requested
        try {
            return DeflateUtils.decompress(plainText);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress plainText");
        }
    }
}
