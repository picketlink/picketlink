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

import org.picketlink.scim.model.v11.SCIMMetaData;

/**
 * Base SCIM Object
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public abstract class AbstractSCIMResource implements SCIMResource {

    private String id;
    private SCIMMetaData meta;
    private String[] schemas;

    @Override
    public String getId() {
        return id;
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

    public void setMeta(SCIMMetaData meta) {
        this.meta = meta;
    }

    public void setSchemas(String[] schemas) {
        this.schemas = schemas;
    }
}