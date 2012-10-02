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
import java.util.Map;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * UserQuery. All applied conditions will be resolved with logical AND.
 */
public interface UserQuery {
    // TODO: Javadocs
    // TODO: Exceptions

    // TODO: add searchBy stuff that makes sense: email, first/last/full name, organization?

    // TODO: make clear comment in javadoc about usage of wildcards -
    // TODO: should support at least usage of '*' for all built in attributes mentioned above.

    // Operations

    UserQuery reset();

    UserQuery getImmutable();

    List<User> executeQuery();

    List<User> executeQuery(UserQuery query);

    // Conditions

    UserQuery setName(String name);

    String getName();

    UserQuery setRelatedGroup(Group group);

    UserQuery setRelatedGroup(String groupId);

    Group getRelatedGroup();

    UserQuery setRole(Role role);

    UserQuery setRole(String name);

    Role getRole();

    UserQuery setAttributeFilter(String name, String[] values);

    Map<String, String[]> getAttributeFilters();

    // Built in attributes

    UserQuery setFirstName(String firstName);

    String getFirstName();

    UserQuery setLastName(String lastName);

    String getLastName();

    UserQuery setEmail(String email);

    String getEmail();

    UserQuery setEnabled(boolean enabled);

    boolean getEnabled();

    // Pagination

    UserQuery sort(boolean ascending);

    void setRange(Range range);

    Range getRange();

}
