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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.ConfigurationName;
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;
import org.picketlink.idm.jpa.internal.AttributeList;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import static java.util.Map.Entry;

/**
 * <p>This class holds all the mapping configuration for a specific JPA Entity and their corresponding IDM model classes.
 * A specific JPA entity can be mapped to and from different IDM model classes.
 * </p>
 * <p>Each {@link EntityMapping} holds the specific mapping for a IDM model type.</p>
 *
 * @author pedroigor
 */
public class EntityMapper {

    private final List<EntityMapping> entityMappings;
    private final Class<?> entityType;
    private final JPAIdentityStore store;

    public EntityMapper(Class<?> entityType, JPAIdentityStore jpaIdentityStore) {
        this.entityType = entityType;
        this.store = jpaIdentityStore;

        List<EntityMapping> mappings = new ArrayList<EntityMapping>();

        for (ModelMapper modelMapper : getModelMappers()) {
            mappings.addAll(modelMapper.createMapping(entityType));
        }

        this.entityMappings = Collections.unmodifiableList(mappings);
    }

    public boolean supports(Class<?> attributedType) {
        return getMappingsFor(attributedType) != null;
    }

    public Object createEntity(AttributedType attributedType, EntityManager entityManager) {
        Object entityInstance = null;

        try {
            MappedAttribute mappedAttribute = getEntityType().getAnnotation(MappedAttribute.class);

            if (mappedAttribute != null && mappedAttribute.value().length() > 0) {
                Property<Object> property = PropertyQueries
                        .createQuery(attributedType.getClass())
                        .addCriteria(new NamedPropertyCriteria(mappedAttribute.value()))
                        .getFirstResult();

                if (property != null) {
                    entityInstance = property.getValue(attributedType);
                }
            } else {
                entityInstance = this.entityType.newInstance();
            }

            if (entityInstance != null) {
                EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

                for (Property property : entityMapping.getProperties().keySet()) {
                    Property mappedProperty = entityMapping.getProperties().get(property);
                    Object value = property.getValue(attributedType);

                    if (mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                        AttributedType ownerType = (AttributedType) value;

                        if (ownerType == null || ownerType.getId() == null) {
                            throw new IdentityManagementException("Owner does not exists or was not provided.");
                        }

                        mappedProperty.setValue(entityInstance, entityManager.find(mappedProperty.getJavaClass(), ownerType.getId()));
                    } else {
                        // if the property maps to a mapped type is because we have a many-to-one relationship
                        // this is the case when a type has a hierarchy
                        if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                            AttributedType ownerType = (AttributedType) value;

                            if (ownerType != null) {
                                mappedProperty.setValue(entityInstance, entityManager.find(mappedProperty.getJavaClass(), ownerType.getId()));
                            }
                        } else {
                            mappedProperty.setValue(entityInstance, value);
                        }
                    }
                }

                entityManager.persist(entityInstance);

                for (Attribute attribute : attributedType.getAttributes()) {
                    this.store.setAttribute(attributedType, attribute, entityManager);
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Could not create entity.", e);
        }

        return entityInstance;
    }

    public Object updateEntity(AttributedType attributedType, EntityManager entityManager) {
        Object entityInstance = null;

        if (getEntityType().isAnnotationPresent(MappedAttribute.class)) {
            Property<Object> property = PropertyQueries
                    .createQuery(attributedType.getClass())
                    .addCriteria(new NamedPropertyCriteria(getEntityType().getAnnotation(MappedAttribute.class).value()))
                    .getFirstResult();

            if (property != null) {
                entityInstance = property.getValue(attributedType);

                if (entityInstance == null) {
                    List childs = this.store.getAssociatedEntities(attributedType, this.store.getAttributedTypeEntity(attributedType, entityManager), entityManager, this);

                    for (Object child: childs) {
                        entityManager.remove(child);
                    }
                }
            }
        } else {
            entityInstance = entityManager.find(getEntityType(), attributedType.getId());
        }

        if (entityInstance != null) {
            if (List.class.isInstance(entityInstance)) {
                if (AttributeList.class.isInstance(entityInstance)) {
                    AttributeList instances = (AttributeList) entityInstance;
                    Iterator iterator = instances.getOriginalList().iterator();

                    while (iterator.hasNext()) {
                        Object instance = iterator.next();

                        if (!instances.contains(instance)) {
                            entityManager.remove(instance);
                        }
                    }
                }

                List instances = (List) entityInstance;
                Iterator iterator = instances.iterator();

                while (iterator.hasNext()) {
                    updateEntity(attributedType, iterator.next(), entityManager);
                }

            } else {
                updateEntity(attributedType, entityInstance, entityManager);
            }

            if (isRoot()) {
                this.store.removeAllAttributes(attributedType, entityManager);

                for (Attribute attribute : attributedType.getAttributes()) {
                    this.store.setAttribute(attributedType, attribute, entityManager);
                }
            }
        }

        return entityInstance;
    }

    private void updateEntity(AttributedType attributedType, Object entityInstance, EntityManager entityManager) {
        EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

        for (Property property : entityMapping.getProperties().keySet()) {
            Property mappedProperty = entityMapping.getProperties().get(property);

            if (mappedProperty.getAnnotatedElement().isAnnotationPresent(ConfigurationName.class)) {
                continue;
            }

            Object value = property.getValue(attributedType);

            if (value != null) {
                if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                    for (EntityMapper entityMapper : getEntityMappers()) {
                        if (mappedProperty.getJavaClass().equals(entityMapper.getEntityType())) {
                            AttributedType attributedType1 = (AttributedType) value;

                            mappedProperty.setValue(entityInstance, entityManager.find(mappedProperty.getJavaClass(), attributedType1.getId()));
                        }
                    }
                } else {
                    mappedProperty.setValue(entityInstance, value);
                }
            } else {
                mappedProperty.setValue(entityInstance, null);
            }
        }

        entityManager.persist(entityInstance);
    }

    public <V extends AttributedType> void populate(V attributedType, Object entityInstance, EntityManager entityManager) {
        if (getEntityType().isAnnotationPresent(MappedAttribute.class)) {
            MappedAttribute mappedAttribute = getEntityType().getAnnotation(MappedAttribute.class);
            Property<Object> property = PropertyQueries
                    .createQuery(attributedType.getClass())
                    .addCriteria(new NamedPropertyCriteria(mappedAttribute.value()))
                    .getFirstResult();

            if (property != null) {
                if (List.class.isAssignableFrom(property.getJavaClass())) {
                    List instances = (List) property.getValue(attributedType);

                    if (instances == null) {
                        instances = new AttributeList();
                    }

                    instances.add(entityInstance);

                    entityInstance = instances;
                }

                property.setValue(attributedType, entityInstance);
            }
        } else {
            for (EntityMapping entityMapping : getEntityMappings()) {
                for (Property property : entityMapping.getProperties().keySet()) {
                    Property mappedProperty = entityMapping.getProperties().get(property);

                    if (!mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                        Object value = mappedProperty.getValue(entityInstance);

                        if (value != null) {
                            if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                                for (EntityMapper entityMapper : getEntityMappers()) {
                                    if (mappedProperty.getJavaClass().equals(entityMapper.getEntityType())) {
                                        property.setValue(attributedType, entityMapper.createType(value, entityManager));
                                    }
                                }
                            } else {
                                property.setValue(attributedType, value);
                            }
                        } else {
                            property.setValue(attributedType, null);
                        }
                    }
                }
            }
        }
    }

    public <P extends AttributedType> P createType(Object entityInstance, EntityManager entityManager) {
        P attributedType = null;

        if (entityInstance != null) {
            try {
                attributedType =
                        (P) Class.forName(getTypeProperty().getValue(entityInstance).toString()).newInstance();

                EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

                for (Property property : entityMapping.getProperties().keySet()) {
                    Property mappedProperty = entityMapping.getProperties().get(property);

                    if (mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                        Object ownerType = mappedProperty.getValue(entityInstance);

                        if (ownerType == null) {
                            throw new IdentityManagementException("Owner does not exists or was not provided.");
                        }

                        AttributedType ownerAttributedType = null;

                        for (EntityMapper entityMapper : getEntityMappers()) {
                            if (mappedProperty.getJavaClass().equals(entityMapper.getEntityType())) {
                                ownerAttributedType = entityMapper.createType(ownerType, entityManager);
                            }
                        }

                        property.setValue(attributedType, ownerAttributedType);
                    } else {
                        // if the property maps to a mapped type is because we have a many-to-one relationship
                        // this is the case when a type has a hierarchy
                        if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                            property.setValue(attributedType, createType(mappedProperty.getValue(entityInstance), entityManager));
                        } else {
                            property.setValue(attributedType, mappedProperty.getValue(entityInstance));
                        }
                    }
                }

                this.store.populateAttributedType(attributedType, entityInstance, entityManager);
                this.store.populateAllAttributes(attributedType, entityManager);

            } catch (Exception e) {
                throw new IdentityManagementException("Could not create [" + attributedType + " from entity [" + entityInstance + "].", e);
            }
        }

        return attributedType;
    }

    public Entry<Property, Property> getProperty(Class<? extends AttributedType> attributedType, String propertyName) {
        EntityMapping entityMapping = getMappingsFor(attributedType);

        for (Entry<Property, Property> property : entityMapping.getProperties().entrySet()) {
            if (property.getKey().getName().equals(propertyName)) {
                return property;
            }
        }

        return null;
    }

    public List<EntityMapping> getEntityMappings() {
        return this.entityMappings;
    }

    public Class<?> getEntityType() {
        return this.entityType;
    }

    public Entry<Property, Property> getProperty(Class<?> attributedType,
                                                 Class<? extends Annotation> annotation) {
        EntityMapping entityMapping = getMappingsFor(attributedType);

        for (Entry<Property, Property> property : entityMapping.getProperties().entrySet()) {
            if (property.getValue().getAnnotatedElement().isAnnotationPresent(annotation)) {
                return property;
            }
        }

        return null;
    }

    public EntityMapping getMappingsFor(Class<?> attributedType) {
        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.getSupportedType().equals(attributedType)) {
                return entityMapping;
            }
        }

        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.getSupportedType().isAssignableFrom(attributedType)) {
                return entityMapping;
            }
        }

//        for (EntityMapping entityMapping : new ArrayList<EntityMapping>(getEntityMappings())) {
//            if (entityMapping.getSupportedType().isAssignableFrom(attributedType)) {
//                entityMappings.add(entityMapping);
//
//                for (ModelMapper mapper : getModelMappers()) {
//                    if (mapper.supports(getEntityType())) {
//                        EntityMapping mapping = mapper.createMapping(attributedType, getEntityType());
//
//                        if (!mapping.getProperties().isEmpty()) {
//                            this.entityMappings.add(mapping);
//                            entityMappings.add(mapping);
//                        }
//                    }
//                }
//            }
//        }

        return null;
    }

    public boolean isRoot() {
        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.isRootMapping()) {
                return true;
            }
        }

        return false;
    }

    public Property getIdProperty() {
        return PropertyQueries
                .createQuery(getEntityType())
                .addCriteria(new AnnotatedPropertyCriteria(Id.class))
                .getFirstResult();
    }

    private <V extends IdentityType> Property getTypeProperty() {
        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.getTypeProperty() != null) {
                return entityMapping.getTypeProperty();
            }
        }

        return null;
    }

    private List<ModelMapper> getModelMappers() {
        ArrayList<ModelMapper> modelMappers = new ArrayList<ModelMapper>();

        modelMappers.add(new RelationshipMapper());
        modelMappers.add(new PartitionMapper());
        modelMappers.add(new IdentityTypeMapper());
        modelMappers.add(new AttributedValueMapper());
        modelMappers.add(new NamedMappedAttribute());
        modelMappers.add(new AttributeTypeMapper());
        modelMappers.add(new ManagedCredentialAttributeMapper());

        return modelMappers;
    }

    private Object updateMapperAttribute(AttributedType attributedType, Object entityInstance, EntityManager entityManager) {
        Property<Object> property = PropertyQueries
                .createQuery(attributedType.getClass())
                .addCriteria(new NamedPropertyCriteria(getEntityType().getAnnotation(MappedAttribute.class).value()))
                .getFirstResult();

        if (property != null) {
            entityInstance = property.getValue(attributedType);

            if (entityInstance != null) {
                for (EntityMapping entityMapping : getEntityMappings()) {
                    for (Property ownerReference : entityMapping.getProperties().values()) {
                        if (ownerReference.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                            ownerReference.setValue(entityInstance, entityManager.find(ownerReference.getJavaClass(), attributedType.getId()));
                        }
                    }
                }
            }
        }

        return entityInstance;
    }

    private List<EntityMapper> getEntityMappers() {
        return this.store.getEntityMappers();
    }

}
