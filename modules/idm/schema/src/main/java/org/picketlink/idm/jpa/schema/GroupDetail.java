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
package org.picketlink.idm.jpa.schema;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.picketlink.idm.jpa.annotations.AttributeOf;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.model.sample.Group;

/**
 * Contains group-specific state.  The @AttributeOf annotation specifies that this entity
 * contains state for the identity types specified by the supportedTypes value.
 *
 * @author Shane Bryzak
 */
@Entity
@AttributeOf(supportedTypes = {Group.class})
public class GroupDetail {

    @OwnerReference
    @Id
    @OneToOne
    private IdentityObject identity;

    @AttributeValue
    @ManyToOne
    private IdentityObject parent;

    @AttributeValue
    private String name;

    @AttributeValue
    private String groupPath;

    public IdentityObject getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityObject identity) {
        this.identity = identity;
    }

    public IdentityObject getParent() {
        return parent;
    }

    public void setParent(IdentityObject parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }
}
