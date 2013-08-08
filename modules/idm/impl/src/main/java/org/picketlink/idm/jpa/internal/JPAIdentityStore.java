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

package org.picketlink.idm.jpa.internal;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.credential.handler.DigestCredentialHandler;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.credential.handler.TOTPCredentialHandler;
import org.picketlink.idm.credential.handler.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.annotations.Stored;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.internal.RelationshipReference;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.RelationshipMember;
import org.picketlink.idm.jpa.annotations.entity.ConfigurationName;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;
import org.picketlink.idm.jpa.internal.mappers.EntityMapper;
import org.picketlink.idm.jpa.internal.mappers.EntityMapping;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.*;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.*;
import static org.picketlink.idm.IDMMessages.*;
import static org.picketlink.idm.config.IdentityStoreConfiguration.*;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation
 * context.
 *
 * @author Shane Bryzak
 * @author Pedro Silva
 */
@CredentialHandlers(
        {
                PasswordCredentialHandler.class,
                X509CertificateCredentialHandler.class,
                DigestCredentialHandler.class,
                TOTPCredentialHandler.class
        })
public class JPAIdentityStore
        extends AbstractIdentityStore<JPAIdentityStoreConfiguration>
        implements CredentialStore<JPAIdentityStoreConfiguration>, PartitionStore<JPAIdentityStoreConfiguration>,
        AttributeStore<JPAIdentityStoreConfiguration> {

    // Invocation context parameters
    public static final String INVOCATION_CTX_ENTITY_MANAGER = "CTX_ENTITY_MANAGER";
    // Event context parameters
    public static final String EVENT_CONTEXT_IDENTITY = "IDENTITY_ENTITY";
    private final List<EntityMapper> entityMappers = new ArrayList<EntityMapper>();

    @Override
    public void setup(JPAIdentityStoreConfiguration config) {
        super.setup(config);

        for (Class<?> entityType : config.getEntityTypes()) {
            EntityMapper entityMapper = new EntityMapper(entityType, this);

            if (!entityMapper.getEntityMappings().isEmpty()) {
                this.entityMappers.add(entityMapper);
            }
        }
    }

    @Override
    public void addAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            entityMapper.persist(attributedType, entityManager);

            if (entityMapper.isRoot() && Relationship.class.isInstance(attributedType)) {
                storeRelationshipMembers((Relationship) attributedType, entityManager);
            }
        }

        entityManager.flush();
    }

    @Override
    public void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            entityMapper.updateEntity(attributedType, entityManager);
        }

        entityManager.flush();
    }

    @Override
    public void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper rootMapper = getRootMapper(attributedType.getClass());

        if (Relationship.class.isAssignableFrom(attributedType.getClass())) {
            removeChildRelationships(context, (Relationship) attributedType, entityManager);
        } else if (IdentityType.class.isInstance(attributedType)) {
            removeRelationships(context, (IdentityType) attributedType);
            removeCredentials(attributedType, entityManager);
        }

        removeAssociatedEntities(attributedType, entityManager, rootMapper);

        entityManager.remove(getRootEntity(attributedType, entityManager));
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        add(identityContext, partition);

        // now that the partition entity is created, let`s populate the configuration name.
        // the configuration name is not part of the Model API, so we need to do this manually.
        EntityMapper entityMapper = getRootMapper(partition.getClass());
        EntityManager entityManager = getEntityManager(identityContext);
        Object partitionEntity = getRootEntity(partition, entityManager);
        Entry<Property, Property> configurationNameProperty = entityMapper.getProperty(partition.getClass(), ConfigurationName.class);

        configurationNameProperty.getValue().setValue(partitionEntity, configurationName);

        entityManager.merge(partitionEntity);
        entityManager.flush();
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        EntityMapper entityMapper = getRootMapper(partition.getClass());
        EntityManager entityManager = getEntityManager(identityContext);
        Object partitionEntity = entityManager.find(entityMapper.getEntityType(), partition.getId());
        Entry<Property, Property> configurationNameProperty = entityMapper.getProperty(partition.getClass(), ConfigurationName.class);

        String configurationName = configurationNameProperty.getValue().getValue(partitionEntity).toString();

        if (configurationName == null) {
            throw new IdentityManagementException("No configuration name defined for partition [" + partition + "].");
        }

        return configurationName;
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        EntityManager entityManager = getEntityManager(identityContext);
        String PARTITION_NAME_PROPERTY = "name";

        EntityMapper entityMapper = getEntityMapperForProperty(partitionClass, PARTITION_NAME_PROPERTY);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(entityMapper.getEntityType());
        Root from = cq.from(entityMapper.getEntityType());

        Entry<Property, Property> nameEntityMapping = entityMapper.getProperty(partitionClass, PARTITION_NAME_PROPERTY);
        Entry<Property, Property> typeEntityMapping = entityMapper.getProperty(partitionClass, PartitionClass.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.equal(from.get(nameEntityMapping.getValue().getName()), name));

        if (!Partition.class.equals(partitionClass)) {
            predicates.add(cb.equal(from.get(typeEntityMapping.getValue().getName()),
                    partitionClass.getName()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query query = entityManager.createQuery(cq);

        query.setMaxResults(1);

        List result = query.getResultList();

        if (!result.isEmpty()) {
            return entityMapper.createType(result.get(0), entityManager);
        }

        return null;
    }

    @Override
    public <P extends Partition> P lookupById(final IdentityContext context, final Class<P> partitionClass,
                                              final String id) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper entityMapper = getRootMapper(Partition.class);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(entityMapper.getEntityType());
        Root from = cq.from(entityMapper.getEntityType());

        Entry<Property, Property> idEntityMapping = entityMapper.getProperty(Partition.class, Identifier.class);
        Entry<Property, Property> typeEntityMapping = entityMapper.getProperty(partitionClass, PartitionClass.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.equal(from.get(idEntityMapping.getValue().getName()), id));

        if (!Partition.class.equals(partitionClass)) {
            predicates.add(cb.equal(from.get(typeEntityMapping.getValue().getName()),
                    partitionClass.getName()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query query = entityManager.createQuery(cq);

        query.setMaxResults(1);

        List result = query.getResultList();

        if (!result.isEmpty()) {
            return entityMapper.createType(result.get(0), entityManager);
        }

        return null;
    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {
        update(identityContext, (AttributedType) partition);
    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {
        remove(identityContext, (AttributedType) partition);
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType attributedType, String attributeName) {
        EntityManager entityManager = getEntityManager(context);
        Map<String, Attribute<Serializable>> attributes = getAttributes(attributedType, attributeName, entityManager);

        return (Attribute<V>) attributes.get(attributeName);
    }

    @Override
    public void loadAttributes(IdentityContext context, AttributedType attributedType) {
        Map<String, Attribute<Serializable>> attributes = getAttributes(attributedType, null, getEntityManager(context));

        for (Attribute attribute : attributes.values()) {
            attributedType.setAttribute(attribute);
        }
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType attributedType, String attributeName) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());

        Entry<Property, Property> attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class);
        Entry<Property, Property> ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = cb.createQuery(attributeMapper.getEntityType());
        Root<?> from = cq.from(attributeMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(cb.equal(from.get(attributeNameProperty.getValue().getName()), attributeName));

        if (getConfig().supportsType(attributedType.getClass(), IdentityOperation.create)
                && !String.class.equals(ownerProperty.getValue().getJavaClass())) {
            predicates.add(cb.equal(from.get(ownerProperty.getValue().getName()),
                    getOwnerEntity(attributedType, ownerProperty.getValue(), entityManager)));
        } else {
            predicates.add(cb.equal(from.get(ownerProperty.getValue().getName()), attributedType.getId()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        for (Object entity : entityManager.createQuery(cq).getResultList()) {
            entityManager.remove(entity);
        }

        entityManager.flush();
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> result = new ArrayList<V>();
        EntityManager entityManager = getEntityManager(context);
        EntityMapper rootMapper = getRootMapper(identityQuery.getIdentityType());

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] parameter = identityQuery.getParameter(IdentityType.ID);

            if (parameter.length > 0) {
                Object entity = entityManager.find(rootMapper.getEntityType(), parameter[0]);

                if (entity != null) {
                    result.add(rootMapper.<V>createType(entity, entityManager));
                }
            }
        } else {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery cq = cb.createQuery(rootMapper.getEntityType());
            List<Predicate> predicates = new ArrayList<Predicate>();
            Root<?> from = cq.from(rootMapper.getEntityType());

            Partition partition = context.getPartition();

            if (identityQuery.getParameter(IdentityType.PARTITION) != null) {
                partition = (Partition) identityQuery.getParameter(IdentityType.PARTITION)[0];
            }

            Entry<Property, Property> partitionProperty = rootMapper.getProperty(OwnerReference.class);

            if (partitionProperty != null) {
                Join<Object, Object> join = from.join(partitionProperty.getValue().getName());
                predicates.add(cb.equal(join, entityManager.find(partitionProperty.getValue().getJavaClass(), partition.getId())));
            }

            if (!IdentityType.class.equals(identityQuery.getIdentityType())) {
                Entry<Property, Property> property = rootMapper.getProperty(identityQuery.getIdentityType(), IdentityClass.class);
                predicates.add(cb.equal(from.get(property.getValue().getName()), identityQuery.getIdentityType().getName()));
            }

            for (QueryParameter queryParameter : identityQuery.getParameters().keySet()) {
                if (IdentityType.PARTITION.equals(queryParameter)) {
                    continue;
                }

                if (AttributeParameter.class.isInstance(queryParameter)) {
                    AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                    Object[] parameterValues = identityQuery.getParameter(attributeParameter);
                    EntityMapper parameterEntityMapper =
                            getEntityMapperForProperty(identityQuery.getIdentityType(), attributeParameter.getName());

                    if (parameterEntityMapper != null) {
                        Root<?> propertyEntityJoin = from;

                        Entry<Property, Property> ownerProperty = parameterEntityMapper.getProperty(identityQuery.getIdentityType(), OwnerReference.class);

                        if (ownerProperty.getValue().getJavaClass().equals(rootMapper.getEntityType())) {
                            propertyEntityJoin = cq.from(parameterEntityMapper.getEntityType());

                            if (ownerProperty.getValue().getAnnotatedElement().isAnnotationPresent(Id.class)) {
                                predicates.add(cb.and(cb.equal(propertyEntityJoin, from)));
                            } else {
                                predicates.add(cb.and(cb.equal(propertyEntityJoin.get(ownerProperty.getValue()
                                        .getName()), from)));
                            }
                        }

                        Object parameterValue = parameterValues[0];

                        Property mappedProperty = (Property) parameterEntityMapper.getProperty(identityQuery.getIdentityType(), attributeParameter.getName()).getValue();

                        if (IdentityType.CREATED_AFTER.equals(queryParameter) || IdentityType.EXPIRY_AFTER.equals(queryParameter)) {
                            predicates.add(cb.greaterThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getName()), (Date) parameterValue));
                        } else if (IdentityType.CREATED_BEFORE.equals(queryParameter) || IdentityType.EXPIRY_BEFORE.equals(queryParameter)) {
                            predicates.add(cb.lessThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getName()), (Date) parameterValue));
                        } else {
                            if (isMappedType(mappedProperty.getJavaClass())) {
                                AttributedType ownerType = (AttributedType) parameterValue;

                                if (ownerType != null) {
                                    parameterValue = entityManager.find(mappedProperty.getJavaClass(), ownerType.getId());
                                }
                            }

                            predicates.add(cb.equal(propertyEntityJoin.get(mappedProperty.getName()), parameterValue));
                        }
                    } else {
                        addAttributeQueryPredicates(identityQuery.getIdentityType(), cb, cq, from, predicates,
                                attributeParameter,
                                parameterValues);
                    }
                }
            }

            cq.select(from);
            cq.where(predicates.toArray(new Predicate[predicates.size()]));

            Query query = entityManager.createQuery(cq);

            if (identityQuery.getLimit() > 0) {
                query.setMaxResults(identityQuery.getLimit());

                if (identityQuery.getOffset() > 0) {
                    query.setFirstResult(identityQuery.getOffset());
                }
            }

            for (Object entity : query.getResultList()) {
                result.add(rootMapper.<V>createType(entity, entityManager));
            }
        }

        return result;
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        int limit = identityQuery.getLimit();
        int offset = identityQuery.getOffset();

        identityQuery.setLimit(0);
        identityQuery.setOffset(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);
        identityQuery.setOffset(offset);

        return resultCount;
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        EntityManager entityManager = getEntityManager(context);
        List entities = new ArrayList();

        Object[] identityParameterValues = query.getParameter(Relationship.IDENTITY);

        if (identityParameterValues != null) {
            for (Object parameterValue : identityParameterValues) {
                if (IdentityType.class.isInstance(parameterValue)) {
                    entities = findIdentityTypeRelationships(context, (IdentityType) parameterValue);
                } else {
                    throw MESSAGES.queryUnsupportedParameterValue("Relationship.IDENTITY", parameterValue);
                }
            }
        } else {
            EntityMapper entityMapper = getRootMapper(query.getRelationshipClass());
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<?> cq = cb.createQuery(entityMapper.getEntityType());
            Root from = cq.from(entityMapper.getEntityType());

            List<Predicate> predicates = new ArrayList<Predicate>();

            Property typeProperty = entityMapper.getProperty(RelationshipClass.class).getValue();

            if (!Relationship.class.equals(query.getRelationshipClass())) {
                predicates.add(cb.equal(from.get(typeProperty.getName()), query.getRelationshipClass().getName()));
            }

            Object[] idParameter = query.getParameter(Relationship.ID);

            if (idParameter != null && idParameter.length > 0) {
                Property idProperty = entityMapper.getProperty(Identifier.class).getValue();
                predicates.add(cb.equal(from.get(idProperty.getName()), idParameter[0]));
            } else {
                for (Entry<QueryParameter, Object[]> entry : query.getParameters().entrySet()) {
                    QueryParameter queryParameter = entry.getKey();
                    Object[] values = entry.getValue();

                    if (queryParameter instanceof RelationshipQueryParameter) {
                        RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) entry.getKey();
                        List<String> identityTypeIdentifiers = new ArrayList<String>();

                        for (Object object : values) {
                            IdentityType identityType = (IdentityType) object;

                            if (identityType == null) {
                                return Collections.emptyList();
                            }

                            if (getConfig().supportsType(identityType.getClass(), IdentityOperation.create)) {
                                identityTypeIdentifiers.add(identityType.getId());
                            } else {
                                identityTypeIdentifiers.add(RelationshipReference.formatId(identityType));
                            }
                        }

                        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);
                        Property<Object> relationshipProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

                        Subquery<?> subquery = cq.subquery(relationshipMemberMapper.getEntityType());
                        Root fromRelationshipIdentityType = subquery.from(relationshipMemberMapper.getEntityType());
                        subquery.select(fromRelationshipIdentityType.get(relationshipProperty.getName()));

                        Property<String> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor
                                .class).getValue();

                        Predicate conjunction = cb.conjunction();

                        conjunction.getExpressions().add(
                                cb.equal(fromRelationshipIdentityType.get(descriptorProperty.getName()),
                                        identityTypeParameter.getName()));

                        Property<Object> identityProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

                        if (identityProperty.getJavaClass().equals(String.class)) {
                            conjunction.getExpressions().add(fromRelationshipIdentityType.get(identityProperty.getName()).in(identityTypeIdentifiers));
                        } else {
                            Join join = fromRelationshipIdentityType.join(identityProperty.getName());
                            EntityMapper identityTypeMapper = getMapperForEntity(identityProperty.getJavaClass());
                            Property identifierProperty = identityTypeMapper.getProperty(Identifier.class).getValue();

                            conjunction.getExpressions().add(join.get(identifierProperty.getName()).in(identityTypeIdentifiers));
                        }

                        subquery.where(conjunction);

                        predicates.add(cb.in(from).value(subquery));
                    } else if (AttributeParameter.class.equals(entry.getKey().getClass())) {
                        AttributeParameter attributeParameter = (AttributeParameter) entry.getKey();
                        Object[] parameterValues = entry.getValue();
                        EntityMapper parameterEntityMapper =
                                getEntityMapperForProperty(query.getRelationshipClass(), attributeParameter.getName());

                        if (parameterEntityMapper != null) {
                            Root<?> propertyEntityJoin = from;

                            Entry<Property, Property> ownerProperty = parameterEntityMapper.getProperty(query
                                    .getRelationshipClass(), OwnerReference.class);

                            if (ownerProperty.getValue().getJavaClass().equals(entityMapper.getEntityType())) {
                                propertyEntityJoin = cq.from(parameterEntityMapper.getEntityType());
                                predicates.add(cb.and(cb.equal(propertyEntityJoin.get(ownerProperty.getValue()
                                        .getName()), from)));
                            }

                            Object parameterValue = parameterValues[0];

                            Property mappedProperty = (Property) parameterEntityMapper.getProperty(query
                                    .getRelationshipClass(), attributeParameter.getName()).getValue();

                            if (isMappedType(mappedProperty.getJavaClass())) {
                                AttributedType ownerType = (AttributedType) parameterValue;

                                if (ownerType != null) {
                                    parameterValue = entityManager.find(mappedProperty.getJavaClass(), ownerType.getId());
                                }
                            }

                            predicates.add(cb.equal(propertyEntityJoin.get(mappedProperty.getName()), parameterValue));
                        } else {
                            addAttributeQueryPredicates(query.getRelationshipClass(), cb, cq, from, predicates,
                                    attributeParameter,
                                    parameterValues);
                        }
                    }
                }
            }

            cq.select(from);
            cq.where(predicates.toArray(new Predicate[predicates.size()]));

            entities = entityManager.createQuery(cq).getResultList();
        }

        List<V> result = new ArrayList<V>();

        for (Object relationshipObject : entities) {
            result.add((V) convertToRelationshipType(context, relationshipObject));
        }

        return result;
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType attributedType, Attribute<? extends Serializable> attribute) {
        removeAttribute(context, attributedType, attribute.getName());

        Serializable values = attribute.getValue();

        if (!values.getClass().isArray()) {
            values = new Serializable[]{values};
        }

        if (values instanceof byte[]) {
            values = new Serializable[]{values};
        }

        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();
        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        EntityManager entityManager = getEntityManager(context);

        for (Serializable attributeValue : (Serializable[]) values) {
            Object attributeEntity = null;

            try {
                attributeEntity = attributeMapper.getEntityType().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(attributeMapper.getEntityType(), e);
            }

            attributeNameProperty.setValue(attributeEntity, attribute.getName());
            attributeValueProperty.setValue(attributeEntity, Base64.encodeObject(attributeValue));

            if (getConfig().supportsType(attributedType.getClass(), IdentityOperation.create)
                    && !String.class.equals(ownerProperty.getJavaClass())) {
                ownerProperty.setValue(attributeEntity, getOwnerEntity(attributedType, ownerProperty, entityManager));
            } else {
                ownerProperty.setValue(attributeEntity, attributedType.getId());
            }

            entityManager.persist(attributeEntity);
        }
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        Class<? extends CredentialStorage> storageType = storage.getClass();
        EntityMapper attributeMapper = getCredentialAttributeMapper(storageType);

        Object newCredential = null;

        try {
            newCredential = attributeMapper.getEntityType().newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(attributeMapper.getEntityType(), e);
        }

        Date effectiveDate = storage.getEffectiveDate();

        if (effectiveDate == null) {
            effectiveDate = new Date();
        }

        EntityManager entityManager = getEntityManager(context);

        Object ownerEntity = getRootEntity(account, entityManager);

        Property ownerProperty = attributeMapper.getProperty(storageType, OwnerReference.class).getValue();
        Property typeProperty = attributeMapper.getProperty(storageType, CredentialClass.class).getValue();
        Property effectiveProperty = attributeMapper.getProperty(storageType, EffectiveDate.class).getValue();
        Property expiryProperty = attributeMapper.getProperty(storageType, ExpiryDate.class).getValue();

        ownerProperty.setValue(newCredential, ownerEntity);
        typeProperty.setValue(newCredential, storageType.getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        for (Property<Object> property : PropertyQueries
                .createQuery(storageType)
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class))
                .getResultList()) {
            Entry<Property, Property> mappedProperty = attributeMapper.getProperty(storageType, property.getName());

            if (mappedProperty == null) {
                throw new IdentityManagementException("No mapping found for property [" + storageType + "." + property.getName() + "].");
            }

            mappedProperty.getValue().setValue(newCredential, property.getValue(storage));
        }

        entityManager.persist(newCredential);
        entityManager.flush();
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account
            account, Class<T> storageClass) {
        EntityMapper attributeMapper = getCredentialAttributeMapper(storageClass);

        Property identityTypeProperty = attributeMapper.getProperty(storageClass, OwnerReference.class).getValue();
        Property typeProperty = attributeMapper.getProperty(storageClass, CredentialClass.class).getValue();
        Property effectiveProperty = attributeMapper.getProperty(storageClass, EffectiveDate.class).getValue();

        EntityManager entityManager = getEntityManager(context);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
        Root<?> root = criteria.from(attributeMapper.getEntityType());

        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = getRootEntity(account, entityManager);

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date>get(effectiveProperty.getName()), new Date()));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        Object lastCredential = null;

        Query query = entityManager.createQuery(criteria);

        query.setMaxResults(1);

        List<?> result = query.getResultList();

        if (!result.isEmpty()) {
            lastCredential = result.get(0);
        }

        return convertToCredentialStorage(lastCredential, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account
            account, Class<T> storageClass) {
        EntityMapper attributeMapper = getCredentialAttributeMapper(storageClass);

        Property identityTypeProperty = attributeMapper.getProperty(storageClass, OwnerReference.class).getValue();
        Property typeProperty = attributeMapper.getProperty(storageClass, CredentialClass.class).getValue();
        Property effectiveProperty = attributeMapper.getProperty(storageClass, EffectiveDate.class).getValue();

        EntityManager entityManager = getEntityManager(context);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
        Root<?> root = criteria.from(attributeMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = getRootEntity(account, entityManager);

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        List<T> storages = new ArrayList<T>();

        for (Object object : entityManager.createQuery(criteria).getResultList()) {
            storages.add(convertToCredentialStorage(object, storageClass));
        }

        return storages;
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    public Object getOwnerEntity(final AttributedType attributedType, final Property ownerProperty,
                                 final EntityManager entityManager) {
        EntityMapper attributedTypeMapper = getRootMapper(attributedType.getClass());

        Object entity = null;

        if (ownerProperty.getJavaClass().isAssignableFrom(attributedTypeMapper.getEntityType())) {
            entity = getRootEntity(attributedType, entityManager);
        } else {
            EntityMapper ownerMapper = getMapperForEntity(ownerProperty.getJavaClass());

            List associatedEntities = attributedTypeMapper.getAssociatedEntities(attributedType, ownerMapper, entityManager);

            if (!associatedEntities.isEmpty()) {
                entity = associatedEntities.get(0);
            }
        }
        return entity;
    }

    private EntityManager getEntityManager(IdentityContext context) {
        if (!context.isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    public List<EntityMapper> getMapperFor(Class<? extends AttributedType> attributedType) {
        List<EntityMapper> mappers = new ArrayList<EntityMapper>();
        EntityMapper rootEntityMapper = null;

        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.getEntityType().isAnnotationPresent(IdentityManaged.class)) {
                for (EntityMapping entityMapping : entityMapper.getEntityMappings()) {
                    if (entityMapping.getSupportedType().equals(attributedType)) {
                        mappers.add(entityMapper);

                        if (entityMapper.isRoot()) {
                            rootEntityMapper = entityMapper;
                        }
                    }
                }

                if (rootEntityMapper == null) {
                    for (EntityMapping entityMapping : entityMapper.getEntityMappings()) {
                        if (entityMapping.getSupportedType().isAssignableFrom(attributedType)
                                || attributedType.isAssignableFrom(entityMapping.getSupportedType())) {
                            mappers.add(entityMapper);

                            if (entityMapper.isRoot()) {
                                rootEntityMapper = entityMapper;
                            }
                        }
                    }
                }
            }
        }

        if (mappers.isEmpty()) {
            throw new IdentityManagementException("No entity mapper found for type [" + attributedType + "].");
        }

        if (rootEntityMapper == null) {
            throw new IdentityManagementException("No root mapper found for type [" + attributedType + "].");
        }

        // we always put the root mapper at the first index,
        // this allows other mappers to load owner references by the identifier.
        mappers.remove(rootEntityMapper);
        mappers.add(0, rootEntityMapper);

        return mappers;
    }

    public EntityMapper getRootMapperForEntity(Class<?> entityClass) {
        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.isRoot() && entityMapper.getEntityType().equals(entityClass)) {
                return entityMapper;
            }
        }

        throw new IdentityManagementException("No mapper for entity type [" + entityClass + "].");
    }

    public EntityMapper getMapperForEntity(Class<?> entityClass) {
        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.getEntityType().equals(entityClass)) {
                return entityMapper;
            }
        }

        throw new IdentityManagementException("No mapper for entity type [" + entityClass + "].");
    }

    public List<EntityMapper> getEntityMappers() {
        return this.entityMappers;
    }

    public boolean isMappedType(Class mappedClass) {
        for (EntityMapper entityMapper : getEntityMappers()) {
            if (entityMapper.getEntityType().equals(mappedClass)) {
                return true;
            }
        }

        return false;
    }

    public Object getRootEntity(AttributedType attributedType, EntityManager entityManager) {
        return entityManager.find(getRootMapper(attributedType.getClass()).getEntityType(), attributedType.getId());
    }

    private EntityMapper getEntityMapperForProperty(Class<? extends AttributedType> attributedType, String propertyName) {
        for (EntityMapper entityMapper : getMapperFor(attributedType)) {
            Entry<Property, Property> property = entityMapper.getProperty(attributedType, propertyName);

            if (property != null) {
                return entityMapper;
            }
        }

        return null;
    }

    private EntityMapper getEntityMapperForProperty(Class<? extends Annotation> annotation) {
        for (EntityMapper entityMapper : this.entityMappers) {
            Entry<Property, Property> property = entityMapper.getProperty(annotation);

            if (property != null) {
                return entityMapper;
            }
        }

        return null;
    }

    private List<?> findIdentityTypeRelationships(IdentityContext context, IdentityType identityType) {
        EntityManager em = getEntityManager(context);
        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(relationshipMemberMapper.getEntityType());
        Root<?> root = criteria.from(relationshipMemberMapper.getEntityType());

        Property<Object> identityTypeProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

        if (identityTypeProperty.getJavaClass().equals(String.class)) {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()),
                    RelationshipReference.formatId(identityType)));
        } else {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()),
                    em.find(identityTypeProperty.getJavaClass(), identityType.getId())));
        }

        List<Object> relationships = new ArrayList<Object>();

        List<?> result = em.createQuery(criteria).getResultList();

        Property<Object> ownerProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

        for (Object object : result) {
            relationships.add(ownerProperty.getValue(object));
        }

        return relationships;

    }

    private <T extends Relationship> T convertToRelationshipType(IdentityContext context, Object relationshipObject) {
        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        Property<Object> identityProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();
        Property<String> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor.class).getValue();
        EntityManager entityManager = getEntityManager(context);

        EntityMapper relMapper = getRootMapper(Relationship.class);

        T relationshipType = relMapper.createType(relationshipObject, entityManager);

        List<?> identities = findChildRelationships(context, relationshipType);

        boolean supportsType = getConfig().supportsType(IdentityType.class, IdentityOperation.create);

        RelationshipReference reference = null;

        if (!supportsType) {
            reference = new RelationshipReference(relationshipType);
        }

        for (Object object : identities) {
            String descriptor = descriptorProperty.getValue(object).toString();

            Property<Object> identityTypeProperty = PropertyQueries.createQuery(relationshipType.getClass())
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getSingleResult();
            IdentityType identityType = null;

            Object identityTypeEntity = identityProperty.getValue(object);

            if (!supportsType) {
                reference.addIdentityTypeReference(descriptor, identityTypeEntity.toString());
            } else {
                EntityMapper entityMapper = getRootMapperForEntity(identityTypeEntity.getClass());

                identityType = entityMapper.createType(identityTypeEntity, entityManager);
            }

            identityTypeProperty.setValue(relationshipType, identityType);
        }

        if (reference != null) {
            return (T) reference;
        } else {
            return relationshipType;
        }
    }

    private List<?> findChildRelationships(IdentityContext context, Relationship relationship) {
        EntityManager em = getEntityManager(context);
        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(relationshipMemberMapper.getEntityType());
        Root<?> root = criteria.from(relationshipMemberMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property ownerProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

        Join<?, ?> join = root.join(ownerProperty.getName());

        EntityMapper relationshipMapper = getRootMapper(relationship.getClass());

        Property identifierProperty = relationshipMapper.getProperty(Identifier.class).getValue();

        predicates.add(builder.equal(join.get(identifierProperty.getName()), relationship.getId()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private void removeRelationships(IdentityContext context, IdentityType identityType) {
        // First we build a list of all the relationships that the specified identity
        // is participating in
        List<?> relationshipsToRemove = findIdentityTypeRelationships(context, identityType);

        // Now that we have the list, we can iterate through and remove the records
        for (Object relationship : relationshipsToRemove) {
            remove(context, convertToRelationshipType(context, relationship));
        }
    }

    private <T extends CredentialStorage> T convertToCredentialStorage(Object entity, Class<T> storageType) {
        T storage = null;

        if (entity != null) {
            EntityMapper attributeMapper = getCredentialAttributeMapper(storageType);

            try {
                storage = storageType.newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(storageType, e);
            }

            Entry<Property, Property> effectiveProperty = attributeMapper.getProperty(storageType, EffectiveDate.class);
            Entry<Property, Property> expiryProperty = attributeMapper.getProperty(storageType, ExpiryDate.class);

            effectiveProperty.getKey().setValue(storage, effectiveProperty.getValue().getValue(entity));
            expiryProperty.getKey().setValue(storage, expiryProperty.getValue().getValue(entity));

            for (Property<Object> property : PropertyQueries
                    .createQuery(storageType)
                    .addCriteria(new AnnotatedPropertyCriteria(Stored.class))
                    .getResultList()) {
                Entry<Property, Property> mappedProperty = attributeMapper.getProperty(storageType, property.getName());

                if (mappedProperty == null) {
                    throw new IdentityManagementException("No mapping found for property [" + storageType + "." + property.getName() + "].");
                }

                mappedProperty.getKey().setValue(storage, mappedProperty.getValue().getValue(entity));
            }

        }

        return storage;
    }

    private EntityMapper getCredentialAttributeMapper(Class<? extends CredentialStorage> credentialStorageClass) {
        for (EntityMapper entityMapper : this.entityMappers) {
            ManagedCredential managedCredential = entityMapper.getEntityType().getAnnotation(ManagedCredential.class);

            if (managedCredential != null) {
                if (managedCredential.value().length > 0) {
                    for (Class<?> supportedType : managedCredential.value()) {
                        if (supportedType.equals(credentialStorageClass)) {
                            return entityMapper;
                        }
                    }

                    for (Class<?> supportedType : managedCredential.value()) {
                        if (supportedType.isAssignableFrom(credentialStorageClass)) {
                            return entityMapper;
                        }
                    }
                } else {
                    return entityMapper;
                }
            }
        }

        throw new IdentityManagementException("No mapper for for credential storage type [" + credentialStorageClass + "].");
    }

    private void removeCredentials(AttributedType attributedType, EntityManager entityManager) {
        List entities = new ArrayList();

        for (EntityMapper attributeMapper : getEntityMappers()) {
            if (attributeMapper.getEntityType().isAnnotationPresent(ManagedCredential.class)) {
                Property identityTypeProperty = attributeMapper.getProperty(OwnerReference.class).getValue();
                Property effectiveProperty = attributeMapper.getProperty(EffectiveDate.class).getValue();

                CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
                Root<?> root = criteria.from(attributeMapper.getEntityType());
                List<Predicate> predicates = new ArrayList<Predicate>();

                Object agentInstance = getRootEntity(attributedType, entityManager);

                predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

                List result = entityManager.createQuery(criteria).getResultList();

                for (Object storageEntity : result) {
                    entities.add(storageEntity);
                }
            }
        }

        for (Object credentialEntity : entities) {
            entityManager.remove(credentialEntity);
        }
    }

    private void removeChildRelationships(final IdentityContext context, final Relationship attributedType, final EntityManager entityManager) {
        for (Object child : findChildRelationships(context, (Relationship) attributedType)) {
            entityManager.remove(child);
        }
    }

    private void removeAssociatedEntities(final AttributedType attributedType, final EntityManager entityManager, final EntityMapper rootMapper) {
        for (EntityMapper childMapper : getMapperFor(attributedType.getClass())) {
            if (!childMapper.isRoot()) {
                for (Object child : rootMapper.getAssociatedEntities(attributedType, childMapper, entityManager)) {
                    entityManager.remove(child);
                }
            }
        }
    }

    private EntityMapper getRootMapper(Class<? extends AttributedType> aClass) {
        return getMapperFor(aClass).get(0);
    }

    private Map<String, Attribute<Serializable>> getAttributes(final AttributedType attributedType, final String attributeName, final EntityManager entityManager) {
        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());

        Class<?> attributeEntityClass = attributeMapper.getEntityType();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = cb.createQuery(attributeEntityClass);
        Root<?> from = cq.from(attributeEntityClass);
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();

        if (attributeName != null) {
            predicates.add(cb.equal(from.get(attributeNameProperty.getName()),
                    attributeName));
        }

        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        if (getConfig().supportsType(attributedType.getClass(), IdentityOperation.create)
                && !String.class.equals(ownerProperty.getJavaClass())) {
            predicates.add(cb.equal(from.get(ownerProperty.getName()),
                    getOwnerEntity(attributedType, ownerProperty, entityManager)));
        } else {
            predicates.add(cb.equal(from.get(ownerProperty.getName()), attributedType.getId()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
        Map<String, Attribute<Serializable>> attributes = new HashMap<String, Attribute<Serializable>>();

        for (Object attributeEntity : entityManager.createQuery(cq).getResultList()) {
            String storedName = attributeNameProperty.getValue(attributeEntity).toString();
            Serializable storedValue = (Serializable) Base64.decodeToObject(attributeValueProperty.getValue(attributeEntity).toString());

            Attribute<Serializable> attribute = attributes.get(storedName);

            if (attribute == null) {
                attribute = new Attribute<Serializable>(storedName, storedValue);
            } else {
                // if it is a multi-valued attribute
                if (attribute != null) {
                    Serializable[] values = null;

                    if (attribute.getValue().getClass().isArray()) {
                        values = (Serializable[]) attribute.getValue();
                    } else {
                        values = (Serializable[]) Array.newInstance(attribute.getValue().getClass(), 1);
                        values[0] = attribute.getValue();
                    }

                    Serializable[] newValues = Arrays.copyOf(values, values.length + 1);

                    newValues[newValues.length - 1] = storedValue;

                    attribute.setValue(newValues);

                }
            }

            attributes.put(attribute.getName(), attribute);
        }

        return attributes;
    }

    private void addAttributeQueryPredicates(Class<? extends AttributedType> attributedType,
                                             final CriteriaBuilder cb,
                                             final CriteriaQuery<?> cq,
                                             final Root from,
                                             final List<Predicate> predicates,
                                             final AttributeParameter attributeParameter,
                                             final Object[] parameterValues) {
        String[] valuesToSearch = new String[parameterValues.length];

        for (int i = 0; i < parameterValues.length; i++) {
            valuesToSearch[i] = Base64.encodeObject((Serializable) parameterValues[i]);
        }

        EntityMapper attributeMapper = getAttributeMapper(attributedType);

        Class<?> attributeEntityClass = attributeMapper.getEntityType();
        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();
        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        Subquery<?> subQuery = cq.subquery(attributeEntityClass);
        Root fromProject = subQuery.from(attributeEntityClass);
        subQuery.select(fromProject.get(ownerProperty.getName()));

        Predicate conjunction = cb.conjunction();

        conjunction.getExpressions().add(
                cb.equal(
                        fromProject.get(attributeNameProperty.getName()),
                        attributeParameter.getName()));
        conjunction.getExpressions().add(
                (fromProject.get(attributeValueProperty.getName())
                        .in((Object[]) valuesToSearch)));

        subQuery.where(conjunction);

        subQuery.groupBy(subQuery.getSelection()).having(
                cb.equal(cb.count(subQuery.getSelection()), valuesToSearch.length));

        predicates.add(cb.in(from).value(subQuery));
    }

    private EntityMapper getAttributeMapper(Class<? extends AttributedType> attributedType) {
        List<EntityMapper> attributeMappers = new ArrayList<EntityMapper>();

        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.getMappingsFor(Attribute.class) != null) {
                attributeMappers.add(entityMapper);
            }
        }

        EntityMapper secondaryMapper = null;

        if (getConfig().supportsType(attributedType, IdentityOperation.create)) {
            List<EntityMapper> attributedTypeMappers = getMapperFor(attributedType);

            for (EntityMapper entityMapper : attributedTypeMappers) {
                Class<?> entityType = entityMapper.getEntityType();

                for (EntityMapper mapper : attributeMappers) {
                    EntityMapping mappings = mapper.getMappingsFor(Attribute.class);

                    if (mappings != null) {
                        if (mappings.getOwnerType().equals(entityType)) {
                            return mapper;
                        } else if (mappings.getOwnerType().isAssignableFrom(entityType)) {
                            secondaryMapper = mapper;
                        }
                    }
                }
            }

            if (secondaryMapper != null) {
                return secondaryMapper;
            }
        }

        for (EntityMapper mapper : attributeMappers) {
            EntityMapping mappings = mapper.getMappingsFor(Attribute.class);

            if (mappings != null) {
                if (String.class.equals(mappings.getOwnerType())) {
                    return mapper;
                }
            }
        }

        throw new IdentityManagementException("Could not find mapper for attributes for type [" + attributedType + "].");
    }

    private void storeRelationshipMembers(Relationship relationship, EntityManager entityManager) {
        Object ownerEntity = getRootEntity(relationship, entityManager);

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE)).getResultList();

        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        for (Property<IdentityType> prop : props) {
            Object relationshipIdentity = null;

            try {
                relationshipIdentity = relationshipMemberMapper.getEntityType().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(relationshipMemberMapper.getEntityType(), e);
            }

            IdentityType identityType = prop.getValue(relationship);

            if (identityType != null) {
                Property<Object> identityTypeProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

                if (getConfig().supportsType(identityType.getClass(), IdentityOperation.create)) {
                    identityTypeProperty.setValue(relationshipIdentity, getRootEntity(identityType, entityManager));
                } else {
                    if (!String.class.equals(identityTypeProperty.getJavaClass())) {
                        throw new IdentityManagementException("This store does not support type [" + identityType
                                .getClass() + "]. @RelationshipMember should reference a String field to store " +
                                "only ids.");
                    }

                    identityTypeProperty.setValue(relationshipIdentity, RelationshipReference.formatId(identityType));
                }

                Property<Object> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor.class).getValue();
                Property<Object> ownerProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

                descriptorProperty.setValue(relationshipIdentity, prop.getName());
                ownerProperty.setValue(relationshipIdentity, ownerEntity);
            }

            entityManager.persist(relationshipIdentity);
        }
    }

}