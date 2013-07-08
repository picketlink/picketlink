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

package org.picketlink.test.idm.query;

import java.util.List;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.util.IDMUtil;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreWithoutAttributesTestSuite;
import org.picketlink.test.idm.suites.LDAPJPAMixedStoreTestSuite;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p/>
 * Test case for the Query API when retrieving {@link Group} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class GroupQueryTestCase extends AbstractIdentityQueryTestCase<Group> {

    @Override
    protected Group createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someGroup";
        }

        return createGroup(name, null, partition);
    }

    @Override
    protected Group getIdentityType() {
        return getIdentityManager().getGroup("someGroup");
    }

    @After
    public void onFinish() {
        IdentityQuery<Group> query = getIdentityManager().createIdentityQuery(Group.class);

        List<Group> result = query.getResultList();

        for (Group group : result) {
            getIdentityManager().remove(group);
        }
    }

    @Test
    @ExcludeTestSuite({LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class,
            LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testFindByTier() throws Exception {
        Group someGroup = new Group("someGroup");

        Tier applicationATier = getPartitionManager().getPartition(Tier.class, "Application A");

        IdentityManager applicationA = getPartitionManager().createIdentityManager(applicationATier);

        applicationA.add(someGroup);

        IdentityQuery<Group> query = applicationA.createIdentityQuery(Group.class);

        query.setParameter(Group.PARTITION, applicationATier);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroup.getId()));

        Tier applicationBTier = getPartitionManager().getPartition(Tier.class, "Application B");

        IdentityManager applicationB = getPartitionManager().createIdentityManager(applicationBTier);

        Group anotherRole = new Group("anotherRole");

        applicationB.add(anotherRole);

        query = applicationB.createIdentityQuery(Group.class);

        query.setParameter(Group.PARTITION, applicationBTier);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, anotherRole.getId()));
    }

    @Test
    public void testFindByName() throws Exception {
        Group group = createGroup("admin", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group>createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, group.getName());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(group.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Finds groups by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindWithParent() throws Exception {
        Group group = createGroup("someGroup", "Parent Group");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group>createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, group.getParentGroup());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(group.getId(), result.get(0).getId());
        assertEquals(group.getParentGroup().getId(), result.get(0).getParentGroup().getId());
    }

    @Test
    @Ignore
    public void testFindGroupMembers() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group groupA = new Group("a");

        identityManager.add(groupA);

        Group groupB = new Group("b", groupA);

        identityManager.add(groupB);

        Group groupC = new Group("c", groupB);

        identityManager.add(groupC);

        Group groupD = new Group("d", groupC);

        identityManager.add(groupD);

        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, groupA);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, groupB.getId()));

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, groupC);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, groupA.getId()));
        assertTrue(contains(result, groupB.getId()));

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, groupD);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, groupA.getId()));
        assertTrue(contains(result, groupB.getId()));
        assertTrue(contains(result, groupC.getId()));
    }

    /**
     * <p>
     * Finds all groups for a specific user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUserGroups() throws Exception {
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);
        Group someImportantGroup = createGroup("someImportantGroup", null);

        User user = createUser("someUser");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        List<GroupMembership> result = query.getResultList();

        assertFalse(containsMembership(result, someGroup));
        assertFalse(containsMembership(result, someAnotherGroup));
        assertFalse(containsMembership(result, someImportantGroup));

        relationshipManager.addToGroup(user, someGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsMembership(result, someGroup));
        assertFalse(containsMembership(result, someAnotherGroup));
        assertFalse(containsMembership(result, someImportantGroup));

        relationshipManager.addToGroup(user, someAnotherGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsMembership(result, someGroup));
        assertTrue(containsMembership(result, someAnotherGroup));
        assertFalse(containsMembership(result, someImportantGroup));

        relationshipManager.addToGroup(user, someImportantGroup);

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, new Object[]{user});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsMembership(result, someGroup));
        assertTrue(containsMembership(result, someAnotherGroup));
        assertTrue(containsMembership(result, someImportantGroup));
    }

    @Test
    @ExcludeTestSuite({LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class,
            LDAPIdentityStoreWithoutAttributesTestSuite.class})
    public void testFindWithSorting() throws Exception {
        createGroup("someGroup", null);
        // Sleep is needed to avoid same createdDate
        Thread.sleep(1000);
        createGroup("someAnotherGroup", null);
        Thread.sleep(1000);
        createGroup("someImportantGroup", null);

        // Default sorting by group name
        IdentityQuery<Group> groupQuery = getIdentityManager().createIdentityQuery(Group.class);

        groupQuery.setSortParameters(Group.NAME);

        List<Group> groups = groupQuery.getResultList();

        assertEquals(3, groups.size());
        assertEquals(groups.get(0).getName(), "someAnotherGroup");
        assertEquals(groups.get(1).getName(), "someGroup");
        assertEquals(groups.get(2).getName(), "someImportantGroup");

        // Descending sorting by creationDate
        groupQuery = getIdentityManager().createIdentityQuery(Group.class);
        groupQuery.setSortAscending(false);
        groupQuery.setSortParameters(IdentityType.ENABLED, IdentityType.CREATED_DATE);
        groups = groupQuery.getResultList();

        assertEquals(3, groups.size());
        assertEquals(groups.get(0).getName(), "someImportantGroup");
        assertEquals(groups.get(1).getName(), "someAnotherGroup");
        assertEquals(groups.get(2).getName(), "someGroup");
    }

}
