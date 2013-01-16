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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Test case for {@link User} basic management operations.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserManagementTestCase extends AbstractIdentityTypeTestCase<User> {

    /**
     * <p>
     * Creates a new {@link User} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        User newUserInstance = createUser("jduke");

        newUserInstance.setEmail("jduke@jboss.org");
        newUserInstance.setFirstName("Java");
        newUserInstance.setLastName("Duke");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(newUserInstance);

        // let's retrieve the user information and see if they are properly stored
        User storedUserInstance = identityManager.getUser(newUserInstance.getLoginName());

        assertNotNull(storedUserInstance);

        assertEquals(newUserInstance.getLoginName(), storedUserInstance.getLoginName());
        assertEquals(newUserInstance.getFirstName(), storedUserInstance.getFirstName());
        assertEquals(newUserInstance.getLastName(), storedUserInstance.getLastName());
        assertEquals(newUserInstance.getEmail(), storedUserInstance.getEmail());
        assertTrue(storedUserInstance.isEnabled());
        assertTrue(new Date().compareTo(storedUserInstance.getCreatedDate()) > 0);
    }

    @Test
    public void testGet() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        User storedUserInstance = createUser("admin");
        
        storedUserInstance.setEmail("admin@jboss.org");
        storedUserInstance.setFirstName("The");
        storedUserInstance.setLastName("Administrator");

        identityManager.update(storedUserInstance);

        storedUserInstance = identityManager.getUser(storedUserInstance.getLoginName());

        assertNotNull(storedUserInstance);

        assertEquals("admin", storedUserInstance.getLoginName());
        assertEquals("The", storedUserInstance.getFirstName());
        assertEquals("Administrator", storedUserInstance.getLastName());
        assertEquals("admin@jboss.org", storedUserInstance.getEmail());
    }

    /**
     * <p>
     * Updates the stored user information.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        User storedUserInstance = createUser("admin");
        
        storedUserInstance.setEmail("admin@jboss.org");
        storedUserInstance.setFirstName("The");
        storedUserInstance.setLastName("Administrator");

        identityManager.update(storedUserInstance);
        
        storedUserInstance = identityManager.getUser(storedUserInstance.getLoginName());
        
        assertEquals("admin", storedUserInstance.getLoginName());
        assertEquals("The", storedUserInstance.getFirstName());
        assertEquals("Administrator", storedUserInstance.getLastName());
        assertEquals("admin@jboss.org", storedUserInstance.getEmail());

        // let's update some user information
        storedUserInstance.setFirstName("Updated " + storedUserInstance.getFirstName());
        storedUserInstance.setLastName("Updated " + storedUserInstance.getLastName());
        storedUserInstance.setEmail("Updated " + storedUserInstance.getEmail());

        identityManager.update(storedUserInstance);

        // let's load again the user from the store and check for the updated information
        User updatedUser = identityManager.getUser(storedUserInstance.getLoginName());

        assertEquals("Updated The", updatedUser.getFirstName());
        assertEquals("Updated Administrator", updatedUser.getLastName());
        assertEquals("Updated admin@jboss.org", updatedUser.getEmail());

    }

    /**
     * <p>
     * Remove from the LDAP tree an already stored user.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User someUser = createUser("admin");
        User anotherUser = createUser("someAnotherUser");

        assertNotNull(someUser);
        assertNotNull(anotherUser);

        identityManager.remove(someUser);

        User removedUserInstance = getIdentityManager().getUser(someUser.getLoginName());

        assertNull(removedUserInstance);
        
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
