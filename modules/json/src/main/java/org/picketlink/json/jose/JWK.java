/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.picketlink.json.util.JsonUtil;

/**
 * The base class for JSON Web Keys (JWKs). It serializes to a JSON object.
 *
 * <p>
 * The following JSON object members are common to all JWK types:
 *
 * <ul>
 * <li>{@link #getKeyType kty} (required)
 * <li>{@link #getKeyUse use} (optional)
 * <li>{@link #getKeyOperations key_ops} (optional)
 * <li>{@link #getKeyIdentifier kid} (optional)
 * </ul>
 *
 * <p>
 * Example JSON object representation of a public RSA JWK:
 *
 * <pre>
 * {
 *   "kty" : "RSA",
 *   "n"   : "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
 *            4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
 *            tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
 *            QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
 *            SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
 *            w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
 *   "e"   : "AQAB",
 *   "alg" : "RS256",
 *   "kid" : "2011-04-29"
 * }
 * </pre>
 *
 * <p>
 * Example JSON object representation of a public and private RSA JWK (with both the first and the second private key
 * representations):
 *
 * <pre>
 * {
 *   "kty" : "RSA",
 *   "n"   : "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
 *            4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
 *            tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
 *            QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
 *            SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
 *            w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
 *   "e"   : "AQAB",
 *   "d"   : "X4cTteJY_gn4FYPsXB8rdXix5vwsg1FLN5E3EaG6RJoVH-HLLKD9
 *            M7dx5oo7GURknchnrRweUkC7hT5fJLM0WbFAKNLWY2vv7B6NqXSzUvxT0_YSfqij
 *            wp3RTzlBaCxWp4doFk5N2o8Gy_nHNKroADIkJ46pRUohsXywbReAdYaMwFs9tv8d
 *            _cPVY3i07a3t8MN6TNwm0dSawm9v47UiCl3Sk5ZiG7xojPLu4sbg1U2jx4IBTNBz
 *            nbJSzFHK66jT8bgkuqsk0GjskDJk19Z4qwjwbsnn4j2WBii3RL-Us2lGVkY8fkFz
 *            me1z0HbIkfz0Y6mqnOYtqc0X4jfcKoAC8Q",
 *   "p"   : "83i-7IvMGXoMXCskv73TKr8637FiO7Z27zv8oj6pbWUQyLPQBQxtPV
 *            nwD20R-60eTDmD2ujnMt5PoqMrm8RfmNhVWDtjjMmCMjOpSXicFHj7XOuVIYQyqV
 *            WlWEh6dN36GVZYk93N8Bc9vY41xy8B9RzzOGVQzXvNEvn7O0nVbfs",
 *   "q"   : "3dfOR9cuYq-0S-mkFLzgItgMEfFzB2q3hWehMuG0oCuqnb3vobLyum
 *            qjVZQO1dIrdwgTnCdpYzBcOfW5r370AFXjiWft_NGEiovonizhKpo9VVS78TzFgx
 *            kIdrecRezsZ-1kYd_s1qDbxtkDEgfAITAG9LUnADun4vIcb6yelxk",
 *   "dp"  : "G4sPXkc6Ya9y8oJW9_ILj4xuppu0lzi_H7VTkS8xj5SdX3coE0oim
 *            YwxIi2emTAue0UOa5dpgFGyBJ4c8tQ2VF402XRugKDTP8akYhFo5tAA77Qe_Nmtu
 *            YZc3C3m3I24G2GvR5sSDxUyAN2zq8Lfn9EUms6rY3Ob8YeiKkTiBj0",
 *   "dq"  : "s9lAH9fggBsoFR8Oac2R_E2gw282rT2kGOAhvIllETE1efrA6huUU
 *            vMfBcMpn8lqeW6vzznYY5SSQF7pMdC_agI3nG8Ibp1BUb0JUiraRNqUfLhcQb_d9
 *            GF4Dh7e74WbRsobRonujTYN1xCaP6TO61jvWrX-L18txXw494Q_cgk",
 *   "qi"  : "GyM_p6JrXySiz1toFgKbWV-JdI3jQ4ypu9rbMWx3rQJBfmt0FoYzg
 *            UIZEVFEcOqwemRN81zoDAaa-Bk0KWNGDjJHZDdDmFhW3AN7lI-puxk_mHZGJ11rx
 *            yR8O55XLSe3SPmRfKwZI6yU24ZxvQKFYItdldUKGzO6Ia6zTKhAVRU",
 *   "alg" : "RS256",
 *   "kid" : "2011-04-29"
 * }
 * </pre>
 *
 * <p>
 * See RFC 3447.
 *
 * <p>
 * See http://en.wikipedia.org/wiki/RSA_%28algorithm%29
 *
 * @author Giriraj Sharma
 */
public class JWK {

    /** The key parameters for JWK implementation of JOSE. */
    private JsonObject keyParameters;

    /**
     * Instantiates a new JWK.
     *
     * @param keyParameters the key parameters
     */
    protected JWK(JsonObject keyParameters) {
        this.keyParameters = keyParameters;
    }

    /**
     * Gets the key type, required.
     *
     * @return the key type
     */
    public String getKeyType() {
        return getKeyParameter(KEY_TYPE);
    }

    /**
     * Gets the key use, optional.
     *
     * @return the key use
     */
    public String getKeyUse() {
        return getKeyParameter(KEY_USE);
    }

    /**
     * Gets the key operations, optional.
     *
     * @return the key operations
     */
    public List<String> getKeyOperations() {
        return getKeyParameterValues(KEY_OPERATIONS);
    }

    /**
     * Gets the intended JOSE algorithm for the key, optional.
     *
     * @return the key algorithm
     */
    public String getKeyAlgorithm() {
        return getKeyParameter(KEY_ALGORITHM);
    }

    /**
     * Gets the key identifier, optional.
     *
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return getKeyParameter(KEY_ID);
    }

    /**
     * Gets the x509 URL.
     *
     * @return the x509 URL
     */
    public String getX509Url() {
        return getKeyParameter(X509_URL);
    }

    /**
     * Gets the x509 certificate chain.
     *
     * @return the x509 certificate chain
     */
    public List<String> getX509CertificateChain() {
        return getKeyParameterValues(X509_CERTIFICATE_CHAIN);
    }

    /**
     * Gets the x509 SHA1 certificate thumbprint.
     *
     * @return the x509 SHA1 certificate thumbprint
     */
    public String getX509SHA1CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA1_THUMBPRINT);
    }

    /**
     * Gets the x509 SHA256 certificate thumbprint.
     *
     * @return the x509 SHA256 certificate thumbprint
     */
    public String getX509SHA256CertificateThumbprint() {
        return getKeyParameter(X509_CERTIFICATE_SHA256_THUMBPRINT);
    }

    /**
     * Gets the modulus value for the RSA key.
     *
     * @return the modulus
     */
    public String getModulus() {
        return getKeyParameter(MODULUS);
    }

    /**
     * Gets the public exponent of the RSA key.
     *
     * @return the public exponent
     */
    public String getPublicExponent() {
        return getKeyParameter(PUBLIC_EXPONENT);
    }

    /**
     * Gets the private exponent of the RSA key.
     *
     * @return the private exponent
     */
    public String getPrivateExponent() {
        return getKeyParameter(PRIVATE_EXPONENT);
    }

    /**
     * Gets the first prime factor of the private RSA key.
     *
     * @return the prime p
     */
    public String getPrimeP() {
        return getKeyParameter(PRIME_P);
    }

    /**
     * Gets second prime factor of the private RSA key.
     *
     * @return the prime q
     */
    public String getPrimeQ() {
        return getKeyParameter(PRIME_Q);
    }

    /**
     * Gets the first factor Chinese Remainder Theorem exponent of the private RSA key.
     *
     * @return the prime exponent p
     */
    public String getPrimeExponentP() {
        return getKeyParameter(PRIME_EXPONENT_P);
    }

    /**
     * Gets the second factor Chinese Remainder Theorem exponent of the private RSA key.
     *
     * @return the prime exponent q
     */
    public String getPrimeExponentQ() {
        return getKeyParameter(PRIME_EXPONENT_Q);
    }

    /**
     * Gets the The first Chinese Remainder Theorem coefficient of the private RSA key.
     *
     * @return the CRT coefficient
     */
    public String getCRTCoefficient() {
        return getKeyParameter(CRT_COEFFICIENT);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getPlainkeyParameters();
    }

    /**
     * Gets the key parameter.
     *
     * @param name the name
     * @return the key parameter
     */
    private String getKeyParameter(String name) {
        return JsonUtil.getValue(name, this.keyParameters);
    }

    /**
     * Gets the key parameter values.
     *
     * @param name the name
     * @return the key parameter values
     */
    public List<String> getKeyParameterValues(String name) {
        return JsonUtil.getValues(name, this.keyParameters);
    }

    /**
     * Gets the plainkey parameters.
     *
     * @return the plainkey parameters
     */
    private String getPlainkeyParameters() {
        StringWriter keyParameterWriter = new StringWriter();

        Json.createWriter(keyParameterWriter).writeObject(this.keyParameters);

        return keyParameterWriter.getBuffer().toString();
    }

    /**
     * Builds up the {@link java.security.interfaces.RSAPublicKey} using modulus and public exponent of RSA Key.
     *
     * @return the RSA public key
     */
    public RSAPublicKey toRSAPublicKey() {
        if (getModulus() == null) {
            throw MESSAGES.invalidNullArgument("Modulus");
        }

        if (getPublicExponent() == null) {
            throw MESSAGES.invalidNullArgument("Public Exponent");
        }

        try {
            BigInteger modulus = new BigInteger(b64Decode(getModulus()));
            BigInteger publicExponent = new BigInteger(b64Decode(getPublicExponent()));

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (Exception e) {
            throw MESSAGES.cryptoCouldNotParseKey(toString(), e);
        }
    }

    /**
     * Gets the {@link javax.json.JsonObject}.
     *
     * @return the JSON object
     */
    public JsonObject getJsonObject() {
        return this.keyParameters;
    }
}