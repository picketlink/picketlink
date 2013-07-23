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
package org.picketlink.test.idm.other.shane.model.scenario1.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.jpa.annotations.entity.SupportedAttributes;

/**
 * Stores credential information
 *
 * @author Shane Bryzak
 */
@Entity
@ManagedCredential
@SupportedAttributes(EncodedPasswordStorage.class)
public class PasswordHash implements Serializable {

    @Id @GeneratedValue private long credentialId;
    @ManyToOne @OwnerReference
    IdentityObject identity;
    @CredentialClass private String credentialClass;
    @EffectiveDate
    private Date effectiveDate;
    @ExpiryDate
    private Date expiryDate;
    @CredentialProperty(name = "encodedHash") private String passwordEncodedHash;

    @CredentialProperty private String salt;

    public long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(long credentialId) {
        this.credentialId = credentialId;
    }

    public IdentityObject getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityObject identity) {
        this.identity = identity;
    }

    public String getCredentialClass() {
        return credentialClass;
    }

    public void setCredentialClass(String credentialClass) {
        this.credentialClass = credentialClass;
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

    public String getPasswordEncodedHash() {
        return passwordEncodedHash;
    }

    public void setPasswordEncodedHash(String passwordEncodedHash) {
        this.passwordEncodedHash = passwordEncodedHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}