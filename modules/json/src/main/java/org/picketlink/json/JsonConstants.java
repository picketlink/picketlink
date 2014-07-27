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
 * Define JSON Constants.
 *
 * @author Anil Saldhana
 * @author Giriraj Sharma
 * @since March 07, 2014
 */
public interface JsonConstants {

    /** The rsa key. */
    String RSA = "RSA";

    /**
     * The Interface COMMON.
     */
    interface COMMON {

        /** The algorithm. */
        String ALG = "alg";

        /** The encryption algorithm. */
        String ENC = "enc";

        /** The signature algorithm. */
        String SIG = "sig";

        /** The key id, optional. */
        String KEY_ID = "kid";

        /** The header type. */
        String HEADER_TYPE = "typ";

        /** The header content type. */
        String HEADER_CONTENT_TYPE = "cty";

        /** The header JWK Set URL. */
        String HEADER_JWK_SET_URL = "jku";

        /** The header JSON Web Key. */
        String HEADER_JSON_WEB_KEY = "keys";

        /** The RSA_SHA_256 algorithm. */
        String RSA_SHA_256 = "RS256";

        /** The HMAC_SHA_256 algorithm. */
        String HMAC_SHA_256 = "HS256";

        /** The period for serialization. */
        String PERIOD = ".";
    }

    /**
     * The Interface JWT.
     */
    interface JWT {

        /** The claim issuer. */
        String CLAIM_ISSUER = "iss";

        /** The claim subject. */
        String CLAIM_SUBJECT = "sub";

        /** The claim audience. */
        String CLAIM_AUDIENCE = "aud";

        /** The claim expiration. */
        String CLAIM_EXPIRATION = "exp";

        /** The claim not before. */
        String CLAIM_NOT_BEFORE = "nbf";

        /** The claim issued at. */
        String CLAIM_ISSUED_AT = "iat";

        /** The claim id. */
        String CLAIM_ID = "jti";
    }

    /**
     * The Interface JSON Web Key.
     */
    interface JWK {

        /** The key type, required. */
        String KEY_TYPE = "kty";

        /** The key use, optional. */
        String KEY_USE = "use";

        /** The key operations, optional. */
        String KEY_OPERATIONS = "key_ops";

        /** The intended JOSE algorithm for the key, optional. */
        String KEY_ALGORITHM = "alg";

        /** The key identifier, optional. */
        String KEY_IDENTIFIER = "kid";

        /** The X509 certificate URL, optional. */
        String X509_URL = "x5u";

        /** The X509 certificate chain. optional. */
        String X509_CERTIFICATE_CHAIN = "x5c";

        /** The X509 certificate SHA1 Thumbprint, optional. */
        String X509_CERTIFICATE_SHA1_THUMBPRINT = "x5t";

        /** The X509 certificate SHA256 Thumbprint, optional. */
        String X509_CERTIFICATE_SHA256_THUMBPRINT = "x5t#S256";
    }

    /**
     * The Interface RSA JSON Web Key.
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
     * The Interface JSON Web Signature.
     */
    interface JWS {

        /** HMAC using SHA-256 hash algorithm (required). */
        String SIGN_ALG_HS256 = "HS256";

        /** HMAC using SHA-384 hash algorithm (optional). */
        String SIGN_ALG_HS384 = "HS384";

        /** HMAC using SHA-512 hash algorithm (optional). */
        String SIGN_ALG_HS512 = "HS512";

        /** ECDSA using P-256 curve and SHA-256 hash algorithm (recommended). */
        String SIGN_ALG_ES256 = "ES256";

        /** ECDSA using P-384 curve and SHA-384 hash algorithm (optional). */
        String SIGN_ALG_ES383 = "ES384";

        /** ECDSA using P-521 curve and SHA-512 hash algorithm (optional). */
        String SIGN_ALG_ES512 = "ES512";

        /** RSASSA-PKCS-v1_5 using SHA-256 hash algorithm (recommended). */
        String SIGN_ALG_RS256 = "RS256";

        /** RSASSA-PKCS-v1_5 using SHA-384 hash algorithm (optional). */
        String SIGN_ALG_RS383 = "RS384";

        /** RSASSA-PKCS-v1_5 using SHA-512 hash algorithm (optional). */
        String SIGN_ALG_RS512 = "RS512";
    }

    /**
     * The Interface JWE.
     */
    interface JWE {

        /** The header critical parameter. */
        String HEADER_CRITICAL_PARAMETER = "crit";

        /** The Content Encryption Key (CEK) bit length. */
        String CEK_BITLENGTH = "cek_bitlength";

        /** The compression algorithm. */
        String COMPRESSION_ALG = "zip";

        /**
         * JWE Encryption Methods
         */

        /**
         * AES_128_CBC_HMAC_SHA_256 authenticated encryption using a 256 bit key (required).
         */
        String ENC_A128CBC_HS256 = "A128CBC-HS256";

        /**
         * AES_192_CBC_HMAC_SHA_384 authenticated encryption using a 384 bit key (optional).
         */
        String ENC_A192CBC_HS384 = "A192CBC-HS384";

        /**
         * AES_256_CBC_HMAC_SHA_512 authenticated encryption using a 512 bit key (required).
         */
        String ENC_A256CBC_HS512 = "A256CBC-HS512";

        /**
         * AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 128 bit key (recommended).
         */
        String ENC_A128GCM = "A128GCM";

        /**
         * AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 192 bit key (optional).
         */
        String ENC_A192GCM = "A192GCM";

        /**
         * AES in Galois/Counter Mode (GCM) (NIST.800-38D) using a 256 bit key (recommended).
         */
        String ENC_A256GCM = "A256GCM";

        /**
         * JWE Algorithms
         */

        /**
         * RSAES-PKCS1-V1_5 (RFC 3447) (required).
         */
        String ALG_RSA1_5 = "RSA1_5";

        /**
         * RSAES using Optimal Asymmetric Encryption Padding (OAEP) (RFC 3447), with the default parameters specified by RFC
         * 3447 in section A.2.1 (recommended).
         */
        String ALG_RSA_OAEP = "RSA-OAEP";

        /**
         * RSAES using Optimal Asymmetric Encryption Padding (OAEP) (RFC 3447), with the SHA-256 hash function and the MGF1 with
         * SHA-256 mask generation function (recommended).
         */
        String ALG_RSA_OAEP_256 = "RSA-OAEP-256";

    }
}