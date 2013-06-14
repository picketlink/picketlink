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

package org.picketlink.idm.jpa.schema;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.picketlink.idm.jpa.annotations.CreationDate;
import org.picketlink.idm.jpa.annotations.Enabled;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.model.annotation.IdentityPartition;

/**
 * <p>JPA {@link Entity} that maps IdentityType instances.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
public class IdentityObject implements Serializable {

    private static final long serialVersionUID = -9155861474157098664L;

    @IdentityClass
    private String identityClass;

    @IdentityPartition
    @ManyToOne
    private Partition partition;

    @Identifier
    @Id
    private String id;

    @Enabled
    private boolean enabled;

    @CreationDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ExpiryDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityObject other = (IdentityObject) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
