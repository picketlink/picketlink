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
package org.picketlink.test.idm.model.complex.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.picketlink.idm.jpa.annotations.Partition;

/**
 * This entity bean contains partition records such as Realms and Tiers
 *
 * @author Shane Bryzak
 */
@Partition
@Entity
public class IdentityPartition implements Serializable {
    private static final long serialVersionUID = -361112181956236802L;

    @Id private String partitionId;
    private String partitionType;

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getPartitionType() {
        return partitionType;
    }

    public void setPartitionType(String partitionType) {
        this.partitionType = partitionType;
    }
}
