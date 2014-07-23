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
 * The Class JWEBuilder.
 *
 * @author Giriraj Sharma
 * @param <T> the generic type
 * @param <B> the generic type
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
     * Instantiates a new JWE builder.
     *
     * @param tokenType the token type
     */
    protected JWEBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.headerBuilder = Json.createObjectBuilder();
    }

    /**
     * Type.
     *
     * @param type the type
     * @return
     */
    public B type(String type) {
        header(HEADER_TYPE, type);
        return (B) this;
    }

    /**
     * Content type.
     *
     * @param contentType the content type
     * @return
     */
    public B contentType(String contentType) {
        header(HEADER_CONTENT_TYPE, contentType);
        return (B) this;
    }

    /**
     * Algorithm.
     *
     * @param algorithm the algorithm
     * @return
     */
    public B algorithm(String algorithm) {
        header(ALG, algorithm);
        return (B) this;
    }

    /**
     * Encryption algorithm.
     *
     * @param encAlgorithm the enc algorithm
     * @param cekBitLength the cek bit length
     * @return
     */
    public B encryptionAlgorithm(String encAlgorithm, int cekBitLength) {
        header(ENC, encAlgorithm);
        header(CEK_BITLENGTH, cekBitLength);
        return (B) this;
    }

    /**
     * Key identifier.
     *
     * @param keyId the key id
     * @return
     */
    public B keyIdentifier(String keyId) {
        header(KEY_ID, keyId);
        return (B) this;
    }

    /**
     * Compression algorithm.
     *
     * @param zipAlgorithm the zip algorithm
     * @return
     */
    public B compressionAlgorithm(String zipAlgorithm) {
        header(COMPRESSION_ALG, zipAlgorithm);
        return (B) this;
    }

    /**
     * Keys.
     *
     * @param keySet the key set
     * @return
     */
    public B keys(JWKSet keySet) {
        header(HEADER_JSON_WEB_KEY, keySet.getJsonObject().getJsonArray(HEADER_JSON_WEB_KEY));
        return (B) this;
    }

    /**
     * Keys.
     *
     * @param keys the keys
     * @return
     */
    public B keys(JWK... keys) {
        JWKSet jwkSet = new JWKSet(keys);
        return keys(jwkSet);
    }

    /**
     * JWK set.
     *
     * @param jwkSetURL the JWK Set URL
     * @return
     */
    public B JWKSet(String jwkSetURL) {
        header(HEADER_JWK_SET_URL, jwkSetURL);
        return (B) this;
    }

    /**
     * X509 url.
     *
     * @param x509URL the x509 url
     * @return
     */
    public B X509URL(String x509URL) {
        header(X509_URL, x509URL);
        return (B) this;
    }

    /**
     * X509 certificate chain.
     *
     * @param certificates the certificates
     * @return
     */
    public B X509CertificateChain(String... certificates) {
        if (certificates.length == 1) {
            header(X509_CERTIFICATE_CHAIN, certificates[0]);
        } else if (certificates.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String operation : certificates) {
                arrayBuilder.add(operation);
            }

            this.headerBuilder.add(X509_CERTIFICATE_CHAIN, arrayBuilder);
        }
        return (B) this;
    }

    /**
     * X509 certificate sha1 thumbprint.
     *
     * @param sha1Thumbprint the sha1 thumbprint
     * @return
     */
    public B X509CertificateSHA1Thumbprint(String sha1Thumbprint) {
        header(X509_CERTIFICATE_SHA1_THUMBPRINT, sha1Thumbprint);
        return (B) this;
    }

    /**
     * X509 certificate sha256 thumbprint.
     *
     * @param sha256Thumbprint the sha256 thumbprint
     * @return
     */
    public B X509CertificateSHA256Thumbprint(String sha256Thumbprint) {
        header(X509_CERTIFICATE_SHA256_THUMBPRINT, sha256Thumbprint);
        return (B) this;
    }

    /**
     * Header.
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public B header(String name, String... value) {
        setString(this.headerBuilder, name, value);
        return (B) this;
    }

    /**
     * Header.
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public B header(String name, int... value) {
        setInt(this.headerBuilder, name, value);
        return (B) this;
    }

    /**
     * Header.
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public B header(String name, List<JsonObject> value) {
        setJsonObject(this.headerBuilder, name, value);
        return (B) this;
    }

    /**
     * Header.
     *
     * @param name the name
     * @param value the value
     * @return
     */
    public B header(String name, JsonArray value) {
        setJsonObject(this.headerBuilder, name, value);
        return (B) this;
    }

    /**
     * Builds.
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
     * Sets the string.
     *
     * @param builder the builder
     * @param name the name
     * @param values the values
     * @return
     */
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

    /**
     * Sets the int.
     *
     * @param builder the builder
     * @param name the name
     * @param values the values
     * @return
     */
    private B setInt(JsonObjectBuilder builder, String name, int... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (int value : values) {
                arrayBuilder.add(value);
            }

            builder.add(name, arrayBuilder);
        }

        return (B) this;
    }

    /**
     * Sets the json object.
     *
     * @param builder the builder
     * @param name the name
     * @param values the values
     * @return
     */
    private B setJsonObject(JsonObjectBuilder builder, String name, List<JsonObject> values) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Iterator<JsonObject> iterator = values.iterator();
        while (iterator.hasNext()) {
            arrayBuilder.add(iterator.next());
        }
        builder.add(name, arrayBuilder);
        return (B) this;
    }

    /**
     * Sets the json object.
     *
     * @param builder the builder
     * @param name the name
     * @param values the values
     * @return
     */
    private B setJsonObject(JsonObjectBuilder builder, String name, JsonArray values) {
        builder.add(name, values);
        return (B) this;
    }

}
