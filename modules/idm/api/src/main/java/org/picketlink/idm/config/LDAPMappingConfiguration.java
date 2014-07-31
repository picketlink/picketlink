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
package org.picketlink.idm.config;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author pedroigor
 */
public class LDAPMappingConfiguration {

    private final Class<? extends AttributedType> mappedClass;
    private final Set<String> objectClasses;
    private final String baseDN;
    private final Map<String, String> mappedProperties;
    private final Property<String> idProperty;
    private final Class<? extends AttributedType> relatedAttributedType;
    private final String parentMembershipAttributeName;
    private final Map<String, String> parentMapping;
    private final Set<String> readOnlyAttributes;
    private final int hierarchySearchDepth;
    private final Property<String> bindingProperty;

    LDAPMappingConfiguration(Class<? extends AttributedType> mappedClass,
        Set<String> objectClasses,
        String baseDN,
        String idPropertyName,
        final String bindingPropertyName,
        Map<String, String> mappedProperties,
        Set<String> readOnlyAttributes,
        Map<String, String> parentMapping,
        Class<? extends AttributedType> relatedAttributedType,
        String parentMembershipAttributeName,
        int hierarchySearchDepth) {
        this.mappedClass = mappedClass;
        this.objectClasses = objectClasses;
        this.baseDN = baseDN;
        this.mappedProperties = mappedProperties;
        this.readOnlyAttributes = readOnlyAttributes;
        this.parentMapping = parentMapping;
        this.hierarchySearchDepth = hierarchySearchDepth;

        if (idPropertyName != null) {
            this.idProperty = PropertyQueries
                .<String>createQuery(getMappedClass())
                .addCriteria(new NamedPropertyCriteria(idPropertyName)).getFirstResult();
        } else {
            this.idProperty = null;
        }

        if (IdentityType.class.isAssignableFrom(mappedClass) && idProperty == null) {
            throw new SecurityConfigurationException("Id attribute not mapped to any property of [" + mappedClass + "].");
        }

        Property bindingProperty = this.idProperty;

        if (bindingPropertyName != null) {
            bindingProperty = getBindingProperty(bindingPropertyName);
        }

        this.bindingProperty = bindingProperty;
        this.relatedAttributedType = relatedAttributedType;
        this.parentMembershipAttributeName = parentMembershipAttributeName;
    }

    public Class<? extends AttributedType> getMappedClass() {
        return this.mappedClass;
    }

    public Set<String> getObjectClasses() {
        return this.objectClasses;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public Map<String, String> getMappedProperties() {
        return this.mappedProperties;
    }

    public Property<String> getIdProperty() {
        return this.idProperty;
    }

    public Property<String> getBindingProperty() {
        return this.bindingProperty;
    }

    public Class<? extends AttributedType> getRelatedAttributedType() {
        return this.relatedAttributedType;
    }

    public String getParentMembershipAttributeName() {
        return this.parentMembershipAttributeName;
    }

    public Map<String, String> getParentMapping() {
        return this.parentMapping;
    }

    public Set<String> getReadOnlyAttributes() {
        return this.readOnlyAttributes;
    }

    public int getHierarchySearchDepth() {
        return this.hierarchySearchDepth;
    }

    private Property getBindingProperty(final String bindingPropertyName) {
        Property bindingProperty = PropertyQueries
                .<String>createQuery(getMappedClass())
                .addCriteria(new NamedPropertyCriteria(bindingPropertyName)).getFirstResult();

        // We don't have Java property, so actually delegate to setAttribute/getAttribute
        if (bindingProperty == null) {
            bindingProperty = new Property<String>() {

                @Override
                public String getName() {
                    return bindingPropertyName;
                }

                @Override
                public Type getBaseType() {
                    return null;
                }

                @Override
                public Class<String> getJavaClass() {
                    return String.class;
                }

                @Override
                public AnnotatedElement getAnnotatedElement() {
                    return null;
                }

                @Override
                public Member getMember() {
                    return null;
                }

                @Override
                public String getValue(Object instance) {
                    if (!(instance instanceof AttributedType)) {
                        throw new IllegalStateException("Instance [ " + instance + " ] not an instance of AttributedType");
                    }

                    AttributedType attributedType = (AttributedType) instance;
                    Attribute<String> attr = attributedType.getAttribute(bindingPropertyName);
                    return attr!=null ? attr.getValue() : null;
                }

                @Override
                public void setValue(Object instance, String value) {
                    if (!(instance instanceof AttributedType)) {
                        throw new IllegalStateException("Instance [ " + instance + " ] not an instance of AttributedType");
                    }

                    AttributedType attributedType = (AttributedType) instance;
                    attributedType.setAttribute(new Attribute(bindingPropertyName, value));
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
                public boolean isAnnotationPresent(Class annotation) {
                    return false;
                }
            };
        }

        return bindingProperty;
    }
}