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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the Query API when retrieving {@link User} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserQueryTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Find an {@link User} by id.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindById() throws Exception {
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.ID, "admin");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getId());
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
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");

        // find only by the first name
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getId());

        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.LAST_NAME, "Administrator");

        // find only by the last name
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.FIRST_NAME, "The");
        query.setParameter(User.LAST_NAME, "Administrator");

        // find by first and last names
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getId());

        query = getIdentityManager().<User> createQuery(User.class);

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
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.EMAIL, "admin@jboss.org");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getId());
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
        User user = getUser("someUser");
        
        user.setEnabled(true);
        
        getIdentityManager().update(user);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.ENABLED, true);
        
        // all enabled users
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());

        boolean match = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(user.getId())) {
                match = true;
            }
        }
        
        assertTrue(match);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.ENABLED, false);
        
        // only disabled users. No users are disabled.
        result = query.getResultList();

        assertTrue(result.isEmpty());
        
        user.setEnabled(false);
        
        // let's disabled the user and try to find him
        getIdentityManager().update(user);

        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.ENABLED, false);

        // get the previously disabled user
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals(user.getId(), result.get(0).getId());
    }

    private User getUser(String userName) {
        User user = new SimpleUser(userName);
        
        if (getIdentityManager().getUser(user.getId()) == null) {
            getIdentityManager().add(user);            
        }
        
        user = getIdentityManager().getUser(userName);
        return user;
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
        User user = getUser("someUser");
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.CREATED_DATE, user.getCreatedDate());
        
        // only the previously created user
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someUser", result.get(0).getId());
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.CREATED_DATE, new Date());
        
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
        User user = getUser("someUser");
        
        Date expirationDate = new Date();
        
        user = getIdentityManager().getUser("someUser");
        
        user.setExpirationDate(expirationDate);
        
        getIdentityManager().update(user);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(User.EXPIRY_DATE, user.getExpirationDate());
        
        // all expired users
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        
        boolean match = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(user.getId())) {
                match = true;
            }
        }
        
        assertTrue(match);
        
        assertEquals("someUser", result.get(0).getId());
        
        query = getIdentityManager().<User> createQuery(User.class);

        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.HOUR, 1);
        
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
        User someUser = getUser("someUser");
        User someAnotherUser = getUser("someAnotherUser");
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);
        
        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.YEAR, -1);
        
        // users between the given time period
        query.setParameter(User.CREATED_AFTER, calendar.getTime());
        query.setParameter(User.CREATED_BEFORE, new Date());
        
        Thread.sleep(500);
        
        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        
        boolean matchSomeUser = false;
        boolean matchAnotherUser = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        
        assertEquals("someUser", result.get(0).getId());
        assertEquals("someAnotherUser", result.get(1).getId());
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        User someFutureUser = getUser("someFutureUser");
        User someAnotherFutureUser = getUser("someAnotherFutureUser");

        // users created after the given time
        query.setParameter(User.CREATED_AFTER, calendar.getTime());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        boolean matchSomeFutureUser = false;
        boolean matchSomeAnotherFutureUser = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            } else if (resultUser.getId().equals(someFutureUser.getId())) {
                matchSomeFutureUser = true;
            } else if (resultUser.getId().equals(someAnotherFutureUser.getId())) {
                matchSomeAnotherFutureUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        assertTrue(matchSomeFutureUser);
        assertTrue(matchSomeAnotherFutureUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        // users created before the given time
        query.setParameter(User.CREATED_BEFORE, new Date());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());

        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            } else if (resultUser.getId().equals(someFutureUser.getId())) {
                matchSomeFutureUser = true;
            } else if (resultUser.getId().equals(someAnotherFutureUser.getId())) {
                matchSomeAnotherFutureUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        assertTrue(matchSomeFutureUser);
        assertTrue(matchSomeAnotherFutureUser);
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users created after the given time. Should return an empty list.
        query.setParameter(User.CREATED_AFTER, new Date());
        
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
        User someUser = getUser("someUser");
        
        someUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someUser);
        
        User someAnotherUser = getUser("someAnotherUser");
        
        someAnotherUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someAnotherUser);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);
        
        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.YEAR, -1);
        
        Date createdDate = calendar.getTime();
        
        // users between the given time period
        query.setParameter(User.EXPIRY_AFTER, createdDate);
        query.setParameter(User.EXPIRY_BEFORE, new Date());
        
        Thread.sleep(500);
        
        User someFutureUser = getUser("someFutureUser");
        
        someFutureUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someFutureUser);
        
        User someAnotherFutureUser = getUser("someAnotherFutureUser");
        
        someAnotherFutureUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someAnotherFutureUser);

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        
        boolean matchSomeUser = false;
        boolean matchAnotherUser = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users expired after the given time
        query.setParameter(User.EXPIRY_AFTER, createdDate);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());

        boolean matchSomeFutureUser = false;
        boolean matchSomeAnotherFutureUser = false;
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            } else if (resultUser.getId().equals(someFutureUser.getId())) {
                matchSomeFutureUser = true;
            } else if (resultUser.getId().equals(someAnotherFutureUser.getId())) {
                matchSomeAnotherFutureUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        assertTrue(matchSomeFutureUser);
        assertTrue(matchSomeAnotherFutureUser);
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users expired before the given time
        query.setParameter(User.EXPIRY_BEFORE, new Date());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        for (User resultUser : result) {
            if (resultUser.getId().equals(someUser.getId())) {
                matchSomeUser = true;
            } else if (resultUser.getId().equals(someAnotherUser.getId())) {
                matchAnotherUser = true;
            } else if (resultUser.getId().equals(someFutureUser.getId())) {
                matchSomeFutureUser = true;
            } else if (resultUser.getId().equals(someAnotherFutureUser.getId())) {
                matchSomeAnotherFutureUser = true;
            }
        }
        
        assertTrue(matchSomeUser);
        assertTrue(matchAnotherUser);
        assertTrue(matchSomeFutureUser);
        assertTrue(matchSomeAnotherFutureUser);
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users expired after the given time. Should return an empty list.
        query.setParameter(User.EXPIRY_AFTER, new Date());
        
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
    public void testFindByUserDefinedAttribute() throws Exception {
        User someUser = getUser("someUser");
        
        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));
        
        getIdentityManager().update(someUser);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("someUser", result.get(0).getId());
        
        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));
        
        getIdentityManager().update(someUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        
        result = query.getResultList();
        
        assertTrue(result.isEmpty());

        someUser.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));
        
        getIdentityManager().update(someUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
    }
}
