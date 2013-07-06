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
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.addToGroup(someAgent, someGroup);

        assertTrue(relationshipManager.isMember(someAgent, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup");

        assertFalse(relationshipManager.isMember(someAgent, someAnotherGroup));

        relationshipManager.addToGroup(someAgent, someAnotherGroup);

        assertTrue(relationshipManager.isMember(someAgent, someAnotherGroup));
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.addToGroup(someAgent, someGroup);
        relationshipManager.addToGroup(someAgent, someAnotherGroup);

        assertTrue(relationshipManager.isMember(someAgent, someGroup));
        assertTrue(relationshipManager.isMember(someAgent, someAnotherGroup));

        relationshipManager.removeFromGroup(someAgent, someGroup);

        assertFalse(relationshipManager.isMember(someAgent, someGroup));

        relationshipManager.removeFromGroup(someAgent, someAnotherGroup);

        assertFalse(relationshipManager.isMember(someAgent, someAnotherGroup));
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
        
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        
        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[] {user});
        
        List<GroupMembership> result = query.getResultList();
        
        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        relationshipManager.addToGroup(user, someGroup);
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[]{user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        relationshipManager.addToGroup(user, someAnotherGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        relationshipManager.addToGroup(user, someImportantGroup);
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    private boolean contains(List<GroupMembership> result, String groupName) {
        for (GroupMembership resultGroup : result) {
            if (resultGroup.getGroup().getName().equals(groupName)) {
                return true;
            }
        }

        return false;
    }
}
