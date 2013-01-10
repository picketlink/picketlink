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

package org.picketlink.test.idm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Agent} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AgentQueryTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Find an {@link Agent} by id.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindById() throws Exception {
        loadOrCreateAgent("someAgent", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ID, "someAgent");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("someAgent", result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Group} and {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroupRole() throws Exception {
        Agent user = loadOrCreateAgent("someAgent", true);
        Group salesGroup = loadOrCreateGroup("Sales", null, true);
        Role managerRole = loadOrCreateRole("Manager", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_GROUP_ROLE, new GroupRole[] { new SimpleGroupRole(user, managerRole, salesGroup) });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantGroupRole(user, managerRole, salesGroup);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_GROUP_ROLE, new GroupRole[] { new SimpleGroupRole(user, managerRole, salesGroup) });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroup() throws Exception {
        Agent user = loadOrCreateAgent("admin", true);
        Group administratorGroup = loadOrCreateGroup("Administrators", null, true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { "Administrators" });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.addToGroup(user, administratorGroup);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { "Administrators" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBySingleRole() throws Exception {
        Agent user = loadOrCreateAgent("admin", true);
        Role administratorRole = loadOrCreateRole("Administrators", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { "Administrators" });

        List<Agent> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantRole(user, administratorRole);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { "Administrators" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleGroups() throws Exception {
        Agent user = loadOrCreateAgent("admin", true);
        Group administratorGroup = loadOrCreateGroup("Administrators", null, true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(user, administratorGroup);
        identityManager.addToGroup(user, someGroup);

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());

        identityManager.removeFromGroup(user, someGroup);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleRoles() throws Exception {
        Agent user = loadOrCreateAgent("admin", true);
        Role administratorRole = loadOrCreateRole("Administrators", true);
        Role someRole = loadOrCreateRole("someRole", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(user, administratorRole);
        identityManager.grantRole(user, someRole);

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());

        identityManager.revokeRole(user, someRole);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Group}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleAgentWithGroups() throws Exception {
        Agent adminAgent = loadOrCreateAgent("admin", true);
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        Group administratorGroup = loadOrCreateGroup("Administrators", null, true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(adminAgent, administratorGroup);
        identityManager.addToGroup(someAgent, administratorGroup);

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.addToGroup(adminAgent, someGroup);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.MEMBER_OF, new String[] { administratorGroup.getName(), someGroup.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getId()));

        assertFalse(contains(result, someAgent.getId()));
    }

    /**
     * <p>
     * Find an {@link Agent} by his associated {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByMultipleAgentWithRoles() throws Exception {
        Agent adminAgent = loadOrCreateAgent("admin", true);
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        Role administratorRole = loadOrCreateRole("Administrators", true);
        Role someRole = loadOrCreateRole("someRole", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(adminAgent, administratorRole);
        identityManager.grantRole(someAgent, administratorRole);

        IdentityQuery<Agent> query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName() });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getId()));
        assertTrue(contains(result, someAgent.getId()));

        identityManager.grantRole(adminAgent, someRole);

        query = identityManager.createQuery(Agent.class);

        query.setParameter(Agent.HAS_ROLE, new String[] { administratorRole.getName(), someRole.getName() });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminAgent.getId()));
        assertFalse(contains(result, someAgent.getId()));
    }

    /**
     * <p>
     * Finds users with the enabled/disabled status.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindEnabledAndDisabledAgents() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);
        Agent someAnotherAgent = loadOrCreateAgent("someAnotherAgent", true);

        someAgent.setEnabled(true);
        someAnotherAgent.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);
        identityManager.update(someAnotherAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ENABLED, true);

        // all enabled users
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ENABLED, false);

        // only disabled users. No users are disabled.
        result = query.getResultList();

        assertFalse(contains(result, someAgent.getId()));
        assertFalse(contains(result, someAnotherAgent.getId()));

        someAgent.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAgent);

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ENABLED, false);

        // get the previously disabled user
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));
        assertFalse(contains(result, someAnotherAgent.getId()));

        someAnotherAgent.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAnotherAgent);

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ENABLED, true);

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getId()));
        assertFalse(contains(result, someAnotherAgent.getId()));
    }

    /**
     * <p>
     * Finds users by the creation date.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindCreationDate() throws Exception {
        Agent user = loadOrCreateAgent("someAgent", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.CREATED_DATE, user.getCreatedDate());

        // only the previously created user
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someAgent", result.get(0).getId());

        query = identityManager.<Agent> createQuery(Agent.class);

        Calendar futureDate = Calendar.getInstance();

        futureDate.add(Calendar.MINUTE, 1);

        query.setParameter(Agent.CREATED_DATE, futureDate.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users by the expiration date.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindExpiryDate() throws Exception {
        Agent user = loadOrCreateAgent("someAgent", true);

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        user = identityManager.getAgent("someAgent");

        user.setExpirationDate(expirationDate);

        identityManager.update(user);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.EXPIRY_DATE, user.getExpirationDate());

        // all expired users
        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, user.getId()));

        assertEquals("someAgent", result.get(0).getId());

        query = identityManager.<Agent> createQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Agent.EXPIRY_DATE, calendar.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users created between a specific date.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBetweenCreationDate() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);
        Agent someAnotherAgent = loadOrCreateAgent("someAnotherAgent", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        // users between the given time period
        query.setParameter(Agent.CREATED_AFTER, calendar.getTime());
        query.setParameter(Agent.CREATED_BEFORE, new Date());

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        Agent someFutureAgent = loadOrCreateAgent("someFutureAgent", true);
        Agent someAnotherFutureAgent = loadOrCreateAgent("someAnotherFutureAgent", true);

        // users created after the given time
        query.setParameter(Agent.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));
        assertTrue(contains(result, someFutureAgent.getId()));
        assertTrue(contains(result, someAnotherFutureAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        // users created before the given time
        query.setParameter(Agent.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));
        assertTrue(contains(result, someFutureAgent.getId()));
        assertTrue(contains(result, someAnotherFutureAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Agent.EXPIRY_DATE, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users using the IDM specific attributes and user defined attributes.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        Agent user = loadOrCreateAgent("admin", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(user);

        user.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(user);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getId()));
        assertEquals(1, result.size());

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(Agent.ID, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(Agent.ID, "admin");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getId()));
        assertEquals(1, result.size());

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(Agent.ID, "Bad ID");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users expired between a specific date.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        someAgent.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        Agent someAnotherAgent = loadOrCreateAgent("someAnotherAgent", true);

        someAnotherAgent.setExpirationDate(new Date());

        identityManager.update(someAnotherAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        // users between the given time period
        query.setParameter(Agent.EXPIRY_AFTER, expiryDate);
        query.setParameter(Agent.EXPIRY_BEFORE, new Date());

        Agent someFutureAgent = loadOrCreateAgent("someFutureAgent", true);

        someFutureAgent.setExpirationDate(new Date());

        identityManager.update(someFutureAgent);

        Agent someAnotherFutureAgent = loadOrCreateAgent("someAnotherFutureAgent", true);

        someAnotherFutureAgent.setExpirationDate(new Date());

        identityManager.update(someAnotherFutureAgent);

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));
        assertFalse(contains(result, someFutureAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        // users expired after the given time
        query.setParameter(Agent.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));
        assertTrue(contains(result, someFutureAgent.getId()));
        assertTrue(contains(result, someAnotherFutureAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        // users expired before the given time
        query.setParameter(Agent.EXPIRY_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someAgent.getId()));
        assertTrue(contains(result, someAnotherAgent.getId()));
        assertTrue(contains(result, someFutureAgent.getId()));
        assertTrue(contains(result, someAnotherFutureAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        // users expired after the given time. Should return an empty list.
        query.setParameter(Agent.EXPIRY_AFTER, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link Agent} by looking its attributes.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByAgentDefinedAttributes() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        someAgent.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));

        someAgent.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getId()));

        someAgent.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));
    }

    /**
     * <p>
     * Find an {@link Agent} by looking its multi-valued attributes.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByAgentDefinedMultiValuedAttributes() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        someAgent.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someAgent);

        IdentityQuery<Agent> query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Agent> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someAgent.getId()));

        someAgent.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someAgent.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someAgent);

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someAgent.getId()));

        query = identityManager.<Agent> createQuery(Agent.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    private boolean contains(List<Agent> result, String userId) {
        for (Agent resultAgent : result) {
            if (resultAgent.getId().equals(userId)) {
                return true;
            }
        }

        return false;
    }
}
