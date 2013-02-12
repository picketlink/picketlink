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

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.picketlink.idm.jpa.annotations.EntityType;
import org.picketlink.idm.jpa.annotations.IDMEntity;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Entity
@IDMEntity(EntityType.IDENTITY_CREDENTIAL)
public class CredentialObject {
    
    @Id
    @GeneratedValue
    private long internalId;
    
    @IDMProperty (PropertyType.CREDENTIAL_TYPE)
    private String type;
    
    @IDMProperty (PropertyType.CREDENTIAL_VALUE)
    private String credential;
    
    @IDMProperty (PropertyType.CREDENTIAL_EFFECTIVE_DATE)
    @Temporal (TemporalType.TIMESTAMP)
    private Date effectiveDate;
    
    @IDMProperty (PropertyType.CREDENTIAL_EXPIRY_DATE)
    @Temporal (TemporalType.TIMESTAMP)
    private Date expiryDate;
    
    @IDMProperty (PropertyType.CREDENTIAL_IDENTITY)
    @ManyToOne
    private IdentityObject identityType;

    public long getInternalId() {
        return internalId;
    }
    
    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public IdentityObject getIdentityType() {
        return identityType;
    }

    public void setIdentityType(IdentityObject identityType) {
        this.identityType = identityType;
    }

}