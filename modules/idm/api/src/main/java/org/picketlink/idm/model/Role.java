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
package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.AttributeProperty;

/**
 * Represents a role, which may be assigned to account objects in various ways
 * to grant specific application privileges
 *
 * @author Shane Bryzak
 */
public class Role extends AbstractIdentityType {
    private static final long serialVersionUID = -9044601754527766512L;

    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @AttributeProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Role)) {
            return false;
        }

        Role other = (Role) obj;

        // FIXME The Partition should also be taken into account.
        return other.getName() != null && this.getName() != null
                && other.getName().equals(this.getName());
    }
}
