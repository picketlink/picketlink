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
 * Represents a Group, which may be used to form collections of other identity objects
 *
 * @author Shane Bryzak
 */
public class Group extends AbstractIdentityType {

    private String name;
    private Group parentGroup;
    private String path;

    public Group() {
    }

    public Group(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating SimpleGroup - name cannot be null or empty");
        }

        this.name = name;
        this.path = getPath(this);
    }

    public Group(String name, Group parentGroup) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating SimpleGroup - name cannot be null or empty");
        }

        this.name = name;
        this.parentGroup = parentGroup;
        this.path = getPath(this);
    }

    @AttributeProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        return parentGroup;
    }

    public void setParentGroup(Group group) {
        this.parentGroup = group;
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

        if (!(obj instanceof Group)) {
            return false;
        }

        Group other = (Group) obj;

        // FIXME The Partition should also be taken into account
        return other.getId() != null && this.getId() != null && other.getId().equals(this.getId());
    }

}
