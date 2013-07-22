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
package org.picketlink.idm.model.sample;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

import java.util.List;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.query.IdentityQuery;

/**
 * This class provides a number of static convenience methods for looking up identities from the sample
 * identity model.
 *
 * @author Shane Bryzak
 */
public class IdentityLocator {

    /**
     * <p>
     * Returns an {@link Agent} with the given <code>loginName</code>. {@link User} are also agents, so if the
     * <code>loginName</code> maps to the an {@link User} it will be returned.
     * </p>
     *
     * @param loginName
     * @throws IdentityManagementException If cannot retrieve the {@link Agent}.
     */
    public static Agent getAgent(IdentityManager identityManager, String loginName) throws IdentityManagementException {
        if (isNullOrEmpty(loginName)) {
            return null;
        }

        List<Agent> agents = identityManager.createIdentityQuery(Agent.class)
                .setParameter(Agent.LOGIN_NAME, loginName).getResultList();
        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same login name");
        }
    }

    /**
     * <p>
     * Returns an {@link User} with the given <code>loginName</code>.
     * </p>
     *
     * @param loginName
     * @return If there is no {@link User} with the given <code>loginName</code> this method returns null.
     */
    public static User getUser(IdentityManager identityManager, String loginName) {
        if (isNullOrEmpty(loginName)) {
            return null;
        }

        List<User> agents = identityManager.createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, loginName).getResultList();

        if (agents.isEmpty()) {
            return null;
        } else if (agents.size() == 1) {
            return agents.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Agent objects found with same login name");
        }
    }

    /**
     * <p>
     * Returns an {@link Role} with the given <code>name</code>.
     * </p>
     *
     * @param loginName
     * @return If there is no {@link Role} with the given <code>name</code> this method returns null.
     */
    public static Role getRole(IdentityManager identityManager, String name) {
        if (isNullOrEmpty(name)) {
            return null;
        }

        List<Role> roles = identityManager.createIdentityQuery(Role.class).setParameter(Role.NAME, name).getResultList();
        if (roles.isEmpty()) {
            return null;
        } else if (roles.size() == 1) {
            return roles.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Role objects found with same name");
        }
    }

    /**
     * <p>
     * Returns the {@link Group} with the specified <code>groupPath</code>. Eg.: /groupA/groupB/groupC.
     * </p>
     * <p>
     * You can also provide the name only. In this case, the group returned will be the root group. Eg.: /Administrators.
     * </p>
     *
     * @param groupPath
     * @return if there is no {@link Group} with the given <code>groupPath</code> this method returns null.
     */
    public static Group getGroup(IdentityManager identityManager, String groupPath) {
        if (isNullOrEmpty(groupPath)) {
            return null;
        }

        if (!groupPath.startsWith("/")) {
            groupPath = "/" + groupPath;
        }

        String[] paths = groupPath.split("/");

        if (paths.length > 0) {
            String name = paths[paths.length - 1];

            IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

            query.setParameter(Group.NAME, name);

            List<Group> result = query.getResultList();

            for (Group group : result) {
                if (group.getPath().equals(groupPath)) {
                    return group;
                }
            }
        }

        return null;
    }

    /**
     * <p>
     * Returns the {@link Group} with the given name and child of the given parent {@link Group}.
     * </p>
     *
     * @param groupName
     * @param parent Must be a {@link Group} instance with a valid identifier.
     * @return if there is no {@link Group} this method returns null.
     */
    public static Group getGroup(IdentityManager identityManager, String groupName, Group parent) {
        if (groupName == null || parent == null) {
            return null;
        }

        return getGroup(identityManager, new Group(groupName, parent).getPath());
    }
}
