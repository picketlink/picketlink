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
package org.picketlink.idm.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.Range;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * An implementation of {@link UserQuery} that delegates to the store
 *
 * @author anil saldhana
 * @since Sep 13, 2012
 */
public class DefaultUserQuery implements UserQuery {

    protected IdentityStore store = null;
    private String name;
    private Group relatedGroup;
    private String relatedGroupID;
    private Role role;
    private String roleName;

    private Map<String, String[]> filters = new HashMap<String, String[]>();
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled = true;
    private Range range;

    public DefaultUserQuery(IdentityStore store) {
        this.store = store;
    }

    @Override
    public UserQuery reset() {
        return null;
    }

    @Override
    public UserQuery getImmutable() {
        return null;
    }

    @Override
    public List<User> executeQuery(UserQuery query) {
        return null;
    }

    @Override
    public UserQuery setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UserQuery setRelatedGroup(Group group) {
        this.relatedGroup = group;
        return this;
    }

    @Override
    public UserQuery setRelatedGroup(String groupId) {
        this.relatedGroupID = groupId;
        return this;
    }

    @Override
    public Group getRelatedGroup() {
        if (this.relatedGroup != null) {
            return relatedGroup;
        }

        if (this.relatedGroupID == null) {
            return null;
        }

        return new SimpleGroup(null, this.relatedGroupID, null);
    }

    @Override
    public UserQuery setRole(Role role) {
        this.role = role;
        return this;
    }

    @Override
    public UserQuery setRole(String name) {
        this.roleName = name;
        return this;
    }

    @Override
    public Role getRole() {
        if (this.role != null) {
            return this.role;
        }

        if (this.roleName == null) {
            return null;
        }

        return new SimpleRole(this.roleName);
    }

    @Override
    public UserQuery setAttributeFilter(String name, String[] values) {
        filters.put(name, values);
        return this;
    }

    @Override
    public Map<String, String[]> getAttributeFilters() {
        return Collections.unmodifiableMap(filters);
    }

    @Override
    public UserQuery setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public UserQuery setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public UserQuery setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public UserQuery setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean getEnabled() {
        return enabled;
    }

    @Override
    public UserQuery sort(boolean ascending) {
        return null;
    }

    @Override
    public void setRange(Range range) {
        this.range = range;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public List<User> executeQuery() {
        return store.executeQuery(this, null);
    }
}
