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
package org.picketlink.idm.credential.encoder;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


/**
 * Implementation of {@link PasswordEncoder} that uses PBKDF2
 *
 * @author Anil Saldhana
 * @since June 18, 2013
 */
public class PBKDF2PasswordEncoder implements PasswordEncoder {
    private final byte[] salt;
    private final int keyLength, iterationCount;
    public static final String ALGO = "PBKDF2WithHmacSHA1";

    public PBKDF2PasswordEncoder(byte[] salt, int iterationCount, int keyLength) {
        this.salt = salt;
        this.iterationCount = iterationCount;
        this.keyLength = keyLength;
    }

    @Override
    public String encode(String rawPassword) {
        try {
            final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGO);
            final KeySpec keySpec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterationCount, keyLength);
            try {
                final SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
                return new String(secretKey.getEncoded());
            } catch (InvalidKeySpecException ikse) {
                throw new RuntimeException(ikse);
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
    }

    @Override
    public boolean verify(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
