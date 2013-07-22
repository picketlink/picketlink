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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;
import org.picketlink.test.idm.other.shane.model.scenario1.User;

/**
 * Stores user email addresses
 *
 * @author Shane Bryzak
 */
@Entity
@IdentityManaged({User.class})
@MappedAttribute("emails")
public class UserEmail implements Serializable {
    private static final long serialVersionUID = 4044401260242743000L;

    @Id @GeneratedValue private Long emailId;
    @ManyToOne @OwnerReference private IdentityObject identity;
    private @ManyToOne EmailType emailType;
    private String emailAddress;
    private boolean primaryEmail;

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long emailId) {
        this.emailId = emailId;
    }

    public IdentityObject getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityObject identity) {
        this.identity = identity;
    }

    public EmailType getEmailType() {
        return emailType;
    }

    public void setEmailType(EmailType emailType) {
        this.emailType = emailType;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(boolean primaryEmail) {
        this.primaryEmail = primaryEmail;
    }
}
