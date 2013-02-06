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

package org.picketlink.test.idm.query;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.FileIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;

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
            name = "someAgent";
        }
        
        return (T) createAgent(name, partition);
    }
    
    @Override
    protected T getIdentityType() {
        return (T) getIdentityManager().getAgent("someAgent");
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
        T agentType = createIdentityType("someAgent", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.<T> createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.LOGIN_NAME, "someAgent");

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

        query.setParameter(Agent.MEMBER_OF, administratorGroup.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.addToGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

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

        query.setParameter(Agent.HAS_ROLE, administratorRole.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.grantRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(Agent.HAS_ROLE, administratorRole.getName(), someRole.getName());

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

        query.setParameter(User.MEMBER_OF, "Administrators");

        List<T> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.addToGroup(agentType, administratorGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, "Administrators");

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

        query.setParameter(User.HAS_ROLE, "Administrators");

        List<T> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantRole(agentType, administratorRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, "Administrators");

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

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());

        identityManager.removeFromGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup.getName());

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

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(agentType.getId(), result.get(0).getId());

        identityManager.revokeRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole.getName());

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

        query.setParameter(User.MEMBER_OF, administratorGroup.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.addToGroup(agentType, someGroup);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

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

        query.setParameter(User.HAS_ROLE, administratorRole.getName());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertTrue(contains(result, someagent.getId()));

        identityManager.grantRole(agentType, someRole);

        query = identityManager.createIdentityQuery((Class<T>) agentType.getClass());

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, agentType.getId()));
        assertFalse(contains(result, someagent.getId()));
    }

    @Test
    @ExcludeTestSuite({FileIdentityStoreTestSuite.class, LDAPIdentityStoreTestSuite.class})
    public void testFindByLoginNameAndCreationDateWithSorting() throws Exception {
        createAgent("john");
        createAgent("demo");
        createAgent("root");
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
