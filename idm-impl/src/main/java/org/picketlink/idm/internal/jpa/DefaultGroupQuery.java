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

package org.picketlink.idm.internal.jpa;

import java.util.List;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultGroupQuery extends AbstractQuery<DefaultGroupQuery> implements GroupQuery {

    private String id;
    private Group parentGroup;
    private Role role;
    private User relatedUser;
    private IdentityStore store;

    public DefaultGroupQuery(IdentityStore store) {
        this.store = store;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    @Override
    public GroupQuery setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return the parentGroup
     */
    public Group getParentGroup() {
        return parentGroup;
    }

    @Override
    public GroupQuery setParentGroup(Group parentGroup) {
        this.parentGroup = parentGroup;
        return this;
    }

    @Override
    public GroupQuery setParentGroup(String groupId) {
        this.parentGroup = new SimpleGroup(groupId, null, null);
        return this;
    }

    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    @Override
    public GroupQuery setRole(Role role) {
        this.role = role;
        return this;
    }

    /**
     * @return the relatedUser
     */
    public User getRelatedUser() {
        return relatedUser;
    }

    @Override
    public GroupQuery setRelatedUser(User relatedUser) {
        this.relatedUser = relatedUser;
        return this;
    }

    @Override
    public GroupQuery immutable() {
        return super.getImmutable();
    }

    @Override
    public List<Group> executeQuery(GroupQuery query) {
        return null;
    }

    @Override
    public List<Group> executeQuery() {
        return this.store.executeQuery(this, null);
    }

    @Override
    public GroupQuery addAttributeFilter(String name, String[] values) {
        return super.setAttributeFilter(name, values);
    }

    @Override
    public GroupQuery setRole(String role) {
        this.role = new SimpleRole(role);
        return this;
    }

    @Override
    public GroupQuery setRelatedUser(String user) {
        this.relatedUser = new SimpleUser(user);
        return this;
    }

}
