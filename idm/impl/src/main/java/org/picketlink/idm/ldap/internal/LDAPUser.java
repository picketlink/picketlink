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

import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.picketlink.idm.model.User;

/**
 * LDAP Representation of an {@link User}
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class LDAPUser extends DirContextAdaptor implements User {

    private static final long serialVersionUID = 1L;

    protected String userid, firstName, lastName, fullName, email;

    // protected transient ManagedAttributeLookup lookup;

    public LDAPUser() {
        Attribute oc = new BasicAttribute(OBJECT_CLASS);

        oc.add("inetOrgPerson");
        oc.add("organizationalPerson");
        oc.add("person");
        oc.add("top");
        oc.add("extensibleObject");

        getLDAPAttributes().put(oc);
    }

    public LDAPUser(Attributes attributes) {
        this();
        addAllLDAPAttributes(attributes);
    }

    public LDAPUser(Attributes attributes, LDAPUserCustomAttributes customAttributes) {
        this(attributes);
        setCustomAttributes(customAttributes);
    }

    // public LDAPUser(String userId, ManagedAttributeLookup lookup) {
    // this();
    // setLookup(lookup);
    // setId(userId);
    // setFullName(userId);
    // }

//    public LDAPUser(String userId, ManagedAttributeLookup lookup) {
//        this();
//        setLookup(lookup);
//        setId(userId);
//    }

//    public ManagedAttributeLookup getLookup() {
//        return lookup;
//    }
//
//    public void setLookup(ManagedAttributeLookup lookup) {
//        this.lookup = lookup;
//    }

    @SuppressWarnings("rawtypes")
    @Override
    public Attributes getAttributes(String name) throws NamingException {
        Attributes collectiveAttributes = new BasicAttributes(true);
        NamingEnumeration ne = getLDAPAttributes().getAll();
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

//    @Override
//    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(String name) {
//        if (lookup == null) {
//            throw new IllegalStateException("ManagedAttributeLookup injection has not happened");
//        }
//        if (lookup.isManaged(name) == false) {
//            // FIXME
//            // return customAttributes.getAttribute(name);
//
//        }
//        return super.getAttribute(name);
//    }

    // TODO methods no longer required?
    /*
     * @Override public String[] getAttributeValues(String name) { if(lookup == null){ throw new
     * IllegalStateException("ManagedAttributeLookup injection has not happened"); } if (lookup.isManaged(name) == false) {
     * Object value = customAttributes.getAttribute(name); if (value instanceof String[]) { return (String[]) value; } else {
     * return new String[] { (String) value }; } } return super.getAttributeValues(name); }
     * 
     * @Override public Map<String, String[]> getAttributes() { Map<String, String[]> map = super.getAttributes(); Map<String,
     * Object> values = customAttributes.getAttributes(); Set<String> keys = values.keySet(); for (String key : keys) { Object
     * value = values.get(key); if (value instanceof String[]) { map.put(key, (String[]) value); } else if (value instanceof
     * String) { String[] arr = new String[] { (String) value }; map.put(key, arr); } } return map; }
     */

//    @Override
//    public void setAttribute(org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
//        if (lookup == null) {
//            throw new IllegalStateException("ManagedAttributeLookup injection has not happened");
//        }
//        if (lookup.isManaged(attribute.getName())) {
//            super.setAttribute(attribute);
//        } else {
//            // FIXME
//            // setCustomAttribute(name, value);
//        }
//    }

    // TODO method no longer required
    /*
     * @Override public void setAttribute(String name, String[] values) { if(lookup == null){ throw new
     * IllegalStateException("ManagedAttributeLookup injection has not happened"); } if (lookup.isManaged(name)) {
     * super.setAttribute(name, values); } else { setCustomAttribute(name, values); } }
     */

    public void setId(String id) {
        this.userid = id;
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
        try {
//            if (firstName == null) {
                Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.GIVENNAME);
                if (theAttribute != null) {
//                    firstName = (String) theAttribute.get();
                    return (String) theAttribute.get();
                }
//            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    @Override
    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.GIVENNAME);

//        if (theAttribute == null) {
            getLDAPAttributes().put(LDAPConstants.GIVENNAME, firstName);
//        } else {
//            replaceAttribute(LDAPConstants.GIVENNAME, firstName);
//        }

//        Attribute cnAttribute = getLDAPAttributes().get(CN);

//        if (cnAttribute != null) {
//            replaceAttribute(CN, firstName);
//        }

//        getLDAPAttributes().put(CN, firstName);
    }

    @Override
    public String getLastName() {
        try {
//            if (lastName == null) {
                Attribute theAttribute = getLDAPAttributes().get(SN);
                if (theAttribute != null) {
//                    lastName = (String) theAttribute.get();
                    return (String) theAttribute.get();
                }
//            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    @Override
    public void setLastName(String lastName) {
//        this.lastName = lastName;
//        Attribute theAttribute = getLDAPAttributes().get(SN);
//
//        if (theAttribute == null) {
            getLDAPAttributes().put(SN, lastName);
//        } else {
//            theAttribute.clear();
//            theAttribute.add(lastName);
//            // theAttribute.set(0, lastName);
//        }
//
//        Attribute cnAttribute = getLDAPAttributes().get(CN);
//
//        if (cnAttribute == null) {
//            cnAttribute = new BasicAttribute(CN, lastName);
//            getLDAPAttributes().put(cnAttribute);
//        } else {
//            try {
//                replaceAttribute(SN, lastName);
//                replaceAttribute(CN, cnAttribute.get().toString() + " " + lastName);
//            } catch (NamingException e) {
//                throw new RuntimeException("Could not set user's last name.", e);
//            }
//        }
    }

    @Override
    public String getFullName() {
        try {
//            if (fullName == null) {
                Attribute theAttribute = getLDAPAttributes().get(CN);
                
                if (theAttribute != null) {
//                    fullName = (String) theAttribute.get();
                    return (String) theAttribute.get();
                }
//            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    public void setFullName(String fullName) {
//        this.fullName = fullName;
//
//        Attribute theAttribute = getLDAPAttributes().get(CN);
//
//        if (theAttribute == null) {
            getLDAPAttributes().put(CN, fullName);
//        } else {
//            theAttribute.set(0, fullName);
//        }
//
    }

    @Override
    public String getEmail() {
        try {
//            if (email == null) {
                Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.EMAIL);
                if (theAttribute != null) {
//                    email = (String) theAttribute.get();
                    return (String) theAttribute.get();
                }
//            }
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
        return null;
    }

    @Override
    public void setEmail(String email) {
//        this.email = email;
//        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.EMAIL);
//        if (theAttribute == null) {
            setAttribute(new org.picketlink.idm.model.Attribute<String>(LDAPConstants.EMAIL, email));
//        } else {
//            replaceAttribute(LDAPConstants.EMAIL, email);
//        }
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