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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.UserQuery;

/**
 * <p>
 * Tests the query support for {@link User} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class JPAUserQueryTestCase extends AbstractJPAIdentityManagerTestCase {

    private static final String GROUP_NAME_PREFIX = "Administrators";
    private static final String ROLE_NAME_PREFIX = "admin";
    private static final String USER_EMAIL = "myemail@company.com";
    private static final String USER_LAST_NAME = "Saldhana";
    private static final String USER_FIRST_NAME = "Anil";
    private static final String USER_USERNAME = "asaldhana";
    private User user;

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.test.idm.internal.jpa.AbstractJPAIdentityStoreTestCase#onSetupTest()
     */
    @Override
    @Before
    public void onSetupTest() throws Exception {
        super.onSetupTest();
        loadUsers();
    }

    /**
     * <p>
     * Tests a simple query using the username property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByUserName() throws Exception {
        UserQuery query = createUserQuery();

        query.setName(this.user.getKey());

        assertQueryResult(query);
        
        query.setName("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the firstName property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByFirstName() throws Exception {
        UserQuery query = createUserQuery();

        query.setFirstName(this.user.getFirstName());

        assertQueryResult(query);
        
        query.setFirstName("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the lastName property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByLastName() throws Exception {
        UserQuery query = createUserQuery();

        query.setLastName(this.user.getLastName());

        assertQueryResult(query);
        
        query.setLastName("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the email property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByEmail() throws Exception {
        UserQuery query = createUserQuery();

        query.setEmail(this.user.getEmail());

        assertQueryResult(query);
        
        query.setEmail("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the role property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByRole() throws Exception {
        UserQuery query = createUserQuery();

        query.setRole(ROLE_NAME_PREFIX + 1);

        assertQueryResult(query);
        
        query.setRole("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the group property.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByGroup() throws Exception {
        UserQuery query = createUserQuery();

        query.setRelatedGroup(GROUP_NAME_PREFIX + 1);

        assertQueryResult(query);
        
        query.setRelatedGroup("Invalid");
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Tests a simple query using the user's attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testfindByAttributes() throws Exception {
        UserQuery query = createUserQuery();

        query.setName(this.user.getKey());
        query.setAttributeFilter("attribute1", new String[] { "attributeValue1", "attributeValue12", "attributeValue123" });
        query.setAttributeFilter("attribute2", new String[] { "attributeValue2", "attributeValue21", "attributeValue23" });

        assertQueryResult(query);
        
        query.setAttributeFilter("Invalid", new String[] {"Invalid"});
        
        assertTrue(query.executeQuery().isEmpty());
    }

    /**
     * <p>
     * Asserts if the result returned by the specified {@link UserQuery} match the expected values.
     * </p>
     *
     * @param query
     */
    private void assertQueryResult(UserQuery query) {
        List<User> result = query.executeQuery();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(this.user.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Create and persist a {@link User} instance for testing.
     * </p>
     */
    private void loadUsers() {
        IdentityManager identityManager = getIdentityManager();

        this.user = identityManager.getUser(USER_USERNAME + 1);

        // if users are already loaded then do nothing
        if (this.user != null) {
            return;
        }

        for (int i = 0; i < 10; i++) {
            int index = i + 1;
            User currentUser = identityManager.createUser(USER_USERNAME + index);

            // store the instance used for testing
            if (this.user == null) {
                this.user = currentUser;
            }

            currentUser.setEmail(USER_EMAIL + index);
            currentUser.setFirstName(USER_FIRST_NAME + index);
            currentUser.setLastName(USER_LAST_NAME + index);

            Role role = identityManager.createRole(ROLE_NAME_PREFIX + index);
            Group group = identityManager.createGroup(GROUP_NAME_PREFIX + index, (Group) null);

            identityManager.grantRole(role, user, group);

            currentUser.setAttribute("attribute1", "attributeValue1");
            currentUser.setAttribute("attribute1", "attributeValue12");
            currentUser.setAttribute("attribute1", "attributeValue123");

            currentUser.setAttribute("attribute2", "attributeValue2");
        }
    }

    private UserQuery createUserQuery() {
        return getIdentityManager().createUserQuery();
    }

}