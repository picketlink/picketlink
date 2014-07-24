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
import static org.picketlink.json.JsonConstants.JWK_RSA.CRT_COEFFICIENT;
import static org.picketlink.json.JsonConstants.JWK_RSA.MODULUS;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_EXPONENT_P;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_EXPONENT_Q;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_P;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIME_Q;
import static org.picketlink.json.JsonConstants.JWK_RSA.PRIVATE_EXPONENT;
import static org.picketlink.json.JsonConstants.JWK_RSA.PUBLIC_EXPONENT;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.JsonUtil.b64Decode;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
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
 * The Class JWK.
 *
 * @author Giriraj Sharma
 */
public class JWK {

    /** The key parameters. */
    private JsonObject keyParameters;

    /**
     * Instantiates a new jwk.
     *
     * @param keyParameters the key parameters
     */
    protected JWK(JsonObject keyParameters) {
        this.keyParameters = keyParameters;
    }

    /**
     * Gets the key type.
     *
     * @return the key type
     */
    public String getKeyType() {
        return getKeyParameter(KEY_TYPE);
    }

    /**
     * Gets the key use.
     *
     * @return the key use
     */
    public String getKeyUse() {
        return getKeyParameter(KEY_USE);
    }

    /**
     * Gets the key operations.
     *
     * @return the key operations
     */
    public List<String> getKeyOperations() {
        return getKeyParameterValues(KEY_OPERATIONS);
    }

    /**
     * Gets the key algorithm.
     *
     * @return the key algorithm
     */
    public String getKeyAlgorithm() {
        return getKeyParameter(KEY_ALGORITHM);
    }

    /**
     * Gets the key identifier.
     *
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return getKeyParameter(KEY_IDENTIFIER);
    }

    /**
     * Gets the x509 url.
     *
     * @return the x509 url
     */
    public String getX509Url() {
        return getKeyParameter(X509_URL);
    }

    /**
     * Gets the x509 certificate chain.
     *
     * @return the x509 certificate chain
     */
    public List<String> getX509CertificateChain() {
        return getKeyParameterValues(X509_CERTIFICATE_CHAIN);
    }

    /**
     * Gets the x509 sha1 certificate thumbprint.
     *
     * @return the x509 sha1 certificate thumbprint
     */
    public String getX509SHA1CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT);
    }

    /**
     * Gets the x509 sha256 certificate thumbprint.
     *
     * @return the x509 sha256 certificate thumbprint
     */
    public String getX509SHA256CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA256_THUMBPRINT);
    }

    /**
     * Gets the modulus.
     *
     * @return the modulus
     */
    public String getModulus() {
        return getKeyParameter(MODULUS);
    }

    /**
     * Gets the public exponent.
     *
     * @return the public exponent
     */
    public String getPublicExponent() {
        return getKeyParameter(PUBLIC_EXPONENT);
    }

    /**
     * Gets the private exponent.
     *
     * @return the private exponent
     */
    public String getPrivateExponent() {
        return getKeyParameter(PRIVATE_EXPONENT);
    }

    /**
     * Gets the prime p.
     *
     * @return the prime p
     */
    public String getPrimeP() {
        return getKeyParameter(PRIME_P);
    }

    /**
     * Gets the prime q.
     *
     * @return the prime q
     */
    public String getPrimeQ() {
        return getKeyParameter(PRIME_Q);
    }

    /**
     * Gets the prime exponent p.
     *
     * @return the prime exponent p
     */
    public String getPrimeExponentP() {
        return getKeyParameter(PRIME_EXPONENT_P);
    }

    /**
     * Gets the prime exponent q.
     *
     * @return the prime exponent q
     */
    public String getPrimeExponentQ() {
        return getKeyParameter(PRIME_EXPONENT_Q);
    }

    /**
     * Gets the CRT coefficient.
     *
     * @return the CRT coefficient
     */
    public String getCRTCoefficient() {
        return getKeyParameter(CRT_COEFFICIENT);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getPlainkeyParameters();
    }

    /**
     * Gets the key parameter.
     *
     * @param name the name
     * @return the key parameter
     */
    private String getKeyParameter(String name) {
        return getValue(name, this.keyParameters);
    }

    /**
     * Gets the key parameter values.
     *
     * @param name the name
     * @return the key parameter values
     */
    public List<String> getKeyParameterValues(String name) {
        return getValues(name, this.keyParameters);
    }

    /**
     * Gets the plainkey parameters.
     *
     * @return the plainkey parameters
     */
    private String getPlainkeyParameters() {
        StringWriter keyParameterWriter = new StringWriter();

        Json.createWriter(keyParameterWriter).writeObject(this.keyParameters);

        return keyParameterWriter.getBuffer().toString();
    }

    /**
     * Gets the values.
     *
     * @param name the name
     * @param jsonObject the json object
     * @return the values
     */
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

    /**
     * Gets the value.
     *
     * @param <R> the generic type
     * @param value the value
     * @return the value
     */
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

    /**
     * Gets the value.
     *
     * @param name the name
     * @param jsonObject the json object
     * @return the value
     */
    private String getValue(String name, JsonObject jsonObject) {
        JsonValue value = jsonObject.get(name);

        if (value != null) {
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
        }

        return null;
    }

    /**
     * To rsa public key.
     *
     * @return the RSA public key
     */
    public RSAPublicKey toRSAPublicKey() {
        if (getModulus() == null) {
            throw MESSAGES.invalidNullArgument("Modulus");
        }

        if (getPublicExponent() == null) {
            throw MESSAGES.invalidNullArgument("Public Exponent");
        }

        try {
            BigInteger modulus = new BigInteger(b64Decode(getModulus()));
            BigInteger publicExponent = new BigInteger(b64Decode(getPublicExponent()));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (Exception e) {
            throw MESSAGES.cryptoCouldNotParseKey(toString(), e);
        }
    }

    /**
     * Gets the json object.
     *
     * @return the json object
     */
    public JsonObject getJsonObject() {
        return this.keyParameters;
    }
}