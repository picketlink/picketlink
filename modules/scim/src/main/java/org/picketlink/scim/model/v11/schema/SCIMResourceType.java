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
 * SCIM Resource Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public class SCIMResourceType {

    private String id;
    private SCIMMetaData meta;
    private String[] schemas;
    private String name;
    private String description;
    private String schema;
    private String endpoint;
    private SchemaExtensions[] schemaExtensions;

    public String getId() {
        return id;
    }

    public SCIMMetaData getMeta() {
        return meta;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMeta(SCIMMetaData meta) {
        this.meta = meta;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }

    public String getName() {
        return name;
    }

    public SCIMResourceType setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SCIMResourceType setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public SCIMResourceType setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public SCIMResourceType setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public SchemaExtensions[] getSchemaExtensions() {
        return schemaExtensions;
    }

    public SCIMResourceType setSchemaExtensions(SchemaExtensions[] schemaExtensions) {
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
