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

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.model.annotation.RelationshipStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.query.RelationshipQueryParameter;

import static org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype.GROUP_MEMBERSHIP;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_GROUP;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER;

/**
 * A Relationship that represents an identity's membership in a Group
 *
 * @author Shane Bryzak
 */
@RelationshipStereotype(GROUP_MEMBERSHIP)
public class GroupMembership extends AbstractAttributedType implements Relationship {

    public static final RelationshipQueryParameter MEMBER = RELATIONSHIP_QUERY_ATTRIBUTE.byName("member");
    public static final RelationshipQueryParameter GROUP = RELATIONSHIP_QUERY_ATTRIBUTE.byName("group");

    private static final long serialVersionUID = 6851576454138812116L;

    private Account member;
    private Group group;

    public GroupMembership() {

    }

    public GroupMembership(Account member, Group group) {
        this.member = member;
        this.group = group;
    }

    @InheritsPrivileges("group")
    @StereotypeProperty(RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER)
    public Account getMember() {
        return member;
    }

    public void setMember(Account member) {
        this.member = member;
    }

    @StereotypeProperty(RELATIONSHIP_GROUP_MEMBERSHIP_GROUP)
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
