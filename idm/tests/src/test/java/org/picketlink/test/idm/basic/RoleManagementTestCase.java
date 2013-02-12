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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

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
        
        User anotherUser = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group", null);

        identityManager.grantRole(anotherUser, role);
        identityManager.addToGroup(anotherUser, group);
        identityManager.grantGroupRole(anotherUser, role, group);

        RelationshipQuery<?> relationshipQuery = identityManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ROLE, role);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.ROLE, role);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(role);

        relationshipQuery = identityManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ROLE, role);

        assertTrue(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);

        relationshipQuery.setParameter(GroupRole.ROLE, role);

        assertTrue(relationshipQuery.getResultList().isEmpty());
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
