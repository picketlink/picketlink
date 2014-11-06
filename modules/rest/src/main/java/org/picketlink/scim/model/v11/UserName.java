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
package org.picketlink.scim.model.v11;

/**
 * SCIM User Name type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class UserName {
    private String formatted;
    private String familyName;
    private String givenName;
    private String middleName;
    private String honorificPrefix;
    private String honorificSuffix;

    public String getFormatted() {
        return formatted;
    }

    public UserName setFormatted(String formatted) {
        this.formatted = formatted;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public UserName setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public UserName setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getMiddleName() {
        return middleName;
    }

    public UserName setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public UserName setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
        return this;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public UserName setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
        return this;
    }
}