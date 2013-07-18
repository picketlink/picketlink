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
package org.picketlink.test.idm.model.complex.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import static org.picketlink.test.idm.basic.CustomIdentityTypeTestCase.MyCustomIdentityType;

/**
 * This entity bean stores the user attribute values that are mapped directly to the User class 
 *
 * @author Shane Bryzak
 *
 */
@IdentityManaged (MyCustomIdentityType.class)
@Entity
public class CustomIdentityTypeObject implements Serializable {
    private static final long serialVersionUID = -2360572753933756991L;

    @Id
    @OneToOne
    @OwnerReference
    private IdentityObject identity;

    @AttributeValue
    private String someIdentifier;

    @AttributeValue
    private String someAttribute;

    public IdentityObject getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityObject identity) {
        this.identity = identity;
    }

    public String getSomeIdentifier() {
        return someIdentifier;
    }

    public void setSomeIdentifier(String someIdentifier) {
        this.someIdentifier = someIdentifier;
    }

    public String getSomeAttribute() {
        return someAttribute;
    }

    public void setSomeAttribute(String someAttribute) {
        this.someAttribute = someAttribute;
    }
}
