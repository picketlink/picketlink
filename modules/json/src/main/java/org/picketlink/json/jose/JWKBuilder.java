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

import static org.picketlink.json.JsonConstants.COMMON.KEY_ID;
import static org.picketlink.json.JsonConstants.JWK.KEY_ALGORITHM;
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
import static org.picketlink.json.util.Base64Util.b64Decode;
import static org.picketlink.json.util.Base64Util.b64Encode;

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
 * <li>{@link #keyType(String) kty} (required)
 * <li>{@link #keyUse(String) use} (optional)
 * <li>{@link #keyOperations(String...) key_ops} (optional)
 * <li>{@link #keyAlgorithm(String) alg} (optional)
 * <li>{@link #keyIdentifier(String) kid} (required)
 * <li>{@link #X509Url(String) x5u} (optional)
 * <li>{@link #X509CertificateChain(String...) x5c} (optional)
 * <li>{@link #X509CertificateSHA1Thumbprint(String) x5t} (optional)
 * <li>{@link #X509CertificateSHA256Thumbprint(String) x5t#S256} (optional)
 * </ul>
 *
 * @param <T> the generic type
 * @param <B> the generic type
 *
 * @author Giriraj Sharma
 */

public class JWKBuilder<T extends JWK, B extends JWKBuilder<?, ?>> {

    private final JsonObjectBuilder keyParametersBuilder;

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
     * Gets token type.
     *
     * @return token type
     */
    protected Class<T> getTokenType() {
        return this.tokenType;
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
     * The kty (key type) member identifies the cryptographic algorithm family used with the key. kty values should either be
     * registered in the IANA JSON Web Key Types registry defined in [JWA] or be a value that contains a Collision-Resistant
     * Name. The kty value is a case-sensitive string. This member MUST be present in a JWK.
     *
     * @param type
     * @return
     */
    public JWKBuilder<T, B> keyType(String type) {
        keyParameter(KEY_TYPE, type);
        return this;
    }

    /**
     * The use (public key use) member identifies the intended use of the public key. The use parameter is intended for use
     * cases in which it is useful to distinguish between public signing keys and public encryption keys.
     *
     * Values defined by this specification are:
     * <ul>
     * <li>sig (signature)</li>
     * <li>enc (encryption)</li>
     * </ul>
     *
     * Other values MAY be used. Public Key Use values can be registered in the IANA JSON Web Key Use registry defined in
     * Section 8.2. The use value is a case-sensitive string. Use of the use member is OPTIONAL, unless the application requires
     * its presence.
     *
     * <p>
     *
     * @param use the key use
     * @return
     */
    public JWKBuilder<T, B> keyUse(String use) {
        keyParameter(KEY_USE, use);
        return this;
    }

    /**
     * The key_ops (key operations) member identifies the operation(s) that the key is intended to be used for. The key_ops
     * parameter is intended for use cases in which public, private, or symmetric keys may be present.
     *
     * Its value is an array of key operation values. Values defined by this specification are:
     *
     * <ul>
     * <li>sign (compute signature or MAC)</li>
     * <li>verify (verify signature or MAC)</li>
     * <li>encrypt (encrypt content)</li>
     * <li>decrypt (decrypt content and validate decryption, if applicable)</li>
     * <li>wrapKey (encrypt key)</li>
     * <li>unwrapKey (decrypt key and validate decryption, if applicable)</li>
     * <li>deriveKey (derive key)</li>
     * <li>deriveBits (derive bits not to be used as a key).</li>
     * </ul>
     *
     * <p>
     * Other values MAY be used. Key operation values can be registered in the IANA JSON Web Key Operations registry defined in
     * Section 8.3. The key operation values are case-sensitive strings. Duplicate key operation values MUST NOT be present in
     * the array.
     *
     * <p>
     * Multiple unrelated key operations SHOULD NOT be specified for a key because of the potential vulnerabilities associated
     * with using the same key with multiple algorithms. Thus, the combinations sign with verify, encrypt with decrypt, and
     * wrapKey with unwrapKey are permitted, but other combinations SHOULD NOT be used.The use and key_ops JWK members SHOULD
     * NOT be used together.
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
     * The alg (algorithm) member identifies the algorithm intended for use with the key. The values used should either be
     * registered in the IANA JSON Web Signature and Encryption Algorithms registry defined in [JWA] or be a value that contains
     * a Collision-Resistant Name. Use of this member is OPTIONAL.
     *
     * @param algorithm the algorithm
     * @return
     */
    public JWKBuilder<T, B> keyAlgorithm(String algorithm) {
        keyParameter(KEY_ALGORITHM, algorithm);
        return this;
    }

    /**
     * The kid (key ID) member can be used to match a specific key. This can be used, for instance, to choose among a set of
     * keys within a JWK Set during key rollover. The structure of the kid value is unspecified. When kid values are used within
     * a JWK Set, different keys within the JWK Set SHOULD use distinct kid values. (One example in which different keys might
     * use the same kid value is if they have different kty (key type) values but are considered to be equivalent alternatives
     * by the application using them.) The kid value is a case-sensitive string. Use of this member is OPTIONAL.
     *
     * <p>
     * When used with JWS or JWE, the kid value is used to match a JWS or JWE kid Header Parameter value.
     *
     * @param identifier the identifier
     * @return
     */
    public JWKBuilder<T, B> keyIdentifier(String identifier) {
        keyParameter(KEY_ID, identifier);
        return this;
    }

    /**
     * The x5u (X.509 URL) member is a URI [RFC3986] that refers to a resource for an X.509 public key certificate or
     * certificate chain [RFC5280]. The identified resource MUST provide a representation of the certificate or certificate
     * chain that conforms to RFC 5280 [RFC5280] in PEM encoded form [RFC1421]. The key in the first certificate MUST match the
     * public key represented by other members of the JWK. The protocol used to acquire the resource MUST provide integrity
     * protection; an HTTP GET request to retrieve the certificate MUST use TLS [RFC2818, RFC5246]; the identity of the server
     * MUST be validated, as per Section 6 of RFC 6125 [RFC6125]. Use of this member is OPTIONAL.
     *
     * @param url the url
     * @return
     */
    public JWKBuilder<T, B> X509Url(String url) {
        keyParameter(X509_URL, url);
        return this;
    }

    /**
     * The x5c (X.509 Certificate Chain) member contains a chain of one or more PKIX certificates [RFC5280]. The certificate
     * chain is represented as a JSON array of certificate value strings. Each string in the array is a base64 encoded
     * ([RFC4648] Section 4 -- not base64url encoded) DER [ITU.X690.1994] PKIX certificate value. The PKIX certificate
     * containing the key value MUST be the first certificate. This MAY be followed by additional certificates, with each
     * subsequent certificate being the one used to certify the previous one. The key in the first certificate MUST match the
     * public key represented by other members of the JWK. Use of this member is OPTIONAL.
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
     * The x5t (X.509 Certificate SHA-1 Thumbprint) member is a base64url encoded SHA-1 thumbprint (a.k.a. digest) of the DER
     * encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key represented by other
     * members of the JWK. Use of this member is OPTIONAL.
     *
     * @param sha1Thumbprint the SHA1 thumbprint
     * @return
     */
    public JWKBuilder<T, B> X509CertificateSHA1Thumbprint(String sha1Thumbprint) {
        keyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT, sha1Thumbprint);
        return this;
    }

    /**
     * The x5t#S256 (X.509 Certificate SHA-256 Thumbprint) member is a base64url encoded SHA-256 thumbprint (a.k.a. digest) of
     * the DER encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key represented by
     * other members of the JWK. Use of this member is OPTIONAL.
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
     * @param json the jwk encoded json string
     * @return
     */
    public T build(String json) {
        byte[] keyParameters = b64Decode(json);
        return build(Json.createReader(new ByteArrayInputStream(keyParameters)).readObject());
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
}