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
package org.picketlink.idm.jpa.model.sample.complex.entity;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.complex.CustomerUser;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;

/**
 * @author pedroigor
 */
@IdentityManaged ({EmployeeUser.class, CustomerUser.class})
@Entity
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1021647558840635982L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @AttributeValue
    private Person person;

    @AttributeValue
    private String userName;

    @OwnerReference
    @OneToOne
    private IdentityObject identityObject;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public IdentityObject getIdentityObject() {
        return identityObject;
    }

    public void setIdentityObject(final IdentityObject identityObject) {
        this.identityObject = identityObject;
    }
}
