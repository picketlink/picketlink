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
 * Base SCIM Object
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public abstract class AbstractResource {
    private String id;

    private String externalId;

    private Meta meta;

    private String[] schemas;

    public String getId() {
        return id;
    }

    public AbstractResource setId(String id) {
        this.id = id;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public AbstractResource setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Meta getMeta() {
        return meta;
    }

    public AbstractResource setMeta(Meta meta) {
        this.meta = meta;
        return this;
    }

    public String[] getSchemas() {
        return schemas;
    }

    public AbstractResource setSchemas(String[] schemas) {
        this.schemas = schemas;
        return this;
    }
}