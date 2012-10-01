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
package org.picketlink.idm.query;

import java.util.List;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * MembershipQuery. All applied conditions will be resolved with logical AND.
 */
public interface MembershipQuery {
    // TODO: Javadocs
    // TODO: Exceptions

    // Operations

    MembershipQuery reset();

    MembershipQuery immutable();

    List<Membership> executeQuery();

    List<Membership> executeQuery(MembershipQuery query);

    // Conditions

    MembershipQuery setUser(User user);

    MembershipQuery setUser(String user);

    User getUser();

    MembershipQuery setGroup(Group group);

    MembershipQuery setGroup(String groupId);

    Group getGroup();

    MembershipQuery setRole(Role role);

    MembershipQuery setRole(String role);

    Role getRole();

    void setRange(Range range);

    Range getRange();

}
