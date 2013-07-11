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

import java.util.Date;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for the {@link Role} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RoleManagementTestCase extends AbstractIdentityTypeTestCase<Role> {

    @Test
    public void testCreate() throws Exception {
        Role newRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        Role storedRole = identityManager.getRole(newRole.getName());

        assertNotNull(storedRole);
        assertEquals(newRole.getId(), storedRole.getId());
        assertEquals(newRole.getName(), storedRole.getName());
        assertNotNull(storedRole.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedRole.getPartition().getName());
        assertTrue(storedRole.isEnabled());
        assertNull(storedRole.getExpirationDate());
        assertNotNull(storedRole.getCreatedDate());
        assertTrue(new Date().compareTo(storedRole.getCreatedDate()) >= 0);
    }

    @Test
    public void testRemove() throws Exception {
        Role storedRole = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedRole);

        Role removedRole = identityManager.getRole(storedRole.getName());

        assertNull(removedRole);
        
//        User anotherUser = createUser("user");
//        Role role = createRole("role");
//        Group group = createGroup("group", null);
//
//        PartitionManager partitionManager = getPartitionManager();
//
//        partitionManager.grantRole(anotherUser, role);
//        partitionManager.addToGroup(anotherUser, group);
//        partitionManager.grantGroupRole(anotherUser, role, group);
//
//        RelationshipQuery<?> relationshipQuery = partitionManager.createRelationshipQuery(Grant.class);
//
//        relationshipQuery.setParameter(Grant.ROLE, role);
//
//        assertFalse(relationshipQuery.getResultList().isEmpty());
//
//        relationshipQuery = partitionManager.createRelationshipQuery(GroupRole.class);
//
//        relationshipQuery.setParameter(GroupRole.ROLE, role);
//
//        assertFalse(relationshipQuery.getResultList().isEmpty());
//
//        identityManager.remove(role);
//
//        relationshipQuery = partitionManager.createRelationshipQuery(Grant.class);
//
//        relationshipQuery.setParameter(Grant.ROLE, role);
//
//        assertTrue(relationshipQuery.getResultList().isEmpty());
//
//        relationshipQuery = partitionManager.createRelationshipQuery(GroupRole.class);
//
//        relationshipQuery.setParameter(GroupRole.ROLE, role);
//
//        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Test
    public void testEqualsMethod() {
        Role instanceA = createRole("roleA");
        Role instanceB = createRole("roleB");
        
        assertFalse(instanceA.equals(instanceB));
        
        IdentityManager identityManager = getIdentityManager();
        
        assertTrue(instanceA.getName().equals(identityManager.getRole(instanceA.getName()).getName()));
    }

    @Override
    protected Role createIdentityType() {
        return createRole("Administrator");
    }

    @Override
    protected Role getIdentityType() {
        return getIdentityManager().getRole("Administrator");
    }

}
