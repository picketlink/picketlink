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
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.picketlink.idm.jpa.annotations.CreationDate;
import org.picketlink.idm.jpa.annotations.Enabled;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.IdentityType;

/**
 * Root entity bean that defines each identity object
 *
 * @author Shane Bryzak
 */
@IdentityManaged (IdentityType.class)
@Entity
public class IdentityObject extends AttributedTypeObject {
    private static final long serialVersionUID = -4903614087285491855L;

    @IdentityClass
    private String identityType;

    @CreationDate
    private Date createdDate;

    @ExpiryDate
    private Date expirationDate;

    @Enabled
    private boolean enabled;

    @OwnerReference
    @ManyToOne
    private IdentityPartition partition;

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public IdentityPartition getPartition() {
        return partition;
    }

    public void setPartition(IdentityPartition partition) {
        this.partition = partition;
    }
}
