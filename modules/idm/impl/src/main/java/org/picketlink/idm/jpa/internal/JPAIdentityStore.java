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
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.credential.handler.DigestCredentialHandler;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.credential.handler.TOTPCredentialHandler;
import org.picketlink.idm.credential.handler.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.internal.RelationshipReference;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.CredentialClass;
import org.picketlink.idm.jpa.annotations.EffectiveDate;
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
import org.picketlink.idm.jpa.annotations.entity.PermissionManaged;
import org.picketlink.idm.jpa.internal.mappers.EntityMapper;
import org.picketlink.idm.jpa.internal.mappers.EntityMapping;
import org.picketlink.idm.jpa.internal.mappers.PermissionEntityMapper;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.permission.Permission;
import org.picketlink.idm.permission.acl.spi.PermissionStore;
import org.picketlink.idm.permission.annotations.AllowedOperation;
import org.picketlink.idm.permission.annotations.AllowedOperations;
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
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.picketlink.common.reflection.Reflections.newInstance;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMInternalLog.JPA_STORE_LOGGER;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * Implementation of IdentityStore that stores its state in a relational database.
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
        AttributeStore<JPAIdentityStoreConfiguration>, PermissionStore {

    // Invocation context parameters
    public static final String INVOCATION_CTX_ENTITY_MANAGER = "CTX_ENTITY_MANAGER";

    // Event context parameters
    public static final String EVENT_CONTEXT_IDENTITY = "IDENTITY_ENTITY";

    private final List<EntityMapper> entityMappers = new ArrayList<EntityMapper>();

    private List<PermissionEntityMapper> permissionMappers = new ArrayList<PermissionEntityMapper>();

    @Override
    public void setup(JPAIdentityStoreConfiguration config) {
        super.setup(config);

        if (config.getContextInitializers().isEmpty()) {
            JPA_STORE_LOGGER.jpaContextInitializerNotProvided();
        }

        for (Class<?> entityType : config.getEntityTypes()) {
            if (entityType.isAnnotationPresent(PermissionManaged.class)) {
                permissionMappers.add(new PermissionEntityMapper(entityType));
            } else {
                configureEntityMapper(entityType);
            }
        }

        logEntityMappers();
    }

    @Override
    public void addAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            if (entityMapper.isPersist()) {
                entityMapper.persist(attributedType, entityManager);
            }

            if (Relationship.class.isInstance(attributedType)) {
                if (entityMapper.isRoot()) {
                    storeRelationshipMembers((Relationship) attributedType, entityManager);
                }
            }
        }
    }

    @Override
    public void updateAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            entityMapper.updateEntity(attributedType, entityManager);
        }
    }

    @Override
    public void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper rootMapper = getRootMapper(attributedType.getClass());

        if (Relationship.class.isAssignableFrom(attributedType.getClass())) {
            removeChildRelationships(context, (Relationship) attributedType, entityManager);
        }

        removeAssociatedEntities(attributedType, entityManager, rootMapper);

        entityManager.remove(getRootEntity(attributedType, entityManager));
    }

    @Override
    protected void removeFromRelationships(IdentityContext context, IdentityType identityType) {
        // First we build a list of all the relationships that the specified identity
        // is participating in
        List<?> relationshipsToRemove = findIdentityTypeRelationships(context, identityType);

        // Now that we have the list, we can iterate through and remove the records
        for (Object relationship : relationshipsToRemove) {
            remove(context, convertToRelationshipType(context, relationship));
        }
    }

    @Override
    protected void removeCredentials(IdentityContext context, Account account) {
        EntityManager entityManager = getEntityManager(context);
        List entities = new ArrayList();

        for (EntityMapper attributeMapper : getEntityMappers()) {
            if (attributeMapper.getEntityType().isAnnotationPresent(ManagedCredential.class)) {
                CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
                Root<?> root = criteria.from(attributeMapper.getEntityType());

                Object agentInstance = getRootEntity(account, entityManager);

                Property identityTypeProperty = attributeMapper.getProperty(OwnerReference.class).getValue();

                criteria.where(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));

                Property effectiveProperty = attributeMapper.getProperty(EffectiveDate.class).getValue();

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

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        add(identityContext, partition);

        // now that the partition entity is created, let`s populate the configuration name.
        // the configuration name is not part of the Model API, so we need to do this manually.
        EntityMapper entityMapper = getRootMapper(partition.getClass());
        EntityManager entityManager = getEntityManager(identityContext);
        Object partitionEntity = getRootEntity(partition, entityManager);
        Property configurationNameProperty = entityMapper.getProperty(partition.getClass(), ConfigurationName.class).getValue();

        configurationNameProperty.setValue(partitionEntity, configurationName);

        entityManager.merge(partitionEntity);
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        EntityMapper entityMapper = getRootMapper(partition.getClass());
        EntityManager entityManager = getEntityManager(identityContext);
        Object partitionEntity = entityManager.find(entityMapper.getEntityType(), partition.getId());
        Property configurationNameProperty = entityMapper.getProperty(partition.getClass(), ConfigurationName.class).getValue();

        String configurationName = configurationNameProperty.getValue(partitionEntity).toString();

        if (isNullOrEmpty(configurationName)) {
            throw MESSAGES.partitionWithNoConfigurationName(partition);
        }

        return configurationName;
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        List<P> result = getPartitions(identityContext, partitionClass, name);

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw MESSAGES.partitionFoundWithSameNameAndType(name, partitionClass);
            }

            return result.get(0);
        }

        return null;
    }

    @Override
    public <P extends Partition> List<P> get(IdentityContext identityContext, Class<P> partitionClass) {
        return getPartitions(identityContext, partitionClass, null);
    }

    public <P extends Partition> List<P> getPartitions(IdentityContext identityContext, Class<P> partitionClass, String name) {
        EntityManager entityManager = getEntityManager(identityContext);
        String PARTITION_NAME_PROPERTY_NAME = "name";
        EntityMapper entityMapper = getEntityMapperForProperty(partitionClass, PARTITION_NAME_PROPERTY_NAME);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(entityMapper.getEntityType());
        Root from = cq.from(entityMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (!isNullOrEmpty(name)) {
            Property nameProperty = entityMapper.getProperty(partitionClass, PARTITION_NAME_PROPERTY_NAME).getValue();
            predicates.add(cb.equal(from.get(nameProperty.getName()), name));
        }

        if (!Partition.class.equals(partitionClass)) {
            Property typeProperty = entityMapper.getProperty(partitionClass, PartitionClass.class).getValue();
            predicates.add(cb.equal(from.get(typeProperty.getName()), partitionClass.getName()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query query = entityManager.createQuery(cq);

        List<P> result = new ArrayList<P>();

        for (Object entity : query.getResultList()) {
            result.add(entityMapper.<P>createType(entity, entityManager));
        }

        return result;
    }

    @Override
    public <P extends Partition> P lookupById(final IdentityContext context, final Class<P> partitionClass,
                                              final String id) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper entityMapper = getRootMapper(Partition.class);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(entityMapper.getEntityType());
        Root from = cq.from(entityMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property idProperty = entityMapper.getProperty(Partition.class, Identifier.class).getValue();

        predicates.add(cb.equal(from.get(idProperty.getName()), id));

        if (!Partition.class.equals(partitionClass)) {
            Property typeProperty = entityMapper.getProperty(partitionClass, PartitionClass.class).getValue();
            predicates.add(cb.equal(from.get(typeProperty.getName()), partitionClass.getName()));
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
        EntityMapper attributeMapper = getAttributeMapper(attributedType.getClass());
        EntityManager entityManager = getEntityManager(context);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = cb.createQuery(attributeMapper.getEntityType());
        Root<?> from = cq.from(attributeMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();

        predicates.add(cb.equal(from.get(attributeNameProperty.getName()), attributeName));

        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();

        if (getConfig().supportsType(attributedType.getClass(), IdentityOperation.create)
                && !String.class.equals(ownerProperty.getJavaClass())) {
            predicates.add(cb.equal(from.get(ownerProperty.getName()), getOwnerEntity(attributedType, ownerProperty, entityManager)));
        } else {
            predicates.add(cb.equal(from.get(ownerProperty.getName()), attributedType.getId()));
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        for (Object entity : entityManager.createQuery(cq).getResultList()) {
            entityManager.remove(entity);
        }
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        List<V> result = new ArrayList<V>();
        Class<V> type = identityQuery.getIdentityType();

        if (identityQuery.getParameter(IdentityType.ID) != null) {
            Object[] parameter = identityQuery.getParameter(IdentityType.ID);

            if (parameter.length > 0) {
                V identityType = (V) lookupIdentityTypeById(context, type, parameter[0].toString());

                if (identityType != null) {
                    result.add(identityType);
                }
            }

            return result;
        }

        EntityMapper rootMapper = getRootMapper(type);
        EntityManager entityManager = getEntityManager(context);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(rootMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();
        Root<?> rootEntity = cq.from(rootMapper.getEntityType());
        Partition partition = context.getPartition();

        if (identityQuery.getParameter(IdentityType.PARTITION) != null) {
            partition = (Partition) identityQuery.getParameter(IdentityType.PARTITION)[0];
        }

        Entry<Property, Property> partitionProperty = rootMapper.getProperty(OwnerReference.class);

        if (partitionProperty != null) {
            Join<Object, Object> join = rootEntity.join(partitionProperty.getValue().getName());
            predicates.add(cb.equal(join, entityManager.find(partitionProperty.getValue().getJavaClass(), partition.getId())));
        }

        if (!IdentityType.class.equals(type)) {
            Property typeProperty = rootMapper.getProperty(type, IdentityClass.class).getValue();
            predicates.add(cb.equal(rootEntity.get(typeProperty.getName()), type.getName()));
        }

        for (QueryParameter queryParameter : identityQuery.getParameters().keySet()) {
            if (IdentityType.PARTITION.equals(queryParameter)) {
                continue;
            }

            if (AttributeParameter.class.isInstance(queryParameter)) {
                AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                Object[] parameterValues = identityQuery.getParameter(attributeParameter);
                EntityMapper parameterEntityMapper =
                        getEntityMapperForProperty(type, attributeParameter.getName());

                if (parameterEntityMapper != null) {
                    Property attributeProperty = (Property) parameterEntityMapper.getProperty(type, attributeParameter.getName()).getValue();
                    Root<?> attributeOwnerEntity = rootEntity;

                    if (!parameterEntityMapper.getEntityType().equals(rootMapper.getEntityType())) {
                        attributeOwnerEntity = cq.from(parameterEntityMapper.getEntityType());

                        Property ownerProperty = parameterEntityMapper.getProperty(OwnerReference.class).getValue();

                        if (ownerProperty != null) {
                            if (ownerProperty.getAnnotatedElement().isAnnotationPresent(Id.class)) {
                                predicates.add(cb.and(cb.equal(attributeOwnerEntity, rootEntity)));
                            } else {
                                predicates.add(cb.and(cb.equal(attributeOwnerEntity.get(ownerProperty.getName()), rootEntity)));
                            }
                        }
                    }

                    Object parameterValue = parameterValues[0];

                    if (IdentityType.CREATED_AFTER.equals(queryParameter) || IdentityType.EXPIRY_AFTER.equals(queryParameter)) {
                        predicates.add(cb
                                       .greaterThanOrEqualTo(attributeOwnerEntity.<Date>get(attributeProperty.getName()), (Date) parameterValue));
                    } else if (IdentityType.CREATED_BEFORE.equals(queryParameter) || IdentityType.EXPIRY_BEFORE.equals(queryParameter)) {
                        predicates.add(cb.lessThanOrEqualTo(attributeOwnerEntity.<Date>get(attributeProperty.getName()), (Date) parameterValue));
                    } else {
                        if (isMappedType(attributeProperty.getJavaClass())) {
                            AttributedType ownerType = (AttributedType) parameterValue;

                            if (ownerType != null) {
                                parameterValue = entityManager.find(attributeProperty.getJavaClass(), ownerType.getId());
                            }
                        }

                        predicates.add(cb.equal(attributeOwnerEntity.get(attributeProperty.getName()), parameterValue));
                    }
                } else {
                    addAttributeQueryPredicates(type, cb, cq, rootEntity, predicates, attributeParameter, parameterValues);
                }
            }
        }

        Property idProperty = rootMapper.getProperty(Id.class).getValue();

        cq.select(rootEntity.get(idProperty.getName()));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query query = entityManager.createQuery(cq);

        if (identityQuery.getLimit() > 0) {
            query.setMaxResults(identityQuery.getLimit());

            if (identityQuery.getOffset() > 0) {
                query.setFirstResult(identityQuery.getOffset());
            }
        }

        for (Object entity : query.getResultList()) {
            result.add(rootMapper.<V>createType(entityManager.find(rootMapper.getEntityType(), entity), entityManager));
        }

        return result;
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext
                                                                      context, RelationshipQuery<V> query) {
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
            Root root = cq.from(entityMapper.getEntityType());
            List<Predicate> predicates = new ArrayList<Predicate>();

            Property typeProperty = entityMapper.getProperty(RelationshipClass.class).getValue();

            if (!Relationship.class.equals(query.getRelationshipClass())) {
                predicates.add(cb.equal(root.get(typeProperty.getName()), query.getRelationshipClass().getName()));
            }

            Object[] idParameterValues = query.getParameter(Relationship.ID);
            Property idProperty = entityMapper.getProperty(Identifier.class).getValue();

            if (idParameterValues != null && idParameterValues.length > 0) {
                predicates.add(cb.equal(root.get(idProperty.getName()), idParameterValues[0]));
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

                            EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);
                            Property<Object> identityTypeProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

                            if (identityTypeProperty.getJavaClass().equals(String.class)) {
                                identityTypeIdentifiers.add(RelationshipReference.formatId(identityType));
                            } else {
                                identityTypeIdentifiers.add(identityType.getId());
                            }
                        }

                        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);
                        Property<Object> relationshipProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

                        Subquery<?> subQuery = cq.subquery(relationshipMemberMapper.getEntityType());
                        Root fromRelationshipIdentityType = subQuery.from(relationshipMemberMapper.getEntityType());

                        subQuery.select(fromRelationshipIdentityType.get(relationshipProperty.getName()).get(idProperty.getName()));

                        List<Predicate> subQueryPredicates = new ArrayList<Predicate>();

                        Property<String> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor.class).getValue();

                        subQueryPredicates.add(
                                cb.equal(fromRelationshipIdentityType.get(descriptorProperty.getName()),
                                        identityTypeParameter.getName()));

                        Property<Object> identityProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

                        if (identityProperty.getJavaClass().equals(String.class)) {
                            subQueryPredicates.add(fromRelationshipIdentityType.get(identityProperty.getName()).in(identityTypeIdentifiers));
                        } else {
                            Join join = fromRelationshipIdentityType.join(identityProperty.getName());
                            EntityMapper identityTypeMapper = getMapperForEntity(identityProperty.getJavaClass());
                            Property identifierProperty = identityTypeMapper.getProperty(Identifier.class).getValue();

                            subQueryPredicates.add(join.get(identifierProperty.getName()).in(identityTypeIdentifiers));
                        }

                        subQuery.where(subQueryPredicates.toArray(new Predicate[subQueryPredicates.size()]));

                        predicates.add(cb.in(root.get(idProperty.getName())).value(subQuery));
                    } else if (AttributeParameter.class.equals(entry.getKey().getClass())) {
                        AttributeParameter attributeParameter = (AttributeParameter) entry.getKey();
                        Object[] parameterValues = entry.getValue();
                        EntityMapper parameterEntityMapper =
                                getEntityMapperForProperty(query.getRelationshipClass(), attributeParameter.getName());

                        if (parameterEntityMapper != null) {
                            Root<?> propertyEntityJoin = root;

                            Property ownerProperty = parameterEntityMapper.getProperty(query
                                    .getRelationshipClass(), OwnerReference.class).getValue();

                            if (ownerProperty.getJavaClass().equals(entityMapper.getEntityType())) {
                                propertyEntityJoin = cq.from(parameterEntityMapper.getEntityType());
                                predicates.add(cb.and(cb.equal(propertyEntityJoin.get(ownerProperty.getName()), root)));
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
                            addAttributeQueryPredicates(query.getRelationshipClass(), cb, cq, root, predicates,
                                    attributeParameter,
                                    parameterValues);
                        }
                    }
                }
            }

            cq.select(root);

            cq.where(predicates.toArray(new Predicate[predicates.size()]));

            entities = entityManager.createQuery(cq).getResultList();
        }

        List<V> result = new ArrayList<V>();

        for (Object relationshipObject : entities) {
            result.add(this.<V>convertToRelationshipType(context, relationshipObject));
        }

        return result;
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType attributedType, Attribute<? extends
            Serializable> attribute) {
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
            Object attributeEntity = attributeMapper.createEntity();

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
        EntityMapper credentialMapper = getCredentialAttributeMapper(storage.getClass());
        Object newCredential = credentialMapper.createEntity();
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapping entityMapping : credentialMapper.getEntityMappings()) {
            for (Property property : entityMapping.getProperties().keySet()) {
                Property mappedProperty = entityMapping.getProperties().get(property);

                if (mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                    mappedProperty.setValue(newCredential, getOwnerEntity(account, mappedProperty, entityManager));
                } else {
                    mappedProperty.setValue(newCredential, property.getValue(storage));
                }
            }
        }

        entityManager.persist(newCredential);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account
            account, Class<T> storageClass) {
        List<T> credentials = retrieveCredentials(context, account, storageClass);

        if (!credentials.isEmpty()) {
            return credentials.get(0);
        }

        return null;
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account
            account, Class<T> storageClass) {
        EntityMapper attributeMapper = getCredentialAttributeMapper(storageClass);
        EntityManager entityManager = getEntityManager(context);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeMapper.getEntityType());
        Root<?> root = criteria.from(attributeMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = getRootEntity(account, entityManager);

        Property identityTypeProperty = attributeMapper.getProperty(storageClass, OwnerReference.class).getValue();

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));

        Property typeProperty = attributeMapper.getProperty(storageClass, CredentialClass.class).getValue();

        Property effectiveProperty = attributeMapper.getProperty(storageClass, EffectiveDate.class).getValue();

        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date>get(effectiveProperty.getName()), new Date()));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        List<T> storages = new ArrayList<T>();

        for (Object object : entityManager.createQuery(criteria).getResultList()) {
            storages.add(convertToCredentialStorage(object, storageClass));
        }

        return storages;
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

    /**
     * <p>Returns all {@link EntityMapper} instances used to map the given {@link AttributedType}. Only mappers for
     * {@link IdentityManaged} annotated entity classes are considered, what means that this method can only be
     * used
     * when
     * trying to persist or populate @{link AttributedType} instances.</p>
     *
     * @param attributedType
     *
     * @return
     */
    public List<EntityMapper> getMapperFor(Class<? extends AttributedType> attributedType) {
        List<EntityMapper> mappers = new ArrayList<EntityMapper>();

        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.getEntityType().isAnnotationPresent(IdentityManaged.class)) {
                for (EntityMapping entityMapping : entityMapper.getEntityMappings()) {
                    if ((entityMapping.getSupportedType().equals(attributedType) || entityMapping.getSupportedType().isAssignableFrom(attributedType))
                            && entityMapper.isRoot()) {
                        mappers.add(0, entityMapper);
                    } else if (entityMapping.getSupportedType().isAssignableFrom(attributedType)) {
                        mappers.add(entityMapper);
                    } else {
                        if (Partition.class.equals(attributedType)
                                || IdentityType.class.equals(attributedType) || Relationship.class.equals(attributedType)) {
                            if (attributedType.isAssignableFrom(entityMapping.getSupportedType())) {
                                mappers.add(entityMapper);
                            }
                        }
                    }
                }
            }
        }

        if (mappers.isEmpty()) {
            throw new IdentityManagementException("No entity mapper found for type [" + attributedType + "].");
        }

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

    private <V extends IdentityType> IdentityType lookupIdentityTypeById(IdentityContext context, Class<V> type, String identifier) {
        EntityManager entityManager = getEntityManager(context);

        if (IdentityType.class.equals(type)) {
            // when querying based on the IdentityType base type, we try to load the instance from all available mappers.
            for (EntityMapper entityMapper : getEntityMappers()) {
                if (entityMapper.getMappingsFor(type) != null && entityMapper.isRoot() && entityMapper.isPersist()) {
                    Object entity = entityManager.find(entityMapper.getEntityType(), identifier);
                    V identityType = entityMapper.<V>createType(entity, entityManager);

                    if (identityType != null) {
                        return identityType;
                    }
                }
            }
        } else {
            // we know the right type, we just lookup based on its root mapper
            Object entity = entityManager.find(getRootMapper(type).getEntityType(), identifier);

            if (entity != null) {
                return getRootMapperForEntity(entity.getClass()).<V>createType(entity, entityManager);
            }
        }

        return null;
    }

    private EntityMapper getEntityMapperForProperty(Class<? extends AttributedType> attributedType, String
            propertyName) {
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

    private <T extends Relationship> T convertToRelationshipType(IdentityContext context, Object
            relationshipObject) {
        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        Property<Object> identityProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();
        Property<String> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor.class).getValue();
        EntityManager entityManager = getEntityManager(context);

        EntityMapper relMapper = getRootMapper(Relationship.class);

        T relationshipType = relMapper.createType(relationshipObject, entityManager);
        boolean isReference = !identityProperty.getJavaClass().equals(String.class);

        RelationshipReference reference = null;

        if (!isReference) {
            reference = new RelationshipReference(relationshipType);
        }

        for (Object object : findChildRelationships(context, relationshipType)) {
            String descriptor = descriptorProperty.getValue(object).toString();

            Property<Object> identityTypeProperty = PropertyQueries.createQuery(relationshipType.getClass())
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getSingleResult();
            IdentityType identityType = null;

            Object identityTypeEntity = identityProperty.getValue(object);

            if (!isReference) {
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

    private <T extends CredentialStorage> T convertToCredentialStorage(Object entity, Class<T> storageType) {
        T storage = null;

        if (entity != null) {
            EntityMapper credentialMapper = getCredentialAttributeMapper(storageType);

            try {
                storage = newInstance(storageType);
            } catch (Exception e) {
                throw MESSAGES.instantiationError(storageType, e);
            }

            for (EntityMapping entityMapping : credentialMapper.getEntityMappings()) {
                for (Property property : entityMapping.getProperties().keySet()) {
                    Property mappedProperty = entityMapping.getProperties().get(property);

                    if (!mappedProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                        property.setValue(storage, mappedProperty.getValue(entity));
                    }
                }
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

    private void removeChildRelationships(final IdentityContext context, final Relationship attributedType,
                                          final EntityManager entityManager) {
        for (Object child : findChildRelationships(context, (Relationship) attributedType)) {
            entityManager.remove(child);
        }
    }

    private void removeAssociatedEntities(final AttributedType attributedType, final EntityManager entityManager,
                                          final EntityMapper rootMapper) {
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

    private Map<String, Attribute<Serializable>> getAttributes(final AttributedType attributedType,
                                                               final String attributeName, final EntityManager entityManager) {
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
        Subquery<?> subQueryOwnerAttributesByValue = cq.subquery(attributeMapper.getEntityType());
        Root fromAttributeType = subQueryOwnerAttributesByValue.from(attributeMapper.getEntityType());
        Property ownerProperty = attributeMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
        String ownerIdentifierPropertyName = getRootMapper(attributedType).getProperty(Identifier.class).getValue().getName();
        Path selection;

        if (String.class.equals(ownerProperty.getJavaClass())) {
            selection = fromAttributeType.get(ownerProperty.getName());
        } else {
            selection = fromAttributeType.get(ownerProperty.getName()).get(ownerIdentifierPropertyName);
        }

        subQueryOwnerAttributesByValue.select(selection);

        List<Predicate> conjunction = new ArrayList<Predicate>();

        Property attributeNameProperty = attributeMapper.getProperty(Attribute.class, AttributeName.class).getValue();

        conjunction.add(cb.equal(fromAttributeType.get(attributeNameProperty.getName()), attributeParameter.getName()));

        Property attributeValueProperty = attributeMapper.getProperty(Attribute.class, AttributeValue.class).getValue();

        conjunction.add(fromAttributeType.get(attributeValueProperty.getName()).in((Object[]) valuesToSearch));

        subQueryOwnerAttributesByValue.where(conjunction.toArray(new Predicate[conjunction.size()]));
        subQueryOwnerAttributesByValue.groupBy(selection).having(cb.equal(cb.count(selection), valuesToSearch.length));

        predicates.add(cb.in(from.get(ownerIdentifierPropertyName)).value(subQueryOwnerAttributesByValue));
    }

    private EntityMapper getAttributeMapper(Class<? extends AttributedType> attributedType) {
        List<EntityMapper> attributeMappers = new ArrayList<EntityMapper>();

        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.getMappingsFor(Attribute.class) != null) {
                attributeMappers.add(entityMapper);
            }
        }

        if (!attributeMappers.isEmpty()) {
            boolean supportsType = getConfig().supportsType(attributedType, IdentityOperation.create);

            // if the store supports the type, we try to find the most specific mapper for its corresponding entity.
            if (supportsType) {
                EntityMapper secondaryMapper = null;

                for (EntityMapper entityMapper : getMapperFor(attributedType)) {
                    for (EntityMapper mapper : attributeMappers) {
                        Class<?> entityType = entityMapper.getEntityType();
                        EntityMapping mappings = mapper.getMappingsFor(Attribute.class);

                        if (mappings.getOwnerType().equals(entityType)) {
                            return mapper;
                        } else if (mappings.getOwnerType().isAssignableFrom(entityType)) {
                            secondaryMapper = mapper;
                        }
                    }
                }

                if (secondaryMapper != null) {
                    return secondaryMapper;
                }
            }

            // as a fallback, we check if the attribute mappers support id-based references for the type. this is specially useful when using a single
            // attribute entity to store attributes for all types based on their ids.
            for (EntityMapper mapper : attributeMappers) {
                EntityMapping mappings = mapper.getMappingsFor(Attribute.class);

                    if (String.class.equals(mappings.getOwnerType())) {
                        return mapper;
                    }
            }

            // in this case, the store does not support the type. So the attribute mapping must provide a String-based field to store only references to the type based on the id.
            if (!supportsType) {
                throw new IdentityManagementException("The store does not support type [" + attributedType + "]. The attribute mapping must provide a String-based field to reference instances of this type.");
            }
        }

        throw new IdentityManagementException("Could not find attribute mapper for type [" + attributedType + "].");
    }

    private void storeRelationshipMembers(Relationship relationship, EntityManager entityManager) {
        Object ownerEntity = getRootEntity(relationship, entityManager);

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType>createQuery(relationship.getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE)).getResultList();

        EntityMapper relationshipMemberMapper = getEntityMapperForProperty(RelationshipMember.class);

        for (Property<IdentityType> prop : props) {
            Object relationshipIdentity = relationshipMemberMapper.createEntity();
            IdentityType identityType = prop.getValue(relationship);

            if (identityType != null) {
                Property<Object> identityTypeProperty = relationshipMemberMapper.getProperty(RelationshipMember.class).getValue();

                // in this case we hold only the reference to the identity type identifier
                if (identityTypeProperty.getJavaClass().equals(String.class)) {
                    identityTypeProperty.setValue(relationshipIdentity, RelationshipReference.formatId(identityType));
                } else {
                    identityTypeProperty.setValue(relationshipIdentity, getRootEntity(identityType, entityManager));
                }

                Property<Object> descriptorProperty = relationshipMemberMapper.getProperty(RelationshipDescriptor.class).getValue();
                Property<Object> ownerProperty = relationshipMemberMapper.getProperty(OwnerReference.class).getValue();

                descriptorProperty.setValue(relationshipIdentity, prop.getName());
                ownerProperty.setValue(relationshipIdentity, ownerEntity);
            }

            entityManager.persist(relationshipIdentity);
        }
    }

    /**
     * <p> Creates an {@link EntityMapper} for the given mapped entity. This method looks first for the owner
     * references
     * in order to have them configured first. The order is important to make sure the entities are created or
     * updated
     * in the correct order of dependency.
     * <p/>
     *
     * @param entityType
     */
    private void configureEntityMapper(Class<?> entityType) {
        EntityMapper entityMapper = new EntityMapper(entityType, this);
        Entry<Property, Property> ownerProperty = entityMapper.getProperty(OwnerReference.class);

        if (ownerProperty != null) {
            Class<?> ownerClass = ownerProperty.getValue().getJavaClass();

            // When working with multiple partitions supporting different types, some owner references point to a
            // String valued mapped property that holds only a reference to the corresponding type. This reference is
            // usually the id.
            if (!String.class.equals(ownerClass)) {
                if (getConfig().getEntityTypes().contains(ownerClass)) {
                    configureEntityMapper(ownerClass);
                }
            }
        }

        if (entityType.getSuperclass().isAnnotationPresent(IdentityManaged.class)) {
            configureEntityMapper(entityType.getSuperclass());
        }

        if (!this.entityMappers.contains(entityMapper)) {
            this.entityMappers.add(entityMapper);
        }
    }

    private EntityManager getEntityManager(IdentityContext context) {
        EntityManager entityManager = (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);

        if (entityManager == null) {
            throw MESSAGES.storeJpaCouldNotGetEntityManagerFromStoreContext();
        }

        return entityManager;
    }

    private void logEntityMappers() {
        if (JPA_STORE_LOGGER.isDebugEnabled()) {
            JPA_STORE_LOGGER.debug("Supported EntityMappers: [");

            for (EntityMapper entityMapper : this.entityMappers) {
                JPA_STORE_LOGGER.debugf(" %s: [", entityMapper.getEntityType());

                JPA_STORE_LOGGER.debugf("  Is root: %s", entityMapper.isRoot());
                JPA_STORE_LOGGER.debugf("  Mappings: [");

                for (EntityMapping entityMapping : entityMapper.getEntityMappings()) {
                    JPA_STORE_LOGGER.debugf("   %s: ", entityMapping.getSupportedType());
                    JPA_STORE_LOGGER.debugf("    Owner Type: %s", entityMapping.getOwnerType());

                    if (entityMapping.getTypeProperty() != null) {
                        JPA_STORE_LOGGER.debugf("    Has type property: %s", entityMapping.getTypeProperty().getName());
                    }

                    for (Property property : entityMapping.getProperties().keySet()) {
                        JPA_STORE_LOGGER.debugf("     Property: %s, %s", property.getName(), property.getJavaClass());

                        Property mappedProperty = entityMapping.getProperties().get(property);

                        if (mappedProperty != null) {
                            StringBuffer propertyAnnotations = new StringBuffer();

                            for (Annotation annotation : mappedProperty.getAnnotatedElement().getAnnotations()) {
                                if (propertyAnnotations.length() != 0) {
                                    propertyAnnotations.append(",");
                                }

                                propertyAnnotations.append(annotation.annotationType());
                            }

                            JPA_STORE_LOGGER.debugf("      Mapped Property: %s, %s, annotations [%s]", mappedProperty.getName(), mappedProperty.getJavaClass(), propertyAnnotations);
                        }
                    }
                }

                JPA_STORE_LOGGER.debugf("   ]");
                JPA_STORE_LOGGER.debugf("  ]");
                JPA_STORE_LOGGER.debug(" ]");
            }

            JPA_STORE_LOGGER.debug("]");
        }
    }

    private PermissionEntityMapper getPermissionMapperForResource(Class resourceClass) {
        int score = -1;
        PermissionEntityMapper mapper = null;

        // Loop through all the supported resource classes and find the best match
        for (PermissionEntityMapper m : permissionMappers) {
            for (Class<?> cls : m.getResourceClasses()) {
                if (cls.isAssignableFrom(resourceClass)) {
                    int currentScore = 0;
                    Class<?> currentClass = resourceClass;
                    while (!currentClass.equals(cls) && !Object.class.equals(currentClass)) {
                        currentScore++;
                        currentClass = currentClass.getSuperclass();
                    }

                    if (mapper == null || score == -1 || currentScore < score) {
                        score = currentScore;
                        mapper = m;
                    }
                }
            }
        }

        return mapper;
    }

    @Override
    public List<Permission> listPermissions(IdentityContext ctx, Object resource) {
        return listPermissions(ctx, resource, null);
    }

    @Override
    public List<Permission> listPermissions(IdentityContext ctx, Object resource, String operation) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource may not be null");
        }

        EntityManager em = getEntityManager(ctx);
        Class<?> resourceClass = ctx.getPermissionHandlerPolicy().getResourceClass(resource);
        PermissionEntityMapper mapper = getPermissionMapperForResource(resourceClass);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(mapper.getEntityClass());
        Root from = cq.from(mapper.getEntityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        // Set the resource class and resource identifier predicates
        predicates.add(cb.equal(from.get(mapper.getResourceClass().getName()),
                ctx.getPermissionHandlerPolicy().getResourceClass(resource).getName()));
        predicates.add(cb.equal(from.get(mapper.getResourceIdentifier().getName()),
                ctx.getPermissionHandlerPolicy().getIdentifier(resource)));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        List results = em.createQuery(cq).getResultList();

        List<Permission> perms = new ArrayList<Permission>();
        for (Object result : results) {
            Object owner = mapper.getOwner().getValue(result);
            IdentityType assignee = null;
            // If the owner value is a String, then it must be an identifier value
            if (String.class.equals(owner.getClass())) {
                assignee = lookupIdentityTypeById(ctx, IdentityType.class, (String) owner);
            } else {

                for (EntityMapper entityMapper : getEntityMappers()) {
                    if (entityMapper.getMappingsFor(IdentityType.class) != null && entityMapper.isRoot() && entityMapper.isPersist()) {
                        IdentityType identityType = entityMapper.<IdentityType>createType(owner, em);
                        if (identityType != null) {
                            assignee = identityType;
                            break;
                        }
                    }
                }
            }

            if (assignee == null) {
                throw new IdentityManagementException(String.format(
                        "Could not determine permission assignee [%s] for resource [%s]",
                        owner, resource));
            }

            PermissionOperationSet opSet = new PermissionOperationSet(result, resourceClass, mapper);

            for (String op : opSet.getOperations()) {
                if (operation != null && operation.equals(op)) {
                    perms.add(new Permission(resource, assignee, op));
                } else if (operation == null) {
                    perms.add(new Permission(resource, assignee, op));
                }
            }
        }

        return perms;
    }

    @Override
    public List<Permission> listPermissions(IdentityContext ctx, Set<Object> resources, String operation) {
        List<Permission> perms = new ArrayList<Permission>();

        for (Object resource : resources) {
            perms.addAll(listPermissions(ctx, resource, operation));
        }

        return perms;
    }

    @Override
    public List<Permission> listPermissions(IdentityContext ctx, Class<?> resourceClass, Serializable identifier) {
        return listPermissions(ctx, resourceClass, identifier, null);
    }

    @Override
    public List<Permission> listPermissions(IdentityContext ctx, Class<?> resourceClass, Serializable identifier, String operation) {
        if (resourceClass == null) {
            throw new IllegalArgumentException("Resource class may not be null");
        }

        if (identifier == null) {
            throw new IllegalArgumentException("Resource identifier may not be null");
        }

        EntityManager em = getEntityManager(ctx);
        PermissionEntityMapper mapper = getPermissionMapperForResource(resourceClass);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(mapper.getEntityClass());
        Root from = cq.from(mapper.getEntityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        // Set the resource class and resource identifier predicates
        predicates.add(cb.equal(from.get(mapper.getResourceClass().getName()), resourceClass.getName()));
        predicates.add(cb.equal(from.get(mapper.getResourceIdentifier().getName()), identifier));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        List results = em.createQuery(cq).getResultList();

        List<Permission> perms = new ArrayList<Permission>();
        for (Object result : results) {
            Object owner = mapper.getOwner().getValue(result);
            IdentityType assignee = null;
            // If the owner value is a String, then it must be an identifier value
            if (String.class.equals(owner.getClass())) {
                assignee = lookupIdentityTypeById(ctx, IdentityType.class, (String) owner);
            } else {

                for (EntityMapper entityMapper : getEntityMappers()) {
                    if (entityMapper.getMappingsFor(IdentityType.class) != null && entityMapper.isRoot() && entityMapper.isPersist()) {
                        IdentityType identityType = entityMapper.<IdentityType>createType(owner, em);
                        if (identityType != null) {
                            assignee = identityType;
                            break;
                        }
                    }
                }
            }

            if (assignee == null) {
                throw new IdentityManagementException(String.format(
                        "Could not determine permission assignee [%s] for resource class [%s] with identifier [%s]",
                        owner, resourceClass, identifier));
            }

            PermissionOperationSet opSet = new PermissionOperationSet(result, resourceClass, mapper);

            for (String op : opSet.getOperations()) {
                if (operation != null && operation.equals(op)) {
                    perms.add(new Permission(resourceClass, identifier, assignee, op));
                } else if (operation == null) {
                    perms.add(new Permission(resourceClass, identifier, assignee, op));
                }
            }
        }

        return perms;
    }

    private Object lookupPermissionEntity(IdentityContext ctx, PermissionEntityMapper mapper, IdentityType assignee, Object resource) {
        EntityManager em = getEntityManager(ctx);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(mapper.getEntityClass());
        Root from = cq.from(mapper.getEntityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        // Set the assignee, resource class and resource identifier predicates
        if (String.class.equals(mapper.getOwner().getBaseType())) {
            predicates.add(cb.equal(from.get(mapper.getOwner().getName()), assignee.getId()));
        } else {
            predicates.add(cb.equal(from.get(mapper.getOwner().getName()),
                    getOwnerEntity(assignee, mapper.getOwner(), em)));
        }

        predicates.add(cb.equal(from.get(mapper.getResourceClass().getName()),
                ctx.getPermissionHandlerPolicy().getResourceClass(resource).getName()));
        predicates.add(cb.equal(from.get(mapper.getResourceIdentifier().getName()),
                ctx.getPermissionHandlerPolicy().getIdentifier(resource)));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        Query query = em.createQuery(cq);

        query.setMaxResults(1);

        try {
            return query.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public boolean grantPermission(IdentityContext context, IdentityType assignee, Object resource, String operation) {
        EntityManager em = getEntityManager(context);

        PermissionEntityMapper mapper = getPermissionMapperForResource(resource.getClass());
        Serializable identifier = context.getPermissionHandlerPolicy().getIdentifier(resource);
        Class<?> resourceClass = context.getPermissionHandlerPolicy().getResourceClass(resource);

        // We first attempt to lookup an existing entity
        Object entity = lookupPermissionEntity(context, mapper, assignee, resource);

        // If there is no existing entity we create a new one
        if (entity == null) {
            try {
                entity = mapper.getEntityClass().newInstance();

                // Set the assignee property - this will either be a String, or a reference to an
                // identity entity
                if (String.class.equals(mapper.getOwner().getBaseType())) {
                    mapper.getOwner().setValue(entity, assignee.getId());
                } else {
                    Object identityEntity = getOwnerEntity(assignee, mapper.getOwner(), em);
                    mapper.getOwner().setValue(entity, identityEntity);
                }

                // Set the resource class
                mapper.getResourceClass().setValue(entity, resourceClass.getName());

                // Set the resource identifier
                Serializable resourceIdentifier = context.getPermissionHandlerPolicy().getIdentifier(resource);
                if (resourceIdentifier == null) {
                    throw new IdentityManagementException(String.format(
                            "No identifier value could be generated for resource [%s]", resource));
                }

                // TODO this is a nasty hack, we still need to support type conversion between a multitude of types
                mapper.getResourceIdentifier().setValue(entity, resourceIdentifier.toString());

                PermissionOperationSet operationSet = new PermissionOperationSet(entity, resourceClass, mapper);
                operationSet.appendOperation(operation);

                em.persist(entity);

                return true;

            } catch (Exception ex) {
                throw new IdentityManagementException("Error persisting permission", ex);
            }
        } else {
            PermissionOperationSet operationSet = new PermissionOperationSet(entity, resourceClass, mapper);
            operationSet.appendOperation(operation);
            em.merge(entity);
            return true;
        }
    }

    protected class PermissionOperationSet {
        private PermissionEntityMapper mapper;
        private AllowedOperations perms;
        private Object entity;

        public PermissionOperationSet(Object entity, Class resourceClass, PermissionEntityMapper mapper) {
            this.entity = entity;
            this.mapper = mapper;
            this.perms = (AllowedOperations) resourceClass.getAnnotation(AllowedOperations.class);
        }

        public void appendOperation(String operation) {
            adjustOperation(operation, true);
        }

        public void removeOperation(String operation) {
            adjustOperation(operation, false);
        }

        private String adjustCSVOperation(String value, String operation, boolean mode) {
            Set<String> ops = new HashSet<String>();

            if (value != null && !"".equals(value)) {
                for (String op : value.split(",")) {
                    ops.add(op);
                }
            }

            if (mode) {
                ops.add(operation);
            } else {
                ops.remove(operation);
            }

            StringBuilder sb = new StringBuilder();
            for (String op : ops) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(op);
            }

            return sb.toString();
        }

        public Set<String> getOperations() {
            Object opValue = mapper.getOperation().getValue(entity);
            Set<String> operations = new HashSet<String>();

            // Determine how the permission operations are stored - first check if bitmasks are used
            if (perms != null) {
                try {
                    // Convert the operations value to a long for convenience
                    long ops = opValue != null ? Long.valueOf(opValue.toString()) : 0;

                    for (AllowedOperation o : perms.value()) {
                        if (o.mask() > 0) {
                            if ((o.mask() & ops) != 0) {
                                operations.add(o.value());
                            }
                        }
                    }

                    return operations;
                } catch (NumberFormatException ex) {
                    // Do nothing, revert to default behaviour below
                }
            }

            // Operations are stored as a comma separated value
            for (String op : ((String) opValue).split(",")) {
                operations.add(op);
            }

            return operations;
        }

        private void adjustOperation(String operation, boolean mode) {
            Object operations = mapper.getOperation().getValue(entity);
            Object newValue = null;

            // Determine how the permission operations are stored - first check if bitmasks are used
            if (perms != null) {
                AllowedOperation perm = null;

                for (AllowedOperation o : perms.value()) {
                    if (o.value().equals(operation)) {
                        perm = o;
                        break;
                    }
                }

                // Check if there is a bitmask value for the operation
                if (perm != null && perm.mask() > 0) {

                    // Convert the operations value to a long for convenience
                    long ops = operations != null ? Long.valueOf(operations.toString()) : 0;
                    if (mode) {
                        ops |= perm.mask();
                    } else {
                        ops ^= perm.mask();
                    }

                    if (String.class.equals(mapper.getOperation().getBaseType())) {
                        mapper.getOperation().setValue(entity, Long.toString(ops));
                    } else {
                        // TODO may need to do some further type conversion here...
                        mapper.getOperation().setValue(entity, ops);
                    }

                // Otherwise the operations should be stored as a comma-separated String
                } else if (perm != null || (perm == null && perms.value().length == 0)) {
                    mapper.getOperation().setValue(entity,
                            adjustCSVOperation((String) mapper.getOperation().getValue(entity), operation, mode));
                } else {
                    // Trying to set an operation value that isn't defined - throw an exception
                    throw new IllegalArgumentException(String.format(
                            "Attempted to set illegal permission operation [%s] for object [%s]",
                            operation, entity));
                }
            } else {
                mapper.getOperation().setValue(entity,
                        adjustCSVOperation((String) mapper.getOperation().getValue(entity), operation, mode));
            }
        }
    }

    @Override
    public boolean revokePermission(IdentityContext context, IdentityType assignee, Object resource, String operation) {
        EntityManager em = getEntityManager(context);

        PermissionEntityMapper mapper = getPermissionMapperForResource(resource.getClass());
        Serializable identifier = context.getPermissionHandlerPolicy().getIdentifier(resource);
        Class<?> resourceClass = context.getPermissionHandlerPolicy().getResourceClass(resource);

        // We first attempt to lookup an existing entity
        Object entity = lookupPermissionEntity(context, mapper, assignee, resource);

        // If there's no entity found then there's nothing to do
        if (entity == null) {
            return false;
        } else {
            PermissionOperationSet operationSet = new PermissionOperationSet(entity, resourceClass, mapper);
            operationSet.removeOperation(operation);
            em.merge(entity);
            return true;
        }
    }

    @Override
    public void revokeAllPermissions(IdentityContext ctx, Object resource) {
        EntityManager em = getEntityManager(ctx);
        PermissionEntityMapper mapper = getPermissionMapperForResource(resource.getClass());

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(mapper.getEntityClass());
        Root from = cq.from(mapper.getEntityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        // Set the resource class and resource identifier predicates
        predicates.add(cb.equal(from.get(mapper.getResourceClass().getName()),
                ctx.getPermissionHandlerPolicy().getResourceClass(resource).getName()));
        predicates.add(cb.equal(from.get(mapper.getResourceIdentifier().getName()),
                ctx.getPermissionHandlerPolicy().getIdentifier(resource)));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

        List results = em.createQuery(cq).getResultList();
        for (Object result : results) {
            em.remove(result);
        }
    }
}