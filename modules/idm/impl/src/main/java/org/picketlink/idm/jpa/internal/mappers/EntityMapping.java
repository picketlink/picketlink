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

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public class EntityMapping {

    private final Map<Property, Property> properties;
    private final Class<?> supportedType;
    private Property typeProperty;
    private boolean persist = true;

    public EntityMapping(Class<?> managedType, boolean persist) {
        this(managedType);
        this.persist = persist;
    }

    public EntityMapping(Class<?> managedType) {
        this.supportedType = managedType;
        this.properties = new HashMap<Property, Property>();
    }

    public void addProperty(Property property, Property mappedProperty) {
        if (mappedProperty != null) {
            this.properties.put(property, mappedProperty);
        }
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

    public boolean isPersist() {
        return this.persist;
    }

    public Map<Property, Property> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    public Property getTypeProperty() {
        return this.typeProperty;
    }

    public Class<?> getOwnerType() {
        for (Property property : getProperties().values()) {
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

        if (ownerProperty == null) {
            throw MESSAGES.configJpaStoreRequiredMappingAnnotation(entityType, OwnerReference.class);
        }

        addProperty(new PropertyMapping() {

            @Override
            public Object getValue(Object instance) {
                IdentityManaged identityManaged =
                        (IdentityManaged) ownerProperty.getJavaClass().getAnnotation(IdentityManaged.class);

                if (identityManaged != null) {
                    for (Class<?> ownerType : identityManaged.value()) {
                        Property<Object> ownerProperty = PropertyQueries
                                .createQuery(instance.getClass())
                                .addCriteria(new TypedPropertyCriteria(ownerType, MatchOption.ALL))
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
                            .addCriteria(new TypedPropertyCriteria(ownerType, MatchOption.ALL))
                            .getFirstResult();

                    if (ownerProperty != null && ownerType.isInstance(value)) {
                        ownerProperty.setValue(instance, value);
                        return;
                    }
                }
            }
        }, ownerProperty);
    }

    public void addProperty(final String propertyName, Property mappedProperty) {
        addProperty(new PropertyMapping() {

            @Override
            public String getName() {
                return propertyName;
            }

            @Override
            public Object getValue(Object instance) {
                if (instance != null) {
                    Property<Object> property = PropertyQueries
                            .createQuery(instance.getClass())
                            .addCriteria(new NamedPropertyCriteria(propertyName))
                            .getFirstResult();

                    if (property != null) {
                        return property.getValue(instance);
                    }
                }

                return null;
            }

            @Override
            public void setValue(Object instance, Object value) {
                if (instance != null) {
                    Property<Object> property = PropertyQueries
                            .createQuery(instance.getClass())
                            .addCriteria(new NamedPropertyCriteria(propertyName))
                            .getFirstResult();

                    if (property != null) {
                        property.setValue(instance, value);
                    }
                }
            }
        }, mappedProperty);
    }

    public boolean supports(Class<?> type) {
        return this.supportedType.isAssignableFrom(type);
    }

    public Class<?> getSupportedType() {
        return this.supportedType;
    }

    public void addMappedProperty(final Property mappedProperty) {
        addProperty(new PropertyMapping() {
            @Override
            public Object getValue(Object instance) {
                return null;
            }

            @Override
            public void setValue(Object instance, Object value) {
                //TODO: Implement setValue
            }
        }, mappedProperty);
    }

    public void addNotNullMappedProperty(final Property mappedProperty) {
        addProperty(new PropertyMapping() {
            @Override
            public Object getValue(Object instance) {
                return null;
            }

            @Override
            public void setValue(Object instance, Object value) {
                //TODO: Implement setValue
            }
        }, new PropertyMapping() {
                        @Override
                        public String getName() {
                            return mappedProperty.getName();
                        }

                        @Override
                        public AnnotatedElement getAnnotatedElement() {
                            return mappedProperty.getAnnotatedElement();
                        }

                        @Override
                        public Class getJavaClass() {
                            return mappedProperty.getJavaClass();
                        }

                        @Override
                        public Object getValue(final Object instance) {
                            return mappedProperty.getValue(instance);
                        }

                        @Override
                        public void setValue(final Object instance, final Object value) {
                            if (value != null) {
                                mappedProperty.setValue(instance, value);
                            }
                        }
                    });
    }

    private abstract class PropertyMapping<V> implements Property<V> {

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
            return null;
        }

        @Override
        public Class<?> getDeclaringClass() {
            return null;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setAccessible() {
        }

        @Override
        public boolean isAnnotationPresent(final Class<? extends Annotation> annotation) {
            return false;
        }
    }
}
