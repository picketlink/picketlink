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

/**
 * User representation
 */
public interface User extends IdentityType {
    String KEY_PREFIX = "USER://";

    // TODO: Javadocs
    // TODO: Exceptions

    // TODO: minimal set of "hard-coded" attributes that make sense:
    // TODO: Personal - First/Last/Full Name, Phone, Email, Organization, Created Date, Birthdate; Too much??

    // TODO: separate UserProfile?

    // TODO: for some of those builtin attributes like email proper validation (dedicated exception?) is needed

    // TODO: authentication - password/token validation

    // TODO: non human identity - another interface?

    // Built in attributes

    String getId();

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    // TODO: this one could be configurable with some regex
    String getFullName();

    String getEmail();

    void setEmail(String email);
}
