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

import static org.picketlink.json.JsonConstants.COMMON.ALG;
import static org.picketlink.json.JsonConstants.COMMON.ENC;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_CONTENT_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JSON_WEB_KEY;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JWK_SET_URL;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.KEY_ID;
import static org.picketlink.json.JsonConstants.JWE.COMPRESSION_ALG;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_CHAIN;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA1_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA256_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_URL;
import static org.picketlink.json.JsonConstants.JWE.CEK_BITLENGTH;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.JsonUtil.b64Decode;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * JSON Web Encryption (JWE) header Builder.
 *
 * <p>
 * Supports build of all Principal Registered Parameter Names of the JWE specification:
 *
 * <ul>
 * <li>alg
 * <li>enc
 * <li>epk
 * <li>zip
 * <li>jku
 * <li>jwk
 * <li>x5u
 * <li>x5t
 * <li>x5c
 * <li>kid
 * <li>typ
 * <li>cty
 * </ul>
 *
 * <p>
 * Example header:
 *
 * <pre>
 * {
 *   "alg" : "RSA1_5",
 *   "enc" : "A128CBC-HS256"
 * }
 * </pre>
 *
 * @param <T> the generic type
 * @param <B> the generic type
 * @author Giriraj Sharma
 */

public class JWEBuilder<T extends JWE, B extends JWEBuilder<T, B>> {

    /** The header builder. */
    private final JsonObjectBuilder headerBuilder;

    /** The token type. */
    private final Class<T> tokenType;

    /**
     * Instantiates a new JWE builder.
     */
    public JWEBuilder() {
        this((Class<T>) JWE.class);
    }

    /**
     * Instantiates a new {@link org.picketlink.json.jose.JWE} builder.
     *
     * @param tokenType the token type
     */
    protected JWEBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.headerBuilder = Json.createObjectBuilder();
    }

    /**
     * Sets the type of JOSE Header.
     *
     * @param type the String type
     * @return
     */
    public JWEBuilder<T, B> type(String type) {
        header(HEADER_TYPE, type);
        return this;
    }

    /**
     * Sets the content type of JOSE Header.
     *
     * @param contentType the String content type
     * @return
     */
    public JWEBuilder<T, B> contentType(String contentType) {
        header(HEADER_CONTENT_TYPE, contentType);
        return this;
    }

    /**
     * Sets the algorithm used to encrypt or determine the value of the Content Encryption Key (CEK).
     *
     *
     * <ul>
     * <li>{@link #RSA1_5}
     * <li>{@link #RSA_OAEP RSA-OAEP}
     * <li>{@link #RSA_OAEP_256 RSA-OAEP-256}
     * </ul>
     *
     * @param algorithm the algorithm as a string
     * @return
     */
    public JWEBuilder<T, B> algorithm(String algorithm) {
        header(ALG, algorithm);
        return this;
    }

    /**
     * Gets the encryption algorithm used to encrypt the Plaintext to produce the Ciphertext.
     *
     * <ul>
     * <li>{@link #A128CBC_HS256 A128CBC-HS256}
     * <li>{@link #A192CBC_HS384 A192CBC-HS384}
     * <li>{@link #A256CBC_HS512 A256CBC-HS512}
     * <li>{@link #A128GCM}
     * <li>{@link #A192GCM}
     * <li>{@link #A256GCM}
     * </ul>
     *
     * @param encAlgorithm the encryption algorithm
     * @param cekBitLength the content encryption key bit length
     * @return
     */
    public JWEBuilder<T, B> encryptionAlgorithm(String encAlgorithm, int cekBitLength) {
        header(ENC, encAlgorithm);
        header(CEK_BITLENGTH, cekBitLength);
        return this;
    }

    /**
     * Sets the key identifier used to determine the private key needed to decrypt the JWE.
     *
     * @param keyId the key id
     * @return
     */
    public JWEBuilder<T, B> keyIdentifier(String keyId) {
        header(KEY_ID, keyId);
        return this;
    }

    /**
     * Sets the compression algorithm. The zip (compression algorithm) applied to the Plaintext before encryption, if any. The
     * zip value defined by this specification is:
     *
     * <ul>
     * DEF - Compression with the DEFLATE [RFC1951] algorithm
     * </ul>
     *
     * @param zipAlgorithm the zip algorithm
     * @return
     */
    public JWEBuilder<T, B> compressionAlgorithm(String zipAlgorithm) {
        header(COMPRESSION_ALG, zipAlgorithm);
        return this;
    }

    /**
     * Sets the JWK Set.
     *
     * <p>
     * The JWK Set resource contains the public key to which the JWE was encrypted; this can be used to determine the private
     * key needed to decrypt the JWE.
     *
     * @param keySet the key set
     * @return
     */
    public JWEBuilder<T, B> keys(JWKSet keySet) {
        header(HEADER_JSON_WEB_KEY, keySet.getJsonObject().getJsonArray(HEADER_JSON_WEB_KEY));
        return this;
    }

    /**
     * Sets the JWK keys.
     *
     * <p>
     * The JWK Keys contains the public key to which the JWE was encrypted; this can be used to determine the private key needed
     * to decrypt the JWE.
     *
     * @param keys the keys
     * @return
     */
    public JWEBuilder<T, B> keys(JWK... keys) {
        JWKSet jwkSet = new JWKSet(keys);
        return keys(jwkSet);
    }

    /**
     * Updates the {@link org.picketlink.json.jose.JWE} JSON with the JWKSetURL.
     *
     * @param jwkSetURL the JWK Set URL
     * @return
     */
    public JWEBuilder<T, B> JWKSet(String jwkSetURL) {
        header(HEADER_JWK_SET_URL, jwkSetURL);
        return this;
    }

    /**
     * Sets the x509 URL.
     *
     * <p>
     * X.509 public key certificate or certificate chain [RFC5280] contains the public key to which the JWE was encrypted; this
     * can be used to determine the private key needed to decrypt the JWE.
     *
     * @param x509URL the x509 url
     * @return
     */
    public JWEBuilder<T, B> X509URL(String x509URL) {
        header(X509_URL, x509URL);
        return this;
    }

    /**
     * Sets the x509 certificate chain.
     *
     * <p>
     * The X.509 public key certificate or certificate chain [RFC5280] contains the public key to which the JWE was encrypted;
     * this can be used to determine the private key needed to decrypt the JWE.
     *
     * @param certificates the certificates
     * @return
     */
    public JWEBuilder<T, B> X509CertificateChain(String... certificates) {
        if (certificates.length == 1) {
            header(X509_CERTIFICATE_CHAIN, certificates[0]);
        } else if (certificates.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String operation : certificates) {
                arrayBuilder.add(operation);
            }

            this.headerBuilder.add(X509_CERTIFICATE_CHAIN, arrayBuilder);
        }
        return this;
    }

    /**
     * Sets the x509 SHA1 certificate thumbprint.
     *
     * <p>
     * The certificate referenced by the thumbprint contains the public key to which the JWE was encrypted; this can be used to
     * determine the private key needed to decrypt the JWE.
     *
     * @param sha1Thumbprint the sha1 thumbprint
     * @return
     */
    public JWEBuilder<T, B> X509CertificateSHA1Thumbprint(String sha1Thumbprint) {
        header(X509_CERTIFICATE_SHA1_THUMBPRINT, sha1Thumbprint);
        return this;
    }

    /**
     * Sets the x509 SHA256 certificate thumbprint.
     *
     * <p>
     * The certificate referenced by the thumbprint contains the public key to which the JWE was encrypted; this can be used to
     * determine the private key needed to decrypt the JWE.
     *
     * @param sha256Thumbprint the sha256 thumbprint
     * @return
     */
    public JWEBuilder<T, B> X509CertificateSHA256Thumbprint(String sha256Thumbprint) {
        header(X509_CERTIFICATE_SHA256_THUMBPRINT, sha256Thumbprint);
        return this;
    }

    /**
     * Updates {@link org.picketlink.json.jose.JWE} Header with the specified string header and its value(s).
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public JWEBuilder<T, B> header(String name, String... value) {
        setString(this.headerBuilder, name, value);
        return this;
    }

    /**
     * Updates {@link org.picketlink.json.jose.JWE} Header with the specified string header and its value(s).
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public JWEBuilder<T, B> header(String name, int... value) {
        setInt(this.headerBuilder, name, value);
        return this;
    }

    /**
     * Updates {@link org.picketlink.json.jose.JWE} Header with the specified string header and its value(s).
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public JWEBuilder<T, B> header(String name, List<JsonObject> value) {
        setJsonObject(this.headerBuilder, name, value);
        return this;
    }

    /**
     * Updates {@link org.picketlink.json.jose.JWE} Header with the specified string header and its value(s).
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public JWEBuilder<T, B> header(String name, JsonArray value) {
        setJsonObject(this.headerBuilder, name, value);
        return this;
    }

    /**
     * Builds {@link javax.json.JsonObjectBuilder}.
     *
     * @return
     */
    public T build() {
        return build(this.headerBuilder.build());
    }

    /**
     * Builds String JSON.
     *
     * @param json the json
     * @return
     */
    public T build(String json) {

        byte[] keyParameters = b64Decode(json);
        return build(Json.createReader(new ByteArrayInputStream(keyParameters)).readObject());
    }

    /**
     * Gets the header builder.
     *
     * @return the header builder
     */
    protected JsonObjectBuilder getHeaderBuilder() {
        return this.headerBuilder;
    }

    /**
     * Gets the token type.
     *
     * @return the token type
     */
    protected Class<T> getTokenType() {
        return this.tokenType;
    }

    /**
     * Builds JsonObject.
     *
     * @param headersObject the headers object
     * @return
     */
    protected T build(JsonObject headersObject) {
        try {
            Constructor<T> constructor = this.tokenType.getDeclaredConstructor(JsonObject.class);

            constructor.setAccessible(true);

            return constructor.newInstance(headersObject);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateToken(this.tokenType, e);
        }
    }

    /**
     * Updates the {@link javax.json.JsonObjectBuilder} with specified header parameter and its value(s).
     *
     * @param builderuilder
     * @param name the name
     * @param values the values
     * @return
     */
    private JWEBuilder<T, B> setString(JsonObjectBuilder builder, String name, String... values) {
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

    /**
     * Updates the {@link javax.json.JsonObjectBuilder} with specified header parameter and its value(s).
     *
     * @param builderuilder
     * @param name the name
     * @param values the values
     * @return
     */
    private JWEBuilder<T, B> setInt(JsonObjectBuilder builder, String name, int... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (int value : values) {
                arrayBuilder.add(value);
            }

            builder.add(name, arrayBuilder);
        }

        return this;
    }

    /**
     * POpulates the specified header parameter of {@link javax.json.JsonObjectBuilder} with its collection.
     *
     * @param builderuilder
     * @param name the name
     * @param values the values
     * @return
     */
    private JWEBuilder<T, B> setJsonObject(JsonObjectBuilder builder, String name, List<JsonObject> values) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Iterator<JsonObject> iterator = values.iterator();
        while (iterator.hasNext()) {
            arrayBuilder.add(iterator.next());
        }
        builder.add(name, arrayBuilder);
        return this;
    }

    /**
     * <p>
     * Updates the the specified header of {@link javax.json.JsonObjectBuilder} with the {@link javax.json.JsonArray}.
     * </p>
     *
     * @param builder the builder
     * @param name the name of the header or claim
     * @param values the values for the header or claim
     * @return
     */
    private JWEBuilder<T, B> setJsonObject(JsonObjectBuilder builder, String name, JsonArray values) {
        builder.add(name, values);
        return this;
    }

}
