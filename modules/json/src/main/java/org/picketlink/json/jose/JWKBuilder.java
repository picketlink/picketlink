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
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.JsonUtil.b64Decode;
import static org.picketlink.json.util.JsonUtil.b64Encode;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * @author Giriraj Sharma
 */
public class JWKBuilder<T extends JWK, B extends JWKBuilder<T, B>> {

    private final JsonObjectBuilder keyParametersBuilder;
    private final Class<T> tokenType;

    public JWKBuilder() {
        this((Class<T>) JWK.class);
    }

    protected JWKBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.keyParametersBuilder = Json.createObjectBuilder();
    }

    public B keyType(String type) {
        keyParameter(KEY_TYPE, type);
        return (B) this;
    }

    public B keyUse(String use) {
        keyParameter(KEY_USE, use);
        return (B) this;
    }

    public B keyOperations(String... keyOperations) {
        if (keyOperations.length == 1) {
            keyParameter(KEY_OPERATIONS, keyOperations[0]);
        } else if (keyOperations.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String operation : keyOperations) {
                arrayBuilder.add(operation);
            }

            this.keyParametersBuilder.add(KEY_OPERATIONS, arrayBuilder);
        }
        return (B) this;
    }

    public B keyAlgorithm(String algorithm) {
        keyParameter(KEY_ALGORITHM, algorithm);
        return (B) this;
    }

    public B keyIdentifier(String identifier) {
        keyParameter(KEY_IDENTIFIER, identifier);
        return (B) this;
    }

    public B X509Url(String url) {
        keyParameter(X509_URL, url);
        return (B) this;
    }

    public B X509CertificateChain(String... X509CertificateChain) {
        if (X509CertificateChain.length == 1) {
            keyParameter(X509_CERTIFICATE_CHAIN, X509CertificateChain[0]);
        } else if (X509CertificateChain.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String certificate : X509CertificateChain) {
                arrayBuilder.add(certificate);
            }

            this.keyParametersBuilder.add(X509_CERTIFICATE_CHAIN, arrayBuilder);
        }
        return (B) this;
    }

    public B X509CertificateSHA1Thumbprint(String sha1Thumbprint) {
        keyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT, sha1Thumbprint);
        return (B) this;
    }

    public B X509CertificateSHA256Thumbprint(String sha256Thumbprint) {
        keyParameter(X509_CERTIFICATE_SHA256_THUMBPRINT, sha256Thumbprint);
        return (B) this;
    }

    public B modulus(BigInteger modulus) {
        keyParameter(MODULUS, b64Encode(modulus.toByteArray()));
        return (B) this;
    }

    public B publicExponent(BigInteger publicExponent) {
        keyParameter(PUBLIC_EXPONENT, b64Encode(publicExponent.toByteArray()));
        return (B) this;
    }

    public B privateExponent(BigInteger privateExponent) {
        keyParameter(PRIVATE_EXPONENT, b64Encode(privateExponent.toByteArray()));
        return (B) this;
    }

    public B primeP(BigInteger primeP) {
        keyParameter(PRIME_P, b64Encode(primeP.toByteArray()));
        return (B) this;
    }

    public B primeQ(BigInteger primeQ) {
        keyParameter(PRIME_Q, b64Encode(primeQ.toByteArray()));
        return (B) this;
    }

    public B primeExponentP(BigInteger primeExponentP) {
        keyParameter(PRIME_EXPONENT_P, b64Encode(primeExponentP.toByteArray()));
        return (B) this;
    }

    public B primeExponentQ(BigInteger primeExponentQ) {
        keyParameter(PRIME_EXPONENT_Q, b64Encode(primeExponentQ.toByteArray()));
        return (B) this;
    }

    public B crtCoefficient(BigInteger crtCoefficient) {
        keyParameter(CRT_COEFFICIENT, b64Encode(crtCoefficient.toByteArray()));
        return (B) this;
    }

    public B curve(String curve) {
        keyParameter(CURVE, curve);
        return (B) this;
    }

    public B x(BigInteger x) {
        keyParameter(PUBLIC_X, b64Encode(x.toByteArray()));
        return (B) this;
    }

    public B y(BigInteger y) {
        keyParameter(PUBLIC_Y, b64Encode(y.toByteArray()));
        return (B) this;
    }

    public B d(BigInteger d) {
        keyParameter(PRIVATE_D, b64Encode(d.toByteArray()));
        return (B) this;
    }

    public B k(BigInteger value) {
        keyParameter(PRIVATE_KEY_VALUE, b64Encode(value.toByteArray()));
        return (B) this;
    }

    public B keyParameter(String name, String... value) {
        setString(this.keyParametersBuilder, name, value);
        return (B) this;
    }

    public T build() {
        return build(this.keyParametersBuilder.build());
    }

    public T build(String json) {

        byte[] keyParameters = b64Decode(json);
        return build(Json.createReader(new ByteArrayInputStream(keyParameters)).readObject());
    }

    protected JsonObjectBuilder getkeyParametersBuilder() {
        return this.keyParametersBuilder;
    }

    protected Class<T> getTokenType() {
        return this.tokenType;
    }

    protected T build(JsonObject keyParametersObject) {
        try {
            Constructor<T> constructor = this.tokenType.getDeclaredConstructor(JsonObject.class);

            constructor.setAccessible(true);

            return constructor.newInstance(keyParametersObject);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateToken(this.tokenType, e);
        }
    }

    private B setString(JsonObjectBuilder builder, String name, String... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String value : values) {
                arrayBuilder.add(value.toString());
            }

            builder.add(name, arrayBuilder);
        }
        return (B) this;
    }
}
