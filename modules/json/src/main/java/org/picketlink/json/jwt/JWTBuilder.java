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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;

import static org.picketlink.json.JsonConstants.COMMON.PERIOD;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_AUDIENCE;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_EXPIRATION;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ID;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ISSUED_AT;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_ISSUER;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_NOT_BEFORE;
import static org.picketlink.json.JsonConstants.JWT.CLAIM_SUBJECT;
import static org.picketlink.json.JsonConstants.JWT.HEADER_CONTENT_TYPE;
import static org.picketlink.json.JsonConstants.JWT.HEADER_TYPE;
import static org.picketlink.json.JsonMessages.MESSAGES;
import static org.picketlink.json.util.JsonUtil.b64Decode;

/**
 * <p>A {@link JWTBuilder} is used to construct {@link JWT} instances.
 * It provides a fluent API with methods to populate the claims for a given {@link JWT} type.</p>
 *
 * <p>{@link JWT} instances may also be built from a {@link java.lang.String} representing the
 * token in a JSON format.</p>
 *
 * <p>The default implementation builds {@link JWT} instances.</p>
 *
 * @param <T>
 * @param <B>
 */
public class JWTBuilder<T extends JWT, B extends JWTBuilder> {

    private final JsonObjectBuilder headersBuilder;
    private final JsonObjectBuilder claimsBuilder;
    private final Class<T> tokenType;

    public JWTBuilder() {
        this((Class<T>) JWT.class);
    }

    protected JWTBuilder(Class<T> tokenType) {
        this.tokenType = tokenType;
        this.headersBuilder = Json.createObjectBuilder();
        this.claimsBuilder = Json.createObjectBuilder();
        type("JWT");
    }

    public B type(String type) {
        header(HEADER_TYPE, type);
        return (B) this;
    }

    public B contentType(String contentType) {
        header(HEADER_CONTENT_TYPE, contentType);
        return (B) this;
    }

    public B id(String id) {
        claim(CLAIM_ID, id);
        return (B) this;
    }

    public B issuer(String issuer) {
        claim(CLAIM_ISSUER, issuer);
        return (B) this;
    }

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

    public B subject(String subject) {
        claim(CLAIM_SUBJECT, subject);
        return (B) this;
    }

    public B issuedAt(int issuedAt) {
        claim(CLAIM_ISSUED_AT, issuedAt);
        return (B) this;
    }

    public B expiration(int expirationTime) {
        claim(CLAIM_EXPIRATION, expirationTime);
        return (B) this;
    }

    public B notBefore(int notBefore) {
        claim(CLAIM_NOT_BEFORE, notBefore);
        return (B) this;
    }

    public B claim(String name, String... value) {
        setString(this.claimsBuilder, name, value);
        return (B) this;
    }

    public B claim(String name, int... value) {
        setInt(this.claimsBuilder, name, value);
        return (B) this;
    }

    public B header(String name, String... value) {
        setString(this.headersBuilder, name, value);
        return (B) this;
    }

    public B header(String name, int... value) {
        setInt(this.headersBuilder, name, value);
        return (B) this;
    }

    /**
     * <p>Builds a {@link JWT} instance using the provided claims.</p>
     *
     * @return
     */
    public T build() {
        return build(this.headersBuilder.build(), this.claimsBuilder.build());
    }

    /**
     * <p>Builds a {@link JWT} instance from its JSON representation.</p>
     *
     * @return
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
     * <p>Subclasses can use this method to obatain a reference to the {@link javax.json.JsonObjectBuilder} being
     * used to manage the claims set.</p>
     *
     * @return
     */
    protected JsonObjectBuilder getClaimsBuilder() {
        return this.claimsBuilder;
    }

    protected JsonObjectBuilder getHeadersBuilder() {
        return this.headersBuilder;
    }

    protected Class<T> getTokenType() {
        return this.tokenType;
    }

    protected T build(JsonObject headersObject, JsonObject claimsObject) {
        try {
            Constructor<T> constructor = this.tokenType.getDeclaredConstructor(JsonObject.class, JsonObject.class);

            constructor.setAccessible(true);

            return (T) constructor.newInstance(headersObject, claimsObject);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateToken(this.tokenType, e);
        }
    }

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
}
