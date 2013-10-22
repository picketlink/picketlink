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
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for the relationship between {@link Agent} and {@link Group} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
public class AgentGroupsRelationshipTestCase<T extends Agent> extends AbstractPartitionManagerTestCase {

    public AgentGroupsRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

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
        return (T) getAgent("someAgent");
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

        BasicModel.addToGroup(relationshipManager, someAgent, someGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someAgent, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup");

        assertFalse(BasicModel.isMember(relationshipManager, someAgent, someAnotherGroup));

        BasicModel.addToGroup(relationshipManager, someAgent, someAnotherGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someAgent, someAnotherGroup));
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

        BasicModel.addToGroup(relationshipManager, someAgent, someGroup);
        BasicModel.addToGroup(relationshipManager, someAgent, someAnotherGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someAgent, someGroup));
        assertTrue(BasicModel.isMember(relationshipManager, someAgent, someAnotherGroup));

        BasicModel.removeFromGroup(relationshipManager, someAgent, someGroup);

        assertFalse(BasicModel.isMember(relationshipManager, someAgent, someGroup));

        BasicModel.removeFromGroup(relationshipManager, someAgent, someAnotherGroup);

        assertFalse(BasicModel.isMember(relationshipManager, someAgent, someAnotherGroup));
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

        BasicModel.addToGroup(relationshipManager, user, someGroup);
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[]{user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        BasicModel.addToGroup(relationshipManager, user, someAnotherGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        BasicModel.addToGroup(relationshipManager, user, someImportantGroup);
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, new Object[] {user});
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    @Test
    public void testGroupWithMultipleAgents() throws Exception {
        T someAgent = createIdentityType("someAgent");
        T someAnotherAgent = createIdentityType("someAnotherAgent");
        Group someGroup = createGroup("someGroup", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, someAgent, someGroup);
        BasicModel.addToGroup(relationshipManager, someAnotherAgent, someGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someAgent, someGroup));
        assertTrue(BasicModel.isMember(relationshipManager, someAnotherAgent, someGroup));

        BasicModel.removeFromGroup(relationshipManager, someAgent, someGroup);

        assertFalse(BasicModel.isMember(relationshipManager, someAgent, someGroup));
        assertTrue(BasicModel.isMember(relationshipManager, someAnotherAgent, someGroup));
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
