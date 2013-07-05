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
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreWithoutAttributesTestSuite;
import org.picketlink.test.idm.suites.LDAPJPAMixedStoreTestSuite;
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
public class GroupMembershipTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testCreate() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
    }

    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testUpdate() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        partitionManager.update(groupMembership);

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

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someUser, someGroup);

        assertTrue(partitionManager.isMember(someUser, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        assertFalse(partitionManager.isMember(someUser, someAnotherGroup));

        partitionManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(partitionManager.isMember(someUser, someAnotherGroup));
    }

    @Test
    public void testAddUserToParentGroup() throws Exception {
        User someUser = createUser("someUser");
        Group groupB = createGroup("b", "a");
        Group groupA = groupB.getParentGroup();
        
        assertNotNull(groupA);

        IdentityManager identityManager = getIdentityManager();
        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someUser, groupA);

        assertTrue(partitionManager.isMember(someUser, groupA));
        assertTrue(partitionManager.isMember(someUser, groupB));

        identityManager.remove(groupB);
        identityManager.remove(groupA);
        
        // group testing path is /a/b 
        assertFalse(partitionManager.isMember(someUser, groupB));
        
        groupA = createGroup("a", null);
        
        groupB = createGroupWithParent("b", groupA);
        
        Group groupC = createGroupWithParent("c", groupB);

        partitionManager.addToGroup(someUser, groupA);
        
        // group testing path is /a/b/c
        assertTrue(partitionManager.isMember(someUser, groupA));
        assertTrue(partitionManager.isMember(someUser, groupB));
        assertTrue(partitionManager.isMember(someUser, groupC));

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
        partitionManager.addToGroup(someUser, anotherGroupB);

        assertTrue(partitionManager.isMember(someUser, anotherGroupB));
        assertFalse(partitionManager.isMember(someUser, groupA));
        assertFalse(partitionManager.isMember(someUser, groupB));
        assertFalse(partitionManager.isMember(someUser, groupC));
        assertFalse(partitionManager.isMember(someUser, groupD));
        assertFalse(partitionManager.isMember(someUser, qaGroup));

        partitionManager.addToGroup(someUser, groupB);
        
        assertTrue(partitionManager.isMember(someUser, groupB));
        assertTrue(partitionManager.isMember(someUser, groupD));
        assertTrue(partitionManager.isMember(someUser, qaGroup));
        assertTrue(partitionManager.isMember(someUser, anotherGroupB));

        partitionManager.removeFromGroup(someUser, anotherGroupB);
        
        assertTrue(partitionManager.isMember(someUser, groupB));
        assertTrue(partitionManager.isMember(someUser, groupD));
        assertTrue(partitionManager.isMember(someUser, qaGroup));
        assertTrue(partitionManager.isMember(someUser, anotherGroupB));
    }

    @Test
    public void testRemoveUserWithRelationships() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();
        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someUser, someGroup);
        
        identityManager.remove(someUser);
        
        assertFalse(partitionManager.isMember(someUser, someGroup));
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        IdentityManager identityManager = getIdentityManager();
        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(someUser, someGroup);
        partitionManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(partitionManager.isMember(someUser, someGroup));
        assertTrue(partitionManager.isMember(someUser, someAnotherGroup));

        partitionManager.removeFromGroup(someUser, someGroup);

        assertFalse(partitionManager.isMember(someUser, someGroup));
        assertTrue(partitionManager.isMember(someUser, someAnotherGroup));

        partitionManager.removeFromGroup(someUser, someAnotherGroup);

        assertFalse(partitionManager.isMember(someUser, someAnotherGroup));
    }

    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testFindByAttributes() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.add(groupMembership);

        RelationshipQuery<GroupMembership> query = partitionManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "1");

        List<GroupMembership> result = query.getResultList();

        Assert.assertTrue(result.isEmpty());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        partitionManager.update(groupMembership);

        result = query.getResultList();

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        query = partitionManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = partitionManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute3"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = partitionManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute2"), "1", "2", "3");

        result = query.getResultList();

        Assert.assertFalse(result.isEmpty());
    }
    
    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testLargeAttributeValue() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.add(groupMembership);

        // Create a large array of values
        Integer[] val = new Integer[100];
        for (int i = 0; i < 100; i++) {
            val[i] = i;
        }
        
        groupMembership.setAttribute(new Attribute<Serializable>("Values", val));

        partitionManager.update(groupMembership);
        
        RelationshipQuery<GroupMembership> query = partitionManager.createRelationshipQuery(GroupMembership.class);
        
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

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        List<Group> result = query.getResultList();

        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(user, someGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        partitionManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        partitionManager.addToGroup(user, someImportantGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

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
        PartitionManager partitionManager = getPartitionManager();

        RelationshipQuery<GroupMembership> query = partitionManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        return query.getResultList();
    }

    private boolean contains(List<Group> result, String roleId) {
        for (Group resultGroup : result) {
            if (resultGroup.getName().equals(roleId)) {
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
