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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractGrantRelationshipTestCase<T extends IdentityType> extends AbstractIdentityManagerTestCase {
    
    protected abstract T createIdentityType(String name, Partition partition);
    protected abstract T createIdentityType(String name);

    protected abstract T getIdentityType();
    
    /**
     * <p>
     * Tests granting roles to users.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantRoleToAgent() throws Exception {
        T someAgent = createIdentityType("someAgent");
        Role someRole = createRole("someRole");

        PartitionManager partitionManager = getPartitionManager();
        IdentityManager identityManager = getIdentityManager();

        partitionManager.grantRole(someAgent, someRole);

        assertTrue(partitionManager.hasRole(someAgent, someRole));

        Role someAnotherRole = createRole("someAnotherRole");

        assertFalse(partitionManager.hasRole(someAgent, someAnotherRole));

        partitionManager.grantRole(someAgent, someAnotherRole);

        assertTrue(partitionManager.hasRole(someAgent, someAnotherRole));
        assertTrue(partitionManager.hasRole(someAgent, someRole));
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
        T someAgent = createIdentityType("someAgent");

        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");

        PartitionManager partitionManager = getPartitionManager();
        IdentityManager identityManager = getIdentityManager();

        partitionManager.grantRole(someAgent, someRole);
        partitionManager.grantRole(someAgent, someAnotherRole);

        assertTrue(partitionManager.hasRole(someAgent, someRole));
        assertTrue(partitionManager.hasRole(someAgent, someAnotherRole));

        partitionManager.revokeRole(someAgent, someRole);

        assertFalse(partitionManager.hasRole(someAgent, someRole));
        assertTrue(partitionManager.hasRole(someAgent, someAnotherRole));

        partitionManager.revokeRole(someAgent, someAnotherRole);

        assertFalse(partitionManager.hasRole(someAgent, someAnotherRole));
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
        
        T user = createIdentityType("someAgent");

        PartitionManager partitionManager = getPartitionManager();
        IdentityManager identityManager = getIdentityManager();
        
        IdentityQuery<Role> query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        List<Role> result = query.getResultList();
        
        assertFalse(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        partitionManager.grantRole(user, someRole);
        
        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        partitionManager.grantRole(user, someAnotherRole);

        query = identityManager.createIdentityQuery(Role.class);
        
        query.setParameter(Role.ROLE_OF, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        partitionManager.grantRole(user, someImportantRole);
        
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
