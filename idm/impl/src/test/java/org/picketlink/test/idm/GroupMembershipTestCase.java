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

package org.picketlink.test.idm;

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
        User someUser = loadOrCreateUser("someUser", true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(someUser, someGroup);

        assertTrue(identityManager.isMember(someUser, someGroup));

        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        assertFalse(identityManager.isMember(someUser, someAnotherGroup));

        identityManager.addToGroup(someUser, someAnotherGroup);

        assertTrue(identityManager.isMember(someUser, someAnotherGroup));
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
        User someUser = loadOrCreateUser("someUser", true);
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

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
    public void testFindByAttributes() throws Exception {
        User someUser = getUser();
        Group someGroup = getGroup();

        GroupMembership groupMembership = new GroupMembership(someUser, someGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.add(groupMembership);

        RelationshipQuery<GroupMembership> query = identityManager.createQuery(GroupMembership.class);

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

        query = identityManager.createQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute1"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = identityManager.createQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute3"), "2");

        result = query.getResultList();

        Assert.assertTrue(result.isEmpty());
        
        query = identityManager.createQuery(GroupMembership.class);

        query.setParameter(GroupMembership.ATTRIBUTE.byName("attribute2"), new String[] {"1", "2", "3"});

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
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);
        Group someImportantGroup = loadOrCreateGroup("someImportantGroup", null, true);

        User user = loadOrCreateUser("someUser", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.removeFromGroup(user, someGroup);
        identityManager.removeFromGroup(user, someAnotherGroup);
        identityManager.removeFromGroup(user, someImportantGroup);

        IdentityQuery<Group> query = identityManager.createQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        List<Group> result = query.getResultList();

        assertFalse(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someGroup);

        query = identityManager.createQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertFalse(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someAnotherGroup);

        query = identityManager.createQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertFalse(contains(result, "someImportantGroup"));

        identityManager.addToGroup(user, someImportantGroup);

        query = identityManager.createQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, "someGroup"));
        assertTrue(contains(result, "someAnotherGroup"));
        assertTrue(contains(result, "someImportantGroup"));
    }

    private Group getGroup() {
        return loadOrCreateGroup("someGroup", null, true);
    }

    private User getUser() {
        return loadOrCreateUser("someUser", true);
    }

    private List<GroupMembership> getGroupMembership(User someUser, Group someGroup) {
        IdentityManager identityManager = getIdentityManager();

        RelationshipQuery<GroupMembership> query = identityManager.createQuery(GroupMembership.class);

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
