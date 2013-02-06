/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.FileIdentityStoreTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;

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
    @ExcludeTestSuite ({LDAPIdentityStoreTestSuite.class})
    public void testFindByTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier someTier = new Tier("Some Group Tier");
        
        identityManager.createTier(someTier);

        Group someGroupTier = new SimpleGroup("someGroupTier");
        
        identityManager.forTier(someTier).add(someGroupTier);
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
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
        
        query = identityManager.createIdentityQuery(Group.class);
        
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

        identityManager.removeFromGroup(user, someGroup);
        identityManager.removeFromGroup(user, someAnotherGroup);
        identityManager.removeFromGroup(user, someImportantGroup);

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
    @ExcludeTestSuite({ LDAPIdentityStoreTestSuite.class })
    public void testFindWithSorting() throws Exception {
        createGroup("someGroup", null);
        createGroup("someAnotherGroup", null);
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
