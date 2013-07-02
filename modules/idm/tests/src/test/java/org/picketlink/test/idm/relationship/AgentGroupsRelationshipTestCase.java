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
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Group} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentGroupsRelationshipTestCase<T extends Agent> extends AbstractIdentityManagerTestCase {
    
    protected T createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someAgent";
        }
        
        return (T) createAgent(name, partition);
    }

    protected T createIdentityType(String name) {
        if (name == null) {
            name = "someAgent";
        }
        
        return (T) createAgent(name);
    }

    protected T getIdentityType() {
        return (T) getIdentityManager().getAgent("someAgent");
    }
    
    /**
     * <p>
     * Tests adding an {@link Agent} as a member of a {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testAddAgentToGroup() throws Exception {
        T someAgent = createIdentityType("someAgent");
        Group someGroup = createGroup("someGroup");

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someAgent, someGroup);

        assertTrue(partitionManager.isMember(someAgent, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup");

        assertFalse(partitionManager.isMember(someAgent, someAnotherGroup));

        partitionManager.addToGroup(someAgent, someAnotherGroup);

        assertTrue(partitionManager.isMember(someAgent, someAnotherGroup));
    }

    /**
     * <p>
     * Tests removing an {@link Agent} from a {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemoveAgentFromGroup() throws Exception {
        T someAgent = createIdentityType("someAgent");
        Group someGroup = createGroup("someGroup");
        Group someAnotherGroup = createGroup("someAnotherGroup");

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someAgent, someGroup);
        partitionManager.addToGroup(someAgent, someAnotherGroup);

        assertTrue(partitionManager.isMember(someAgent, someGroup));
        assertTrue(partitionManager.isMember(someAgent, someAnotherGroup));

        partitionManager.removeFromGroup(someAgent, someGroup);

        assertFalse(partitionManager.isMember(someAgent, someGroup));

        partitionManager.removeFromGroup(someAgent, someAnotherGroup);

        assertFalse(partitionManager.isMember(someAgent, someAnotherGroup));
    }
    
    /**
     * <p>
     * Finds all groups for a specific user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindAgentGroups() throws Exception {
        Group someGroup = createGroup("someGroup");
        Group someAnotherGroup = createGroup("someAnotherGroup");
        Group someImportantGroup = createGroup("someImportantGroup");
        
        T user = createIdentityType("someAgent");
        
        IdentityManager identityManager = getIdentityManager();
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        List<Group> result = query.getResultList();
        
        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(user, someGroup);
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        partitionManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        partitionManager.addToGroup(user, someImportantGroup);
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    private boolean contains(List<Group> result, String roleId) {
        for (Group resultGroup : result) {
            if (resultGroup.getName().equals(roleId)) {
                return true;
            }
        }

        return false;
    }
}
