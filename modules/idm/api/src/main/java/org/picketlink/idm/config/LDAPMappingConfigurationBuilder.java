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

package org.picketlink.idm.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.config.annotation.ParameterConfigID;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;

/**
 * @author Pedro Igor
 */
public class LDAPMappingConfigurationBuilder extends
        AbstractIdentityConfigurationChildBuilder<LDAPMappingConfiguration> {

    private final LDAPStoreConfigurationBuilder ldapStoreBuilder;
    private Class<? extends AttributedType> mappedClass;
    private final Set<String> objectClasses = new HashSet<String>();
    private String baseDN;
    private String idPropertyName;
    private final Map<String, String> mappedProperties = new HashMap<String, String>();
    private final Set<String> readOnlyAttributes = new HashSet<String>();
    private final Map<String, String> parentMapping = new HashMap<String, String>();
    private Class<? extends AttributedType> relatedAttributedType;
    private String parentMembershipAttributeName;

    public LDAPMappingConfigurationBuilder(Class<? extends AttributedType> attributedType, LDAPStoreConfigurationBuilder builder) {
        super(builder);
        this.mappedClass = attributedType;
        this.ldapStoreBuilder = builder;
    }

    @Override
    protected LDAPMappingConfiguration create() {
        return new LDAPMappingConfiguration(
                this.mappedClass,
                this.objectClasses,
                this.baseDN,
                this.idPropertyName,
                this.mappedProperties,
                this.readOnlyAttributes,
                this.parentMapping,
                this.relatedAttributedType,
                this.parentMembershipAttributeName);
    }

    @Override
    protected void validate() {
        if (this.mappedClass == null) {
            throw new SecurityConfigurationException("Mapped class not provided.");
        }

        if (!Relationship.class.isAssignableFrom(this.mappedClass)) {
            if (isNullOrEmpty(this.baseDN)) {
                throw new SecurityConfigurationException("No base DN provided for mapped class [" + this.mappedClass + "].");
            }

            if (isNullOrEmpty(this.idPropertyName)) {
                throw new SecurityConfigurationException("No attribute provided as the identifier for mapped class [" + this.mappedClass + "].");
            }
        }

        for (String propertyName : this.mappedProperties.keySet()) {
            Property<String> property =
                    PropertyQueries.<String>createQuery(this.mappedClass).addCriteria(new NamedPropertyCriteria(propertyName)).getFirstResult();

            if (property == null) {
                throw new SecurityConfigurationException("Could not resolve property [" + propertyName + "] from mapped class [" + this.mappedClass + "].");
            }

            if (!property.getAnnotatedElement().isAnnotationPresent(AttributeProperty.class) && !Relationship.class.isAssignableFrom(this.mappedClass)) {
                throw new SecurityConfigurationException("Mapped properties must be annotated with @AttributeProperty. Property [" + this.mappedClass + "." + propertyName + "].");
            }
        }
    }

    @Override
    protected Builder<LDAPMappingConfiguration> readFrom(LDAPMappingConfiguration fromConfiguration) {
        this.mappedClass = fromConfiguration.getMappedClass();
        this.objectClasses.addAll(fromConfiguration.getObjectClasses());
        this.baseDN = fromConfiguration.getBaseDN();

        if (fromConfiguration.getIdProperty() != null) {
            this.idPropertyName = fromConfiguration.getIdProperty().getName();
        }

        this.mappedProperties.putAll(fromConfiguration.getMappedProperties());
        this.readOnlyAttributes.addAll(fromConfiguration.getReadOnlyAttributes());
        this.parentMapping.putAll(fromConfiguration.getParentMapping());
        this.relatedAttributedType = fromConfiguration.getRelatedAttributedType();
        this.parentMembershipAttributeName = fromConfiguration.getParentMembershipAttributeName();

        return this;
    }

    public LDAPMappingConfigurationBuilder objectClasses(String... objectClasses) {
        this.objectClasses.addAll(Arrays.asList(objectClasses));
        return this;
    }

    public LDAPMappingConfigurationBuilder attribute(@ParameterConfigID(name="propertyName") String propertyName,
                                                     @ParameterConfigID(name="ldapAttributeName") String ldapAttributeName) {
        this.mappedProperties.put(propertyName, ldapAttributeName);
        return this;
    }

    public LDAPMappingConfigurationBuilder readOnlyAttribute(@ParameterConfigID(name="propertyName") String propertyName,
                                                             @ParameterConfigID(name="ldapAttributeName") String ldapAttributeName) {
        this.mappedProperties.put(propertyName, ldapAttributeName);
        this.readOnlyAttributes.add(propertyName);
        return this;
    }

    public LDAPMappingConfigurationBuilder attribute(@ParameterConfigID(name="propertyName") String propertyName,
                                                     @ParameterConfigID(name="ldapAttributeName") String ldapAttributeName,
                                                     @ParameterConfigID(name="identifier") boolean identifier) {
        attribute(propertyName, ldapAttributeName);

        if (identifier) {
            this.idPropertyName = propertyName;
        }

        return this;
    }

    public LDAPMappingConfigurationBuilder mapping(Class<? extends AttributedType> attributedType) {
        return this.ldapStoreBuilder.mapping(attributedType);
    }

    public LDAPMappingConfigurationBuilder baseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }

    public LDAPMappingConfigurationBuilder forMapping(Class<? extends AttributedType> attributedType) {
        this.relatedAttributedType = attributedType;
        return this;
    }

    public LDAPMappingConfigurationBuilder parentMembershipAttributeName(String parentMembershipAttributeName) {
        this.parentMembershipAttributeName = parentMembershipAttributeName;
        return this;
    }

    public LDAPMappingConfigurationBuilder parentMapping(String parentId, String baseDN) {
        this.parentMapping.put(parentId, baseDN);
        return this;
    }
}