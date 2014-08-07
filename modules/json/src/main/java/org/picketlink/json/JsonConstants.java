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
package org.picketlink.json;

/**
 * Define JSON Constants associated with JSON Object Signing and Encryption(JOSE) implementations.
 *
 * @author Anil Saldhana
 * @author Giriraj Sharma
 * @since March 07, 2014
 */
public interface JsonConstants {

    /**
     * JOSE implementations can represent RSA [RFC3447] keys. In this case, the kty member value MUST be RSA.
     */
    String RSA = "RSA";

    /**
     * The Interface COMMON represents common JSON Constants used with JSON Web Signature(JWS) and JSON Web Encryption(JWE).
     */
    interface COMMON {

        /**
         * The alg (algorithm) Header Parameter identifies the cryptographic algorithm used to secure the JWS or JWE. The
         * signature, MAC, or plaintext value is not valid if the alg value does not represent a supported algorithm, or if
         * there is not a key for use with that algorithm associated with the party that digitally signed or MACed the content.
         * alg values should either be registered in the IANA JSON Web Signature and Encryption Algorithms registry defined in
         * [JWA] or be a value that contains a Collision-Resistant Name. The alg value is a case-sensitive string containing a
         * StringOrURI value.
         */
        String ALG = "alg";

        /**
         * The enc (encryption algorithm) Header Parameter identifies the content encryption algorithm used to encrypt the
         * Plaintext to produce the Ciphertext. This algorithm MUST be an AEAD algorithm with a specified key length. The
         * recipient MUST reject the JWE if the enc value does not represent a supported algorithm. enc values should either be
         * registered in the IANA JSON Web Signature and Encryption Algorithms registry defined in [JWA] or be a value that
         * contains a Collision-Resistant Name. The enc value is a case-sensitive string containing a StringOrURI value.
         */
        String ENC = "enc";

        /**
         * The kid (key ID) member can be used to match a specific key. This can be used, for instance, to choose among a set of
         * keys within a JWK Set during key rollover. The structure of the kid value is unspecified. When kid values are used
         * within a JWK Set, different keys within the JWK Set SHOULD use distinct kid values. (One example in which different
         * keys might use the same kid value is if they have different kty (key type) values but are considered to be equivalent
         * alternatives by the application using them.) The kid value is a case-sensitive string. Use of this member is
         * OPTIONAL.
         *
         * <p>
         * When used with JWS or JWE, the kid value is used to match a JWS or JWE kid Header Parameter value.
         */
        String KEY_ID = "kid";

        /**
         * The typ (type) Header Parameter is used by JWS or JWE to declare the MIME Media Type [IANA.MediaTypes] of this
         * complete JWS or JWE object. This is intended for use by the application when more than one kind of object could be present
         * in an application data structure that can contain a JWS or JWE object; the application can use this value to
         * disambiguate among the different kinds of objects that might be present. Use of this Header Parameter is OPTIONAL.
         */
        String HEADER_TYPE = "typ";

        /**
         * The cty (content type) Header Parameter is used by JWS or JWE applications to declare the MIME Media Type
         * [IANA.MediaTypes] of the secured content (the payload) or encrypted plaintext. This is intended for use by the
         * application when more than one kind of object could be present in the JWS payload or JWE encrypted plaintext; the
         * application can use this value to disambiguate among the different kinds of objects that might be present. Use of
         * this Header Parameter is OPTIONAL.
         */
        String HEADER_CONTENT_TYPE = "cty";

        /**
         * The jku (JWK Set URL) Header Parameter is a URI [RFC3986] that refers to a resource for a set of JSON-encoded public
         * keys, one of which corresponds to the key used to digitally sign the JWS or encrypt plaintext using JWE. The keys
         * MUST be encoded as a JSON Web Key Set (JWK Set) [JWK]. The protocol used to acquire the resource MUST provide
         * integrity protection; an HTTP GET request to retrieve the JWK Set MUST use TLS [RFC2818, RFC5246]; the identity of
         * the server MUST be validated, as per Section 6 of RFC 6125 [RFC6125]. Use of this Header Parameter is OPTIONAL.
         */
        String HEADER_JWK_SET_URL = "jku";

        /**
         * The jwk (JSON Web Key) Header Parameter is the public key that corresponds to the key used to digitally sign the JWS.
         * This key is represented as a JSON Web Key [JWK]. Use of this Header Parameter is OPTIONAL.
         */
        String HEADER_JSON_WEB_KEY = "keys";

        /** The period for serialization of JWS or JWE. */
        String PERIOD = ".";
    }

    /**
     * JSON Web Token (JWT) is a compact URL-safe means of representing claims to be transferred between two parties. The claims
     * in a JWT are encoded as a JavaScript Object Notation (JSON) object that is used as the payload of a JSON Web Signature
     * (JWS) structure or as the plaintext of a JSON Web Encryption (JWE) structure, enabling the claims to be digitally signed
     * or MACed and/or encrypted.
     *
     * @see http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html
     */
    interface JWT {

        /**
         * The iss (issuer) claim identifies the principal that issued the JWT. The processing of this claim is generally
         * application specific. The iss value is a case-sensitive string containing a StringOrURI value. Use of this claim is
         * OPTIONAL.
         */
        String CLAIM_ISSUER = "iss";

        /**
         * The sub (subject) claim identifies the principal that is the subject of the JWT. The Claims in a JWT are normally
         * statements about the subject. The subject value MAY be scoped to be locally unique in the context of the issuer or
         * MAY be globally unique. The processing of this claim is generally application specific. The sub value is a
         * case-sensitive string containing a StringOrURI value. Use of this claim is OPTIONAL.
         */
        String CLAIM_SUBJECT = "sub";

        /**
         * The aud (audience) claim identifies the recipients that the JWT is intended for. Each principal intended to process
         * the JWT MUST identify itself with a value in the audience claim. If the principal processing the claim does not
         * identify itself with a value in the aud claim when this claim is present, then the JWT MUST be rejected. In the
         * general case, the aud value is an array of case-sensitive strings, each containing a StringOrURI value. In the
         * special case when the JWT has one audience, the aud value MAY be a single case-sensitive string containing a
         * StringOrURI value. The interpretation of audience values is generally application specific. Use of this claim is
         * OPTIONAL.
         */
        String CLAIM_AUDIENCE = "aud";

        /**
         * The exp (expiration time) claim identifies the expiration time on or after which the JWT MUST NOT be accepted for
         * processing. The processing of the exp claim requires that the current date/time MUST be before the expiration
         * date/time listed in the exp claim. Implementers MAY provide for some small leeway, usually no more than a few
         * minutes, to account for clock skew. Its value MUST be a number containing an IntDate value. Use of this claim is
         * OPTIONAL.
         */
        String CLAIM_EXPIRATION = "exp";

        /**
         * The nbf (not before) claim identifies the time before which the JWT MUST NOT be accepted for processing. The
         * processing of the nbf claim requires that the current date/time MUST be after or equal to the not-before date/time
         * listed in the nbf claim. Implementers MAY provide for some small leeway, usually no more than a few minutes, to
         * account for clock skew. Its value MUST be a number containing an IntDate value. Use of this claim is OPTIONAL.
         */
        String CLAIM_NOT_BEFORE = "nbf";

        /**
         * The iat (issued at) claim identifies the time at which the JWT was issued. This claim can be used to determine the
         * age of the JWT. Its value MUST be a number containing an IntDate value. Use of this claim is OPTIONAL.
         */
        String CLAIM_ISSUED_AT = "iat";

        /**
         * The jti (JWT ID) claim provides a unique identifier for the JWT. The identifier value MUST be assigned in a manner
         * that ensures that there is a negligible probability that the same value will be accidentally assigned to a different
         * data object. The jti claim can be used to prevent the JWT from being replayed. The jti value is a case-sensitive
         * string. Use of this claim is OPTIONAL.
         */
        String CLAIM_ID = "jti";
    }

    /**
     * A JSON Web Key (JWK) is a JavaScript Object Notation (JSON) data structure that represents a cryptographic key. This
     * specification also defines a JSON Web Key Set (JWK Set) JSON data structure that represents a set of JWKs. Cryptographic
     * algorithms and identifiers for use with this specification are described in the separate JSON Web Algorithms (JWA)
     * specification and IANA registries defined by that specification.
     *
     * @see http://self-issued.info/docs/draft-ietf-jose-json-web-key.html
     */
    interface JWK {

        /**
         * The kty (key type) member identifies the cryptographic algorithm family used with the key. kty values should either
         * be registered in the IANA JSON Web Key Types registry defined in [JWA] or be a value that contains a
         * Collision-Resistant Name. The kty value is a case-sensitive string. This member MUST be present in a JWK.
         */
        String KEY_TYPE = "kty";

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
         * Section 8.2. The use value is a case-sensitive string. Use of the use member is OPTIONAL, unless the application
         * requires its presence.
         */
        String KEY_USE = "use";

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
         * Other values MAY be used. Key operation values can be registered in the IANA JSON Web Key Operations registry defined
         * in Section 8.3. The key operation values are case-sensitive strings. Duplicate key operation values MUST NOT be
         * present in the array.
         *
         * <p>
         * Multiple unrelated key operations SHOULD NOT be specified for a key because of the potential vulnerabilities
         * associated with using the same key with multiple algorithms. Thus, the combinations sign with verify, encrypt with
         * decrypt, and wrapKey with unwrapKey are permitted, but other combinations SHOULD NOT be used.The use and key_ops JWK
         * members SHOULD NOT be used together.
         */
        String KEY_OPERATIONS = "key_ops";

        /**
         * The alg (algorithm) member identifies the algorithm intended for use with the key. The values used should either be
         * registered in the IANA JSON Web Signature and Encryption Algorithms registry defined in [JWA] or be a value that
         * contains a Collision-Resistant Name. Use of this member is OPTIONAL.
         */
        String KEY_ALGORITHM = "alg";

        /**
         * The x5u (X.509 URL) member is a URI [RFC3986] that refers to a resource for an X.509 public key certificate or
         * certificate chain [RFC5280]. The identified resource MUST provide a representation of the certificate or certificate
         * chain that conforms to RFC 5280 [RFC5280] in PEM encoded form [RFC1421]. The key in the first certificate MUST match
         * the public key represented by other members of the JWK. The protocol used to acquire the resource MUST provide
         * integrity protection; an HTTP GET request to retrieve the certificate MUST use TLS [RFC2818, RFC5246]; the identity
         * of the server MUST be validated, as per Section 6 of RFC 6125 [RFC6125]. Use of this member is OPTIONAL.
         */
        String X509_URL = "x5u";

        /**
         * The x5c (X.509 Certificate Chain) member contains a chain of one or more PKIX certificates [RFC5280]. The certificate
         * chain is represented as a JSON array of certificate value strings. Each string in the array is a base64 encoded
         * ([RFC4648] Section 4 -- not base64url encoded) DER [ITU.X690.1994] PKIX certificate value. The PKIX certificate
         * containing the key value MUST be the first certificate. This MAY be followed by additional certificates, with each
         * subsequent certificate being the one used to certify the previous one. The key in the first certificate MUST match
         * the public key represented by other members of the JWK. Use of this member is OPTIONAL.
         */
        String X509_CERTIFICATE_CHAIN = "x5c";

        /**
         * The x5t (X.509 Certificate SHA-1 Thumbprint) member is a base64url encoded SHA-1 thumbprint (a.k.a. digest) of the
         * DER encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key represented by
         * other members of the JWK. Use of this member is OPTIONAL.
         */
        String X509_CERTIFICATE_SHA1_THUMBPRINT = "x5t";

        /**
         * The x5t#S256 (X.509 Certificate SHA-256 Thumbprint) member is a base64url encoded SHA-256 thumbprint (a.k.a. digest)
         * of the DER encoding of an X.509 certificate [RFC5280]. The key in the certificate MUST match the public key
         * represented by other members of the JWK. Use of this member is OPTIONAL.
         */
        String X509_CERTIFICATE_SHA256_THUMBPRINT = "x5t#S256";
    }

    /**
     * A RSA JSON Web Key (JWK) is a JavaScript Object Notation (JSON) data structure that represents a cryptographic RSA
     * key(Public or Private) or key parameters exclusively used for RSA Keys.
     */
    interface JWK_RSA {

        /** The modulus value for the RSA key. */
        String MODULUS = "n";

        /** The public exponent of the RSA key. */
        String PUBLIC_EXPONENT = "e";

        /** The private exponent of the RSA key. */
        String PRIVATE_EXPONENT = "d";

        /** The first prime factor of the private RSA key. */
        String PRIME_P = "p";

        /** The second prime factor of the private RSA key. */
        String PRIME_Q = "q";

        /**
         * The first factor Chinese Remainder Theorem exponent of the private RSA key.
         */
        String PRIME_EXPONENT_P = "dp";

        /**
         * The second factor Chinese Remainder Theorem exponent of the private RSA key.
         */
        String PRIME_EXPONENT_Q = "dq";

        /**
         * The first Chinese Remainder Theorem coefficient of the private RSA key.
         */
        String CRT_COEFFICIENT = "qi";
    }

    /**
     * JSON Web Signature (JWS) represents content secured with digital signatures or Message Authentication Codes (MACs) using
     * JavaScript Object Notation (JSON) based data structures. Cryptographic algorithms and identifiers for use with this
     * specification are described in the separate JSON Web Algorithms (JWA) specification and an IANA registry defined by that
     * specification. Related encryption capabilities are described in the separate JSON Web Encryption (JWE) specification.
     *
     * @see http://self-issued.info/docs/draft-ietf-jose-json-web-signature.html
     */
    interface JWS {

        /** JSON Web Signature Algorithm : HMAC using SHA-256 hash algorithm (required). */
        String SIGN_ALG_HS256 = "HS256";

        /** JSON Web Signature Algorithm : HMAC using SHA-384 hash algorithm (optional). */
        String SIGN_ALG_HS384 = "HS384";

        /** JSON Web Signature Algorithm : HMAC using SHA-512 hash algorithm (optional). */
        String SIGN_ALG_HS512 = "HS512";

        /** JSON Web Signature Algorithm : ECDSA using P-256 curve and SHA-256 hash algorithm (recommended). */
        String SIGN_ALG_ES256 = "ES256";

        /** JSON Web Signature Algorithm : ECDSA using P-384 curve and SHA-384 hash algorithm (optional). */
        String SIGN_ALG_ES383 = "ES384";

        /** JSON Web Signature Algorithm : ECDSA using P-521 curve and SHA-512 hash algorithm (optional). */
        String SIGN_ALG_ES512 = "ES512";

        /** JSON Web Signature Algorithm : RSASSA-PKCS-v1_5 using SHA-256 hash algorithm (recommended). */
        String SIGN_ALG_RS256 = "RS256";

        /** JSON Web Signature Algorithm : RSASSA-PKCS-v1_5 using SHA-384 hash algorithm (optional). */
        String SIGN_ALG_RS383 = "RS384";

        /** JSON Web Signature Algorithm : RSASSA-PKCS-v1_5 using SHA-512 hash algorithm (optional). */
        String SIGN_ALG_RS512 = "RS512";
    }

    /**
     * JSON Web Encryption (JWE) represents encrypted content using JavaScript Object Notation (JSON) based data structures.
     * Cryptographic algorithms and identifiers for use with this specification are described in the separate JSON Web
     * Algorithms (JWA) specification and IANA registries defined by that specification. Related digital signature and MAC
     * capabilities are described in the separate JSON Web Signature (JWS) specification.
     *
     * @see http://self-issued.info/docs/draft-ietf-jose-json-web-encryption.html
     */
    interface JWE {

        /**
         * The crit (critical) Header Parameter value is an array listing the Header Parameter names present in the JOSE Header
         * that use those extensions. If any of the listed extension Header Parameters are not understood and supported by the
         * receiver, it MUST reject the JWS or JWE. Senders MUST NOT include Header Parameter names defined by the initial RFC
         * versions and MUST NOT use the empty list [] as the crit value. Recipients MAY reject the JWS if the critical list
         * contains any Header Parameter names defined by the initial RFC versions or any other constraints on its use are
         * violated.
         */
        String HEADER_CRITICAL_PARAMETER = "crit";

        /** The Content Encryption Key (CEK) bit length. */
        String CEK_BITLENGTH = "cek_bitlength";

        /**
         * The zip (compression algorithm) applied to the Plaintext before encryption, if any. The zip value defined by this
         * specification is:
         *
         * <ul>
         * <li>DEF - Compression with the DEFLATE [RFC1951] algorithm</li>
         * </ul>
         *
         * <p>
         * Other values MAY be used. Compression algorithm values can be registered in the IANA JSON Web Encryption Compression
         * Algorithm registry defined in [JWA]. The zip value is a case-sensitive string. If no zip parameter is present, no
         * compression is applied to the Plaintext before encryption.
         */
        String COMPRESSION_ALG = "zip";

        /**
         * JSON Web Encryption Method : AES_128_CBC_HMAC_SHA_256 authenticated encryption using a 256 bit key (required).
         */
        String ENC_A128CBC_HS256 = "A128CBC-HS256";

        /**
         * JSON Web Encryption Method : AES_192_CBC_HMAC_SHA_384 authenticated encryption using a 384 bit key (optional).
         */
        String ENC_A192CBC_HS384 = "A192CBC-HS384";

        /**
         * JSON Web Encryption Method : AES_256_CBC_HMAC_SHA_512 authenticated encryption using a 512 bit key (required).
         */
        String ENC_A256CBC_HS512 = "A256CBC-HS512";

        /**
         * JSON Web Encryption Method : AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 128 bit key (recommended).
         */
        String ENC_A128GCM = "A128GCM";

        /**
         * JSON Web Encryption Method : AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 192 bit key (optional).
         */
        String ENC_A192GCM = "A192GCM";

        /**
         * JSON Web Encryption Method : AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 256 bit key (recommended).
         */
        String ENC_A256GCM = "A256GCM";

        /**
         * JWE Algorithm Method : RSAES-PKCS1-V1_5 (RFC 3447) (required). A key of size 2048 bits or larger MUST be used with
         * this algorithm.
         */
        String ALG_RSA1_5 = "RSA1_5";

        /**
         * JWE Algorithm Method : RSAES using Optimal Asymmetric Encryption Padding (OAEP) (RFC 3447), with the default
         * parameters specified by RFC 3447 in section A.2.1 (recommended). A key of size 2048 bits or larger MUST be used with
         * this algorithm.
         */
        String ALG_RSA_OAEP = "RSA-OAEP";

        /**
         * JWE Algorithm Method : RSAES using Optimal Asymmetric Encryption Padding (OAEP) (RFC 3447), with the SHA-256 hash
         * function and the MGF1 with SHA-256 mask generation function (recommended). A key of size 2048 bits or larger MUST be
         * used with this algorithm.
         */
        String ALG_RSA_OAEP_256 = "RSA-OAEP-256";

    }
}