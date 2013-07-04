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
package org.picketlink.test.tmp;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.AbstractPartition;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Tier;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class FileIdentityStoreTestCase {

    @Test
    public void testCreateRealm() {
        PartitionManager partitionManager = createPartitionManager();

        Realm realm = new Realm(Realm.DEFAULT_REALM);

        partitionManager.add(realm);

        realm = partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);

        assertNotNull(realm);
        assertNotNull(realm.getId());
        assertEquals(Realm.DEFAULT_REALM, realm.getName());

        partitionManager.remove(realm);

        realm = partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM);

        assertNull(realm);
    }

    @Test
    public void testCreateTier() {
        PartitionManager partitionManager = createPartitionManager();

        String name = "Application A";

        Tier tier = new Tier(name);

        partitionManager.add(tier);

        tier = partitionManager.getPartition(Tier.class, name);

        assertNotNull(tier);
        assertNotNull(tier.getId());
        assertEquals(name, tier.getName());

        partitionManager.remove(tier);

        tier = partitionManager.getPartition(Tier.class, name);

        assertNull(tier);
    }

    @Test
    public void testCreateCustomPartition() {
        PartitionManager partitionManager = createPartitionManager();

        String name = "Custom Partition";

        CustomPartition partition = new CustomPartition(name);

        partition.setAttributeA("Attribute A");
        partition.setAttributeB(100l);
        partition.setAttributeC(90);
        partition.setAttributeD(Arrays.asList(new String[] {"Value1", "Value2", "Value3"}));

        partitionManager.add(partition);

        partition = partitionManager.getPartition(CustomPartition.class, name);

        assertNotNull(partition);
        assertNotNull(partition.getId());
        assertEquals(name, partition.getName());
        assertEquals("Attribute A", partition.getAttributeA());
        assertEquals(100l, partition.getAttributeB().longValue());
        assertEquals(90, partition.getAttributeC());
        assertEquals(3, partition.getAttributeD().size());
        assertTrue(contains(partition.getAttributeD(), "Value1"));
        assertTrue(contains(partition.getAttributeD(), "Value2"));
        assertTrue(contains(partition.getAttributeD(), "Value3"));

        partitionManager.remove(partition);

        partition = partitionManager.getPartition(CustomPartition.class, name);

        assertNull(partition);
    }

    @Test
    public void testUpdatePartition() {
        PartitionManager partitionManager = createPartitionManager();

        String name = "Custom Partition";

        CustomPartition partition = new CustomPartition(name);

        partition.setAttributeA("Attribute A");
        partition.setAttributeB(100l);
        partition.setAttributeC(90);
        partition.setAttributeD(Arrays.asList(new String[] {"Value1", "Value2", "Value3"}));

        partitionManager.add(partition);

        partition.setAttributeA("Changed Attribute A");

        partitionManager.update(partition);

        partition = partitionManager.getPartition(CustomPartition.class, name);

        assertNotNull(partition);
        assertNotNull(partition.getId());
        assertEquals(name, partition.getName());
        assertEquals("Changed Attribute A", partition.getAttributeA());
    }

    private PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportAllFeatures();

        return new DefaultPartitionManager(builder.build());
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
