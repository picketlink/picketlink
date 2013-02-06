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

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.FileIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;

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
        assertTrue(contains(result, someRoleRealm.getId()));
        
        Tier someAnotherTier = new Tier("Some Another Role Tier");
        
        identityManager.createTier(someAnotherTier);
        
        Role someRoleTestingTier = new SimpleRole("someRoleTestingRealm");
        
        identityManager.forTier(someAnotherTier).add(someRoleTestingTier);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.PARTITION, someAnotherTier);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someRoleTestingTier.getId()));
    }
    
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
        
        identityManager.revokeRole(user, someRole);
        identityManager.revokeRole(user, someAnotherRole);
        identityManager.revokeRole(user, someImportantRole);
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        List<Role> result = query.getResultList();
        
        assertFalse(contains(result, someRole.getId()));
        assertFalse(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));
        
        identityManager.grantRole(user, someRole);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertFalse(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));
        
        identityManager.grantRole(user, someAnotherRole);

        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertFalse(contains(result, someImportantRole.getId()));

        identityManager.grantRole(user, someImportantRole);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertTrue(contains(result, someImportantRole.getId()));
        
        identityManager.revokeRole(user, someRole);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertFalse(contains(result, someRole.getId()));
        assertTrue(contains(result, someAnotherRole.getId()));
        assertTrue(contains(result, someImportantRole.getId()));
    }

    @Test
    @ExcludeTestSuite({LDAPIdentityStoreTestSuite.class})
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
