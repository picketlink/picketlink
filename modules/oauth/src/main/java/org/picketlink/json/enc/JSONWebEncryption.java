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
package org.picketlink.json.enc;

import static org.picketlink.json.PicketLinkJSONConstants.COMMON.PERIOD;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.Base64;
import org.picketlink.json.PicketLinkJSONMessages;
import org.picketlink.json.util.PicketLinkJSONUtil;

/**
 * Represents JSON Web Encryption http://tools.ietf.org/html/draft-jones-json-web-encryption
 *
 * @author anil saldhana
 * @since Jul 27, 2012
 */
public class JSONWebEncryption {
    protected JSONWebEncryptionHeader jsonWebEncryptionHeader;

    /**
     * Create an attached Header
     *
     * @return
     */
    public JSONWebEncryptionHeader createHeader() {
        if (jsonWebEncryptionHeader == null) {
            jsonWebEncryptionHeader = new JSONWebEncryptionHeader();
        }
        return jsonWebEncryptionHeader;
    }

    /**
     * Get the {@link JSONWebEncryptionHeader}
     *
     * @return
     */
    public JSONWebEncryptionHeader getJsonWebEncryptionHeader() {
        return jsonWebEncryptionHeader;
    }

    /**
     * Set the {@link JSONWebEncryptionHeader}
     *
     * @param jsonWebEncryptionHeader
     */
    public void setJsonWebEncryptionHeader(JSONWebEncryptionHeader jsonWebEncryptionHeader) {
        this.jsonWebEncryptionHeader = jsonWebEncryptionHeader;
    }

    /**
     * Encrypt
     *
     * @param plainText
     * @param recipientPublicKey
     * @return
     * @throws ProcessingException
     */
    public String encrypt(String plainText, PublicKey recipientPublicKey) throws ProcessingException {
        if (jsonWebEncryptionHeader == null) {
            throw PicketLinkJSONMessages.MESSAGES.jsonEncryptionHeaderMissing();
        }
        if (plainText == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("plainText");
        }
        if (recipientPublicKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("recipientPublicKey");
        }
        byte[] contentMasterKey = createContentMasterKey();
        return encrypt(plainText, recipientPublicKey, contentMasterKey);
    }

    /**
     * Encrypt
     *
     * @param plainText
     * @param recipientPublicKey
     * @param contentMasterKey
     * @return
     * @throws ProcessingException
     */
    public String encrypt(String plainText, PublicKey recipientPublicKey, byte[] contentMasterKey) throws ProcessingException {
        if (jsonWebEncryptionHeader == null) {
            throw PicketLinkJSONMessages.MESSAGES.jsonEncryptionHeaderMissing();
        }
        if (plainText == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("plainText");
        }
        if (recipientPublicKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("recipientPublicKey");
        }
        if (contentMasterKey == null) {
            return encrypt(plainText, recipientPublicKey);
        }

        SecretKey contentEncryptionKey = new SecretKeySpec(contentMasterKey, PicketLinkJSONUtil.AES);

        // Encrypt using Recipient's public key to yield JWE Encrypted Key
        byte[] jweEncryptedKey = encryptKey(recipientPublicKey, contentMasterKey);
        String encodedJWEKey = PicketLinkJSONUtil.b64Encode(jweEncryptedKey);

        StringBuilder builder = new StringBuilder(PicketLinkJSONUtil.b64Encode(jsonWebEncryptionHeader.toString()));
        builder.append(PERIOD);
        builder.append(encodedJWEKey);

        if (jsonWebEncryptionHeader.needIntegrity()) {
            int cekLength = jsonWebEncryptionHeader.getCEKLength();
            byte[] cek = generateCEK(contentEncryptionKey.getEncoded(), cekLength);

            // Deal with IV
            String iv;
            try {
                iv = jsonWebEncryptionHeader.getDelegate().getString("iv");
            } catch (JSONException e) {
                throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
            }
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());

            byte[] encryptedText = PicketLinkJSONUtil.encryptUsingAES_CBC(plainText, cek, ivParameterSpec);
            String encodedJWEText = PicketLinkJSONUtil.b64Encode(encryptedText);
            builder.append(PERIOD);
            builder.append(encodedJWEText);

            int cikLength = jsonWebEncryptionHeader.getCIKLength();
            byte[] cik = generateCIK(contentEncryptionKey.getEncoded(), cikLength);
            byte[] integrityValue = performMac(cik, builder.toString().getBytes());
            String encodedIntegrityValue = PicketLinkJSONUtil.b64Encode(integrityValue);

            builder.append(PERIOD);
            builder.append(encodedIntegrityValue);
        } else {
            // Encrypt the plain text
            byte[] encryptedText = encryptText(plainText, recipientPublicKey);
            String encodedJWEText = PicketLinkJSONUtil.b64Encode(encryptedText);
            builder.append(PERIOD);
            builder.append(encodedJWEText);
        }

        return builder.toString();
    }

    /**
     * Decrypt using a {@link PrivateKey}
     *
     * @param encryptedText
     * @param privateKey
     * @return
     * @throws ProcessingException
     */
    public String decrypt(String encryptedText, PrivateKey privateKey) throws ProcessingException {
        if (privateKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("privateKey");
        }
        try {
            String[] splitBits = encryptedText.split("\\.");
            int length = splitBits.length;
            String encodedHeader = splitBits[0];
            String encodedKey = splitBits[1];
            String encodedValue = splitBits[2];
            String encodedIntegrity = null;
            if (length == 4) {
                encodedIntegrity = splitBits[3];
            }

            String decodedHeader = new String(Base64.decode(encodedHeader));
            JSONWebEncryptionHeader header = new JSONWebEncryptionHeader();
            header.load(decodedHeader);

            if (header.needIntegrity()) {

                byte[] decodedKey = Base64.decode(encodedKey);

                byte[] secretKey = decryptKey(privateKey, decodedKey);

                int cekLength = header.getCEKLength();
                byte[] cek = generateCEK(secretKey, cekLength);

                // Deal with IV
                String iv;
                try {
                    iv = header.getDelegate().getString("iv");
                } catch (JSONException e) {
                    throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
                }

                IvParameterSpec ivParameter = new IvParameterSpec(iv.getBytes());

                byte[] decodedText = Base64.decode(encodedValue);
                byte[] plainText = PicketLinkJSONUtil.decryptUsingAES_CBC(decodedText, cek, ivParameter);

                int cikLength = header.getCIKLength();
                byte[] cik = generateCIK(secretKey, cikLength);

                StringBuilder builder = new StringBuilder(PicketLinkJSONUtil.b64Encode(header.toString()));
                builder.append(PERIOD).append(encodedKey).append(PERIOD).append(encodedValue);

                byte[] integrityValue = performMac(cik, builder.toString().getBytes());
                String encodedIntegrityValue = PicketLinkJSONUtil.b64Encode(integrityValue);

                if (byteEquals(encodedIntegrityValue.getBytes(), encodedIntegrity.getBytes())) {
                    return new String(plainText);
                } else {
                    throw new RuntimeException("Integrity Checks Failed");
                }
            }

            Cipher textCipher = header.getCipherBasedOnAlg();
            textCipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decodedText = Base64.decode(encodedValue);
            byte[] plainText = textCipher.doFinal(decodedText);

            return new String(plainText);
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    private byte[] encryptText(String plainText, PublicKey recipientPublicKey) throws ProcessingException {
        if (recipientPublicKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("recipientPublicKey");
        }
        try {
            Cipher cipher = jsonWebEncryptionHeader.getCipherBasedOnAlg();
            cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);

            return cipher.doFinal(plainText.getBytes());
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    private byte[] encryptKey(PublicKey publicKey, byte[] contentMasterKey) throws ProcessingException {
        if (publicKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("publicKey");
        }
        try {
            Cipher cipher = jsonWebEncryptionHeader.getCipherBasedOnAlg();
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return cipher.doFinal(contentMasterKey);
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    private byte[] decryptKey(PrivateKey privateKey, byte[] encryptedKey) throws ProcessingException {
        if (privateKey == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("privateKey");
        }
        try {
            Cipher cipher = jsonWebEncryptionHeader.getCipherBasedOnAlg();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            return cipher.doFinal(encryptedKey);
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    /**
     * Generate a random byte array.
     *
     * @return
     */
    private byte[] createContentMasterKey() {
        return UUID.randomUUID().toString().getBytes();
    }

    /**
     * Content Integrity Key (CIK) A key used with a MAC function to ensure the integrity of the Ciphertext and the parameters
     * used to create it.
     *
     * @param keyBytes
     * @param cikByteLength
     * @return
     * @throws ProcessingException
     */
    private byte[] generateCIK(byte[] keyBytes, int cikByteLength) throws ProcessingException {
        // "Integrity"
        final byte[] otherInfo = { 73, 110, 116, 101, 103, 114, 105, 116, 121 };
        ConcatenationKeyDerivation kdfGen = new ConcatenationKeyDerivation(PicketLinkJSONUtil.SHA_256);
        return kdfGen.concatKDF(keyBytes, cikByteLength, otherInfo);
    }

    /**
     * Content Encryption Key (CEK) A symmetric key used to encrypt the Plaintext for the recipient to produce the Ciphertext.
     *
     * @param keyBytes
     * @param cekByteLength
     * @return
     * @throws ProcessingException
     */
    private byte[] generateCEK(byte[] keyBytes, int cekByteLength) throws ProcessingException {
        // "Encryption"
        final byte[] otherInfo = { 69, 110, 99, 114, 121, 112, 116, 105, 111, 110 };
        ConcatenationKeyDerivation kdfGen = new ConcatenationKeyDerivation(PicketLinkJSONUtil.SHA_256);
        return kdfGen.concatKDF(keyBytes, cekByteLength, otherInfo);
    }

    private byte[] performMac(byte[] key, byte[] data) throws ProcessingException {
        Mac mac = null;
        try {
            mac = Mac.getInstance(jsonWebEncryptionHeader.getMessageAuthenticationCodeAlgo());

            mac.init(new SecretKeySpec(key, mac.getAlgorithm()));
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    private boolean byteEquals(byte[] b1, byte[] b2) {
        // Check if the addresses match
        if (b1 == b2) {
            return true;
        }

        // Check if either one is null
        if (b1 == null || b2 == null) {
            return false;
        }

        // Match on the lengths
        if (b1.length != b2.length) {
            return false;
        }

        // Match each byte
        int notMatching = 0;

        for (int index = 0; index != b1.length; index++) {
            notMatching |= (b1[index] ^ b2[index]);
        }
        return notMatching == 0;
    }
}
