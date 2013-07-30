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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.model.Statement;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
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
        User user = SampleModel.getUser(getIdentityManager(), userName);

        if (user != null) {
            getIdentityManager().remove(user);
        }

        user = new User(userName);
        getIdentityManager().add(user);

        return user;
    }

    protected User createUser(String userName, Partition partition) {
        IdentityManager identityManager = getIdentityManagerForPartition(partition);

        User user = SampleModel.getUser(identityManager, userName);

        if (user != null) {
            identityManager.remove(user);
        }

        user = new User(userName);
        identityManager.add(user);

        return user;
    }

    protected User getUser(String userName) {
        return SampleModel.getUser(getIdentityManager(), userName);
    }

    protected Agent createAgent(String loginName) {
        Agent agent = SampleModel.getAgent(getIdentityManager(), loginName);

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

        Agent agent = SampleModel.getAgent(identityManager, loginName);

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
        return SampleModel.getAgent(getIdentityManager(), loginName);
    }

    protected Role createRole(String name) {
        Role role = SampleModel.getRole(getIdentityManager(), name);

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

        Role role = SampleModel.getRole(identityManager, name);

        if (role != null) {
            identityManager.remove(role);
            role = null;
        }

        role = new Role(name);
        identityManager.add(role);

        return role;
    }

    protected Role getRole(String name) {
        return SampleModel.getRole(getIdentityManager(), name);
    }

    protected Group createGroupWithParent(String name, Group parentGroup) {
        String path = name;

        if (parentGroup != null) {
            path = parentGroup.getPath() + "/" + name;
        }

        Group group = SampleModel.getGroup(getIdentityManager(), path);

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
        Group parentGroup = SampleModel.getGroup(getIdentityManager(), parentGroupName);

        String path = name;

        if (parentGroupName != null) {
            path = "/" + parentGroupName + "/" + name;

            if (parentGroup != null) {
                path = parentGroup.getPath() + "/" + name;
            }
        }

        Group group = SampleModel.getGroup(getIdentityManager(), path);

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
        Group parentGroup = SampleModel.getGroup(identityManager, parentGroupName);

        if (parentGroup != null && parentGroupName != null) {
            identityManager.remove(parentGroup);
            parentGroup = null;
        }

        if (parentGroup == null && parentGroupName != null) {
            parentGroup = new Group(parentGroupName);
            identityManager.add(parentGroup);
        }

        Group group = SampleModel.getGroup(identityManager, name);

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
        return SampleModel.getGroup(getIdentityManager(), name);
    }

    protected Group getGroup(String name, Group parent) {
        return SampleModel.getGroup(getIdentityManager(), name, parent);
    }

    @Rule
    public TestRule watcher = new TestWatcher() {

        @Override
        public Statement apply(Statement base, Description description) {
            List<Class<? extends IdentityConfigurationTester>> ignoredList = new ArrayList<Class<? extends IdentityConfigurationTester>>();

            IgnoreTester ignoreTester = description.getAnnotation(IgnoreTester.class);

            if (ignoreTester != null) {
                ignoredList.addAll(Arrays.asList(ignoreTester.value()));
            }

            ignoreTester = description.getTestClass().getAnnotation(IgnoreTester.class);

            if (ignoreTester != null) {
                ignoredList.addAll(Arrays.asList(ignoreTester.value()));
            }

            for (Class<? extends IdentityConfigurationTester> testerType: ignoredList) {
                if (testerType.equals(visitor.getClass())) {
                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {

                        }
                    };
                }
            }

            return super.apply(base, description);
        }
    };

    public IdentityConfigurationTester getVisitor() {
        return visitor;
    }
}