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
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
import org.picketlink.idm.jpa.annotations.ExpiryDate;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PartitionClass;
import org.picketlink.idm.jpa.annotations.entity.ConfigurationName;
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
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Map.*;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.*;
import static org.picketlink.idm.IDMMessages.*;

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
        implements CredentialStore<JPAIdentityStoreConfiguration>, PartitionStore<JPAIdentityStoreConfiguration> {

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

        Object rootEntity = entityManager.find(rootMapper.getEntityType(), attributedType.getId());

        if (Relationship.class.isAssignableFrom(attributedType.getClass())) {
            List<?> childRelationships = findChildRelationships(context, (Relationship) attributedType);

            for (Object child : childRelationships) {
                entityManager.remove(child);
            }
        } else if (IdentityType.class.isInstance(attributedType)) {
            removeRelationships(context, attributedType);
            removeCredentials(context, attributedType, entityManager);
        }

        for (EntityMapper childMapper : getMapperFor(attributedType.getClass())) {
            if (!childMapper.isRoot()) {
                for (Object child : rootMapper.getAssociatedEntities(attributedType, childMapper, entityManager)) {
                    entityManager.remove(child);
                }
            }
        }

        removeAttributes(context, attributedType);

        entityManager.remove(rootEntity);
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        add(identityContext, partition);

        // now that the partition entity is created, let`s populate the configuration name.
        // the configuration name is not part of the Model API, so we need to do this manually.
        EntityMapper entityMapper = getRootMapper(partition.getClass());
        EntityManager entityManager = getEntityManager(identityContext);
        Object partitionEntity = entityManager.find(entityMapper.getEntityType(), partition.getId());
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

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery query = criteriaBuilder.createQuery(entityMapper.getEntityType());
        Root from = query.from(entityMapper.getEntityType());

        Entry<Property, Property> nameEntityMapping = entityMapper.getProperty(partitionClass, PARTITION_NAME_PROPERTY);
        Entry<Property, Property> typeEntityMapping = entityMapper.getProperty(partitionClass, PartitionClass.class);

        query.where(
                criteriaBuilder.equal(from.get(typeEntityMapping.getValue().getName()), partitionClass.getName()),
                criteriaBuilder.equal(from.get(nameEntityMapping.getValue().getName()), name));

        List result = entityManager.createQuery(query).getResultList();

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
        populateAttribute(attributedType, getEntityManager(context), attributeName);
        return attributedType.getAttribute(attributeName);
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType type, String attributeName) {
        removeAttribute(type, attributeName, getEntityManager(context));
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> result = new ArrayList<V>();
        EntityManager entityManager = getEntityManager(context);

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] parameter = identityQuery.getParameter(IdentityType.ID);

            if (parameter.length > 0) {
                for (EntityMapper mapper : this.entityMappers) {
                    if (mapper.isRoot()) {
                        EntityMapping mapping = mapper.getMappingsFor(identityQuery.getIdentityType());

                        if (mapping != null) {
                            Object entity = entityManager.find(mapper.getEntityType(), parameter[0]);

                            if (entity != null && entity.getClass().equals(mapper.getEntityType())) {
                                result.add(mapper.<V>createType(entity, entityManager));
                                return result;
                            }
                        }
                    }
                }
            }
        } else {
            EntityMapper rootMapper = getRootMapper(identityQuery.getIdentityType());
            CriteriaBuilder qb = entityManager.getCriteriaBuilder();
            CriteriaQuery cq = qb.createQuery(rootMapper.getEntityType());
            List<Predicate> predicates = new ArrayList<Predicate>();
            Root<?> from = cq.from(rootMapper.getEntityType());

            Partition partition = context.getPartition();

            if (identityQuery.getParameter(IdentityType.PARTITION) != null) {
                partition = (Partition) identityQuery.getParameter(IdentityType.PARTITION)[0];
            }

            EntityMapping rootMapping = rootMapper.getMappingsFor(identityQuery.getIdentityType());
            EntityMapper partitionRootMapper = getRootMapper(partition.getClass());

            for (Property ownerProperty : rootMapping.getProperties().keySet()) {
                Property mappedOwnerProperty = rootMapping.getProperties().get(ownerProperty);

                if (mappedOwnerProperty.getJavaClass().equals(partitionRootMapper.getEntityType())) {
                    Join<Object, Object> join = from.join(mappedOwnerProperty.getName());
                    predicates.add(qb.equal(join, entityManager.find(partitionRootMapper.getEntityType(), partition.getId())));
                }
            }

            if (!IdentityType.class.equals(identityQuery.getIdentityType())) {
                Entry<Property, Property> property = rootMapper.getProperty(identityQuery.getIdentityType(), IdentityClass.class);
                predicates.add(qb.equal(from.get(property.getValue().getName()), identityQuery.getIdentityType().getName()));
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
                            predicates.add(qb.and(qb.equal(from, propertyEntityJoin)));
                        }

                        Object parameterValue = parameterValues[0];

                        Property mappedProperty = (Property) parameterEntityMapper.getProperty(identityQuery.getIdentityType(), attributeParameter.getName()).getValue();

                        if (IdentityType.CREATED_AFTER.equals(queryParameter) || IdentityType.EXPIRY_AFTER.equals(queryParameter)) {
                            predicates.add(qb.greaterThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getName()), (Date) parameterValue));
                        } else if (IdentityType.CREATED_BEFORE.equals(queryParameter) || IdentityType.EXPIRY_BEFORE.equals(queryParameter)) {
                            predicates.add(qb.lessThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getName()), (Date) parameterValue));
                        } else {
                            if (isMappedType(mappedProperty.getJavaClass())) {
                                AttributedType ownerType = (AttributedType) parameterValue;

                                if (ownerType != null) {
                                    parameterValue = entityManager.find(mappedProperty.getJavaClass(), ownerType.getId());
                                }
                            }

                            predicates.add(qb.equal(propertyEntityJoin.get(mappedProperty.getName()), parameterValue));
                        }
                    } else {
                        String[] valuesToSearch = new String[parameterValues.length];

                        for (int i = 0; i < parameterValues.length; i++) {
                            valuesToSearch[i] = Base64.encodeObject((Serializable) parameterValues[i]);
                        }

                        EntityMapper attributeMapper = getRootMapper(Attribute.class);

                        Class<?> attributeEntityClass = attributeMapper.getEntityType();
                        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();
                        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
                        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

                        Subquery<?> subquery = cq.subquery(attributeEntityClass);
                        Root fromProject = subquery.from(attributeEntityClass);
                        subquery.select(fromProject.get(ownerProperty.getName()));

                        Predicate conjunction = qb.conjunction();

                        conjunction.getExpressions().add(
                                qb.equal(
                                        fromProject.get(attributeNameProperty.getName()),
                                        attributeParameter.getName()));
                        conjunction.getExpressions().add(
                                (fromProject.get(attributeValueProperty.getName())
                                        .in((Object[]) valuesToSearch)));

                        subquery.where(conjunction);

                        subquery.groupBy(subquery.getSelection()).having(
                                qb.equal(qb.count(subquery.getSelection()), valuesToSearch.length));

                        predicates.add(qb.in(from).value(subquery));
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
        List<V> queryResult = new ArrayList<V>();

        EntityManager em = getEntityManager(context);

        List result = new ArrayList();

        Object[] identityParameterValues = query.getParameter(Relationship.IDENTITY);

        if (identityParameterValues != null) {
            for (Object parameterValue : identityParameterValues) {
                String identityId = null;

                if (String.class.isInstance(parameterValue)) {
                    identityId = (String) parameterValue;
                } else if (IdentityType.class.isInstance(parameterValue)) {
                    IdentityType identityType = (IdentityType) parameterValue;
                    identityId = identityType.getId();
                } else {
                    throw MESSAGES.queryUnsupportedParameterValue("Relationship.IDENTITY", parameterValue);
                }

                result = findIdentityTypeRelationships(context, identityId);
            }
        } else {
            EntityMapper entityMapper = getRootMapper(query.getRelationshipClass());
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipMapping().getEntityClass());
            Root<?> root = criteria.from(getConfig().getRelationshipMapping().getEntityClass());

            List<Predicate> predicates = new ArrayList<Predicate>();

            if (!Relationship.class.equals(query.getRelationshipClass())) {
                predicates.add(builder.equal(root.get(getConfig().getRelationshipMapping().getRelationshipClass().getName()),
                        query.getRelationshipClass().getName()));
            }

            Object[] idParameter = query.getParameter(Relationship.ID);

            if (idParameter != null && idParameter.length > 0) {
                predicates.add(builder.equal(root.get(entityMapper.getIdProperty().getName()),
                        idParameter[0]));
            } else {
                Property<Object> identityProperty = getConfig().getRelationshipIdentityMapping().getRelationshipMember();
                Property<String> descriptorProperty = getConfig().getRelationshipIdentityMapping().getRelationshipDescriptor();
                Property<Object> relationshipProperty = getConfig().getRelationshipIdentityMapping().getRelationshipOwner();

                for (Entry<QueryParameter, Object[]> entry : query.getParameters().entrySet()) {
                    QueryParameter queryParameter = entry.getKey();
                    Object[] values = entry.getValue();

                    if (queryParameter instanceof RelationshipQueryParameter) {
                        RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) entry.getKey();
                        List<String> identityTypeIdentifiers = new ArrayList<String>();

                        for (Object object : values) {
                            IdentityType identityType = (IdentityType) object;

                            if (identityType == null) {
                                return queryResult;
                            }

                            identityTypeIdentifiers.add(identityType.getId());
                        }

                        Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipIdentityMapping().getEntityClass());
                        Root fromRelationshipIdentityType = subquery.from(getConfig().getRelationshipIdentityMapping().getEntityClass());
                        subquery.select(fromRelationshipIdentityType.get(relationshipProperty.getName()));

                        Predicate conjunction = builder.conjunction();

                        conjunction.getExpressions().add(
                                builder.equal(fromRelationshipIdentityType.get(descriptorProperty.getName()),
                                        identityTypeParameter.getName()));

                        if (identityProperty.getJavaClass().equals(String.class)) {
                            conjunction.getExpressions().add(fromRelationshipIdentityType.get(identityProperty.getName()).in(identityTypeIdentifiers));
                        } else {
                            Join join = fromRelationshipIdentityType.join(identityProperty.getName());

                            List<Object> entities = new ArrayList<Object>();

                            for (String id : identityTypeIdentifiers) {
                                entities.add(em.find(identityProperty.getJavaClass(), id));
                            }

                            conjunction.getExpressions().add(join.in(entities));
                        }

                        subquery.where(conjunction);

                        predicates.add(builder.in(root).value(subquery));
                    }

                    if (AttributeParameter.class.equals(entry.getKey().getClass())) {
                        AttributeParameter customParameter = (AttributeParameter) entry.getKey();
                        Object[] attributeValues = entry.getValue();

                        String[] valuesToSearch = new String[attributeValues.length];

                        for (int i = 0; i < attributeValues.length; i++) {
                            valuesToSearch[i] = Base64.encodeObject((Serializable) attributeValues[i]);
                        }

                        EntityMapper attributeMapper = getRootMapper(Attribute.class);

                        Class<?> attributeEntityClass = attributeMapper.getEntityType();
                        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();
                        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
                        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

                        Subquery<?> subquery = criteria.subquery(attributeEntityClass);
                        Root fromProject = subquery.from(attributeEntityClass);
                        subquery.select(fromProject.get(ownerProperty.getName()));

                        Predicate conjunction = builder.conjunction();

                        conjunction.getExpressions().add(
                                builder.equal(
                                        fromProject.get(attributeNameProperty.getName()),
                                        customParameter.getName()));
                        conjunction.getExpressions().add(
                                (fromProject.get(attributeValueProperty.getName())
                                        .in((Object[]) valuesToSearch)));

                        subquery.where(conjunction);

                        subquery.groupBy(subquery.getSelection()).having(
                                builder.equal(builder.count(subquery.getSelection()), valuesToSearch.length));

                        predicates.add(builder.in(root).value(subquery));
                    }
                }
            }

            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            result = em.createQuery(criteria).getResultList();
        }

        for (Object relationshipObject : result) {
            queryResult.add((V) convertToRelationshipType(context, relationshipObject));
        }

        return queryResult;
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType attributedType, Attribute<? extends Serializable> attribute) {
        removeAttribute(context, attributedType, attribute.getName());
        setAttribute(attributedType, attribute, getEntityManager(context));
    }

    public void setAttribute(AttributedType attributedType, Attribute<? extends Serializable> attribute, EntityManager entityManager) {
        Serializable values = attribute.getValue();

        if (!values.getClass().isArray()) {
            values = new Serializable[]{values};
        }

        if (values instanceof byte[]) {
            values = new Serializable[]{values};
        }

        Object ownerEntity = getAttributedTypeEntity(attributedType, entityManager);

        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();
        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        for (Serializable attributeValue : (Serializable[]) values) {
            Object attributeEntity = null;

            try {
                attributeEntity = attributeMapper.getEntityType().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(attributeMapper.getEntityType(), e);
            }

            attributeNameProperty.setValue(attributeEntity, attribute.getName());
            attributeValueProperty.setValue(attributeEntity, Base64.encodeObject(attributeValue));
            ownerProperty.setValue(attributeEntity, ownerEntity);

            entityManager.persist(attributeEntity);
        }
    }

    public void populateAttributes(AttributedType attributedType, EntityManager entityManager) {
        populateAttribute(attributedType, entityManager, null);
    }

    private EntityManager getEntityManager(IdentityContext context) {
        if (!context.isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    public List<EntityMapper> getMapperFor(Class<?> attributedType) {
        List<EntityMapper> mappers = new ArrayList<EntityMapper>();

        EntityMapper rootEntityMapper = null;

        for (EntityMapper entityMapper : this.entityMappers) {
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
                    if (entityMapping.getSupportedType().isAssignableFrom(attributedType)) {
                        if (!mappers.contains(entityMapper)) {
                            mappers.add(entityMapper);
                        }

                        if (entityMapper.isRoot()) {
                            rootEntityMapper = entityMapper;
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

    private EntityMapper getEntityMapperForProperty(Class<? extends AttributedType> attributedType, String propertyName) {
        for (EntityMapper entityMapper : getMapperFor(attributedType)) {
            Entry<Property, Property> property = entityMapper.getProperty(attributedType, propertyName);

            if (property != null) {
                return entityMapper;
            }
        }

        return null;
    }

    private List<?> findIdentityTypeRelationships(IdentityContext context, String identityTypeId) {
        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityMapping().getEntityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityMapping().getEntityClass());

        Property<Object> identityTypeProperty = getConfig().getRelationshipIdentityMapping().getRelationshipMember();

        if (identityTypeProperty.getJavaClass().equals(String.class)) {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()), identityTypeId));
        } else {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()), em.find(identityTypeProperty.getJavaClass(), identityTypeId)));
        }

        List<Object> relationships = new ArrayList<Object>();

        List<?> result = em.createQuery(criteria).getResultList();

        for (Object object : result) {
            relationships.add(getConfig().getRelationshipIdentityMapping().getRelationshipOwner().getValue(object));
        }

        return relationships;

    }

    private <T extends Relationship> T convertToRelationshipType(IdentityContext context, Object relationshipObject) {
        Property<Object> identityProperty = getConfig().getRelationshipIdentityMapping().getRelationshipMember();
        Property<String> descriptorProperty = getConfig().getRelationshipIdentityMapping().getRelationshipDescriptor();
        EntityManager entityManager = getEntityManager(context);

        EntityMapper relMapper = getRootMapper(Relationship.class);

        T relationshipType = relMapper.createType(relationshipObject, entityManager);

        List<?> identities = findChildRelationships(context, relationshipType);

        for (Object object : identities) {
            String descriptor = descriptorProperty.getValue(object).toString();

            List<Property<Object>> identityTypeProperty = PropertyQueries.createQuery(relationshipType.getClass())
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getResultList();

            Object identityTypeEntity = null;
            IdentityType identityType = null;

            if (identityProperty.getJavaClass().equals(String.class)) {
                identityTypeEntity = entityManager.find(identityProperty.getJavaClass(), identityProperty.getValue(object).toString());
            } else {
                identityTypeEntity = identityProperty.getValue(object);
            }

            EntityMapper entityMapper = getRootMapperForEntity(identityTypeEntity.getClass());

            identityType = entityMapper.createType(identityTypeEntity, entityManager);

            identityTypeProperty.get(0).setValue(relationshipType, identityType);
        }

        return relationshipType;
    }

    private List<?> findChildRelationships(IdentityContext context, Relationship relationship) {
        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityMapping().getEntityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityMapping().getEntityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        Join<?, ?> join = root.join(getConfig().getRelationshipIdentityMapping().getRelationshipOwner().getName());

        predicates.add(builder.equal(join.get(getConfig().getRelationshipMapping().getRelationshipIdentifier().getName()),
                relationship.getId()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private void removeRelationships(IdentityContext context, AttributedType attributedType) {
        // First we build a list of all the relationships that the specified identity
        // is participating in
        if (getConfig().getRelationshipMapping().getRelationshipClass() != null) {
            List<?> relationshipsToRemove = findIdentityTypeRelationships(context, attributedType.getId());

            // Now that we have the list, we can iterate through and remove the records
            for (Object relationship : relationshipsToRemove) {
                remove(context, convertToRelationshipType(context, relationship));
            }
        }
    }

    private void removeAttributes(IdentityContext context, AttributedType attributedType) {
        getAttribute(context, attributedType, null);

        for (Attribute attribute : attributedType.getAttributes()) {
            removeAttribute(context, attributedType, attribute.getName());
        }
    }

    private EntityMapper getRootMapper(Class<?> aClass) {
        return getMapperFor(aClass).get(0);
    }

    public EntityMapper getRootMapperForEntity(Class<?> entityClass) {
        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.isRoot() && entityMapper.getEntityType().equals(entityClass)) {
                return entityMapper;
            }
        }

        throw new IdentityManagementException("No mapper for entity type [" + entityClass + "].");
    }

    public List<EntityMapper> getEntityMappers() {
        return this.entityMappers;
    }

    private void populateAttribute(AttributedType attributedType, EntityManager entityManager, String attributeName) {
        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());

        Class<?> attributeEntityClass = attributeMapper.getEntityType();

        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = qb.createQuery(attributeEntityClass);
        Root<?> from = cq.from(attributeEntityClass);
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();

        if (attributeName != null) {
            predicates.add(qb.equal(from.get(attributeNameProperty.getName()),
                    attributeName));
        }

        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        predicates.add(qb.equal(from.get(ownerProperty.getName()),
                getAttributedTypeEntity(attributedType, entityManager)));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();

        for (Object attributeEntity : entityManager.createQuery(cq).getResultList()) {
            String storedName = attributeNameProperty.getValue(attributeEntity).toString();
            Serializable storedValue = (Serializable) Base64.decodeToObject(attributeValueProperty.getValue(attributeEntity).toString());

            Attribute<Serializable> attribute = attributedType.getAttribute(storedName);

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

            attributedType.setAttribute(attribute);
        }
    }

    private void removeAttribute(AttributedType type, String attributeName, EntityManager entityManager) {
        EntityMapper entityMapper = getAttributeMapper(type.getClass());

        Entry<Property, Property> attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class);
        Entry<Property, Property> ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class);

        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = qb.createQuery(entityMapper.getEntityType());
        Root<?> from = cq.from(entityMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        EntityMapper rootMapper = getRootMapper(type.getClass());

        if (attributeName != null) {
            predicates.add(qb.equal(from.get(attributeNameProperty.getValue().getName()), attributeName));
        }

        predicates.add(qb.equal(from.get(ownerProperty.getValue().getName()),
                entityManager.find(rootMapper.getEntityType(), type.getId())));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        for (Object entity : entityManager.createQuery(cq).getResultList()) {
            entityManager.remove(entity);
        }
    }

    public void removeAllAttributes(AttributedType attributedType, EntityManager entityManager) {
        removeAttribute(attributedType, null, entityManager);
    }

    private void storeRelationshipMembers(Relationship relationship, EntityManager entityManager) {
        Object ownerEntity = getAttributedTypeEntity(relationship, entityManager);

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE)).getResultList();

        for (Property<IdentityType> prop : props) {
            Object relationshipIdentity = null;

            try {
                relationshipIdentity = getConfig().getRelationshipIdentityMapping().getEntityClass().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(getConfig().getRelationshipIdentityMapping().getEntityClass(), e);
            }

            IdentityType identityType = prop.getValue(relationship);

            if (identityType != null) {
                Property<Object> identityTypeProperty = getConfig().getRelationshipIdentityMapping().getRelationshipMember();

                if (identityTypeProperty.getJavaClass().equals(String.class)) {
                    identityTypeProperty.setValue(relationshipIdentity,
                            identityType.getId());
                } else {
                    identityTypeProperty.setValue(relationshipIdentity, getAttributedTypeEntity(identityType, entityManager));
                }

                getConfig().getRelationshipIdentityMapping().getRelationshipDescriptor().setValue(relationshipIdentity,
                        prop.getName());
                getConfig().getRelationshipIdentityMapping().getRelationshipOwner().setValue(relationshipIdentity,
                        ownerEntity);
            }

            entityManager.persist(relationshipIdentity);
        }
    }

    public Object getAttributedTypeEntity(AttributedType attributedType, EntityManager entityManager) {
        return entityManager.find(getRootMapper(attributedType.getClass()).getEntityType(), attributedType.getId());
    }

    private EntityMapper getAttributeMapper(Class<? extends AttributedType> attributedType) {
        Class<?> attributedTypeEntityType = getRootMapper(attributedType).getEntityType();

        for (EntityMapper entityMapper : getMapperFor(Attribute.class)) {
            if (entityMapper.getMappingsFor(Attribute.class).getOwnerType().isAssignableFrom(attributedTypeEntityType)) {
                return entityMapper;
            }
        }

        throw new IdentityManagementException("Could not find mapper for attributes for type [" + attributedType + "].");
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

        Object ownerEntity = getAttributedTypeEntity(account, entityManager);

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
        return convertToCredentialStorage(context, retrieveLastCredentialEntity(context, account, storageClass), storageClass);
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

        Object agentInstance = getAttributedTypeEntity(account, entityManager);

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        List<T> storages = new ArrayList<T>();

        for (Object object : entityManager.createQuery(criteria).getResultList()) {
            storages.add(convertToCredentialStorage(context, object, storageClass));
        }

        return storages;
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    public boolean isMappedType(Class mappedClass) {
        for (EntityMapper entityMapper : getEntityMappers()) {
            if (entityMapper.getEntityType().equals(mappedClass)) {
                return true;
            }
        }

        return false;
    }

    private <T extends CredentialStorage> T convertToCredentialStorage(IdentityContext context, Object entity,
                                                                       Class<T> storageType) {
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
        for (EntityMapper entityMapper : getMapperFor(credentialStorageClass)) {
            ManagedCredential managedCredential = entityMapper.getEntityType().getAnnotation(ManagedCredential.class);

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

        throw new IdentityManagementException("No mapper for for credential storage type [" + credentialStorageClass + "].");
    }

    private Object retrieveLastCredentialEntity(IdentityContext context, Account account, Class<? extends CredentialStorage> storageClass) {
        EntityMapper attributeMapper = getCredentialAttributeMapper(storageClass);

        Property identityTypeProperty = attributeMapper.getProperty(storageClass, OwnerReference.class).getValue();
        Property typeProperty = attributeMapper.getProperty(storageClass, CredentialClass.class).getValue();
        Property effectiveProperty = attributeMapper.getProperty(storageClass, EffectiveDate.class).getValue();

        EntityManager entityManager = getEntityManager(context);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
        Root<?> root = criteria.from(attributeMapper.getEntityType());

        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = getAttributedTypeEntity(account, entityManager);

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date>get(effectiveProperty.getName()), new Date()));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        Object lastCredential = null;

        List<?> result = entityManager.createQuery(criteria).getResultList();

        if (!result.isEmpty()) {
            lastCredential = result.get(0);
        }

        return lastCredential;
    }

    private List<Object> findCredentials(IdentityContext context, AttributedType attributedType) {
        List storages = new ArrayList();
        Class<CredentialStorage> storageClass = CredentialStorage.class;

        for (EntityMapper attributeMapper : getEntityMappers()) {
            if (attributeMapper.getEntityType().isAnnotationPresent(ManagedCredential.class)) {

                Property identityTypeProperty = attributeMapper.getProperty(storageClass, OwnerReference.class).getValue();
                Property effectiveProperty = attributeMapper.getProperty(storageClass, EffectiveDate.class).getValue();

                EntityManager entityManager = getEntityManager(context);

                CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
                Root<?> root = criteria.from(attributeMapper.getEntityType());
                List<Predicate> predicates = new ArrayList<Predicate>();

                Object agentInstance = getAttributedTypeEntity(attributedType, entityManager);

                predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));

                if (!CredentialStorage.class.equals(storageClass)) {
                    Property typeProperty = attributeMapper.getProperty(storageClass, AttributeClass.class).getValue();
                    predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));
                }

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

                List result = entityManager.createQuery(criteria).getResultList();

                for (Object storageEntity : result) {
                    storages.add(storageEntity);
                }
            }
        }

        return storages;
    }

    private void removeCredentials(IdentityContext context, AttributedType attributedType, EntityManager entityManager) {
        for (Object credentialEntity : findCredentials(context, attributedType)) {
            entityManager.remove(credentialEntity);
        }
    }
}