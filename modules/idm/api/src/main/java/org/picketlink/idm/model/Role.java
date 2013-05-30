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

<<<<<<< HEAD
import org.picketlink.idm.model.annotation.AttributeProperty;

/**
 * Represents a role, which may be assigned to account objects in various ways
 * to grant specific application privileges
=======

import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.QueryParameter;

/**
 * <p>Default {@link IdentityType} implementation  to represent roles.</p>
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
 *
 * @author Shane Bryzak
 */
public class Role extends AbstractIdentityType {
<<<<<<< HEAD
    private static final long serialVersionUID = -9044601754527766512L;
=======

    private static final long serialVersionUID = 5641696145573437982L;

    /**
     * A query parameter used to set the name value.
     */
    public static final QueryParameter NAME = new QueryParameter() {};
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60

    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @AttributeProperty
    public String getName() {
<<<<<<< HEAD
        return name;
=======
        return this.name;
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    }

    public void setName(String name) {
        this.name = name;
    }
<<<<<<< HEAD

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
=======

>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
}
