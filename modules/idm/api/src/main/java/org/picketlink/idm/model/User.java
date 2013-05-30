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
package org.picketlink.idm.model;

import org.picketlink.idm.model.annotation.AttributeProperty;
<<<<<<< HEAD

/**
 * This class represents a User; a human or agent that may
 * consume the services provided by an application.
=======
import org.picketlink.idm.query.QueryParameter;

/**
 * <p>Default {@link IdentityType} implementation  to represent users.</p>
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
 *
 * @author Shane Bryzak
 */
public class User extends Agent {
<<<<<<< HEAD

    private static final long serialVersionUID = 4117586097100398485L;
=======

    private static final long serialVersionUID = -8878182208588103681L;

    /**
     * A query parameter used to set the firstName value.
     */
    public static final QueryParameter FIRST_NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the lastName value.
     */
    public static final QueryParameter LAST_NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the email value.
     */
    public static final QueryParameter EMAIL = new QueryParameter() {};
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60

    private String firstName;
    private String lastName;
    private String email;

<<<<<<< HEAD
    public User(String loginName) {
        super(loginName);
    }

    @AttributeProperty
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @AttributeProperty
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @AttributeProperty
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
=======
    public User() {
        super(null);
    }

    public User(String loginName) {
        super(loginName);
    }

    @AttributeProperty
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @AttributeProperty
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @AttributeProperty
    public String getEmail() {
        return this.email;
>>>>>>> 6f08c37545d08cfc6048373a4b2b7bd23a902c60
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
