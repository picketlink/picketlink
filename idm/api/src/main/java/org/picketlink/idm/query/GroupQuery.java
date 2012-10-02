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
 * GroupQuery. All applied conditions will be resolved with logical AND.
 */
public interface GroupQuery {
    // TODO: Javadocs
    // TODO: Exceptions

    // Operations

    GroupQuery reset();

    GroupQuery immutable();

    List<Group> executeQuery(GroupQuery query);

    List<Group> executeQuery();

    // Conditions

    GroupQuery setName(String name);

    String getName();

    GroupQuery setId(String id);

    String getId();

    GroupQuery setParentGroup(Group group);

    GroupQuery setParentGroup(String groupId);

    Group getParentGroup();

    GroupQuery setRole(Role role);

    GroupQuery setRole(String role);

    Role getRole();

    GroupQuery setRelatedUser(User user);

    GroupQuery setRelatedUser(String user);

    User getRelatedUser();

    GroupQuery addAttributeFilter(String name, String[] values);

    Map<String, String[]> getAttributeFilters();

    GroupQuery sort(boolean ascending);

    void setRange(Range range);

    Range getRange();

}
