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
package org.picketlink.social.standalone.fb;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.Principal;

/**
 * An instance of {@link Principal} representing a facebook user
 *
 * @author Marcel Kolsteren
 * @since Sep 26, 2010
 */
public class FacebookPrincipal implements Principal, Serializable {
    private static final long serialVersionUID = 8086364702249670998L;

    private String accessToken;

    private String id;

    private String name;

    private String username;

    private String firstName;

    private String lastName;

    private JSONObject jsonObject;

    private String gender;

    private String timezone;

    private String locale;

    private String email;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public String getAttribute(String attributeName) {
        if (jsonObject == null) {
            return null;
        } else {
            try {
                return jsonObject.getString(attributeName);
            } catch (JSONException jsonEx) {
                if (jsonEx.getMessage() != null && jsonEx.getMessage().contains("not found")) {
                    return null;
                } else {
                    throw new RuntimeException(jsonEx);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "FacebookPrincipal [id=" + id + ", name=" + name + ", username=" + username + ", firstName=" + firstName +
               ", lastName=" + lastName + ", gender=" + gender + ", timezone=" + timezone + ", locale=" + locale + ", email=" + email + "]";
    }
}