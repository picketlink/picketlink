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
 * GroupRole is a Relationship type that assigns a role within a group to an identity (either a User or Group).
 * 
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public class GroupRole extends GroupMembership implements Relationship {

    private static final long serialVersionUID = 2844617870858266637L;

    public static final RelationshipQueryParameter ROLE = new RelationshipQueryParameter() {
        
        @Override
        public String getName() {
            return "role";
        }
    };;


    private Role role;

    public GroupRole() {
        super();
    }
    
    public GroupRole(Agent member, Group group, Role role) {
        super(member, group);
        this.role = role;
    }

    @RelationshipIdentity
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
