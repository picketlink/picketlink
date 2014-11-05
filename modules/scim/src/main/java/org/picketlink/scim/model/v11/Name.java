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

import org.picketlink.scim.annotations.ResourceAttributeDefinition;

/**
 * SCIM User Name type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class Name {

    @ResourceAttributeDefinition(
        description = "The full name, including all middle names, titles, and suffixes as appropriate, formatted for display (e.g. Ms. Barbara J Jensen, III.).",
        required = false,
        uniqueness = "none"
    )
    private String formatted;

    @ResourceAttributeDefinition(
        description = "The family name of the User, or Last Name in most Western languages (e.g. Jensen given the full name Ms. Barbara J Jensen, III.).",
        required = false,
        uniqueness = "none"
    )
    private String familyName;

    @ResourceAttributeDefinition
    private String givenName;

    @ResourceAttributeDefinition
    private String middleName;

    @ResourceAttributeDefinition
    private String honorificPrefix;

    @ResourceAttributeDefinition
    private String honorificSuffix;

    public String getFormatted() {
        return formatted;
    }

    public Name setFormatted(String formatted) {
        this.formatted = formatted;
        return this;
    }

    public String getFamilyName() {
        return familyName;
    }

    public Name setFamilyName(String familyName) {
        this.familyName = familyName;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public Name setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }

    public String getMiddleName() {
        return middleName;
    }

    public Name setMiddleName(String middleName) {
        this.middleName = middleName;
        return this;
    }

    public String getHonorificPrefix() {
        return honorificPrefix;
    }

    public Name setHonorificPrefix(String honorificPrefix) {
        this.honorificPrefix = honorificPrefix;
        return this;
    }

    public String getHonorificSuffix() {
        return honorificSuffix;
    }

    public Name setHonorificSuffix(String honorificSuffix) {
        this.honorificSuffix = honorificSuffix;
        return this;
    }
}