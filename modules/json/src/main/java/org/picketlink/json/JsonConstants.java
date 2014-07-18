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
 * Define JSON Constants
 *
 * @author Anil Saldhana
 * @since March 07, 2014
 */
public interface JsonConstants {

    String RSA = "RSA";

    interface COMMON {
        String ALG = "alg";
        String ENC = "enc";
        String SIG = "sig";
        String KEY_ID = "kid";

        String HEADER_TYPE = "typ";
        String HEADER_CONTENT_TYPE = "cty";
        String HEADER_JWK_SET_URL = "jku";
        String HEADER_JSON_WEB_KEY = "keys";

        String RSA_SHA_256 = "RS256";
        String HMAC_SHA_256 = "HS256";
        String PERIOD = ".";
    }

    interface JWT {
        String CLAIM_ISSUER = "iss";
        String CLAIM_SUBJECT = "sub";
        String CLAIM_AUDIENCE = "aud";
        String CLAIM_EXPIRATION = "exp";
        String CLAIM_NOT_BEFORE = "nbf";
        String CLAIM_ISSUED_AT = "iat";
        String CLAIM_ID = "jti";
    }

    interface JWK {
        String KEY_TYPE = "kty";
        String KEY_USE = "use";
        String KEY_OPERATIONS = "key_ops";
        String KEY_ALGORITHM = "alg";
        String KEY_IDENTIFIER = "kid";
        String X509_URL = "x5u";
        String X509_CERTIFICATE_CHAIN = "x5c";
        String X509_CERTIFICATE_SHA1_THUMBPRINT = "x5t";
        String X509_CERTIFICATE_SHA256_THUMBPRINT = "x5t#S256";
    }

    interface JWK_RSA {
        String MODULUS = "n";
        String PUBLIC_EXPONENT = "e";
        String PRIVATE_EXPONENT = "d";
        String PRIME_P = "p";
        String PRIME_Q = "q";
        String PRIME_EXPONENT_P = "dp";
        String PRIME_EXPONENT_Q = "dq";
        String CRT_COEFFICIENT = "qi";
    }

    interface JWS {
        String SIGN_ALG_HS256 = "HS256";
        String SIGN_ALG_HS384 = "HS384";
        String SIGN_ALG_HS512 = "HS512";

        String SIGN_ALG_ES256 = "ES256";
        String SIGN_ALG_ES383 = "ES384";
        String SIGN_ALG_ES512 = "ES512";

        String SIGN_ALG_RS256 = "RS256";
        String SIGN_ALG_RS383 = "RS384";
        String SIGN_ALG_RS512 = "RS512";
    }

    interface JWE {
        String COMPRESSION_ALG = "zip";

        String AES = "AES";
        String AES_CBC_128 = "A128CBC";
        String AES_GCM_256 = "A256GCM";
        String INTEGRITY = "int";
        String IV = "iv";

        String ENC_ALG_RSA1_5 = "RSA1_5";
        String ENC_ALG_RSA_OAEP = "RSA-OAEP";
        String ENC_ALG_ECDH_ES = "ECDH-ES";
        String ENC_ALG_A128KW = "A128KW";
        String ENC_ALG_A256KW = "A256KW";

        String ENC_ALG_A128CBC = "A128CBC";
        String ENC_ALG_A192CBC = "A192CBC";
        String ENC_ALG_A256CBC = "A256CBC";
        String ENC_ALG_A512CBC = "A512CBC";
        String ENC_ALG_A128GCM = "A128GCM";
        String ENC_ALG_A192GCM = "A192GCM";
        String ENC_ALG_A256GCM = "A256GCM";
        String ENC_ALG_A512GCM = "A512GCM";
    }
}
