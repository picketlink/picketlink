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

package org.picketlink.idm.file.internal;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.annotation.AttributeProperty;

/**
 * @author Pedro Silva
 *
 */
public abstract class AbstractFileAttributedType<T extends AttributedType> extends AbstractFileType<T> {

    private static final long serialVersionUID = -8312773698663190107L;

    private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

    protected AbstractFileAttributedType(String version, T object) {
        super(version, object);
    }

    public String getId() {
        return getEntry().getId();
    }

    protected T doCreateInstance(Map<String, Serializable> properties) throws Exception {
        return (T) Class.forName(getType()).newInstance();
    }

    @Override
    protected T doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        T attributedType = doCreateInstance(properties);

        attributedType.setId(properties.get("id").toString());

        for (Property<Serializable> property: getAttributedProperties(attributedType)) {
            property.setValue(attributedType, properties.get(property.getName()));
        }

        if (this.attributes == null) {
            this.attributes = new HashMap<String, Serializable>();
        }

        Set<Entry<String, Serializable>> entrySet = this.attributes.entrySet();

        for (Entry<String, Serializable> entry : entrySet) {
            attributedType.setAttribute(new Attribute<Serializable>(entry.getKey(), entry.getValue()));
        }

        return attributedType;
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        T attributedType = getEntry();

        properties.put("id", attributedType.getId());

        for (Property<Serializable> property: getAttributedProperties(attributedType)) {
            properties.put(property.getName(), property.getValue(getEntry()));
        }
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

    private List<Property<Serializable>> getAttributedProperties(T attributedType) {
        PropertyQuery<Serializable> query = PropertyQueries.createQuery(attributedType.getClass());

        query.addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class));

        return query.getResultList();
    }

}