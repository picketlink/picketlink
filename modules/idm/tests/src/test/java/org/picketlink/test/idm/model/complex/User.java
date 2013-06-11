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
package org.picketlink.test.idm.model.complex;

import java.util.Collection;
import java.util.Date;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.test.idm.model.complex.entity.UserAddress;
import org.picketlink.test.idm.model.complex.entity.UserContact;
import org.picketlink.test.idm.model.complex.entity.UserEmail;

/**
 * Represents a complex User object, with deep object graph
 * 
 * @author Shane Bryzak
 *
 */
public class User extends AbstractIdentityType implements Account {

    private static final long serialVersionUID = -8870176959974538663L;

    private String loginName;
    private String title;
    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private Collection<UserAddress> addresses;
    private Collection<UserEmail> emails;
    private Collection<UserContact> contacts;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Collection<UserAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(Collection<UserAddress> addresses) {
        this.addresses = addresses;
    }

    public Collection<UserEmail> getEmails() {
        return emails;
    }

    public void setEmails(Collection<UserEmail> emails) {
        this.emails = emails;
    }

    public Collection<UserContact> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<UserContact> contacts) {
        this.contacts = contacts;
    }
}
