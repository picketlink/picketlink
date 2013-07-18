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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.TOTPCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.PartitionClass;
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
import static java.util.Map.Entry;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
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
            this.entityMappers.add(new EntityMapper(entityType, this));
        }
    }

    @Override
    public void add(IdentityContext context, AttributedType attributedType) {
        attributedType.setId(context.getIdGenerator().generate());

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;
            identityType.setPartition(context.getPartition());
        }

        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            Object entity = entityMapper.createEntity(attributedType, entityManager);

            if (entity != null) {
                entityManager.persist(entity);
            }

            if (Relationship.class.isInstance(attributedType)) {
                addRelationshipIdentity(attributedType, entityManager);
            }
        }

        entityManager.flush();
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        add(identityContext, partition);
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        return "SIMPLE_JPA_STORE_CONFIG";
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
            Object entityInstance = result.get(0);
            P type = entityMapper.createType(entityInstance, entityManager);

            populateAttributedType(type, entityInstance, entityManager);

            return type;
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
    public void update(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);

        for (EntityMapper entityMapper : getMapperFor(attributedType.getClass())) {
            Object entity = entityMapper.updateEntity(attributedType, entityManager);

            if (entity != null) {
                entityManager.merge(entity);
            }
        }

        entityManager.flush();
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
    public void remove(IdentityContext context, AttributedType attributedType) {
        EntityManager entityManager = getEntityManager(context);
        EntityMapper rootMapper = getRootMapper(attributedType.getClass());

        Object rootEntity = entityManager.find(rootMapper.getEntityType(), attributedType.getId());

        if (Relationship.class.isAssignableFrom(attributedType.getClass())) {
            List<?> childRelationships = findChildRelationships(context, (Relationship) attributedType);

            for (Object child : childRelationships) {
                entityManager.remove(child);
            }
        } else {
            for (EntityMapper childMappers : getMapperFor(attributedType.getClass())) {
                if (!childMappers.isRoot()) {
                    CriteriaBuilder qb = entityManager.getCriteriaBuilder();
                    CriteriaQuery cq = qb.createQuery(childMappers.getEntityType());
                    List<Predicate> predicates = new ArrayList<Predicate>();
                    Root from = cq.from(childMappers.getEntityType());

                    for (EntityMapping entityMapping : childMappers.getMappings()) {
                        for (Property ownerProperty : entityMapping.getProperties().keySet()) {
                            Property mappedOwnerProperty = entityMapping.getProperties().get(ownerProperty);

                            if (mappedOwnerProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                                Root propertyEntityJoin = cq.from(rootMapper.getEntityType());

                                if (mappedOwnerProperty.getAnnotatedElement().isAnnotationPresent(Id.class)) {
                                    predicates.add(qb.and(qb.equal(from, propertyEntityJoin)));
                                    predicates.add(qb.equal(propertyEntityJoin, rootEntity));
                                } else {
                                    predicates.add(qb.and(qb.equal(from.get(mappedOwnerProperty.getName()), propertyEntityJoin)));
                                    predicates.add(qb.equal(propertyEntityJoin, rootEntity));
                                }
                            }
                        }
                    }

                    cq.where(predicates.toArray(new Predicate[predicates.size()]));

                    cq.select(from);

                    Query childQuery = entityManager.createQuery(cq);

                    for (Object child : childQuery.getResultList()) {
                        entityManager.remove(child);
                    }
                }
            }

            removeIdentityTypeRelationships(context, attributedType);
            removeIdentityTypeAttributes(context, rootEntity);
        }

        entityManager.remove(rootEntity);
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        EntityMapper rootMapper = getRootMapper(identityQuery.getIdentityType());
        EntityManager entityManager = getEntityManager(context);

        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery cq = qb.createQuery(rootMapper.getEntityType());
        List<Predicate> predicates = new ArrayList<Predicate>();
        Root<?> from = cq.from(rootMapper.getEntityType());

        Partition partition = context.getPartition();

        if (identityQuery.getParameter(IdentityType.PARTITION) != null) {
            partition = (Partition) identityQuery.getParameter(IdentityType.PARTITION)[0];
        }

        EntityMapper partitionRootMapper = getRootMapper(partition.getClass());

        for (EntityMapping entityMapping : rootMapper.getMappingsFor(identityQuery.getIdentityType())) {
            for (Property ownerProperty : entityMapping.getProperties().keySet()) {
                Property mappedOwnerProperty = entityMapping.getProperties().get(ownerProperty);
                Class mappedClass = mappedOwnerProperty.getJavaClass();

                if (mappedClass.equals(partitionRootMapper.getEntityType())) {
                    Join<Object, Object> partitionJoing = from.join(mappedOwnerProperty.getName());
                    predicates.add(qb.equal(partitionJoing, entityManager.find(partitionRootMapper.getEntityType(), partition.getId())));
                }
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
                    boolean addedJoin = false;

                    Root<?> propertyEntityJoin = from;

                    for (EntityMapping entityMapping : parameterEntityMapper.getMappings()) {
                        if (addedJoin) {
                            break;
                        }

                        for (Property ownerProperty : entityMapping.getProperties().keySet()) {
                            Property mappedOwnerProperty = entityMapping.getProperties().get(ownerProperty);
                            Class mappedClass = mappedOwnerProperty.getJavaClass();

                            if (mappedClass.equals(rootMapper.getEntityType()) && mappedOwnerProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                                propertyEntityJoin = cq.from(parameterEntityMapper.getEntityType());
                                predicates.add(qb.and(qb.equal(from, propertyEntityJoin)));
                                addedJoin = true;
                                break;
                            }
                        }
                    }

                    Entry<Property, Property> mappedProperty =
                            parameterEntityMapper.getProperty(identityQuery.getIdentityType(), attributeParameter.getName());

                    if (IdentityType.CREATED_AFTER.equals(queryParameter) || IdentityType.EXPIRY_AFTER.equals(queryParameter)) {
                        predicates.add(qb.greaterThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getValue().getName()), (Date) parameterValues[0]));
                    } else if (IdentityType.CREATED_BEFORE.equals(queryParameter) || IdentityType.EXPIRY_BEFORE.equals(queryParameter)) {
                        predicates.add(qb.lessThanOrEqualTo(propertyEntityJoin.<Date>get(mappedProperty.getValue().getName()), (Date) parameterValues[0]));
                    } else {
                        predicates.add(qb.equal(propertyEntityJoin.get(mappedProperty.getValue().getName()), parameterValues[0]));
                    }
                } else {
                    String[] valuesToSearch = new String[parameterValues.length];

                    for (int i = 0; i < parameterValues.length; i++) {
                        valuesToSearch[i] = Base64.encodeObject((Serializable) parameterValues[i]);
                    }

                    Class<?> attributeEntityClass = null;
                    Property attributeNameProperty = null;
                    Property attributeValueProperty = null;
                    Property ownerProperty = null;

                    for (EntityMapper entityMapper : getMapperFor(Attribute.class)) {
                        attributeEntityClass = entityMapper.getEntityType();
                        attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class).getValue();
                        attributeValueProperty = entityMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
                        ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
                    }

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

        List<V> result = new ArrayList<V>();

        for (Object entity : query.getResultList()) {
            V attributedType = rootMapper.<V>createType(entity, entityManager);

            populateAttributedType(attributedType, entity, entityManager);

            result.add(attributedType);
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

        List<?> result = new ArrayList<Object>();

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
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipMapping().getEntityClass());
            Root<?> root = criteria.from(getConfig().getRelationshipMapping().getEntityClass());

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(builder.equal(root.get(getConfig().getRelationshipMapping().getRelationshipClass().getName()),
                    query.getRelationshipClass().getName()));

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
            }

            Set<Entry<QueryParameter, Object[]>> entrySet = query.getParameters().entrySet();

            for (Entry<QueryParameter, Object[]> entry : entrySet) {
                if (AttributeParameter.class.equals(entry.getKey().getClass())) {
                    AttributeParameter customParameter = (AttributeParameter) entry.getKey();
                    Object[] attributeValues = entry.getValue();

                    String[] valuesToSearch = new String[attributeValues.length];

                    for (int i = 0; i < attributeValues.length; i++) {
                        valuesToSearch[i] = Base64.encodeObject((Serializable) attributeValues[i]);
                    }

                    Class<?> attributeEntityClass = null;
                    Property attributeNameProperty = null;
                    Property attributeValueProperty = null;
                    Property ownerProperty = null;

                    for (EntityMapper entityMapper : getMapperFor(Attribute.class)) {
                        attributeEntityClass = entityMapper.getEntityType();
                        attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class).getValue();
                        attributeValueProperty = entityMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
                        ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
                    }

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

        Object ownerEntity = getAttributedTypeEntity(attributedType, entityManager);

        Class<?> attributeEntityClass = null;
        Property attributeNameProperty = null;
        Property attributeValueProperty = null;
        Property ownerProperty = null;

        for (EntityMapper entityMapper : getMapperFor(Attribute.class)) {
            attributeEntityClass = entityMapper.getEntityType();
            attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class).getValue();
            attributeValueProperty = entityMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
            ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
        }

        for (Serializable attributeValue : (Serializable[]) values) {
            Object attributeEntity = null;

            try {
                attributeEntity = attributeEntityClass.newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(attributeEntityClass.getName(), e);
            }

            attributeNameProperty.setValue(attributeEntity, attribute.getName());
            attributeValueProperty.setValue(attributeEntity, Base64.encodeObject(attributeValue));
            ownerProperty.setValue(attributeEntity, ownerEntity);

            entityManager.persist(attributeEntity);
        }
    }

    public void populateAllAttributes(AttributedType attributedType, EntityManager entityManager) {
        populateAttribute(attributedType, entityManager, null);
    }

    private EntityManager getEntityManager(IdentityContext context) {
        if (!context.isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    private List<EntityMapper> getMapperFor(Class<?> attributedType) {
        List<EntityMapper> mappers = new ArrayList<EntityMapper>();

        EntityMapper rootEntityMapper = null;

        for (EntityMapper entityMapper : this.entityMappers) {
            if (entityMapper.supports(attributedType)) {
                if (entityMapper.isRoot()) {
                    rootEntityMapper = entityMapper;
                }
                mappers.add(entityMapper);
            }
        }

        if (mappers.isEmpty()) {
            throw new IdentityManagementException("No entity mapper found for type [" + attributedType + "].");
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
            EntityMapper entityMapper = getRootMapper(IdentityType.class);

            if (identityProperty.getJavaClass().equals(String.class)) {
                identityTypeEntity = entityManager.find(identityProperty.getJavaClass(), identityProperty.getValue(object).toString());
                identityType = entityMapper.createType(identityTypeEntity, entityManager);
            } else {
                identityTypeEntity = identityProperty.getValue(object);
                identityType = entityMapper.createType(identityTypeEntity, entityManager);
            }

            populateAttributedType(identityType, identityTypeEntity, entityManager);

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

    private void removeIdentityTypeRelationships(IdentityContext context, AttributedType attributedType) {
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

    private void removeIdentityTypeAttributes(IdentityContext context, Object object) {
        EntityManager em = getEntityManager(context);

        List<?> results = findIdentityTypeAttributes(context, object);

        for (Object result : results) {
            em.remove(result);
        }
    }

    private List<?> findIdentityTypeAttributes(IdentityContext context, Object entity) {
        EntityManager em = getEntityManager(context);

        Class<?> attributeEntityClass = null;
        Property attributeNameProperty = null;
        Property attributeValueProperty = null;
        Property ownerProperty = null;

        for (EntityMapper entityMapper: getMapperFor(Attribute.class)) {
            attributeEntityClass = entityMapper.getEntityType();
            attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class).getValue();
            attributeValueProperty = entityMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
            ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeEntityClass);
        Root<?> root = criteria.from(attributeEntityClass);
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(ownerProperty.getName()), entity));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private EntityMapper getRootMapper(Class<? extends AttributedType> aClass) {
        return getMapperFor(aClass).get(0);
    }

    public List<EntityMapper> getEntityMappers() {
        return this.entityMappers;
    }

    private void populateAttribute(AttributedType attributedType, EntityManager entityManager, String attributeName) {
        Class<?> attributeEntityClass = null;
        Property attributeNameProperty = null;
        Property attributeValueProperty = null;
        Property ownerProperty = null;

        for (EntityMapper entityMapper: getMapperFor(Attribute.class)) {
            attributeEntityClass = entityMapper.getEntityType();
            attributeNameProperty = entityMapper.getProperty(Attribute.class, AttributeName.class).getValue();
            attributeValueProperty = entityMapper.getProperty(Attribute.class, AttributeValue.class).getValue();
            ownerProperty = entityMapper.getProperty(Attribute.class, OwnerReference.class).getValue();
        }

        CriteriaBuilder qb = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> cq = qb.createQuery(attributeEntityClass);
        Root<?> from = cq.from(attributeEntityClass);
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (attributeName != null) {
            predicates.add(qb.equal(from.get(attributeNameProperty.getName()),
                    attributeName));
        }

        predicates.add(qb.equal(from.get(ownerProperty.getName()),
                getAttributedTypeEntity(attributedType, entityManager)));

        cq.where(predicates.toArray(new Predicate[predicates.size()]));

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
        for (EntityMapper entityMapper : getMapperFor(Attribute.class)) {
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
    }

    public void removeAllAttributes(AttributedType attributedType, EntityManager entityManager) {
        removeAttribute(attributedType, null, entityManager);
    }

    private void addRelationshipIdentity(AttributedType attributedType, EntityManager entityManager) {
        Object ownerEntity = getAttributedTypeEntity(attributedType, entityManager);

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType>createQuery(attributedType.getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class, true)).getResultList();

        for (Property<IdentityType> prop : props) {
            Object relationshipIdentity = null;

            try {
                relationshipIdentity = getConfig().getRelationshipIdentityMapping().getEntityClass().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(getConfig().getRelationshipIdentityMapping().getEntityClass().getName(), e);
            }

            IdentityType identityType = prop.getValue(attributedType);

            if (identityType != null) {
                Object identityObject = null;

                try {
                    identityObject = entityManager.find(getConfig().getRelationshipIdentityMapping().getRelationshipMember().getJavaClass(), identityType.getId());
                } catch (IdentityManagementException ignore) {
                    // if the identity object does not exists, use only its id.
                }

                Property<Object> identityTypeProperty = getConfig().getRelationshipIdentityMapping().getRelationshipMember();

                if (identityTypeProperty.getJavaClass().equals(String.class)) {
                    identityTypeProperty.setValue(relationshipIdentity,
                            identityType.getId());
                } else {
                    identityTypeProperty.setValue(relationshipIdentity, identityObject);
                }

                getConfig().getRelationshipIdentityMapping().getRelationshipDescriptor().setValue(relationshipIdentity,
                        prop.getName());
                getConfig().getRelationshipIdentityMapping().getRelationshipOwner().setValue(relationshipIdentity,
                        ownerEntity);
            }

            entityManager.persist(relationshipIdentity);
        }
    }

    private Object getAttributedTypeEntity(AttributedType attributedType, EntityManager entityManager) {
        return entityManager.find(getRootMapper(attributedType.getClass()).getEntityType(), attributedType.getId());
    }

    private void populateAttributedType(AttributedType attributedType, Object rootEntity, EntityManager entityManager) {
        for (EntityMapper finalMapper : getMapperFor(attributedType.getClass())) {
            if (!finalMapper.isRoot()) {
                StringBuffer hql = new StringBuffer();

                hql.append("from " + finalMapper.getEntityType().getName()).append(" where ");

                Property tmpMappedOwnerProperty = null;

                for (EntityMapping entityMapping : finalMapper.getMappings()) {
                    if (tmpMappedOwnerProperty != null) {
                        break;
                    }

                    for (Property ownerProperty : entityMapping.getProperties().keySet()) {
                        Property mappedOwnerProperty = entityMapping.getProperties().get(ownerProperty);

                        if (mappedOwnerProperty.getAnnotatedElement().isAnnotationPresent(OwnerReference.class)) {
                            tmpMappedOwnerProperty = mappedOwnerProperty;
                            break;
                        }
                    }
                }

                hql.append(" ").append(tmpMappedOwnerProperty.getName()).append("= ?");

                Query childQuery = entityManager.createQuery(hql.toString());

                childQuery.setParameter(1, rootEntity);

                List<?> childs = childQuery.getResultList();

                for (Object child : childs) {
                    finalMapper.populate(attributedType, child, entityManager);

                    List<Property<Object>> parentProperties = PropertyQueries
                            .createQuery(attributedType.getClass())
                            .addCriteria(new TypedPropertyCriteria(AttributedType.class, true))
                            .getResultList();

                    for (Property property : parentProperties) {
                        EntityMapper entityMapperForProperty = getEntityMapperForProperty(property.getJavaClass(), property.getName());

                        if (entityMapperForProperty != null) {
                            Entry<Property, Property> property1 = entityMapperForProperty.getProperty(property.getJavaClass(), property.getName());

                            Object value = property1.getValue().getValue(child);

                            if (value != null) {
                                EntityMapper rootMapper = getRootMapper(property.getJavaClass());
                                IdentityType type = rootMapper.createType(value, entityManager);
                                populateAttributedType(type, value, entityManager);
                                property.setValue(attributedType, type);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        //TODO: Implement storeCredential
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

}