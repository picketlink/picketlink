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
package org.picketlink.idm.model.sample;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.QueryParameter;

/**
 * Represents a Group, which may be used to form collections of other identity objects
 *
 * @author Shane Bryzak
 */
public class Group extends AbstractIdentityType {

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

    private String name;
    private Group parentGroup;
    private String path;


    public Group(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
        }

        this.name = name;
        this.path = buildPath(this);
    }

    public Group(String name, Group parentGroup) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
        }

        this.name = name;
        this.parentGroup = parentGroup;

        this.path = buildPath(this);
    }

    @AttributeProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @AttributeProperty
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String getPath(Group group) {
        String name = "/" + group.getName();

        if (group.getParentGroup() != null) {
            name = getPath(group.getParentGroup()) + name;
        }

        return name;
    }

    @AttributeProperty
    public Group getParentGroup() {
        return this.parentGroup;
    }

    @AttributeProperty
    public void setParentGroup(Group group) {
        this.parentGroup = group;
    }

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

        return name;
    }
}
