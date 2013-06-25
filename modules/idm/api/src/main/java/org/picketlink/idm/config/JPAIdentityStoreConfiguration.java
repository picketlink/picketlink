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

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CreationDate;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.CredentialValue;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.Enabled;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.PartitionName;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.RelationshipMember;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * Defines the configuration for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 */
public class JPAIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

    /**
     *
     */
    private final PartitionModel partitionModel = new PartitionModel();

    /**
     * The identity model definition
     */
    private final IdentityModel identityModel = new IdentityModel();

    /**
     * Credential model definition
     */
    private CredentialModel credentialModel = new CredentialModel();

    /**
     *
     */
    private RelationshipModel relationshipModel = new RelationshipModel();

    private class PartitionModel {
        private Map<Class<? extends Partition>, ModelDefinition> definitions =
                new HashMap<Class<? extends Partition>, ModelDefinition>();

        private Property<String> partitionClassProperty;

        /**
         * Map between entity classes and their @OwnerReference property
         */
        private final Map<Class<?>, Property<?>> ownerReferences = new HashMap<Class<?>, Property<?>>();

        public ModelDefinition getDefinition(Class<? extends Partition> partitionClass) {
            if (!definitions.containsKey(partitionClass)) {
                definitions.put(partitionClass, new ModelDefinition());
            }
            return definitions.get(partitionClass);
        }

        public void setPartitionClassProperty(Property<String> partitionClassProperty) {
            this.partitionClassProperty = partitionClassProperty;
        }

        public Property<String> getPartitionClassProperty() {
            return partitionClassProperty;
        }

        public void setOwnerReference(Class<?> entityClass, Property<?> ownerReference) {
            ownerReferences.put(entityClass, ownerReference);
        }
    }

    private class IdentityModel {
        /**
         * Maps between specific identity types and their identity mapping metadata
         */
        private final Map<Class<? extends IdentityType>, ModelDefinition> definitions =
                new HashMap<Class<? extends IdentityType>, ModelDefinition>();

        /**
         * Map between entity classes and their @OwnerReference property
         */
        private final Map<Class<?>, Property<?>> ownerReferences = new HashMap<Class<?>, Property<?>>();

        private Property<String> identityClassProperty;

        public ModelDefinition getDefinition(Class<? extends IdentityType> identityClass) {
            if (!definitions.containsKey(identityClass)) {
                definitions.put(identityClass, new ModelDefinition());
            }
            return definitions.get(identityClass);
        }

        public void setIdentityClassProperty(Property<String> identityClassProperty) {
            this.identityClassProperty = identityClassProperty;
        }

        public void setOwnerReference(Class<?> entityClass, Property<?> ownerReference) {
            ownerReferences.put(entityClass, ownerReference);
        }
    }

    private class CredentialModel {
        private final Map<Class<? extends CredentialStorage>, ModelDefinition> definitions =
                new HashMap<Class<? extends CredentialStorage>, ModelDefinition>();

        /**
         * Map between entity classes and their @OwnerReference property
         */
        private final Map<Class<?>, Property<?>> ownerReferences = new HashMap<Class<?>, Property<?>>();

        private Property<String> credentialClassProperty;
        private Property<?> credentialValueProperty;

        public ModelDefinition getDefinition(Class<? extends CredentialStorage> credentialClass) {
            if (!definitions.containsKey(credentialClass)) {
                definitions.put(credentialClass, new ModelDefinition());
            }
            return definitions.get(credentialClass);
        }

        public void setCredentialClassProperty(Property<String> credentialClassProperty) {
            this.credentialClassProperty = credentialClassProperty;
        }

        public void setCredentialValueProperty(Property<?> credentialValueProperty) {
            this.credentialValueProperty = credentialValueProperty;
        }

        public void setOwnerReference(Class<?> entityClass, Property<?> ownerReference) {
            ownerReferences.put(entityClass, ownerReference);
        }
    }

    private class RelationshipModel {
        private final Map<Class<? extends Relationship>, ModelDefinition> definitions =
                new HashMap<Class<? extends Relationship>, ModelDefinition>();

        /**
         * Map between entity classes and their @OwnerReference property
         */
        private final Map<Class<?>, Property<?>> ownerReferences = new HashMap<Class<?>, Property<?>>();

        private Property<String> relationshipClassProperty;
        private Property<?> relationshipMember;
        private Property<?> relationshipDescriptor;

        public ModelDefinition getDefinition(Class<? extends Relationship> relationshipClass) {
            if (!definitions.containsKey(relationshipClass)) {
                definitions.put(relationshipClass, new ModelDefinition());
            }
            return definitions.get(relationshipClass);
        }

        public void setRelationshipMember(Property<?> relationshipMember) {
            this.relationshipMember = relationshipMember;
        }

        public void setRelationshipDescriptor(Property<?> relationshipDescriptor) {
            this.relationshipDescriptor = relationshipDescriptor;
        }

        public void setRelationshipClassProperty(Property<String> relationshipClassProperty) {
            this.relationshipClassProperty = relationshipClassProperty;
        }

        public void setOwnerReference(Class<?> entityClass, Property<?> ownerReference) {
            ownerReferences.put(entityClass, ownerReference);
        }

    }

    /**
     * Each model definition maps between a Property of the identity model and its corresponding
     * entity bean property, and also maps ad-hoc attribute schemas to the identity type.
     */
    private class ModelDefinition {
        /**
         * Maps between a property of an identity model class and its corresponding entity bean property
         */
        private Map<Property, PropertyMapping> properties = new HashMap<Property, PropertyMapping>();

        /**
         * Ad-hoc attribute values
         */
        private Map<Class<?>, AttributeMapping> attributes = new HashMap<Class<?>, AttributeMapping>();

        public void addProperty(Property property, PropertyMapping mapping) {
            properties.put(property, mapping);
        }

        public void addAttribute(Class<?> cls, AttributeMapping mapping) {
            attributes.put(cls, mapping);
        }
    }

    private class PropertyMapping {
        private final Property<?> entityProperty;
        private final Class<?> entityClass;

        public PropertyMapping(Property<?> entityProperty) {
            this.entityProperty = entityProperty;
            this.entityClass = null;
        }

        public PropertyMapping(Class<?> entityClass) {
            this.entityProperty = null;
            this.entityClass = entityClass;
        }

        public Property<?> getEntityProperty() {
            return entityProperty;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }
    }

    private class AttributeMapping {
        private final Property<String> attributeName;
        private final Property<String> attributeClass;
        private final Property<Object> attributeValue;

        public AttributeMapping(Property<String> attributeName,
                Property<String> attributeClass, Property<Object> attributeValue) {
            this.attributeName = attributeName;
            this.attributeClass = attributeClass;
            this.attributeValue = attributeValue;
        }

        public Property<String> getAttributeName() {
            return attributeName;
        }

        public Property<String> getAttributeClass() {
            return attributeClass;
        }

        public Property<Object> getAttributeValue() {
            return attributeValue;
        }
    }

    protected JPAIdentityStoreConfiguration(List<Class<?>> entityClasses, Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures,
            Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships,
            Map<Class<? extends IdentityType>, Set<FeatureOperation>> supportedIdentityTypes, Set<String> realms,
            Set<String> tiers, List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties, List<Class<? extends CredentialHandler>> credentialHandlers) {
        super(supportedFeatures, supportedRelationships, supportedIdentityTypes, realms, tiers, contextInitializers,
                credentialHandlerProperties, credentialHandlers);

        if (entityClasses == null) {
            throw IDMMessages.MESSAGES.jpaConfigNoEntityClassesProvided();
        }

        // We configure the entity classes in order, so the first step is to sort them
        sortEntityClasses(entityClasses);

        // Then for each one, we determine what kind of state it holds and configure accordingly
        for (Class<?> entityClass : entityClasses) {
            if (isPartitionClass(entityClass)) {
                configurePartitionClass(entityClass);
            } else if (isPartitionAttributeClass(entityClass)) {
                configurePartitionAttributeClass(entityClass);
            } else if (isIdentityClass(entityClass)) {
                configureIdentityClass(entityClass);
            } else if (isIdentityAttributeClass(entityClass)) {
                configureIdentityAttributeClass(entityClass);
            } else if (isCredentialClass(entityClass)) {
                configureCredentialClass(entityClass);
            } else if (isCredentialAttributeClass(entityClass)) {
                configureCredentialAttributeClass(entityClass);
            } else if (isRelationshipClass(entityClass)) {
                configureRelationshipClass(entityClass);
            } else if (isRelationshipIdentityClass(entityClass)) {
                configureRelationshipIdentityClass(entityClass);
            } else if (isRelationshipAttributeClass(entityClass)) {
                configureRelationshipAttributeClass(entityClass);
            } else {
                throw IDMMessages.MESSAGES.jpaConfigAmbiguousEntityBean(entityClass);
            }
        }
    }

    @Override
    protected void initConfig() {  }

    /**
     * The entity classes are sorted in the following order:
     *
     * 1. Primary partition entities
     * 2. Partition attribute entities
     * 3. Primary identity entities
     * 4. Identity attribute entities
     * 5. Primary credential entities
     * 6. Credential attribute entities
     * 7. Primary relationship entity
     * 8. Relationship identity entity
     * 9. Relationship attribute entities
     *
     * @param entityClasses
     */
    private void sortEntityClasses(List<Class<?>> entityClasses) {
        Collections.sort(entityClasses, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> arg0, Class<?> arg1) {
                int arg0weight = isPartitionClass(arg0) ? 9 :
                    (isPartitionAttributeClass(arg0) ? 8 :
                    (isIdentityClass(arg0) ? 7 :
                    (isIdentityAttributeClass(arg0) ? 6 :
                    (isCredentialClass(arg0) ? 5 :
                    (isCredentialAttributeClass(arg0) ? 4 :
                    (isRelationshipClass(arg0) ? 3 :
                    (isRelationshipIdentityClass(arg0) ? 2 :
                    (isRelationshipAttributeClass(arg0) ? 1 : 0))))))));

                int arg1weight = isPartitionClass(arg1) ? 9 :
                    (isPartitionAttributeClass(arg1) ? 8 :
                    (isIdentityClass(arg1) ? 7 :
                    (isIdentityAttributeClass(arg1) ? 6 :
                    (isCredentialClass(arg1) ? 5 :
                    (isCredentialAttributeClass(arg1) ? 4 :
                    (isRelationshipClass(arg1) ? 3 :
                    (isRelationshipIdentityClass(arg1) ? 2 :
                    (isRelationshipAttributeClass(arg1) ? 1 : 0))))))));

                return arg0weight > arg1weight ? 1 : (arg0weight < arg1weight ? -1 : 0);
            }
        });
    }

    /**
     * Check if the specified entity class holds master partition state - if the entity
     * class has a String property annotated with @PartitionClass, and a property
     * annotated with @Identifier, then it meets the required criteria.
     *
     * @param entityClass
     * @return
     */
    private boolean isPartitionClass(Class<?> entityClass) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(PartitionClass.class));
        query.addCriteria(new TypedPropertyCriteria(String.class));

        if (query.getFirstResult() == null) {
            return false;
        }

        query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(Identifier.class));
        return query.getFirstResult() != null;
    }

    private boolean isPartitionAttributeClass(Class<?> entityClass) {
        // If there is a MappedAttribute annotation and it specifies a Partition class in its
        // supportedClasses property, then return true
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            for (Class<?> cls : mappedAttribute.supportedClasses()) {
                if (Partition.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }

        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class));
        Property<?> attributeValueProperty = query.getFirstResult();

        // Otherwise, if there is no attribute value property(s), this is not an attribute class
        if (attributeValueProperty == null) {
            return false;
        }

        query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class));
        Property<?> ownerReferenceProperty = query.getFirstResult();

        // If there is no owner reference, this is not an attribute class
        if (ownerReferenceProperty == null) {
            return false;
        }

        // return true if the owner reference is a reference to a partition class
        if (isPartitionClass(ownerReferenceProperty.getJavaClass())) {
            return true;
        }

        return false;
    }

    private boolean isIdentityClass(Class<?> entityClass) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(IdentityClass.class));
        return (query.getFirstResult() != null);
    }

    private boolean isIdentityAttributeClass(Class<?> entityClass) {
        // If there is a MappedAttribute annotation and it specifies an identity class in its
        // supportedClasses property, then return true
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            for (Class<?> cls : mappedAttribute.supportedClasses()) {
                if (IdentityType.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }

        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class));
        Property<?> attributeValueProperty = query.getFirstResult();

        // If there is no attribute value property(s), this is not an attribute class
        if (attributeValueProperty == null) {
            return false;
        }

        query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class));
        Property<?> ownerReferenceProperty = query.getFirstResult();

        // If there is no owner reference, this is not an attribute class
        if (ownerReferenceProperty == null) {
            return false;
        }

        // return true if the owner reference is a reference to a partition class
        if (isIdentityClass(ownerReferenceProperty.getJavaClass())) {
            return true;
        }

        return false;
    }

    private boolean isCredentialClass(Class<?> entityClass) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(CredentialClass.class));
        return (query.getFirstResult() != null);
    }

    private boolean isCredentialAttributeClass(Class<?> entityClass) {
        // If there is a MappedAttribute annotation and it specifies a CredentialStorage class in its
        // supportedClasses property, then return true
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            for (Class<?> cls : mappedAttribute.supportedClasses()) {
                if (CredentialStorage.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }

        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class));
        Property<?> attributeValueProperty = query.getFirstResult();

        // If there is no attribute value property(s), this is not an attribute class
        if (attributeValueProperty == null) {
            return false;
        }

        query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class));
        Property<?> ownerReferenceProperty = query.getFirstResult();

        // If there is no owner reference, this is not an attribute class
        if (ownerReferenceProperty == null) {
            return false;
        }

        // return true if the owner reference is a reference to a partition class
        if (isCredentialClass(ownerReferenceProperty.getJavaClass())) {
            return true;
        }

        return false;
    }

    private boolean isRelationshipClass(Class<?> entityClass) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(RelationshipClass.class));
        return (query.getFirstResult() != null);
    }

    private boolean isRelationshipIdentityClass(Class<?> entityClass) {
        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(RelationshipDescriptor.class));
        return (query.getFirstResult() != null);
    }

    private boolean isRelationshipAttributeClass(Class<?> entityClass) {
        // If there is a MappedAttribute annotation and it specifies a Relationship class in its
        // supportedClasses property, then return true
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            for (Class<?> cls : mappedAttribute.supportedClasses()) {
                if (Relationship.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
        }

        PropertyQuery<Object> query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class));
        Property<?> attributeValueProperty = query.getFirstResult();

        // If there is no attribute value property(s), this is not an attribute class
        if (attributeValueProperty == null) {
            return false;
        }

        query = PropertyQueries.createQuery(entityClass);
        query.addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class));
        Property<?> ownerReferenceProperty = query.getFirstResult();

        // If there is no owner reference, this is not an attribute class
        if (ownerReferenceProperty == null) {
            return false;
        }

        // return true if the owner reference is a reference to a partition class
        if (isRelationshipClass(ownerReferenceProperty.getJavaClass())) {
            return true;
        }

        return false;
    }

    private void configurePartitionClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Partition>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {Partition.class};

        Property<String> idProperty = PropertyQueries.<String>createQuery(Partition.class)
                .addCriteria(new NamedPropertyCriteria("id"))
                .getSingleResult();

        Property<String> nameProperty = PropertyQueries.<String>createQuery(Partition.class)
                .addCriteria(new NamedPropertyCriteria("name"))
                .getSingleResult();

        // Query the partition class property on the entity
        Property<String> classProperty = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(PartitionClass.class))
                .getSingleResult();

        partitionModel.setPartitionClassProperty(classProperty);

        for (Class<? extends Partition> partitionClass : types) {
            ModelDefinition definition = partitionModel.getDefinition(partitionClass);

            // First query the identifier property on the entity
            Property<?> prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Identifier.class))
                    .getSingleResult();

            definition.addProperty(idProperty, new PropertyMapping(prop));

            // next query the name property on the entity
            prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(PartitionName.class))
                    .getSingleResult();

            definition.addProperty(nameProperty, new PropertyMapping(prop));

            // Finally query for any @AttributeValue properties on the entity, and map them to their
            // corresponding identity property
            List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                .getResultList();

            for (Property<Object> value : attributeValues) {
                Property<?> identityProperty = PropertyQueries.createQuery(partitionClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                        .addCriteria(new NamedPropertyCriteria(value.getName()))
                        .getFirstResult();

                if (identityProperty != null) {
                    definition.addProperty(identityProperty, new PropertyMapping(value));
                }
            }
        }
    }

    private void configurePartitionAttributeClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Partition>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {Partition.class};

        // First determine the @OwnerReference property, and store it for this entity
        Property<?> ownerReference = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getSingleResult();
        partitionModel.setOwnerReference(entityClass, ownerReference);

        // If the @MappedAttribute annotation is present, then either
        // A) the entity class contains ad-hoc attribute values, or
        // B) the entity class itself is mapped to a property of the partition, either as a
        // many-to-one or one-to-one relationship
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            Property<String> attributeClass = PropertyQueries.<String>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getFirstResult();

            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            // If there is a property annotated with @AttributeClass, then the entity contains
            // ad-hoc attribute values
            if (attributeClass != null) {
                // Create an AttributeMapping
                Property<String> attributeName = PropertyQueries.<String>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeName.class))
                        .getFirstResult();

                Property<Object> attributeValue = PropertyQueries.<Object>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                        .getFirstResult();

                AttributeMapping mapping = new AttributeMapping(attributeName, attributeClass, attributeValue);

                for (Class<? extends Partition> partitionClass : types) {
                    ModelDefinition definition = partitionModel.getDefinition(partitionClass);

                    if (mappedAttribute.supportedClasses().length == 0) {
                        definition.addAttribute(Object.class, mapping);
                    } else {
                        for (Class<?> cls : mappedAttribute.supportedClasses()) {
                            definition.addAttribute(cls, mapping);
                        }
                    }
                }
            } else {
                // Otherwise the the entity class must be mapped to a property of the partition -
                // iterate through the supported types and create a PropertyMapping for each of them
                for (Class<? extends Partition> partitionClass : types) {
                    ModelDefinition definition = partitionModel.getDefinition(partitionClass);

                    Property<Object> attributeProperty = PropertyQueries.<Object>createQuery(partitionClass)
                            .addCriteria(new NamedPropertyCriteria(mappedAttribute.name()))
                            .getFirstResult();

                    if (attributeProperty != null) {
                        definition.addProperty(attributeProperty, new PropertyMapping(entityClass));
                    }
                }
            }
        } else {
            // Otherwise the entity should have a one-to-one relationship with the
            // master partition entity
            for (Class<? extends Partition> partitionClass : types) {
                ModelDefinition definition = partitionModel.getDefinition(partitionClass);

                // Query for any @AttributeValue properties on the entity, and map them to their
                // corresponding partition property
                List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                    .getResultList();

                for (Property<Object> value : attributeValues) {
                    Property<?> partitionProperty = PropertyQueries.createQuery(partitionClass)
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                            .addCriteria(new NamedPropertyCriteria(value.getName()))
                            .getFirstResult();

                    if (partitionProperty != null) {
                        definition.addProperty(partitionProperty, new PropertyMapping(value));
                    }
                }
            }
        }
    }

    private void configureIdentityClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends IdentityType>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {IdentityType.class};

        Property<String> idProperty = PropertyQueries.<String>createQuery(IdentityType.class)
                .addCriteria(new NamedPropertyCriteria("id"))
                .getSingleResult();

        Property<?> enabledProperty = PropertyQueries.createQuery(IdentityType.class)
                .addCriteria(new NamedPropertyCriteria("enabled"))
                .getSingleResult();

        Property<Date> createdProperty = PropertyQueries.<Date>createQuery(IdentityType.class)
                .addCriteria(new NamedPropertyCriteria("createdDate"))
                .getSingleResult();

        Property<Date> expirationProperty = PropertyQueries.<Date>createQuery(IdentityType.class)
                .addCriteria(new NamedPropertyCriteria("expirationDate"))
                .getSingleResult();

        Property<Partition> partitionProperty = PropertyQueries.<Partition>createQuery(IdentityType.class)
                .addCriteria(new NamedPropertyCriteria("partition"))
                .getSingleResult();

        // Query the identity class property on the entity
        Property<String> classProperty = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(IdentityClass.class))
                .getSingleResult();

        identityModel.setIdentityClassProperty(classProperty);

        for (Class<? extends IdentityType> identityClass : types) {
            ModelDefinition definition = identityModel.getDefinition(identityClass);

            // First query the identifier property on the entity
            Property<String> idProp = PropertyQueries.<String>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Identifier.class))
                    .getSingleResult();

            definition.addProperty(idProperty, new PropertyMapping(idProp));

            // next query the enabled property on the entity
            Property<?> prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Enabled.class))
                    .getFirstResult();

            // We don't *really* need an enabled property - if it's absent, we assume everything is enabled
            if (prop != null) {
                definition.addProperty(enabledProperty, new PropertyMapping(prop));
            }

            Property<Date> dateProp = PropertyQueries.<Date>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(CreationDate.class))
                    .getFirstResult();

            // Likewise we don't really need the created date property
            if (dateProp != null) {
                definition.addProperty(createdProperty,  new PropertyMapping(prop));
            }

            dateProp = PropertyQueries.<Date>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ExpiryDate.class))
                    .getFirstResult();

            // Or the expiry date property
            if (dateProp != null) {
                definition.addProperty(expirationProperty,  new PropertyMapping(prop));
            }

            // The @OwnerReference annotation is used to link the identity to the owning partition
            prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                    .getFirstResult();

            if (prop != null) {
                definition.addProperty(partitionProperty, new PropertyMapping(prop));
            }

            // Finally query for any @AttributeValue properties on the entity, and map them to their
            // corresponding identity property
            List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                .getResultList();

            for (Property<Object> value : attributeValues) {
                Property<?> identityProperty = PropertyQueries.createQuery(identityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                        .addCriteria(new NamedPropertyCriteria(value.getName()))
                        .getFirstResult();

                if (identityProperty != null) {
                    definition.addProperty(identityProperty, new PropertyMapping(value));
                }
            }
        }
    }

    private void configureIdentityAttributeClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends IdentityType>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {IdentityType.class};

        // First determine the @OwnerReference property, and store it for this entity
        Property<?> ownerReference = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getSingleResult();
        identityModel.setOwnerReference(entityClass, ownerReference);

        // If the @MappedAttribute annotation is present, then either
        // A) the entity class contains ad-hoc attribute values, or
        // B) the entity class itself is mapped to a property of the partition, either as a
        // many-to-one or one-to-one relationship
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            Property<String> attributeClass = PropertyQueries.<String>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getFirstResult();

            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            // If there is a property annotated with @AttributeClass, then the entity contains
            // ad-hoc attribute values
            if (attributeClass != null) {
                // Create an AttributeMapping
                Property<String> attributeName = PropertyQueries.<String>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeName.class))
                        .getFirstResult();

                Property<Object> attributeValue = PropertyQueries.<Object>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                        .getFirstResult();

                AttributeMapping mapping = new AttributeMapping(attributeName, attributeClass, attributeValue);

                for (Class<? extends IdentityType> identityClass : types) {
                    ModelDefinition definition = identityModel.getDefinition(identityClass);

                    if (mappedAttribute.supportedClasses().length == 0) {
                        definition.addAttribute(Object.class, mapping);
                    } else {
                        for (Class<?> cls : mappedAttribute.supportedClasses()) {
                            definition.addAttribute(cls, mapping);
                        }
                    }
                }
            } else {
                // Otherwise the the entity class must be mapped to a property of the identity -
                // iterate through the supported types and create a PropertyMapping for each of them
                for (Class<? extends IdentityType> identityClass : types) {
                    ModelDefinition definition = identityModel.getDefinition(identityClass);

                    Property<Object> attributeProperty = PropertyQueries.<Object>createQuery(identityClass)
                            .addCriteria(new NamedPropertyCriteria(mappedAttribute.name()))
                            .getFirstResult();

                    if (attributeProperty != null) {
                        definition.addProperty(attributeProperty, new PropertyMapping(entityClass));
                    }
                }
            }
        } else {
            // Otherwise the entity should have a one-to-one relationship with the
            // master identity entity
            for (Class<? extends IdentityType> identityClass : types) {
                ModelDefinition definition = identityModel.getDefinition(identityClass);

                // Query for any @AttributeValue properties on the entity, and map them to their
                // corresponding partition property
                List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                    .getResultList();

                for (Property<Object> value : attributeValues) {
                    Property<?> identityProperty = PropertyQueries.createQuery(identityClass)
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                            .addCriteria(new NamedPropertyCriteria(value.getName()))
                            .getFirstResult();

                    if (identityProperty != null) {
                        definition.addProperty(identityProperty, new PropertyMapping(value));
                    }
                }
            }
        }
    }

    private void configureCredentialClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends CredentialStorage>[] types = (entityClass.isAnnotationPresent(ManagedCredential.class)) ?
            entityClass.getAnnotation(ManagedCredential.class).supportedClasses() :
            new Class[] {CredentialStorage.class};

        Property<Date> effectiveDateProperty = PropertyQueries.<Date>createQuery(CredentialStorage.class)
                .addCriteria(new NamedPropertyCriteria("effectiveDate"))
                .getSingleResult();

        Property<Date> expiryDateProperty = PropertyQueries.<Date>createQuery(CredentialStorage.class)
                .addCriteria(new NamedPropertyCriteria("expiryDate"))
                .getSingleResult();

        // Query the credential class property on the entity
        Property<String> classProperty = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(CredentialClass.class))
                .getSingleResult();

        credentialModel.setCredentialClassProperty(classProperty);

        // Query the credential value property on the entity
        Property<?> valueProperty = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(CredentialValue.class))
                .getSingleResult();

        credentialModel.setCredentialValueProperty(valueProperty);

        for (Class<? extends CredentialStorage> credentialClass : types) {
            ModelDefinition definition = credentialModel.getDefinition(credentialClass);

            // First query the effectiveDate property on the entity
            Property<Date> dateProp = PropertyQueries.<Date>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(EffectiveDate.class))
                    .getSingleResult();

            definition.addProperty(effectiveDateProperty, new PropertyMapping(dateProp));

            // next query the expiry date property on the entity
            dateProp = PropertyQueries.<Date>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(ExpiryDate.class))
                    .getSingleResult();

            definition.addProperty(expiryDateProperty, new PropertyMapping(dateProp));

            // The @OwnerReference annotation is used to link the credential to the owning identity
            Property<?> prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                    .getSingleResult();

            credentialModel.setOwnerReference(entityClass, prop);

            List<Property<Object>> storedValues = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(CredentialValue.class))
                    .getResultList();
            for (Property<Object> value : storedValues) {
                Property<?> credentialProperty = PropertyQueries.createQuery(credentialClass)
                        .addCriteria(new AnnotatedPropertyCriteria(Stored.class))
                        .getFirstResult();

                if (credentialProperty != null) {
                    definition.addProperty(credentialProperty, new PropertyMapping(value));
                } else {
                    // If we can't find the property on the superclass, we still need to store it for when we
                    // encounter a subclass with that property

                    // TODO
                }
            }


            // Finally query for any @AttributeValue properties on the entity, and map them to their
            // corresponding credential property
            List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                .getResultList();

            for (Property<Object> value : attributeValues) {
                Property<?> credentialProperty = PropertyQueries.createQuery(credentialClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                        .addCriteria(new NamedPropertyCriteria(value.getName()))
                        .getFirstResult();

                if (credentialProperty != null) {
                    definition.addProperty(credentialProperty, new PropertyMapping(value));
                }
            }
        }
    }

    private void configureCredentialAttributeClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends CredentialStorage>[] types = (entityClass.isAnnotationPresent(ManagedCredential.class)) ?
            entityClass.getAnnotation(ManagedCredential.class).supportedClasses() :
            new Class[] {CredentialStorage.class};

        // First determine the @OwnerReference property, and store it for this entity
        Property<?> ownerReference = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getSingleResult();
        credentialModel.setOwnerReference(entityClass, ownerReference);

        // If the @MappedAttribute annotation is present, then either
        // A) the entity class contains ad-hoc attribute values, or
        // B) the entity class itself is mapped to a property of the credential, either as a
        // many-to-one or one-to-one relationship
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            Property<String> attributeClass = PropertyQueries.<String>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getFirstResult();

            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            // If there is a property annotated with @AttributeClass, then the entity contains
            // ad-hoc attribute values
            if (attributeClass != null) {
                // Create an AttributeMapping
                Property<String> attributeName = PropertyQueries.<String>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeName.class))
                        .getFirstResult();

                Property<Object> attributeValue = PropertyQueries.<Object>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                        .getFirstResult();

                AttributeMapping mapping = new AttributeMapping(attributeName, attributeClass, attributeValue);

                for (Class<? extends CredentialStorage> credentialClass : types) {
                    ModelDefinition definition = credentialModel.getDefinition(credentialClass);

                    if (mappedAttribute.supportedClasses().length == 0) {
                        definition.addAttribute(Object.class, mapping);
                    } else {
                        for (Class<?> cls : mappedAttribute.supportedClasses()) {
                            definition.addAttribute(cls, mapping);
                        }
                    }
                }
            } else {
                // Otherwise the the entity class must be mapped to a property of the credential -
                // iterate through the supported types and create a PropertyMapping for each of them
                for (Class<? extends CredentialStorage> credentialClass : types) {
                    ModelDefinition definition = credentialModel.getDefinition(credentialClass);

                    Property<Object> attributeProperty = PropertyQueries.<Object>createQuery(credentialClass)
                            .addCriteria(new NamedPropertyCriteria(mappedAttribute.name()))
                            .getFirstResult();

                    if (attributeProperty != null) {
                        definition.addProperty(attributeProperty, new PropertyMapping(entityClass));
                    }
                }
            }
        } else {
            // Otherwise the entity should have a one-to-one relationship with the
            // master credential entity
            for (Class<? extends CredentialStorage> credentialClass : types) {
                ModelDefinition definition = credentialModel.getDefinition(credentialClass);

                // Query for any @AttributeValue properties on the entity, and map them to their
                // corresponding partition property
                List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                    .getResultList();

                for (Property<Object> value : attributeValues) {
                    Property<?> credentialProperty = PropertyQueries.createQuery(credentialClass)
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                            .addCriteria(new NamedPropertyCriteria(value.getName()))
                            .getFirstResult();

                    if (credentialProperty != null) {
                        definition.addProperty(credentialProperty, new PropertyMapping(value));
                    }
                }
            }
        }
    }

    private void configureRelationshipClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Relationship>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {Relationship.class};

        Property<String> idProperty = PropertyQueries.<String>createQuery(Relationship.class)
                .addCriteria(new NamedPropertyCriteria("id"))
                .getSingleResult();

        // Query the relationship class property on the entity
        Property<String> classProperty = PropertyQueries.<String>createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipClass.class))
                .getSingleResult();

        relationshipModel.setRelationshipClassProperty(classProperty);

        for (Class<? extends Relationship> relationshipClass : types) {
            ModelDefinition definition = relationshipModel.getDefinition(relationshipClass);

            // First query the identifier property on the entity
            Property<?> prop = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Identifier.class))
                    .getSingleResult();

            definition.addProperty(idProperty, new PropertyMapping(prop));

            // Finally query for any @AttributeValue properties on the entity, and map them to their
            // corresponding relationship property
            List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                .getResultList();

            for (Property<Object> value : attributeValues) {
                Property<?> relationshipProperty = PropertyQueries.createQuery(relationshipClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                        .addCriteria(new NamedPropertyCriteria(value.getName()))
                        .getFirstResult();

                if (relationshipProperty != null) {
                    definition.addProperty(relationshipProperty, new PropertyMapping(value));
                }
            }
        }
    }

    private void configureRelationshipIdentityClass(Class<?> entityClass) {
        // First determine the @OwnerReference property, and store it for this entity
        Property<?> ownerReference = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getSingleResult();
        relationshipModel.setOwnerReference(entityClass, ownerReference);

        // then store the relationship member property
        Property<?> relationshipMember = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipMember.class))
                .getSingleResult();
        relationshipModel.setRelationshipMember(relationshipMember);

        // finally store the relationship descriptor property
        Property<?> relationshipDescriptor = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipDescriptor.class))
                .getSingleResult();
        relationshipModel.setRelationshipDescriptor(relationshipDescriptor);
    }

    private void configureRelationshipAttributeClass(Class<?> entityClass) {
        @SuppressWarnings("unchecked")
        Class<? extends Relationship>[] types = (entityClass.isAnnotationPresent(IdentityManaged.class)) ?
            entityClass.getAnnotation(IdentityManaged.class).value() :
            new Class[] {Relationship.class};

        // First determine the @OwnerReference property, and store it for this entity
        Property<?> ownerReference = PropertyQueries.createQuery(entityClass)
                .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                .getSingleResult();
        relationshipModel.setOwnerReference(entityClass, ownerReference);

        // If the @MappedAttribute annotation is present, then either
        // A) the entity class contains ad-hoc attribute values, or
        // B) the entity class itself is mapped to a property of the relationship, either as a
        // many-to-one or one-to-one relationship
        if (entityClass.isAnnotationPresent(MappedAttribute.class)) {
            Property<String> attributeClass = PropertyQueries.<String>createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getFirstResult();

            MappedAttribute mappedAttribute = entityClass.getAnnotation(MappedAttribute.class);

            // If there is a property annotated with @AttributeClass, then the entity contains
            // ad-hoc attribute values
            if (attributeClass != null) {
                // Create an AttributeMapping
                Property<String> attributeName = PropertyQueries.<String>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeName.class))
                        .getFirstResult();

                Property<Object> attributeValue = PropertyQueries.<Object>createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                        .getFirstResult();

                AttributeMapping mapping = new AttributeMapping(attributeName, attributeClass, attributeValue);

                for (Class<? extends Relationship> relationshipClass : types) {
                    ModelDefinition definition = relationshipModel.getDefinition(relationshipClass);

                    if (mappedAttribute.supportedClasses().length == 0) {
                        definition.addAttribute(Object.class, mapping);
                    } else {
                        for (Class<?> cls : mappedAttribute.supportedClasses()) {
                            definition.addAttribute(cls, mapping);
                        }
                    }
                }
            } else {
                // Otherwise the the entity class must be mapped to a property of the relationship -
                // iterate through the supported types and create a PropertyMapping for each of them
                for (Class<? extends Relationship> relationshipClass : types) {
                    ModelDefinition definition = relationshipModel.getDefinition(relationshipClass);

                    Property<Object> attributeProperty = PropertyQueries.<Object>createQuery(relationshipClass)
                            .addCriteria(new NamedPropertyCriteria(mappedAttribute.name()))
                            .getFirstResult();

                    if (attributeProperty != null) {
                        definition.addProperty(attributeProperty, new PropertyMapping(entityClass));
                    }
                }
            }
        } else {
            // Otherwise the entity should have a one-to-one relationship with the
            // master relationship entity
            for (Class<? extends Relationship> relationshipClass : types) {
                ModelDefinition definition = relationshipModel.getDefinition(relationshipClass);

                // Query for any @AttributeValue properties on the entity, and map them to their
                // corresponding partition property
                List<Property<Object>> attributeValues = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                    .getResultList();

                for (Property<Object> value : attributeValues) {
                    Property<?> relationshipProperty = PropertyQueries.createQuery(relationshipClass)
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                            .addCriteria(new NamedPropertyCriteria(value.getName()))
                            .getFirstResult();

                    if (relationshipProperty != null) {
                        definition.addProperty(relationshipProperty, new PropertyMapping(value));
                    }
                }
            }
        }
    }
}
