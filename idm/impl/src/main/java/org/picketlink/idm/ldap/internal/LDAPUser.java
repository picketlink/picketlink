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
package org.picketlink.idm.ldap.internal;

import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SN;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import org.picketlink.idm.model.User;

/**
 * LDAP Representation of an {@link User}
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class LDAPUser extends LDAPAgent implements User {

    private static final long serialVersionUID = 1L;

    public LDAPUser(String dnSuffix) {
        super(dnSuffix);
    }

    public LDAPUser(String loginName, String userDNSuffix) {
        super(loginName, userDNSuffix);
    }

    @Override
    public String getFirstName() {
        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.GIVENNAME);
        
        return getAttributeValue(theAttribute);
    }

    private String getAttributeValue(Attribute attribute) {
        if (attribute == null) {
            return null;
        }
        
        Object value = null;

        try {
            value = attribute.get();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        if (value != null) {
            if (value.getClass().isArray()) {
                return ((Object[]) value)[0].toString();
            } else {
                return value.toString();
            }
        }

        return null;
    }

    @Override
    public void setFirstName(String firstName) {
        if (firstName == null) {
            firstName = " ";
        }
        
        getLDAPAttributes().put(LDAPConstants.GIVENNAME, firstName);
    }

    @Override
    public String getLastName() {
        Attribute theAttribute = getLDAPAttributes().get(SN);

        return getAttributeValue(theAttribute);
    }

    @Override
    public void setLastName(String lastName) {
        if (lastName == null) {
            lastName = " ";
        }

        getLDAPAttributes().put(SN, lastName);
    }

    public void setFullName(String fullName) {
        getLDAPAttributes().put(CN, fullName);
    }

    @Override
    public String getEmail() {
        try {
            Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.EMAIL);
            if (theAttribute != null) {
                return (String) theAttribute.get();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void setEmail(String email) {
        if (email == null) {
            email = " ";
        }
        
        getLDAPAttributes().put(LDAPConstants.EMAIL, email);
    }
    
    /**
     * <p>
     * Returns the user CN attribute value. The CN is composed of user's first and last name.
     * </p>
     * 
     * @param ldapUser
     * @return
     */
    public String getUserCN() {
        String fullName = getFirstName();

        if (getLastName() != null) {
            fullName = fullName + " " + getLastName();
        }
        
        return fullName;
    }



}