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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreWithoutAttributesTestSuite;
import org.picketlink.test.idm.suites.LDAPJPAMixedStoreTestSuite;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Role} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class RoleQueryTestCase extends AbstractIdentityQueryTestCase<Role> {

    @Override
    protected Role createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someRole";
        }

        return createRole(name, partition);
    }

    @Override
    protected Role getIdentityType() {
        return getIdentityManager().getRole("someRole");
    }

    @After
    public void onFinish() {
        IdentityQuery<Role> query = getIdentityManager().createIdentityQuery(Role.class);

        List<Role> result = query.getResultList();

        for (Role role : result) {
            getIdentityManager().remove(role);
        }
    }

//    @Test
//    @ExcludeTestSuite({ LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class,
//            LDAPIdentityStoreWithoutAttributesTestSuite.class })
//    public void testFindByTier() throws Exception {
//        Tier someTier = new Tier("Some Role Tier");
//
//        getIdentityManagerFactory().createTier("Some Role Tier", null);
//
//        Role someRoleRealm = new SimpleRole("someRoleRealm");
//
//        getIdentityManagerFactory().createIdentityManager(someTier).add(someRoleRealm);
//
//        IdentityQuery<Role> query = getIdentityManagerFactory().createIdentityManager(someTier).createIdentityQuery(Role.class);
//
//        assertNotNull(someTier);
//
//        query.setParameter(Role.PARTITION, someTier);
//
//        List<Role> result = query.getResultList();
//
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.size());
//        assertTrue(contains(result, someRoleRealm.getId()));
//
//        Tier someAnotherTier = new Tier("Some Another Role Tier");
//
//        getIdentityManagerFactory().createTier("Some Another Role Tier", null);
//
//        Role someRoleTestingTier = new SimpleRole("someRoleTestingRealm");
//
//        getIdentityManagerFactory().createIdentityManager(someAnotherTier).add(someRoleTestingTier);
//
//        query = getIdentityManagerFactory().createIdentityManager(someAnotherTier).createIdentityQuery(Role.class);
//
//        query.setParameter(Role.PARTITION, someAnotherTier);
//
//        result = query.getResultList();
//
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.size());
//        assertTrue(contains(result, someRoleTestingTier.getId()));
//    }

    /**
     * <p>
     * Find an {@link Role} by name.
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
        assertEquals(role.getId(), result.get(0).getId());
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

        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);

        query.setParameter(Role.ROLE_OF, new Object[] { user });

        List<Role> result = query.getResultList();

        assertFalse(contains(result, someRole.getId()));
        assertFalse(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));

        identityManager.grantRole(user, someRole);

        query = identityManager.createIdentityQuery(Role.class);

        query.setParameter(Role.ROLE_OF, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertFalse(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));

        identityManager.grantRole(user, someAnotherRole);

        query = identityManager.createIdentityQuery(Role.class);

        query.setParameter(Role.ROLE_OF, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));

        identityManager.grantRole(user, someImportantRole);

        query = identityManager.createIdentityQuery(Role.class);

        query.setParameter(Role.ROLE_OF, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertTrue(contains(result, someImportantRole.getId()));

        identityManager.revokeRole(user, someRole);

        query.setParameter(Role.ROLE_OF, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertFalse(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertTrue(contains(result, someImportantRole.getId()));
    }

    @Test
    @ExcludeTestSuite({ LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class,
            LDAPIdentityStoreWithoutAttributesTestSuite.class })
    public void testFindWithSorting() throws Exception {
        createRole("someRole");
        createRole("someAnotherRole");
        createRole("someImportantRole");

        // Descending sorting by roleName
        IdentityQuery<Role> roleQuery = getIdentityManager().createIdentityQuery(Role.class);
        roleQuery.setSortAscending(false);
        List<Role> roles = roleQuery.getResultList();

        assertEquals(3, roles.size());
        assertEquals(roles.get(0).getName(), "someRole");
        assertEquals(roles.get(1).getName(), "someImportantRole");
        assertEquals(roles.get(2).getName(), "someAnotherRole");
    }

}
