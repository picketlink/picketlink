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
import static org.picketlink.json.JsonConstants.JWE.CEK_BITLENGTH;
import static org.picketlink.json.JsonConstants.JWE.COMPRESSION_ALG;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_CHAIN;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA1_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA256_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_URL;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.Base64Util.b64Decode;

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
 * <li>{@link #type(String) alg}</li>
 * <li>{@link #contentType(String) typ}</li>
 * <li>{@link #algorithm(String) cty}</li>
 * <li>{@link #encryptionAlgorithm(String, int) enc}</li>
 * <li>{@link #compressionAlgorithm(String) zip}</li>
 * <li>{@link #keys(JWKSet) keys}</li>
 * <li>{@link #JWKSet(String) jku}</li>
 * <li>{@link #X509URL(String) x5u}</li>
 * <li>{@link #X509CertificateChain(String...) x5c}</li>
 * <li>{@link #X509CertificateSHA1Thumbprint(String) x5t}</li>
 * <li>{@link #X509CertificateSHA256Thumbprint(String) x5t#S256}</li>
 * </ul>
 *
 * <p>
 * Example header:
 *
 * <pre>
 * {
 *   "alg":"RSA1_5",
 *   "kid":"2011-04-29",
 *   "enc":"A128CBC-HS256",
 *   "jku":"https://server.example.com/keys.jwks"
 * }
 * </pre>
 *
 * @param <T> the generic type
 * @param <B> the generic type
 * @author Giriraj Sharma
 */

public class JWEBuilder<T extends JWE, B extends JWEBuilder<?, ?>> {

    private final JsonObjectBuilder headerBuilder;

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
     * Gets the token type.
     *
     * @return the token type
     */
    protected Class<T> getTokenType() {
        return this.tokenType;
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
     * Sets the type of JSON Web Encryption
     * <p>
     * The typ (type) Header Parameter is used by JWS or JWE to declare the MIME Media Type [IANA.MediaTypes] of this complete
     * JWS or JWE object. This is intended for use by the application when more than one kind of object could be present in an
     * application data structure that can contain a JWS or JWE object; the application can use this value to disambiguate among
     * the different kinds of objects that might be present. Use of this Header Parameter is OPTIONAL.
     *
     * @param type the String type
     * @return
     */
    public JWEBuilder<T, B> type(String type) {
        header(HEADER_TYPE, type);
        return this;
    }

    /**
     * Sets the content type of JSON Web Encryption
     *
     * <p>
     * The cty (content type) Header Parameter is used by JWS or JWE applications to declare the MIME Media Type
     * [IANA.MediaTypes] of the secured content (the payload) or encrypted plaintext. This is intended for use by the
     * application when more than one kind of object could be present in the JWS payload or JWE encrypted plaintext; the
     * application can use this value to disambiguate among the different kinds of objects that might be present. Use of this
     * Header Parameter is OPTIONAL.
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
     * <p>
     * The alg (algorithm) Header Parameter identifies the cryptographic algorithm used to secure the JWS or JWE. The signature,
     * MAC, or plaintext value is not valid if the alg value does not represent a supported algorithm, or if there is not a key
     * for use with that algorithm associated with the party that digitally signed or MACed the content. alg values should
     * either be registered in the IANA JSON Web Signature and Encryption Algorithms registry defined in [JWA] or be a value
     * that contains a Collision-Resistant Name. The alg value is a case-sensitive string containing a StringOrURI value.
     *
     * <ul>
     * <li>RSA1_5
     * <li>RSA-OAEP
     * <li>RSA-OAEP-256
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
     * Sets the encryption algorithm used to encrypt the Plaintext to produce the Ciphertext.
     *
     * <p>
     * The enc (encryption algorithm) Header Parameter identifies the content encryption algorithm used to encrypt the Plaintext
     * to produce the Ciphertext. This algorithm MUST be an AEAD algorithm with a specified key length. The recipient MUST
     * reject the JWE if the enc value does not represent a supported algorithm. enc values should either be registered in the
     * IANA JSON Web Signature and Encryption Algorithms registry defined in [JWA] or be a value that contains a
     * Collision-Resistant Name. The enc value is a case-sensitive string containing a StringOrURI value.
     *
     * <ul>
     * <li>ENC_A128CBC_HS256
     * <li>ENC_A192CBC_HS384
     * <li>ENC_A256CBC_HS512
     * <li>ENC_A128GCM
     * <li>ENC_A192GCM
     * <li>ENC_A256GCM
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
     * <p>
     * The kid (key ID) member can be used to match a specific key. This can be used, for instance, to choose among a set of
     * keys within a JWK Set during key rollover. The structure of the kid value is unspecified. When kid values are used within
     * a JWK Set, different keys within the JWK Set SHOULD use distinct kid values. (One example in which different keys might
     * use the same kid value is if they have different kty (key type) values but are considered to be equivalent alternatives
     * by the application using them.) The kid value is a case-sensitive string. Use of this member is OPTIONAL.
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
     * <li>DEF - Compression with the DEFLATE [RFC1951] algorithm</li>
     * </ul>
     *
     * <p>
     * Other values MAY be used. Compression algorithm values can be registered in the IANA JSON Web Encryption Compression
     * Algorithm registry defined in [JWA]. The zip value is a case-sensitive string. If no zip parameter is present, no
     * compression is applied to the Plaintext before encryption.
     *
     * @param zipAlgorithm the zip algorithm
     * @return
     */
    public JWEBuilder<T, B> compressionAlgorithm(String zipAlgorithm) {
        header(COMPRESSION_ALG, zipAlgorithm);
        return this;
    }

    /**
     * Sets the JSON Web Key Set.
     *
     * <p>
     * The JWK (JSON Web Key) Header Parameter is the public key that corresponds to the key used to digitally sign the JWS.
     * This key is represented as a JSON Web Key [JWK]. Use of this Header Parameter is OPTIONAL.
     *
     * @param keySet the key set
     * @return
     */
    public JWEBuilder<T, B> keys(JWKSet keySet) {
        header(HEADER_JSON_WEB_KEY, keySet.getJsonObject().getJsonArray(HEADER_JSON_WEB_KEY));
        return this;
    }

    /**
     * Returns the JWK Set consisting of JWK Keys.
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
     * <p>
     * The jku (JWK Set URL) Header Parameter is a URI [RFC3986] that refers to a resource for a set of JSON-encoded public
     * keys, one of which corresponds to the key used to digitally sign the JWS or encrypt plaintext using JWE. The keys MUST be
     * encoded as a JSON Web Key Set (JWK Set) [JWK]. The protocol used to acquire the resource MUST provide integrity
     * protection; an HTTP GET request to retrieve the JWK Set MUST use TLS [RFC2818, RFC5246]; the identity of the server MUST
     * be validated, as per Section 6 of RFC 6125 [RFC6125]. Use of this Header Parameter is OPTIONAL.
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
     * The x5u (X.509 URL) member is a URI [RFC3986] that refers to a resource for an X.509 public key certificate or
     * certificate chain [RFC5280]. The identified resource MUST provide a representation of the certificate or certificate
     * chain that conforms to RFC 5280 [RFC5280] in PEM encoded form [RFC1421]. The key in the first certificate MUST match the
     * public key represented by other members of the JWK. The protocol used to acquire the resource MUST provide integrity
     * protection; an HTTP GET request to retrieve the certificate MUST use TLS [RFC2818, RFC5246]; the identity of the server
     * MUST be validated, as per Section 6 of RFC 6125 [RFC6125]. Use of this member is OPTIONAL.
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
     * The x5c (X.509 Certificate Chain) member contains a chain of one or more PKIX certificates [RFC5280]. The certificate
     * chain is represented as a JSON array of certificate value strings. Each string in the array is a base64 encoded
     * ([RFC4648] Section 4 -- not base64url encoded) DER [ITU.X690.1994] PKIX certificate value. The PKIX certificate
     * containing the key value MUST be the first certificate. This MAY be followed by additional certificates, with each
     * subsequent certificate being the one used to certify the previous one. The key in the first certificate MUST match the
     * public key represented by other members of the JWK. Use of this member is OPTIONAL.
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
     * The x5t (X.509 Certificate SHA-1 Thumbprint) member is a base64url encoded SHA-1 thumbprint (a.k.a. digest) of the DER
     * encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key represented by other
     * members of the JWK. Use of this member is OPTIONAL.
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
     * The x5t#S256 (X.509 Certificate SHA-256 Thumbprint) member is a base64url encoded SHA-256 thumbprint (a.k.a. digest) of
     * the DER encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key represented by
     * other members of the JWK. Use of this member is OPTIONAL.
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
     * Builds {@link javax.json.JsonObject}.
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

}