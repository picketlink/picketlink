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
package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.AttributeProperty;

import java.util.Date;

/**
 * Abstract base class for IdentityType implementations
 *
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityType extends AbstractAttributedType implements IdentityType {

    private static final long serialVersionUID = 2843998332737143820L;

    private boolean enabled = true;
    private Date createdDate = new Date();
    private Date expirationDate = null;

    private Partition partition;

    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @AttributeProperty
    public Date getExpirationDate() {
        return this.expirationDate;
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    @AttributeProperty
    public Date getCreatedDate() {
        return this.createdDate;
    }

    @Override
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityType other = (IdentityType) obj;

        return (getId() != null && other.getId() != null && getPartition() != null && other.getPartition() != null)
                && (getId().equals(other.getId()) && getPartition().equals(other.getPartition()));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
