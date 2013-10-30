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

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.config.annotation.ParameterConfigID;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private int hierarchySearchDepth = 3;

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
                this.parentMembershipAttributeName,
                this.hierarchySearchDepth);
    }

    @Override
    protected void validate() {
        if (this.mappedClass == null) {
            throw new SecurityConfigurationException("Mapped class not provided.");
        }

        if (this.hierarchySearchDepth < 0) {
            throw new SecurityConfigurationException("The hierarchy search depth can not be negative.");
        }

        if (!Relationship.class.isAssignableFrom(this.mappedClass)) {
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

            if (!property.isAnnotationPresent(AttributeProperty.class) && !Relationship.class.isAssignableFrom(this.mappedClass)) {
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
        this.hierarchySearchDepth = fromConfiguration.getHierarchySearchDepth();

        return this;
    }

    /**
     * <p>Defines the object classes for this type.</p>
     *
     * @param objectClasses
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder objectClasses(String... objectClasses) {
        this.objectClasses.addAll(Arrays.asList(objectClasses));
        return this;
    }

    /**
     * <p>Maps a type property to a specific LDAP attribute.</p>
     *
     * @param propertyName
     * @param ldapAttributeName
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder attribute(@ParameterConfigID(name = "propertyName") String propertyName,
                                                     @ParameterConfigID(name = "ldapAttributeName") String ldapAttributeName) {
        this.mappedProperties.put(propertyName, ldapAttributeName);
        return this;
    }

    /**
     * <p>Maps a type property to a specific read-only LDAP attribute.</p>
     *
     * @param propertyName
     * @param ldapAttributeName
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder readOnlyAttribute(@ParameterConfigID(name = "propertyName") String propertyName,
                                                             @ParameterConfigID(name = "ldapAttributeName") String ldapAttributeName) {
        this.mappedProperties.put(propertyName, ldapAttributeName);
        this.readOnlyAttributes.add(propertyName);
        return this;
    }

    /**
     * <p>Maps a type property to a specific LDAP attribute and mark it as an identifier.</p>
     *
     * @param propertyName
     * @param ldapAttributeName
     * @param identifier
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder attribute(@ParameterConfigID(name = "propertyName") String propertyName,
                                                     @ParameterConfigID(name = "ldapAttributeName") String ldapAttributeName,
                                                     @ParameterConfigID(name = "identifier") boolean identifier) {
        attribute(propertyName, ldapAttributeName);

        if (identifier) {
            this.idPropertyName = propertyName;
        }

        return this;
    }

    /**
     * <p>Sets the the search depth level when retrieving the hierarchy (usually the parents) for a type.</p>
     *
     * @param hierarchySearchDepth An int value representing the search depth.
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder hierarchySearchDepth(int hierarchySearchDepth) {
        this.hierarchySearchDepth = hierarchySearchDepth;
        return this;
    }

    /**
     * <p>Maps a specific {@link AttributedType}.</p>
     *
     * @param attributedType
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder mapping(Class<? extends AttributedType> attributedType) {
        return this.ldapStoreBuilder.mapping(attributedType);
    }

    /**
     * <>Sets the base DN for this type.</>
     *
     * @param baseDN
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder baseDN(String baseDN) {
        this.baseDN = baseDN;
        return this;
    }

    /**
     * <p>Associates the given type to a mapped type. This is usually used when configuration relationship types.</p>
     *
     * @param attributedType
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder forMapping(Class<? extends AttributedType> attributedType) {
        this.relatedAttributedType = attributedType;
        return this;
    }

    /**
     * <p>Defines the LDAP attribute name used to create parent-child relationships.</p>
     *
     * @param parentMembershipAttributeName
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder parentMembershipAttributeName(String parentMembershipAttributeName) {
        this.parentMembershipAttributeName = parentMembershipAttributeName;
        return this;
    }

    /**
     * <p>Defines a alternative Base DN in the cases when this type is a child of a parent entry with the given
     * identifier.</p>
     *
     * @param parentId
     * @param baseDN
     *
     * @return
     */
    public LDAPMappingConfigurationBuilder parentMapping(String parentId, String baseDN) {
        this.parentMapping.put(parentId, baseDN);
        return this;
    }
}