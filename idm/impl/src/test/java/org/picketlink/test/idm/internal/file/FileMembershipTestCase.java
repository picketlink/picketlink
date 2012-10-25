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
package org.picketlink.test.idm.internal.file;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Tests the creation of memberships using the {@link FileBasedIdentityStore}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class FileMembershipTestCase extends AbstractFileIdentityManagerTestCase {

    /**
     * <p>
     * Tests the creation of an {@link Membership} with populating some basic attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantRole() throws Exception {
        IdentityManager identityStore = getIdentityManager();

        Role role = identityStore.createRole("admin");
        User user = identityStore.createUser("asaldhan");
        Group group = identityStore.createGroup("Administrators", (Group) null);

        identityStore.grantRole(role, user, group);
        
        Collection<Role> roles = getIdentityManager().getRoles(null, group);
        
        Assert.assertNotNull(roles);
        Assert.assertFalse(roles.isEmpty());
        
        User anotherUser = identityStore.createUser("anotherUser");
        
        roles = getIdentityManager().getRoles(anotherUser, null);
        
        Assert.assertNotNull(roles);
        Assert.assertTrue(roles.isEmpty());
    }

}