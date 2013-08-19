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

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.runners.Parameterized.*;
import static org.picketlink.test.idm.IdentityConfigurationTestFactory.*;

/**
 * <p>
 * Base class for test cases using a specific {@link PartitionManager} instance.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@RunWith(ParameterizedRunner.class)
public abstract class AbstractPartitionManagerTestCase {

    protected final IdentityConfigurationTester visitor;
    private PartitionManager partitionManager;

    public AbstractPartitionManagerTestCase(IdentityConfigurationTester visitor) {
        this.visitor = visitor;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<Object[]>();

        for (IdentityConfigurationTester tester: getConfigurations()) {
            parameters.add(new Object[] {tester});
        }

        return parameters;
    }

    @Before
    public void onBefore() {
        this.visitor.beforeTest();
        this.partitionManager = this.visitor.getPartitionManager();
    }

    @After
    public void onAfter() {
        this.visitor.afterTest();
    }

    public PartitionManager getPartitionManager() {
        return this.partitionManager;
    }

    public IdentityManager getIdentityManager() {
        return getPartitionManager().createIdentityManager();
    }

    protected User createUser(String userName) {
        User user = BasicModel.getUser(getIdentityManager(), userName);

        if (user != null) {
            getIdentityManager().remove(user);
        }

        user = new User(userName);
        getIdentityManager().add(user);

        return user;
    }

    protected User createUser(String userName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);

        User user = BasicModel.getUser(identityManager, userName);

        if (user != null) {
            identityManager.remove(user);
        }

        user = new User(userName);
        identityManager.add(user);

        return user;
    }

    protected User getUser(String userName) {
        return BasicModel.getUser(getIdentityManager(), userName);
    }

    protected Agent createAgent(String loginName) {
        Agent agent = BasicModel.getAgent(getIdentityManager(), loginName);

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

        Agent agent = BasicModel.getAgent(identityManager, loginName);

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
        return BasicModel.getAgent(getIdentityManager(), loginName);
    }

    protected Role createRole(String name) {
        Role role = BasicModel.getRole(getIdentityManager(), name);

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

        Role role = BasicModel.getRole(identityManager, name);

        if (role != null) {
            identityManager.remove(role);
            role = null;
        }

        role = new Role(name);
        identityManager.add(role);

        return role;
    }

    protected Role getRole(String name) {
        return BasicModel.getRole(getIdentityManager(), name);
    }

    protected Group createGroupWithParent(String name, Group parentGroup) {
        String path = name;

        if (parentGroup != null) {
            path = parentGroup.getPath() + "/" + name;
        }

        Group group = BasicModel.getGroup(getIdentityManager(), path);

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
        Group parentGroup = BasicModel.getGroup(getIdentityManager(), parentGroupName);

        String path = name;

        if (parentGroupName != null) {
            path = "/" + parentGroupName + "/" + name;

            if (parentGroup != null) {
                path = parentGroup.getPath() + "/" + name;
            }
        }

        Group group = BasicModel.getGroup(getIdentityManager(), path);

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
        Group parentGroup = BasicModel.getGroup(identityManager, parentGroupName);

        if (parentGroup != null && parentGroupName != null) {
            identityManager.remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new Group(parentGroupName);
            identityManager.add(parentGroup);
        }

        Group group = BasicModel.getGroup(identityManager, name);

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
        return BasicModel.getGroup(getIdentityManager(), name);
    }

    protected Group getGroup(String name, Group parent) {
        return BasicModel.getGroup(getIdentityManager(), name, parent);
    }

    public IdentityConfigurationTester getVisitor() {
        return visitor;
    }
}