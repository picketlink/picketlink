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

package org.picketlink.test.idm.query;

<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreWithoutAttributesTestSuite;
import org.picketlink.test.idm.suites.LDAPJPAMixedStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPUsersJPARolesGroupsFileRelationshipTestSuite;
import org.picketlink.test.idm.suites.LDAPUsersJPARolesGroupsRelationshipsTestSuite;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Agent} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AgentQueryTestCase<T extends Agent> extends AbstractIdentityQueryTestCase<T> {

    protected T createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someSimpleAgent";
        }

        return (T) createAgent(name, partition);
    }

    @Override
    protected T getIdentityType() {
        return (T) getIdentityManager().getAgent("someSimpleAgent");
    }

    @After
    public void onFinish() {
        T agentType = createIdentityType(null, null);

        IdentityQuery<T> query = getIdentityManager().createIdentityQuery((Class<T>) agentType.getClass());

        List<T> result = query.getResultList();

        for (T agent : result) {
            getIdentityManager().remove(agent);
        }
    }

    @Test
    public void testFindByLoginName() throws Exception {
        T agentType = createIdentityType(null, null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.<T> createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.LOGIN_NAME, agentType.getLoginName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    @Test
    public void testFindByMultipleAgentWithGroups() throws Exception {
        T agentType = createIdentityType("admin", null);
        T someAgent = createIdentityType("someAgent", null);

        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(agentType, administratorGroup);
        identityManager.addToGroup(someAgent, administratorGroup);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.MEMBER_OF, administratorGroup);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.addToGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.MEMBER_OF, administratorGroup, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, agentType.getId()));
    }

    @Test
    public void testFindByMultipleAgentWithRoles() throws Exception {
        T agentType = createIdentityType("admin", null);
        T someAgent = createIdentityType("someAgent", null);

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(agentType, administratorRole);
        identityManager.grantRole(someAgent, administratorRole);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.HAS_ROLE, administratorRole);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.grantRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.HAS_ROLE, administratorRole, someRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, agentType.getId()));
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group} and {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroupRole() throws Exception {
        T agentType = createIdentityType("someUser", null);
        Group salesGroup = createGroup("Sales", null);
        Role managerRole = createRole("Manager");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_GROUP_ROLE, new GroupRole(agentType, salesGroup, managerRole));

        List<T> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantGroupRole(agentType, managerRole, salesGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_GROUP_ROLE, new GroupRole(agentType, salesGroup, managerRole));

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroup() throws Exception {
        T agentType = createIdentityType("admin", null);
        Group administratorGroup = createGroup("Administrators", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup);

        List<T> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.addToGroup(agentType, administratorGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleRole() throws Exception {
        T agentType = createIdentityType("admin", null);
        Role administratorRole = createRole("Administrators");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole);

        List<T> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantRole(agentType, administratorRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleGroups() throws Exception {
        T agentType = createIdentityType("admin", null);
        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(agentType, administratorGroup);
        identityManager.addToGroup(agentType, someGroup);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup, someGroup);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());

        identityManager.removeFromGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup, someGroup);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleRoles() throws Exception {
        T agentType = createIdentityType("admin", null);
        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(agentType, administratorRole);
        identityManager.grantRole(agentType, someRole);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());

        identityManager.revokeRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole, someRole);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleUserWithGroups() throws Exception {
        T agentType = createIdentityType("admin", null);
        T someAgent = createIdentityType("someUser", null);

        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(agentType, administratorGroup);
        identityManager.addToGroup(someAgent, administratorGroup);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.addToGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));

        assertFalse(contains(result, someAgent.getId()));
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleUserWithRoles() throws Exception {
        T agentType = createIdentityType("admin", null);
        T someagent = createIdentityType("someUser", null);

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(agentType, administratorRole);
        identityManager.grantRole(someagent, administratorRole);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someagent.getId()));

        identityManager.grantRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole, someRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertFalse(contains(result, someagent.getId()));
    }

    @Test
    @ExcludeTestSuite({ LDAPIdentityStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class,
            LDAPJPAMixedStoreTestSuite.class, LDAPUsersJPARolesGroupsRelationshipsTestSuite.class,
            LDAPUsersJPARolesGroupsRelationshipsTestSuite.class, LDAPUsersJPARolesGroupsFileRelationshipTestSuite.class })
    public void testFindByLoginNameAndCreationDateWithSorting() throws Exception {
        createAgent("john");
        // Sleep is needed to avoid same createdDate
        IDMUtil.sleep(1000);
        createAgent("demo");
        IDMUtil.sleep(1000);
        createAgent("root");
        IDMUtil.sleep(1000);
        Agent mary = createAgent("mary");
        mary.setEnabled(false);
        getIdentityManager().update(mary);

        // Default sorting (loginName)
        IdentityQuery<Agent> agentQuery = getIdentityManager().createIdentityQuery(Agent.class);
        List<Agent> agents = agentQuery.getResultList();

        assertEquals(4, agents.size());
        assertEquals(agents.get(0).getLoginName(), "demo");
        assertEquals(agents.get(1).getLoginName(), "john");
        assertEquals(agents.get(2).getLoginName(), "mary");
        assertEquals(agents.get(3).getLoginName(), "root");

        // Descending sorting by enablement and creationDate
        agentQuery = getIdentityManager().createIdentityQuery(Agent.class);
        agentQuery.setSortAscending(false);
        agentQuery.setSortParameters(IdentityType.ENABLED, IdentityType.CREATED_DATE);
        agents = agentQuery.getResultList();

        assertEquals(4, agents.size());
        assertEquals(agents.get(0).getLoginName(), "root");
        assertEquals(agents.get(1).getLoginName(), "demo");
        assertEquals(agents.get(2).getLoginName(), "john");
        assertEquals(agents.get(3).getLoginName(), "mary");
    }
}
