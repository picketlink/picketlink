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
import org.picketlink.idm.model.AttributedType;

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

    LDAPMappingConfiguration(Class<? extends AttributedType> mappedClass,
                             Set<String> objectClasses,
                             String baseDN,
                             String idPropertyName,
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
}