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

import org.picketlink.idm.credential.util.BCrypt;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;


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
        return encode(rawPassword, generateSalt().getBytes(), true);
    }

    public String encode(String rawPassword, byte[] salt, boolean appendSalt) {
        try {
            final SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGO);
            final KeySpec keySpec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterationCount, keyLength);
            try {
                final SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);

                if (appendSalt) {
                    return new String(secretKey.getEncoded()) + ":" + toHex(salt);
                } else {
                    // backward compatibility when salt was not being generated for each password.
                    return new String(secretKey.getEncoded());
                }
            } catch (InvalidKeySpecException ikse) {
                throw new RuntimeException(ikse);
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
    }

    @Override
    public boolean verify(String rawPassword, String encodedPassword) {
        int saltIndex = encodedPassword.lastIndexOf(":");

        if (saltIndex != -1) {
            byte[] salt = fromHex(encodedPassword.substring(saltIndex + 1));
            return encode(rawPassword, salt, true).equals(encodedPassword);
        }

        // backward compatibility when salt was not being generated for each password.
        return encode(rawPassword, this.salt, false).equals(encodedPassword);
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param   hex         the hex string
     * @return              the hex string decoded into a byte array
     */
    protected byte[] fromHex(String hex) {
        byte[] binary = new byte[hex.length() / 2];
        for (int i = 0; i < binary.length; i++) {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    protected String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }

        return hex;
    }

    protected String generateSalt() {
        return BCrypt.gensalt();
    }
}
