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

import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.model.sample.complex.CustomerUser;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * @author pedroigor
 */
@IdentityManaged (CustomerUser.class)
@Entity
public class Customer extends Person {

    @OwnerReference
    @OneToOne
    private IdentityObject IdentityObject;

    public IdentityObject getIdentityObject() {
        return IdentityObject;
    }

    public void setIdentityObject(final IdentityObject identityObject) {
        IdentityObject = identityObject;
    }
}
