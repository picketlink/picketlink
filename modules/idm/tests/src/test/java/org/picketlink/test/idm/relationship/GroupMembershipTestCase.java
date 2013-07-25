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

import java.io.Serializable;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for {@link GroupMembership} basic management operations.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class GroupMembershipTestCase extends AbstractPartitionManagerTestCase {

    public GroupMembershipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
    }

    @Test
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testUpdate() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        relationshipManager.update(groupMembership);

        result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
        Assert.assertNotNull(groupMembership.getAttribute("attribute1"));
        Assert.assertNotNull(groupMembership.getAttribute("attribute2"));

        Assert.assertEquals("1", groupMembership.getAttribute("attribute1").getValue());
        Assert.assertEquals(3, groupMembership.<String[]> getAttribute("attribute2").getValue().length);
    }

    @Test
    public void testAddUserToGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.addToGroup(relationshipManager, someUser, someGroup);

        assertTrue(SampleModel.isMember(relationshipManager, someUser, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        assertFalse(SampleModel.isMember(relationshipManager, someUser, someAnotherGroup));

        SampleModel.addToGroup(relationshipManager, someUser, someAnotherGroup);

        assertTrue(SampleModel.isMember(relationshipManager, someUser, someAnotherGroup));
    }

    @Test
    public void testAddUserToParentGroup() throws Exception {
        User someUser = createUser("someUser");
        Group groupB = createGroup("b", "a");
        Group groupA = groupB.getParentGroup();
        
        assertNotNull(groupA);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.addToGroup(relationshipManager, someUser, groupB);

        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupB));

        identityManager.remove(groupB);
        identityManager.remove(groupA);
        
        // group testing path is /a/b 
        assertFalse(SampleModel.isMember(relationshipManager, someUser, groupB));
        
        groupA = createGroup("a", null);
        
        groupB = createGroupWithParent("b", groupA);
        
        Group groupC = createGroupWithParent("c", groupB);

        SampleModel.addToGroup(relationshipManager, someUser, groupC);
        
        // group testing path is /a/b/c
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupC));

        identityManager.remove(groupC);
        identityManager.remove(groupB);
        identityManager.remove(groupA);

        groupA = createGroup("a", null);
        
        groupB = createGroupWithParent("b", groupA);
        
        groupC = createGroupWithParent("c", groupB);
        
        Group groupD = createGroupWithParent("d", groupC);
        
        Group qaGroup = createGroupWithParent("QA Group", groupC);
                
        Group anotherGroupB = createGroupWithParent("b", qaGroup);
        
        // group testing paths are: /a/b/c/QA Group/b and /a/b/c/d
        SampleModel.addToGroup(relationshipManager, someUser, anotherGroupB);

        assertTrue(SampleModel.isMember(relationshipManager, someUser, anotherGroupB));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupC));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, qaGroup));
        assertFalse(SampleModel.isMember(relationshipManager, someUser, groupD));

        SampleModel.removeFromGroup(relationshipManager, someUser, anotherGroupB);
        SampleModel.addToGroup(relationshipManager, someUser, groupD);
        
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, groupD));
        assertFalse(SampleModel.isMember(relationshipManager, someUser, anotherGroupB));
    }

    @Test
    public void testRemoveUserWithRelationships() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.addToGroup(relationshipManager, someUser, someGroup);
        
        identityManager.remove(someUser);
        
        assertFalse(SampleModel.isMember(relationshipManager, someUser, someGroup));
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.addToGroup(relationshipManager, someUser, someGroup);
        SampleModel.addToGroup(relationshipManager, someUser, someAnotherGroup);

        assertTrue(SampleModel.isMember(relationshipManager, someUser, someGroup));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, someAnotherGroup));

        SampleModel.removeFromGroup(relationshipManager, someUser, someGroup);

        assertFalse(SampleModel.isMember(relationshipManager, someUser, someGroup));
        assertTrue(SampleModel.isMember(relationshipManager, someUser, someAnotherGroup));

        SampleModel.removeFromGroup(relationshipManager, someUser, someAnotherGroup);

        assertFalse(SampleModel.isMember(relationshipManager, someUser, someAnotherGroup));
    }

    @Test
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testFindByAttributes() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "1");

        List<GroupMembership> result = query.getResultList();

        Assert.assertTrue(result.isEmpty());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        relationshipManager.update(groupMembership);

        result = query.getResultList();

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute3"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute2"), "1", "2", "3");

        result = query.getResultList();

        Assert.assertFalse(result.isEmpty());
    }
    
    @Test
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testLargeAttributeValue() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        // Create a large array of values
        Integer[] val = new Integer[100];
        for (int i = 0; i < 100; i++) {
            val[i] = i;
        }
        
        groupMembership.setAttribute(new Attribute<Serializable>("Values", val));

        relationshipManager.update(groupMembership);
        
        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);
        
        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        GroupMembership updatedIdentityType = result.get(0);

        Integer[] retrievedVal = updatedIdentityType.<Integer[]>getAttribute("Values").getValue();

        for (Integer value: retrievedVal) {
            assertTrue(contains(retrievedVal, value));
        }
    }

    @Test
    public void testFindUserGroups() throws Exception {
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);
        Group someImportantGroup = createGroup("someImportantGroup", null);

        User user = createUser("someUser");

        PartitionManager partitionManager = getPartitionManager();
        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[] { user });

        List<GroupMembership> result = query.getResultList();

        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        SampleModel.addToGroup(relationshipManager, user, someGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        SampleModel.addToGroup(relationshipManager, user, someAnotherGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        SampleModel.addToGroup(relationshipManager, user, someImportantGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    private Group createGroup() {
        return createGroup("someGroup", null);
    }

    private User createUser() {
        return createUser("someUser");
    }

    private List<GroupMembership> getGroupMembership(User someUser, Group someGroup) {
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        return query.getResultList();
    }

    private boolean contains(List<GroupMembership> result, String groupName) {
        for (GroupMembership resultGroup : result) {
            if (resultGroup.getGroup().getName().equals(groupName)) {
                return true;
            }
        }

        return false;
    }

    private boolean contains(Integer[] result, Integer value) {
        for (Integer resultValue : result) {
            if (resultValue.equals(value)) {
                return true;
            }
        }

        return false;
    }

}
