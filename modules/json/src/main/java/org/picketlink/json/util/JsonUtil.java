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
package org.picketlink.json.util;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Parses a JSON object.
 *
 * <p>
 * Specific JSON to Java entity mapping :
 *
 * <ul>
 * <li>JSON true|false map to {@link java.lang.Boolean}.
 * <li>JSON numbers map to {@link java.lang.Number}.
 * <ul>
 * <li>JSON integer numbers map to {@link java.lang.Long}.
 * <li>JSON fraction numbers map to {@link java.lang.Double}.
 * </ul>
 * <li>JSON strings map to {@link java.lang.String}.
 * <li>JSON arrays map to {@link javax.json.JsonArray}.
 * <li>JSON objects map to {@link javax.json.JsonObject}.
 * </ul>
 *
 * @throws ParseException If the string cannot be parsed to a valid JSON object.
 *
 * @author Anil Saldhana
 * @author Giriraj Sharma
 */
public class JsonUtil {

    /**
     * Parses the specified key value from the {@link javax.json.JsonObject} into a collection of strings.
     *
     * @param name the header or claim name
     * @param jsonObject the JSON object representing the headers set or the claims set.
     * @return a collection of values for the specified key in JsonObject
     */
    public static List<String> getValues(String name, JsonObject jsonObject) {

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
    @SuppressWarnings("unchecked")
    public static <R> R getValue(JsonValue value) {

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
    public static String getValue(String name, JsonObject jsonObject) {

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