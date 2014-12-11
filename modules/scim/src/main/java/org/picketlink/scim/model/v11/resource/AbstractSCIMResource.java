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
package org.picketlink.scim.model.v11.resource;

import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.model.v11.SCIMMetaData;

/**
 * Base SCIM Object
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public abstract class AbstractSCIMResource implements SCIMResource {

    @ResourceAttributeDefinition(
            name = "id",
            type = "string",
            multiValued = false,
            description = "Unique identifier for the SCIM resource as defined by the Service Provider. Each representation of the resource MUST include a non-empty id value. This identifier MUST be unique across the Service Provider's entire set of resources. It MUST be a stable, non-reassignable identifier that does not change when the same resource is returned in subsequent requests. The value of the id attribute is always issued by the Service Provider and MUST never be specified by the Service Consumer. REQUIRED.",
            required = true,
            caseExact = false,
            mutability = "readOnly",
            returned = "always",
            uniqueness = "server"
            )
    private String id;

    @ResourceAttributeDefinition(
            name = "externalId",
            type = "string",
            multiValued = false,
            description = "An identifier for the Resource as defined by the Service Consumer.",
            required = true,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String[] externalId;

    private SCIMMetaData meta;
    private String[] schemas;

    @Override
    public String getId() {
        return id;
    }

    public String[] getExternalId() {
        return this.externalId;
    }

    @Override
    public SCIMMetaData getMeta() {
        return meta;
    }

    @Override
    public String[] getSchemas() {
        return schemas;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExternalId(String[] externalId) {
        this.externalId = externalId;
    }

    public void setMeta(SCIMMetaData meta) {
        this.meta = meta;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }
}