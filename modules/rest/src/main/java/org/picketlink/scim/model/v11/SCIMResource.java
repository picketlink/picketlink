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
    private SchemaExtensions[] schemaExtensions;

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

    public SchemaExtensions[] getSchemaExtensions() {
        return schemaExtensions;
    }

    public SCIMResource setSchemaExtensions(SchemaExtensions[] schemaExtensions) {
        this.schemaExtensions = schemaExtensions;
        return this;
    }

    public static class SchemaExtensions {
        private String schema;
        private boolean required;

        public String getSchema() {
            return schema;
        }

        public SchemaExtensions setSchema(String schema) {
            this.schema = schema;
            return this;
        }

        public boolean isRequired() {
            return required;
        }

        public SchemaExtensions setRequired(boolean required) {
            this.required = required;
            return this;
        }
    }
}
