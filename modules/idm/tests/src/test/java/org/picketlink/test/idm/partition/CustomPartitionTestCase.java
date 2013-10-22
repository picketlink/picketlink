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
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.AbstractPartition;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>Test case for the custom partitions.</p>
 *
 * @author Pedro Silva
 *
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
public class CustomPartitionTestCase extends AbstractPartitionTestCase<CustomPartitionTestCase.CustomPartition> {

    public static final String CUSTOM_PARTITION_NAME = "Custom Partition";

    public CustomPartitionTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected CustomPartition createPartition() {
        CustomPartition customPartition = new CustomPartition(CUSTOM_PARTITION_NAME);

        if (getPartitionManager().getPartition(customPartition.getClass(), customPartition.getName()) != null) {
            getPartitionManager().remove(customPartition);
        }

        getPartitionManager().add(customPartition);

        return customPartition;
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

        if (partitionManager.getPartition(partition.getClass(), partition.getName()) != null) {
            partitionManager.remove(partition);
        }

        partitionManager.add(partition);

        partition.setAttributeA("Changed Attribute A");

        partitionManager.update(partition);

        partition = partitionManager.getPartition(CustomPartition.class, name);

        assertNotNull(partition);
        assertNotNull(partition.getId());
        assertEquals(name, partition.getName());
        assertEquals("Changed Attribute A", partition.getAttributeA());
        assertEquals(100l, (long) partition.getAttributeB());
        assertEquals(90, partition.getAttributeC());
    }

    public static class CustomPartition extends AbstractPartition {

        private String attributeA;
        private Long attributeB;
        private int attributeC;

        public CustomPartition() {
            super(null);
        }

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
