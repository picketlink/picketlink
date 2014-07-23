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

        /** The key id. */
        String KEY_ID = "kid";

        /** The header type. */
        String HEADER_TYPE = "typ";

        /** The header content type. */
        String HEADER_CONTENT_TYPE = "cty";

        /** The header jwk set url. */
        String HEADER_JWK_SET_URL = "jku";

        /** The header json web key. */
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

        /** The key type. */
        String KEY_TYPE = "kty";

        /** The key use. */
        String KEY_USE = "use";

        /** The key operations. */
        String KEY_OPERATIONS = "key_ops";

        /** The key algorithm. */
        String KEY_ALGORITHM = "alg";

        /** The key identifier. */
        String KEY_IDENTIFIER = "kid";

        /** The X509 certificate url. */
        String X509_URL = "x5u";

        /** The X509 certificate chain. */
        String X509_CERTIFICATE_CHAIN = "x5c";

        /** The X509 certificate SHA1 thumbprint. */
        String X509_CERTIFICATE_SHA1_THUMBPRINT = "x5t";

        /** The X509 certificate SHA256 thumbprint. */
        String X509_CERTIFICATE_SHA256_THUMBPRINT = "x5t#S256";
    }

    /**
     * The Interface RSA JSON Web Key.
     */
    interface JWK_RSA {

        /** The modulus. */
        String MODULUS = "n";

        /** The public exponent. */
        String PUBLIC_EXPONENT = "e";

        /** The private exponent. */
        String PRIVATE_EXPONENT = "d";

        /** The prime p. */
        String PRIME_P = "p";

        /** The prime q. */
        String PRIME_Q = "q";

        /** The prime exponent p. */
        String PRIME_EXPONENT_P = "dp";

        /** The prime exponent q. */
        String PRIME_EXPONENT_Q = "dq";

        /** The crt coefficient. */
        String CRT_COEFFICIENT = "qi";
    }

    /**
     * The Interface JSON Web Signature.
     */
    interface JWS {

        /** The SIGN_ALG_HS256 signature algorithm. */
        String SIGN_ALG_HS256 = "HS256";

        /** The SIGN_ALG_HS384 signature algorithm. */
        String SIGN_ALG_HS384 = "HS384";

        /** The SIGN_ALG_HS512 signature algorithm. */
        String SIGN_ALG_HS512 = "HS512";

        /** The SIGN_ALG_ES256 signature algorithm. */
        String SIGN_ALG_ES256 = "ES256";

        /** The SIGN_ALG_ES383 signature algorithm. */
        String SIGN_ALG_ES383 = "ES384";

        /** The SIGN_ALG_ES512 signature algorithm. */
        String SIGN_ALG_ES512 = "ES512";

        /** The SIGN_ALG_RS256 signature algorithm. */
        String SIGN_ALG_RS256 = "RS256";

        /** The SIGN_ALG_RS383 signature algorithm. */
        String SIGN_ALG_RS383 = "RS384";

        /** The SIGN_ALG_RS512 signature algorithm. */
        String SIGN_ALG_RS512 = "RS512";
    }

    /**
     * The Interface JWE.
     */
    interface JWE {

        /** The header critical parameter. */
        String HEADER_CRITICAL_PARAMETER = "crit";

        /** The content encryption key bitlength. */
        String CEK_BITLENGTH = "cek_bitlength";

        /** The compression algorithm. */
        String COMPRESSION_ALG = "zip";

        /** The ENC_A128CBC_HS256 encryption algorithm. */
        String ENC_A128CBC_HS256 = "A128CBC-HS256";

        /** The ENC_A192CBC_HS384 encryption algorithm. */
        String ENC_A192CBC_HS384 = "A192CBC-HS384";

        /** The ENC_A256CBC_HS512 encryption algorithm. */
        String ENC_A256CBC_HS512 = "A256CBC-HS512";

        /** The ENC_A128CBC_HS256_DEPRECATED encryption algorithm. */
        String ENC_A128CBC_HS256_DEPRECATED = "A128CBC+HS256";

        /** The ENC_A256CBC_HS512_DEPRECATED encryption algorithm. */
        String ENC_A256CBC_HS512_DEPRECATED = "A256CBC+HS512";

        /** The ENC_A128GCM encryption algorithm. */
        String ENC_A128GCM = "A128GCM";

        /** The ENC_A192GCM encryption algorithm. */
        String ENC_A192GCM = "A192GCM";

        /** The ENC_A256GCM encryption algorithm. */
        String ENC_A256GCM = "A256GCM";

        /** The ALG_RSA1_5 algorithm. */
        String ALG_RSA1_5 = "RSA1_5";

        /** The ALG_RSA_OAEP algorithm. */
        String ALG_RSA_OAEP = "RSA-OAEP";

        /** The ALG_RSA_OAEP_256 algorithm. */
        String ALG_RSA_OAEP_256 = "RSA-OAEP-256";

        /** The ALG_A128KW algorithm. */
        String ALG_A128KW = "A128KW";

        /** The ALG_A192KW algorithm. */
        String ALG_A192KW = "A192KW";

        /** The ALG_A256KW algorithm. */
        String ALG_A256KW = "A256KW";

        /** The dir. */
        String DIR = "dir";

        /** The ALG_ECDH_ES algorithm. */
        String ALG_ECDH_ES = "ECDH_ES";

        /** The ALG_ECDH_ES_A128KW algorithm. */
        String ALG_ECDH_ES_A128KW = "ECDH-ES+A128KW";

        /** The ALG_ECDH_ES_A192KW algorithm. */
        String ALG_ECDH_ES_A192KW = "ECDH-ES+A192KW";

        /** The ALG_ECDH_ES_A256KW algorithm. */
        String ALG_ECDH_ES_A256KW = "ECDH-ES+A256KW";

        /** The ALG_A128GCMKW algorithm. */
        String ALG_A128GCMKW = "A128GCMKW";

        /** The ALG_A192GCMKW algorithm. */
        String ALG_A192GCMKW = "A192GCMKW";

        /** The ALG_A256GCMKW algorithm. */
        String ALG_A256GCMKW = "A256GCMKW";

        /** The ALG_PBES2_HS256_A128KW algorithm. */
        String ALG_PBES2_HS256_A128KW = "PBES2-HS256+A128KW";

        /** The ALG_PBES2_HS256_A192KW algorithm. */
        String ALG_PBES2_HS256_A192KW = "PBES2-HS256+A192KW";

        /** The ALG_PBES2_HS256_A256KW algorithm. */
        String ALG_PBES2_HS256_A256KW = "PBES2-HS256+A256KW";

    }
}
