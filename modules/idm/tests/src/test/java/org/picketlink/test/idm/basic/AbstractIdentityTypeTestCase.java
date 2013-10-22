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

package org.picketlink.test.idm.basic;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * <p>
 * Common tests for all {@link IdentityType} types using the default realm.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class AbstractIdentityTypeTestCase<T extends IdentityType> extends AbstractPartitionManagerTestCase {

    public AbstractIdentityTypeTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testDisable() throws Exception {
        T enabledIdentityType = createIdentityType();

        assertTrue(enabledIdentityType.isEnabled());

        IdentityManager identityManager = getIdentityManager();

        enabledIdentityType.setEnabled(false);

        identityManager.update(enabledIdentityType);

        T disabledIdentityType = getIdentityType();

        assertFalse(disabledIdentityType.isEnabled());

        disabledIdentityType.setEnabled(true);

        identityManager.update(disabledIdentityType);

        enabledIdentityType = getIdentityType();

        assertTrue(enabledIdentityType.isEnabled());
    }

    @Test
    public void testLookupById() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        T identityType = createIdentityType();

        T lookedUpIdentityType = identityManager.lookupIdentityById((Class<T>) identityType.getClass(), identityType.getId());

        assertNotNull(lookedUpIdentityType);
        assertEquals(identityType.getId(), lookedUpIdentityType.getId());

        lookedUpIdentityType = (T) identityManager.lookupIdentityById(IdentityType.class, identityType.getId());

        assertNotNull(lookedUpIdentityType);
        assertEquals(identityType.getId(), lookedUpIdentityType.getId());

        assertNull(identityManager.lookupIdentityById(identityType.getClass(), "bad_id"));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testExpiration() throws Exception {
        T validIdentityType = createIdentityType();

        Date expirationDate = new Date();

        IdentityManager identityManager = getIdentityManager();

        validIdentityType.setExpirationDate(expirationDate);

        identityManager.update(validIdentityType);

        T expiredIdentityType = getIdentityType();

        assertNotNull(expiredIdentityType.getExpirationDate());
        assertTrue(expirationDate.compareTo(expiredIdentityType.getExpirationDate()) == 0);
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testSetOneValuedAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String>("one-valued", "1"));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String> oneValuedAttribute = updatedIdentityType.getAttribute("one-valued");

        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testSetMultiValuedAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertNotNull(multiValuedAttribute.getValue());
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[] { "1", "2", "3" }));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testSetMultipleAttributes() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        assertNotNull(updatedIdentityType.<String> getAttribute("QuestionTotal"));
        assertNotNull(updatedIdentityType.<String> getAttribute("Question1"));
        assertNotNull(updatedIdentityType.<String> getAttribute("Question1Answer"));
        assertNotNull(updatedIdentityType.<String> getAttribute("Question2"));
        assertNotNull(updatedIdentityType.<String> getAttribute("Question2Answer"));

        assertEquals("2", updatedIdentityType.<String> getAttribute("QuestionTotal").getValue());
        assertEquals("What is favorite toy?", updatedIdentityType.<String> getAttribute("Question1").getValue());
        assertEquals("Gum", updatedIdentityType.<String> getAttribute("Question1Answer").getValue());
        assertEquals("What is favorite word?", updatedIdentityType.<String> getAttribute("Question2").getValue());
        assertEquals("Hi", updatedIdentityType.<String> getAttribute("Question2Answer").getValue());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testSetLargeAttributeValue() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        // Create a large array of values
        Integer[] val = new Integer[100];
        for (int i = 0; i < 100; i++) {
            val[i] = i;
        }

        storedIdentityType.setAttribute(new Attribute<Integer[]>("Values", val));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        assertNotNull(updatedIdentityType.<Integer[]>getAttribute("Values"));

        Integer[] retrievedVal = updatedIdentityType.<Integer[]>getAttribute("Values").getValue();

        for (Integer value: retrievedVal) {
            assertTrue(contains(retrievedVal, value));
        }
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testUpdateAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedIdentityType.setAttribute(multiValuedAttribute);

        identityManager.update(updatedIdentityType);

        updatedIdentityType = getIdentityType();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[] { "3", "4", "5" }));
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testGetAllAttributes() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Collection<Attribute<? extends Serializable>> allAttributes = updatedIdentityType.getAttributes();

        assertFalse(allAttributes.isEmpty());

        boolean hasQuestionTotal = false;
        boolean hasQuestion1 = false;
        boolean hasQuestion1Answer = false;
        boolean hasQuestion2 = false;
        boolean hasQuestion2Answer = false;

        for (Attribute<? extends Serializable> attribute : allAttributes) {
            if (attribute.getName().equals("QuestionTotal")) {
                hasQuestionTotal = true;
            }
            if (attribute.getName().equals("Question1")) {
                hasQuestion1 = true;
            }
            if (attribute.getName().equals("Question1Answer")) {
                hasQuestion1Answer = true;
            }
            if (attribute.getName().equals("Question2")) {
                hasQuestion2 = true;
            }
            if (attribute.getName().equals("Question2Answer")) {
                hasQuestion2Answer = true;
            }
        }

        assertTrue(hasQuestionTotal);
        assertTrue(hasQuestion1);
        assertTrue(hasQuestion1Answer);
        assertTrue(hasQuestion2);
        assertTrue(hasQuestion2Answer);
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class})
    public void testRemoveAttribute() throws Exception {
        T storedIdentityType = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        identityManager.update(storedIdentityType);

        T updatedIdentityType = getIdentityType();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityType.removeAttribute("multi-valued");

        identityManager.update(updatedIdentityType);

        updatedIdentityType = getIdentityType();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

    @Test (expected=IdentityManagementException.class)
    public void testAddDuplicatedObject() throws Exception {
        T storedIdentityType = createIdentityType();

        storedIdentityType.setId(null);

        getIdentityManager().add(storedIdentityType);
    }

    protected abstract T createIdentityType();
    protected abstract T getIdentityType();

    private boolean contains(Integer[] result, Integer value) {
        for (Integer resultValue : result) {
            if (resultValue.equals(value)) {
                return true;
            }
        }

        return false;
    }
}
