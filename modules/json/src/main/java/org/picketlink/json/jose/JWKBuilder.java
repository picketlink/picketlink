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
import static org.picketlink.json.util.JsonUtil.b64Encode;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * The base class for building JSON Web Keys (JWKs) with desired key parameters.
 *
 * <p>
 * The following JSON object members are common to all JWK types:
 *
 * <ul>
 * <li>{@link #getKeyType kty} (required)
 * <li>{@link #getKeyUse use} (optional)
 * <li>{@link #getKeyOperations key_ops} (optional)
 * <li>{@link #getKeyID kid} (optional)
 * </ul>
 *
 * @param <T> the generic type
 * @param <B> the generic type
 *
 * @author Giriraj Sharma
 */

public class JWKBuilder<T extends JWK, B extends JWKBuilder<?, ?>> {

    /** The key parameters builder. */
    private final JsonObjectBuilder keyParametersBuilder;

    /** Token type. */
    private final Class<T> tokenType;

    /**
     * Instantiates a new JWK builder.
     */
    public JWKBuilder() {
        this((Class<T>) JWK.class);
    }

    /**
     * Instantiates a new JWK builder.
     *
     * @param tokenTypeoken type
     */
    protected JWKBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.keyParametersBuilder = Json.createObjectBuilder();
    }

    /**
     * Sets the key type, required.
     *
     * @param type
     * @return
     */
    public JWKBuilder<T, B> keyType(String type) {
        keyParameter(KEY_TYPE, type);
        return this;
    }

    /**
     * Sets the key use, optional.
     *
     * @param use the use
     * @return
     */
    public JWKBuilder<T, B> keyUse(String use) {
        keyParameter(KEY_USE, use);
        return this;
    }

    /**
     * Sets the key operations, optional.
     *
     * <ul>
     * <li>{@link #SIGN sign}
     * <li>{@link #VERIFY verify}
     * <li>{@link #ENCRYPT encrypt}
     * <li>{@link #DECRYPT decrypt}
     * <li>{@link #WRAP_KEY wrapKey}
     * <li>{@link #UNWRAP_KEY unwrapKey}
     * <li>{@link #DERIVE_KEY deriveKey}
     * <li>{@link #DERIVE_BITS deriveBits}
     * </ul>
     *
     * @param keyOperations the key operations
     * @return
     */
    public JWKBuilder<T, B> keyOperations(String... keyOperations) {
        if (keyOperations.length == 1) {
            keyParameter(KEY_OPERATIONS, keyOperations[0]);
        } else if (keyOperations.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String operation : keyOperations) {
                arrayBuilder.add(operation);
            }

            this.keyParametersBuilder.add(KEY_OPERATIONS, arrayBuilder);
        }
        return this;
    }

    /**
     * Sets the intended JOSE algorithm for the key, optional.
     *
     * @param algorithm the algorithm
     * @return
     */
    public JWKBuilder<T, B> keyAlgorithm(String algorithm) {
        keyParameter(KEY_ALGORITHM, algorithm);
        return this;
    }

    /**
     * Sets the key identifier, optional.
     *
     * @param identifier the identifier
     * @return
     */
    public JWKBuilder<T, B> keyIdentifier(String identifier) {
        keyParameter(KEY_IDENTIFIER, identifier);
        return this;
    }

    /**
     * Sets the x509 URL.
     *
     * @param url the url
     * @return
     */
    public JWKBuilder<T, B> X509Url(String url) {
        keyParameter(X509_URL, url);
        return this;
    }

    /**
     * Sets the x509 certificate chain.
     *
     * @param X509CertificateChain the x509 certificate chain
     * @return
     */
    public JWKBuilder<T, B> X509CertificateChain(String... X509CertificateChain) {
        if (X509CertificateChain.length == 1) {
            keyParameter(X509_CERTIFICATE_CHAIN, X509CertificateChain[0]);
        } else if (X509CertificateChain.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String certificate : X509CertificateChain) {
                arrayBuilder.add(certificate);
            }

            this.keyParametersBuilder.add(X509_CERTIFICATE_CHAIN, arrayBuilder);
        }
        return this;
    }

    /**
     * Sets the x509 SHA1 certificate thumbprint.
     *
     * @param sha1Thumbprint the SHA1 thumbprint
     * @return
     */
    public JWKBuilder<T, B> X509CertificateSHA1Thumbprint(String sha1Thumbprint) {
        keyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT, sha1Thumbprint);
        return this;
    }

    /**
     * Sets the x509 SHA256 certificate thumbprint.
     *
     * @param sha256Thumbprint the SHA256 thumbprint
     * @return
     */
    public JWKBuilder<T, B> X509CertificateSHA256Thumbprint(String sha256Thumbprint) {
        keyParameter(X509_CERTIFICATE_SHA256_THUMBPRINT, sha256Thumbprint);
        return this;
    }

    /**
     * Sets the modulus value for the RSA key.
     *
     * @param modulus the modulus
     * @return
     */
    public JWKBuilder<T, B> modulus(BigInteger modulus) {
        keyParameter(MODULUS, b64Encode(modulus.toByteArray()));
        return this;
    }

    /**
     * Sets the public exponent of the RSA key.
     *
     * @param publicExponent the public exponent
     * @return
     */
    public JWKBuilder<T, B> publicExponent(BigInteger publicExponent) {
        keyParameter(PUBLIC_EXPONENT, b64Encode(publicExponent.toByteArray()));
        return this;
    }

    /**
     * Sets the private exponent of the RSA key.
     *
     * @param privateExponent the private exponent
     * @return
     */
    public JWKBuilder<T, B> privateExponent(BigInteger privateExponent) {
        keyParameter(PRIVATE_EXPONENT, b64Encode(privateExponent.toByteArray()));
        return this;
    }

    /**
     * Sets the first prime factor of the private RSA key.
     *
     * @param primeP the prime p
     * @return
     */
    public JWKBuilder<T, B> primeP(BigInteger primeP) {
        keyParameter(PRIME_P, b64Encode(primeP.toByteArray()));
        return this;
    }

    /**
     * Sets second prime factor of the private RSA key.
     *
     * @param primeQ the prime q
     * @return
     */
    public JWKBuilder<T, B> primeQ(BigInteger primeQ) {
        keyParameter(PRIME_Q, b64Encode(primeQ.toByteArray()));
        return this;
    }

    /**
     * Sets the first factor Chinese Remainder Theorem exponent of the private RSA key.
     *
     * @param primeExponentP the prime exponent p
     * @return
     */
    public JWKBuilder<T, B> primeExponentP(BigInteger primeExponentP) {
        keyParameter(PRIME_EXPONENT_P, b64Encode(primeExponentP.toByteArray()));
        return this;
    }

    /**
     * Sets the second factor Chinese Remainder Theorem exponent of the private RSA key.
     *
     * @param primeExponentQ the prime exponent q
     * @return
     */
    public JWKBuilder<T, B> primeExponentQ(BigInteger primeExponentQ) {
        keyParameter(PRIME_EXPONENT_Q, b64Encode(primeExponentQ.toByteArray()));
        return this;
    }

    /**
     * Sets the The first Chinese Remainder Theorem coefficient of the private RSA key.
     *
     * @param crtCoefficient the CRT coefficient
     * @return
     */
    public JWKBuilder<T, B> crtCoefficient(BigInteger crtCoefficient) {
        keyParameter(CRT_COEFFICIENT, b64Encode(crtCoefficient.toByteArray()));
        return this;
    }

    /**
     * Sets the Key parameter.
     *
     * @param name the name of key parameter
     * @param value the value(s) of key parameter
     * @return
     */
    public JWKBuilder<T, B> keyParameter(String name, String... value) {
        setString(this.keyParametersBuilder, name, value);
        return this;
    }

    /**
     * Builds {@link javax.json.JsonObjectBuilder} of key parameters.
     *
     * @return
     */
    public T build() {
        return build(this.keyParametersBuilder.build());
    }

    /**
     * Builds the String JSON.
     *
     * @param json the JSON formatted string
     * @return
     */
    public T build(String json) {
        return build(Json.createReader(new ByteArrayInputStream(json.getBytes())).readObject());
    }

    /**
     * Gets the key parameters builder.
     *
     * @return the key parameters builder
     */
    protected JsonObjectBuilder getkeyParametersBuilder() {
        return this.keyParametersBuilder;
    }

    /**
     * Gets token type.
     *
     * @return token type
     */
    protected Class<T> getTokenType() {
        return this.tokenType;
    }

    /**
     * Builds the key {@link javax.json.JsonObject}.
     *
     * @param keyParametersObject the key parameters object
     * @return
     */
    protected T build(JsonObject keyParametersObject) {
        try {
            Constructor<T> constructor = this.tokenType.getDeclaredConstructor(JsonObject.class);
            constructor.setAccessible(true);

            return constructor.newInstance(keyParametersObject);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateToken(this.tokenType, e);
        }
    }

    /**
     * Updates the {@link javax.json.JsonObjectBuilder} with specified key parameter and its value(s).
     *
     * @param builderuilder
     * @param name the name
     * @param values the values
     * @return
     */
    private JWKBuilder<T, B> setString(JsonObjectBuilder builder, String name, String... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String value : values) {
                arrayBuilder.add(value.toString());
            }
            builder.add(name, arrayBuilder);
        }
        return this;
    }
}