/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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