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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;

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
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.Base64Util.b64Decode;

/**
 * <p>
 * A {@link JWTBuilder} is used to construct {@link JWT} instances. It provides a fluent API with methods to populate the claims
 * for a given {@link JWT} type.
 * </p>
 *
 * <p>
 * {@link JWT} instances may also be built from a {@link java.lang.String} representing the token in a JSON format.
 * </p>
 *
 * <p>
 * The default implementation builds {@link JWT} instances.
 * </p>
 *
 * @param <T> the generic type
 * @param <B> the generic type
 *
 * @author Giriraj Sharma
 */
public class JWTBuilder<T extends JWT, B extends JWTBuilder<?, ?>> {

    /** The headers builder for building the headers with their respective values. */
    private final JsonObjectBuilder headersBuilder;

    /** The claims builder for building the claims with their respective values. */
    private final JsonObjectBuilder claimsBuilder;

    /** The token type. */
    private final Class<T> tokenType;

    /**
     * Instantiates a new JWT builder.
     */
    public JWTBuilder() {
        this((Class<T>) JWT.class);
    }

    /**
     * <p>
     * Instantiates a new JWT builder with the token value.
     * </p>
     *
     * @param tokenType the token type
     */
    public JWTBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.headersBuilder = Json.createObjectBuilder();
        this.claimsBuilder = Json.createObjectBuilder();
        type("JWT");
    }

    /**
     * <p>
     * Gets the token type.
     * </p>
     *
     * @return the token type
     */
    protected Class<T> getTokenType() {
        return this.tokenType;
    }

    /**
     * <p>
     * Gets the JSON Web Token headers builder.
     * </p>
     *
     * @return the headers builder
     */
    protected JsonObjectBuilder getHeadersBuilder() {
        return this.headersBuilder;
    }

    /**
     * <p>
     * Subclasses can use this method to obtain a reference to the {@link javax.json.JsonObjectBuilder} being used to manage the
     * claims set.
     * </p>
     *
     * @return the claims builder
     */
    protected JsonObjectBuilder getClaimsBuilder() {
        return this.claimsBuilder;
    }

    /**
     * <p>
     * Sets the MIME Media Type [IANA.MediaTypes] of this complete JWT in contexts where this is useful to the application.
     * </p>
     *
     * @param type the type
     * @return the JWT builder
     */
    public B type(String type) {
        header(HEADER_TYPE, type);
        return (B) this;
    }

    /**
     * <p>
     * Sets the Content type used by this specification to convey structural information about the JWT.
     * </p>
     *
     * @param contentType the content type
     * @return the JWT builder
     */
    public B contentType(String contentType) {
        header(HEADER_CONTENT_TYPE, contentType);
        return (B) this;
    }

    /**
     * <p>
     * Sets the unique identifier for a JWT.
     * </p>
     *
     * @param id the id
     * @return the JWT builder
     */
    public B id(String id) {
        claim(CLAIM_ID, id);
        return (B) this;
    }

    /**
     * <p>
     * Sets the principal that issued the JWT.
     * </p>
     *
     * @param issuer the issuer
     * @return the JWT builder
     */
    public B issuer(String issuer) {
        claim(CLAIM_ISSUER, issuer);
        return (B) this;
    }

    /**
     * <p>
     * Sets the audience that the JWT is intended for.
     * </p>
     *
     * @param audience the audience
     * @return the JWT builder
     */
    public B audience(String... audience) {

        if (audience.length == 1) {
            claim(CLAIM_AUDIENCE, audience[0]);
        } else if (audience.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            for (String aud : audience) {
                arrayBuilder.add(aud);
            }
            this.claimsBuilder.add(CLAIM_AUDIENCE, arrayBuilder);
        }
        return (B) this;
    }

    /**
     * <p>
     * Sets the principal that is the subject of the JWT.
     * </p>
     *
     * @param subject the subject
     * @return the JWT builder
     */
    public B subject(String subject) {
        claim(CLAIM_SUBJECT, subject);
        return (B) this;
    }

    /**
     * <p>
     * Sets the time at which the JWT was issued.
     * </p>
     *
     * @param issuedAt the issued at
     * @return the JWT builder
     */
    public B issuedAt(int issuedAt) {
        claim(CLAIM_ISSUED_AT, issuedAt);
        return (B) this;
    }

    /**
     * <p>
     * Sets the expiration time on or after which the token MUST NOT be accepted for processing.
     * </p>
     *
     * @param expirationTime the expiration time
     * @return the JWT builder
     */
    public B expiration(int expirationTime) {
        claim(CLAIM_EXPIRATION, expirationTime);
        return (B) this;
    }

    /**
     * <p>
     * Sets the time before which the token MUST NOT be accepted for processing
     * </p>
     *
     * @param notBefore the not before
     * @return the JWT builder
     */
    public B notBefore(int notBefore) {
        claim(CLAIM_NOT_BEFORE, notBefore);
        return (B) this;
    }

    /**
     * <p>
     * Updates the claims set with the specified claim string value(s).
     * </p>
     *
     * @param name the name of the claim
     * @param value the value(s) of the claim
     * @return the JWT builder
     */
    public B claim(String name, String... value) {
        setString(this.claimsBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Updates the claims set with the specified claim int value(s).
     * </p>
     *
     * @param name the name of the claim
     * @param value the value(s) of the claim
     * @return the JWT builder
     */
    public B claim(String name, int... value) {
        setInt(this.claimsBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Updates the headers set with the specified header string value(s).
     * </p>
     *
     * @param name the name of the header
     * @param value the value(s) of the header
     * @return the JWT builder
     */
    public B header(String name, String... value) {
        setString(this.headersBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Updates the headers set with the specified header int value(s).
     * </p>
     *
     * @param name the name of the header
     * @param value the value(s) of the header
     * @return the JWT builder
     */
    public B header(String name, int... value) {
        setInt(this.headersBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Populates the headers set with the specified header {@link javax.json.JsonObject} collection.
     * </p>
     *
     * @param name the name of the header
     * @param value the value(s) of the header
     * @return the JWT builder
     */
    public B header(String name, List<JsonObject> value) {
        setJsonObject(this.headersBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Updates the headers set with the specified header {@link javax.json.JsonArray} value.
     * </p>
     *
     * @param name the name of the header
     * @param value the value(s) of the header
     * @return the JWT builder
     */
    public B header(String name, JsonArray value) {
        setJsonObject(this.headersBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>
     * Updates the {@link javax.json.JsonObjectBuilder} with the specified key value(s) pair.
     * </p>
     *
     * @param builder the builder
     * @param name the name of the header or claim
     * @param values the values for the header or claim
     * @return the JWT builder
     */
    private B setString(JsonObjectBuilder builder, String name, String... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (String value : values) {
                arrayBuilder.add(value.toString());
            }
            builder.add(name, arrayBuilder);
        }
        return (B) this;
    }

    /**
     * <p>
     * Updates the {@link javax.json.JsonObjectBuilder} with the specified key value(s) pair.
     * </p>
     *
     * @param builder the builder
     * @param name the name of the header or claim
     * @param values the values for the header or claim
     * @return the JWT builder
     */
    private B setInt(JsonObjectBuilder builder, String name, int... values) {
        if (values.length == 1) {
            builder.add(name, values[0]);
        } else if (values.length > 1) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (int value : values) {
                arrayBuilder.add(value);
            }
            builder.add(name, arrayBuilder);
        }
        return (B) this;
    }

    /**
     * <p>
     * Updates the the specified key of {@link javax.json.JsonObjectBuilder} with the {@link javax.json.JsonObject} collection.
     * </p>
     *
     * @param builder the builder
     * @param name the name of the header or claim
     * @param values the values for the header or claim
     * @return the JWT builder
     */
    private B setJsonObject(JsonObjectBuilder builder, String name, List<JsonObject> values) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Iterator<JsonObject> iterator = values.iterator();
        while (iterator.hasNext()) {
            arrayBuilder.add(iterator.next());
        }
        builder.add(name, arrayBuilder);
        return (B) this;
    }

    /**
     * <p>
     * Updates the the specified key of {@link javax.json.JsonObjectBuilder} with the {@link javax.json.JsonArray}.
     * </p>
     *
     * @param builder the builder
     * @param name the name of the header or claim
     * @param values the values for the header or claim
     * @return the JWT builder
     */
    private B setJsonObject(JsonObjectBuilder builder, String name, JsonArray values) {
        builder.add(name, values);
        return (B) this;
    }

    /**
     * <p>
     * Builds a {@link JWT} instance using the provided claims.
     * </p>
     *
     * @return the t
     */
    public T build() {
        return build(this.headersBuilder.build(), this.claimsBuilder.build());
    }

    /**
     * <p>
     * Builds a {@link JWT} instance from its JSON representation.
     * </p>
     *
     * @param json the jwt encoded json string
     * @return the t
     */
    public T build(String json) {
        if (!json.contains(PERIOD)) {
            throw MESSAGES.invalidFormat(json);
        }

        String[] portions = json.split("\\" + PERIOD);

        byte[] header = b64Decode(portions[0]);
        byte[] claims = b64Decode(portions[1]);

        return build(Json.createReader(new ByteArrayInputStream(header)).readObject(), Json.createReader(new ByteArrayInputStream(claims)).readObject());
    }

    /**
     * <p>
     * Builds the {@link javax.json.JsonObject} of headers and claims set.
     * </p>
     *
     * @param headersObject the headers object
     * @param claimsObject the claims object
     * @return
     */
    protected T build(JsonObject headersObject, JsonObject claimsObject) {
        try {
            Constructor<T> constructor = this.tokenType.getDeclaredConstructor(JsonObject.class, JsonObject.class);
            constructor.setAccessible(true);

            return (T) constructor.newInstance(headersObject, claimsObject);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateToken(this.tokenType, e);
        }
    }

}