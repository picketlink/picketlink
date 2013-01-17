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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

/**
 * <p>
 * Test case for {@link Role} basic management operations.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RoleManagementTestCase extends AbstractIdentityTypeTestCase<Role> {

    /**
     * <p>
     * Creates a new {@link Role} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        Role newRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        Role storedRole = identityManager.getRole(newRole.getName());

        assertNotNull(storedRole);

        assertEquals(newRole.getName(), storedRole.getName());
        assertNotNull(storedRole.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedRole.getPartition().getName());
    }

    /**
     * <p>
     * Loads from the LDAP tree an already stored role.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        Role storedRoleInstance = createIdentityType();

        assertNotNull(storedRoleInstance);

        IdentityManager identityManager = getIdentityManager();

        storedRoleInstance = identityManager.getRole(storedRoleInstance.getName());

        assertNotNull(storedRoleInstance);
        assertEquals("Administrator", storedRoleInstance.getName());
    }

    /**
     * <p>
     * Remove from the LDAP tree an already stored role.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        Role storedRoleInstance = createIdentityType();

        assertNotNull(storedRoleInstance);

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedRoleInstance);

        Role removedRoleInstance = identityManager.getRole(storedRoleInstance.getName());

        assertNull(removedRoleInstance);
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
