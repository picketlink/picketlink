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
package org.picketlink.test.idm.partition;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Partition;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * <p>Base class for partition management.</p>
 *
 * @author pedroigor
 */
public abstract class AbstractPartitionTestCase<T extends Partition> extends AbstractPartitionManagerTestCase {

    public AbstractPartitionTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() {
        T partition = createPartition();

        String name = partition.getName();

        partition = (T) getPartitionManager().getPartition(partition.getClass(), name);

        assertNotNull(partition);
        assertNotNull(partition.getId());
        assertEquals(name, partition.getName());
    }

    @Test (expected = IdentityManagementException.class)
    public void failCreateWithInvalidConfigurationName() {
        T partition = createPartition();

        partition.setId(null);

        getPartitionManager().add(partition, "invalid_config_name");
    }

    @Test
    public void testRemove() {
        T partition = createPartition();

        String name = partition.getName();

        partition = (T) getPartitionManager().getPartition(partition.getClass(), name);

        assertNotNull(partition);

        getPartitionManager().remove(partition);

        partition = (T) getPartitionManager().getPartition(partition.getClass(), name);

        assertNull(partition);
    }

    @Test
    public void testSetOneValuedAttribute() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String>("one-valued", "1"));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

        Attribute<String> oneValuedAttribute = updatedIdentityType.getAttribute("one-valued");

        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }

    @Test
    public void testSetMultiValuedAttribute() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertNotNull(multiValuedAttribute.getValue());
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[] { "1", "2", "3" }));
    }

    @Test
    public void testSetMultipleAttributes() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

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
    public void testLargeAttributeValue() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        // Create a large array of values
        Integer[] val = new Integer[100];
        for (int i = 0; i < 100; i++) {
            val[i] = i;
        }

        storedIdentityType.setAttribute(new Attribute<Integer[]>("Values", val));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

        assertNotNull(updatedIdentityType.<Integer[]>getAttribute("Values"));

        Integer[] retrievedVal = updatedIdentityType.<Integer[]>getAttribute("Values").getValue();

        for (Integer value: retrievedVal) {
            assertTrue(contains(retrievedVal, value));
        }
    }

    @Test
    public void testGetAllAttributes() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String>("QuestionTotal", "2"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1", "What is favorite toy?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question1Answer", "Gum"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2", "What is favorite word?"));
        storedIdentityType.setAttribute(new Attribute<String>("Question2Answer", "Hi"));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

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
    public void testUpdateAttribute() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] { "3", "4", "5" });

        updatedIdentityType.setAttribute(multiValuedAttribute);

        PartitionManager.update(updatedIdentityType);

        updatedIdentityType = getPartition();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);
        assertEquals(3, multiValuedAttribute.getValue().length);

        String[] values = multiValuedAttribute.getValue();

        Arrays.sort(values);

        assertTrue(Arrays.equals(values, new String[] { "3", "4", "5" }));
    }

    @Test
    public void testRemoveAttribute() throws Exception {
        T storedIdentityType = createPartition();

        PartitionManager PartitionManager = getPartitionManager();

        storedIdentityType.setAttribute(new Attribute<String[]>("multi-valued", new String[] { "1", "2", "3" }));

        PartitionManager.update(storedIdentityType);

        T updatedIdentityType = getPartition();

        Attribute<String[]> multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNotNull(multiValuedAttribute);

        updatedIdentityType.removeAttribute("multi-valued");

        PartitionManager.update(updatedIdentityType);

        updatedIdentityType = getPartition();

        multiValuedAttribute = updatedIdentityType.getAttribute("multi-valued");

        assertNull(multiValuedAttribute);
    }

    @Test
    public void failDuplicatedPartition() {
        T partition = createPartition();

        String name = partition.getName();

        try {
            getPartitionManager().add(partition);
            fail();
        } catch (IdentityManagementException ime) {

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failNullPartition() {
        try {
            getPartitionManager().add(null);
            fail();
        } catch (IdentityManagementException ime) {

        } catch (Exception e) {
            fail();
        }
    }

    protected abstract T createPartition();

    protected abstract T getPartition();

    private boolean contains(Integer[] result, Integer value) {
        for (Integer resultValue : result) {
            if (resultValue.equals(value)) {
                return true;
            }
        }

        return false;
    }
}
