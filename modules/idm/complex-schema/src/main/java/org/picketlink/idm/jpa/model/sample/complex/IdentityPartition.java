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
package org.picketlink.idm.jpa.model.sample.complex;

import javax.persistence.Entity;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.PartitionName;
import org.picketlink.idm.jpa.annotations.entity.ConfigurationName;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.Partition;

/**
 * This entity bean contains partition records such as Realms and Tiers
 *
 * @author Shane Bryzak
 */
@Entity
@IdentityManaged ({Partition.class})
public class IdentityPartition extends AttributedTypeObject {

    private static final long serialVersionUID = -361112181956236802L;

    @PartitionName
    private String name;

    @PartitionClass
    private String partitionType;

    @ConfigurationName
    private String configurationName;

    @AttributeValue
    private String attributeA;

    @AttributeValue
    private Long attributeB;

    @AttributeValue
    private int attributeC;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(String partitionType) {
        this.partitionType = partitionType;
    }

    public String getAttributeA() {
        return attributeA;
    }

    public void setAttributeA(String attributeA) {
        this.attributeA = attributeA;
    }

    public Long getAttributeB() {
        return attributeB;
    }

    public void setAttributeB(Long attributeB) {
        this.attributeB = attributeB;
    }

    public int getAttributeC() {
        return attributeC;
    }

    public void setAttributeC(int attributeC) {
        this.attributeC = attributeC;
    }
}
