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
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for {@link GroupMembership} basic management operations.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
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

        assertEquals(1, result.size());

        groupMembership = result.get(0);

        assertEquals(someUser.getId(), groupMembership.getMember().getId());
        assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testUpdate() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        assertEquals(1, result.size());

        groupMembership = result.get(0);

        assertEquals(someUser.getId(), groupMembership.getMember().getId());
        assertEquals(someGroup.getId(), groupMembership.getGroup().getId());

        groupMembership.setAttribute(new Attribute<String>("attribute1", "1"));
        groupMembership.setAttribute(new Attribute<String[]>("attribute2", new String[]{"1", "2", "3"}));

        relationshipManager.update(groupMembership);

        result = getGroupMembership(someUser, someGroup);

        assertEquals(1, result.size());

        groupMembership = result.get(0);

        assertEquals(someUser.getId(), groupMembership.getMember().getId());
        assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
        assertNotNull(groupMembership.getAttribute("attribute1"));
        assertNotNull(groupMembership.getAttribute("attribute2"));

        assertEquals("1", groupMembership.getAttribute("attribute1").getValue());
        assertEquals(3, groupMembership.<String[]>getAttribute("attribute2").getValue().length);
    }

    @Test
    public void testAddUserToGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, someUser, someGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, someGroup));

        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        assertFalse(BasicModel.isMember(relationshipManager, someUser, someAnotherGroup));

        BasicModel.addToGroup(relationshipManager, someUser, someAnotherGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, someAnotherGroup));
    }

    @Test
    public void testAddUserToParentGroup() throws Exception {
        User someUser = createUser("someUser");
        Group groupB = createGroup("b", "a");
        Group groupA = groupB.getParentGroup();

        assertNotNull(groupA);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, someUser, groupB);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupB));

        identityManager.remove(groupB);
        identityManager.remove(groupA);

        // group testing path is /a/b 
        assertFalse(BasicModel.isMember(relationshipManager, someUser, groupB));

        groupB = createGroup("b", "a");

        groupA = groupB.getParentGroup();

        Group groupC = createGroupWithParent("c", groupB);

        BasicModel.addToGroup(relationshipManager, someUser, groupC);

        // group testing path is /a/b/c
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupC));

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
        BasicModel.addToGroup(relationshipManager, someUser, anotherGroupB);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, anotherGroupB));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupC));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, qaGroup));
        assertFalse(BasicModel.isMember(relationshipManager, someUser, groupD));

        BasicModel.removeFromGroup(relationshipManager, someUser, anotherGroupB);
        BasicModel.addToGroup(relationshipManager, someUser, groupD);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupA));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupB));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, groupD));
        assertFalse(BasicModel.isMember(relationshipManager, someUser, anotherGroupB));
    }

    @Test
    public void testRemoveUserWithRelationships() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, someUser, someGroup);

        identityManager.remove(someUser);

        assertFalse(BasicModel.isMember(relationshipManager, someUser, someGroup));
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        IdentityManager identityManager = getIdentityManager();
        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, someUser, someGroup);
        BasicModel.addToGroup(relationshipManager, someUser, someAnotherGroup);

        assertTrue(BasicModel.isMember(relationshipManager, someUser, someGroup));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, someAnotherGroup));

        BasicModel.removeFromGroup(relationshipManager, someUser, someGroup);

        assertFalse(BasicModel.isMember(relationshipManager, someUser, someGroup));
        assertTrue(BasicModel.isMember(relationshipManager, someUser, someAnotherGroup));

        BasicModel.removeFromGroup(relationshipManager, someUser, someAnotherGroup);

        assertFalse(BasicModel.isMember(relationshipManager, someUser, someAnotherGroup));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
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

        for (Integer value : retrievedVal) {
            assertTrue(contains(retrievedVal, value));
        }
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testSetOneValuedAttribute() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        groupMembership.setAttribute(new Attribute<String>("one-valued", "1"));

        relationshipManager.update(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());

        GroupMembership updatedIdentityType = result.get(0);

        Attribute<String> oneValuedAttribute = updatedIdentityType.getAttribute("one-valued");

        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testSetMultiValuedAttribute() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        groupMembership.setAttribute(new Attribute<String[]>("multi-valued", new String[]{"1", "2", "3"}));

        relationshipManager.update(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());

        GroupMembership updatedIdentityType = result.get(0);

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertNotNull(multiValuedAttribute.getValue());
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[]{"1", "2", "3"}));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testSetMultipleAttributes() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        groupMembership.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        groupMembership.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        groupMembership.setAttribute(new Attribute<String>("Question1Answer", "Gum"));
        groupMembership.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        groupMembership.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        relationshipManager.update(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());

        GroupMembership updatedIdentityType = result.get(0);

        assertNotNull(updatedIdentityType.<String>getAttribute("QuestionTotal"));
        assertNotNull(updatedIdentityType.<String>getAttribute("Question1"));
        assertNotNull(updatedIdentityType.<String>getAttribute("Question1Answer"));
        assertNotNull(updatedIdentityType.<String>getAttribute("Question2"));
        assertNotNull(updatedIdentityType.<String>getAttribute("Question2Answer"));

        assertEquals("2", updatedIdentityType.<String>getAttribute("QuestionTotal").getValue());
        assertEquals("What is favorite toy?", updatedIdentityType.<String>getAttribute("Question1").getValue());
        assertEquals("Gum", updatedIdentityType.<String>getAttribute("Question1Answer").getValue());
        assertEquals("What is favorite word?", updatedIdentityType.<String>getAttribute("Question2").getValue());
        assertEquals("Hi", updatedIdentityType.<String>getAttribute("Question2Answer").getValue());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testUpdateAttribute() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        groupMembership.setAttribute(new Attribute<String[]>("multi-valued", new String[]{"1", "2", "3"}));

        relationshipManager.update(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());

        GroupMembership updatedIdentityType = result.get(0);

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[]{"3", "4", "5"});

        updatedIdentityType.setAttribute(multiValuedAttribute);

        relationshipManager.update(updatedIdentityType);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        updatedIdentityType = result.get(0);

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[]{"3", "4", "5"}));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testRemoveAttribute() throws Exception {
        User someUser = createUser();
        Group someGroup = createGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(groupMembership);

        groupMembership.setAttribute(new Attribute<String[]>("multi-valued", new String[]{"1", "2", "3"}));

        relationshipManager.update(groupMembership);

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        List<GroupMembership> result = query.getResultList();

        assertFalse(result.isEmpty());

        GroupMembership updatedIdentityType = result.get(0);

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityType.removeAttribute("multi-valued");

        relationshipManager.update(updatedIdentityType);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, someUser);
        query.setParameter(GroupMembership.GROUP, someGroup);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        updatedIdentityType = result.get(0);

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
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

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

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

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        BasicModel.addToGroup(relationshipManager, user, someImportantGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

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
