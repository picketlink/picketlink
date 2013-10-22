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

package org.picketlink.test.idm.query;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * <p>
 * Test case for the Query API when retrieving {@link User} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
public class UserQueryTestCase extends AgentQueryTestCase<User> {

    public UserQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected User createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someUser";
        }

        return createUser(name, partition);
    }

    @Override
    protected User getIdentityType() {
        return getUser("someUser");
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
        User admin = createUser("admin");

        admin.setFirstName("The");
        admin.setLastName("Administrator");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(admin);

        assertNotNull(admin);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");

        // find only by the first name
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, admin.getId()));

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

        assertEquals(admin.getId(), result.get(0).getId());

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
        User admin = createUser("admin");

        admin.setEmail("admin@jboss.org");

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(admin);

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals(admin.getId(), result.get(0).getId());

        query = identityManager.<User> createIdentityQuery(User.class);

        query.setParameter(User.EMAIL, "badadmin@jboss.org");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Ignore
    public void testFindWithPaginationAndSorting() throws Exception {
        createPopulatedUser("john", "John", "Anthony");
        // Sleep is needed to avoid same createdDate
        Thread.sleep(1000);
        createPopulatedUser("root", "Root", "Root");
        Thread.sleep(1000);
        createPopulatedUser("mary", "Mary", "Kelly");
        Thread.sleep(1000);
        createPopulatedUser("demo", "Demo", "Demo");
        Thread.sleep(1000);
        createPopulatedUser("mary2", "Mary", "Anthony");
        Thread.sleep(1000);
        createPopulatedUser("john2", "John", "Kelly");

        // Page1 with default sorting (loginName)
        IdentityQuery<User> userQuery = getIdentityManager().createIdentityQuery(User.class);

        userQuery.setSortParameters(User.LOGIN_NAME);

        userQuery.setOffset(0);
        userQuery.setLimit(5);
        List<User> users = userQuery.getResultList();

        assertEquals(5, users.size());
        assertEquals(users.get(0).getLoginName(), "demo");
        assertEquals(users.get(1).getLoginName(), "john");
        assertEquals(users.get(2).getLoginName(), "john2");
        assertEquals(users.get(3).getLoginName(), "mary");
        assertEquals(users.get(4).getLoginName(), "mary2");

        // Page2 with default sorting (loginName)
        userQuery = getIdentityManager().createIdentityQuery(User.class);

        userQuery.setSortParameters(User.LOGIN_NAME);

        userQuery.setOffset(5);
        userQuery.setLimit(5);
        users = userQuery.getResultList();

        assertEquals(1, users.size());
        assertEquals(users.get(0).getLoginName(), "root");

        // Sorting by lastName and firstName
        userQuery = getIdentityManager().createIdentityQuery(User.class);
        userQuery.setOffset(0);
        userQuery.setLimit(5);
        userQuery.setSortParameters(User.LAST_NAME, User.FIRST_NAME);
        users = userQuery.getResultList();

        assertEquals(5, users.size());
        assertEquals(users.get(0).getLoginName(), "john");
        assertEquals(users.get(1).getLoginName(), "mary2");
        assertEquals(users.get(2).getLoginName(), "demo");
        assertEquals(users.get(3).getLoginName(), "john2");
        assertEquals(users.get(4).getLoginName(), "mary");

        // Sort by creation date
        userQuery = getIdentityManager().createIdentityQuery(User.class);
        userQuery.setOffset(0);
        userQuery.setLimit(3);
        userQuery.setSortParameters(IdentityType.CREATED_DATE);
        users = userQuery.getResultList();

        assertEquals(3, users.size());
        assertEquals(users.get(0).getLoginName(), "john");
        assertEquals(users.get(1).getLoginName(), "root");
        assertEquals(users.get(2).getLoginName(), "mary");
    }

    private void createPopulatedUser(String username, String firstName, String lastName) {
        User user = createUser(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        getIdentityManager().update(user);
    }
}
