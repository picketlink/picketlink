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
package org.picketlink.test.idm.other.shane.model.scenario1.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.test.idm.other.shane.model.scenario1.Group;

/**
 * Contains group-specific attribute values
 *
 * @author Shane Bryzak
 */
@Entity
@IdentityManaged({Group.class})
public class GroupDetail implements Serializable {
    private static final long serialVersionUID = 5125034753443642890L;

    @Id @OneToOne private IdentityObject identity;
    @AttributeValue(name = "name") private String groupName;
    @ManyToOne private IdentityObject parentGroup;

    public IdentityObject getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityObject identity) {
        this.identity = identity;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public IdentityObject getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(IdentityObject parentGroup) {
        this.parentGroup = parentGroup;
    }
}
