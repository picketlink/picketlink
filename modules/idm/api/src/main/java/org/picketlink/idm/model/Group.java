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
<<<<<<< HEAD

/**
 * Represents a Group, which may be used to form collections of other identity objects
=======
import org.picketlink.idm.query.QueryParameter;

/**
 * <p>Default {@link IdentityType} implementation  to represent groups.</p>
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
 *
 * @author Shane Bryzak
 */
public class Group extends AbstractIdentityType {

<<<<<<< HEAD
=======
    private static final long serialVersionUID = -3553832607918448916L;

    /**
     * A query parameter used to set the name value.
     */
    public static final QueryParameter NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the path.
     */
    public static final QueryParameter PATH = new QueryParameter() {};

    /**
     * A query parameter used to set the parent value.
     */
    public static final QueryParameter PARENT = new QueryParameter() {};

    public static final String PATH_SEPARATOR = "/";

>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    private String name;
    private Group parentGroup;
    private String path;

<<<<<<< HEAD
    public Group() {
    }

    public Group(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating SimpleGroup - name cannot be null or empty");
        }

        this.name = name;
        this.path = getPath(this);
=======
    public Group(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
        }

        this.name = name;
        this.path = buildPath(this);
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    }

    public Group(String name, Group parentGroup) {
        if (name == null || name.isEmpty()) {
<<<<<<< HEAD
            throw new IllegalArgumentException("Error creating SimpleGroup - name cannot be null or empty");
=======
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
        }

        this.name = name;
        this.parentGroup = parentGroup;
<<<<<<< HEAD
        this.path = getPath(this);
=======
        this.path = buildPath(this);
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    }

    @AttributeProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

<<<<<<< HEAD
=======
    @AttributeProperty
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

<<<<<<< HEAD
    private String getPath(Group group) {
        String name = "/" + group.getName();

        if (group.getParentGroup() != null) {
            name = getPath(group.getParentGroup()) + name;
        }

        return name;
    }

    @AttributeProperty
    public Group getParentGroup() {
        return parentGroup;
    }

=======
    @AttributeProperty
    public Group getParentGroup() {
        return this.parentGroup;
    }

    @AttributeProperty
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    public void setParentGroup(Group group) {
        this.parentGroup = group;
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

        if (!(obj instanceof Group)) {
            return false;
        }

        Group other = (Group) obj;

        // FIXME The Partition should also be taken into account
        return other.getId() != null && this.getId() != null && other.getId().equals(this.getId());
    }
=======
    /**
     * <p>Builds the group's path based on an parent group.</p>
     *
     * @param parentGroup
     * @return
     */
    private String buildPath(Group parentGroup) {
        String name = PATH_SEPARATOR + parentGroup.getName();

        if (parentGroup.getParentGroup() != null) {
            name = buildPath(parentGroup.getParentGroup()) + name;
        }
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60

        return name;
    }
}
