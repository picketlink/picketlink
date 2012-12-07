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

        assertTrue(contains(result, user.getId()));
        
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
        
        assertTrue(contains(result, user.getId()));
        
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
        
        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        User someFutureUser = getUser("someFutureUser");
        User someAnotherFutureUser = getUser("someAnotherFutureUser");

        // users created after the given time
        query.setParameter(User.CREATED_AFTER, calendar.getTime());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        assertTrue(contains(result, someFutureUser.getId()));
        assertTrue(contains(result, someAnotherFutureUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);

        // users created before the given time
        query.setParameter(User.CREATED_BEFORE, new Date());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        assertTrue(contains(result, someFutureUser.getId()));
        assertTrue(contains(result, someAnotherFutureUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users created after the given time. Should return an empty list.
        query.setParameter(User.CREATED_AFTER, new Date());
        
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
        User user = getUser("admin");
        
        user.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));
        
        getIdentityManager().update(user);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);
        
        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        
        List<User> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");
        
        result = query.getResultList();
        
        assertTrue(result.isEmpty());
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        query.setParameter(User.EMAIL, "admin@jboss.org");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(User.FIRST_NAME, "The");
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        query = getIdentityManager().<User> createQuery(User.class);
        
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
        User someUser = getUser("someUser");
        
        someUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someUser);
        
        User someAnotherUser = getUser("someAnotherUser");
        
        someAnotherUser.setExpirationDate(new Date());
        
        getIdentityManager().update(someAnotherUser);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);
        
        Calendar calendar = Calendar.getInstance();
        
        calendar.add(Calendar.YEAR, -1);
        
        Date expiryDate = calendar.getTime();
        
        // users between the given time period
        query.setParameter(User.EXPIRY_AFTER, expiryDate);
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
        
        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users expired after the given time
        query.setParameter(User.EXPIRY_AFTER, expiryDate);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());

        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        assertTrue(contains(result, someFutureUser.getId()));
        assertTrue(contains(result, someAnotherFutureUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);
        
        // users expired before the given time
        query.setParameter(User.EXPIRY_BEFORE, new Date());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        
        assertTrue(contains(result, someUser.getId()));
        assertTrue(contains(result, someAnotherUser.getId()));
        assertTrue(contains(result, someFutureUser.getId()));
        assertTrue(contains(result, someAnotherFutureUser.getId()));
        
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
    public void testFindByUserDefinedAttributes() throws Exception {
        User someUser = getUser("someUser");
        
        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));
        
        getIdentityManager().update(someUser);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getId()));

        someUser.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));
        
        getIdentityManager().update(someUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        
        result = query.getResultList();
        
        assertFalse(contains(result, someUser.getId()));

        someUser.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));
        
        getIdentityManager().update(someUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getId()));
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
        User someUser = getUser("someUser");
        
        someUser.setAttribute(new Attribute<String[]>("someAttribute", new String[] {"someAttributeValue1", "someAttributeValue2"}));
        
        getIdentityManager().update(someUser);
        
        IdentityQuery<User> query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] {"someAttributeValue1", "someAttributeValue2"});

        List<User> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] {"someAttributeValue1", "someAttributeValue2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] {"someAttributeValueChanged", "someAttributeValue2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] {"someAttributeValue"});

        result = query.getResultList();

        assertFalse(contains(result, someUser.getId()));
        
        someUser.setAttribute(new Attribute<String[]>("someAttribute", new String[] {"someAttributeValue1", "someAttributeValueChanged"}));
        someUser.setAttribute(new Attribute<String[]>("someAttribute2", new String[] {"someAttribute2Value1", "someAttribute2Value2"}));
        
        getIdentityManager().update(someUser);
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] {"someAttributeValue1", "someAttributeValueChanged"});
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] {"someAttribute2Value1", "someAttribute2Value2"});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someUser.getId()));
        
        query = getIdentityManager().<User> createQuery(User.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] {"someAttributeValue1", "someAttributeValueChanged"});
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] {"someAttribute2ValueChanged", "someAttribute2Value2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    private boolean contains(List<User> result, String userId) {
        for (User resultUser : result) {
            if (resultUser.getId().equals(userId)) {
                return true;
            }
        }        
        
        return false;
    }
}
