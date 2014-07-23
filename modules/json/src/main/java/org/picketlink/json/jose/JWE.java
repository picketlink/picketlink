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

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;
import static org.picketlink.json.JsonConstants.COMMON.ALG;
import static org.picketlink.json.JsonConstants.COMMON.ENC;
import static org.picketlink.json.JsonConstants.COMMON.KEY_ID;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_CONTENT_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JWK_SET_URL;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_JSON_WEB_KEY;
import static org.picketlink.json.JsonConstants.JWK.X509_URL;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_CHAIN;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA1_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWK.X509_CERTIFICATE_SHA256_THUMBPRINT;
import static org.picketlink.json.JsonConstants.JWE.COMPRESSION_ALG;
import static org.picketlink.json.JsonConstants.JWE.HEADER_CRITICAL_PARAMETER;
import static org.picketlink.json.JsonConstants.JWE.CEK_BITLENGTH;
import static org.picketlink.json.util.JsonUtil.b64Encode;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * The Class JWE.
 *
 * @author Giriraj Sharma
 */
public class JWE {

    /** The headers. */
    private JsonObject headers;

    /**
     * Instantiates a new jwe.
     *
     * @param headers the headers
     */
    protected JWE(JsonObject headers) {
        this.headers = headers;
    }

    /**
     * Encode.
     *
     * @return the string
     */
    public String encode() {
        return b64Encode(getPlainHeaders());
    }

    /**
     * Ge type.
     *
     * @return the string
     */
    public String geType() {
        return getHeader(HEADER_TYPE);
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * Gets the algorithm.
     *
     * @return the algorithm
     */
    public String getAlgorithm() {
        return getHeader(ALG);
    }

    /**
     * Gets the encryption algorithm.
     *
     * @return the encryption algorithm
     */
    public String getEncryptionAlgorithm() {
        return getHeader(ENC);
    }

    /**
     * Gets the CEK bit length.
     *
     * @return the CEK bit length
     */
    public String getCEKBitLength() {
        return getHeader(CEK_BITLENGTH);
    }

    /**
     * Gets the key identifier.
     *
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return getHeader(KEY_ID);
    }

    /**
     * Gets the compression algorithm.
     *
     * @return the compression algorithm
     */
    public String getCompressionAlgorithm() {
        return getHeader(COMPRESSION_ALG);
    }

    /**
     * Gets the JWK set.
     *
     * @return the JWK set
     */
    public String getJWKSet() {
        return getHeader(HEADER_JWK_SET_URL);
    }

    /**
     * Gets the jwk.
     *
     * @return the jwk
     */
    public String getJWK() {
        return getHeader(HEADER_JSON_WEB_KEY);
    }

    /**
     * Gets the x509 url.
     *
     * @return the x509 url
     */
    public String getX509Url() {
        return getHeader(X509_URL);
    }

    /**
     * Gets the x509 certificate chain.
     *
     * @return the x509 certificate chain
     */
    public List<String> getX509CertificateChain() {
        return getHeaderValues(X509_CERTIFICATE_CHAIN);
    }

    /**
     * Gets the x509 sha1 certificate thumbprint.
     *
     * @return the x509 sha1 certificate thumbprint
     */
    public String getX509SHA1CertificateThumbprint() {
        return getHeader(X509_CERTIFICATE_SHA1_THUMBPRINT);
    }

    /**
     * Gets the x509 sha256 certificate thumbprint.
     *
     * @return the x509 sha256 certificate thumbprint
     */
    public String getX509SHA256CertificateThumbprint() {
        return getHeader(X509_CERTIFICATE_SHA256_THUMBPRINT);
    }

    /**
     * Gets the critical header.
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
     * Gets the headers.
     *
     * @return the headers
     */
    public JsonObject getHeaders() {
        return this.headers;
    }

    /**
     * Gets the header.
     *
     * @param name the name
     * @return the header
     */
    public String getHeader(String name) {
        return getValue(name, this.headers);
    }

    /**
     * Gets the header values.
     *
     * @param name the name
     * @return the header values
     */
    public List<String> getHeaderValues(String name) {
        return getValues(name, this.headers);
    }

    /**
     * Gets the json object.
     *
     * @return the json object
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

    /**
     * Gets the values.
     *
     * @param name the name
     * @param jsonObject the json object
     * @return the values
     */
    private List<String> getValues(String name, JsonObject jsonObject) {
        JsonValue headerValue = jsonObject.get(name);
        List<String> values = new ArrayList<String>();

        if (headerValue != null) {
            if (JsonArray.class.isInstance(headerValue)) {
                JsonArray array = (JsonArray) headerValue;

                for (JsonValue value : array.getValuesAs(JsonValue.class)) {
                    values.add(getValue(value).toString());
                }
            } else {
                values.add(getValue(name, jsonObject).toString());
            }
        }

        return values;
    }

    /**
     * Gets the value.
     *
     * @param <R> the generic type
     * @param value the value
     * @return the value
     */
    private <R> R getValue(JsonValue value) {
        if (ARRAY.equals(value.getValueType())) {
            JsonArray array = (JsonArray) value;
            for (JsonValue jsonValue : array) {
                return getValue(jsonValue);
            }
        } else if (STRING.equals(value.getValueType())) {
            return (R) ((JsonString) value).getString();
        } else if (NUMBER.equals(value.getValueType())) {
            return (R) ((JsonNumber) value).bigDecimalValue().toPlainString();
        } else if (TRUE.equals(value.getValueType()) || FALSE.equals(value.getValueType())) {
            return (R) Boolean.valueOf(value.toString());
        }

        return null;
    }

    /**
     * Gets the value.
     *
     * @param name the name
     * @param jsonObject the json object
     * @return the value
     */
    private String getValue(String name, JsonObject jsonObject) {
        JsonValue value = jsonObject.get(name);

        if (ARRAY.equals(value.getValueType())) {
            JsonArray array = (JsonArray) value;
            for (JsonValue jsonValue : array) {
                return getValue(jsonValue);
            }
        } else if (STRING.equals(value.getValueType())) {
            return ((JsonString) value).getString();
        } else if (NUMBER.equals(value.getValueType())) {
            return ((JsonNumber) value).bigDecimalValue().toPlainString();
        } else if (TRUE.equals(value.getValueType()) || FALSE.equals(value.getValueType())) {
            return value.toString();
        }
        return null;
    }
}
