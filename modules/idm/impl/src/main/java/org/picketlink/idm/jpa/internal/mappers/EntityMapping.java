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

    private final Map<Property, Property> properties = new HashMap<Property, Property>();
    private final Map<Property, Property> referenceProperties = new HashMap<Property, Property>();
    private final Class<?> managedType;
    private final boolean rootMapping;
    private Property typeProperty;

    public EntityMapping(Class<?> managedType, boolean rootMapping) {
        this.managedType = managedType;
        this.rootMapping = rootMapping;
    }

    public EntityMapping(Class<?> managedType) {
        this(managedType, false);
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

    public Class<?> getManagedType() {
        return this.managedType;
    }

    public Map<Property, Property> getProperties() {
        return properties;
    }

    public boolean isRootMapping() {
        return this.rootMapping;
    }

    public Property getTypeProperty() {
        return this.typeProperty;
    }

    public void addReferenceProperty(final Property annotatedProperty) {
        addProperty(new PropertyMapping() {

            @Override
            public Object getValue(Object instance) {
                return instance;
            }

            @Override
            public void setValue(Object instance, Object value) {
                System.out.println(value);
            }
        }, annotatedProperty);
    }

    public Map<Property, Property> getReferenceProperties() {
        return this.referenceProperties;
    }

    public void addSelfReferenceProperty(Property<Object> property, Class<?> entityType) {
        addProperty(property, new PropertyMapping() {
            @Override
            public Object getValue(Object instance) {
                return instance;
            }

            @Override
            public void setValue(Object instance, Object value) {
            }
        });
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
