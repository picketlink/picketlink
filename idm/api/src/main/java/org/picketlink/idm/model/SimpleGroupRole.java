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

/**
 * Simple implementation of the {@link GroupRole} interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 * @since Sep 4, 2012
 */
public class SimpleGroupRole implements GroupRole {

    private static final long serialVersionUID = 2844617870858266637L;

    private IdentityType member;
    private Role role;
    private Group group;

    public SimpleGroupRole(IdentityType member, Role role, Group group) {
        if (member == null) {
            throw new IllegalStateException("Member may not be null.");
        }

        if (role == null) {
            throw new IllegalStateException("Role may not be null.");
        }

//        if (group == null) {
//            throw new IllegalStateException("Group may not be null.");
//        }

        this.member = member;
        this.role = role;
        this.group = group;
    }

    @Override
    public IdentityType getMember() {
        return member;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public Role getRole() {
        return role;
    }
}