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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
import org.picketlink.test.idm.ExcludeTestSuite;
import org.picketlink.test.idm.suites.LDAPIdentityStoreTestSuite;

/**
 * <p>
 * Test case for {@link GroupMembership} basic management operations.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class GroupMembershipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests adding a {@link GroupMembership}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        User someUser = getUser();
        Group someGroup = getGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

        List<GroupMembership> result = getGroupMembership(someUser, someGroup);

        Assert.assertEquals(1, result.size());

        groupMembership = result.get(0);

        Assert.assertEquals(someUser.getId(), groupMembership.getMember().getId());
        Assert.assertEquals(someGroup.getId(), groupMembership.getGroup().getId());
    }

    /**
     * <p>
     * Tests updating a {@link GroupMembership}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    @ExcludeTestSuite (LDAPIdentityStoreTestSuite.class)
    public void testUpdate() throws Exception {
        User someUser = getUser();
        Group someGroup = getGroup();

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

    /**
     * <p>
     * Tests adding an {@link User} as a member of a {@link Group} using the <code>IdentityManager.addToGroupMethod</code>.
     * </p>
     * 
     * @throws Exception
     */
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
    public void testRemoveUserWithRelationships() throws Exception {
        User someUser = createUser("someUser");
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);
        
        identityManager.remove(someUser);
        
        assertFalse(identityManager.isMember(someUser, someGroup));
    }

    /**
     * <p>
     * Tests removing an {@link User} from a {@link Group} using the <code>IdentityManager.removeFromGroup</code> method.
     * </p>
     * 
     * @throws Exception
     */
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

    /**
     * <p>
     * Tests querying using attributes..
     * </p>
     * 
     * @throws Exception
     */
    @Test
    @ExcludeTestSuite (LDAPIdentityStoreTestSuite.class)
    public void testFindByAttributes() throws Exception {
        User someUser = getUser();
        Group someGroup = getGroup();

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

    private Group getGroup() {
        return createGroup("someGroup", null);
    }

    private User getUser() {
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
}
