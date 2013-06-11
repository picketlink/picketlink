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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.PropertyType;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.TOTPCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.AttributedType.AttributeParameter;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityProperty;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.SecurityContext;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
 *
 * @author Shane Bryzak
 * @author Pedro Silva
 */
@CredentialHandlers({PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class, TOTPCredentialHandler.class})
public class JPAIdentityStore implements CredentialStore<JPAIdentityStoreConfiguration> {

    // Invocation context parameters
    public static final String INVOCATION_CTX_ENTITY_MANAGER = "CTX_ENTITY_MANAGER";

    // Event context parameters
    public static final String EVENT_CONTEXT_USER_ENTITY = "USER_ENTITY";
    public static final String EVENT_CONTEXT_GROUP_ENTITY = "GROUP_ENTITY";
    public static final String EVENT_CONTEXT_ROLE_ENTITY = "ROLE_ENTITY";

    /**
     * The configuration for this instance
     */
    private JPAIdentityStoreConfiguration config;

    @Override
    public void setup(JPAIdentityStoreConfiguration config) {
        this.config = config;
    }

    @Override
    public JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    @Override
    public void add(SecurityContext context, AttributedType value) {
        if (value instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            IdentityType identityType = (IdentityType) value;

            IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

            Object entity = handler.createEntity(context, identityType, this);

            EntityManager em = getEntityManager(context);

            em.persist(entity);
            em.flush();

            updateIdentityTypeAttributes(context, identityType, entity);

            AbstractBaseEvent event = handler.raiseCreatedEvent(identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            context.getEventBridge().raiseEvent(event);
        } else if (value instanceof Relationship) {
            checkRelationshipClassProvided();

            addRelationship(context, (Relationship) value);
        }
    }

    @Override
    public void update(SecurityContext context, AttributedType attributedType) {
        if (attributedType instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            IdentityType identityType = (IdentityType) attributedType;

            Object entity = lookupIdentityObjectById(context, identityType.getId());

            IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

            handler.populateEntity(context, entity, identityType, this);

            updateIdentityTypeAttributes(context, identityType, entity);

            EntityManager em = getEntityManager(context);

            em.merge(entity);
            em.flush();

            AbstractBaseEvent event = handler.raiseUpdatedEvent(identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identityType);
            context.getEventBridge().raiseEvent(event);
        } else if (attributedType instanceof Relationship) {
            checkRelationshipClassProvided();

            Relationship relationship = (Relationship) attributedType;

            Object entity = lookupRelationshipObjectById(context, relationship.getId());

            updateRelationshipAttributes(context, relationship, entity);

            EntityManager em = getEntityManager(context);

            em.merge(entity);
            em.flush();
        }

    }

    @Override
    public void remove(SecurityContext context, AttributedType attributedType) {
        if (attributedType instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            removeIdentityType(context, attributedType);
        } else if (attributedType instanceof Relationship) {
            checkRelationshipClassProvided();

            Relationship relationship = (Relationship) attributedType;

            removeRelationship(context, relationship);
        }
    }

    @Override
    public User getUser(SecurityContext context, String loginName) {
        if (loginName == null) {
            return null;
        }

        DefaultIdentityQuery<User> defaultIdentityQuery = new DefaultIdentityQuery<User>(context, User.class, this);

        defaultIdentityQuery.setParameter(User.LOGIN_NAME, loginName);

        List<User> resultList = defaultIdentityQuery.getResultList();

        User user = null;

        if (!resultList.isEmpty()) {
            user = resultList.get(0);
        }

        return user;
    }

    @Override
    public Group getGroup(SecurityContext context, String groupPath) {
        if (groupPath == null) {
            return null;
        }

        if (!groupPath.startsWith("/")) {
            groupPath = "/" + groupPath;
        }

        DefaultIdentityQuery<Group> defaultIdentityQuery = new DefaultIdentityQuery<Group>(context, Group.class, this);

        defaultIdentityQuery.setParameter(Group.PATH, groupPath);

        List<Group> resultList = defaultIdentityQuery.getResultList();

        Group group = null;

        if (!resultList.isEmpty()) {
            group = resultList.get(0);
        }

        return group;
    }

    @Override
    public Group getGroup(SecurityContext context, String name, Group parent) {
        if (name == null || parent == null) {
            return null;
        }

        String path = "/" + name;

        if (parent != null) {
            Object storedParent = lookupIdentityObjectById(context, parent.getId());

            path = getConfig().getModelProperty(PropertyType.GROUP_PATH).getValue(storedParent) + path;
        }

        return getGroup(context, path);
    }

    @Override
    public Role getRole(SecurityContext context, String name) {
        if (name == null) {
            return null;
        }

        DefaultIdentityQuery<Role> defaultIdentityQuery = new DefaultIdentityQuery<Role>(context, Role.class, this);

        defaultIdentityQuery.setParameter(Role.NAME, name);

        List<Role> resultList = defaultIdentityQuery.getResultList();

        Role role = null;

        if (!resultList.isEmpty()) {
            role = resultList.get(0);
        }

        return role;

    }

    @Override
    public Agent getAgent(SecurityContext context, String loginName) {
        if (loginName == null) {
            return null;
        }

        DefaultIdentityQuery<Agent> defaultIdentityQuery = new DefaultIdentityQuery<Agent>(context, Agent.class, this);

        defaultIdentityQuery.setParameter(Agent.LOGIN_NAME, loginName);

        List<Agent> resultList = defaultIdentityQuery.getResultList();

        Agent agent = null;

        if (!resultList.isEmpty()) {
            agent = resultList.get(0);
        } else {
            agent = getUser(context, loginName);
        }

        return agent;
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(SecurityContext context, RelationshipQuery<T> query) {
        return fetchQueryResults(context, query, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
        List<T> result = new ArrayList<T>();

        EntityManager em = getEntityManager(context);

        JPACriteriaQueryBuilder criteriaBuilder = new JPACriteriaQueryBuilder(context, this, identityQuery);

        List<Predicate> predicates = criteriaBuilder.getPredicates(context);

        CriteriaQuery<?> criteria = criteriaBuilder.getCriteria();

        List<Order> orders = criteriaBuilder.getOrders();

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        criteria.orderBy(orders);

        TypedQuery<?> query = em.createQuery(criteria);

        if (identityQuery.getLimit() > 0) {
            query.setMaxResults(identityQuery.getLimit());

            if (identityQuery.getOffset() > 0) {
                query.setFirstResult(identityQuery.getOffset());
            }
        }

        List<?> queryResult = query.getResultList();

        for (Object identity : queryResult) {
            T identityType = (T) convertToIdentityType(context, identity);

            result.add(identityType);
        }

        return result;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(SecurityContext context, IdentityQuery<T> identityQuery) {
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
    public <T extends Relationship> int countQueryResults(SecurityContext context, RelationshipQuery<T> query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(SecurityContext context, IdentityType identity, Attribute<? extends Serializable> attribute) {
        Serializable value = attribute.getValue();

        if (value != null) {
            Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME);
            Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);
            Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE);

            List<?> storedAttributes = findIdentityTypeAttributes(context, identity, attribute.getName());

            // store a new attribute
            if (storedAttributes.isEmpty()) {
                Serializable[] values = null;

                if (value.getClass().isArray()) {
                    values = (Serializable[]) value;
                } else {
                    values = new Serializable[]{value};
                }

                Object entity = lookupIdentityObjectById(context, identity.getId());

                for (Serializable attribValue : values) {
                    Object newAttribute = null;

                    try {
                        newAttribute = getConfig().getAttributeClass().newInstance();
                    } catch (Exception e) {
                        throw MESSAGES.instantiationError(getConfig().getAttributeClass().getName(), e);
                    }

                    attributeNameProperty.setValue(newAttribute, attribute.getName());
                    attributeValueProperty.setValue(newAttribute, Base64.encodeObject(attribValue));
                    attributeIdentityProperty.setValue(newAttribute, entity);

                    getEntityManager(context).persist(newAttribute);
                }
            } else {
                removeAttribute(context, identity, attribute.getName());
                setAttribute(context, identity, attribute);
            }
        }
    }

    @Override
    public void removeAttribute(SecurityContext context, IdentityType identity, String name) {
        List<?> storedAttributes = findIdentityTypeAttributes(context, identity, name);

        for (Object storedAttribute : storedAttributes) {
            getEntityManager(context).remove(storedAttribute);
        }
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(SecurityContext context, IdentityType identityType,
                                                              String attributeName) {
        List<?> attributes = findIdentityTypeAttributes(context, identityType, attributeName);

        populateAttributes(identityType, attributes);

        return identityType.getAttribute(attributeName);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(SecurityContext context, Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(context, agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        List<?> result = em.createQuery(criteria).getResultList();

        List<T> storages = new ArrayList<T>();

        for (Object object : result) {
            storages.add(convertToCredentialStorage(context, object, storageClass));
        }

        return storages;
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(SecurityContext context, Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();
        return convertToCredentialStorage(context, retrieveLastCredentialEntity(context, agent, storageClass), storageClass);
    }

    @Override
    public void storeCredential(SecurityContext context, Agent agent, CredentialStorage storage) {
        checkCredentialClassProvided();

        Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

        Object newCredential = null;

        try {
            newCredential = getConfig().getCredentialClass().newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(getConfig().getCredentialClass().getName(), e);
        }

        Date effectiveDate = storage.getEffectiveDate();

        if (effectiveDate == null) {
            effectiveDate = new Date();
        }

        Object agentInstance = lookupIdentityObjectById(context, agent.getId());

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        identityTypeProperty.setValue(newCredential, agentInstance);
        typeProperty.setValue(newCredential, storage.getClass().getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        EntityManager em = getEntityManager(context);

        em.persist(newCredential);

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        Property<Object> attributeName = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME);
        Property<Object> attributeValue = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE);
        Property<Object> attributeCredential = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

        for (Property<Object> property : annotatedTypes) {
            Object newCredentialAttribute = null;

            try {
                newCredentialAttribute = this.getConfig().getCredentialAttributeClass().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(getConfig().getCredentialAttributeClass().getName(), e);
            }

            attributeName.setValue(newCredentialAttribute, property.getName());
            attributeValue.setValue(newCredentialAttribute, Base64.encodeObject((Serializable) property.getValue(storage)));
            attributeCredential.setValue(newCredentialAttribute, newCredential);

            em.persist(newCredentialAttribute);
        }

        em.flush();
    }

    @Override
    public void updateCredential(SecurityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = context.getCredentialUpdater(credential.getClass(), this);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credential.getClass());
        }

        handler.update(context, agent, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public void validateCredentials(SecurityContext context, Credentials credentials) {
        CredentialHandler handler = context.getCredentialValidator(credentials.getClass(), this);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credentials.getClass());
        }

        handler.validate(context, credentials, this);
    }

    protected Partition convertPartitionEntityToPartition(Object partitionObject) {
        checkPartitionClassProvided();

        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        String type = typeProperty.getValue(partitionObject).toString();

        Partition partition = null;

        if (Realm.class.getName().equals(type)) {
            partition = convertPartitionEntityToRealm(partitionObject);
        } else if (Tier.class.getName().equals(type)) {
            partition = convertPartitionEntityToTier(partitionObject);
        } else {
            throw MESSAGES.partitionUnsupportedType(type);
        }

        return partition;
    }

    protected EntityManager getEntityManager(SecurityContext context) {
        if (!context.isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    /**
     * <p>
     * Lookup a stored {@link IdentityType} using the id.
     * </p>
     *
     * @param id
     * @return
     */
    protected Object lookupIdentityObjectById(SecurityContext context, String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());

        criteria.where(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), id));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            throw MESSAGES.attributedTypeNotFoundWithId(IdentityType.class, id, context.getPartition());
        } else {
            return results.get(0);
        }
    }

    protected Object lookupAndCreatePartitionObject(SecurityContext context, Partition partition) {
        checkPartitionClassProvided();

        EntityManager entityManager = getEntityManager(context);

        Object partitionObject = entityManager.find(getConfig().getPartitionClass(), partition.getId());

        if (partitionObject == null) {
            try {
                partitionObject = getConfig().getPartitionClass().newInstance();

                getConfig().setModelPropertyValue(partitionObject, PropertyType.PARTITION_ID, partition.getId(), true);
                getConfig().setModelPropertyValue(partitionObject, PropertyType.PARTITION_TYPE, partition.getClass().getName(),
                        true);

                entityManager.persist(partitionObject);
                entityManager.flush();
            } catch (Exception e) {
                throw new IdentityManagementException("Error creating Partition [" + partition + "].", e);
            }
        }

        return partitionObject;
    }

    protected List<String> getAllowedPartitionIds(SecurityContext context, Partition currentPartition) {
        List<String> partitionIds = new ArrayList<String>();

        partitionIds.add(context.getPartition().getId());

        if (currentPartition != null) {
            partitionIds.add(currentPartition.getId());
        }

        return partitionIds;
    }

    /**
     * <p>
     * Converts the given object to an instance of its corresponding {@link IdentityType}.
     * </p>
     *
     * @param relationshipObject
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T extends Relationship> T convertToRelationshipType(SecurityContext context, Object relationshipObject) {
        Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
        Property<Object> idProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID);
        Property<Object> descriptorProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS);

        final T relationshipType;
        final Class<?> relationshipClass;

        String typeName = typeProperty.getValue(relationshipObject).toString();

        try {
            relationshipClass = Class.forName(typeName);
            relationshipType = (T) relationshipClass.newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(typeName, e);
        }

        List<Property<Object>> identityTypeIdProperty = PropertyQueries.createQuery(relationshipClass)
                .addCriteria(new NamedPropertyCriteria("id")).getResultList();

        identityTypeIdProperty.get(0).setValue(relationshipType, idProperty.getValue(relationshipObject));

        List<?> identities = findChildRelationships(context, relationshipType);

        for (Object object : identities) {
            String descriptor = descriptorProperty.getValue(object).toString();

            List<Property<Object>> identityTypeProperty = PropertyQueries.createQuery(relationshipClass)
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getResultList();

            IdentityType identityType = null;

            if (identityProperty.getJavaClass().equals(String.class)) {
                identityType = context.getIdentityManager().lookupIdentityById(IdentityType.class,
                        identityProperty.getValue(object).toString());
            } else {
                identityType = convertToIdentityType(context, identityProperty.getValue(object));
            }

            identityTypeProperty.get(0).setValue(relationshipType, identityType);
        }

        populateRelationshipAttributes(context, relationshipType, relationshipObject);

        return relationshipType;
    }

    /**
     * <p>
     * Converts the given object to an instance of its corresponding {@link IdentityType}.
     * </p>
     *
     * @param entity
     * @return
     */
    private <T extends IdentityType> T convertToIdentityType(SecurityContext context, Object entity) {
        String discriminator = getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getValue(entity).toString();
        IdentityTypeHandler<? extends IdentityType> identityTypeManager = IdentityTypeHandlerFactory.getHandler(getConfig()
                .getIdentityTypeFromDiscriminator(discriminator));

        @SuppressWarnings("unchecked")
        T identityType = (T) identityTypeManager.createIdentityType(context, entity, this);

        populateIdentityTypeAttributes(context, identityType, entity);

        return identityType;
    }

    /**
     * <p>
     * Stores the specified {@link Attribute} for the given {@link IdentityType} entity.
     * </p>
     *
     * @param identity
     * @param userAttribute
     */
    private void storeRelationshipAttribute(SecurityContext context, Object identity,
                                            Attribute<? extends Serializable> userAttribute) {
        Serializable value = userAttribute.getValue();
        Serializable[] values = null;

        if (value.getClass().isArray()) {
            values = (Serializable[]) value;
        } else {
            values = new Serializable[]{value};
        }

        Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(
                PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP);
        Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

        for (Object attribValue : values) {
            Object newInstance = null;

            try {
                newInstance = getConfig().getRelationshipAttributeClass().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(getConfig().getRelationshipAttributeClass().getName(), e);
            }

            attributeNameProperty.setValue(newInstance, userAttribute.getName());
            attributeValueProperty.setValue(newInstance, Base64.encodeObject((Serializable) attribValue));
            attributeIdentityProperty.setValue(newInstance, identity);

            getEntityManager(context).persist(newInstance);
        }
    }

    /**
     * <p>
     * Removes the store attributes not present in the {@link Relationship} instance.
     * </p>
     *
     * @param relationship
     * @param identity
     */
    private void removeAttributes(SecurityContext context, Relationship relationship, Object identity) {
        List<?> storedAttributes = findRelationshipAttributes(context, identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getValue(attribute)
                    .toString();

            if (relationship.getAttribute(attributeName) == null) {
                getEntityManager(context).remove(attribute);
            }
        }
    }

    /**
     * <p>
     * Removes the store attributes not present in the {@link IdentityType} instance.
     * </p>
     *
     * @param identityType
     * @param identity
     */
    private void removeAttributes(SecurityContext context, IdentityType identityType, Object identity) {
        List<?> storedAttributes = findAllIdentityTypeAttributes(context, identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getValue(attribute).toString();

            if (identityType.getAttribute(attributeName) == null) {
                getEntityManager(context).remove(attribute);
            }
        }
    }

    /**
     * <p>
     * Returns all stored attributes for the given {@link IdentityType} that matchs the {@link Attribute} name.
     * </p>
     *
     * @param identityType
     * @param name
     * @return
     */
    private List<?> findIdentityTypeAttributes(SecurityContext context, IdentityType identityType, String name) {
        if (identityType.getId() == null) {
            throw MESSAGES.nullArgument("IdentityType identifier");
        }

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()),
                identityType.getId()));
        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getName()), name));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>
     * Returns all stored attributes for the given {@link Relationship} that matchs the {@link Attribute} name.
     * </p>
     *
     * @param relationship
     * @param idValue
     * @param attribute
     * @return
     */
    private List<?> findRelationshipAttributes(SecurityContext context, Relationship relationship,
                                               Attribute<? extends Serializable> attribute) {
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(
                PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipAttributeClass());
        Root<?> root = criteria.from(getConfig().getRelationshipAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName()),
                relationship.getId()));

        predicates
                .add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getName()),
                        attribute.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>
     * Returns all stored attributes for the given {@link IdentityType} entity.
     * </p>
     *
     * @param object
     * @return
     */
    private List<?> findAllIdentityTypeAttributes(SecurityContext context, Object object) {
        Class<?> attributeClass = getConfig().getAttributeClass();
        String identityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY).getName();

        EntityManager em = getEntityManager(context);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeClass);
        Root<?> root = criteria.from(attributeClass);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(identityProperty), object));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>
     * Returns all stored attributes for the given {@link IdentityType} entity.
     * </p>
     *
     * @param object
     * @return
     */
    private List<?> findRelationshipAttributes(SecurityContext context, Object object) {
        Class<?> attributeClass = getConfig().getRelationshipAttributeClass();
        String identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName();

        EntityManager em = getEntityManager(context);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeClass);
        Root<?> root = criteria.from(attributeClass);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(identityProperty), object));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>
     * Lookup a stored {@link Relationship} using the id.
     * </p>
     *
     * @param id
     * @return
     */
    private Object lookupRelationshipObjectById(SecurityContext context, String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipClass());
        Root<?> root = criteria.from(getConfig().getRelationshipClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName()), id));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            throw MESSAGES.attributedTypeNotFoundWithId(Relationship.class, id, context.getPartition());
        } else {
            return results.get(0);
        }
    }

    /**
     * <p>
     * Removes all relationships associated with the given {@link IdentityType}.
     * </p>
     *
     * @param entity
     */
    private void removeIdentityTypeRelationships(SecurityContext context, Object entity) {
        // First we build a list of all the relationships that the specified identity
        // is participating in
        if (getConfig().getRelationshipClass() != null) {
            List<?> relationshipsToRemove = findIdentityTypeRelationships(context,
                    getConfig().getModelPropertyValue(String.class, entity, PropertyType.IDENTITY_ID));

            // Now that we have the list, we can iterate through and remove the records
            for (Object relationship : relationshipsToRemove) {
                remove(context, convertToRelationshipType(context, relationship));
            }
        }
    }

    /**
     * <p>
     * Returns all relationships associated with the given {@link IdentityType} using its identifier.
     * </p>
     *
     * @param identityTypeEntity
     * @return
     */
    private List<?> findIdentityTypeRelationships(SecurityContext context, String identityTypeId) {
        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);

        if (identityTypeProperty.getJavaClass().equals(String.class)) {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()), identityTypeId));
        } else {
            criteria.where(builder.equal(root.get(identityTypeProperty.getName()),
                    lookupIdentityObjectById(context, identityTypeId)));
        }

        List<Object> relationships = new ArrayList<Object>();

        List<?> result = em.createQuery(criteria).getResultList();

        for (Object object : result) {
            relationships.add(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getValue(object));
        }

        return relationships;

    }

    /**
     * <p>
     * Removes all attributes for given {@link IdentityType}.
     * </p>
     *
     * @param object
     */
    private void removeIdentityTypeAttributes(SecurityContext context, Object object) {
        EntityManager em = getEntityManager(context);

        if (getConfig().getAttributeClass() != null) {
            List<?> results = findAllIdentityTypeAttributes(context, object);
            for (Object result : results) {
                em.remove(result);
            }
        }
    }

    /**
     * <p>
     * Updates the attributes for the given {@link IdentityType}.
     * </p>
     *
     * @param identityType
     * @param entity
     */
    private void updateIdentityTypeAttributes(SecurityContext context, IdentityType identityType, Object entity) {
        if (getConfig().supportsFeature(FeatureGroup.attribute, null)) {
            Collection<Attribute<? extends Serializable>> attributes = identityType.getAttributes();

            if (attributes != null) {
                for (Attribute<? extends Serializable> attribute : attributes) {
                    setAttribute(context, identityType, attribute);
                }
            }

            removeAttributes(context, identityType, entity);
        }
    }

    /**
     * <p>
     * Updates the attributes for the given {@link IdentityType}.
     * </p>
     *
     * @param relationship
     * @param identity
     */
    private void updateRelationshipAttributes(SecurityContext context, Relationship relationship, Object identity) {
        List<Property<Serializable>> attributeProperties = PropertyQueries.<Serializable>createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

        for (Property<Serializable> attributeProperty : attributeProperties) {
            relationship.setAttribute(new Attribute<Serializable>(attributeProperty.getName(), attributeProperty
                    .getValue(relationship)));
        }

        Collection<Attribute<? extends Serializable>> attributes = relationship.getAttributes();

        if (attributes != null && !attributes.isEmpty()) {
            EntityManager em = getEntityManager(context);

            for (Attribute<? extends Serializable> attribute : attributes) {
                // remove the attributes to persist them again. Only the current attribute, not all.
                List<?> results = findRelationshipAttributes(context, relationship, attribute);

                for (Object object : results) {
                    em.remove(object);
                }

                storeRelationshipAttribute(context, identity, attribute);
            }

            removeAttributes(context, relationship, identity);
        }
    }

    /**
     * <p>
     * Populates the given {@link IdentityType} instance with the attributes associated with the given entity.
     * </p>
     *
     * @param identityType
     * @param entity
     */
    private void populateIdentityTypeAttributes(SecurityContext context, IdentityType identityType, Object entity) {
        for (MappedAttribute attrib : getConfig().getAttributeProperties().values()) {
            Member member = attrib.getAttributeProperty().getMember();
            String mappedName = null;
            Object value = null;

            if (member instanceof Field) {
                Field field = (Field) member;
                IDMAttribute annotation = field.getAnnotation(IDMAttribute.class);

                field.setAccessible(true);

                mappedName = annotation.name();

                try {
                    value = field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new IdentityManagementException("Could not get value from field [" + field + "].", e);
                }
            }

            identityType.setAttribute(new Attribute<Serializable>(mappedName, (Serializable) value));
        }

        if (getConfig().getAttributeClass() != null) {
            List<?> attributes = findAllIdentityTypeAttributes(context, entity);

            populateAttributes(identityType, attributes);
        }
    }

    private void populateAttributes(IdentityType identityType, List<?> attributes) {
        for (Object object : attributes) {
            Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME);
            Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE);

            String attributeName = attributeNameProperty.getValue(object).toString();

            Attribute<Serializable> attribute = identityType.getAttribute(attributeName);

            Serializable attribValue = (Serializable) Base64.decodeToObject(attributeValueProperty.getValue(object).toString());

            if (attribute == null) {
                attribute = new Attribute<Serializable>(attributeName, attribValue);
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

                    newValues[newValues.length - 1] = attribValue;

                    attribute.setValue(newValues);

                }
            }

            identityType.setAttribute(attribute);
        }
    }

    /**
     * <p>
     * Populates the given {@link Relationship} instance with the attributes associated with the given entity.
     * </p>
     *
     * @param relationshipType
     * @param relationship
     */
    private void populateRelationshipAttributes(SecurityContext context, Relationship relationshipType, Object relationship) {
        if (getConfig().getRelationshipAttributeClass() != null) {
            List<?> results = findRelationshipAttributes(context, relationship);

            if (!results.isEmpty()) {
                for (Object object : results) {
                    Property<Object> attributeNameProperty = getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
                    Property<Object> attributeValueProperty = getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

                    String attribName = (String) attributeNameProperty.getValue(object);
                    Serializable attribValue = (Serializable) Base64.decodeToObject(attributeValueProperty.getValue(object)
                            .toString());

                    List<Property<Serializable>> attributeProperties = PropertyQueries
                            .<Serializable>createQuery(relationshipType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

                    Property<Serializable> relationshipAttributeProperty = null;

                    for (Property<Serializable> attributeProperty : attributeProperties) {
                        String propertyName = attributeProperty.getName();

                        if (propertyName.equals(attribName)) {
                            relationshipAttributeProperty = attributeProperty;
                            break;
                        }
                    }

                    if (relationshipAttributeProperty != null) {
                        relationshipAttributeProperty.setValue(relationshipType, attribValue);
                    } else {
                        Attribute<Serializable> identityTypeAttribute = relationshipType.getAttribute(attribName);

                        if (identityTypeAttribute == null) {
                            identityTypeAttribute = new Attribute<Serializable>(attribName, attribValue);
                            relationshipType.setAttribute(identityTypeAttribute);
                        } else {
                            // if it is a multi-valued attribute
                            if (identityTypeAttribute.getValue() != null) {
                                Serializable[] values = null;

                                if (identityTypeAttribute.getValue().getClass().isArray()) {
                                    values = (Serializable[]) identityTypeAttribute.getValue();
                                } else {
                                    values = (Serializable[]) Array.newInstance(attribValue.getClass(), 1);
                                    values[0] = identityTypeAttribute.getValue();
                                }

                                Serializable[] newValues = Arrays.copyOf(values, values.length + 1);

                                newValues[newValues.length - 1] = attribValue;

                                identityTypeAttribute.setValue(newValues);

                                relationshipType.setAttribute(identityTypeAttribute);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addRelationship(SecurityContext context, Relationship relationship) {
        relationship.setId(context.getIdGenerator().generate());

        Object entity = null;

        try {
            entity = getConfig().getRelationshipClass().newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(getConfig().getRelationshipClass().getName(), e);
        }

        getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).setValue(entity, relationship.getId());
        getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).setValue(entity, relationship.getClass().getName());

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType>createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(IdentityProperty.class)).getResultList();

        EntityManager em = getEntityManager(context);

        em.persist(entity);

        for (Property<IdentityType> prop : props) {
            Object relationshipIdentity = null;

            try {
                relationshipIdentity = getConfig().getRelationshipIdentityClass().newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(getConfig().getRelationshipIdentityClass().getName(), e);
            }

            IdentityType identityType = prop.getValue(relationship);

            if (identityType != null) {
                Object identityObject = null;

                try {
                    identityObject = lookupIdentityObjectById(context, identityType.getId());
                } catch (IdentityManagementException ignore) {
                    // if the identity object does not exists, use only its id.
                }

                Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);

                if (identityTypeProperty.getJavaClass().equals(String.class)) {
                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).setValue(relationshipIdentity,
                            identityType.getId());
                } else {
                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).setValue(relationshipIdentity,
                            identityObject);
                }

                getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).setValue(relationshipIdentity,
                        prop.getName());
                getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).setValue(relationshipIdentity,
                        entity);
            }

            em.persist(relationshipIdentity);
        }

        updateRelationshipAttributes(context, relationship, entity);

        em.flush();
    }

    private void removeIdentityType(SecurityContext context, AttributedType value) {
        IdentityType identityType = (IdentityType) value;

        Object entity = lookupIdentityObjectById(context, identityType.getId());

        EntityManager em = getEntityManager(context);

        IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

        handler.remove(context, entity, identityType, this);

        // Remove credentials
        removeCredentials(context, entity);
        // Remove attributes
        removeIdentityTypeAttributes(context, entity);
        // Remove relationships
        removeIdentityTypeRelationships(context, entity);

        // Remove the identity object itself
        em.remove(entity);
        em.flush();

        AbstractBaseEvent event = handler.raiseDeletedEvent(identityType);
        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
        context.getEventBridge().raiseEvent(event);
    }

    private void removeRelationship(SecurityContext context, Relationship relationship) {
        if (relationship.getId() == null) {
            DefaultRelationshipQuery<?> query = null;

            if (GroupRole.class.isInstance(relationship)) {
                GroupRole groupRole = (GroupRole) relationship;

                query = new DefaultRelationshipQuery<GroupRole>(context, GroupRole.class, this);

                query.setParameter(GroupRole.ASSIGNEE, groupRole.getAssignee());
                query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                query.setParameter(GroupRole.ROLE, groupRole.getRole());
            } else if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                query = new DefaultRelationshipQuery<Grant>(context, Grant.class, this);

                query.setParameter(Grant.ASSIGNEE, grant.getAssignee());
                query.setParameter(Grant.ROLE, grant.getRole());
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                query = new DefaultRelationshipQuery<GroupMembership>(context, GroupMembership.class, this);

                query.setParameter(GroupMembership.MEMBER, groupMembership.getMember());
                query.setParameter(GroupMembership.GROUP, groupMembership.getGroup());
            }

            @SuppressWarnings("unchecked")
            List<Relationship> result = (List<Relationship>) fetchQueryResults(context, query, true);

            if (result.size() == 1) {
                relationship = result.get(0);
            } else if (result.size() > 1) {
                throw MESSAGES.relationshipAmbiguosFound(relationship);
            }
        }

        Object entity = lookupRelationshipObjectById(context, relationship.getId());

        List<?> childRelationships = findChildRelationships(context, relationship);

        EntityManager em = getEntityManager(context);

        for (Object object : childRelationships) {
            em.remove(object);
        }

        Object[] attributes = relationship.getAttributes().toArray();

        for (Object object : attributes) {
            Attribute<?> attribute = (Attribute<?>) object;
            relationship.removeAttribute(attribute.getName());
        }

        removeAttributes(context, relationship, entity);

        em.remove(entity);
        em.flush();
    }

    /**
     * <p>
     * Finds all child relationships for the given {@link Relationship}.
     * </p>
     *
     * @param relationship
     * @return
     */
    private List<?> findChildRelationships(SecurityContext context, Relationship relationship) {
        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        Join<?, ?> join = root.join(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName()),
                relationship.getId()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends Relationship> List<T> fetchQueryResults(SecurityContext context, RelationshipQuery<T> query,
                                                               boolean matchExactGroup) {
        List<T> result = new ArrayList<T>();

        EntityManager em = getEntityManager(context);

        List<?> queryResult = new ArrayList<Object>();

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

                queryResult = findIdentityTypeRelationships(context, identityId);
            }
        } else {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipClass());
            Root<?> root = criteria.from(getConfig().getRelationshipClass());

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).getName()),
                    query.getRelationshipType().getName()));

            Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
            Property<Object> descriptorProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR);
            Property<Object> relationshipProperty = getConfig().getModelProperty(
                    PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

            Set<Entry<QueryParameter, Object[]>> parameters = query.getParameters().entrySet();

            for (Entry<QueryParameter, Object[]> entry : parameters) {
                QueryParameter queryParameter = entry.getKey();
                Object[] values = entry.getValue();

                if (entry.getKey() instanceof RelationshipQueryParameter) {
                    RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) entry.getKey();

                    for (Object object : values) {
                        IdentityType identityType = (IdentityType) object;

                        if (identityType != null) {
                            IdentityQuery<? extends IdentityType> identityQuery = context.getIdentityManager().createIdentityQuery(identityType.getClass());

                            identityQuery.setParameter(IdentityType.ID, identityType.getId());
                            identityQuery.setParameter(IdentityType.PARTITION, identityType.getPartition());

                            List<? extends IdentityType> identityQueryResult = identityQuery.getResultList();

                            if (identityQueryResult.isEmpty()) {
                                return result;
                            }

                            identityType = identityQueryResult.get(0);

                            if (Role.class.isInstance(identityType) || Group.class.isInstance(identityType)) {
                                if (Realm.class.isInstance(context.getPartition()) && !context.getPartition().equals(identityType.getPartition())) {
                                    if (!Tier.class.isInstance(identityType.getPartition())) {
                                        return result;
                                    }
                                }
                            }

                            List<String> objects = new ArrayList<String>();

                            objects.add(identityType.getId());

                            if (Group.class.isInstance(identityType) && !matchExactGroup) {
                                List<Group> groupParents = getParentGroups(context, (Group) identityType);

                                for (Group group : groupParents) {
                                    objects.add(group.getId());
                                }
                            }

                            Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipIdentityClass());
                            Root fromProject = subquery.from(getConfig().getRelationshipIdentityClass());
                            subquery.select(fromProject.get(relationshipProperty.getName()));

                            Predicate conjunction = builder.conjunction();

                            conjunction.getExpressions().add(
                                    builder.equal(fromProject.get(descriptorProperty.getName()),
                                            identityTypeParameter.getName()));

                            if (identityProperty.getJavaClass().equals(String.class)) {
                                conjunction.getExpressions().add(
                                        builder.in(fromProject.get(identityProperty.getName())).value(objects));
                            } else {
                                List<Object> identityObjects = new ArrayList<Object>();

                                for (String id : objects) {
                                    Object identityObject = null;

                                    try {
                                        identityObject = lookupIdentityObjectById(context, id);
                                    } catch (IdentityManagementException ime) {
                                        // we ignore, the type may not exists
                                    }

                                    if (identityObject != null) {
                                        identityObjects.add(identityObject);
                                    }
                                }

                                if (!identityObjects.isEmpty()) {
                                    conjunction.getExpressions().add(
                                            builder.in(fromProject.get(identityProperty.getName())).value(identityObjects));
                                }
                            }

                            subquery.where(conjunction);

                            predicates.add(builder.in(root).value(subquery));
                        } else {
                            return result;
                        }
                    }
                }

                if (queryParameter instanceof AttributeParameter) {
                    AttributeParameter customParameter = (AttributeParameter) queryParameter;

                    Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipAttributeClass());
                    Root fromProject = subquery.from(getConfig().getRelationshipAttributeClass());

                    subquery.select(fromProject.get(getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName()));

                    Predicate conjunction = builder.conjunction();

                    Serializable[] valuesToSearch = new Serializable[values.length];

                    for (int i = 0; i < values.length; i++) {
                        valuesToSearch[i] = Base64.encodeObject((Serializable) values[i]);
                    }

                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(getConfig()
                                    .getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getName()), customParameter
                                    .getName()));
                    conjunction.getExpressions().add(
                            (fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE).getName())
                                    .in((Object[]) valuesToSearch)));

                    subquery.where(conjunction);

                    subquery.groupBy(subquery.getSelection()).having(
                            builder.equal(builder.count(subquery.getSelection()), valuesToSearch.length));

                    predicates.add(builder.in(root).value(subquery));
                }
            }

            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            queryResult = em.createQuery(criteria).getResultList();
        }

        for (Object relationshipObject : queryResult) {
            result.add((T) convertToRelationshipType(context, relationshipObject));
        }

        return result;
    }

    private List<Group> getParentGroups(SecurityContext context, Group identityType) {
        IdentityQuery<Group> query = context.getIdentityManager().createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, identityType);

        return query.getResultList();
    }

    private Realm convertPartitionEntityToRealm(Object partitionObject) {
        Realm realm = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Realm.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);

                realm = new Realm(idProperty.getValue(partitionObject).toString());
            }
        }

        return realm;
    }

    private Tier convertPartitionEntityToTier(Object partitionObject) {
        Tier tier = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Tier.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
                tier = new Tier(idProperty.getValue(partitionObject).toString());
            }
        }

        return tier;
    }

    private <T extends CredentialStorage> T convertToCredentialStorage(SecurityContext context, Object instance,
                                                                       Class<T> storageClass) {
        T storage = null;

        if (instance != null) {
            try {
                storage = storageClass.newInstance();
            } catch (Exception e) {
                throw MESSAGES.instantiationError(storageClass.getName(), e);
            }

            Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);
            Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

            List<Property<Object>> effectiveDateProperty = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria("effectiveDate")).getResultList();

            effectiveDateProperty.get(0).setValue(storage, effectiveProperty.getValue(instance));

            List<Property<Object>> expiryDateProperty = PropertyQueries.createQuery(storageClass)
                    .addCriteria(new NamedPropertyCriteria("expiryDate")).getResultList();

            expiryDateProperty.get(0).setValue(storage, expiryProperty.getValue(instance));

            EntityManager em = getEntityManager(context);

            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> attributeCriteria = builder.createQuery(getConfig().getCredentialAttributeClass());
            Root<?> attributeRoot = attributeCriteria.from(getConfig().getCredentialAttributeClass());
            List<Predicate> attributePredicates = new ArrayList<Predicate>();

            Property<Object> attributeCredential = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

            attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), instance));

            attributeCriteria.where(attributePredicates.toArray(new Predicate[attributePredicates.size()]));

            List<?> attributes = em.createQuery(attributeCriteria).getResultList();

            Property<Object> attributeName = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME);
            Property<Object> attributeValue = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE);

            for (Object attribute : attributes) {
                String name = attributeName.getValue(attribute).toString();
                String value = attributeValue.getValue(attribute).toString();

                List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storageClass)
                        .addCriteria(new NamedPropertyCriteria(name)).getResultList();

                if (annotatedTypes.isEmpty()) {
                    throw new IdentityManagementException("Could not find property [" + attributeName.getName()
                            + "] on CredentialStorage [" + storageClass.getName() + "].");
                } else if (annotatedTypes.size() > 1) {
                    throw new IdentityManagementException("Ambiguos property [" + attributeName.getName()
                            + "] on CredentialStorage [" + storageClass.getName() + "].");
                }

                Property<Object> property = annotatedTypes.get(0);

                property.setValue(storage, Base64.decodeToObject(value));
            }
        }

        return storage;
    }

    /**
     * <p>
     * Returns the last stored credential for the given {@link Agent} considering the given storageClass. The last credential is
     * the one which the effectiveDate is more close to the current date.
     * </p>
     *
     * @param agent
     * @param storageClass
     * @return
     */
    private <T> Object retrieveLastCredentialEntity(SecurityContext context, Agent agent, Class<T> storageClass) {
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        EntityManager em = getEntityManager(context);

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(context, agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date>get(effectiveProperty.getName()), new Date()));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        Object lastCredential = null;

        List<?> result = em.createQuery(criteria).getResultList();

        if (!result.isEmpty()) {
            lastCredential = result.get(0);
        }

        return lastCredential;
    }

    private void checkCredentialClassProvided() {
        if (getConfig().getCredentialClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store credentials.");
        }
    }

    private void checkIdentityTypeClassProvided() {
        if (getConfig().getIdentityClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store identity types.");
        }
    }

    private void checkPartitionClassProvided() {
        if (getConfig().getPartitionClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store partitions.");
        }
    }

    private void checkRelationshipClassProvided() {
        if (getConfig().getRelationshipClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store relationships.");
        }

        if (getConfig().getRelationshipIdentityClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store relationships identity types.");
        }

        if (getConfig().getRelationshipAttributeClass() == null) {
            throw new IdentityManagementException("No class Entity class provided to store relationships attributes.");
        }
    }

    private void removeCredentials(SecurityContext context, Object object) {
        if (getConfig().getCredentialClass() != null) {
            EntityManager em = getEntityManager(context);
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
            Root<?> root = criteria.from(getConfig().getCredentialClass());
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY).getName()),
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();

            for (Object credential : results) {
                CriteriaQuery<?> attributeCriteria = builder.createQuery(getConfig().getCredentialAttributeClass());
                Root<?> attributeRoot = attributeCriteria.from(getConfig().getCredentialAttributeClass());
                List<Predicate> attributePredicates = new ArrayList<Predicate>();

                Property<Object> attributeCredential = getConfig().getModelProperty(
                        PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

                attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), credential));

                attributeCriteria.where(attributePredicates.toArray(new Predicate[attributePredicates.size()]));

                List<?> attributes = em.createQuery(attributeCriteria).getResultList();

                for (Object attribute : attributes) {
                    em.remove(attribute);
                }

                em.remove(credential);
            }
        }
    }

}