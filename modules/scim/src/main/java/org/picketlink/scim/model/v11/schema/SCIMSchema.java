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
package org.picketlink.scim.model.v11.schema;

import org.picketlink.scim.model.v11.SCIMMetaData;

/**
 * SCIM SchemaElement Type
 *
 * @author Giriraj Sharma
 * @since Oct 1, 2014
 */
public class SCIMSchema {

    private String id;
    private String name;
    private String description;
    private Attributes[] attributes;
    private SCIMMetaData meta;

    public String getId() {
        return id;
    }

    public SCIMSchema setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public SCIMSchema setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SCIMSchema setDescription(String description) {
        this.description = description;
        return this;
    }

    public Attributes[] getAttributes() {
        return attributes;
    }

    public SCIMSchema setAttributes(Attributes[] attributes) {
        this.attributes = attributes;
        return this;
    }

    public SCIMMetaData getMeta() {
        return meta;
    }

    public SCIMSchema setMeta(SCIMMetaData meta) {
        this.meta = meta;
        return this;
    }

    public static class Attributes {

        private String name;
        private String type;
        private boolean multiValued;
        private String description;
        private boolean required;
        private boolean caseExact;
        private SubAttributes[] subAttributes;
        private String mutability;
        private String returned;
        private String uniqueness;

        public String getName() {
            return name;
        }

        public Attributes setName(String name) {
            this.name = name;
            return this;
        }

        public String getType() {
            return type;
        }

        public Attributes setType(String type) {
            this.type = type;
            return this;
        }

        public boolean isMultiValued() {
            return multiValued;
        }

        public Attributes setMultiValued(boolean multiValued) {
            this.multiValued = multiValued;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Attributes setDescription(String description) {
            this.description = description;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public Attributes setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isCaseExact() {
            return caseExact;
        }

        public Attributes setCaseExact(boolean caseExact) {
            this.caseExact = caseExact;
            return this;
        }

        public SubAttributes[] getSubAttributes() {
            return subAttributes;
        }

        public Attributes setSubAttributes(SubAttributes[] subAttributes) {
            this.subAttributes = subAttributes;
            return this;
        }

        public String getMutability() {
            return mutability;
        }

        public Attributes setMutability(String mutability) {
            this.mutability = mutability;
            return this;
        }

        public String getReturned() {
            return returned;
        }

        public Attributes setReturned(String returned) {
            this.returned = returned;
            return this;
        }

        public String getUniqueness() {
            return uniqueness;
        }

        public Attributes setUniqueness(String uniqueness) {
            this.uniqueness = uniqueness;
            return this;
        }
    }

    public static class SubAttributes {

        private String name;
        private String type;
        private String description;
        private boolean multiValued;
        private boolean readOnly;
        private boolean required;
        private boolean caseExact;
        private String[] canonicalValues;
        private String mutability;
        private String returned;
        private String uniqueness;

        public String getName() {
            return name;
        }

        public SubAttributes setName(String name) {
            this.name = name;
            return this;
        }

        public String getType() {
            return type;
        }

        public SubAttributes setType(String type) {
            this.type = type;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public SubAttributes setDescription(String description) {
            this.description = description;
            return this;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public SubAttributes setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public SubAttributes setRequired(boolean required) {
            this.required = required;
            return this;
        }

        public boolean isCaseExact() {
            return caseExact;
        }

        public SubAttributes setCaseExact(boolean caseExact) {
            this.caseExact = caseExact;
            return this;
        }

        public String[] getCanonicalValues() {
            return canonicalValues;
        }

        public SubAttributes setCanonicalValues(String[] canonicalValues) {
            this.canonicalValues = canonicalValues;
            return this;
        }

        public boolean isMultiValued() {
            return multiValued;
        }

        public SubAttributes setMultiValued(boolean multiValued) {
            this.multiValued = multiValued;
            return this;
        }

        public String getMutability() {
            return mutability;
        }

        public SubAttributes setMutability(String mutability) {
            this.mutability = mutability;
            return this;
        }

        public String getReturned() {
            return returned;
        }

        public SubAttributes setReturned(String returned) {
            this.returned = returned;
            return this;
        }

        public String getUniqueness() {
            return uniqueness;
        }

        public SubAttributes setUniqueness(String uniqueness) {
            this.uniqueness = uniqueness;
            return this;
        }
    }
}
