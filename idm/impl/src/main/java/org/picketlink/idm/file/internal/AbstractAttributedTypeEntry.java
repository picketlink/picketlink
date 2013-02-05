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

package org.picketlink.idm.file.internal;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;

/**
 * @author Pedro Silva
 * 
 */
public abstract class AbstractAttributedTypeEntry<T extends AttributedType> extends AbstractFileEntry<T> {

    private static final long serialVersionUID = -8312773698663190107L;
    
    private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

    protected AbstractAttributedTypeEntry(String version, T object) {
        super(version, object);
    }

    @Override
    protected T doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        T attributedType = doCreateInstance(properties);

        attributedType.setId(properties.get("id").toString());
        
        if (this.attributes == null) {
            this.attributes = new HashMap<String, Serializable>();
        }
        
        Set<Entry<String, Serializable>> entrySet = this.attributes.entrySet();
        
        for (Entry<String, Serializable> entry : entrySet) {
            attributedType.setAttribute(new Attribute<Serializable>(entry.getKey(), entry.getValue()));
        }
        
        return attributedType;
    }

    protected abstract T doCreateInstance(Map<String, Serializable> properties) throws Exception;

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        T attributedType = getEntry();
        
        properties.put("id", attributedType.getId());
    }
    
    @Override
    protected void doWriteObject(ObjectOutputStream s) throws Exception {
        super.doWriteObject(s);
        
        T attributedType = getEntry();
        
        Collection<Attribute<? extends Serializable>> typeAttributes = attributedType.getAttributes();
        
        for (Attribute<? extends Serializable> attribute : typeAttributes) {
            this.attributes.put(attribute.getName(), attribute.getValue());
        }
        
        s.writeObject(this.attributes);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void doReadObject(ObjectInputStream s) throws Exception {
        this.attributes = (Map<String, Serializable>) s.readObject();
    }
}