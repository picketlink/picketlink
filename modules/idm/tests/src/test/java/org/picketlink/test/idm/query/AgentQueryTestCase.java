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

import java.util.List;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
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

    public AgentQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    protected T createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someSimpleAgent";
        }

        return (T) createAgent(name, partition);
    }

    @Override
    protected T createInstance(String name) {
        return (T) new Agent(name);
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.addToGroup(agentType, administratorGroup);
        relationshipManager.addToGroup(someAgent, administratorGroup);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(containsMembership(result, agentType));
        assertTrue(containsMembership(result, someAgent));

        relationshipManager.addToGroup(agentType, someGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(containsMembership(result, agentType));
    }

    @Test
    public void testFindByMultipleAgentWithRoles() throws Exception {
        T agentType = createIdentityType("admin", null);
        T someAgent = createIdentityType("someAgent", null);

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.grantRole(agentType, administratorRole);
        relationshipManager.grantRole(someAgent, administratorRole);

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        List<Grant> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(containsGrant(result, agentType));
        assertTrue(containsGrant(result, someAgent));

        relationshipManager.grantRole(agentType, someRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);
        query.setParameter(Grant.ROLE, someRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(containsGrant(result, agentType));
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group} and {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testFindBySingleGroupRole() throws Exception {
        T agentType = createIdentityType("someUser", null);
        Group salesGroup = createGroup("Sales", null);
        Role managerRole = createRole("Manager");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        RelationshipQuery<GroupRole> query = relationshipManager.createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.ROLE, managerRole);
        query.setParameter(GroupRole.ASSIGNEE, agentType);
        query.setParameter(GroupRole.GROUP, salesGroup);

        List<GroupRole> result = query.getResultList();

        assertTrue(result.isEmpty());

        relationshipManager.grantGroupRole(agentType, managerRole, salesGroup);

        query = relationshipManager.createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.ROLE, managerRole);
        query.setParameter(GroupRole.ASSIGNEE, agentType);
        query.setParameter(GroupRole.GROUP, salesGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getAssignee().getId());
        assertEquals(managerRole.getId(), result.get(0).getRole().getId());
        assertEquals(salesGroup.getId(), result.get(0).getGroup().getId());
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);

        List<GroupMembership> result = query.getResultList();

        assertTrue(result.isEmpty());

        relationshipManager.addToGroup(agentType, administratorGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getMember().getId());
        assertEquals(administratorGroup.getId(), result.get(0).getGroup().getId());
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        List<Grant> result = query.getResultList();

        assertTrue(result.isEmpty());

        relationshipManager.grantRole(agentType, administratorRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getAssignee().getId());
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.addToGroup(agentType, administratorGroup);
        relationshipManager.addToGroup(agentType, someGroup);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);
        query.setParameter(GroupMembership.GROUP, someGroup);

//        visitor.commit();

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getMember().getId());

        relationshipManager.removeFromGroup(agentType, someGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);
        query.setParameter(GroupMembership.GROUP, someGroup);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getMember().getId());
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.grantRole(agentType, administratorRole);
        relationshipManager.grantRole(agentType, someRole);

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        List<Grant> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getAssignee().getId());

        relationshipManager.revokeRole(agentType, someRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);
        query.setParameter(Grant.ROLE, someRole);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getAssignee().getId());
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.addToGroup(agentType, administratorGroup);
        relationshipManager.addToGroup(someAgent, administratorGroup);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsMembership(result, agentType));
        assertTrue(containsMembership(result, someAgent));

        relationshipManager.addToGroup(agentType, someGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.GROUP, administratorGroup, someGroup);
        query.setParameter(GroupMembership.GROUP, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsMembership(result, agentType));
        assertFalse(containsMembership(result, someAgent));
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
        T someAgent = createIdentityType("someUser", null);

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.grantRole(agentType, administratorRole);
        relationshipManager.grantRole(someAgent, administratorRole);

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);

        List<Grant> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsGrant(result, agentType));
        assertTrue(containsGrant(result, someAgent));

        relationshipManager.grantRole(agentType, someRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ROLE, administratorRole);
        query.setParameter(Grant.ROLE, someRole);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsGrant(result, agentType));
        assertFalse(containsGrant(result, someAgent));
    }

    @Test
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testFindByLoginNameAndCreationDateWithSorting() throws Exception {
        createAgent("john");
        // Sleep is needed to avoid same createdDate
        Thread.sleep(1000);
        createAgent("demo");
        Thread.sleep(1000);
        createAgent("root");
        Thread.sleep(1000);
        Agent mary = createAgent("mary");
        mary.setEnabled(false);
        getIdentityManager().update(mary);

        // Default sorting (loginName)
        IdentityQuery<Agent> agentQuery = getIdentityManager().createIdentityQuery(Agent.class);

        agentQuery.setSortParameters(Agent.LOGIN_NAME);

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

    @Override
    @Ignore
    @Test
    public void testFindByTier() throws Exception {

    }
}
