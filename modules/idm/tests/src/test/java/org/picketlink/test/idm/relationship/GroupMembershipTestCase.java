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

<<<<<<< HEAD
=======
import java.io.Serializable;
import java.util.List;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
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

import java.io.Serializable;
import java.util.List;

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

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

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

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        identityManager.update(groupMembership);

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

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);

        assertTrue(identityManager.isMember(someUser, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        assertFalse(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(identityManager.isMember(someUser, someAnotherGroup));
    }

    @Test
    public void testAddUserToParentGroup() throws Exception {
        User someUser = createUser("someUser");
        Group groupB = createGroup("b", "a");
        Group groupA = groupB.getParentGroup();
        
        assertNotNull(groupA);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.addToGroup(someUser, groupA);

        assertTrue(identityManager.isMember(someUser, groupA));
        assertTrue(identityManager.isMember(someUser, groupB));
        
        identityManager.remove(groupB);
        identityManager.remove(groupA);
        
        // group testing path is /a/b 
        assertFalse(identityManager.isMember(someUser, groupB));
        
        groupA = createGroup("a", null);
        
        groupB = createGroupWithParent("b", groupA);
        
        Group groupC = createGroupWithParent("c", groupB);
        
        identityManager.addToGroup(someUser, groupA);
        
        // group testing path is /a/b/c
        assertTrue(identityManager.isMember(someUser, groupA));
        assertTrue(identityManager.isMember(someUser, groupB));
        assertTrue(identityManager.isMember(someUser, groupC));

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
        identityManager.addToGroup(someUser, anotherGroupB);

        assertTrue(identityManager.isMember(someUser, anotherGroupB));
        assertFalse(identityManager.isMember(someUser, groupA));
        assertFalse(identityManager.isMember(someUser, groupB));
        assertFalse(identityManager.isMember(someUser, groupC));
        assertFalse(identityManager.isMember(someUser, groupD));
        assertFalse(identityManager.isMember(someUser, qaGroup));
        
        identityManager.addToGroup(someUser, groupB);
        
        assertTrue(identityManager.isMember(someUser, groupB));
        assertTrue(identityManager.isMember(someUser, groupD));
        assertTrue(identityManager.isMember(someUser, qaGroup));
        assertTrue(identityManager.isMember(someUser, anotherGroupB));
        
        identityManager.removeFromGroup(someUser, anotherGroupB);
        
        assertTrue(identityManager.isMember(someUser, groupB));
        assertTrue(identityManager.isMember(someUser, groupD));
        assertTrue(identityManager.isMember(someUser, qaGroup));
        assertTrue(identityManager.isMember(someUser, anotherGroupB));
    }

    @Test
    public void testRemoveUserWithRelationships() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);
        
        identityManager.remove(someUser);
        
        assertFalse(identityManager.isMember(someUser, someGroup));
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);
        identityManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(identityManager.isMember(someUser, someGroup));
        assertTrue(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.removeFromGroup(someUser, someGroup);

        assertFalse(identityManager.isMember(someUser, someGroup));
        assertTrue(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.removeFromGroup(someUser, someAnotherGroup);

        assertFalse(identityManager.isMember(someUser, someAnotherGroup));
    }

    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testFindByAttributes() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

        RelationshipQuery<GroupMembership> query = identityManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute1"), "1");

        List<GroupMembership> result = query.getResultList();

        Assert.assertTrue(result.isEmpty());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[] { "1", "2", "3" }));

        identityManager.update(groupMembership);

        result = query.getResultList();

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        query = identityManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute1"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = identityManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute3"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = identityManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute2"), "1", "2", "3");

        result = query.getResultList();

        Assert.assertFalse(result.isEmpty());
    }
    
    @Test
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class, LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testLargeAttributeValue() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

        // Create a large array of values
        Integer[] val = new Integer[100];
        for (int i = 0; i < 100; i++) {
            val[i] = i;
        }
        
        groupMembership.setAttribute(new Attribute<Serializable>("Values", val));

        identityManager.update(groupMembership);
        
        RelationshipQuery<GroupMembership> query = identityManager.createRelationshipQuery(GroupMembership.class);
        
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

        identityManager.addToGroup(user, someGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someImportantGroup);

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
        IdentityManager identityManager = getIdentityManager();

        RelationshipQuery<GroupMembership> query = identityManager.createRelationshipQuery(GroupMembership.class);

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
