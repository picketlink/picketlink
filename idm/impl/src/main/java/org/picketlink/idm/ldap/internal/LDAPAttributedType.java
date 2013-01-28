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

import static org.picketlink.idm.ldap.internal.LDAPConstants.ENTRY_UUID;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

import org.picketlink.idm.internal.util.Base64;
import org.picketlink.idm.model.AttributedType;

/**
 * <p>
 * An adaptor class that provides barebones implementation of the {@link DirContext}.
 * </p>
 * 
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public abstract class LDAPAttributedType extends LDAPEntry implements AttributedType {

    private static final long serialVersionUID = 1L;

    public LDAPAttributedType(String dnSuffix) {
        super(dnSuffix);
    }

    public void setId(String id) {
        Attribute theAttribute = getLDAPAttributes().get(ENTRY_UUID);

        if (theAttribute == null) {
            getLDAPAttributes().put(ENTRY_UUID, id);
        } else {
            theAttribute.set(0, id);
        }
    }

    @Override
    public String getId() {
        Attribute theAttribute = getLDAPAttributes().get(ENTRY_UUID);

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
    public Attributes getAttributes(Name name, String[] ids) throws NamingException {
        return getAttributes(name.toString(), ids);
    }

    @Override
    public void setAttribute(org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        Serializable value = attribute.getValue();

        getLDAPAttributes().put(attribute.getName(), value);
        getCustomAttributes().addAttribute(attribute.getName(), value);
    }

    @Override
    public void removeAttribute(String name) {
        getLDAPAttributes().remove(name);
        getCustomAttributes().removeAttribute(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(String name) {
        try {
            Attribute theAttribute = getLDAPAttributes().get(name);
            Object value = null;

            if (theAttribute != null) {
                value = theAttribute.get();
            } else if (getCustomAttributes().getAttributes().containsKey(name)) {
                value = getCustomAttributes().getAttribute(name);
            } else {
                return null;
            }

            return new org.picketlink.idm.model.Attribute<T>(name, (T) value);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> getAttributes() {
        try {
            Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> attribs = new ArrayList<org.picketlink.idm.model.Attribute<? extends Serializable>>();

            // retrieve all ldap attributes
            NamingEnumeration<? extends Attribute> theAttributes = getLDAPAttributes().getAll();

            while (theAttributes.hasMore()) {
                Attribute anAttribute = theAttributes.next();
                NamingEnumeration<Object> ne = (NamingEnumeration<Object>) anAttribute.getAll();

                List<String> theList = new ArrayList<String>();
                while (ne.hasMoreElements()) {
                    String val = null;
                    Object obj = ne.next();
                    if (obj instanceof byte[]) {
                        val = new String(Base64.encodeBytes((byte[]) obj));
                    } else {
                        val = (String) obj;
                    }
                    theList.add(val);
                }
                String[] valuesArr = new String[theList.size()];
                theList.toArray(valuesArr);

                attribs.add(new org.picketlink.idm.model.Attribute<Serializable>(anAttribute.getID(), valuesArr));
            }

            // retrieve all custom attributes
            Map<String, Serializable> customAttributes = getCustomAttributes().getAttributes();
            Set<Entry<String, Serializable>> entrySet = customAttributes.entrySet();

            for (Entry<String, Serializable> entry : entrySet) {
                attribs.add(new org.picketlink.idm.model.Attribute<Serializable>(entry.getKey(), (Serializable) entry
                        .getValue()));
            }

            return attribs;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }



    public boolean isMember(LDAPAttributedType member) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);

        return memberAttribute != null && memberAttribute.contains(member.getDN());
    }



}