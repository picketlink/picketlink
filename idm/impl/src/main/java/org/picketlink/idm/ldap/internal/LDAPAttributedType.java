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