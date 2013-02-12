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

import java.io.Serializable;

import org.picketlink.idm.query.QueryParameter;

/**
 * This interface represents a User; a human or non-human agent that may 
 * consume the services provided by an application.
 * 
 * @author Shane Bryzak
 */
public interface User extends Agent, Serializable {

    /**
     * A query parameter used to set the firstName value.
     */
    QueryParameter FIRST_NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the lastName value.
     */
    QueryParameter LAST_NAME = new QueryParameter() {};

    /**
     * A query parameter used to set the email value.
     */
    QueryParameter EMAIL = new QueryParameter() {};

    /**
     * This String prefixes all values returned by the getKey() method.
     */
    String KEY_PREFIX = "USER://";

    // Built in attributes

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    String getEmail();

    void setEmail(String email);

}
