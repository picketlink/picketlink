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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
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
    private Map<Class<? extends Partition>, ModelDefinition> partitionModel =
            new HashMap<Class<? extends Partition>, ModelDefinition>();

    /**
     * The identity model definition, which maps between specific identity types
     * and their identity mapping metadata
     */
    private Map<Class<? extends IdentityType>, ModelDefinition> identityModel =
            new ConcurrentHashMap<Class<? extends IdentityType>, ModelDefinition>();

    /**
     * Credential model definition
     */
    private Map<Class<? extends CredentialStorage>, ModelDefinition> credentialModel;

    /**
     *
     */
    private RelationshipModel relationshipModel;

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

    private class RelationshipModel {
        private Class<?> relationshipClass;
        private Class<?> relationshipIdentityClass;
        private Map<Class<?>, AttributeMapping> attributes = new HashMap<Class<?>, AttributeMapping>();
    }

    private class PropertyMapping {
        private Property<?> entityProperty;

        public PropertyMapping(Property<?> entityProperty) {
            this.entityProperty = entityProperty;
        }
    }

    private class AttributeMapping {
        private PropertyMapping ownerReference;
        private Property<String> attributeName;
        private Property<String> attributeClass;
        private Property<Object> attributeValue;
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

        for (Class<? extends Partition> type : types) {
            ModelDefinition definition = new ModelDefinition();

            // First query the identifier property on the entity
            Property<?> identifier = PropertyQueries.createQuery(entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Identifier.class))
                    .getSingleResult();

            definition.addProperty(idProperty, new PropertyMapping(identifier));
            partitionModel.put(type, definition);

            // Next query for any @AttributeProperty properties, and map them to their
            // corresponding entity property
            List<Property<Object>> partitionProperties = PropertyQueries.createQuery(type)
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class))
                .getResultList();

            for (Property<Object> property : partitionProperties) {
                Property<?> entityProperty = PropertyQueries.createQuery(entityClass)
                        .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                        .addCriteria(new NamedPropertyCriteria(property.getName()))
                        .getFirstResult();

                if (entityProperty != null) {
                    definition.addProperty(property, new PropertyMapping(entityProperty));
                }
            }
        }
    }

    private void configurePartitionAttributeClass(Class<?> entityClass) {

    }

    private void configureIdentityClass(Class<?> entityClass) {

    }

    private void configureIdentityAttributeClass(Class<?> entityClass) {

    }

    private void configureCredentialClass(Class<?> entityClass) {

    }

    private void configureCredentialAttributeClass(Class<?> entityClass) {

    }

    private void configureRelationshipClass(Class<?> entityClass) {

    }

    private void configureRelationshipIdentityClass(Class<?> entityClass) {

    }

    private void configureRelationshipAttributeClass(Class<?> entityClass) {

    }
}
