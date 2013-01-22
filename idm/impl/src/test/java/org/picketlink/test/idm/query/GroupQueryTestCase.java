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
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Group} instances.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class GroupQueryTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testFindById() throws Exception {
        Group group = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.ID, group.getId());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(group.getName(), result.get(0).getName());
    }
    
    @Test
    public void testPagination() throws Exception {
        for (int i = 0; i < 50; i++) {
            createGroup("someGroup" + i + 1, null);
        }
        
        IdentityManager identityManager = getIdentityManager();
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
        query.setLimit(10);
        query.setOffset(0);
        
        int resultCount = query.getResultCount();
        
        assertEquals(50, resultCount);
        
        List<Group> firstPage = query.getResultList();
        
        assertEquals(10, firstPage.size());
        
        List<String> groupIds = new ArrayList<String>();
        
        for (Group Group : firstPage) {
            groupIds.add(Group.getId());
        }
        
        query.setOffset(10);
        
        List<Group> secondPage = query.getResultList();
        
        assertEquals(10, secondPage.size());
        
        for (Group Group : secondPage) {
            assertFalse(groupIds.contains(Group.getId()));
            groupIds.add(Group.getId());
        }
        
        query.setOffset(20);
        
        List<Group> thirdPage = query.getResultList();
        
        assertEquals(10, thirdPage.size());
        
        for (Group Group : thirdPage) {
            assertFalse(groupIds.contains(Group.getId()));
            groupIds.add(Group.getId());
        }
        
        query.setOffset(30);
        
        List<Group> fourthPage = query.getResultList();
        
        assertEquals(10, fourthPage.size());
        
        for (Group Group : fourthPage) {
            assertFalse(groupIds.contains(Group.getId()));
            groupIds.add(Group.getId());
        }
        
        query.setOffset(40);
        
        List<Group> fifthyPage = query.getResultList();
        
        assertEquals(10, fifthyPage.size());
        
        for (Group Group : fifthyPage) {
            assertFalse(groupIds.contains(Group.getId()));
            groupIds.add(Group.getId());
        }
        
        assertEquals(50, groupIds.size());
        
        query.setOffset(50);
        
        List<Group> invalidPage = query.getResultList();
        
        assertEquals(0, invalidPage.size());
    }
    
    @Test
    public void testFindByRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Group someGroupDefaultRealm = new SimpleGroup("someGroupRealm");
        
        identityManager.add(someGroupDefaultRealm);
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
        Realm defaultRealm = identityManager.getRealm(Realm.DEFAULT_REALM);
        
        assertNotNull(defaultRealm);
        
        query.setParameter(Group.PARTITION, defaultRealm);
        
        List<Group> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupDefaultRealm.getName()));
        
        Realm testingRealm = identityManager.getRealm("Testing");
        
        if (testingRealm == null) {
            testingRealm = new Realm("Testing");
            identityManager.createRealm(testingRealm);
        }
        
        Group someGroupTestingRealm = new SimpleGroup("someGroupTestingRealm");
        
        identityManager.forRealm(testingRealm).add(someGroupTestingRealm);
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.PARTITION, testingRealm);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupTestingRealm.getName()));
    }
    
    @Test
    public void testFindByTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier someTier = new Tier("Some Group Tier");
        
        identityManager.createTier(someTier);

        Group someGroupTier = new SimpleGroup("someGroupTier");
        
        identityManager.forTier(someTier).add(someGroupTier);
        
        IdentityQuery<Group> query = identityManager.createIdentityQuery(Group.class);
        
        assertNotNull(someTier);
        
        query.setParameter(Group.PARTITION, someTier);
        
        List<Group> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupTier.getName()));
        
        Tier someAnotherTier = new Tier("Some Another Group Tier");
        
        identityManager.createTier(someAnotherTier);
        
        Group someGroupTestingTier = new SimpleGroup("someGroupTestingRealm");
        
        identityManager.forTier(someAnotherTier).add(someGroupTestingTier);
        
        query = identityManager.createIdentityQuery(Group.class);
        
        query.setParameter(Group.PARTITION, someAnotherTier);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroupTestingTier.getName()));
    }
    
    @Test
    public void testFindByName() throws Exception {
        Group group = createGroup("admin", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, "admin");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(group.getName(), result.get(0).getName());
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
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        someGroup.setEnabled(true);
        someAnotherGroup.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);
        identityManager.update(someAnotherGroup);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.ENABLED, true);

        // all enabled groups
        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.ENABLED, false);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        someGroup.setEnabled(false);

        identityManager.update(someGroup);

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.ENABLED, false);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroup.getName()));

        someAnotherGroup.setEnabled(false);

        identityManager.update(someAnotherGroup);

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.ENABLED, true);

        result = query.getResultList();

        assertTrue(result.isEmpty());
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
        Group group = createGroup("someGroup", "Parent Group");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.PARENT, group.getParentGroup().getName());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
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
        Group group = createGroup("someGroup", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.CREATED_DATE, group.getCreatedDate());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("someGroup", result.get(0).getName());

        query = identityManager.<Group> createIdentityQuery(Group.class);
        
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
        Group group = createGroup("someGroup", null);

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        group = identityManager.getGroup("someGroup");

        group.setExpirationDate(expirationDate);

        identityManager.update(group);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.EXPIRY_DATE, expirationDate);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, group.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

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
        Group someGroup = createGroup("someGroup", null);
        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        query.setParameter(Group.CREATED_AFTER, calendar.getTime());
        query.setParameter(Group.CREATED_BEFORE, new Date());

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        Group someFutureGroup = createGroup("someFutureGroup", null);
        Group someAnotherFutureGroup = createGroup("someAnotherFutureGroup", null);

        query.setParameter(Group.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        Calendar futureDate = Calendar.getInstance();
        
        futureDate.add(Calendar.MINUTE, 1);
        
        // Should return an empty list.
        query.setParameter(User.CREATED_AFTER, futureDate.getTime());

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
        Group group = createGroup("admin", null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(group);

        group.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(group);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.NAME, "admin");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, group.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

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
        Group someGroup = createGroup("someGroup", null);

        someGroup.setExpirationDate(new Date());

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        Group someAnotherGroup = createGroup("someAnotherGroup", null);

        someAnotherGroup.setExpirationDate(new Date());

        identityManager.update(someAnotherGroup);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();
        
        query.setParameter(Group.EXPIRY_AFTER, expiryDate);
        query.setParameter(Group.EXPIRY_BEFORE, new Date());

        Group someFutureGroup = createGroup("someFutureGroup", null);
        
        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);
        
        someFutureGroup.setExpirationDate(calendar.getTime());

        identityManager.update(someFutureGroup);

        Group someAnotherFutureGroup = createGroup("someAnotherFutureGroup", null);

        someAnotherFutureGroup.setExpirationDate(calendar.getTime());

        identityManager.update(someAnotherFutureGroup);

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.EXPIRY_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(Group.EXPIRY_BEFORE, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, someGroup.getName()));
        assertTrue(contains(result, someAnotherGroup.getName()));
        assertTrue(contains(result, someFutureGroup.getName()));
        assertTrue(contains(result, someAnotherFutureGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        Calendar futureDate = Calendar.getInstance();
        
        futureDate.add(Calendar.MINUTE, 2);
        
        // Should return an empty list.
        query.setParameter(User.EXPIRY_AFTER, futureDate.getTime());

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
        Group someGroup = createGroup("someGroup", null);

        someGroup.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroup.getName()));

        someGroup.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(someGroup);

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        someGroup.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(someGroup);

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
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
        Group someGroup = createGroup("someGroup", null);

        someGroup.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValue2" }));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someGroup);

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        List<Group> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttributeValue1",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValueChanged",
                "someAttributeValue2" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue" });

        result = query.getResultList();

        assertTrue(result.isEmpty());

        someGroup.setAttribute(new Attribute<String[]>("someAttribute", new String[] { "someAttributeValue1",
                "someAttributeValueChanged" }));
        someGroup.setAttribute(new Attribute<String[]>("someAttribute2", new String[] { "someAttribute2Value1",
                "someAttribute2Value2" }));

        identityManager.update(someGroup);

        query = identityManager.<Group> createIdentityQuery(Group.class);

        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute"), new Object[] { "someAttributeValue1",
                "someAttributeValueChanged" });
        query.setParameter(IdentityType.ATTRIBUTE.byName("someAttribute2"), new Object[] { "someAttribute2Value1",
                "someAttribute2Value2" });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someGroup.getName()));

        query = identityManager.<Group> createIdentityQuery(Group.class);

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
