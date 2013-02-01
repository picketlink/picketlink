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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;

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

    private static final long serialVersionUID = 7193133057734386770L;
    
    private LDAPCustomAttributes customAttributes;

    private String id;

    public LDAPAttributedType(String dnSuffix) {
        super(dnSuffix);
        this.customAttributes = new LDAPCustomAttributes(dnSuffix);
    }

    public void setId(String id) {
        this.id = id;
//        Attribute theAttribute = getLDAPAttributes().get(ENTRY_UUID);
//
//        if (theAttribute == null) {
//            getLDAPAttributes().put(ENTRY_UUID, id);
//        } else {
//            theAttribute.set(0, id);
//        }
    }

    @Override
    public String getId() {
        return this.id;
//        Attribute theAttribute = getLDAPAttributes().get(ENTRY_UUID);
//
//        if (theAttribute != null) {
//            try {
//                return (String) theAttribute.get();
//            } catch (NamingException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        return null;
    }

    @Override
    public Attributes getAttributes(Name name, String[] ids) throws NamingException {
        return getAttributes(name.toString(), ids);
    }

    @Override
    public void setAttribute(org.picketlink.idm.model.Attribute<? extends Serializable> attribute) {
        getCustomAttributes().addAttribute(attribute.getName(), attribute.getValue());
    }

    @Override
    public void removeAttribute(String name) {
        getCustomAttributes().removeAttribute(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> org.picketlink.idm.model.Attribute<T> getAttribute(String name) {
        org.picketlink.idm.model.Attribute<T> attribute = null;

        if (getCustomAttributes().getAttributes().containsKey(name)) {
            T value = (T) getCustomAttributes().getAttribute(name);
            
            attribute = new org.picketlink.idm.model.Attribute<T>(name, value);
        }

        return attribute;
    }

    @Override
    public Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> getAttributes() {
        Collection<org.picketlink.idm.model.Attribute<? extends Serializable>> attribs = new ArrayList<org.picketlink.idm.model.Attribute<? extends Serializable>>();

        // retrieve all custom attributes
        Map<String, Serializable> customAttributes = getCustomAttributes().getAttributes();
        Set<Entry<String, Serializable>> entrySet = customAttributes.entrySet();

        for (Entry<String, Serializable> entry : entrySet) {
            attribs.add(new org.picketlink.idm.model.Attribute<Serializable>(entry.getKey(), (Serializable) entry.getValue()));
        }

        return attribs;
    }
    
    public LDAPCustomAttributes getCustomAttributes() {
        return this.customAttributes;
    }

    public void setCustomAttributes(LDAPCustomAttributes customAttributes) {
        this.customAttributes = customAttributes;
    }
    
}