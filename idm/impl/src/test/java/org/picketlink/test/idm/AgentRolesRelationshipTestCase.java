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
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentRolesRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests granting roles to users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantRoleToAgent() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);
        Role someRole = loadOrCreateRole("someRole", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(someAgent, someRole);

        assertTrue(identityManager.hasRole(someAgent, someRole));

        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);

        assertFalse(identityManager.hasRole(someAgent, someAnotherRole));

        identityManager.grantRole(someAgent, someAnotherRole);

        assertTrue(identityManager.hasRole(someAgent, someAnotherRole));
        assertTrue(identityManager.hasRole(someAgent, someRole));
    }

    /**
     * <p>
     * Tests revoking roles from users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRevokeRoleFromAgent() throws Exception {
        Agent someAgent = loadOrCreateAgent("someAgent", true);

        Role someRole = loadOrCreateRole("someRole", true);
        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(someAgent, someRole);
        identityManager.grantRole(someAgent, someAnotherRole);

        assertTrue(identityManager.hasRole(someAgent, someRole));
        assertTrue(identityManager.hasRole(someAgent, someAnotherRole));

        identityManager.revokeRole(someAgent, someRole);

        assertFalse(identityManager.hasRole(someAgent, someRole));
        assertTrue(identityManager.hasRole(someAgent, someAnotherRole));

        identityManager.revokeRole(someAgent, someAnotherRole);

        assertFalse(identityManager.hasRole(someAgent, someAnotherRole));
    }
    
    /**
     * <p>
     * Finds all roles for a specific user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindAgentRoles() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);
        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);
        Role someImportantRole = loadOrCreateRole("someImportantRole", true);
        
        Agent user = loadOrCreateAgent("someAgent", true);
        
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
