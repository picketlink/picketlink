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
package org.picketlink.json.jose;

/**
 * Represents the JsonWebEncryptionHeader
 *
 * @author anil saldhana
 * @since Jul 27, 2012
 */
public class JsonWebEncryptionHeader {
//FIXME: need to review JWE and support JSR-353
//    private JSONObject delegate;
//
//    /**
//     * Get the underlying {@link JSONObject}
//     *
//     * @return
//     */
//    public JSONObject getDelegate() {
//        return delegate;
//    }
//
//    /**
//     * Set the underlying {@link JSONObject}
//     *
//     * @param delegate
//     */
//    public void setDelegate(JSONObject delegate) {
//        this.delegate = delegate;
//    }
//
//    /**
//     * Check if there is a need for integrity value
//     *
//     * @return
//     */
//    public boolean needIntegrity() {
//        if (delegate != null)
//            try {
//                return delegate.getString("int") != null;
//            } catch (JSONException e) {
//                throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
//            }
//        else
//            return false;
//    }
//
//    /**
//     * Based on the alg entry, determine the {@link Cipher}
//     *
//     * @return
//     * @throws ProcessingException
//     */
//    public Cipher getCipherBasedOnAlg() throws ProcessingException {
//        try {
//            if (delegate == null) {
//                return Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }
//
//            if (JsonConstants.JWE.ENC_ALG_RSA_OAEP.equals(delegate.getString(ALG))) {
//                return Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
//            } else if (JsonConstants.JWE.ENC_ALG_RSA1_5.equals(delegate.getString(ALG))) {
//                return Cipher.getInstance("RSA/ECB/PKCS1Padding");
//            }
//        } catch (Exception e) {
//            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
//        }
//        return null;
//    }
//
//    /**
//     * Based on the enc entry, determine the {@link Cipher}
//     *
//     * @return
//     * @throws ProcessingException
//     */
//    public Cipher getCipherBasedOnEnc() throws ProcessingException {
//        if (delegate != null) {
//            String enc = null;
//            try {
//                enc = delegate.getString(ENC);
//            } catch (JSONException e1) {
//                throw PicketLinkJSONMessages.MESSAGES.ignorableError(e1);
//            }
//            if (enc.contains("CBC")) {
//                try {
//                    return Cipher.getInstance("AES/CBC/PKCS5Padding");
//                } catch (Exception e) {
//                    throw PicketLinkJSONMessages.MESSAGES.processingException(e);
//                }
//            }
//        }
//        return null;
//    }
//
//    /**
//     * Get the CEK length
//     *
//     * @return
//     */
//    public int getCEKLength() {
//        int cekLength = 128 / 8;
//        if (delegate == null)
//            return cekLength;
//
//        String enc = null;
//        try {
//            enc = delegate.getString(ENC);
//        } catch (JSONException e) {
//            throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
//        }
//        if (ENC_ALG_A128CBC.equals(enc)) {
//            cekLength = 128 / 8;
//        } else if (ENC_ALG_A192CBC.equals(enc)) {
//            cekLength = 192 / 8;
//        } else if (ENC_ALG_A256CBC.equals(enc)) {
//            cekLength = 256 / 8;
//        } else if (ENC_ALG_A512CBC.equals(enc)) {
//            cekLength = 512 / 8;
//        }
//        return cekLength;
//    }
//
//    /**
//     * Get the CIK length
//     *
//     * @return
//     */
//    public int getCIKLength() {
//        int cikLength = 256 / 8;
//        if (delegate == null)
//            return cikLength;
//
//        String integrity = null;
//
//        try {
//            integrity = delegate.getString("int");
//        } catch (JSONException e) {
//            throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
//        }
//
//        if (SIGN_ALG_HS256.equals(integrity)) {
//            cikLength = 256 / 8;
//        } else if (SIGN_ALG_HS384.equals(integrity)) {
//            cikLength = 384 / 8;
//        } else if (SIGN_ALG_HS512.equals(integrity)) {
//            cikLength = 512 / 8;
//        }
//        return cikLength;
//    }
//
//    /**
//     * Get the Message Authentication Code algorithm
//     *
//     * @return
//     */
//    public String getMessageAuthenticationCodeAlgo() {
//        String algo = "HMACSHA256";
//        if (delegate == null)
//            return algo;
//
//        String integrity = null;
//
//        try {
//            integrity = delegate.getString("int");
//        } catch (JSONException e) {
//            throw PicketLinkJSONMessages.MESSAGES.ignorableError(e);
//        }
//
//        if ("HS256".equals(integrity)) { // HMAC SHA-256
//            algo = "HMACSHA256";
//        } else if ("HS384".equals(integrity)) { // HMAC SHA-384
//            algo = "HMACSHA384";
//        } else if ("HS512".equals(integrity)) { // HMAC SHA-512
//            algo = "HMACSHA512";
//        }
//        return algo;
//    }
//
//    /**
//     * Given a JSON String, load internals
//     *
//     * @param json
//     * @throws ProcessingException
//     */
//    public void load(String json) throws ProcessingException {
//        try {
//            this.delegate = new JSONObject(json);
//        } catch (JSONException j) {
//            throw PicketLinkJSONMessages.MESSAGES.processingException(j);
//        }
//    }
//
//    /**
//     * Provide a JSON Representation
//     */
//    @Override
//    public String toString() {
//        if (delegate == null)
//            return "";
//
//        return delegate.toString();
//    }
}
