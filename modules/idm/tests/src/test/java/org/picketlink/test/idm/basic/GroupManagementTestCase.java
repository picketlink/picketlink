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

package org.picketlink.test.idm.basic;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JDBCStoreConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * <p>
 * Test case for the {@link Group} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPStoreConfigurationTester.class,
        JDBCStoreConfigurationTester.class})
public class GroupManagementTestCase extends AbstractIdentityTypeTestCase<Group> {

    public GroupManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        Group newGroup = createGroup("someGroup", null);

        Group storedGroup = getGroup(newGroup.getName());

        assertNotNull(storedGroup);
        assertEquals(newGroup.getId(), storedGroup.getId());
        assertEquals(newGroup.getName(), storedGroup.getName());
        assertNotNull(storedGroup.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedGroup.getPartition().getName());
        assertTrue(storedGroup.isEnabled());
        assertNull(storedGroup.getExpirationDate());
        assertNotNull(storedGroup.getCreatedDate());
        assertTrue(new Date().compareTo(storedGroup.getCreatedDate()) >= 0);
    }

    @Test (expected=IdentityManagementException.class)
    public void testFailCreateWithSameName() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group group = new Group("group");

        identityManager.add(group);

        Group groupWithSameName = new Group("group");

        identityManager.add(groupWithSameName);
    }

    @Test
    @Configuration (exclude = {SingleConfigLDAPJPAStoreConfigurationTester.class, JDBCStoreConfigurationTester.class})
    public void testCreateWithSameName() throws Exception {
        Group managerGroup = createGroup("managers", null);

        // the QA Group was mapped to a different DN. See the LDAP test suite configuration.
        Group qaManagerGroup = createGroup("managers", "QA Group");

        Group storedManagerGroup = getGroup(managerGroup.getPath());

        assertNotNull(storedManagerGroup);
        assertEquals(managerGroup.getId(), storedManagerGroup.getId());
        assertNull(storedManagerGroup.getParentGroup());

        Group storedQAManagerGroup = getGroup(qaManagerGroup.getPath());

        assertNotNull(storedQAManagerGroup);
        assertEquals(qaManagerGroup.getId(), storedQAManagerGroup.getId());
        assertEquals(qaManagerGroup.getPath(), storedQAManagerGroup.getPath());
        assertFalse(storedQAManagerGroup.getId().equals(storedManagerGroup.getId()));
        assertNotNull(storedQAManagerGroup.getParentGroup());
    }

    @Test
    @Configuration (exclude = SingleConfigLDAPJPAStoreConfigurationTester.class)
    public void testCreateWithMultipleParentGroups() {
        Group groupA = createGroup("QA Group", null);

        Group groupB = createGroupWithParent("groupB", groupA);

        Group groupC = createGroupWithParent("groupC", groupB);

        Group groupD = createGroupWithParent("groupD", groupC);

        Group storedGroupD = getGroup("/QA Group/groupB/groupC/groupD");

        assertNotNull(storedGroupD);
        assertEquals(storedGroupD.getId(), groupD.getId());
        assertNotNull(storedGroupD.getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getId(), groupC.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getId(), groupB.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getParentGroup().getId(), groupA.getId());
    }

    @Test
    @Configuration (exclude = {LDAPUserGroupJPARoleConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class})
    public void testDefaultHierarchyDepthConfiguration() {
        Group groupRoot = createGroup("Root Group", null);
        Group groupA = createGroupWithParent("QA Group", groupRoot);
        Group groupB = createGroupWithParent("groupB", groupA);
        Group groupC = createGroupWithParent("groupC", groupB);
        Group groupD = createGroupWithParent("groupD", groupC);

        Group storedGroupD = getGroup("/Root Group/QA Group/groupB/groupC/groupD");

        assertNotNull(storedGroupD);
        assertEquals(storedGroupD.getId(), groupD.getId());
        assertNotNull(storedGroupD.getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getId(), groupC.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getId(), groupB.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getParentGroup().getId(), groupA.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup().getParentGroup().getParentGroup());
    }

    @Test
    @Configuration (exclude = SingleConfigLDAPJPAStoreConfigurationTester.class)
    public void testGetGroupPath() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group groupA = createGroup("groupA", null);

        Group groupB = createGroupWithParent("groupB", groupA);

        Group groupC = createGroupWithParent("groupC", groupB);

        Group groupD = createGroupWithParent("groupD", groupC);

        Group storedGroupD = getGroup("/groupA/groupB/groupC/groupD");

        assertEquals(storedGroupD.getId(), groupD.getId());
        assertEquals("/groupA/groupB/groupC/groupD", storedGroupD.getPath());

        Group storedGroupB = getGroup("/groupA/groupB");

        assertEquals(storedGroupB.getId(), groupB.getId());
        assertEquals("/groupA/groupB", storedGroupB.getPath());

        Group storedGroupA = getGroup("/groupA");

        assertEquals(storedGroupA.getId(), groupA.getId());
        assertEquals("/groupA", storedGroupA.getPath());

        storedGroupA = getGroup("groupA");

        assertEquals(storedGroupA.getId(), groupA.getId());
        assertEquals("/groupA", storedGroupA.getPath());
    }

    @Test
    @Configuration (exclude = SingleConfigLDAPJPAStoreConfigurationTester.class)
    public void testCreateWithParentGroup() throws Exception {
        Group childGroup = createGroup("childGroup", "parentGroup");

        Group storedChildGroup = getGroup(childGroup.getPath());

        assertNotNull(storedChildGroup);
        assertEquals(childGroup.getName(), storedChildGroup.getName());
        assertNotNull(storedChildGroup.getParentGroup());
        assertEquals(childGroup.getParentGroup().getId(), storedChildGroup.getParentGroup().getId());
    }

    @Test
    @Configuration (exclude = SingleConfigLDAPJPAStoreConfigurationTester.class)
    public void testGetWithParent() throws Exception {
        Group storedGroup = createIdentityType();

        Group parentGroup = getGroup("Test Parent Group");

        assertNotNull(parentGroup);
        assertNotNull("Test Parent Group", parentGroup.getName());
        assertNotNull("/Test Parent Group", parentGroup.getPath());

        storedGroup = getGroup("Test Group", parentGroup);

        assertNotNull(storedGroup);
        assertNotNull(storedGroup.getParentGroup());
        assertEquals("Test Group", storedGroup.getName());

        Group invalidParentGroup = createGroup("invalidParentGroup", null);

        Group invalidGroup = getGroup("Test Group", invalidParentGroup);

        assertNull(invalidGroup);
    }

    @Test
    public void testRemove() throws Exception {
        Group storedGroup = createIdentityType();

        assertNotNull(storedGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedGroup);

        Group removedGroup = getGroup(storedGroup.getName());

        assertNull(removedGroup);

        User anotherUser = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, anotherUser, group);

        RelationshipQuery<?> relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.GROUP, group);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(group);

        relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.GROUP, group);

        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Override
    protected Group createIdentityType() {
        return createGroup("Test Group", "Test Parent Group");
    }

    @Override
    protected Group getIdentityType() {
        return getGroup("Test Group", getGroup("Test Parent Group"));
    }

    @Test
    public void testEqualsMethod() {
        Group instanceA = createGroup("groupA");
        Group instanceB = createGroup("groupB");

        assertFalse(instanceA.equals(instanceB));

        IdentityManager identityManager = getIdentityManager();

        assertTrue(instanceA.getName().equals(getGroup(instanceA.getPath()).getName()));
    }

}
