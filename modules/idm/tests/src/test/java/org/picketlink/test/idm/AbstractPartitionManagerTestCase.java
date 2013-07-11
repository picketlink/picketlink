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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import static org.junit.runners.Parameterized.Parameters;
import static org.picketlink.test.idm.IdentityConfigurationTestFactory.getConfigurations;

/**
 * <p>
 * Base class for test cases using a specific {@link PartitionManager} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@RunWith(Parameterized.class)
public abstract class AbstractPartitionManagerTestCase {

    private final IdentityConfigurationTestVisitor visitor;
    private PartitionManager partitionManager;

    public AbstractPartitionManagerTestCase(IdentityConfigurationTestVisitor visitor) {
        this.visitor = visitor;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<Object[]>();

        parameters.add(getConfigurations());

        return parameters;
    }

    @Before
    public void onBefore() {
        this.visitor.beforeTest();
    }

    @After
    public void afterBefore() {
        this.visitor.afterTest();
    }

    public PartitionManager getPartitionManager() {
        if (this.partitionManager == null) {
            this.partitionManager = this.visitor.buildConfiguration();
        }

        return this.partitionManager;
    }

    public IdentityManager getIdentityManager() {
        return getPartitionManager().createIdentityManager();
    }

    protected User createUser(String userName) {
        User user = getIdentityManager().getUser(userName);

        if (user != null) {
            getIdentityManager().remove(user);
        }

        user = new User(userName);
        getIdentityManager().add(user);

        return user;
    }

    protected User createUser(String userName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);

        User user = identityManager.getUser(userName);

        if (user != null) {
            identityManager.remove(user);
        }

        user = new User(userName);
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

        agent = new Agent(loginName);

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

        agent = new Agent(loginName);

        identityManager.add(agent);

        return agent;
    }

    private IdentityManager getIdentityManagerForPartition(Partition partition) {
        if (partition == null) {
            return getPartitionManager().createIdentityManager();
        } else {
            return getPartitionManager().createIdentityManager(partition);
        }
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

        role = new Role(name);
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

        role = new Role(name);
        identityManager.add(role);

        return role;
    }

    protected Role getRole(String name) {
        return getIdentityManager().getRole(name);
    }

    protected Group createGroupWithParent(String name, Group parentGroup) {
        String path = name;

        if (parentGroup != null) {
            path = parentGroup.getPath() + "/" + name;
        }

        Group group = getIdentityManager().getGroup(path);

        if (group != null) {
            getIdentityManager().remove(group);
            group = null;
        }

        if (group == null) {
            if (parentGroup == null) {
                group = new Group(name);
            } else {
                group = new Group(name, parentGroup);
            }

            getIdentityManager().add(group);
        }

        return group;
    }

    protected Group createGroup(String name, String parentGroupName) {
        Group parentGroup = getIdentityManager().getGroup(parentGroupName);

        String path = name;

        if (parentGroupName != null) {
            path = "/" + parentGroupName + "/" + name;

            if (parentGroup != null) {
                path = parentGroup.getPath() + "/" + name;
            }
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
            parentGroup = new Group(parentGroupName);
            getIdentityManager().add(parentGroup);
        }

        if (group == null) {
            if (parentGroupName == null) {
                group = new Group(name);
            } else {
                group = new Group(name, parentGroup);
            }

            getIdentityManager().add(group);
        }

        return group;
    }

    protected Group createGroup(String name) {
        return createGroup(name, null);
    }

    protected Group createGroup(String name, String parentGroupName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);
        Group parentGroup = identityManager.getGroup(parentGroupName);

        if (parentGroup != null && parentGroupName != null) {
            identityManager.remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new Group(parentGroupName);
            identityManager.add(parentGroup);
        }

        Group group = identityManager.getGroup(name);

        if (group != null) {
            identityManager.remove(group);
            group = null;
        }

        if (group == null) {
            if (parentGroupName == null) {
                group = new Group(name);
            } else {
                group = new Group(name, parentGroup);
            }

            identityManager.add(group);
        }

        return group;
    }

    protected Group getGroup(String name) {
        return getIdentityManager().getGroup(name);
    }

}