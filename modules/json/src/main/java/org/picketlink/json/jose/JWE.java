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

import static org.picketlink.json.JsonConstants.COMMON.ALG;
import static org.picketlink.json.JsonConstants.COMMON.ENC;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_CONTENT_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JSON_WEB_KEY;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JWK_SET_URL;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.KEY_ID;
import static org.picketlink.json.JsonConstants.JWE.CEK_BITLENGTH;
import static org.picketlink.json.JsonConstants.JWE.COMPRESSION_ALG;
import static org.picketlink.json.JsonConstants.JWE.HEADER_CRITICAL_PARAMETER;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_CHAIN;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA1_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA256_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_URL;
import static org.picketlink.json.util.Base64Util.b64Encode;

import java.io.StringWriter;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import org.picketlink.json.util.JsonUtil;

/**
 * JSON Web Encryption (JWE) header.
 *
 * <p>
 * Supports all Principal Registered Parameter Names of the JWE specification:
 *
 * <ul>
 * <li>{@link #getType() alg}</li>
 * <li>{@link #getContentType() typ}</li>
 * <li>{@link #getAlgorithm() cty}</li>
 * <li>{@link #getEncryptionAlgorithm() enc}</li>
 * <li>{@link #getCompressionAlgorithm() zip}</li>
 * <li>{@link #getJWK() keys}</li>
 * <li>{@link #getJWKSet() jku}</li>
 * <li>{@link #getX509Url() x5u}</li>
 * <li>{@link #getX509CertificateChain() x5c}</li>
 * <li>{@link #getX509SHA1CertificateThumbprint() x5t}</li>
 * <li>{@link #getX509SHA256CertificateThumbprint() x5t#S256}</li>
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
 * @author Giriraj Sharma
 */
public class JWE {

    /** The JOSE headers for JWE. */
    private JsonObject headers;

    /**
     * Instantiates a new JWE.
     *
     * @param headers the headers
     */
    protected JWE(JsonObject headers) {
        this.headers = headers;
    }

    /**
     * <p>
     * Encodes the JSON representation of headers of a JWE according to the specification.
     * </p>
     *
     * <p>
     * In order to decode, refer to the corresponding {@link JWEBuilder} of this class.
     * </p>
     *
     * @return the string
     */
    public String encode() {
        return b64Encode(getPlainHeaders());
    }

    /**
     * Gets the type of JOSE Header.
     *
     * @return the string
     */
    public String getType() {
        return getHeader(HEADER_TYPE);
    }

    /**
     * Gets the content type of JOSE Header.
     *
     * @return the content type
     */
    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * Gets the algorithm used to encrypt or determine the value of the Content Encryption Key (CEK).
     *
     *
     * <ul>
     * <li>{@link #RSA1_5}
     * <li>{@link #RSA_OAEP RSA-OAEP}
     * <li>{@link #RSA_OAEP_256 RSA-OAEP-256}
     * </ul>
     *
     * @return the algorithm
     */
    public String getAlgorithm() {
        return getHeader(ALG);
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
     * @return the encryption algorithm
     */
    public String getEncryptionAlgorithm() {
        return getHeader(ENC);
    }

    /**
     * Gets the Content Encryption Key bit length.
     *
     * @return the Content Encryption Key bit length
     */
    public String getCEKBitLength() {
        return getHeader(CEK_BITLENGTH);
    }

    /**
     * Gets the key identifier used to determine the private key needed to decrypt the JWE.
     *
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return getHeader(KEY_ID);
    }

    /**
     * Gets the compression algorithm. The zip (compression algorithm) applied to the Plaintext before encryption, if any. The
     * zip value defined by this specification is:
     *
     * <ul>
     * DEF - Compression with the DEFLATE [RFC1951] algorithm
     * </ul>
     *
     * @return the compression algorithm
     */
    public String getCompressionAlgorithm() {
        return getHeader(COMPRESSION_ALG);
    }

    /**
     * Gets the JWK Set.
     *
     * <p>
     * The JWK Set resource contains the public key to which the JWE was encrypted; this can be used to determine the private
     * key needed to decrypt the JWE.
     *
     * @return the JWK Set
     */
    public String getJWKSet() {
        return getHeader(HEADER_JWK_SET_URL);
    }

    /**
     * Gets the JWK.
     *
     * <p>
     * JWK key is the public key to which the JWE was encrypted; this can be used to determine the private key needed to decrypt
     * the JWE.
     *
     * @return the JWK
     */
    public String getJWK() {
        return getHeader(HEADER_JSON_WEB_KEY);
    }

    /**
     * Gets the x509 URL.
     *
     * <p>
     * X.509 public key certificate or certificate chain [RFC5280] contains the public key to which the JWE was encrypted; this
     * can be used to determine the private key needed to decrypt the JWE.
     *
     * @return the x509 URL
     */
    public String getX509Url() {
        return getHeader(X509_URL);
    }

    /**
     * Gets the x509 certificate chain.
     *
     * <p>
     * The X.509 public key certificate or certificate chain [RFC5280] contains the public key to which the JWE was encrypted;
     * this can be used to determine the private key needed to decrypt the JWE.
     *
     * @return the x509 certificate chain
     */
    public List<String> getX509CertificateChain() {
        return getHeaderValues(X509_CERTIFICATE_CHAIN);
    }

    /**
     * Gets the x509 SHA1 certificate thumbprint.
     *
     * <p>
     * The certificate referenced by the thumbprint contains the public key to which the JWE was encrypted; this can be used to
     * determine the private key needed to decrypt the JWE.
     *
     * @return the x509 SHA1 certificate thumbprint
     */
    public String getX509SHA1CertificateThumbprint() {
        return getHeader(X509_CERTIFICATE_SHA1_THUMBPRINT);
    }

    /**
     * Gets the x509 SHA256 certificate thumbprint.
     *
     * <p>
     * The certificate referenced by the thumbprint contains the public key to which the JWE was encrypted; this can be used to
     * determine the private key needed to decrypt the JWE.
     *
     * @return the x509 SHA256 certificate thumbprint
     */
    public String getX509SHA256CertificateThumbprint() {
        return getHeader(X509_CERTIFICATE_SHA256_THUMBPRINT);
    }

    /**
     * Gets the critical header.
     *
     * <p>
     * The "crit" (critical) Header Parameter indicates that extensions to the initial RFC versions of [[ this specification ]]
     * and [JWA] are being used that MUST be understood and processed. Its value is an array listing the Header Parameter names
     * present in the JOSE Header that use those extensions.
     *
     * @return the critical header
     */
    public List<String> getCriticalHeader() {
        return getHeaderValues(HEADER_CRITICAL_PARAMETER);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getPlainHeaders();
    }

    /**
     * Gets the {@link javax.json.JsonObject} headers.
     *
     * @return the headers
     */
    public JsonObject getHeaders() {
        return this.headers;
    }

    /**
     * Gets the string representation of headers.
     *
     * @param name the name
     * @return the header
     */
    public String getHeader(String name) {
        return JsonUtil.getValue(name, this.headers);
    }

    /**
     * Gets the header values for the specified name.
     *
     * @param name the name
     * @return the header values
     */
    public List<String> getHeaderValues(String name) {
        return JsonUtil.getValues(name, this.headers);
    }

    /**
     * Gets the {@link javax.json.JsonObject}.
     *
     * @return the JSON object
     */
    public JsonObject getJsonObject() {
        return this.headers;
    }

    /**
     * Gets the plain headers.
     *
     * @return the plain headers
     */
    private String getPlainHeaders() {
        StringWriter keyParameterWriter = new StringWriter();

        Json.createWriter(keyParameterWriter).writeObject(this.headers);

        return keyParameterWriter.getBuffer().toString();
    }
}