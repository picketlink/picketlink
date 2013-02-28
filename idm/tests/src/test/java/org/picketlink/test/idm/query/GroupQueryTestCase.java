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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPJPAMixedStoreTestSuite;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Group} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
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
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class})
    public void testFindByTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier someTier = new Tier("Some Group Tier");
        
        identityManager.createTier(someTier);

        Group someGroupTier = new SimpleGroup("someGroupTier");
        
        identityManager.forTier(someTier).add(someGroupTier);
        
        IdentityQuery<Group> query = identityManager.forTier(someTier).createIdentityQuery(Group.class);
        
        assertNotNull(someTier);
        
        query.setParameter(Group.PARTITION, someTier);
        
        List<Group> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupTier.getId()));
        
        Tier someAnotherTier = new Tier("Some Another Group Tier");
        
        identityManager.createTier(someAnotherTier);
        
        Group someGroupTestingTier = new SimpleGroup("someGroupTestingRealm");
        
        identityManager.forTier(someAnotherTier).add(someGroupTestingTier);
        
        query = identityManager.forTier(someAnotherTier).createIdentityQuery(Group.class);
        
        query.setParameter(Group.PARTITION, someAnotherTier);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupTestingTier.getId()));
    }
    
    @Test
    public void testFindByName() throws Exception {
        Group group = createGroup("admin", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

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

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, group.getParentGroup().getName());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(group.getId(), result.get(0).getId());
        assertEquals(group.getParentGroup().getId(), result.get(0).getParentGroup().getId());
    }
    
    @Test
    public void testFindGroupMembers() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Group groupA = new SimpleGroup("a");
        
        identityManager.add(groupA);
        
        Group groupB = new SimpleGroup("b", groupA);
        
        identityManager.add(groupB);
        
        Group groupC = new SimpleGroup("c", groupB);
        
        identityManager.add(groupC);

        Group groupD = new SimpleGroup("d", groupC);
        
        identityManager.add(groupD);
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, groupA);
        
        List<Group> result = query.getResultList();
        
        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, groupB);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, groupA.getId()));
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, groupC);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, groupA.getId()));
        assertTrue(contains(result, groupB.getId()));
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.HAS_MEMBER, groupD);
        
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

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        List<Group> result = query.getResultList();

        assertFalse(contains(result, someGroup.getId()));
        assertFalse(contains(result, someAnotherGroup.getId()));
        assertFalse(contains(result, someImportantGroup.getId()));

        identityManager.addToGroup(user, someGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getId()));
        assertFalse(contains(result, someAnotherGroup.getId()));
        assertFalse(contains(result, someImportantGroup.getId()));

        identityManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getId()));
        assertTrue(contains(result, someAnotherGroup.getId()));
        assertFalse(contains(result, someImportantGroup.getId()));

        identityManager.addToGroup(user, someImportantGroup);

        query = identityManager.createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getId()));
        assertTrue(contains(result, someAnotherGroup.getId()));
        assertTrue(contains(result, someImportantGroup.getId()));
    }

    @Test
    @ExcludeTestSuite({ LDAPIdentityStoreTestSuite.class, LDAPJPAMixedStoreTestSuite.class })
    public void testFindWithSorting() throws Exception {
        createGroup("someGroup", null);
        // Sleep is needed to avoid same createdDate
        IDMUtil.sleep(1);
        createGroup("someAnotherGroup", null);
        IDMUtil.sleep(1);
        createGroup("someImportantGroup", null);

        // Default sorting by group name
        IdentityQuery<Group> groupQuery = getIdentityManager().createIdentityQuery(Group.class);
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
