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

import java.util.List;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.IdentityLocator;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractGrantRelationshipTestCase<T extends IdentityType> extends AbstractPartitionManagerTestCase {

    public AbstractGrantRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        IdentityLocator.grantRole(relationshipManager, someAgent, someRole);

        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someRole));

        Role someAnotherRole = createRole("someAnotherRole");

        assertFalse(IdentityLocator.hasRole(relationshipManager, someAgent, someAnotherRole));

        IdentityLocator.grantRole(relationshipManager, someAgent, someAnotherRole);

        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someAnotherRole));
        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someRole));
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        IdentityLocator.grantRole(relationshipManager, someAgent, someRole);
        IdentityLocator.grantRole(relationshipManager, someAgent, someAnotherRole);

        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someRole));
        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someAnotherRole));

        IdentityLocator.revokeRole(relationshipManager, someAgent, someRole);

        assertFalse(IdentityLocator.hasRole(relationshipManager, someAgent, someRole));
        assertTrue(IdentityLocator.hasRole(relationshipManager, someAgent, someAnotherRole));

        IdentityLocator.revokeRole(relationshipManager, someAgent, someAnotherRole);

        assertFalse(IdentityLocator.hasRole(relationshipManager, someAgent, someAnotherRole));
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();
        
        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);
        
        query.setParameter(Grant.ASSIGNEE, user);
        
        List<Grant> result = query.getResultList();
        
        assertFalse(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        IdentityLocator.grantRole(relationshipManager, user, someRole);
        
        query = relationshipManager.createRelationshipQuery(Grant.class);
        
        query.setParameter(Grant.ASSIGNEE, user);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertFalse(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        IdentityLocator.grantRole(relationshipManager, user, someAnotherRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);
        
        query.setParameter(Grant.ASSIGNEE, user);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertFalse(contains(result, "someImportantRole"));

        IdentityLocator.grantRole(relationshipManager, user, someImportantRole);
        
        query = relationshipManager.createRelationshipQuery(Grant.class);
        
        query.setParameter(Grant.ASSIGNEE, user);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someRole"));
        assertTrue(contains(result, "someAnotherRole"));
        assertTrue(contains(result, "someImportantRole"));
    }

    private boolean contains(List<Grant> result, String roleId) {
        for (Grant resultRole : result) {
            if (resultRole.getRole().getName().equals(roleId)) {
                return true;
            }
        }

        return false;
    }

}
