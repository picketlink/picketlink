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
package org.picketlink.test.idm;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.Tier;
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
        if (this.identityManager == null) {
            throw new RuntimeException("Identity Manager is not set.");
        }
        return this.identityManager;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    protected User createUser(String userName) {
        User user = getIdentityManager().getUser(userName);

        if (user != null) {
            getIdentityManager().remove(user);
        }

        user = new SimpleUser(userName);
        getIdentityManager().add(user);

        return user;
    }
    
    protected User createUser(String userName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);
        
        User user = identityManager.getUser(userName);

        if (user != null) {
            identityManager.remove(user);
        }

        user = new SimpleUser(userName);
        identityManager.add(user);

        return user;
    }

    protected User getUser(String userName) {
        return getIdentityManager().getUser(userName);
    }

    protected Agent createAgent(String loginName) {
        Agent agent = getIdentityManager().getAgent(loginName);

        if (agent != null) {
            getIdentityManager().remove(agent);
            agent = null;
        }

        agent = new SimpleAgent(loginName);

        getIdentityManager().add(agent);

        return agent;
    }

    protected Agent createAgent(String loginName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);
        
        Agent agent = identityManager.getAgent(loginName);

        if (agent != null) {
            identityManager.remove(agent);
            agent = null;
        }

        agent = new SimpleAgent(loginName);

        identityManager.add(agent);

        return agent;
    }

    private IdentityManager getIdentityManagerForPartition(Partition partition) {
        IdentityManager identityManager = getIdentityManager();

        if (partition != null) {
            if (Realm.class.isInstance(partition)) {
                identityManager = identityManager.forRealm((Realm) partition);
            } else if (Tier.class.isInstance(partition)) {
                identityManager = identityManager.forTier((Tier) partition);
            } else {
                throw new IllegalArgumentException("Unexpected partition type.");
            }
        }
        return identityManager;
    }

    protected Agent getAgent(String loginName) {
        return getIdentityManager().getAgent(loginName);
    }

    protected Role createRole(String name) {
        Role role = getIdentityManager().getRole(name);

        if (role != null) {
            getIdentityManager().remove(role);
            role = null;
        }

        role = new SimpleRole(name);
        getIdentityManager().add(role);

        return role;
    }
    
    protected Role createRole(String name, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);
        
        Role role = identityManager.getRole(name);

        if (role != null) {
            identityManager.remove(role);
            role = null;
        }

        role = new SimpleRole(name);
        identityManager.add(role);

        return role;
    }

    protected Role getRole(String name) {
        return getIdentityManager().getRole(name);
    }

    protected Group createGroup(String name, String parentGroupName) {
        Group parentGroup = getIdentityManager().getGroup(parentGroupName);

        String path = name;
        
        if (parentGroupName != null) {
            path = "/" + parentGroupName + "/" + name;
        }

        Group group = getIdentityManager().getGroup(path);

        if (group != null) {
            getIdentityManager().remove(group);
            group = null;
        }

        if (parentGroup != null) {
            getIdentityManager().remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new SimpleGroup(parentGroupName);
            getIdentityManager().add(parentGroup);
        }
        
        if (group == null) {
            if (parentGroupName == null) {
                group = new SimpleGroup(name);
            } else {
                group = new SimpleGroup(name, parentGroup);
            }

            getIdentityManager().add(group);
        }

        return group;
    }
    
    protected Group createGroup(String name, String parentGroupName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);
        Group parentGroup = identityManager.getGroup(parentGroupName);

        if (parentGroup != null && parentGroupName != null) {
            identityManager.remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new SimpleGroup(parentGroupName);
            identityManager.add(parentGroup);
        }

        Group group = identityManager.getGroup(name);

        if (group != null) {
            identityManager.remove(group);
            group = null;
        }

        if (group == null) {
            if (parentGroupName == null) {
                group = new SimpleGroup(name);
            } else {
                group = new SimpleGroup(name, parentGroup);
            }

            identityManager.add(group);
        }

        return group;
    }

    protected Group getGroup(String name) {
        return getIdentityManager().getGroup(name);
    }

}