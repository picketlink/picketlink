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
package org.picketlink.idm.jpa.internal.mappers;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

/**
 * @author pedroigor
 */
public class EntityMapping {

    private final Map<Property, Property> properties;
    private final Class<?> supportedType;
    private final boolean rootMapping;
    private Property typeProperty;

    public EntityMapping(Class<?> managedType, boolean rootMapping) {
        this.supportedType = managedType;
        this.rootMapping = rootMapping;
        this.properties = new HashMap<Property, Property>();
    }

    public EntityMapping(Class<?> supportedType) {
        this(supportedType, false);
    }

    public void addProperty(Property property, Property mappedProperty) {
        this.properties.put(property, mappedProperty);
    }

    public void addTypeProperty(Property property) {
        addProperty(new PropertyMapping() {
            @Override
            public Object getValue(Object instance) {
                return instance.getClass().getName();
            }

            @Override
            public void setValue(Object instance, Object value) {
                //TODO: Implement setValue
            }
        }, property);
        this.typeProperty = property;
    }

    public Map<Property, Property> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public boolean isRootMapping() {
        return this.rootMapping;
    }

    public Property getTypeProperty() {
        return this.typeProperty;
    }

    public Class<?> getOwnerType() {
        for (Property property: getProperties().values()) {
            if (property.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                return property.getJavaClass();
            }
        }

        return null;
    }

    public void addOwnerProperty(Class<?> entityType) {
        final Property<Object> ownerProperty = PropertyQueries
                .createQuery(entityType)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getFirstResult();

        if (ownerProperty != null) {
            addProperty(new PropertyMapping() {

                @Override
                public Object getValue(Object instance) {
                    IdentityManaged identityManaged =
                            (IdentityManaged) ownerProperty.getJavaClass().getAnnotation(IdentityManaged.class);

                    if (identityManaged != null) {
                        for (Class<?> ownerType : identityManaged.value()) {
                            Property<Object> ownerProperty = PropertyQueries
                                    .createQuery(instance.getClass())
                                    .addCriteria(new TypedPropertyCriteria(ownerType, true))
                                    .getFirstResult();

                            if (ownerProperty != null && !ownerProperty.getJavaClass().equals(instance.getClass())) {
                                return ownerProperty.getValue(instance);
                            }
                        }
                    }

                    return instance;
                }

                @Override
                public void setValue(Object instance, Object value) {
                    IdentityManaged identityManaged =
                            (IdentityManaged) ownerProperty.getJavaClass().getAnnotation(IdentityManaged.class);

                    for (Class<?> ownerType : identityManaged.value()) {
                        Property<Object> ownerProperty = PropertyQueries
                                .createQuery(instance.getClass())
                                .addCriteria(new TypedPropertyCriteria(ownerType, true))
                                .getFirstResult();

                        if (ownerProperty != null && ownerType.isInstance(value)) {
                            ownerProperty.setValue(instance, value);
                            return;
                        }
                    }
                }
            }, ownerProperty);
        }
    }

    public void addProperty(final Object getterValue, Property property) {
        addProperty(new PropertyMapping() {

            @Override
            public Object getValue(Object instance) {
                return getterValue;
            }

            @Override
            public void setValue(Object instance, Object value) {
            }
        }, property);
    }

    public boolean supports(Class<?> type) {
        return this.supportedType.isAssignableFrom(type);
    }

    public Class<?> getSupportedType() {
        return this.supportedType;
    }

    public void addMappedProperty(Property mappedProperty) {
        addProperty(new PropertyMapping() {
            @Override
            public Object getValue(Object instance) {
                return null;  //TODO: Implement getValue
            }

            @Override
            public void setValue(Object instance, Object value) {
                //TODO: Implement setValue
            }
        }, mappedProperty);
    }

    private abstract class PropertyMapping implements Property {

        @Override
        public String getName() {
            return "";
        }

        @Override
        public Type getBaseType() {
            return getClass();
        }

        @Override
        public Class getJavaClass() {
            return getClass();
        }

        @Override
        public AnnotatedElement getAnnotatedElement() {
            return getClass();
        }

        @Override
        public Member getMember() {
            return null;  //TODO: Implement getMember
        }

        @Override
        public Class<?> getDeclaringClass() {
            return null;  //TODO: Implement getDeclaringClass
        }

        @Override
        public boolean isReadOnly() {
            return false;  //TODO: Implement isReadOnly
        }

        @Override
        public void setAccessible() {
            //TODO: Implement setAccessible
        }

    }
}
