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
