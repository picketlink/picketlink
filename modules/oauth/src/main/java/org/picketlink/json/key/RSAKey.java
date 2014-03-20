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
package org.picketlink.json.key;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import org.json.JSONException;
import org.json.JSONObject;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.Base64;
import org.picketlink.json.PicketLinkJSONMessages;
import org.picketlink.json.PicketLinkJSONConstants;
import org.picketlink.json.util.PicketLinkJSONUtil;

/**
 * RSA based public key JSON representation
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class RSAKey implements JSONKey {
    protected String kid;
    protected String mod;
    protected String exp;
    private KeyUse keyUse;

    /**
     * Get the Algorithm
     */
    @Override
    public String getAlg() {
        return PicketLinkJSONConstants.RSA;
    }

    /**
     * Get the K-ID
     */
    @Override
    public String getKid() {
        return kid;
    }

    /**
     * Set the K-ID
     *
     * @param kid
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    /**
     * Parse a {@link JSONObject} into a {@link RSAKey}
     *
     * @param json
     * @throws JSONException
     */
    public void parse(JSONObject json) throws JSONException {
        String alg = json.getString(PicketLinkJSONConstants.COMMON.ALG);
        if (PicketLinkJSONConstants.RSA.equals(alg) == false) {
            throw PicketLinkJSONMessages.MESSAGES.wrongJsonKey();
        }
        kid = json.getString(PicketLinkJSONConstants.KID);
        mod = json.getString(PicketLinkJSONConstants.MOD);
        exp = json.getString(PicketLinkJSONConstants.EXP);
    }

    /**
     * Convert into a {@link JSONObject}
     *
     * @return
     * @throws JSONException
     */
    public JSONObject convert() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(PicketLinkJSONConstants.COMMON.ALG, getAlg());
        json.put(PicketLinkJSONConstants.EXP, exp);
        json.put(PicketLinkJSONConstants.MOD, mod);
        json.put(PicketLinkJSONConstants.KID, kid);
        return json;
    }

    @Override
    public KeyUse getUse() {
        return keyUse;
    }

    /**
     * Set the Key Use
     *
     * @param ku
     */
    public void setUse(KeyUse ku) {
        this.keyUse = ku;
    }

    public static RSAKey convert(RSAPublicKey publicKey) throws ProcessingException {
        BigInteger modulus = publicKey.getModulus();
        BigInteger exponent = publicKey.getPublicExponent();

        RSAKey rsaKey = new RSAKey();
        rsaKey.setMod(PicketLinkJSONUtil.b64Encode(modulus.toByteArray()));
        rsaKey.setExp(PicketLinkJSONUtil.b64Encode(exponent.toByteArray()));
        return rsaKey;
    }

    /**
     * Convert to the JDK representation of a RSA Public Key
     *
     * @return
     * @throws ProcessingException
     */
    public RSAPublicKey convertToPublicKey() throws ProcessingException {
        BigInteger bigModulus = new BigInteger(1, massage(Base64.decode(mod)));
        BigInteger bigEx = new BigInteger(1, massage(Base64.decode(exp)));

        try {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("rsa");
            RSAPublicKeySpec kspec = new RSAPublicKeySpec(bigModulus, bigEx);
            return (RSAPublicKey) rsaKeyFactory.generatePublic(kspec);
        } catch (Exception e) {
            throw PicketLinkJSONMessages.MESSAGES.processingException(e);
        }
    }

    private byte[] massage(byte[] byteArray) {
        if (byteArray[0] == 0) {
            byte[] substring = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, substring, 0, byteArray.length - 1);
            return substring;
        }
        return byteArray;
    }
}
