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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the relationship between {@link User} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserRolesRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests granting roles to users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantRoleToUser() throws Exception {
        User someUser = createUser("someUser");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(someUser, someRole);

        assertTrue(identityManager.hasRole(someUser, someRole));

        Role someAnotherRole = createRole("someAnotherRole");

        assertFalse(identityManager.hasRole(someUser, someAnotherRole));

        identityManager.grantRole(someUser, someAnotherRole);

        assertTrue(identityManager.hasRole(someUser, someAnotherRole));
        assertTrue(identityManager.hasRole(someUser, someRole));
    }

    /**
     * <p>
     * Tests revoking roles from users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRevokeRoleFromUser() throws Exception {
        User someUser = createUser("someUser");

        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(someUser, someRole);
        identityManager.grantRole(someUser, someAnotherRole);

        assertTrue(identityManager.hasRole(someUser, someRole));
        assertTrue(identityManager.hasRole(someUser, someAnotherRole));

        identityManager.revokeRole(someUser, someRole);

        assertFalse(identityManager.hasRole(someUser, someRole));
        assertTrue(identityManager.hasRole(someUser, someAnotherRole));

        identityManager.revokeRole(someUser, someAnotherRole);

        assertFalse(identityManager.hasRole(someUser, someAnotherRole));
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
