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
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Agent} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AgentQueryTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testFindById() throws Exception {
        Agent agent = createAgent("someAgent");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.ID, agent.getId());

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals(agent.getLoginName(), result.get(0).getLoginName());
    }
    
    @Test
    public void testFindByRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Agent someAgentDefaultRealm = new SimpleAgent("someAgentRealm");
        
        identityManager.add(someAgentDefaultRealm);
        
        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);
        
        Realm defaultRealm = identityManager.getRealm(Realm.DEFAULT_REALM);
        
        assertNotNull(defaultRealm);
        
        query.setParameter(Agent.PARTITION, defaultRealm);
        
        List<Agent> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgentDefaultRealm.getLoginName()));
        
        Realm testingRealm = identityManager.getRealm("Testing");
        
        if (testingRealm == null) {
            testingRealm = new Realm("Testing");
            identityManager.createRealm(testingRealm);
        }
        
        Agent someAgentTestingRealm = new SimpleAgent("someAgentTestingRealm");
        
        identityManager.forRealm(testingRealm).add(someAgentTestingRealm);
        
        query = identityManager.createIdentityQuery(Agent.class);
        
        query.setParameter(Agent.PARTITION, testingRealm);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertFalse(contains(result, someAgentDefaultRealm.getLoginName()));
        assertTrue(contains(result, someAgentTestingRealm.getLoginName()));
    }
    
    @Test
    public void testFindByLoginName() throws Exception {
        createAgent("someAgent");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.LOGIN_NAME, "someAgent");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someAgent", result.get(0).getLoginName());
    }

    @Test
    public void testFindBySingleGroupRole() throws Exception {
        Agent user = createAgent("someAgent");
        Group salesGroup = createGroup("Sales", null);
        Role managerRole = createRole("Manager");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_GROUP_ROLE, new GroupRole[] { new GroupRole(user, salesGroup, managerRole) });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantGroupRole(user, managerRole, salesGroup);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_GROUP_ROLE, new GroupRole[] { new GroupRole(user, salesGroup, managerRole) });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    @Test
    public void testFindBySingleGroup() throws Exception {
        Agent user = createAgent("admin");
        Group administratorGroup = createGroup("Administrators", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { "Administrators" });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.addToGroup(user, administratorGroup);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { "Administrators" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    @Test
    public void testFindBySingleRole() throws Exception {
        Agent user = createAgent("admin");
        Role administratorRole = createRole("Administrators");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { "Administrators" });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantRole(user, administratorRole);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { "Administrators" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    @Test
    public void testFindByMultipleGroups() throws Exception {
        Agent user = createAgent("admin");
        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(user, administratorGroup);
        identityManager.addToGroup(user, someGroup);

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());

        identityManager.removeFromGroup(user, someGroup);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    @Test
    public void testFindByMultipleRoles() throws Exception {
        Agent user = createAgent("admin");
        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(user, administratorRole);
        identityManager.grantRole(user, someRole);

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());

        identityManager.revokeRole(user, someRole);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    @Test
    public void testFindByMultipleAgentWithGroups() throws Exception {
        Agent adminAgent = createAgent("admin");
        Agent someAgent = createAgent("someAgent");

        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(adminAgent, administratorGroup);
        identityManager.addToGroup(someAgent, administratorGroup);

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getLoginName()));
        assertTrue(contains(result, someAgent.getLoginName()));

        identityManager.addToGroup(adminAgent, someGroup);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getLoginName()));
        assertFalse(contains(result, someAgent.getLoginName()));
    }

    @Test
    public void testFindByMultipleAgentWithRoles() throws Exception {
        Agent adminAgent = createAgent("admin");
        Agent someAgent = createAgent("someAgent");

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(adminAgent, administratorRole);
        identityManager.grantRole(someAgent, administratorRole);

        IdentityQuery<Agent> query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getLoginName()));
        assertTrue(contains(result, someAgent.getLoginName()));

        identityManager.grantRole(adminAgent, someRole);

        query = identityManager.createIdentityQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getLoginName()));
        assertFalse(contains(result, someAgent.getLoginName()));
    }

    @Test
    public void testFindEnabledAndDisabledAgents() throws Exception {
        Agent someAgent = createAgent("someAgent");
        Agent someAnotherAgent = createAgent("someAnotherAgent");

        someAgent.setEnabled(true);
        someAnotherAgent.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);
        identityManager.update(someAnotherAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.ENABLED, true);

        // all enabled users
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.ENABLED, false);

        // only disabled users. No users are disabled.
        result = query.getResultList();

        assertFalse(contains(result, someAgent.getLoginName()));
        assertFalse(contains(result, someAnotherAgent.getLoginName()));

        someAgent.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAgent);

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.ENABLED, false);

        // get the previously disabled user
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertFalse(contains(result, someAnotherAgent.getLoginName()));

        someAnotherAgent.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAnotherAgent);

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.ENABLED, true);

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getLoginName()));
        assertFalse(contains(result, someAnotherAgent.getLoginName()));
    }

    @Test
    public void testFindCreationDate() throws Exception {
        Agent user = createAgent("someAgent");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.CREATED_DATE, user.getCreatedDate());

        // only the previously created user
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someAgent", result.get(0).getLoginName());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        Calendar futureDate = Calendar.getInstance();

        futureDate.add(Calendar.MINUTE, 1);

        query.setParameter(Agent.CREATED_DATE, futureDate.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindExpiryDate() throws Exception {
        Agent user = createAgent("someAgent");

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        user = identityManager.getAgent("someAgent");

        user.setExpirationDate(expirationDate);

        identityManager.update(user);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.EXPIRY_DATE, user.getExpirationDate());

        // all expired users
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));
        assertEquals("someAgent", result.get(0).getLoginName());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        query.setParameter(Agent.EXPIRY_DATE, calendar.getTime());

        // no users
        result = query.getResultList();
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindBetweenCreationDate() throws Exception {
        Agent someAgent = createAgent("someAgent");
        Agent someAnotherAgent = createAgent("someAnotherAgent");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        // users between the given time period
        query.setParameter(Agent.CREATED_AFTER, calendar.getTime());
        query.setParameter(Agent.CREATED_BEFORE, new Date());

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        Agent someFutureAgent = createAgent("someFutureAgent");
        Agent someAnotherFutureAgent = createAgent("someAnotherFutureAgent");

        // users created after the given time
        query.setParameter(Agent.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));
        assertTrue(contains(result, someFutureAgent.getLoginName()));
        assertTrue(contains(result, someAnotherFutureAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        // users created before the given time
        query.setParameter(Agent.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));
        assertTrue(contains(result, someFutureAgent.getLoginName()));
        assertTrue(contains(result, someAnotherFutureAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Agent.CREATED_AFTER, new Date());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        Agent user = createAgent("admin");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(user);

        user.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(user);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));
        assertEquals(1, result.size());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(Agent.LOGIN_NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(Agent.LOGIN_NAME, "admin");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));
        assertEquals(1, result.size());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(Agent.LOGIN_NAME, "Bad ID");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        Agent someAgent = createAgent("someAgent");

        Date currentDate = new Date();
        
        someAgent.setExpirationDate(currentDate);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        Agent someAnotherAgent = createAgent("someAnotherAgent");

        someAnotherAgent.setExpirationDate(currentDate);

        identityManager.update(someAnotherAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        // users between the given time period
        query.setParameter(Agent.EXPIRY_AFTER, expiryDate);
        query.setParameter(Agent.EXPIRY_BEFORE, currentDate);

        Agent someFutureAgent = createAgent("someFutureAgent");

        someFutureAgent.setExpirationDate(new Date());

        identityManager.update(someFutureAgent);

        Agent someAnotherFutureAgent = createAgent("someAnotherFutureAgent");

        someAnotherFutureAgent.setExpirationDate(new Date());

        identityManager.update(someAnotherFutureAgent);

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));
        assertFalse(contains(result, someFutureAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        // users expired after the given time
        query.setParameter(Agent.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));
        assertTrue(contains(result, someFutureAgent.getLoginName()));
        assertTrue(contains(result, someAnotherFutureAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        // users expired before the given time
        query.setParameter(Agent.EXPIRY_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
        assertTrue(contains(result, someAnotherAgent.getLoginName()));
        assertTrue(contains(result, someFutureAgent.getLoginName()));
        assertTrue(contains(result, someAnotherFutureAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        // users expired after the given time. Should return an empty list.
        query.setParameter(Agent.EXPIRY_AFTER, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindByAgentDefinedAttributes() throws Exception {
        Agent someAgent = createAgent("someAgent");

        someAgent.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));

        someAgent.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getLoginName()));

        someAgent.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));
    }

    @Test
    public void testFindByAgentDefinedMultiValuedAttributes() throws Exception {
        Agent someAgent = createAgent("someAgent");

        someAgent.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getLoginName()));

        someAgent.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someAgent.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getLoginName()));

        query = identityManager.<Agent> createIdentityQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    private boolean contains(List<Agent> result, String userId) {
        for (Agent resultAgent : result) {
            if (resultAgent.getLoginName().equals(userId)) {
                return true;
            }
        }

        return false;
    }
}
