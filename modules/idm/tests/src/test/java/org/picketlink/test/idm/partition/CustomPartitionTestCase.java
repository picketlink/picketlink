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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.AbstractPartition;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.test.idm.AbstractPartitionTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * <p>Test case for the {@link org.picketlink.idm.model.sample.CustomPartition} management operations.</p>
 *
 * @author Pedro Silva
 *
 */
public class CustomPartitionTestCase extends AbstractPartitionTestCase<CustomPartitionTestCase.CustomPartition> {

    public static final String CUSTOM_PARTITION_NAME = "Custom Partition";

    @Override
    protected CustomPartition createPartition() {
        CustomPartition CustomPartition = new CustomPartition(CUSTOM_PARTITION_NAME);

        getPartitionManager().add(CustomPartition, "default");

        return CustomPartition;
    }

    @Override
    protected CustomPartition getPartition() {
        return getPartitionManager().getPartition(CustomPartition.class, "Custom Partition");
    }

    @Test
    public void testAttributes() {
        PartitionManager partitionManager = getPartitionManager();

        String name = "Custom Partition";

        CustomPartition partition = new CustomPartition(name);

        partition.setAttributeA("Attribute A");
        partition.setAttributeB(100l);
        partition.setAttributeC(90);
        partition.setAttributeD(Arrays.asList(new String[]{"Value1", "Value2", "Value3"}));

        partitionManager.add(partition);

        partition.setAttributeA("Changed Attribute A");

        partitionManager.update(partition);

        partition = partitionManager.getPartition(CustomPartition.class, name);

        assertNotNull(partition);
        assertNotNull(partition.getId());
        assertEquals(name, partition.getName());
        assertEquals("Changed Attribute A", partition.getAttributeA());
    }

    public static class CustomPartition extends AbstractPartition {

        private String attributeA;
        private Long attributeB;
        private int attributeC;
        private List<String> attributeD;

        public CustomPartition(String name) {
            super(name);
        }

        @AttributeProperty
        public String getAttributeA() {
            return this.attributeA;
        }

        public void setAttributeA(String attributeA) {
            this.attributeA = attributeA;
        }

        @AttributeProperty
        public Long getAttributeB() {
            return this.attributeB;
        }

        public void setAttributeB(Long attributeB) {
            this.attributeB = attributeB;
        }

        @AttributeProperty
        public int getAttributeC() {
            return this.attributeC;
        }

        public void setAttributeC(int attributeC) {
            this.attributeC = attributeC;
        }

        @AttributeProperty
        public List<String> getAttributeD() {
            return this.attributeD;
        }

        public void setAttributeD(List<String> attributeD) {
            this.attributeD = attributeD;
        }
    }

    private <P extends Object> boolean contains(List<P> list, P value) {
        for (P item: list) {
            if (item.equals(value)) {
                return true;
            }
        }

        return false;
    }
}
