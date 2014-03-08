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
package org.picketlink.json.sig;

import static org.picketlink.json.PicketLinkJSONConstants.COMMON.ALG;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a header
 *
 * @author anil saldhana
 * @since Jul 30, 2012
 */
public class JSONWebSignatureHeader {
    protected String alg;

    public JSONWebSignatureHeader(String alg) {
        this.alg = alg;
    }

    /**
     * Get the Algorithm
     *
     * @return
     */
    public String getAlg() {
        return alg;
    }

    /**
     * Get a {@link JSONObject} representation
     *
     * @return
     * @throws JSONException
     */
    public JSONObject get() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ALG, alg);
        return json;
    }

    /**
     * Given a {@link JSONObject}, obtain {@link JSONWebSignatureHeader}
     *
     * @param json
     * @return
     * @throws JSONException
     */
    public static JSONWebSignatureHeader create(JSONObject json) throws JSONException {
        return new JSONWebSignatureHeader(json.getString(ALG));
    }

    /**
     * Given a JSON String representing the header, obtain {@link JSONWebSignatureHeader}
     *
     * @param json
     * @return
     * @throws JSONExcption
     */
    public static JSONWebSignatureHeader create(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return create(jsonObject);
    }
}
