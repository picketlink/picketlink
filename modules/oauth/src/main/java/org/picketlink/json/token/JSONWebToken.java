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
package org.picketlink.json.token;

import static org.picketlink.json.PicketLinkJSONConstants.COMMON.ALG;
import static org.picketlink.json.PicketLinkJSONConstants.COMMON.HMAC_SHA_256;
import static org.picketlink.json.PicketLinkJSONConstants.COMMON.PERIOD;

import org.json.JSONException;
import org.json.JSONObject;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.Base64;
import org.picketlink.json.PicketLinkJSONMessages;
import org.picketlink.json.PicketLinkJSONConstants;
import org.picketlink.json.enc.JSONWebEncryption;
import org.picketlink.json.enc.JSONWebEncryptionHeader;
import org.picketlink.json.sig.JSONWebSignature;
import org.picketlink.json.sig.JSONWebSignatureHeader;
import org.picketlink.json.util.PicketLinkJSONUtil;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Represents a JSON Web Token
 *
 * @author anil saldhana
 * @since Jul 30, 2012
 */
public class JSONWebToken {
    private JSONObject header;
    private JSONObject data;
    private String plainText = null;
    private String third = null;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    /**
     * Get the {@link PublicKey} for signature
     *
     * @return
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Set the {@link PublicKey} for signature
     *
     * @param publicKey
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Get the Plain Text
     *
     * @return
     */
    public String getPlainText() {
        return plainText;
    }

    /**
     * Set the Plain Text
     *
     * @param plainText
     */
    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    /**
     * Set the JWT Header
     *
     * @param header
     */
    public void setHeader(JSONObject header) {
        this.header = header;
    }

    /**
     * Get the header
     *
     * @return
     */
    public JSONObject getHeader() {
        return header;
    }

    /**
     * Get the data
     *
     * @return
     */
    public JSONObject getData() {
        return data;
    }

    /**
     * Set the data
     *
     * @param data
     */
    public void setData(JSONObject data) {
        this.data = data;
    }

    /**
     * Get the Private Key
     *
     * @return
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Set the Private Key for encryption
     *
     * @param privateKey
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Encode the JWT
     *
     * @return
     * @throws ProcessingException
     */
    public String encode() throws ProcessingException {
        if (header == null) {
            throw PicketLinkJSONMessages.MESSAGES.jsonWebSignatureHeaderMissing();
        }
        try {
            String alg = header.getString(PicketLinkJSONConstants.COMMON.ALG);
            if ("none".equals(alg)) {
                // Plain Text JWT
                String encodedHeader = PicketLinkJSONUtil.b64Encode(header.toString());
                String encodedText = PicketLinkJSONUtil.b64Encode(data.toString());
                StringBuilder builder = new StringBuilder();
                builder.append(encodedHeader).append(PERIOD).append(encodedText);

                return builder.toString();
            } // Process the header now
            else if (header.has("enc")) {
                // JWE usecase

                JSONWebEncryption jsonWebEnc = new JSONWebEncryption();
                JSONWebEncryptionHeader encHeader = jsonWebEnc.createHeader();
                encHeader.setDelegate(header);

                return jsonWebEnc.encrypt(data.toString(), publicKey);
            } else {
                // sig usecase
                JSONWebSignature jsonWebSignature = new JSONWebSignature();
                JSONWebSignatureHeader jsonSigHeader = new JSONWebSignatureHeader(header.getString(ALG));
                jsonWebSignature.setHeader(jsonSigHeader);

                jsonWebSignature.setPayload(data);

                return jsonWebSignature.encode();
            }
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    /**
     * Decode the JWT string
     *
     * @param tokenString
     * @throws ProcessingException
     */
    public void decode(String tokenString) throws ProcessingException {
        load(tokenString);
    }

    /**
     * Load the token from a formatted string
     *
     * @param tokenString
     * @throws ProcessingException
     */
    public void load(String tokenString) throws ProcessingException {
        String[] tokens = tokenString.split("\\.");
        String payload = null;

        int len = tokens.length;
        try {

            if (len > 4)
                throw PicketLinkJSONMessages.MESSAGES.invalidNumberOfTokens(tokens.length);
            String headerStr = new String(Base64.decode(tokens[0]));
            // Process the header
            header = new JSONObject(headerStr);

            if ("none".equals(header.getString(ALG))) {
                payload = new String(Base64.decode(tokens[1]));
                // Process the payload
                data = new JSONObject(payload);
                return;
            }

            // Process the header now
            if (header.has("enc")) {
                // JWE usecase

                JSONWebEncryption jsonWebEnc = new JSONWebEncryption();
                JSONWebEncryptionHeader encHeader = new JSONWebEncryptionHeader();
                encHeader.load(headerStr);
                jsonWebEnc.setJsonWebEncryptionHeader(encHeader);

                plainText = jsonWebEnc.decrypt(tokenString, privateKey);
                try {
                    data = new JSONObject(plainText);
                } catch (JSONException ignore) {
                }
                return;
            } else {
                // sig usecase
                JSONWebSignature jsonWebSignature = JSONWebSignature.decode(tokenString);
                header = jsonWebSignature.getHeader().get();
                data = jsonWebSignature.getPayload();
            }
        } catch (JSONException e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    /**
     * Validate the JWT
     *
     * @throws ProcessingException
     */
    public void validate() throws ProcessingException {
        try {
            String alg = header.getString(ALG);
            if ("none".equals(alg))
                return;

            if (HMAC_SHA_256.equals(alg)) {

                JSONWebSignature sig = new JSONWebSignature();
                JSONWebSignatureHeader sigHeader = JSONWebSignatureHeader.create(header.toString());
                sig.setHeader(sigHeader);
                sig.setPayload(data);

                String encodedSignature = sig.encode().trim();
                // Use the third variable
                if (encodedSignature.equals(third) == false) {
                    throw PicketLinkJSONMessages.MESSAGES.doesNotMatch("signatures");
                }
            }
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }
}
