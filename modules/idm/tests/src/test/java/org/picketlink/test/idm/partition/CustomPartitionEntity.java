/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.test.idm.partition;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;

@IdentityManaged(CustomPartitionTestCase.CustomPartition.class)
@Entity
public class CustomPartitionEntity implements Serializable {

    @AttributeValue
    private String attributeA;

    @AttributeValue
    private Long attributeB;

    @AttributeValue
    private int attributeC;

    @OneToOne
    @Id
    @OwnerReference
    private PartitionTypeEntity partitionTypeEntity;

    public PartitionTypeEntity getPartitionTypeEntity() {
        return partitionTypeEntity;
    }

    public void setPartitionTypeEntity(PartitionTypeEntity partitionTypeEntity) {
        this.partitionTypeEntity = partitionTypeEntity;
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