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

import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.RelationshipQueryParameter;

/**
 * A Relationship that represents an identity's membership in a Group
 * 
 * @author Shane Bryzak
 */
public class GroupMembership extends AbstractAttributedType implements Relationship {
    
    public static final RelationshipQueryParameter MEMBER = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "member";
        }
    };;

    public static final RelationshipQueryParameter GROUP = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "group";
        }
    };;

    private static final long serialVersionUID = 6851576454138812116L;

    private Agent member;
    private Group group;

    public GroupMembership() {
        
    }
    
    public GroupMembership(Agent member, Group group) {
        this.member = member;
        this.group = group;
    }

    @RelationshipIdentity
    public Agent getMember() {
        return member;
    }
    
    public void setMember(Agent member) {
        this.member = member;
    }

    @RelationshipIdentity
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
}
