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
 * Attribute that has value/type pair
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
public abstract class ValueTypeAttribute {

    @ResourceAttributeDefinition(
            name = "display",
            type = "string",
            multiValued = false,
            description = "A human readable name, primarily used for display purposes. READ-ONLY.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String display;

    @ResourceAttributeDefinition(
            name = "primary",
            type = "boolean",
            multiValued = false,
            description = "A Boolean value indicating the 'primary' or preferred attribute value for this attribute. The primary attribute value 'true' MUST appear no more than once.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private boolean primary;

    public String getDisplay() {
        return display;
    }

    public ValueTypeAttribute setDisplay(String display) {
        this.display = display;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    public ValueTypeAttribute setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }
}
