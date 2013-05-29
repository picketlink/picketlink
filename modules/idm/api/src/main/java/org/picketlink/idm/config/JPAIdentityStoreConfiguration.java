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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeType;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CreationDate;
import org.picketlink.idm.jpa.annotations.CredentialType;
import org.picketlink.idm.jpa.annotations.CredentialValue;
import org.picketlink.idm.jpa.annotations.Discriminator;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.Email;
import org.picketlink.idm.jpa.annotations.Enabled;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.FirstName;
import org.picketlink.idm.jpa.annotations.GroupPath;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.Identity;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.IdentityName;
import org.picketlink.idm.jpa.annotations.IdentityPartition;
import org.picketlink.idm.jpa.annotations.LastName;
import org.picketlink.idm.jpa.annotations.LoginName;
import org.picketlink.idm.jpa.annotations.Parent;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * This interface defines the configuration parameters for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 *
 */
public class JPAIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

    public enum PropertyType {
        IDENTITY_ID, IDENTITY_CLASS, GROUP_PATH, IDENTITY_NAME, IDENTITY_ENABLED, IDENTITY_CREATION_DATE, IDENTITY_EXPIRY_DATE, CREDENTIAL_VALUE, ATTRIBUTE_IDENTITY, ATTRIBUTE_NAME, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, GROUP_PARENT, AGENT_LOGIN_NAME, USER_FIRST_NAME, USER_LAST_NAME, USER_EMAIL, IDENTITY_PARTITION, PARTITION_ID, PARTITION_TYPE, PARTITION_PARENT, CREDENTIAL_IDENTITY, CREDENTIAL_TYPE, CREDENTIAL_EFFECTIVE_DATE, CREDENTIAL_EXPIRY_DATE, CREDENTIAL_ATTRIBUTE_CREDENTIAL, CREDENTIAL_ATTRIBUTE_NAME, CREDENTIAL_ATTRIBUTE_VALUE, RELATIONSHIP_ID, RELATIONSHIP_CLASS, RELATIONSHIP_IDENTITY, RELATIONSHIP_IDENTITY_RELATIONSHIP, RELATIONSHIP_DESCRIPTOR, RELATIONSHIP_ATTRIBUTE_NAME, RELATIONSHIP_ATTRIBUTE_VALUE, RELATIONSHIP_ATTRIBUTE_RELATIONSHIP
    };

    /**
     * Model properties
     */
    private Map<PropertyType, Property<Object>> modelProperties = new HashMap<PropertyType, Property<Object>>();

    /*
     * Identity Attribute Properties - this Map contains a Map of the formal attribute properties declared on
     * an IdentityType implementation, for example the firstName property of a User
     */
    private Map<Class<? extends IdentityType>, Map<String,Property<Object>>> identityAttributeProperties = 
            new HashMap<Class<? extends IdentityType>, Map<String,Property<Object>>>();

    /*
     * Ad-hoc Attribute properties
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

    JPAIdentityStoreConfiguration(Class<?> identityClass, Class<?> attributeClass, Class<?> credentialClass,
            Class<?> credentialAttributeClass, Class<?> relationshipClass, Class<?> relationshipIdentityClass,
            Class<?> relationshipAttributeClass, Class<?> partitionClass, Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures,
            Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships, Set<String> realms, Set<String> tiers,
            List<ContextInitializer> contextInitializers, Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        super(supportedFeatures, supportedRelationships, realms, tiers, contextInitializers, credentialHandlerProperties,
                credentialHandlers);
        this.identityClass = identityClass;
        this.attributeClass = attributeClass;
        this.credentialAttributeClass = credentialAttributeClass;
        this.credentialClass = credentialClass;
        this.relationshipAttributeClass = relationshipAttributeClass;
        this.relationshipClass = relationshipClass;
        this.relationshipIdentityClass = relationshipIdentityClass;
        this.partitionClass = partitionClass;
    }

    @Override
    protected void initConfig() throws SecurityConfigurationException {
        configureIdentity();
        configurePartitions();
        configureRelationships();
        configureCredentials();
    }

    public Class<?> getIdentityClass() {
        return this.identityClass;
    }

    public Class<?> getCredentialClass() {
        return this.credentialClass;
    }

    public Class<?> getCredentialAttributeClass() {
        return this.credentialAttributeClass;
    }

    public Class<?> getRelationshipClass() {
        return this.relationshipClass;
    }

    public Class<?> getPartitionClass() {
        return this.partitionClass;
    }

    public Class<?> getRelationshipIdentityClass() {
        return this.relationshipIdentityClass;
    }

    public Class<?> getRelationshipAttributeClass() {
        return this.relationshipAttributeClass;
    }


    public Class<?> getAttributeClass() {
        return this.attributeClass;
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

    public Map<String,Property<Object>> getIdentityAttributeProperties(Class<? extends IdentityType> identityClass) {
        if (!identityAttributeProperties.containsKey(identityClass)) {
            Map<String,Property<Object>> attributeProperties = new HashMap<String,Property<Object>>();

            // Scan for identity attribute properties in the identity class
            List<Property<Object>> props = PropertyQueries.createQuery(identityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();
            for (Property<Object> property : props) {
                attributeProperties.put(property.getName(), property);
            }

            identityAttributeProperties.put(identityClass, attributeProperties);
            return attributeProperties;
        } else {
            return identityAttributeProperties.get(identityClass);
        }
    }

    private void configureIdentity() throws SecurityConfigurationException {
        configureModelProperty(PropertyType.IDENTITY_CLASS, IdentityClass.class, identityClass, null, "identityClass",
                "identityType", "identityTypeName", "typeName", "type");

        // Common properties
        configureModelProperty(PropertyType.IDENTITY_ID, Identifier.class, identityClass, null, "id", "identifier");
        configureModelProperty(PropertyType.IDENTITY_NAME, IdentityName.class, identityClass, null, "name");
        configureModelProperty(PropertyType.IDENTITY_ENABLED, Enabled.class, identityClass, null, "enabled", "active");
        configureModelProperty(PropertyType.IDENTITY_CREATION_DATE, CreationDate.class, identityClass, null, false, "created",
                "creationDate");
        configureModelProperty(PropertyType.IDENTITY_EXPIRY_DATE, ExpiryDate.class, identityClass, null, false, "expires",
                "expiryDate");
        configureModelProperty(PropertyType.IDENTITY_PARTITION, IdentityPartition.class, identityClass, null, false,
                "partition");

        // Group properties
        configureModelProperty(PropertyType.GROUP_PARENT, Parent.class, identityClass, null, "parentGroup", "parent");
        configureModelProperty(PropertyType.GROUP_PATH, GroupPath.class, identityClass, null, "groupPath", "path");

        // Agent properties
        configureModelProperty(PropertyType.AGENT_LOGIN_NAME, LoginName.class, identityClass, null, "loginName", "login");

        // User properties
        configureModelProperty(PropertyType.USER_FIRST_NAME, FirstName.class, identityClass, null, false, "firstName");
        configureModelProperty(PropertyType.USER_LAST_NAME, LastName.class, identityClass, null, false, "lastName");
        configureModelProperty(PropertyType.USER_EMAIL, Email.class, identityClass, null, false, "email");

        configureAttributes();
    }

    /**
     * Configures the identity store for reading and writing attribute values
     *
     * @throws SecurityConfigurationException
     */
    private void configureAttributes() throws SecurityConfigurationException {
        // If an attribute class has been configured, scan it for attribute properties
        if (attributeClass != null) {
            configureModelProperty(PropertyType.ATTRIBUTE_IDENTITY, Parent.class, attributeClass, identityClass);
            configureModelProperty(PropertyType.ATTRIBUTE_NAME, AttributeName.class, attributeClass, null, "attributeName",
                    "name");
            configureModelProperty(PropertyType.ATTRIBUTE_TYPE, AttributeType.class, attributeClass, null, "attributeType",
                    "type");
            configureModelProperty(PropertyType.ATTRIBUTE_VALUE, AttributeValue.class, attributeClass, null, "attributeValue",
                    "value");
        }

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

    private void configureCredentials() {
        if (this.credentialClass != null && this.credentialAttributeClass != null) {
            configureModelProperty(PropertyType.CREDENTIAL_TYPE, CredentialType.class, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_VALUE, CredentialValue.class, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_IDENTITY, Parent.class, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE, EffectiveDate.class, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE, ExpiryDate.class, credentialClass, null);
            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME, AttributeName.class, credentialAttributeClass,
                    String.class);
            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE, AttributeValue.class, credentialAttributeClass,
                    null);
            configureModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL, Parent.class, credentialAttributeClass,
                    credentialClass);
        } else {
            LOGGER.jpaConfigDisablingCredentialFeatures();
            removeFeature(FeatureGroup.credential);
        }
    }

    private void configurePartitions() {
        if (this.partitionClass != null) {
            configureModelProperty(PropertyType.PARTITION_ID, Identifier.class, partitionClass, null, "id", "id");
            configureModelProperty(PropertyType.PARTITION_TYPE, Discriminator.class, partitionClass, null, "type",
                    "partitionType");
            configureModelProperty(PropertyType.PARTITION_PARENT, Parent.class, partitionClass, null, "parent");
        } else {
            LOGGER.jpaConfigDisablingPartitionFeatures();
            removeFeature(FeatureGroup.realm);
            removeFeature(FeatureGroup.tier);
        }
    }

    /*
     * Configures properties for reading and writing identity relationships. As this is an optional feature, the specified
     * relationshipClass property may be left as null in which case no configuration will occur.
     */
    private void configureRelationships() throws SecurityConfigurationException {
        if (this.relationshipClass != null && this.relationshipIdentityClass != null && this.relationshipAttributeClass != null) {
            // Base relationship info
            configureModelProperty(PropertyType.RELATIONSHIP_ID, Identifier.class, relationshipClass, null, "id");
            configureModelProperty(PropertyType.RELATIONSHIP_CLASS, RelationshipClass.class, relationshipClass, null,
                    "relationshipClass");

            // Relationship identities
            configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY, Identity.class, relationshipIdentityClass, null,
                    "identityObject");
            configureModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR, RelationshipDescriptor.class,
                    relationshipIdentityClass, null, "descriptor");
            configureModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP, Parent.class, relationshipIdentityClass,
                    relationshipClass);

            // Relationship attributes
            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME, AttributeName.class, relationshipAttributeClass,
                    null, "attributeName", "name");
            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE, AttributeValue.class, relationshipAttributeClass,
                    null, "attributeValue", "value");
            configureModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP, Parent.class, relationshipAttributeClass,
                    null);
        } else {
            LOGGER.jpaConfigDisablingRelationshipFeatures();
            removeFeature(FeatureGroup.relationship);
        }
    }

    private void configureModelProperty(PropertyType propertyType, Class<? extends Annotation> annotationClass,
            Class<?> targetClass, Class<?> propertyClass, String... possibleNames) {
        configureModelProperty(propertyType, annotationClass, targetClass, propertyClass, false, possibleNames);
    }

    private void configureModelProperty(PropertyType propertyType, Class<? extends Annotation> annotationClass,
            Class<?> targetClass, Class<?> propertyClass, boolean optional, String... possibleNames) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(targetClass);

        if (annotationClass != null) {
            query.addCriteria(new AnnotatedPropertyCriteria(annotationClass));
        }

        if (propertyClass != null) {
            query.addCriteria(new TypedPropertyCriteria(propertyClass));
        }

        List<Property<Object>> props = query.getResultList();

        if (props.size() == 1) {
            modelProperties.put(propertyType, props.get(0));
        } else if (props.size() > 1) {
            throw MESSAGES.jpaConfigAmbiguosPropertyForClass(annotationClass.getName(), targetClass);
        } else {
            if (possibleNames != null && possibleNames.length > 0) {
                Property<Object> p = findNamedProperty(targetClass, possibleNames);

                if (p != null) {
                    modelProperties.put(propertyType, p);
                }
            }

            if (!optional) {
                throw new SecurityConfigurationException("Error configuring JPAIdentityStore - no " + annotationClass.getName()
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
