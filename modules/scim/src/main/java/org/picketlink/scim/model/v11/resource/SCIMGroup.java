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

import java.net.URI;

import org.picketlink.scim.annotations.ResourceAttributeDefinition;
import org.picketlink.scim.annotations.ResourceDefinition;

/**
 * SCIM Group Type
 *
 * @author anil saldhana
 * @since Apr 8, 2013
 */
@ResourceDefinition(
        id = "urn:ietf:params:scim:schemas:core:2.0:Group",
        schema = "urn:ietf:params:scim:schemas:core:2.0:Group",
        name = "Group",
        endpointName = "/Group",
        description = "Group Account"
    )
public class SCIMGroup extends AbstractSCIMResource {

    public static URI ID = URI.create("urn:ietf:params:scim:schemas:core:2.0:Group");

    @ResourceAttributeDefinition(
            name = "displayName",
            type = "string",
            multiValued = false,
            description = "A human readable name for the Group.  REQUIRED.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private String displayName;

    @ResourceAttributeDefinition(
            name = "members",
            type = "complex",
            multiValued = false,
            description = "A list of members of the Group.",
            required = false,
            caseExact = false,
            mutability = "readWrite",
            returned = "default",
            uniqueness = "none"
            )
    private Members[] members;

    public String getDisplayName() {
        return displayName;
    }

    public SCIMGroup setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public Members[] getMembers() {
        return members;
    }

    public SCIMGroup setMembers(Members[] members) {
        this.members = members;
        return this;
    }

    public static class Members {

        @ResourceAttributeDefinition(
                name = "value",
                type = "string",
                multiValued = false,
                description = "The identifier of the member of this Group.",
                required = false,
                caseExact = false,
                mutability = "immutable",
                returned = "default",
                uniqueness = "none"
                )
        private String value;

        @ResourceAttributeDefinition(
                name = "$ref",
                type = "string",
                multiValued = false,
                description = "The URI of the corresponding to the member resource of this Group.",
                required = false,
                caseExact = false,
                mutability = "immutable",
                returned = "default",
                uniqueness = "none"
                )
        private String $ref;

        @ResourceAttributeDefinition(
                name = "display",
                type = "string",
                multiValued = false,
                description = "A label indicating the name of group.",
                required = false,
                caseExact = false,
                canonicalValues = {"User", "Group"},
                mutability = "immutable",
                returned = "default",
                uniqueness = "none"
                )
        private String display;

        public String getValue() {
            return value;
        }

        public Members setValue(String value) {
            this.value = value;
            return this;
        }

        public String get$ref() {
            return $ref;
        }

        public Members set$ref(String $ref) {
            this.$ref = $ref;
            return this;
        }

        public String getDisplay() {
            return display;
        }

        public Members setDisplay(String display) {
            this.display = display;
            return this;
        }
    }
}
