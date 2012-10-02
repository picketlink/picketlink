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
package org.picketlink.test.idm.internal.jpa;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.jpa.DatabaseUser;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Tests the creation of users using the {@link JPAIdentityManager}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class JPAUserTestCase extends AbstractJPAIdentityTypeTestCase {

    private static final String USER_EMAIL = "myemail@company.com";
    private static final String USER_LAST_NAME = "Saldhana";
    private static final String USER_FIRST_NAME = "Anil";
    private static final String USER_FULL_NAME = "Anil Saldhana";
    private static final String USER_USERNAME = "asaldhana";

    /**
     * <p>
     * Tests the creation of an {@link User} with populating some basic attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testUserStore() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User user = identityManager.createUser(USER_USERNAME);

        user.setEmail(USER_EMAIL);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);

        assertUserBasicInformation(user);

        testAddAttributes();

        testGetUser();

        testRemoveUser();
    }
    
    /**
     * <p>
     * Tests the creation of an {@link User} with populating some basic attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testSimpleUserStore() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User user = new DatabaseUser(USER_USERNAME);

        user.setEmail(USER_EMAIL);
        user.setFirstName(USER_FIRST_NAME);
        user.setLastName(USER_LAST_NAME);

        user = identityManager.createUser(user);

        assertUserBasicInformation(user);

        testAddAttributes();

        testGetUser();

        testRemoveUser();
    }

    /**
     * <p>
     * Tests if the user was properly created by retrieving him from the database.
     * </p>
     *
     * @throws Exception
     */
    public void testGetUser() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User user = identityManager.getUser(USER_USERNAME);

        assertUserBasicInformation(user);

        testRemoveAttributes();
    }

    /**
     * <p>
     * Tests the removal of users.
     * </p>
     *
     * @throws Exception
     */
    public void testRemoveUser() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User user = identityManager.getUser(USER_USERNAME);

        assertNotNull(user);

        identityManager.removeUser(user);

        user = identityManager.getUser(USER_USERNAME);

        assertNull(user);
    }

    /**
     * <p>
     * Asserts if the {@link User} is populated with the expected values.
     * </p>
     *
     * @param user
     */
    private void assertUserBasicInformation(User user) {
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals(USER_USERNAME, user.getKey());
        assertEquals(USER_FULL_NAME, user.getFullName());
        assertEquals(USER_FIRST_NAME, user.getFirstName());
        assertEquals(USER_LAST_NAME, user.getLastName());
        assertEquals(USER_EMAIL, user.getEmail());
    }

    @Override
    protected IdentityType getIdentityTypeFromDatabase(IdentityManager identityManager) {
        return identityManager.getUser(USER_USERNAME);
    }

}