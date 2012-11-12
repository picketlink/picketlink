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
package org.picketlink.test.idm.jpa.schema;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.schema.internal.SimpleJPAIdentityStore;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * Tests the creation of memberships using the {@link SimpleJPAIdentityStore}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class JPAMembershipTestCase extends AbstractJPAIdentityManagerTestCase {

    /**
     * <p>
     * Tests the creation of an {@link Membership} with populating some basic attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test @Ignore // FIXME re-enable this after we fix IdentityManager
    public void testMembershipStore() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Role role = new SimpleRole("admin");
        identityManager.createRole(role);

        User user = new SimpleUser("asaldhan");
        identityManager.createUser(user);

        Group group = new SimpleGroup("Administrators", (Group) null);
        identityManager.createGroup(group);

        identityManager.grantRole(user, role, group);

        assertTrue(identityManager.hasRole(user, role, group));

        testRemoveGroup();
    }

    /**
     * <p>
     * Tests the remove of an {@link Membership}.
     * </p>
     *
     * @throws Exception
     */
    public void testRemoveGroup() throws Exception {
        IdentityStore identityStore = createIdentityStore();

        Role role = identityStore.getRole(null, "admin");
        User user = identityStore.getUser(null, "asaldhan");
        Group group = identityStore.getGroup(null, "Administrators");

        Membership membership = identityStore.getMembership(null, user, group, role);

        assertNotNull(membership);

        identityStore.removeMembership(null, user, group, role);

        membership = identityStore.getMembership(null, user, group, role);

        assertNull(membership);
    }

}