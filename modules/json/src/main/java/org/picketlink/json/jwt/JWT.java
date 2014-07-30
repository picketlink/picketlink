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
package org.picketlink.json.jwt;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_CONTENT_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.HEADER_TYPE;
import static org.picketlink.json.JsonConstants.COMMON.PERIOD;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_AUDIENCE;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_EXPIRATION;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ID;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ISSUED_AT;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ISSUER;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_NOT_BEFORE;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_SUBJECT;
import static org.picketlink.json.util.JsonUtil.b64Encode;

/**
 * <p>
 * This class represents a JSON Web Token, providing the standard claims set defined by its specification. It is a
 * representation of the claims to be transferred between two parties.
 * </p>
 *
 * <p>
 * Instances must be created by their corresponding {@link JWTBuilder}. Once created, instances are immutable.
 * </p>
 *
 * <p>
 * The JSON representation of a token is obtained via <code>toString()</code> method.
 * </p>
 *
 * <p>
 * Supports all registered claims of the JWT specification:
 *
 * <ul>
 * <li>iss - Issuer
 * <li>sub - Subject
 * <li>aud - Audience
 * <li>exp - Expiration Time
 * <li>nbf - Not Before
 * <li>iat - Issued At
 * <li>jti - JWT ID
 * <li>typ - Type
 * </ul>
 *
 * @author Pedro Igor
 */
public class JWT {

    /**
     * <p>
     * Holds the headers and their respective values.
     * </p>
     */
    private JsonObject headers;

    /**
     * <p>
     * Holds the claims set and their respective values.
     * </p>
     */
    private final JsonObject claims;

    /**
     * <p>
     * Creates a new instance using the headers set and claims set using their values from the given
     * {@link javax.json.JsonObject}.
     * </p>
     *
     * @param headers the headers
     * @param claims The claims set and their respective values.
     */
    protected JWT(JsonObject headers, JsonObject claims) {
        this.headers = headers;
        this.claims = claims;
    }

    /**
     * <p>
     * Encodes the JSON representation of headers and claims of a JWT according to the specification.
     * </p>
     *
     * <p>
     * In order to decode, refer to the corresponding {@link JWTBuilder} of this class.
     * </p>
     *
     * @return the string
     */
    public String encode() {
        return format(b64Encode(getPlainHeader()), b64Encode(getPlainClaims())).toString();
    }

    /**
     * <p>
     * Declares the MIME Media Type [IANA.MediaTypes] of this complete JWT in contexts where this is useful to the application.
     * </p>
     *
     * @return the string
     */
    public String geType() {
        return getHeader(HEADER_TYPE);
    }

    /**
     * <p>
     * Used by this specification to convey structural information about the JWT.
     * </p>
     *
     * @return the content type
     */
    public String getContentType() {
        return getHeader(HEADER_CONTENT_TYPE);
    }

    /**
     * <p>
     * The unique identifier for a JWT.
     * </p>
     *
     * @return the id
     */
    public String getId() {
        return getClaim(CLAIM_ID);
    }

    /**
     * <p>
     * The principal that issued the JWT.
     * </p>
     *
     * @return the issuer
     */
    public String getIssuer() {
        return getClaim(CLAIM_ISSUER);
    }

    /**
     * <p>
     * Identifies the audience that the JWT is intended for.
     * </p>
     *
     * @return the audience
     */
    public List<String> getAudience() {
        return getClaimValues(CLAIM_AUDIENCE);
    }

    /**
     * <p>
     * Identifies the principal that is the subject of the JWT.
     * </p>
     *
     * @return the subject
     */
    public String getSubject() {
        return getClaim(CLAIM_SUBJECT);
    }

    /**
     * <p>
     * The time at which the JWT was issued.
     * </p>
     *
     * @return the issued at
     */
    public Integer getIssuedAt() {
        return Integer.valueOf(getClaim(CLAIM_ISSUED_AT).toString());
    }

    /**
     * <p>
     * The expiration time on or after which the token MUST NOT be accepted for processing.
     * </p>
     *
     * @return the expiration
     */
    public Integer getExpiration() {
        return Integer.valueOf(getClaim(CLAIM_EXPIRATION).toString());
    }

    /**
     * <p>
     * The time before which the token MUST NOT be accepted for processing
     * </p>
     * .
     *
     * @return the not before
     */
    public Integer getNotBefore() {
        return Integer.valueOf(getClaim(CLAIM_NOT_BEFORE).toString());
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return format(getPlainHeader(), getPlainClaims()).toString();
    }

    /**
     * <p>
     * Subclasses can obtain from this method a {@link javax.json.JsonObject} instance containing the claims set and their
     * respective values.
     * </p>
     *
     * @return the claims
     */
    public JsonObject getClaims() {
        return this.claims;
    }

    /**
     * <p>
     * Subclasses can obtain from this method a {@link javax.json.JsonObject} instance containing headers and their respective
     * values.
     * </p>
     *
     * @return the headers
     */
    public JsonObject getHeaders() {
        return this.headers;
    }

    /**
     * <p>
     * Returns a claim given its name. If the claim represents an array, only the first value is returned.
     * </p>
     *
     * @param name the name
     * @return the claim
     */
    public String getClaim(String name) {
        return getValue(name, this.claims);
    }

    /**
     * <p>
     * Returns a claim given its name.
     * </p>
     *
     * @param name the name
     * @return the claim values
     */
    public List<String> getClaimValues(String name) {
        return getValues(name, this.claims);
    }

    /**
     * <p>
     * Returns a header given its name. If the header represents an array, only the first value is returned.
     * </p>
     *
     * @param name the name
     * @return the header
     */
    public String getHeader(String name) {
        return getValue(name, this.headers);
    }

    /**
     * <p>
     * Returns a header given its name.
     * </p>
     *
     * @param name the name
     * @return the header values
     */
    public List<String> getHeaderValues(String name) {
        return getValues(name, this.headers);
    }

    /**
     * <p>
     * Returns a {@link java.lang.StringBuilder} representing a JWT using its encoded format.
     * </p>
     *
     * @param header The string representing the header.
     * @param claimsSet The string representing the claims set.
     * @return the string builder
     */
    private StringBuilder format(String header, String claimsSet) {
        return new StringBuilder().append(header).append(PERIOD).append(claimsSet);
    }

    /**
     * Gets the plain claims set as a string representation.
     *
     * @return the plain claims set
     */
    private String getPlainClaims() {
        StringWriter claimsWriter = new StringWriter();

        Json.createWriter(claimsWriter).writeObject(this.claims);

        return claimsWriter.getBuffer().toString();
    }

    /**
     * Gets the plain header as a string representation.
     *
     * @return the plain header set
     */
    private String getPlainHeader() {
        StringWriter headerWriter = new StringWriter();

        Json.createWriter(headerWriter).writeObject(this.headers);

        return headerWriter.getBuffer().toString();
    }

    /**
     * Parses the specified key value from the {@link javax.json.JsonObject} into a collection of strings.
     *
     * @param name the header or claim name
     * @param jsonObject the JSON object representing the headers set or the claims set.
     * @return a collection of values for the specified key in JsonObject
     */
    private List<String> getValues(String name, JsonObject jsonObject) {
        JsonValue headerValue = jsonObject.get(name);
        List<String> values = new ArrayList<String>();

        if (headerValue != null) {
            if (JsonArray.class.isInstance(headerValue)) {
                JsonArray array = (JsonArray) headerValue;

                for (JsonValue aud : array.getValuesAs(JsonValue.class)) {
                    values.add(getValue(aud).toString());
                }
            } else {
                values.add(getValue(name, jsonObject).toString());
            }
        }
        return values;
    }

    /**
     * Gets the key value from the {@link javax.json.JsonValue}.
     *
     * @param <R> the generic type as value could be an object, array, number, string or boolean value.
     * @param value the JsonValue which is to be parsed.
     * @return
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
     * Gets the value of the specified key from the {@link javax.json.JsonObject}
     *
     * @param name the key whose value is to be retrieved.
     * @param jsonObject the JSON object representing headers or claims set.
     * @return the value of the specified key.
     */
    private String getValue(String name, JsonObject jsonObject) {
        JsonValue value = jsonObject.get(name);

        if (value != null) {
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
        }

        return null;
    }
}