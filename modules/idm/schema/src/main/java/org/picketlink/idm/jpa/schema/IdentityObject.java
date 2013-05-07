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

import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.IdentityType;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * <p>JPA {@link Entity} that maps IdentityType instances.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@IdentityType
@Entity
public class IdentityObject implements Serializable {

    private static final long serialVersionUID = -9155861474157098664L;

    @IDMProperty(PropertyType.IDENTITY_DISCRIMINATOR)
    private String discriminator;

    @ManyToOne
    @IDMProperty (PropertyType.IDENTITY_PARTITION)
    private PartitionObject partition;

    @IDMProperty(PropertyType.IDENTITY_ID)
    @Id
    private String id;

    @IDMProperty(PropertyType.AGENT_LOGIN_NAME)
    private String loginName;

    @IDMProperty(PropertyType.IDENTITY_NAME)
    private String name;

    @IDMProperty(PropertyType.USER_FIRST_NAME)
    private String firstName;

    @IDMProperty(PropertyType.USER_LAST_NAME)
    private String lastName;

    @IDMProperty(PropertyType.USER_EMAIL)
    private String email;

    @IDMProperty(PropertyType.IDENTITY_ENABLED)
    private boolean enabled;

    @IDMProperty(PropertyType.IDENTITY_CREATION_DATE)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @IDMProperty(PropertyType.IDENTITY_EXPIRY_DATE)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    @ManyToOne
    @IDMProperty(PropertyType.GROUP_PARENT)
    private IdentityObject parent;

    @IDMProperty (PropertyType.GROUP_PATH)
    private String groupPath;

    public String getDiscriminator() {
        return this.discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartitionObject getPartition() {
        return partition;
    }

    public void setPartition(PartitionObject partition) {
        this.partition = partition;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public IdentityObject getParent() {
        return this.parent;
    }

    public void setParent(IdentityObject parent) {
        this.parent = parent;
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
