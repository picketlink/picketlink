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

package org.picketlink.test.idm.relationship;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentRolesRelationshipTestCase<T extends Agent> extends AbstractIdentityManagerTestCase {
    
    protected T createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someAgent";
        }
        
        return (T) createAgent(name, partition);
    }

    protected T getIdentityType() {
        return (T) getIdentityManager().getAgent("someAgent");
    }
    
    /**
     * <p>
     * Tests granting roles to users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantRoleToAgent() throws Exception {
        T someAgent = createIdentityType("someAgent", null);
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(someAgent, someRole);

        assertTrue(identityManager.hasRole(someAgent, someRole));

        Role someAnotherRole = createRole("someAnotherRole");

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
        T someAgent = createIdentityType("someAgent", null);

        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");

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
        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");
        Role someImportantRole = createRole("someImportantRole");
        
        T user = createIdentityType("someAgent", null);
        
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
