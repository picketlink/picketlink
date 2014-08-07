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

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static org.picketlink.json.JsonMessages.MESSAGES;

/**
 * The Class RSASignatureProvider to provide signing and verification of data.
 *
 * @author Pedro Igor
 */
public class RSASignatureProvider implements SignatureProvider {

    /** The Constant ALGORITHM. */
    private static final String ALGORITHM = "RSA";

    /** The signature provider instance. */
    private static RSASignatureProvider instance;

    /**
     * Gets the signature provider Instance.
     *
     * @return the signature provider
     */
    static final SignatureProvider instance() {
        if (instance == null) {
            instance = new RSASignatureProvider();
        }

        return instance;
    }

    /**
     * @see org.picketlink.json.jose.crypto.SignatureProvider#sign(byte[], org.picketlink.json.jose.crypto.Algorithm, byte[])
     */
    public byte[] sign(byte[] data, Algorithm algorithm, byte[] key) {
        try {
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            Signature signature = Signature.getInstance(algorithm.getAlgorithm());

            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();
        } catch (Exception e) {
            throw MESSAGES.cryptoSignatureFailed(algorithm, e);
        }
    }

    /**
     * @see org.picketlink.json.jose.crypto.SignatureProvider#verify(byte[], org.picketlink.json.jose.crypto.Algorithm, byte[],
     *      byte[])
     */
    public boolean verify(byte[] data, Algorithm algorithm, byte[] signature, byte[] key) {
        try {
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            Signature verifier = Signature.getInstance(algorithm.getAlgorithm());
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            verifier.initVerify(publicKey);
            verifier.update(data);

            return verifier.verify(signature);
        } catch (Exception e) {
            throw MESSAGES.cryptoSignatureValidationFailed(algorithm, e);
        }

    }

}