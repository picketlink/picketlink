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
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * @author pedroigor
 */
@IdentityManaged (EmployeeUser.class)
@Entity
public class Employee extends Person {

    private static final long serialVersionUID = -6032781665709810197L;

    @AttributeValue (name = "employeeId")
    private String internalId;

    @ManyToOne
    private OrganizationUnit organizationUnit;

    @OwnerReference
    @OneToOne
    private IdentityObject IdentityObject;

    public Employee() {
        this(null);
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(final String internalId) {
        this.internalId = internalId;
    }

    public Employee(OrganizationUnit organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public OrganizationUnit getOrganizationUnit() {
        return organizationUnit;
    }

    public void setOrganizationUnit(OrganizationUnit organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public IdentityObject getUserAccount() {
        return IdentityObject;
    }

    public void setUserAccount(final IdentityObject userAccount) {
        this.IdentityObject = userAccount;
    }
}
