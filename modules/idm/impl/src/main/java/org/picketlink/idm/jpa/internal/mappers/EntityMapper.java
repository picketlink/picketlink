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
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.annotations.entity.MappedAttribute;
import org.picketlink.idm.jpa.internal.AttributeList;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.Map.Entry;
import static org.picketlink.common.reflection.Reflections.newInstance;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p> This class holds all the mapping configuration for a specific JPA Entity and their corresponding IDM model
 * classes. A specific JPA entity can be mapped to and from different IDM model classes. </p> <p>Each {@link
 * EntityMapping} holds the specific mapping for a IDM model type.</p>
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

        if (mappings.isEmpty()) {
            throw new IdentityManagementException("Entity [" + entityType + "] does not have any mapping.");
        }

        this.entityMappings = Collections.unmodifiableList(mappings);
    }

    public void persist(AttributedType attributedType, EntityManager entityManager) {
        Object entity = getEntityInstance(attributedType, entityManager);

        if (entity != null) {
            EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

            for (Property property : entityMapping.getProperties().keySet()) {
                Object propertyValue = property.getValue(attributedType);
                Property mappedProperty = entityMapping.getProperties().get(property);
                Object mappedValue = propertyValue;

                if (mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                    AttributedType ownerType = (AttributedType) propertyValue;

                    if (ownerType == null || ownerType.getId() == null) {
                        if (isPartitionSupported(ownerType.getClass())) {
                            throw new IdentityManagementException("Owner does not exist or was not provided.");
                        }
                    }

                    mappedValue = this.store.getOwnerEntity(ownerType, mappedProperty, entityManager);
                } else {
                    // if the attributeProperty maps to a mapped type is because we have a many-to-one relationship
                    // this is the case when a type has a hierarchy
                    if (AttributedType.class.isInstance(propertyValue)) {
                        AttributedType ownerType = (AttributedType) propertyValue;

                        if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                            if (ownerType != null) {
                                mappedValue = entityManager.find(mappedProperty.getJavaClass(), ownerType.getId());
                            }
                        }
                    }
                }

                mappedProperty.setValue(entity, mappedValue);
            }

            entityManager.persist(entity);
        }
    }

    public Object updateEntity(AttributedType attributedType, EntityManager entityManager) {
        Object entityInstance = getEntityInstance(attributedType, entityManager);

        if (entityInstance != null) {
            if (List.class.isInstance(entityInstance)) {
                List attributes = (List) entityInstance;

                if (AttributeList.class.isInstance(entityInstance)) {
                    Iterator originalIterator = ((AttributeList) attributes).getOriginalList().iterator();

                    while (originalIterator.hasNext()) {
                        Object attribute = originalIterator.next();

                        if (!attributes.contains(attribute)) {
                            entityManager.remove(attribute);
                        }
                    }
                }

                Iterator iterator = attributes.iterator();

                while (iterator.hasNext()) {
                    updateEntity(attributedType, iterator.next(), entityManager);
                }
            } else {
                updateEntity(attributedType, entityInstance, entityManager);
            }
        }

        return entityInstance;
    }

    public <P extends AttributedType> P createType(Object entityInstance, EntityManager entityManager) {
        P attributedType = null;

        if (entityInstance != null) {
            if (!getEntityType().equals(entityInstance.getClass()) && !getEntityType().isAssignableFrom(entityInstance
                    .getClass())) {
                EntityMapper entityMapper = this.store.getMapperForEntity(entityInstance.getClass());
                Entry<Property, Property> property = entityMapper.getProperty(OwnerReference.class);

                if (property == null) {
                    throw new IdentityManagementException("Entity instance is not a " + getEntityType() + " or does " +
                            "not have a owner reference to this type.");
                }

                entityInstance = property.getValue().getValue(entityInstance);
            }

            try {
                attributedType = (P) newInstance(entityInstance.getClass(), getTypeProperty().getValue(entityInstance).toString());

                EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

                for (Property property : entityMapping.getProperties().keySet()) {
                    Property mappedProperty = entityMapping.getProperties().get(property);
                    Object mappedValue = mappedProperty.getValue(entityInstance);
                    Object propertyValue = mappedValue;

                    if (mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                        if (mappedValue == null) {
                            if (isPartitionSupported(property.getJavaClass())) {
                                throw new IdentityManagementException("Owner does not exists or was not provided.");
                            }
                        } else {
                            EntityMapper entityMapper = this.store.getMapperForEntity(mappedValue.getClass());

                            propertyValue = entityMapper.createType(mappedValue, entityManager);
                        }
                    } else {
                        // if the property maps to a mapped type is because we have a many-to-one relationship
                        // this is the case when a type has a hierarchy
                        if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                            propertyValue = createType(mappedValue, entityManager);
                        }
                    }

                    property.setValue(attributedType, propertyValue);
                }

                if (isRoot()) {
                    for (EntityMapper finalMapper : this.store.getMapperFor(attributedType.getClass())) {
                        if (!finalMapper.isRoot()) {
                            for (Object child : getAssociatedEntities(attributedType, finalMapper, entityManager)) {
                                finalMapper.populate(attributedType, child, entityManager);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new IdentityManagementException("Could not create [" + attributedType + " from entity [" + entityInstance + "].", e);
            }
        }

        return attributedType;
    }

    public Entry<Property, Property> getProperty(Class<?> attributedType, String propertyName) {
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

        if (entityMapping != null) {
            for (Entry<Property, Property> property : entityMapping.getProperties().entrySet()) {
                if (property.getValue().getAnnotatedElement().isAnnotationPresent(annotation)) {
                    return property;
                }
            }
        }

        return null;
    }

    public Entry<Property, Property> getProperty(Class<? extends Annotation> annotation) {
        for (EntityMapping entityMapping : getEntityMappings()) {
            for (Entry<Property, Property> property : entityMapping.getProperties().entrySet()) {
                if (property.getValue().getAnnotatedElement().isAnnotationPresent(annotation)) {
                    return property;
                }
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

        for (EntityMapping entityMapping : getEntityMappings()) {
            if (attributedType.isAssignableFrom(entityMapping.getSupportedType())) {
                return entityMapping;
            }
        }

        return null;
    }

    public boolean isRoot() {
        return getTypeProperty() != null;
    }

    public boolean isPersist() {
        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.getTypeProperty() != null) {
                return entityMapping.isPersist();
            }
        }

        return true;
    }

    public List getAssociatedEntities(AttributedType attributedType, EntityMapper entityMapper, EntityManager entityManager) {
        if (!entityMapper.getEntityType().isAnnotationPresent(IdentityManaged.class)) {
            return Collections.emptyList();
        }

        StringBuilder hql = new StringBuilder();

        hql.append("from ").append(entityMapper.getEntityType().getName()).append(" o where ");

        Entry<Property, Property> ownerProperty = entityMapper.getProperty(attributedType.getClass(), OwnerReference.class);

        if (ownerProperty == null) {
            return Collections.emptyList();
        }

        hql.append(" o.").append(ownerProperty.getValue().getName()).append(" = :owner");

        Query childQuery = entityManager.createQuery(hql.toString());

        Object ownerEntity = this.store.getOwnerEntity(attributedType, ownerProperty.getValue(), entityManager);

        childQuery.setParameter("owner", ownerEntity);

        return childQuery.getResultList();
    }

    public Object createEntity() {
        try {
            return newInstance(getEntityType(), getEntityType().getName());
        } catch (Exception e) {
            throw MESSAGES.instantiationError(getEntityType(), e);
        }
    }

    private <V extends AttributedType> void populate(V attributedType, Object entityInstance, EntityManager entityManager) {
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
                        Object mappedPropertyValue = mappedProperty.getValue(entityInstance);

                        if (mappedPropertyValue != null) {
                            if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                                EntityMapper entityMapper =
                                        this.store.getMapperForEntity(mappedProperty.getJavaClass());
                                Entry<Property, Property> ownerProperty = entityMapper.getProperty(OwnerReference.class);

                                if (ownerProperty != null) {
                                    entityMapper = this.store.getMapperForEntity(ownerProperty.getValue()
                                            .getJavaClass());
                                }

                                mappedPropertyValue = entityMapper.createType(mappedPropertyValue, entityManager);
                            }
                        }

                        property.setValue(attributedType, mappedPropertyValue);
                    }
                }
            }
        }
    }

    private void updateEntity(AttributedType attributedType, Object entityInstance, EntityManager entityManager) {
        EntityMapping entityMapping = getMappingsFor(attributedType.getClass());

        for (Property property : entityMapping.getProperties().keySet()) {
            Property mappedProperty = entityMapping.getProperties().get(property);
            Object value = property.getValue(attributedType);

            if (value != null) {
                if (this.store.isMappedType(mappedProperty.getJavaClass())) {
                    EntityMapper entityMapper = this.store.getMapperForEntity(mappedProperty.getJavaClass());

                    if (mappedProperty.getJavaClass().equals(entityMapper.getEntityType())) {
                        AttributedType referencedType = (AttributedType) value;
                        value = this.store.getOwnerEntity(referencedType, mappedProperty, entityManager);
                    }
                }
            }

            mappedProperty.setValue(entityInstance, value);
        }

        entityManager.persist(entityInstance);
    }

    private Property getTypeProperty() {
        for (EntityMapping entityMapping : getEntityMappings()) {
            if (entityMapping.getTypeProperty() != null) {
                return entityMapping.getTypeProperty();
            }
        }

        return null;
    }

    private List<ModelMapper> getModelMappers() {
        ArrayList<ModelMapper> modelMappers = new ArrayList<ModelMapper>();

        modelMappers.add(new PartitionMapper());
        modelMappers.add(new IdentityTypeMapper());
        modelMappers.add(new RelationshipMapper());
        modelMappers.add(new RelationshipIdentityMapper());
        modelMappers.add(new AttributedValueMapper());
        modelMappers.add(new NamedMappedAttribute());
        modelMappers.add(new AttributeTypeMapper());
        modelMappers.add(new ManagedCredentialAttributeMapper());

        return modelMappers;
    }

    private Object getEntityInstance(AttributedType attributedType, EntityManager entityManager) {
        Object entityInstance = null;

        if (getEntityType().isAnnotationPresent(MappedAttribute.class)) {
            Property<Object> property = PropertyQueries
                    .createQuery(attributedType.getClass())
                    .addCriteria(new NamedPropertyCriteria(getEntityType().getAnnotation(MappedAttribute.class).value()))
                    .getFirstResult();

            if (property != null) {
                entityInstance = property.getValue(attributedType);

                if (entityInstance == null) {
                    for (Object child : getAssociatedEntities(attributedType, this, entityManager)) {
                        entityManager.remove(child);
                    }
                }
            }
        } else {
            if (isRoot()) {
                entityInstance = entityManager.find(getEntityType(), attributedType.getId());
            } else {
                Property attributeProperty = PropertyQueries
                        .createQuery(attributedType.getClass())
                        .addCriteria(new TypedPropertyCriteria(getEntityType(), TypedPropertyCriteria.MatchOption.ALL))
                        .getFirstResult();

                // first we check if this mapper refers to an entity mapped directly into the current type. If so,
                // we just set the owner property.
                if (attributeProperty != null) {
                    entityInstance = attributeProperty.getValue(attributedType);
                } else {
                    List associatedEntities = getAssociatedEntities(attributedType, this, entityManager);

                    if (!associatedEntities.isEmpty()) {
                        if (associatedEntities.size() > 1) {
                            throw new IdentityManagementException("Unexpected associated references count.");
                        }

                        entityInstance = associatedEntities.get(0);
                    }
                }
            }

            if (entityInstance == null) {
                entityInstance = createEntity();
            }
        }

        return entityInstance;
    }

    private boolean isPartitionSupported(Class<?> type) {
        return Partition.class.isAssignableFrom(type) && this.store.getConfig().supportsPartition();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityMapper that = (EntityMapper) o;

        if (!entityType.equals(that.entityType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return entityType.hashCode();
    }

}
