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
package org.picketlink.test.idm;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Base class for test cases using a specific {@link IdentityManager} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AbstractIdentityManagerTestCase {

    private IdentityManager identityManager;

    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    /**
     * <p>
     * Loads or creates a new {@link Group} instance depending on the <code>alwaysCreate</code> argument value. If this argument
     * is true and the provided {@link User} exists it will be re-created (remove/add). Otherwise the stored instance is always
     * returned.
     * </p>
     *
     * @param userName
     * @param alwaysCreate
     * @return
     */
    protected User loadOrCreateUser(String userName, boolean alwaysCreate) {
        User user = getIdentityManager().getUser(userName);

        if (user != null && alwaysCreate) {
            getIdentityManager().remove(user);
            user = null;
        }

        if (user == null) {
            user = new SimpleUser(userName);
            getIdentityManager().add(user);
        }

        return getIdentityManager().getUser(userName);
    }
    
    /**
     * <p>
     * Loads or creates a new {@link Group} instance depending on the <code>alwaysCreate</code> argument value. If this argument
     * is true and the provided {@link User} exists it will be re-created (remove/add). Otherwise the stored instance is always
     * returned.
     * </p>
     *
     * @param id
     * @param alwaysCreate
     * @return
     */
    protected Agent loadOrCreateAgent(String id, boolean alwaysCreate) {
        Agent agent = getIdentityManager().getAgent(id);

        if (agent != null && alwaysCreate) {
            getIdentityManager().remove(agent);
            agent = null;
        }

        if (agent == null) {
            agent = new SimpleAgent(id);
            getIdentityManager().add(agent);
        }

        return getIdentityManager().getAgent(id);
    }

    /**
     * <p>
     * Loads or creates a new {@link Role} instance depending on the <code>alwaysCreate</code> argument value. If this argument
     * is true and the provided {@link Role} exists it will be re-created (remove/add). Otherwise the stored instance is always
     * returned.
     * </p>
     *
     * @param userName
     * @param alwaysCreate
     * @return
     */
    protected Role loadOrCreateRole(String name, boolean alwaysCreate) {
        Role role = getIdentityManager().getRole(name);

        if (role != null && alwaysCreate) {
            getIdentityManager().remove(role);
            role = null;
        }

        if (role == null) {
            role = new SimpleRole(name);
            getIdentityManager().add(role);
        }

        return getIdentityManager().getRole(name);
    }

    /**
     * <p>
     * Loads or creates a new {@link Group} instance depending on the <code>alwaysCreate</code> argument value. If this argument
     * is true and the provided {@link Group} exists it will be re-created (remove/add). Otherwise the stored instance is always
     * returned.
     * </p>
     *
     * @param userName
     * @param alwaysCreate
     * @return
     */
    protected Group loadOrCreateGroup(String name, String parentGroupName, boolean alwaysCreate) {
        Group parentGroup = getIdentityManager().getGroup(parentGroupName);

        if (parentGroup != null && parentGroupName != null && alwaysCreate) {
            getIdentityManager().remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new SimpleGroup(parentGroupName);
            getIdentityManager().add(parentGroup);
        }

        Group group = getIdentityManager().getGroup(name);

        if (group != null && alwaysCreate) {
            getIdentityManager().remove(group);
            group = null;
        }

        if (group == null) {
            if (parentGroupName == null) {
                group = new SimpleGroup(name);
            } else {
                group = new SimpleGroup(name, parentGroup);
            }

            getIdentityManager().add(group);
        }

        return getIdentityManager().getGroup(name);
    }

}