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

import org.picketlink.json.JsonConstants;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * JSON Web Key (JWK) set. Represented by a JSON object that contains an array of {@link JWK JSON Web Keys} (JWKs) as the value
 * of its "keys" member. Additional (custom) members of the JWK Set JSON object are also supported.
 *
 * <p>
 * Example JSON Web Key (JWK) set:
 *
 * <pre>
 * {
 *   "keys" : [ { "kty" : "EC",
 *                "crv" : "P-256",
 *                "x"   : "MKBCTNIcKUSDii11ySs3526iDZ8AiTo7Tu6KPAqv7D4",
 *                "y"   : "4Etl6SRW2YiLUrN5vfvVHuhp7x8PxltmWWlbbM4IFyM",
 *                "use" : "enc",
 *                "kid" : "1" },
 *
 *              { "kty" : "RSA",
 *                "n"   : "0vx7agoebGcQSuuPiLJXZptN9nndrQmbXEps2aiAFbWhM78LhWx
 *                         4cbbfAAtVT86zwu1RK7aPFFxuhDR1L6tSoc_BJECPebWKRXjBZCiFV4n3oknjhMs
 *                         tn64tZ_2W-5JsGY4Hc5n9yBXArwl93lqt7_RN5w6Cf0h4QyQ5v-65YGjQR0_FDW2
 *                         QvzqY368QQMicAtaSqzs8KJZgnYb9c7d0zgdAZHzu6qMQvRL5hajrn1n91CbOpbI
 *                         SD08qNLyrdkt-bFTWhAI4vMQFh6WeZu0fM4lFd2NcRwr3XPksINHaQ-G_xBniIqb
 *                         w0Ls1jF44-csFCur-kEgU8awapJzKnqDKgw",
 *                "e"   : "AQAB",
 *                "alg" : "RS256",
 *                "kid" : "2011-04-29" } ]
 * }
 * </pre>
 *
 * @author Pedro Igor
 */
public class JWKSet {

    private final Map<String, JWK> keys = new HashMap<String, JWK>();

    /**
     * Instantiates a new JWK set.
     */
    public JWKSet() {

    }

    /**
     * Instantiates a new JWK set using JWK Key(s).
     *
     * @param keys the keys
     */
    public JWKSet(JWK... keys) {
        for (JWK key : keys) {
            add(key);
        }
    }

    /**
     * Instantiates a new JWK set using {@link javax.json.JsonArray} of keys.
     *
     * @param keys the keys
     */
    public JWKSet(JsonArray keys) {
        Iterator<JsonValue> iterator = keys.iterator();

        while (iterator.hasNext()) {
            add(new JWKBuilder().build((javax.json.JsonObject) iterator.next()));
        }
    }

    /**
     * Instantiates a new JWK set using string representation of JSON Key Set.
     *
     * @param jsonKeySet the JSON key set
     */
    public JWKSet(String jsonKeySet) {
        JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(jsonKeySet.getBytes())).readObject();
        JsonArray keys = jsonObject.getJsonArray(JsonConstants.COMMON.HEADER_JSON_WEB_KEY);
        Iterator<JsonValue> iterator = keys.iterator();

        while (iterator.hasNext()) {
            JsonObject jsonKey = (JsonObject) iterator.next();
            add(new JWKBuilder().build(jsonKey));
        }
    }

    /**
     * Adds the {@link org.picketlink.json.jose.JWK} to the JWKSet.
     *
     * @param jwk the jwk
     */
    public void add(JWK jwk) {
        this.keys.put(jwk.getKeyIdentifier(), jwk);
    }

    /**
     * Gets the {@link org.picketlink.json.jose.JWK} of specified identifier from JWKSet.
     *
     * @param kid the kid
     * @return the jwk
     */
    public JWK get(String kid) {
        return this.keys.get(kid);
    }

    /**
     * Gets the key map contained in JWKSet.
     *
     * @return the keys
     */
    public Map<String, JWK> getKeys() {
        return Collections.unmodifiableMap(this.keys);
    }

    /**
     * Gets the JSON object of {@link org.picketlink.json.jose.JWK}.
     *
     * @return the json object
     */
    public JsonObject getJsonObject() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Iterator<JWK> iterator = this.keys.values().iterator();

        while (iterator.hasNext()) {
            arrayBuilder.add(iterator.next().getJsonObject());
        }

        return Json.createObjectBuilder().add(JsonConstants.COMMON.HEADER_JSON_WEB_KEY, arrayBuilder.build()).build();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringWriter claimsWriter = new StringWriter();

        Json.createWriter(claimsWriter).writeObject(getJsonObject());

        return claimsWriter.getBuffer().toString();
    }
}
