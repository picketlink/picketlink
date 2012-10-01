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
package org.picketlink.idm.internal.ldap;

import static org.picketlink.idm.internal.ldap.LDAPConstants.CN;
import static org.picketlink.idm.internal.ldap.LDAPConstants.EMAIL;
import static org.picketlink.idm.internal.ldap.LDAPConstants.GIVENNAME;
import static org.picketlink.idm.internal.ldap.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.internal.ldap.LDAPConstants.SN;
import static org.picketlink.idm.internal.ldap.LDAPConstants.UID;

import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.picketlink.idm.internal.ldap.LDAPObjectChangedNotification.NType;
import org.picketlink.idm.model.User;

/**
 * LDAP Representation of an {@link User}
 *
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class LDAPUser extends DirContextAdaptor implements User {

    protected String userid, firstName, lastName, fullName, email, userDNSuffix;

    protected LDAPUserCustomAttributes customAttributes = new LDAPUserCustomAttributes();

    protected ManagedAttributeLookup lookup;

    public LDAPUser() {
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("inetOrgPerson");
        oc.add("organizationalPerson");
        oc.add("person");
        oc.add("top");
        oc.add("extensibleObject");

        attributes.put(oc);
    }
    


    public LDAPUser(String userId, ManagedAttributeLookup lookup) {
        this();
        setLookup(lookup);
        setId(userId);
        setFullName(userId);
    }


    public ManagedAttributeLookup getLookup() {
        return lookup;
    }

    public void setLookup(ManagedAttributeLookup lookup) {
        this.lookup = lookup;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Attributes getAttributes(String name) throws NamingException {
        Attributes collectiveAttributes = new BasicAttributes(true);
        NamingEnumeration ne = attributes.getAll();
        while (ne.hasMore()) {
            collectiveAttributes.put((Attribute) ne.next());
        }
        Map<String, Object> custom = customAttributes.getAttributes();
        Set<String> keys = custom.keySet();
        for (String key : keys) {
            Object value = custom.get(key);
            collectiveAttributes.put(key, value);
        }
        return collectiveAttributes;
    }

    @Override
    public String getAttribute(String name) {
        if (lookup.isManaged(name) == false) {
            return (String) customAttributes.getAttribute(name);
        }
        return super.getAttribute(name);
    }

    @Override
    public String[] getAttributeValues(String name) {
        if (lookup.isManaged(name) == false) {
            Object value = customAttributes.getAttribute(name);
            if (value instanceof String[]) {
                return (String[]) value;
            } else {
                return new String[] { (String) value };
            }
        }
        return super.getAttributeValues(name);
    }

    @Override
    public Map<String, String[]> getAttributes() {
        Map<String, String[]> map = super.getAttributes();
        Map<String, Object> values = customAttributes.getAttributes();
        Set<String> keys = values.keySet();
        for (String key : keys) {
            Object value = values.get(key);
            if (value instanceof String[]) {
                map.put(key, (String[]) value);
            } else if (value instanceof String) {
                String[] arr = new String[] { (String) value };
                map.put(key, arr);
            }
        }
        return map;
    }

    @Override
    public void setAttribute(String name, String value) {
        if (lookup.isManaged(name)) {
            super.setAttribute(name, value);
        } else {
            setCustomAttribute(name, value);
        }
    }

    @Override
    public void setAttribute(String name, String[] values) {
        if (lookup.isManaged(name)) {
            super.setAttribute(name, values);
        } else {
            setCustomAttribute(name, values);
        }
    }

    public void setCustomAttribute(String name, String value) {
        // Add into the custom attributes also
        customAttributes.addAttribute(name, value);
        if (handler != null) {
            handler.handle(new LDAPObjectChangedNotification(this, NType.CUSTOM_ATTRIBUTE, null));
        }
    }

    public void setCustomAttribute(String name, String[] values) {
        // Add into the custom attributes also
        customAttributes.addAttribute(name, values);
        if (handler != null) {
            handler.handle(new LDAPObjectChangedNotification(this, NType.CUSTOM_ATTRIBUTE, null));
        }
    }

    public LDAPUserCustomAttributes getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(LDAPUserCustomAttributes customAttributes) {
        this.customAttributes = customAttributes;
    }

    public void setUserDNSuffix(String udn) {
        this.userDNSuffix = udn;
    }

    public String getDN() {
        try {
            if (userid == null) {
                userid = (String) attributes.get(UID).get();
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return UID + EQUAL + getId() + COMMA + userDNSuffix;
    }

    public void setId(String id) {
        this.userid = id;
        Attribute theAttribute = attributes.get(UID);
        if (theAttribute == null) {
            attributes.put(UID, id);
        } else {
            theAttribute.set(0, id);
        }
    }

    @Override
    public String getId() {
        Attribute theAttribute = attributes.get(UID);
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
    public String getFirstName() {
        try {
            if (firstName == null) {
                Attribute theAttribute = attributes.get(GIVENNAME);
                if (theAttribute != null) {
                    firstName = (String) theAttribute.get();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        Attribute theAttribute = attributes.get(GIVENNAME);

        if (theAttribute == null) {
            attributes.put(GIVENNAME, firstName);
        } else {
            replaceAttribute(GIVENNAME, firstName);
        }

        Attribute cnAttribute = attributes.get(CN);

        if (cnAttribute != null) {
            replaceAttribute(CN, firstName);
        }

        attributes.put(CN, firstName);
    }

    @Override
    public String getLastName() {
        try {
            if (lastName == null) {
                Attribute theAttribute = attributes.get(SN);
                if (theAttribute != null) {
                    lastName = (String) theAttribute.get();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
        Attribute theAttribute = attributes.get(SN);

        if (theAttribute == null) {
            attributes.put(SN, lastName);
        } else {
            theAttribute.set(0, lastName);
        }

        Attribute cnAttribute = attributes.get(CN);

        if (cnAttribute == null) {
            cnAttribute = new BasicAttribute(CN, lastName);
            attributes.put(cnAttribute);
        } else {
            try {
                replaceAttribute(SN, lastName);
                replaceAttribute(CN, cnAttribute.get().toString() + " " + lastName);
            } catch (NamingException e) {
                throw new RuntimeException("Could not set user's last name.", e);
            }
        }

    }

    @Override
    public String getFullName() {
        try {
            if (fullName == null) {
                Attribute theAttribute = attributes.get(CN);
                if (theAttribute != null) {
                    fullName = (String) theAttribute.get();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;

        Attribute theAttribute = attributes.get(CN);

        if (theAttribute == null) {
            attributes.put(CN, fullName);
        } else {
            theAttribute.set(0, fullName);
        }

        setFirstName(getFirstName(fullName));
        setLastName(getLastName(fullName));
    }

    @Override
    public String getEmail() {
        try {
            if (email == null) {
                Attribute theAttribute = attributes.get(EMAIL);
                if (theAttribute != null) {
                    email = (String) theAttribute.get();
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
        Attribute theAttribute = attributes.get(EMAIL);
        if (theAttribute == null) {
            setAttribute(EMAIL, email);
        } else {
            replaceAttribute(EMAIL, email);
        }
    }

    private String getFirstName(String name) {
        String[] tokens = name.split("\\ ");
        int length = tokens.length;
        String firstName = null;

        if (length > 0) {
            firstName = tokens[0];
        }
        return firstName;
    }

    private String getLastName(String name) {
        String lastName = null;

        String[] tokens = name.split("\\ ");

        if (tokens.length > 0) {
            lastName = tokens[tokens.length - 1];
        }

        return lastName;
    }
}