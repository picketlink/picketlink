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
package org.picketlink.idm.model.basic;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.model.annotation.Unique;
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
    public static final QueryParameter NAME = QUERY_ATTRIBUTE.byName("name");

    /**
     * A query parameter used to set the path.
     */
    public static final QueryParameter PATH = QUERY_ATTRIBUTE.byName("path");

    /**
     * A query parameter used to set the parent value.
     */
    public static final QueryParameter PARENT = QUERY_ATTRIBUTE.byName("parentGroup");

    public static final String PATH_SEPARATOR = "/";

    private String name;
    private Group parentGroup;
    private String path;

    public Group() {

    }

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
    @Unique
    public String getPath() {
        this.path = buildPath(this);
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @InheritsPrivileges
    @AttributeProperty
    public Group getParentGroup() {
        return this.parentGroup;
    }

    @AttributeProperty
    public void setParentGroup(Group group) {
        this.parentGroup = group;
    }

    /**
     * <p>Builds the group's path based on the parent groups.</p>
     *
     * @param group
     * @return
     */
    private String buildPath(Group group) {
        String name = PATH_SEPARATOR + group.getName();

        if (group.getParentGroup() != null) {
            name = buildPath(group.getParentGroup()) + name;
        }

        return name;
    }
}
