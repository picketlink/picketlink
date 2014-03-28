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
package org.picketlink.json.key;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.picketlink.json.PicketLinkJSONMessages;
import org.picketlink.json.PicketLinkJSONConstants;

/**
 * Represents a JSON Web Key
 *
 * @author anil saldhana
 * @since Jul 24, 2012
 */
public class JSONWebKey {
    protected JSONArray keys = null;

    public JSONWebKey() {
    }

    /**
     * Set the Keys
     *
     * @param arr
     */
    public void setKeys(JSONArray arr) {
        this.keys = arr;
    }

    /**
     * Get a public key given its kid
     *
     * @param id
     * @return
     * @throws JSONException
     */
    public JSONObject getKey(String id) throws JSONException {
        if (keys == null) {
            throw PicketLinkJSONMessages.MESSAGES.jsonWebKeysMissing();
        }
        int length = keys.length();
        for (int i = 0; i < length; i++) {
            JSONObject json = (JSONObject) keys.get(i);
            if (id.equals(json.get(PicketLinkJSONConstants.KID))) {
                return json;
            }
        }
        return null;
    }

    /**
     * Get the keys
     *
     * @return
     */
    public JSONArray getKeys() {
        return keys;
    }

    /**
     * Parse a {@link JSONObject} into {@link JSONWebKey}
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parse(JSONObject jsonObj) throws JSONException {
        if (jsonObj == null) {
            throw PicketLinkJSONMessages.MESSAGES.invalidNullArgument("obj");
        }
        keys = jsonObj.getJSONArray(PicketLinkJSONConstants.KEYS);
    }
}
