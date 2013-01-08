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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Role} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class RoleQueryTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Find an {@link Role} by id.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByName() throws Exception {
        loadOrCreateRole("admin", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.NAME, "admin");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getName());
    }

    /**
     * <p>
     * Finds roles with the enabled/disabled status.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindEnabledAndDisabledRoles() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);
        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);

        someRole.setEnabled(true);
        someAnotherRole.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);
        identityManager.update(someAnotherRole);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.ENABLED, true);

        // all enabled roles
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.ENABLED, false);

        // only disabled roles. No roles are disabled.
        result = query.getResultList();

        assertTrue(result.isEmpty());

        someRole.setEnabled(false);

        // let's disabled the role and try to find him
        identityManager.update(someRole);

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.ENABLED, false);

        // get the previously disabled role
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));
        assertFalse(contains(result, someAnotherRole.getName()));

        someAnotherRole.setEnabled(false);

        // let's disabled the role and try to find him
        identityManager.update(someAnotherRole);

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.ENABLED, true);

        result = query.getResultList();

        assertFalse(contains(result, someRole.getName()));
        assertFalse(contains(result, someAnotherRole.getName()));
    }

    /**
     * <p>
     * Finds roles by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindCreationDate() throws Exception {
        Role role = loadOrCreateRole("someRole", false);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.CREATED_DATE, role.getCreatedDate());
        
        Thread.sleep(500);
        
        // only the previously created role
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someRole", result.get(0).getName());

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.CREATED_DATE, new Date());

        // no roles
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles by the expiration date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindExpiryDate() throws Exception {
        Role role = loadOrCreateRole("someRole", true);

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        role = identityManager.getRole("someRole");

        role.setExpirationDate(expirationDate);

        identityManager.update(role);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.EXPIRY_DATE, role.getExpirationDate());

        // all expired roles
        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, role.getName()));

        assertEquals("someRole", result.get(0).getName());

        query = identityManager.<Role> createQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Role.EXPIRY_DATE, calendar.getTime());

        // no roles
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles created between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenCreationDate() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);
        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        // roles between the given time period
        query.setParameter(Role.CREATED_AFTER, calendar.getTime());
        query.setParameter(Role.CREATED_BEFORE, new Date());

        Thread.sleep(500);

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        Role someFutureRole = loadOrCreateRole("someFutureRole", true);
        Role someAnotherFutureRole = loadOrCreateRole("someAnotherFutureRole", true);

        // roles created after the given time
        query.setParameter(Role.CREATED_AFTER, calendar.getTime());
        
        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        // roles created before the given time
        query.setParameter(Role.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        Thread.sleep(1000);
        
        // roles created after the given time. Should return an empty list.
        query.setParameter(Role.CREATED_AFTER, new Date());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds roles using the IDM specific attributes and role defined attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        Role role = loadOrCreateRole("admin", true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(role);

        role.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(role);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, role.getName()));
        assertEquals(1, result.size());

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(Role.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }
    
    /**
     * <p>
     * Finds roles expired between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);

        someRole.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        Role someAnotherRole = loadOrCreateRole("someAnotherRole", true);

        someAnotherRole.setExpirationDate(new Date());

        identityManager.update(someAnotherRole);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        // roles between the given time period
        query.setParameter(Role.EXPIRY_AFTER, expiryDate);
        query.setParameter(Role.EXPIRY_BEFORE, new Date());

        Thread.sleep(1000);

        Role someFutureRole = loadOrCreateRole("someFutureRole", true);

        someFutureRole.setExpirationDate(new Date());

        identityManager.update(someFutureRole);

        Role someAnotherFutureRole = loadOrCreateRole("someAnotherFutureRole", true);

        someAnotherFutureRole.setExpirationDate(new Date());

        identityManager.update(someAnotherFutureRole);

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        // roles expired after the given time
        query.setParameter(Role.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        // roles expired before the given time
        query.setParameter(Role.EXPIRY_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someRole.getName()));
        assertTrue(contains(result, someAnotherRole.getName()));
        assertTrue(contains(result, someFutureRole.getName()));
        assertTrue(contains(result, someAnotherFutureRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);
        
        Calendar futureExpiryDate = Calendar.getInstance();
        
        futureExpiryDate.add(Calendar.MINUTE, 1);
        
        // roles expired after the given time. Should return an empty list.
        query.setParameter(Role.EXPIRY_AFTER, futureExpiryDate.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link Role} by looking its attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByRoleDefinedAttributes() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);

        someRole.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));

        someRole.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someRole);

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(contains(result, someRole.getName()));

        someRole.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someRole);

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));
    }

    /**
     * <p>
     * Find an {@link Role} by looking its multi-valued attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByRoleDefinedMultiValuedAttributes() throws Exception {
        Role someRole = loadOrCreateRole("someRole", true);

        someRole.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someRole);

        IdentityQuery<Role> query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someRole.getName()));

        someRole.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someRole.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someRole);

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someRole.getName()));

        query = identityManager.<Role> createQuery(Role.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    private boolean contains(List<Role> result, String roleId) {
        for (Role resultRole : result) {
            if (resultRole.getName().equals(roleId)) {
                return true;
            }
        }

        return false;
    }
}
