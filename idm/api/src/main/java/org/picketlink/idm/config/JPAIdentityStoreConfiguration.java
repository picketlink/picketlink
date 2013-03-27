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

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * This interface defines the configuration parameters for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 *
 */
public class JPAIdentityStoreConfiguration extends BaseAbstractStoreConfiguration<JPAIdentityStoreConfiguration> {

    // Discriminator constants
    private static final String DEFAULT_USER_IDENTITY_DISCRIMINATOR = "USER";
    private static final String DEFAULT_ROLE_IDENTITY_DISCRIMINATOR = "ROLE";
    private static final String DEFAULT_GROUP_IDENTITY_DISCRIMINATOR = "GROUP";
    private static final String DEFAULT_AGENT_IDENTITY_DISCRIMINATOR = "AGENT";

    private String identityTypeAgent = DEFAULT_AGENT_IDENTITY_DISCRIMINATOR;
    private String identityTypeUser = DEFAULT_USER_IDENTITY_DISCRIMINATOR;
    private String identityTypeRole = DEFAULT_ROLE_IDENTITY_DISCRIMINATOR;
    private String identityTypeGroup = DEFAULT_GROUP_IDENTITY_DISCRIMINATOR;

    /**
     * Model properties
     */
    private Map<PropertyType, Property<Object>> modelProperties = new HashMap<PropertyType, Property<Object>>();

    /*
     * Attribute properties
     */
    private Map<String, MappedAttribute> attributeProperties = new HashMap<String, MappedAttribute>();

    /**
     * Entity classes
     */
    private Class<?> identityClass;
    private Class<?> attributeClass;
    private Class<?> credentialClass;
    private Class<?> credentialAttributeClass;
    private Class<?> relationshipClass;
    private Class<?> relationshipIdentityClass;
    private Class<?> relationshipAttributeClass;
    private Class<?> partitionClass;

    @Override
    protected void initConfig() throws SecurityConfigurationException {
        if (this.identityClass == null) {
            throw MESSAGES.jpaConfigIdentityClassNotProvided();
        }

        if (this.partitionClass == null) {
            throw MESSAGES.jpaConfigPartitionClassNotProvided();
        }

        configureIdentity();
        configurePartitions();
        configureRelationships();
        configureCredentials();
    }

    public Class<?> getIdentityClass() {
        return identityClass;
    }

    public JPAIdentityStoreConfiguration setIdentityClass(Class<?> identityClass) {
        this.identityClass = identityClass;
        return this;
    }

    public Class<?> getCredentialClass() {
        return credentialClass;
    }

    public JPAIdentityStoreConfiguration setCredentialClass(Class<?> credentialClass) {
        this.credentialClass = credentialClass;
        return this;
    }

    public Class<?> getCredentialAttributeClass() {
        return this.credentialAttributeClass;
    }

    public JPAIdentityStoreConfiguration setCredentialAttributeClass(Class<?> credentialAttributeClass) {
        this.credentialAttributeClass = credentialAttributeClass;
        return this;
    }

    public Class<?> getRelationshipClass() {
        return this.relationshipClass;
    }

    public Class<?> getPartitionClass() {
        return this.partitionClass;
    }

    public JPAIdentityStoreConfiguration setRelationshipClass(Class<?> relationshipClass) {
        this.relationshipClass = relationshipClass;
        return this;
    }

    public JPAIdentityStoreConfiguration setPartitionClass(Class<?> partitionClass) {
        this.partitionClass = partitionClass;
        return this;
    }

    public Class<?> getRelationshipIdentityClass() {
        return relationshipIdentityClass;
    }

    public JPAIdentityStoreConfiguration setRelationshipIdentityClass(Class<?> relationshipIdentityClass) {
        this.relationshipIdentityClass = relationshipIdentityClass;
        return this;
    }

    public Class<?> getRelationshipAttributeClass() {
        return relationshipAttributeClass;
    }

    public JPAIdentityStoreConfiguration setRelationshipAttributeClass(Class<?> relationshipAttributeClass) {
        this.relationshipAttributeClass = relationshipAttributeClass;
        return this;
    }

    public Class<?> getAttributeClass() {
        return attributeClass;
    }

    public JPAIdentityStoreConfiguration setAttributeClass(Class<?> attributeClass) {
        this.attributeClass = attributeClass;
        return this;
    }

    public boolean isConfigured() {
        return identityClass != null;
    }

    private class PropertyTypeCriteria implements PropertyCriteria {
        private PropertyType pt;

        public PropertyTypeCriteria(PropertyType pt) {
            this.pt = pt;
        }

        public boolean fieldMatches(Field f) {
            return f.isAnnotationPresent(IDMProperty.class) && f.getAnnotation(IDMProperty.class).value().equals(pt);
        }

        public boolean methodMatches(Method m) {
            return m.isAnnotationPresent(IDMProperty.class) && m.getAnnotation(IDMProperty.class).value().equals(pt);
        }
    }

    /**
     * Maps attributes to properties that are spread across the object model
     *
     */
    public class MappedAttribute {
        /**
         * The property of the IdentityObject class that references the object that contains the attribute property
         */
        private Property<Object> identityProperty;

        /**
         * The property of the mapped object that contains the attribute value
         */
        private Property<Object> attributeProperty;

        public MappedAttribute(Property<Object> identityProperty, Property<Object> attributeProperty) {
            this.identityProperty = identityProperty;
            this.attributeProperty = attributeProperty;
        }

        public Property<Object> getIdentityProperty() {
            return identityProperty;
        }

        public Property<Object> getAttributeProperty() {
            return attributeProperty;
        }
    }

    public Property<Object> getModelProperty(PropertyType propertyType) {
        return modelProperties.get(propertyType);
    }

    public <P> P getModelPropertyValue(Class<P> propertyClass, Object instance, PropertyType propertyType) {
        @SuppressWarnings("unchecked")
        Property<P> property = (Property<P>) getModelProperty(propertyType);
        return property == null ? null : property.getValue(instance);
    }

    public void setModelPropertyValue(Object instance, PropertyType propertyType, Object value) {
        setModelPropertyValue(instance, propertyType, value, false);
    }

    public void setModelPropertyValue(Object instance, PropertyType propertyType, Object value, boolean required) {
        if (isModelPropertySet(propertyType)) {
            getModelProperty(propertyType).setValue(instance, value);
        } else if (required) {
            throw MESSAGES.jpaConfigModelPropertyNotConfigured(propertyType.name());
        }
    }

    public Map<String, MappedAttribute> getAttributeProperties() {
        return attributeProperties;
    }

    private String getIdentityTypeUser() {
        return identityTypeUser;
    }

    private String getIdentityTypeGroup() {
        return identityTypeGroup;
    }

    private String getIdentityTypeRole() {
        return identityTypeRole;
    }

    private String getIdentityTypeAgent() {
        return identityTypeAgent;
    }

    public String getIdentityTypeDiscriminator(Class<? extends IdentityType> identityType) {
        String discriminator = null;

        if (User.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeUser();
        } else if (Agent.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeAgent();
        } else if (Role.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeRole();
        } else if (Group.class.isAssignableFrom(identityType)) {
            discriminator = getIdentityTypeGroup();
        } else {
            throw MESSAGES.jpaConfigDiscriminatorNotFoundForIdentityType(identityType);
        }

        return discriminator;
    }

    public Class<? extends IdentityType> getIdentityTypeFromDiscriminator(String discriminator) {
        Class<? extends IdentityType> type = null;

        if (getIdentityTypeUser().equals(discriminator)) {
            type = User.class;
        } else if (getIdentityTypeAgent().equals(discriminator)) {
            type = Agent.class;
        } else if (getIdentityTypeRole().equals(discriminator)) {
            type = Role.class;
        } else if (getIdentityTypeGroup().equals(discriminator)) {
            type = Group.class;
        } else {
            throw new IdentityManagementException("Discriminator [" + discriminator + "] does not map to an IdentityType.");
        }

        return type;
    }

    private void configureCredentials() {
        if (this.credentialClass != null && this.credentialAttributeClass != null) {
            configureModelProperty(PropertyType.CREDENTIAL_TYPE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_VALUE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_IDENTITY, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE, credentialClass, null);

            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME, credentialAttributeClass, String.class);
            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE, credentialAttributeClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL, credentialAttributeClass, credentialClass);
        } else {
            LOGGER.jpaConfigDisablingCredentialFeatures();
            getFeatureSet().removeFeature(FeatureGroup.credential);
        }
    }

    private void configureIdentity() throws SecurityConfigurationException {
        configureModelProperty(PropertyType.IDENTITY_DISCRIMINATOR, identityClass, null, "discriminator", "identityType",
                "identityTypeName", "typeName", "type");
        configureModelProperty(PropertyType.IDENTITY_ID, identityClass, null, "id", "identifier");
        configureModelProperty(PropertyType.IDENTITY_NAME, identityClass, null, "name");
        configureModelProperty(PropertyType.GROUP_PARENT, identityClass, null, "parentGroup", "parent");
        configureModelProperty(PropertyType.GROUP_PATH, identityClass, null, "groupPath", "path");
        configureModelProperty(PropertyType.IDENTITY_ENABLED, identityClass, null, "enabled", "active");
        configureModelProperty(PropertyType.IDENTITY_CREATION_DATE, identityClass, null, false, "created", "creationDate");
        configureModelProperty(PropertyType.IDENTITY_EXPIRY_DATE, identityClass, null, false, "expires", "expiryDate");
        configureModelProperty(PropertyType.IDENTITY_PARTITION, identityClass, null, false, "partition");
        configureModelProperty(PropertyType.AGENT_LOGIN_NAME, identityClass, null, "loginName", "login");
        configureUserProperties();
        configureAttributes();
    }

    private void configurePartitions() {
        if (this.partitionClass != null) {
            configureModelProperty(PropertyType.PARTITION_ID, partitionClass, null, "id", "id");
            configureModelProperty(PropertyType.PARTITION_TYPE, partitionClass, null, "type", "partitionType");
            configureModelProperty(PropertyType.PARTITION_PARENT, partitionClass, null, "parent");
        } else {
            LOGGER.jpaConfigDisablingPartitionFeatures();
            getFeatureSet().removeFeature(FeatureGroup.realm);
            getFeatureSet().removeFeature(FeatureGroup.tier);
        }
    }

    /**
     * Scan for various optional user properties, such as first name, last name and e-mail address.
     *
     * @throws SecurityConfigurationException
     */
    private void configureUserProperties() throws SecurityConfigurationException {
        configureModelProperty(PropertyType.USER_FIRST_NAME, identityClass, null, false, "firstName");
        configureModelProperty(PropertyType.USER_LAST_NAME, identityClass, null, false, "lastName");
        configureModelProperty(PropertyType.USER_EMAIL, identityClass, null, false, "email");
    }

    /*
     * Configures properties for reading and writing identity relationships. As this is an optional feature, the specified
     * relationshipClass property may be left as null in which case no configuration will occur.
     */
    private void configureRelationships() throws SecurityConfigurationException {
        if (this.relationshipClass != null && this.relationshipIdentityClass != null && this.relationshipAttributeClass != null) {
            configureModelProperty(PropertyType.RELATIONSHIP_ID, relationshipClass, null, "id");
            configureModelProperty(PropertyType.RELATIONSHIP_CLASS, relationshipClass, null, "relationshipClass");

            configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY_ID, relationshipIdentityClass, null, "identityObjectId");
            configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY, relationshipIdentityClass, null, "identityObject");
            configureModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR, relationshipIdentityClass, null, "descriptor");
            configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP, relationshipIdentityClass,
                    relationshipClass);

            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME, relationshipAttributeClass, null, "attributeName",
                    "name");
            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE, relationshipAttributeClass, null,
                    "attributeValue", "value");
            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP, relationshipAttributeClass, null);
        } else {
            LOGGER.jpaConfigDisablingRelationshipFeatures();
            getFeatureSet().removeFeature(FeatureGroup.relationship);
        }
    }

    /**
     * Configures the identity store for reading and writing attribute values
     *
     * @throws SecurityConfigurationException
     */
    private void configureAttributes() throws SecurityConfigurationException {
        // If an attribute class has been configured, scan it for attribute properties
        if (attributeClass == null) {
            return;
        }

        configureModelProperty(PropertyType.ATTRIBUTE_IDENTITY, attributeClass, identityClass);
        configureModelProperty(PropertyType.ATTRIBUTE_NAME, attributeClass, null, "attributeName", "name");
        configureModelProperty(PropertyType.ATTRIBUTE_TYPE, attributeClass, null, "attributeType", "type");
        configureModelProperty(PropertyType.ATTRIBUTE_VALUE, attributeClass, null, "attributeValue", "value");

        // Scan for attribute properties in the identity class
        List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                .addCriteria(new AnnotatedPropertyCriteria(IDMAttribute.class)).getResultList();

        for (Property<Object> p : props) {
            String attribName = p.getAnnotatedElement().getAnnotation(IDMAttribute.class).name();

            if (attributeProperties.containsKey(attribName)) {
                Property<Object> other = attributeProperties.get(attribName).getAttributeProperty();

                throw MESSAGES.jpaConfigMultiplePropertiesForAttribute(attribName, other.getDeclaringClass(),
                        other.getAnnotatedElement(), p.getDeclaringClass(), p.getAnnotatedElement());
            }

            attributeProperties.put(attribName, new MappedAttribute(null, p));
        }
    }

    private void configureModelProperty(PropertyType propertyType, Class<?> targetClass, Class<?> propertyClass,
            String... possibleNames) {
        configureModelProperty(propertyType, targetClass, propertyClass, false, possibleNames);
    }

    private void configureModelProperty(PropertyType propertyType, Class<?> targetClass, Class<?> propertyClass,
            boolean optional, String... possibleNames) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(targetClass);

        if (propertyType != null) {
            query.addCriteria(new PropertyTypeCriteria(propertyType));
        }

        if (propertyClass != null) {
            query.addCriteria(new TypedPropertyCriteria(propertyClass));
        }

        List<Property<Object>> props = query.getResultList();

        if (props.size() == 1) {
            modelProperties.put(propertyType, props.get(0));
        } else if (props.size() > 1) {
            throw MESSAGES.jpaConfigAmbiguosPropertyForClass(propertyType.name(), targetClass);
        } else {
            if (possibleNames != null && possibleNames.length > 0) {
                Property<Object> p = findNamedProperty(targetClass, possibleNames);

                if (p != null) {
                    modelProperties.put(propertyType, p);
                }
            }

            if (!optional) {
                throw new SecurityConfigurationException("Error configuring JPAIdentityStore - no " + propertyType.name()
                        + " property found in identity class [" + targetClass.getName() + "]");
            }
        }
    }

    private Property<Object> findNamedProperty(Class<?> targetClass, String... allowedNames) {
        List<Property<Object>> props = PropertyQueries.createQuery(targetClass)
                .addCriteria(new TypedPropertyCriteria(String.class)).addCriteria(new NamedPropertyCriteria(allowedNames))
                .getResultList();

        for (String name : allowedNames) {
            for (Property<Object> prop : props) {
                if (name.equals(prop.getName()))
                    return prop;
            }
        }

        return null;
    }

    private boolean isModelPropertySet(PropertyType propertyType) {
        return modelProperties.containsKey(propertyType);
    }

}
