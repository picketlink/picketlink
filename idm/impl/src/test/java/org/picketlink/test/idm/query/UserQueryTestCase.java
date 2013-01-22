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

package org.picketlink.test.idm.query;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for the Query API when retrieving {@link User} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserQueryTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testFindById() throws Exception {
        User user = createUser("someUser");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.ID, user.getId());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }
    
    @Test
    public void testPagination() throws Exception {
        for (int i = 0; i < 50; i++) {
            createUser("someUser" + i + 1);
        }
        
        IdentityManager identityManager = getIdentityManager();
        
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);
        
        query.setLimit(10);
        query.setOffset(0);
        
        int resultCount = query.getResultCount();
        
        assertEquals(50, resultCount);
        
        List<User> firstPage = query.getResultList();
        
        assertEquals(10, firstPage.size());
        
        List<String> userIds = new ArrayList<String>();
        
        for (User user : firstPage) {
            userIds.add(user.getId());
        }
        
        query.setOffset(10);
        
        List<User> secondPage = query.getResultList();
        
        assertEquals(10, secondPage.size());
        
        for (User user : secondPage) {
            assertFalse(userIds.contains(user.getId()));
            userIds.add(user.getId());
        }
        
        query.setOffset(20);
        
        List<User> thirdPage = query.getResultList();
        
        assertEquals(10, thirdPage.size());
        
        for (User user : thirdPage) {
            assertFalse(userIds.contains(user.getId()));
            userIds.add(user.getId());
        }
        
        query.setOffset(30);
        
        List<User> fourthPage = query.getResultList();
        
        assertEquals(10, fourthPage.size());
        
        for (User user : fourthPage) {
            assertFalse(userIds.contains(user.getId()));
            userIds.add(user.getId());
        }
        
        query.setOffset(40);
        
        List<User> fifthyPage = query.getResultList();
        
        assertEquals(10, fifthyPage.size());
        
        for (User user : fifthyPage) {
            assertFalse(userIds.contains(user.getId()));
            userIds.add(user.getId());
        }
        
        query.setOffset(50);
        
        List<User> invalidPage = query.getResultList();
        
        assertEquals(0, invalidPage.size());
    }
    
    @Test
    public void testFindByRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        User someUserDefaultRealm = new SimpleUser("someUserRealm");
        
        identityManager.add(someUserDefaultRealm);
        
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);
        
        Realm defaultRealm = identityManager.getRealm(Realm.DEFAULT_REALM);
        
        assertNotNull(defaultRealm);
        
        query.setParameter(User.PARTITION, defaultRealm);
        
        List<User> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUserDefaultRealm.getLoginName()));
        
        Realm testingRealm = identityManager.getRealm("Testing");
        
        if (testingRealm == null) {
            testingRealm = new Realm("Testing");
            identityManager.createRealm(testingRealm);
        }
        
        User someUserTestingRealm = new SimpleUser("someUserTestingRealm");
        
        identityManager.forRealm(testingRealm).add(someUserTestingRealm);
        
        query = identityManager.createIdentityQuery(User.class);
        
        query.setParameter(User.PARTITION, testingRealm);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertFalse(contains(result, someUserDefaultRealm.getLoginName()));
        assertTrue(contains(result, someUserTestingRealm.getLoginName()));
    }
    
    @Test
    public void testFindByLoginName() throws Exception {
        createUser("admin");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.LOGIN_NAME, "admin");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by first name.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByFirstNameAndLastName() throws Exception {
        User user = createUser("admin");

        user.setFirstName("The");
        user.setLastName("Administrator");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(user);

        assertNotNull(user);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");

        // find only by the first name
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.LAST_NAME, "Administrator");

        // find only by the last name
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");
        query.setParameter(User.LAST_NAME, "Administrator");

        // find by first and last names
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getLoginName());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");
        query.setParameter(User.LAST_NAME, "Bad Administrator");

        // must not return any result because we provided a invalid last name
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link User} by email.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByEmail() throws Exception {
        User user = createUser("admin");

        user.setEmail("admin@jboss.org");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(user);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getLoginName());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "badadmin@jboss.org");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group} and {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroupRole() throws Exception {
        User user = createUser("someUser");
        Group salesGroup = createGroup("Sales", null);
        Role managerRole = createRole("Manager");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_GROUP_ROLE, new GroupRole(user, salesGroup, managerRole));

        List<User> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantGroupRole(user, managerRole, salesGroup);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_GROUP_ROLE, new GroupRole(user, salesGroup, managerRole));

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBySingleGroup() throws Exception {
        User user = createUser("admin");
        Group administratorGroup = createGroup("Administrators", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, "Administrators");

        List<User> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.addToGroup(user, administratorGroup);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, "Administrators");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBySingleRole() throws Exception {
        User user = createUser("admin");
        Role administratorRole = createRole("Administrators");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, "Administrators");

        List<User> result = query.getResultList();

        assertTrue(result.isEmpty());

        identityManager.grantRole(user, administratorRole);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, "Administrators");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByMultipleGroups() throws Exception {
        User user = createUser("admin");
        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(user, administratorGroup);
        identityManager.addToGroup(user, someGroup);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());

        identityManager.removeFromGroup(user, someGroup);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, administratorGroup.getName());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByMultipleRoles() throws Exception {
        User user = createUser("admin");
        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(user, administratorRole);
        identityManager.grantRole(user, someRole);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());

        identityManager.revokeRole(user, someRole);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, administratorRole.getName());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(user.getLoginName(), result.get(0).getLoginName());
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Group}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByMultipleUserWithGroups() throws Exception {
        User adminUser = createUser("admin");
        User someUser = createUser("someUser");

        Group administratorGroup = createGroup("Administrators", null);
        Group someGroup = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.addToGroup(adminUser, administratorGroup);
        identityManager.addToGroup(someUser, administratorGroup);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, administratorGroup.getName());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminUser.getLoginName()));
        assertTrue(contains(result, someUser.getLoginName()));

        identityManager.addToGroup(adminUser, someGroup);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.MEMBER_OF, administratorGroup.getName(), someGroup.getName());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminUser.getLoginName()));

        assertFalse(contains(result, someUser.getLoginName()));
    }

    /**
     * <p>
     * Find an {@link User} by his associated {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByMultipleUserWithRoles() throws Exception {
        User adminUser = createUser("admin");
        User someUser = createUser("someUser");

        Role administratorRole = createRole("Administrators");
        Role someRole = createRole("someRole");

        IdentityManager identityManager = getIdentityManager();

        identityManager.grantRole(adminUser, administratorRole);
        identityManager.grantRole(someUser, administratorRole);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, administratorRole.getName());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminUser.getLoginName()));
        assertTrue(contains(result, someUser.getLoginName()));

        identityManager.grantRole(adminUser, someRole);

        query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.HAS_ROLE, administratorRole.getName(), someRole.getName());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, adminUser.getLoginName()));
        assertFalse(contains(result, someUser.getLoginName()));
    }

    /**
     * <p>
     * Finds users with the enabled/disabled status.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindEnabledAndDisabledUsers() throws Exception {
        User someUser = createUser("someUser");
        User someAnotherUser = createUser("someAnotherUser");

        someUser.setEnabled(true);
        someAnotherUser.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someUser);
        identityManager.update(someAnotherUser);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.ENABLED, true);

        // all enabled users
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.ENABLED, false);

        // only disabled users. No users are disabled.
        result = query.getResultList();

        assertTrue(result.isEmpty());

        someUser.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someUser);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.ENABLED, false);

        // get the previously disabled user
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));
        assertFalse(contains(result, someAnotherUser.getLoginName()));

        someAnotherUser.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAnotherUser);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.ENABLED, true);

        result = query.getResultList();

        assertFalse(contains(result, someUser.getLoginName()));
        assertFalse(contains(result, someAnotherUser.getLoginName()));
    }

    /**
     * <p>
     * Finds users by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindCreationDate() throws Exception {
        User user = createUser("someUser");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.CREATED_DATE, user.getCreatedDate());

        // only the previously created user
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someUser", result.get(0).getLoginName());

        query = identityManager.<User> createIdentityQuery(User.class);
        
        Calendar futureDate = Calendar.getInstance();

        futureDate.add(Calendar.MINUTE, 1);
        
        query.setParameter(User.CREATED_DATE, futureDate.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users by the expiration date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindExpiryDate() throws Exception {
        User user = createUser("someUser");

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        user = identityManager.getUser("someUser");

        user.setExpirationDate(expirationDate);

        identityManager.update(user);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EXPIRY_DATE, user.getExpirationDate());

        // all expired users
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, user.getLoginName()));

        assertEquals("someUser", result.get(0).getLoginName());

        query = identityManager.<User> createIdentityQuery(User.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        query.setParameter(User.EXPIRY_DATE, calendar.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users created between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenCreationDate() throws Exception {
        User someUser = createUser("someUser");
        User someAnotherUser = createUser("someAnotherUser");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        // users between the given time period
        query.setParameter(User.CREATED_AFTER, calendar.getTime());
        query.setParameter(User.CREATED_BEFORE, new Date());

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        User someFutureUser = createUser("someFutureUser");
        User someAnotherFutureUser = createUser("someAnotherFutureUser");

        // users created after the given time
        query.setParameter(User.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));
        assertTrue(contains(result, someFutureUser.getLoginName()));
        assertTrue(contains(result, someAnotherFutureUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        // users created before the given time
        query.setParameter(User.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));
        assertTrue(contains(result, someFutureUser.getLoginName()));
        assertTrue(contains(result, someAnotherFutureUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        Calendar futureDate = Calendar.getInstance();
        
        futureDate.add(Calendar.MINUTE, 1);
        
        // Should return an empty list.
        query.setParameter(User.CREATED_AFTER, futureDate.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users using the IDM specific attributes and user defined attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        User user = createUser("admin");

        user.setEmail("admin@jboss.org");
        user.setFirstName("The");
        user.setLastName("Administrator");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(user);

        user.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(user);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));
        assertEquals(1, result.size());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(User.FIRST_NAME, "The");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, user.getLoginName()));
        assertEquals(1, result.size());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(User.FIRST_NAME, "Bad First Name");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds users expired between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        User someUser = createUser("someUser");

        someUser.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someUser);

        User someAnotherUser = createUser("someAnotherUser");

        someAnotherUser.setExpirationDate(new Date());

        identityManager.update(someAnotherUser);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        // users between the given time period
        query.setParameter(User.EXPIRY_AFTER, expiryDate);
        query.setParameter(User.EXPIRY_BEFORE, new Date());

        User someFutureUser = createUser("someFutureUser");

        someFutureUser.setExpirationDate(new Date());

        identityManager.update(someFutureUser);

        User someAnotherFutureUser = createUser("someAnotherFutureUser");

        someAnotherFutureUser.setExpirationDate(new Date());

        identityManager.update(someAnotherFutureUser);

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        // users expired after the given time
        query.setParameter(User.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));
        assertTrue(contains(result, someFutureUser.getLoginName()));
        assertTrue(contains(result, someAnotherFutureUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        // users expired before the given time
        query.setParameter(User.EXPIRY_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getLoginName()));
        assertTrue(contains(result, someAnotherUser.getLoginName()));
        assertTrue(contains(result, someFutureUser.getLoginName()));
        assertTrue(contains(result, someAnotherFutureUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);
        
        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 2);

        // users expired after the given time. Should return an empty list.
        query.setParameter(Agent.EXPIRY_AFTER, calendar.getTime());
        
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link User} by looking its attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByUserDefinedAttributes() throws Exception {
        User someUser = createUser("someUser");

        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someUser);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));

        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someUser);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(contains(result, someUser.getLoginName()));

        someUser.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someUser);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));
    }

    /**
     * <p>
     * Find an {@link User} by looking its multi-valued attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByUserDefinedMultiValuedAttributes() throws Exception {
        User someUser = createUser("someUser");

        someUser.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someUser);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someUser.getLoginName()));

        someUser.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someUser.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someUser);

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getLoginName()));

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    private boolean contains(List<User> result, String loginName) {
        for (User resultUser : result) {
            if (resultUser.getLoginName().equals(loginName)) {
                return true;
            }
        }

        return false;
    }
}
