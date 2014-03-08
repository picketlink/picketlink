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
package org.picketlink.json.sig;

import static org.picketlink.json.PicketLinkJSONConstants.COMMON.HMAC_SHA_256;
import static org.picketlink.json.PicketLinkJSONConstants.COMMON.PERIOD;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.Base64;
import org.picketlink.json.PicketLinkJSONMessages;
import org.picketlink.json.util.HmacSha256Util;
import org.picketlink.json.util.PicketLinkJSONUtil;

/**
 * Represents a JSON Web Signature
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class JSONWebSignature {
    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected JSONObject payload;
    protected JSONWebSignatureHeader header;

    /**
     * Create an attached header
     *
     * @param alg
     * @return
     */
    public JSONWebSignatureHeader createHeader(String alg) {
        if (header == null) {
            header = new JSONWebSignatureHeader(alg);
        }
        return header;
    }

    /**
     * Get the JSON Payload
     *
     * @return
     */
    public JSONObject getPayload() {
        return payload;
    }

    /**
     * Set the Payload
     *
     * @param payload
     */
    public void setPayload(JSONObject payload) {
        this.payload = payload;
    }

    /**
     * Set the Payload
     *
     * @param payload
     * @throws JSONException
     */
    public void setPayload(String pay) throws JSONException {
        this.payload = new JSONObject(pay);
    }

    /**
     * Get the JWS Header
     *
     * @return
     */
    public JSONWebSignatureHeader getHeader() {
        return header;
    }

    /**
     * Set the JWS Header
     *
     * @param header
     */
    public void setHeader(JSONWebSignatureHeader header) {
        this.header = header;
    }

    /**
     * Encode the Payload
     *
     * @return
     * @throws ProcessingException
     */
    public String encode() throws ProcessingException {
        if (header == null) {
            throw PicketLinkJSONMessages.MESSAGES.jsonWebSignatureHeaderMissing();
        }

        if (HMAC_SHA_256.equals(header.getAlg())) {
            return encodeUsingHmacSha26();
        }
        throw new RuntimeException();
    }

    /**
     * Decode the Payload
     *
     * @return
     * @throws ProcessingException
     */
    public static JSONWebSignature decode(String encoded) throws ProcessingException {
        String[] tokens = encoded.split("\\.");

        String encodedHeader = tokens[0];
        String encodedPayload = tokens[1];
        String encodedSignature = tokens[2];

        String decodedSignature = null;

        try {
            decodedSignature = new String(Base64.decode(encodedSignature), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e1);
        }

        // Validation
        String hmacEncodedSigValue = HmacSha256Util.encode(encodedHeader + PERIOD + encodedPayload);
        if (hmacEncodedSigValue.equals(decodedSignature) == false) {
            throw PicketLinkJSONMessages.MESSAGES.jsonWebSignatureValidationFailed();
        }
        JSONWebSignature sig = new JSONWebSignature();
        try {
            sig.setHeader(JSONWebSignatureHeader.create(new String(Base64.decode(encodedHeader), "UTF-8")));
            sig.setPayload(new String(Base64.decode(encodedPayload), "UTF-8"));
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
        return sig;
    }

    /**
     * Encode using HmacSha256
     *
     * @return
     * @throws ProcessingException
     */
    protected String encodeUsingHmacSha26() throws ProcessingException {
        try {
            // Encode the header
            String base64EncodedHeader = PicketLinkJSONUtil.b64Encode(header.get().toString());

            // Encode the payload
            String base64EncodedPayload = PicketLinkJSONUtil.b64Encode(payload.toString());

            StringBuilder securedInput = new StringBuilder(base64EncodedHeader);
            securedInput.append(PERIOD).append(base64EncodedPayload);

            String sigValue = HmacSha256Util.encode(securedInput.toString());

            String encodedSig = PicketLinkJSONUtil.b64Encode(sigValue);

            StringBuilder result = new StringBuilder();
            result.append(base64EncodedHeader).append(PERIOD).append(base64EncodedPayload).append(PERIOD).append(encodedSig);
            return result.toString();
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }
}
