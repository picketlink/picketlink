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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Role} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RoleQueryTestCase extends AbstractIdentityManagerTestCase {
    
    @After
    public void onFinish() {
        IdentityQuery<Role> query = getIdentityManager().createIdentityQuery(Role.class);
        
        List<Role> result = query.getResultList();
        
        for (Role role : result) {
            getIdentityManager().remove(role);
        }
    }
    
    @Test
    public void testFindById() throws Exception {
        Role role = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.ID, role.getId());

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(role.getName(), result.get(0).getName());
    }
    
    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class})
    public void testPagination() throws Exception {
        for (int i = 0; i < 50; i++) {
            createRole("someRole" + (i + 1));
        }
        
        IdentityManager identityManager = getIdentityManager();
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        query.setLimit(10);
        query.setOffset(0);
        
        int resultCount = query.getResultCount();
        
        assertEquals(50, resultCount);
        
        List<Role> firstPage = query.getResultList();
        
        assertEquals(10, firstPage.size());
        
        List<String> roleIds = new ArrayList<String>();
        
        for (Role Role : firstPage) {
            roleIds.add(Role.getId());
        }
        
        query.setOffset(10);
        
        List<Role> secondPage = query.getResultList();
        
        assertEquals(10, secondPage.size());
        
        for (Role Role : secondPage) {
            assertFalse(roleIds.contains(Role.getId()));
            roleIds.add(Role.getId());
        }
        
        query.setOffset(20);
        
        List<Role> thirdPage = query.getResultList();
        
        assertEquals(10, thirdPage.size());
        
        for (Role Role : thirdPage) {
            assertFalse(roleIds.contains(Role.getId()));
            roleIds.add(Role.getId());
        }
        
        query.setOffset(30);
        
        List<Role> fourthPage = query.getResultList();
        
        assertEquals(10, fourthPage.size());
        
        for (Role Role : fourthPage) {
            assertFalse(roleIds.contains(Role.getId()));
            roleIds.add(Role.getId());
        }
        
        query.setOffset(40);
        
        List<Role> fifthyPage = query.getResultList();
        
        assertEquals(10, fifthyPage.size());
        
        for (Role Role : fifthyPage) {
            assertFalse(roleIds.contains(Role.getId()));
            roleIds.add(Role.getId());
        }
        
        assertEquals(50, roleIds.size());
        
        query.setOffset(50);
        
        List<Role> invalidPage = query.getResultList();
        
        assertEquals(0, invalidPage.size());
    }
    
    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class})
    public void testFindByRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Role someRoleDefaultRealm = new SimpleRole("someRoleRealm");
        
        identityManager.add(someRoleDefaultRealm);
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        Realm defaultRealm = identityManager.getRealm(Realm.DEFAULT_REALM);
        
        assertNotNull(defaultRealm);
        
        query.setParameter(Role.PARTITION, defaultRealm);
        
        List<Role> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRoleDefaultRealm.getName()));
        
        Realm testingRealm = identityManager.getRealm("Testing");
        
        if (testingRealm == null) {
            testingRealm = new Realm("Testing");
            identityManager.createRealm(testingRealm);
        }
        
        Role someRoleTestingRealm = new SimpleRole("someRoleTestingRealm");
        
        identityManager.forRealm(testingRealm).add(someRoleTestingRealm);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.PARTITION, testingRealm);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRoleTestingRealm.getName()));
    }
    
    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class})
    public void testFindByTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier someTier = new Tier("Some Role Tier");
        
        identityManager.createTier(someTier);

        Role someRoleRealm = new SimpleRole("someRoleRealm");
        
        identityManager.forTier(someTier).add(someRoleRealm);
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        assertNotNull(someTier);
        
        query.setParameter(Role.PARTITION, someTier);
        
        List<Role> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRoleRealm.getName()));
        
        Tier someAnotherTier = new Tier("Some Another Role Tier");
        
        identityManager.createTier(someAnotherTier);
        
        Role someRoleTestingTier = new SimpleRole("someRoleTestingRealm");
        
        identityManager.forTier(someAnotherTier).add(someRoleTestingTier);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.PARTITION, someAnotherTier);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRoleTestingTier.getName()));
    }
    
    /**
     * <p>
     * Find an {@link Role} by id.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByName() throws Exception {
        Role role = createRole("admin");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.NAME, "admin");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(role.getName(), result.get(0).getName());
    }

    /**
     * <p>
     * Finds roles with the enabled/disabled status.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindEnabledAndDisabledRoles() throws Exception {
        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");

        someRole.setEnabled(true);
        someAnotherRole.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);
        identityManager.update(someAnotherRole);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.ENABLED, true);

        // all enabled roles
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.ENABLED, false);

        // only disabled roles. No roles are disabled.
        result = query.getResultList();

        assertTrue(result.isEmpty());

        someRole.setEnabled(false);

        // let's disabled the role and try to find him
        identityManager.update(someRole);

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.ENABLED, false);

        // get the previously disabled role
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRole.getName()));

        someAnotherRole.setEnabled(false);

        // let's disabled the role and try to find him
        identityManager.update(someAnotherRole);

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.ENABLED, true);

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindCreationDate() throws Exception {
        Role role = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.CREATED_DATE, role.getCreatedDate());
        
        // only the previously created role
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(role.getName(), result.get(0).getName());

        query = identityManager.<Role> createIdentityQuery(Role.class);

        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.MINUTE, 1);
        
        query.setParameter(Role.CREATED_DATE, calendar.getTime());

        // no roles
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles by the expiration date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindExpiryDate() throws Exception {
        Role role = createRole("someRole");

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        role = identityManager.getRole("someRole");

        role.setExpirationDate(expirationDate);

        identityManager.update(role);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.EXPIRY_DATE, role.getExpirationDate());

        // all expired roles
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, role.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Role.EXPIRY_DATE, calendar.getTime());

        // no roles
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles created between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenCreationDate() throws Exception {
        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        // roles between the given time period
        query.setParameter(Role.CREATED_AFTER, calendar.getTime());
        query.setParameter(Role.CREATED_BEFORE, new Date());

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        Role someFutureRole = createRole("someFutureRole");
        Role someAnotherFutureRole = createRole("someAnotherFutureRole");

        // roles created after the given time
        query.setParameter(Role.CREATED_AFTER, calendar.getTime());
        
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        // roles created before the given time
        query.setParameter(Role.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        Calendar futureDate = Calendar.getInstance();
        
        futureDate.add(Calendar.MINUTE, 1);
        
        // Should return an empty list.
        query.setParameter(Role.CREATED_AFTER, futureDate.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles using the IDM specific attributes and role defined attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        Role role = createRole("admin");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(role);

        role.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(role);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, role.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    /**
     * <p>
     * Finds roles expired between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        Role someRole = createRole("someRole");

        someRole.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        Role someAnotherRole = createRole("someAnotherRole");

        someAnotherRole.setExpirationDate(new Date());

        identityManager.update(someAnotherRole);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();
        
        // roles between the given time period
        query.setParameter(Role.EXPIRY_AFTER, expiryDate);
        query.setParameter(Role.EXPIRY_BEFORE, new Date());

        Role someFutureRole = createRole("someFutureRole");

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);
        
        someFutureRole.setExpirationDate(calendar.getTime());

        identityManager.update(someFutureRole);

        Role someAnotherFutureRole = createRole("someAnotherFutureRole");

        someAnotherFutureRole.setExpirationDate(calendar.getTime());

        identityManager.update(someAnotherFutureRole);

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        // roles expired after the given time
        query.setParameter(Role.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        // roles expired before the given time
        query.setParameter(Role.EXPIRY_BEFORE, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);
        
        Calendar futureExpiryDate = Calendar.getInstance();
        
        futureExpiryDate.add(Calendar.MINUTE, 2);
        
        // roles expired after the given time. Should return an empty list.
        query.setParameter(Role.EXPIRY_AFTER, futureExpiryDate.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link Role} by looking its attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByRoleDefinedAttributes() throws Exception {
        Role someRole = createRole("someRole");

        someRole.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRole.getName()));

        someRole.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someRole);

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        someRole.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someRole);

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRole.getName()));
    }

    /**
     * <p>
     * Find an {@link Role} by looking its multi-valued attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByRoleDefinedMultiValuedAttributes() throws Exception {
        Role someRole = createRole("someRole");

        someRole.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someRole.getName()));

        someRole.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someRole.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someRole);

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRole.getName()));

        query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    /**
     * <p>
     * Finds all roles for a specific user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUserRoles() throws Exception {
        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");
        Role someImportantRole = createRole("someImportantRole");
        
        User user = createUser("someUser");
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.revokeRole(user, someRole);
        identityManager.revokeRole(user, someAnotherRole);
        identityManager.revokeRole(user, someImportantRole);
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        List<Role> result = query.getResultList();
        
        assertFalse(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));
        
        identityManager.grantRole(user, someRole);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));
        
        identityManager.grantRole(user, someAnotherRole);

        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        identityManager.grantRole(user, someImportantRole);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertTrue(contains(result, "someImportantRole"));
        
        identityManager.revokeRole(user, someRole);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertFalse(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertTrue(contains(result, "someImportantRole"));
    }

    private boolean contains(List<Role> result, String roleId) {
        for (Role resultRole : result) {
            if (resultRole.getName().equals(roleId)) {
                return true;
            }
        }

        return false;
    }
}
