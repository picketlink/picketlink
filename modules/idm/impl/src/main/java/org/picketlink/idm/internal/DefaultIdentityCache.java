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

package org.picketlink.idm.internal;

import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Default {@link IdentityCache} implementation.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultIdentityCache implements IdentityCache {

    private Map<Partition, Map<String, Agent>> agentsCache = new HashMap<Partition, Map<String, Agent>>();
    private Map<Partition, Map<String, Role>> rolesCache = new HashMap<Partition, Map<String, Role>>();
    private Map<Partition, Map<String, Group>> groupsCache = new HashMap<Partition, Map<String, Group>>();

    @Override
    public User lookupUser(Realm realm, String loginName) {
        Agent agent = lookupAgent(realm, loginName);

        if (User.class.isInstance(agent)) {
            return (User) agent;
        }

        return null;
    }

    @Override
    public Group lookupGroup(Partition partition, String groupPath) {
        return getGroups(partition).get(groupPath);
    }

    @Override
    public Role lookupRole(Partition partition, String name) {
        return getRoles(partition).get(name);
    }

    @Override
    public void putUser(Realm realm, User user) {
        putAgent(realm, user);
    }

    @Override
    public void putGroup(Partition partition, Group group) {
        getGroups(partition).get(group.getPath());
    }

    @Override
    public void putRole(Partition partition, Role role) {
        getRoles(partition).put(role.getName(), role);
    }

    @Override
    public Agent lookupAgent(Realm realm, String loginName) {
        return getAgents(realm).get(loginName);
    }

    @Override
    public void putAgent(Realm realm, Agent agent) {
        getAgents(realm).get(agent.getLoginName());
    }

    @Override
    public void invalidate(Partition partition, IdentityType identityType) {
        if (Agent.class.isInstance(identityType)) {
            Agent agent = (Agent) identityType;
            getAgents((Realm) partition).remove(agent.getLoginName());
        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;
            getRoles(partition).remove(role.getName());
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;
            getGroups(partition).remove(group.getPath());
        }
    }

    private Map<String, Agent> getAgents(Realm realm) {
        Map<String, Agent> agents = this.agentsCache.get(realm);

        if (agents == null) {
            agents = new HashMap<String, Agent>();
            this.agentsCache.put(realm, agents);
        }

        return agents;
    }

    private Map<String, Role> getRoles(Partition partition) {
        Map<String, Role> roles = this.rolesCache.get(partition);

        if (roles == null) {
            roles = new HashMap<String, Role>();
            this.rolesCache.put(partition, roles);
        }

        return roles;
    }

    private Map<String, Group> getGroups(Partition partition) {
        Map<String, Group> groups = this.groupsCache.get(partition);

        if (groups == null) {
            groups = new HashMap<String, Group>();
            this.groupsCache.put(partition, groups);
        }

        return groups;
    }


}