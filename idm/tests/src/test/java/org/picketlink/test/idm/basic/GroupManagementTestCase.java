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

package org.picketlink.test.idm.basic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

/**
 * <p>
 * Test case for the {@link Group} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupManagementTestCase extends AbstractIdentityTypeTestCase<Group> {

    @Test
    public void testCreate() throws Exception {
        Group newGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        Group storedGroup = identityManager.getGroup(newGroup.getName());

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
        
        Group group = new SimpleGroup("group");
        
        identityManager.add(group);
        
        Group groupWithSameName = new SimpleGroup("group");
        
        identityManager.add(groupWithSameName);
    }
    
    @Test
    public void testCreateWithSameName() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Group managerGroup = createGroup("managers", null); 
                
        // the QA Group was mapped to a different DN. See the LDAP test suite configuration.
        Group qaManagerGroup = createGroup("managers", "QA Group");
        
        Group storedManagerGroup = identityManager.getGroup(managerGroup.getPath());
        
        assertNotNull(storedManagerGroup);
        assertEquals(managerGroup.getId(), storedManagerGroup.getId());
        assertNull(storedManagerGroup.getParentGroup());
        
        Group storedQAManagerGroup = identityManager.getGroup(qaManagerGroup.getPath());
        
        assertNotNull(storedQAManagerGroup);
        assertEquals(qaManagerGroup.getId(), storedQAManagerGroup.getId());
        assertEquals(qaManagerGroup.getPath(), storedQAManagerGroup.getPath());
        assertFalse(storedQAManagerGroup.getId().equals(storedManagerGroup.getId()));
        assertNotNull(storedQAManagerGroup.getParentGroup());
    }
    
    @Test
    public void testCreateWithMultipleParentGroups() {
        IdentityManager identityManager = getIdentityManager();
        
        Group groupA = createGroup("QA Group", null);
        
        Group groupB = new SimpleGroup("groupB", groupA);
        
        identityManager.add(groupB);
        
        Group groupC = new SimpleGroup("groupC", groupB);
        
        identityManager.add(groupC);
        
        Group groupD = new SimpleGroup("groupD", groupC);
        
        identityManager.add(groupD);
        
        Group storedGroupD = identityManager.getGroup("/QA Group/groupB/groupC/groupD");
        
        assertNotNull(storedGroupD);
        
        assertNotNull(storedGroupD.getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getId(), groupC.getId());
        
        assertNotNull(storedGroupD.getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getId(), groupB.getId());

        assertNotNull(storedGroupD.getParentGroup().getParentGroup().getParentGroup());
        assertEquals(storedGroupD.getParentGroup().getParentGroup().getParentGroup().getId(), groupA.getId());
    }
    
    @Test
    public void testGetGroupPath() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Group groupA = new SimpleGroup("groupA");
        
        identityManager.add(groupA);
        
        Group groupB = new SimpleGroup("groupB", groupA);
        
        identityManager.add(groupB);
        
        Group groupC = new SimpleGroup("groupC", groupB);
        
        identityManager.add(groupC);
        
        Group groupD = new SimpleGroup("groupD", groupC);
        
        identityManager.add(groupD);
        
        Group storedGroupD = identityManager.getGroup("/groupA/groupB/groupC/groupD");
        
        assertEquals(storedGroupD.getId(), groupD.getId());
        assertEquals("/groupA/groupB/groupC/groupD", storedGroupD.getPath());
        
        Group storedGroupB = identityManager.getGroup("/groupA/groupB");
        
        assertEquals(storedGroupB.getId(), groupB.getId());
        assertEquals("/groupA/groupB", storedGroupB.getPath());
        
        Group storedGroupA = identityManager.getGroup("/groupA");
        
        assertEquals(storedGroupA.getId(), groupA.getId());
        assertEquals("/groupA", storedGroupA.getPath());

        storedGroupA = identityManager.getGroup("groupA");
        
        assertEquals(storedGroupA.getId(), groupA.getId());
        assertEquals("/groupA", storedGroupA.getPath());
    }

    @Test
    public void testCreateWithParentGroup() throws Exception {
        Group childGroup = createGroup("childGroup", "parentGroup");

        IdentityManager identityManager = getIdentityManager();

        Group storedChildGroup = identityManager.getGroup(childGroup.getPath());

        assertNotNull(storedChildGroup);
        assertEquals(childGroup.getName(), storedChildGroup.getName());
        assertNotNull(storedChildGroup.getParentGroup());
        assertEquals(childGroup.getParentGroup().getId(), storedChildGroup.getParentGroup().getId());
    }

    @Test
    public void testGetWithParent() throws Exception {
        Group storedGroup = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        Group parentGroup = identityManager.getGroup("Test Parent Group");

        assertNotNull(parentGroup);
        assertNotNull("Test Parent Group", parentGroup.getName());
        assertNotNull("/Test Parent Group", parentGroup.getPath());
        
        storedGroup = identityManager.getGroup("Test Group", parentGroup);

        assertNotNull(storedGroup);
        assertNotNull(storedGroup.getParentGroup());
        assertEquals("Test Group", storedGroup.getName());

        Group invalidParentGroup = createGroup("invalidParentGroup", null);
        
        Group invalidGroup = identityManager.getGroup("Test Group", invalidParentGroup);

        assertNull(invalidGroup);
    }

    @Test
    public void testRemove() throws Exception {
        Group storedGroup = createIdentityType();

        assertNotNull(storedGroup);

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedGroup);

        Group removedGroup = identityManager.getGroup(storedGroup.getName());

        assertNull(removedGroup);
        
        User anotherUser = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group", null);

        identityManager.grantRole(anotherUser, role);
        identityManager.addToGroup(anotherUser, group);
        identityManager.grantGroupRole(anotherUser, role, group);

        RelationshipQuery<?> relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.GROUP, group);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.GROUP, group);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(group);

        relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.GROUP, group);

        assertTrue(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.GROUP, group);

        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Override
    protected Group createIdentityType() {
        return createGroup("Test Group", "Test Parent Group");
    }

    @Override
    protected Group getIdentityType() {
        return getIdentityManager().getGroup("Test Group", getGroup("Test Parent Group"));
    }

}
