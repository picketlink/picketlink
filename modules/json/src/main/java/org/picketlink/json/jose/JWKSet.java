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
 * @author Pedro Igor
 */
public class JWKSet {

    private final Map<String, JWK> keys = new HashMap<String, JWK>();

    public JWKSet() {

    }

    public JWKSet(JWK... keys) {
        for (JWK key : keys) {
            add(key);
        }
    }

    public JWKSet(JsonArray keys) {
        Iterator<JsonValue> iterator = keys.iterator();

        while (iterator.hasNext()) {
            add(new JWKBuilder().build((javax.json.JsonObject) iterator.next()));
        }
    }

    public JWKSet(String jsonKeySet) {
        JsonObject jsonObject = Json.createReader(new ByteArrayInputStream(jsonKeySet.getBytes())).readObject();
        JsonArray keys = jsonObject.getJsonArray(JsonConstants.COMMON.HEADER_JSON_WEB_KEY);
        Iterator<JsonValue> iterator = keys.iterator();

        while (iterator.hasNext()) {
            JsonObject jsonKey = (JsonObject) iterator.next();
            add(new JWKBuilder().build(jsonKey));
        }
    }

    public void add(JWK jwk) {
        this.keys.put(jwk.getKeyIdentifier(), jwk);
    }

    public JWK get(String kid) {
        return this.keys.get(kid);
    }

    public Map<String, JWK> getKeys() {
        return Collections.unmodifiableMap(this.keys);
    }

    public JsonObject getJsonObject() {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        Iterator<JWK> iterator = this.keys.values().iterator();

        while (iterator.hasNext()) {
            arrayBuilder.add(iterator.next().getJsonObject());
        }

        return Json.createObjectBuilder().add(JsonConstants.COMMON.HEADER_JSON_WEB_KEY, arrayBuilder.build()).build();
    }

    @Override
    public String toString() {
        StringWriter claimsWriter = new StringWriter();

        Json.createWriter(claimsWriter).writeObject(getJsonObject());

        return claimsWriter.getBuffer().toString();    }
}
