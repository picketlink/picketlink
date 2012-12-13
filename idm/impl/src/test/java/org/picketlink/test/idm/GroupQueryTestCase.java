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
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Group} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupQueryTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Find an {@link Group} by id.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByName() throws Exception {
        loadOrCreateGroup("admin", null, true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.NAME, "admin");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);

        assertEquals("admin", result.get(0).getName());
    }

    /**
     * <p>
     * Finds groups with the enabled/disabled status.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindEnabledAndDisabledGroups() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        someGroup.setEnabled(true);
        someAnotherGroup.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);
        identityManager.update(someAnotherGroup);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.ENABLED, true);

        // all enabled groups
        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.ENABLED, false);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        someGroup.setEnabled(false);

        identityManager.update(someGroup);

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.ENABLED, false);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));
        assertFalse(contains(result, someAnotherGroup.getName()));

        someAnotherGroup.setEnabled(false);

        identityManager.update(someAnotherGroup);

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.ENABLED, true);

        result = query.getResultList();

        assertFalse(contains(result, someGroup.getName()));
        assertFalse(contains(result, someAnotherGroup.getName()));
    }

    /**
     * <p>
     * Finds groups by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindWithParent() throws Exception {
        Group group = loadOrCreateGroup("someGroup", "Parent Group", true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.PARENT, group.getParentGroup().getName());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals(group.getName(), result.get(0).getName());
        assertEquals(group.getParentGroup().getName(), result.get(0).getParentGroup().getName());
    }

    /**
     * <p>
     * Finds groups by the creation date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindCreationDate() throws Exception {
        Group group = loadOrCreateGroup("someGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.CREATED_DATE, group.getCreatedDate());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
        assertEquals("someGroup", result.get(0).getName());

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.CREATED_DATE, new Date());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds groups by the expiration date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindExpiryDate() throws Exception {
        Group group = loadOrCreateGroup("someGroup", null, true);

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        group = identityManager.getGroup("someGroup");

        group.setExpirationDate(expirationDate);

        identityManager.update(group);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.EXPIRY_DATE, group.getExpirationDate());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, group.getName()));

        assertEquals("someGroup", result.get(0).getName());

        query = identityManager.<Group> createQuery(Group.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.HOUR, 1);

        query.setParameter(Group.EXPIRY_DATE, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds groups created between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenCreationDate() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);
        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        query.setParameter(Group.CREATED_AFTER, calendar.getTime());
        query.setParameter(Group.CREATED_BEFORE, new Date());

        Thread.sleep(500);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        Group someFutureGroup = loadOrCreateGroup("someFutureGroup", null, true);
        Group someAnotherFutureGroup = loadOrCreateGroup("someAnotherFutureGroup", null, true);

        query.setParameter(Group.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.CREATED_AFTER, new Date());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds groups using the IDM specific attributes and group defined attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindUsingMultipleParameters() throws Exception {
        Group group = loadOrCreateGroup("admin", null, true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(group);

        group.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(group);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, group.getName()));
        assertEquals(1, result.size());

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Finds groups expired between a specific date.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindBetweenExpirationDate() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        someGroup.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        Group someAnotherGroup = loadOrCreateGroup("someAnotherGroup", null, true);

        someAnotherGroup.setExpirationDate(new Date());

        identityManager.update(someAnotherGroup);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        query.setParameter(Group.EXPIRY_AFTER, expiryDate);
        query.setParameter(Group.EXPIRY_BEFORE, new Date());

        Thread.sleep(500);

        Group someFutureGroup = loadOrCreateGroup("someFutureGroup", null, true);

        someFutureGroup.setExpirationDate(new Date());

        identityManager.update(someFutureGroup);

        Group someAnotherFutureGroup = loadOrCreateGroup("someAnotherFutureGroup", null, true);

        someAnotherFutureGroup.setExpirationDate(new Date());

        identityManager.update(someAnotherFutureGroup);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.EXPIRY_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());

        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(Group.EXPIRY_AFTER, new Date());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    /**
     * <p>
     * Find an {@link Group} by looking its attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByGroupDefinedAttributes() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        someGroup.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));

        someGroup.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someGroup);

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(contains(result, someGroup.getName()));

        someGroup.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someGroup);

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));
    }

    /**
     * <p>
     * Find an {@link Group} by looking its multi-valued attributes.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testFindByGroupDefinedMultiValuedAttributes() throws Exception {
        Group someGroup = loadOrCreateGroup("someGroup", null, true);

        someGroup.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        IdentityQuery<Group> query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertFalse(contains(result, someGroup.getName()));

        someGroup.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someGroup.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someGroup);

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(contains(result, someGroup.getName()));

        query = identityManager.<Group> createQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2ValueChanged",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    private boolean contains(List<Group> result, String groupName) {
        for (Group resultGroup : result) {
            if (resultGroup.getName().equals(groupName)) {
                return true;
            }
        }

        return false;
    }
}
