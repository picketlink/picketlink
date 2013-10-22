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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Pedro Silva
 */
public abstract class AbstractIdentityQueryTestCase<T extends IdentityType> extends AbstractPartitionManagerTestCase {

    public AbstractIdentityQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    protected abstract T createIdentityType(String name, Partition partition);

    protected abstract T getIdentityType();

    protected abstract T createInstance(String name);

    @Test
    public void testFindById() throws Exception {
        T identityType = createIdentityType(null, null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(AttributedType.ID, identityType.getId());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(identityType.getId(), result.get(0).getId());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testPagination() throws Exception {
        T identityType = null;

        for (int i = 0; i < 50; i++) {
            identityType = createIdentityType("someIdentityType" + (i + 1), null);
        }

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) identityType.getClass());

        query.setLimit(10);
        query.setOffset(0);

        int resultCount = query.getResultCount();

        assertEquals(50, resultCount);

        List<T> firstPage = query.getResultList();

        assertEquals(10, firstPage.size());

        List<String> agentIds = new ArrayList<String>();

        for (T result : firstPage) {
            agentIds.add(result.getId());
        }

        query.setOffset(10);

        List<T> secondPage = query.getResultList();

        assertEquals(10, secondPage.size());

        for (T result : secondPage) {
            assertFalse(agentIds.contains(result.getId()));
            agentIds.add(result.getId());
        }

        query.setOffset(20);

        List<T> thirdPage = query.getResultList();

        assertEquals(10, thirdPage.size());

        for (T result : thirdPage) {
            assertFalse(agentIds.contains(result.getId()));
            agentIds.add(result.getId());
        }

        query.setOffset(30);

        List<T> fourthPage = query.getResultList();

        assertEquals(10, fourthPage.size());

        for (T result : fourthPage) {
            assertFalse(agentIds.contains(result.getId()));
            agentIds.add(result.getId());
        }

        query.setOffset(40);

        List<T> fifthyPage = query.getResultList();

        assertEquals(10, fifthyPage.size());

        for (T result : fifthyPage) {
            assertFalse(agentIds.contains(result.getId()));
            agentIds.add(result.getId());
        }

        assertEquals(50, agentIds.size());

        query.setOffset(50);

        List<T> invalidPage = query.getResultList();

        assertTrue(invalidPage.isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindByRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        T someTypeDefaultRealm = createIdentityType(null, null);

        IdentityQuery<T> query = identityManager.createIdentityQuery((Class<T>) someTypeDefaultRealm.getClass());

        Realm defaultRealm = getPartitionManager().getPartition(Realm.class, Realm.DEFAULT_REALM);

        assertNotNull(defaultRealm);

        query.setParameter(IdentityType.PARTITION, defaultRealm);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(someTypeDefaultRealm.getId(), result.get(0).getId());

        getPartitionManager().add(new Realm("Testing Realm"));

        Realm testingRealm = getPartitionManager().getPartition(Realm.class, "Testing Realm");

        assertNotNull(testingRealm);

        T someAnotherTypeTestingRealm = createIdentityType("someAnotherType", testingRealm);

        query = getPartitionManager().createIdentityManager(testingRealm).createIdentityQuery(
                (Class<T>) someTypeDefaultRealm.getClass());

        query.setParameter(Agent.PARTITION, testingRealm);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(someAnotherTypeTestingRealm.getId(), result.get(0).getId());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindEnabledAndDisabled() throws Exception {
        T someType = createIdentityType(null, null);
        T someAnotherType = createIdentityType("someAnotherAgent", null);

        someType.setEnabled(true);
        someAnotherType.setEnabled(true);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(someType);
        identityManager.update(someAnotherType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) someType.getClass());

        query.setParameter(IdentityType.ENABLED, true);

        // all enabled users
        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, someType.getId()));
        assertTrue(contains(result, someAnotherType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) someType.getClass());

        query.setParameter(IdentityType.ENABLED, false);

        // only disabled users. No users are disabled.
        result = query.getResultList();

        assertTrue(result.isEmpty());

        someType.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someType);

        query = identityManager.<T>createIdentityQuery((Class<T>) someType.getClass());

        query.setParameter(IdentityType.ENABLED, false);

        // get the previously disabled user
        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someType.getId()));

        someAnotherType.setEnabled(false);

        // let's disabled the user and try to find him
        identityManager.update(someAnotherType);

        query = identityManager.<T>createIdentityQuery((Class<T>) someType.getClass());

        query.setParameter(IdentityType.ENABLED, true);

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindCreationDate() throws Exception {
        Thread.sleep(1000);
        T identityType = createIdentityType(null, null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.CREATED_DATE, identityType.getCreatedDate());

        // only the previously created user
        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(identityType.getId(), result.get(0).getId());

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        Calendar futureDate = Calendar.getInstance();

        futureDate.add(Calendar.MINUTE, 1);

        query.setParameter(IdentityType.CREATED_DATE, futureDate.getTime());

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindExpiryDate() throws Exception {
        T identityType = createIdentityType(null, null);

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        identityType = getIdentityType();

        identityType.setExpirationDate(expirationDate);

        identityManager.update(identityType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.EXPIRY_DATE, identityType.getExpirationDate());

        // all expired users
        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(identityType.getId(), result.get(0).getId());

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        query.setParameter(IdentityType.EXPIRY_DATE, calendar.getTime());

        // no users
        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindBetweenCreationDate() throws Exception {
        T identityType = createIdentityType(null, null);
        T someAnotherIdentityType = createIdentityType("someAnotherAgent", null);

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -10);

        // users between the given time period
        query.setParameter(IdentityType.CREATED_AFTER, calendar.getTime());
        query.setParameter(IdentityType.CREATED_BEFORE, new Date());

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherIdentityType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        T someFutureType = createIdentityType("someFutureAgent", null);
        T someAnotherFutureType = createIdentityType("someAnotherFutureAgent", null);

        // users created after the given time
        query.setParameter(IdentityType.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherIdentityType.getId()));
        assertTrue(contains(result, someFutureType.getId()));
        assertTrue(contains(result, someAnotherFutureType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        // users created before the given time
        query.setParameter(IdentityType.CREATED_BEFORE, new Date());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherIdentityType.getId()));
        assertTrue(contains(result, someFutureType.getId()));
        assertTrue(contains(result, someAnotherFutureType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        query.setParameter(IdentityType.CREATED_AFTER, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindBetweenExpirationDate() throws Exception {
        T identityType = createIdentityType(null, null);

        Date currentDate = new Date();

        identityType.setExpirationDate(currentDate);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(identityType);

        T someAnotherType = createIdentityType("someAnotherAgent", null);

        someAnotherType.setExpirationDate(currentDate);

        identityManager.update(someAnotherType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.YEAR, -1);

        Date expiryDate = calendar.getTime();

        // users between the given time period
        query.setParameter(IdentityType.EXPIRY_AFTER, expiryDate);
        query.setParameter(IdentityType.EXPIRY_BEFORE, currentDate);

        T someFutureType = createIdentityType("someFutureAgent", null);

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        someFutureType.setExpirationDate(calendar.getTime());

        identityManager.update(someFutureType);

        T someAnotherFutureType = createIdentityType("someAnotherFutureAgent", null);

        someAnotherFutureType.setExpirationDate(calendar.getTime());

        identityManager.update(someAnotherFutureType);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        // users expired after the given time
        query.setParameter(IdentityType.EXPIRY_AFTER, expiryDate);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherType.getId()));
        assertTrue(contains(result, someFutureType.getId()));
        assertTrue(contains(result, someAnotherFutureType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 1);

        // users expired before the given time
        query.setParameter(IdentityType.EXPIRY_BEFORE, calendar.getTime());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(contains(result, identityType.getId()));
        assertTrue(contains(result, someAnotherType.getId()));
        assertTrue(contains(result, someFutureType.getId()));
        assertTrue(contains(result, someAnotherFutureType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, 2);

        // users expired after the given time. Should return an empty list.
        query.setParameter(IdentityType.EXPIRY_AFTER, calendar.getTime());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindByMultipleParameters() throws Exception {
        T identityType = createIdentityType(null, null);

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(identityType);

        identityType.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        identityManager.update(identityType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.ENABLED, identityType.isEnabled());
        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue");
        query.setParameter(IdentityType.ENABLED, !identityType.isEnabled());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindByDefinedAttributes() throws Exception {
        T identityType = createIdentityType(null, null);

        identityType.setAttribute(new Attribute<String>("someAttribute", "someAttributeValue"));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(identityType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));

        identityType.setAttribute(new Attribute<String>("someAttribute", "someAttributeValueChanged"));

        identityManager.update(identityType);

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValue");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        identityType.setAttribute(new Attribute<String>("someAttribute2", "someAttributeValue2"));

        identityManager.update(identityType);

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), "someAttributeValueChanged");
        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), "someAttributeValue2");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindByMultiValuedAttributes() throws Exception {
        T identityType = createIdentityType(null, null);

        identityType.setAttribute(new Attribute<String[]>("someAttribute", new String[]{"someAttributeValue1",
                "someAttributeValue2"}));

        IdentityManager identityManager = getIdentityManager();

        identityManager.update(identityType);

        IdentityQuery<T> query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), new Object[]{"someAttributeValue1",
                "someAttributeValue2"});

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), new Object[]{"someAttributeValue1",
                "someAttributeValue2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), new Object[]{"someAttributeValueChanged",
                "someAttributeValue2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), new Object[]{"someAttributeValue"});

        result = query.getResultList();

        assertTrue(result.isEmpty());

        identityType.setAttribute(new Attribute<String[]>("someAttribute", new String[]{"someAttributeValue1",
                "someAttributeValueChanged"}));
        identityType.setAttribute(new Attribute<String[]>("someAttribute2", new String[]{"someAttribute2Value1",
                "someAttribute2Value2"}));

        identityManager.update(identityType);

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), new Object[]{"someAttributeValue1",
                "someAttributeValueChanged"});
        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), new Object[]{"someAttribute2Value1",
                "someAttribute2Value2"});

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, identityType.getId()));

        query = identityManager.<T>createIdentityQuery((Class<T>) identityType.getClass());

        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute"), new Object[]{"someAttributeValue1",
                "someAttributeValueChanged"});
        query.setParameter(IdentityType.QUERY_ATTRIBUTE.byName("someAttribute2"), new Object[]{"someAttribute2ValueChanged",
                "someAttribute2Value2"});

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class,
            LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindByTier() throws Exception {
        T someType = createInstance("someType");

        getPartitionManager().add(new Tier("Application A"));

        Tier applicationATier = getPartitionManager().getPartition(Tier.class, "Application A");

        IdentityManager applicationA = getPartitionManager().createIdentityManager(applicationATier);

        applicationA.add(someType);

        IdentityQuery<T> query = (IdentityQuery<T>) applicationA.createIdentityQuery(someType.getClass());

        query.setParameter(IdentityType.PARTITION, applicationATier);

        List<T> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, someType.getId()));

        getPartitionManager().add(new Tier("Application B"));

        Tier applicationBTier = getPartitionManager().getPartition(Tier.class, "Application B");

        IdentityManager applicationB = getPartitionManager().createIdentityManager(applicationBTier);

        T anotherRole = createInstance("anotherType");

        applicationB.add(anotherRole);

        query = (IdentityQuery<T>) applicationB.createIdentityQuery(someType.getClass());

        query.setParameter(Role.PARTITION, applicationBTier);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(contains(result, anotherRole.getId()));
    }

    protected boolean containsMembership(List<GroupMembership> result, Account account) {
        for (GroupMembership identityType : result) {
            if (identityType.getMember().getId().equals(account.getId())) {
                return true;
            }
        }

        return false;
    }

    protected boolean containsMembership(List<GroupMembership> result, Group group) {
        for (GroupMembership identityType : result) {
            if (identityType.getGroup().getId().equals(group.getId())) {
                return true;
            }
        }

        return false;
    }

    protected boolean containsGrant(List<Grant> result, IdentityType identityType) {
        for (Grant grant : result) {
            if (Account.class.isInstance(identityType)) {
                if (grant.getAssignee().getId().equals(identityType.getId())) {
                    return true;
                }
            } else {
                if (grant.getRole().getId().equals(identityType.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean contains(List<T> result, String id) {
        for (T identityType : result) {
            if (identityType.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }
}
