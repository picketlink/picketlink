/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.QueryParameter;

/**
 * GroupRole is a Relationship type that assigns a role within a group to an identity (either a User or Group).
 * 
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public class GroupRole extends GroupMembership implements Relationship {

    private static final long serialVersionUID = 2844617870858266637L;

    QueryParameter ROLE = new QueryParameter() {};

    private Role role;

    public GroupRole(IdentityType member, Group group, Role role) {
        super(member, group);
        this.role = role;
    }

    @RelationshipIdentity
    public Role getRole() {
        return role;
    }
}
