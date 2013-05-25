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
 * SCIM Resource Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SCIMResource extends AbstractResource {

    private String name;
    private String description;
    private String schema;
    private String endpoint;
    private Attributes[] attributes;
    private SubAttributes[] subAttributes;

    public String getName() {
        return name;
    }

    public SCIMResource setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SCIMResource setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public SCIMResource setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public SCIMResource setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Attributes[] getAttributes() {
        return attributes;
    }

    public SCIMResource setAttributes(Attributes[] attributes) {
        this.attributes = attributes;
        return this;
    }

    public SubAttributes[] getSubAttributes() {
        return subAttributes;
    }

    public SCIMResource setSubAttributes(SubAttributes[] subAttributes) {
        this.subAttributes = subAttributes;
        return this;
    }

    public static class Attributes {
        private String name;
        private String type;
        private boolean multiValued;
        private String multiValuedAttributeChildName;
        private String description;
        private String schema;
        private boolean readOnly;
        private boolean required;
        private boolean caseExact;

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

        public String getMultiValuedAttributeChildName() {
            return multiValuedAttributeChildName;
        }

        public Attributes setMultiValuedAttributeChildName(String multiValuedAttributeChildName) {
            this.multiValuedAttributeChildName = multiValuedAttributeChildName;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Attributes setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getSchema() {
            return schema;
        }

        public Attributes setSchema(String schema) {
            this.schema = schema;
            return this;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public Attributes setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
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
    }

    public static class SubAttributes {

        private String name;
        private String type;
        private String description;
        private boolean readOnly;
        private boolean required;
        private boolean caseExact;
        private String[] canonicalValues;

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
    }
}
