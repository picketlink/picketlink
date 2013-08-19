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
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.RelationshipQueryParameter;

/**
 * Represents the grant of a Role to an Assignee
 *
 * @author Shane Bryzak
 */
public class Grant extends AbstractAttributedType implements Relationship {
    private static final long serialVersionUID = -200089007240264375L;

    public static final RelationshipQueryParameter ASSIGNEE = new RelationshipQueryParameter() {

        @Override
        public String getName() {
            return "assignee";
        }
    };;

    public static final RelationshipQueryParameter ROLE = new RelationshipQueryParameter() {

        @Override
        public String getName() {
            return "role";
        }
    };;

    private IdentityType assignee;
    private Role role;

    public Grant() {

    }

    public Grant(IdentityType assignee, Role role) {
        this.assignee = assignee;
        this.role = role;
    }

    public IdentityType getAssignee() {
        return assignee;
    }

    public void setAssignee(IdentityType assignee) {
        this.assignee = assignee;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
