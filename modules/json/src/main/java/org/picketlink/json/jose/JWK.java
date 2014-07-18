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
package org.picketlink.json.jose;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;
import static org.picketlink.json.JsonConstants.JWK.KEY_ALGORITHM;
import static org.picketlink.json.JsonConstants.JWK.KEY_IDENTIFIER;
import static org.picketlink.json.JsonConstants.JWK.KEY_OPERATIONS;
import static org.picketlink.json.JsonConstants.JWK.KEY_TYPE;
import static org.picketlink.json.JsonConstants.JWK.KEY_USE;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_CHAIN;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA1_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA256_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_URL;
import static org.picketlink.json.JsonConstants.JWK_EC.CURVE;
import static org.picketlink.json.JsonConstants.JWK_EC.PRIVATE_D;
import static org.picketlink.json.JsonConstants.JWK_EC.PUBLIC_X;
import static org.picketlink.json.JsonConstants.JWK_EC.PUBLIC_Y;
import static org.picketlink.json.JsonConstants.JWK_OCTET.PRIVATE_KEY_VALUE;
import static org.picketlink.json.JsonConstants.JWK_RSA.CRT_COEFFICIENT;
import static org.picketlink.json.JsonConstants.JWK_RSA.MODULUS;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_EXPONENT_P;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_EXPONENT_Q;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_P;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_Q;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIVATE_EXPONENT;
import static org.picketlink.json.JsonConstants.JWK_RSA.PUBLIC_EXPONENT;
import static org.picketlink.json.util.JsonUtil.b64Decode;
import static org.picketlink.json.util.JsonUtil.b64Encode;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * @author Giriraj Sharma
 */
public class JWK {

    private JsonObject keyParameters;

    protected JWK(JsonObject keyParameters) {
        this.keyParameters = keyParameters;
    }

    public String encode() {
        return b64Encode(getPlainkeyParameters());
    }

    public String getKeyType() {
        return getKeyParameter(KEY_TYPE);
    }

    public String getKeyUse() {
        return getKeyParameter(KEY_USE);
    }

    public List<String> getKeyOperations() {
        return getKeyParameterValues(KEY_OPERATIONS);
    }

    public String getKeyAlgorithm() {
        return getKeyParameter(KEY_ALGORITHM);
    }

    public String getKeyIdentifier() {
        return getKeyParameter(KEY_IDENTIFIER);
    }

    public String getX509Url() {
        return getKeyParameter(X509_URL);
    }

    public List<String> getX509CertificateChain() {
        return getKeyParameterValues(X509_CERTIFICATE_CHAIN);
    }

    public String getX509SHA1CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT);
    }

    public String getX509SHA256CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA256_THUMBPRINT);
    }

    public String getModulus() {
        return getKeyParameter(MODULUS);
    }

    public String getPublicExponent() {
        return getKeyParameter(PUBLIC_EXPONENT);
    }

    public String getPrivateExponent() {
        return getKeyParameter(PRIVATE_EXPONENT);
    }

    public String getPrimeP() {
        return getKeyParameter(PRIME_P);
    }

    public String getPrimeQ() {
        return getKeyParameter(PRIME_Q);
    }

    public String getPrimeExponentP() {
        return getKeyParameter(PRIME_EXPONENT_P);
    }

    public String getPrimeExponentQ() {
        return getKeyParameter(PRIME_EXPONENT_Q);
    }

    public String getCRTCoefficient() {
        return getKeyParameter(CRT_COEFFICIENT);
    }

    public String getCurve() {
        return getKeyParameter(CURVE);
    }

    public String getX() {
        return getKeyParameter(PUBLIC_X);
    }

    public String getY() {
        return getKeyParameter(PUBLIC_Y);
    }

    public String getD() {
        return getKeyParameter(PRIVATE_D);
    }

    public String getK() {
        return getKeyParameter(PRIVATE_KEY_VALUE);
    }

    @Override
    public String toString() {
        return getPlainkeyParameters();
    }

    public JsonObject getKeyParameters() {
        return this.keyParameters;
    }

    public String getKeyParameter(String name) {
        return getValue(name, this.keyParameters);
    }

    public List<String> getKeyParameterValues(String name) {
        return getValues(name, this.keyParameters);
    }

    private String getPlainkeyParameters() {
        StringWriter keyParameterWriter = new StringWriter();

        Json.createWriter(keyParameterWriter).writeObject(this.keyParameters);

        return keyParameterWriter.getBuffer().toString();
    }

    private List<String> getValues(String name, JsonObject jsonObject) {
        JsonValue headerValue = jsonObject.get(name);
        List<String> values = new ArrayList<String>();

        if (headerValue != null) {
            if (JsonArray.class.isInstance(headerValue)) {
                JsonArray array = (JsonArray) headerValue;

                for (JsonValue value : array.getValuesAs(JsonValue.class)) {
                    values.add(getValue(value).toString());
                }
            } else {
                values.add(getValue(name, jsonObject).toString());
            }
        }

        return values;
    }

    private <R> R getValue(JsonValue value) {
        if (ARRAY.equals(value.getValueType())) {
            JsonArray array = (JsonArray) value;
            for (JsonValue jsonValue : array) {
                return getValue(jsonValue);
            }
        } else if (STRING.equals(value.getValueType())) {
            return (R) ((JsonString) value).getString();
        } else if (NUMBER.equals(value.getValueType())) {
            return (R) ((JsonNumber) value).bigDecimalValue().toPlainString();
        } else if (TRUE.equals(value.getValueType()) || FALSE.equals(value.getValueType())) {
            return (R) Boolean.valueOf(value.toString());
        }

        return null;
    }

    private String getValue(String name, JsonObject jsonObject) {
        JsonValue value = jsonObject.get(name);

        if (ARRAY.equals(value.getValueType())) {
            JsonArray array = (JsonArray) value;
            for (JsonValue jsonValue : array) {
                return getValue(jsonValue);
            }
        } else if (STRING.equals(value.getValueType())) {
            return ((JsonString) value).getString();
        } else if (NUMBER.equals(value.getValueType())) {
            return ((JsonNumber) value).bigDecimalValue().toPlainString();
        } else if (TRUE.equals(value.getValueType()) || FALSE.equals(value.getValueType())) {
            return value.toString();
        }

        return null;
    }

    public static RSAPublicKey toRSAPublicKey(JWK rsaJWK)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        BigInteger modulus = new BigInteger(b64Decode(rsaJWK.getModulus()));
        BigInteger publicExponent = new BigInteger(b64Decode(rsaJWK.getPublicExponent()));

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) factory.generatePublic(spec);
    }

    public static RSAPrivateKey toRSAPrivateKey(JWK rsaJWK)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        RSAPrivateKeySpec spec;

        if (rsaJWK.getPrivateExponent() == null) {
            return null;
        }

        BigInteger modulus = new BigInteger(b64Decode(rsaJWK.getModulus()));
        BigInteger privateExponent = new BigInteger(b64Decode(rsaJWK.getPrivateExponent()));

        if (rsaJWK.getPrimeP() == null) {
            spec = new RSAPrivateKeySpec(modulus, privateExponent);
        } else {
            BigInteger publicExponent = new BigInteger(b64Decode(rsaJWK.getPublicExponent()));
            BigInteger primeP = new BigInteger(b64Decode(rsaJWK.getPrimeP()));
            BigInteger primeQ = new BigInteger(b64Decode(rsaJWK.getPrimeQ()));
            BigInteger primeExponentP = new BigInteger(b64Decode(rsaJWK.getPrimeExponentP()));
            BigInteger primeExponentQ = new BigInteger(b64Decode(rsaJWK.getPrimeExponentQ()));
            BigInteger crtCoefficient = new BigInteger(b64Decode(rsaJWK.getCRTCoefficient()));

            spec = new RSAPrivateCrtKeySpec(modulus,
                publicExponent,
                privateExponent,
                primeP,
                primeQ,
                primeExponentP,
                primeExponentQ,
                crtCoefficient);
        }
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    // TODO : FIX ME Using Bouncy Castle API Classes
    // public static ECPublicKey toECPublicKey(JWK ecJWK)
    // throws NoSuchAlgorithmException, InvalidKeySpecException {
    //
    // ECParameterSpec spec = crv.toECParameterSpec();
    // if (spec == null) {
    // throw new NoSuchAlgorithmException("Couldn't get EC parameter spec for curve " + crv);
    // }
    //
    // BigInteger x = new BigInteger(b64Decode(ecJWK.getX()));
    // BigInteger y = new BigInteger(b64Decode(ecJWK.getY()));
    // ECPoint w = new ECPoint(x, y);
    // ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(w, spec);
    // KeyFactory keyFactory = KeyFactory.getInstance("EC");
    //
    // return (ECPublicKey)keyFactory.generatePublic(publicKeySpec);
    // }

    // TODO : FIX ME Using Bouncy Castle API Classes
    // public static ECPrivateKey toECPrivateKey(JWK ecJWK)
    // throws NoSuchAlgorithmException, InvalidKeySpecException {
    //
    // BigInteger d = new BigInteger(b64Decode(ecJWK.getD()));
    // if (d == null) {
    // return null;
    // }
    //
    // ECParameterSpec spec = crv.toECParameterSpec();
    // if (spec == null) {
    // throw new NoSuchAlgorithmException("Couldn't get EC parameter spec for curve " + crv);
    // }
    //
    // ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(d, spec);
    // KeyFactory keyFactory = KeyFactory.getInstance("EC");
    // return (ECPrivateKey)keyFactory.generatePrivate(privateKeySpec);
    // }

}
