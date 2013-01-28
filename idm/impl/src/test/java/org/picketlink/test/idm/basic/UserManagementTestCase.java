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
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

/**
 * <p>
 * Test case for {@link User} basic management operations.
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
