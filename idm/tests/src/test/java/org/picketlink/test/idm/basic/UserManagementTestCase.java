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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * <p>
 * Test case for the {@link User} basic management operations using only the default realm.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserManagementTestCase extends AbstractIdentityTypeTestCase<User> {

    @Test
    public void testCreate() throws Exception {
        User newUser = createUser("jduke");

        assertNotNull(newUser.getId());

        newUser.setEmail("jduke@jboss.org");
        newUser.setFirstName("Java");
        newUser.setLastName("Duke");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(newUser);

        User storedUser = identityManager.getUser(newUser.getLoginName());

        assertNotNull(storedUser);
        assertEquals(newUser.getId(), storedUser.getId());
        assertEquals(newUser.getLoginName(), storedUser.getLoginName());
        assertEquals(newUser.getFirstName(), storedUser.getFirstName());
        assertEquals(newUser.getLastName(), storedUser.getLastName());
        assertEquals(newUser.getEmail(), storedUser.getEmail());
        assertNotNull(storedUser.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedUser.getPartition().getName());
        assertTrue(storedUser.isEnabled());
        assertNull(storedUser.getExpirationDate());
        assertNotNull(storedUser.getCreatedDate());
        assertTrue(new Date().compareTo(storedUser.getCreatedDate()) >= 0);
    }

    @Test
    public void testUpdate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUser = createUser("admin");

        storedUser.setEmail("admin@jboss.org");
        storedUser.setFirstName("The");
        storedUser.setLastName("Administrator");

        identityManager.update(storedUser);

        storedUser = identityManager.getUser(storedUser.getLoginName());

        assertEquals("admin", storedUser.getLoginName());
        assertEquals("The", storedUser.getFirstName());
        assertEquals("Administrator", storedUser.getLastName());
        assertEquals("admin@jboss.org", storedUser.getEmail());

        storedUser.setFirstName("Updated " + storedUser.getFirstName());
        storedUser.setLastName("Updated " + storedUser.getLastName());
        storedUser.setEmail("Updated " + storedUser.getEmail());

        Date actualDate = Calendar.getInstance().getTime();

        storedUser.setExpirationDate(actualDate);

        identityManager.update(storedUser);

        User updatedUser = identityManager.getUser(storedUser.getLoginName());

        assertEquals("Updated The", updatedUser.getFirstName());
        assertEquals("Updated Administrator", updatedUser.getLastName());
        assertEquals("Updated admin@jboss.org", updatedUser.getEmail());
        assertEquals(actualDate, updatedUser.getExpirationDate());

    }

    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User someUser = createUser("admin");
        User anotherUser = createUser("someAnotherUser");

        identityManager.remove(someUser);

        User removedUser = getIdentityManager().getUser(someUser.getLoginName());

        assertNull(removedUser);

        anotherUser = identityManager.getUser(anotherUser.getLoginName());

        assertNotNull(anotherUser);

        Role role = createRole("role");
        Group group = createGroup("group", null);

        identityManager.grantRole(anotherUser, role);
        identityManager.addToGroup(anotherUser, group);
        identityManager.grantGroupRole(anotherUser, role, group);

        RelationshipQuery<?> relationshipQuery = identityManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherUser);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherUser);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.MEMBER, anotherUser);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(anotherUser);

        relationshipQuery = identityManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherUser);

        assertTrue(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherUser);

        assertTrue(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.MEMBER, anotherUser);

        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Test
    public void testEqualsMethod() {
        User instanceA = createUser("userA");
        User instanceB = createUser("userB");
        
        assertFalse(instanceA.equals(instanceB));
        
        IdentityManager identityManager = getIdentityManager();
        
        assertTrue(instanceA.equals(identityManager.getUser(instanceA.getLoginName())));
    }
    @Override
    protected User createIdentityType() {
        return createUser("admin");
    }

    @Override
    protected User getIdentityType() {
        return getIdentityManager().getUser("admin");
    }
    
}
