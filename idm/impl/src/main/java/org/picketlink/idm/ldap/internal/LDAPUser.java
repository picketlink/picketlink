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
package org.picketlink.idm.ldap.internal;

import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.User;

/**
 * LDAP Representation of an {@link User}
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class LDAPUser extends LDAPEntry implements User {

    private static final long serialVersionUID = 1L;

    // protected transient ManagedAttributeLookup lookup;

    public LDAPUser(String dnSuffix) {
        super(dnSuffix);
        Attribute oc = new BasicAttribute(OBJECT_CLASS);

        oc.add("inetOrgPerson");
        oc.add("organizationalPerson");
        oc.add("person");
        oc.add("top");
        oc.add("extensibleObject");

        getLDAPAttributes().put(oc);
    }

    public LDAPUser() {
        this((String) null);
    }

    public LDAPUser(String dnSuffix, Attributes attributes) {
        this(dnSuffix);
        addAllLDAPAttributes(attributes);
    }

    public LDAPUser(String dnSuffix, Attributes attributes, LDAPCustomAttributes customAttributes) {
        this(dnSuffix, attributes);
        setCustomAttributes(customAttributes);
    }

    @Override
    protected String doGetAttributeForBinding() {
        return UID;
    }

    @Override
    public String getLoginName() {
        return getId();
    }
    
    public void setId(String id) {
        Attribute theAttribute = getLDAPAttributes().get(UID);
        if (theAttribute == null) {
            getLDAPAttributes().put(UID, id);
        } else {
            theAttribute.set(0, id);
        }
    }

    @Override
    public String getId() {
        Attribute theAttribute = getLDAPAttributes().get(UID);
        if (theAttribute != null) {
            try {
                return (String) theAttribute.get();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String getKey() {
        return getId();
    }

    @Override
    public String getFirstName() {
        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.GIVENNAME);

        return getAttributeValue(theAttribute);
    }

    private String getAttributeValue(Attribute attribute) {
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
        getLDAPAttributes().put(LDAPConstants.GIVENNAME, firstName);
    }

    @Override
    public String getLastName() {
        Attribute theAttribute = getLDAPAttributes().get(SN);

        return getAttributeValue(theAttribute);
    }

    @Override
    public void setLastName(String lastName) {
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
        setAttribute(new org.picketlink.idm.model.Attribute<String>(LDAPConstants.EMAIL, email));
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