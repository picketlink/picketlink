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

import static org.picketlink.idm.IDMMessages.MESSAGES;

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
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
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
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.PropertyType;
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
import org.picketlink.idm.model.annotation.RelationshipAttribute;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
 *
 * @author Shane Bryzak
 * @author Pedro Silva
 *
 */
@CredentialHandlers({ PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class })
public class JPAIdentityStore implements IdentityStore<JPAIdentityStoreConfiguration>, CredentialStore, PartitionStore {

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
    private SecurityContext context;

    private boolean initialized;

    private Realm defaultRealm;

    public void setup(JPAIdentityStoreConfiguration config, SecurityContext context) {
        if (!initialized) {
            this.config = config;
            this.context = context;

            this.defaultRealm = getRealm(Realm.DEFAULT_REALM);

            if (this.defaultRealm == null) {
                this.defaultRealm = new Realm(Realm.DEFAULT_REALM);
                createPartition(this.defaultRealm);
            }

            this.initialized = true;
        }
    }

    @Override
    public JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    @Override
    public SecurityContext getContext() {
        return this.context;
    }

    @Override
    public void add(AttributedType value) {
        if (value instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            IdentityType identityType = (IdentityType) value;

            IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

            Object entity = handler.createEntity(getContext(), identityType, this);

            EntityManager em = getEntityManager();

            em.persist(entity);
            em.flush();

            updateIdentityTypeAttributes(identityType, entity);

            AbstractBaseEvent event = handler.raiseCreatedEvent(identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (value instanceof Relationship) {
            checkRelationshipClassProvided();

            addRelationship((Relationship) value);
        }
    }

    @Override
    public void createPartition(Partition partition) {
        checkPartitionClassProvided();

        Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
        Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        Class<?> partitionClass = getConfig().getPartitionClass();
        Object partitionObject = null;

        try {
            partitionObject = partitionClass.newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(partitionClass.getName(), e);
        }

        partition.setId(getContext().getIdGenerator().generate());

        idProperty.setValue(partitionObject, partition.getId());
        nameProperty.setValue(partitionObject, partition.getName());
        typeProperty.setValue(partitionObject, partition.getClass().getName());

        if (Tier.class.isInstance(partition)) {
            Tier tier = (Tier) partition;
            Tier parentTier = tier.getParent();

            if (parentTier != null) {
                Property<Object> parentProperty = getConfig().getModelProperty(PropertyType.PARTITION_PARENT);
                parentProperty.setValue(partitionObject, lookupPartitionObject(parentTier));
            }
        }

        EntityManager em = getEntityManager();

        em.persist(partitionObject);
        em.flush();
    }

    @Override
    public Realm getRealm(String realmName) {
        checkPartitionClassProvided();

        return convertPartitionEntityToRealm(lookupPartitionEntityByName(Realm.class, realmName));
    }

    @Override
    public Tier getTier(String tierName) {
        checkPartitionClassProvided();

        return convertPartitionEntityToTier(lookupPartitionEntityByName(Tier.class, tierName));
    }

    @Override
    public void removePartition(Partition partition) {
        checkPartitionClassProvided();

        Object partitionObject = lookupPartitionObject(partition);

        if (partitionObject == null) {
            throw MESSAGES.partitionNotFoundWithId(partition.getId());
        }

        EntityManager entityManager = getEntityManager();

        List<?> associatedIdentityTypes = getIdentityTypesForPartition(partitionObject);

        if (!associatedIdentityTypes.isEmpty()) {
            throw MESSAGES.partitionCouldNotRemoveWithIdentityTypes(partition);
        }

        List<?> childPartitions = getChildPartitions(partitionObject);

        if (!childPartitions.isEmpty()) {
            throw MESSAGES.partitionCouldNotRemoveWithChilds(partition);
        }

        entityManager.remove(partitionObject);
        entityManager.flush();
    }

    @Override
    public void update(AttributedType attributedType) {
        if (attributedType instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            IdentityType identityType = (IdentityType) attributedType;

            Object entity = lookupIdentityObjectById(identityType.getId());

            IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

            handler.populateEntity(getContext(), entity, identityType, this);

            updateIdentityTypeAttributes(identityType, entity);

            EntityManager em = getEntityManager();

            em.merge(entity);
            em.flush();

            IdentityType updatedIdentityType = convertToIdentityType(entity);

            cacheIdentityType(updatedIdentityType);

            AbstractBaseEvent event = handler.raiseUpdatedEvent(identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identityType);
            getContext().getEventBridge().raiseEvent(event);
        } else if (attributedType instanceof Relationship) {
            checkRelationshipClassProvided();

            Relationship relationship = (Relationship) attributedType;

            Object entity = lookupRelationshipObjectById(relationship.getId());

            updateRelationshipAttributes(relationship, entity);

            EntityManager em = getEntityManager();

            em.merge(entity);
            em.flush();
        }

    }

    @Override
    public void remove(AttributedType attributedType) {
        if (attributedType instanceof IdentityType) {
            checkIdentityTypeClassProvided();

            removeIdentityType(attributedType);
        } else if (attributedType instanceof Relationship) {
            checkRelationshipClassProvided();

            Relationship relationship = (Relationship) attributedType;

            removeRelationship(relationship);
        }
    }

    @Override
    public User getUser(String loginName) {
        if (Realm.class.isInstance(context.getPartition())) {
            Realm realm = (Realm) context.getPartition();

            if (loginName == null) {
                return null;
            }

            User user = getContext().getCache().lookupUser(realm, loginName);

            // If the cache doesn't have a reference to the User, we have to look up it's identity object
            // and create a User instance based on it
            if (user == null) {
                DefaultIdentityQuery<User> defaultIdentityQuery = new DefaultIdentityQuery<User>(User.class, this);

                defaultIdentityQuery.setParameter(User.LOGIN_NAME, loginName);

                List<User> resultList = defaultIdentityQuery.getResultList();

                if (!resultList.isEmpty()) {
                    user = resultList.get(0);
                }
            }

            return user;
        } else {
            // FIXME throw a proper exception
            throw new RuntimeException();
        }
    }

    @Override
    public Group getGroup(String groupPath) {
        if (groupPath == null) {
            return null;
        }

        if (!groupPath.startsWith("/")) {
            groupPath = "/" + groupPath;
        }

        // Check the cache first
        Partition partition = getContext().getPartition();
        Group group = getContext().getCache().lookupGroup(partition, groupPath);

        if (group == null) {
            DefaultIdentityQuery<Group> defaultIdentityQuery = new DefaultIdentityQuery<Group>(Group.class, this);

            defaultIdentityQuery.setParameter(Group.PATH, groupPath);

            List<Group> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                group = resultList.get(0);
            }
        }

        return group;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        if (name == null || parent == null) {
            return null;
        }

        String path = "/" + name;

        if (parent != null) {
            Object storedParent = lookupIdentityObjectById(parent.getId());

            if (storedParent == null) {
                throw MESSAGES.groupParentNotFoundWithId(parent.getId(), context.getPartition());
            }

            path = getConfig().getModelProperty(PropertyType.GROUP_PATH).getValue(storedParent) + path;
        }

        return getGroup(path);
    }

    @Override
    public Role getRole(String name) {
        if (name == null) {
            return null;
        }

        // Check the cache first
        Partition partition = getContext().getPartition();
        Role role = getContext().getCache().lookupRole(partition, name);

        // If the cache doesn't have a reference to the Role, we have to look up it's identity object
        // and create a Role instance based on it
        if (role == null) {
            DefaultIdentityQuery<Role> defaultIdentityQuery = new DefaultIdentityQuery<Role>(Role.class, this);

            defaultIdentityQuery.setParameter(Role.NAME, name);

            List<Role> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                role = resultList.get(0);
            }
        }

        return role;

    }

    @Override
    public Agent getAgent(String loginName) {
        if (Realm.class.isInstance(context.getPartition())) {
            Realm realm = (Realm) context.getPartition();

            if (loginName == null) {
                return null;
            }

            // Check the cache first

            Agent agent = null; // = getContext().getCache().lookupAgent(realm, loginName);

            // If the cache doesn't have a reference to the User, we have to look up it's identity object
            // and create a User instance based on it
            if (agent == null) {
                DefaultIdentityQuery<Agent> defaultIdentityQuery = new DefaultIdentityQuery<Agent>(Agent.class, this);

                defaultIdentityQuery.setParameter(Agent.LOGIN_NAME, loginName);

                List<Agent> resultList = defaultIdentityQuery.getResultList();

                if (!resultList.isEmpty()) {
                    agent = resultList.get(0);
                } else {
                    agent = getUser(loginName);
                }
            }

            return agent;
        } else {
            // FIXME throw proper exception
            throw new RuntimeException();
        }
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        return fetchQueryResults(query, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        List<T> result = new ArrayList<T>();

        EntityManager em = getEntityManager();

        JPACriteriaQueryBuilder criteriaBuilder = new JPACriteriaQueryBuilder(this, identityQuery);

        List<Predicate> predicates = criteriaBuilder.getPredicates(getContext());

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
            T identityType = (T) convertToIdentityType(identity);

            cacheIdentityType(identityType);

            result.add(identityType);
        }

        return result;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
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
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(IdentityType identity, Attribute<? extends Serializable> attribute) {
        Serializable value = attribute.getValue();

        if (value == null) {
            return;
        }

        Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME);
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);
        Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE);

        List<?> storedAttributes = findIdentityTypeAttributes(identity, attribute.getName());

        // store a new attribute
        if (storedAttributes.isEmpty()) {
            Serializable[] values = null;

            if (value.getClass().isArray()) {
                values = (Serializable[]) value;
            } else {
                values = new Serializable[] { value };
            }

            Object entity = lookupIdentityObjectById(identity.getId());

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

                getEntityManager().persist(newAttribute);
            }
        } else {
            removeAttribute(identity, attribute.getName());
            setAttribute(identity, attribute);
        }
    }

    @Override
    public void removeAttribute(IdentityType identity, String name) {
        List<?> storedAttributes = findIdentityTypeAttributes(identity, name);

        for (Object storedAttribute : storedAttributes) {
            getEntityManager().remove(storedAttribute);
        }
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        List<?> attributes = findIdentityTypeAttributes(identityType, attributeName);

        populateAttributes(identityType, attributes);

        return identityType.getAttribute(attributeName);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> result = em.createQuery(criteria).getResultList();

        List<T> storages = new ArrayList<T>();

        for (Object object : result) {
            storages.add(convertToCredentialStorage(object, storageClass));
        }

        return storages;
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        checkCredentialClassProvided();
        return convertToCredentialStorage(retrieveLastCredentialEntity(agent, storageClass), storageClass);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
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

        Object agentInstance = lookupIdentityObjectById(agent.getId());

        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        identityTypeProperty.setValue(newCredential, agentInstance);
        typeProperty.setValue(newCredential, storage.getClass().getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        EntityManager em = getEntityManager();

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
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credential.getClass());
        }

        handler.update(agent, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this);

        if (handler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credentials.getClass());
        }

        handler.validate(credentials, this);
    }

    protected Partition convertPartitionEntityToPartition(Object partitionObject) {
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

    protected EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    /**
     * <p>
     * Lookup a stored {@link IdentityType} using the id.
     * </p>
     *
     * @param id
     * @return
     */
    protected Object lookupIdentityObjectById(String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        Join<?, ?> join = root.join(getConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName());

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), id));

        List<String> partitionIds = getAllowedPartitionIds(context.getPartition());

        predicates.add(builder.in(join.get(getConfig().getModelProperty(PropertyType.PARTITION_ID).getName())).value(
                partitionIds));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            throw MESSAGES.attributedTypeNotFoundWithId(IdentityType.class, id, context.getPartition());
        } else {
            return results.get(0);
        }
    }

    protected Object lookupPartitionObject(Partition partition) {
        return getEntityManager().find(getConfig().getPartitionClass(), partition.getId());
    }

    protected List<String> getAllowedPartitionIds(Partition currentPartition) {
        List<String> partitionIds = new ArrayList<String>();

        partitionIds.add(context.getPartition().getId());

        if (currentPartition == null) {
            return partitionIds;
        }

        partitionIds.add(currentPartition.getId());

        if (Tier.class.isInstance(currentPartition)) {
            Tier tier = (Tier) currentPartition;

            partitionIds.add(currentPartition.getId());

            if (tier != null) {
                partitionIds.addAll(getAllowedPartitionIds(tier.getParent()));
            }
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
    private <T extends Relationship> T convertToRelationshipType(Object relationshipObject) {
        Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_ID);
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

        List<?> identities = findChildRelationships(relationshipType);

        for (Object object : identities) {
            String descriptor = descriptorProperty.getValue(object).toString();

            List<Property<Object>> identityTypeProperty = PropertyQueries.createQuery(relationshipClass)
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getResultList();

            IdentityType identityType = getContext().getIdentityManager().lookupIdentityById(IdentityType.class,
                    identityProperty.getValue(object).toString());

            identityTypeProperty.get(0).setValue(relationshipType, identityType);
        }

        populateRelationshipAttributes(relationshipType, relationshipObject);

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
    private <T extends IdentityType> T convertToIdentityType(Object entity) {
        String discriminator = getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getValue(entity).toString();
        IdentityTypeHandler<? extends IdentityType> identityTypeManager = IdentityTypeHandlerFactory.getHandler(getConfig()
                .getIdentityTypeFromDiscriminator(discriminator));

        @SuppressWarnings("unchecked")
        T identityType = (T) identityTypeManager.createIdentityType(entity, this);

        populateIdentityTypeAttributes(identityType, entity);

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
    private void storeRelationshipAttribute(Object identity, Attribute<? extends Serializable> userAttribute) {
        Serializable value = userAttribute.getValue();
        Serializable[] values = null;

        if (value.getClass().isArray()) {
            values = (Serializable[]) value;
        } else {
            values = new Serializable[] { value };
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

            getEntityManager().persist(newInstance);
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
    private void removeAttributes(Relationship relationship, Object identity) {
        List<?> storedAttributes = findRelationshipAttributes(identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getValue(attribute)
                    .toString();

            if (relationship.getAttribute(attributeName) == null) {
                getEntityManager().remove(attribute);
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
    private void removeAttributes(IdentityType identityType, Object identity) {
        List<?> storedAttributes = findAllIdentityTypeAttributes(identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getValue(attribute).toString();

            if (identityType.getAttribute(attributeName) == null) {
                getEntityManager().remove(attribute);
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
    private List<?> findIdentityTypeAttributes(IdentityType identityType, String name) {
        if (identityType.getId() == null) {
            throw MESSAGES.nullArgument("IdentityType identifier");
        }

        EntityManager em = getEntityManager();

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
    private List<?> findRelationshipAttributes(Relationship relationship, Attribute<? extends Serializable> attribute) {
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(
                PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

        EntityManager em = getEntityManager();

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
    private List<?> findAllIdentityTypeAttributes(Object object) {
        Class<?> attributeClass = getConfig().getAttributeClass();
        String identityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY).getName();

        EntityManager em = getEntityManager();
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
    private List<?> findRelationshipAttributes(Object object) {
        Class<?> attributeClass = getConfig().getRelationshipAttributeClass();
        String identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName();

        EntityManager em = getEntityManager();
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
    private Object lookupRelationshipObjectById(String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

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
    private void removeIdentityTypeRelationships(Object entity) {
        // First we build a list of all the relationships that the specified identity
        // is participating in
        if (getConfig().getRelationshipClass() != null) {
            List<?> relationshipsToRemove = findIdentityTypeRelationships(getConfig().getModelPropertyValue(String.class,
                    entity, PropertyType.IDENTITY_ID));

            // Now that we have the list, we can iterate through and remove the records
            for (Object relationship : relationshipsToRemove) {
                remove(convertToRelationshipType(relationship));
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
    private List<?> findIdentityTypeRelationships(String identityTypeId) {
        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());

        criteria.where(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_ID).getName()),
                identityTypeId));

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
    private void removeIdentityTypeAttributes(Object object) {
        EntityManager em = getEntityManager();

        if (getConfig().getAttributeClass() != null) {
            List<?> results = findAllIdentityTypeAttributes(object);
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
    private void updateIdentityTypeAttributes(IdentityType identityType, Object entity) {
        if (getConfig().getFeatureSet().supports(FeatureGroup.attribute)) {
            Collection<Attribute<? extends Serializable>> attributes = identityType.getAttributes();

            if (attributes != null) {
                for (Attribute<? extends Serializable> attribute : attributes) {
                    setAttribute(identityType, attribute);
                }
            }

            removeAttributes(identityType, entity);
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
    private void updateRelationshipAttributes(Relationship relationship, Object identity) {
        List<Property<Serializable>> attributeProperties = PropertyQueries.<Serializable> createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipAttribute.class)).getResultList();

        for (Property<Serializable> attributeProperty : attributeProperties) {
            relationship.setAttribute(new Attribute<Serializable>(attributeProperty.getName(), attributeProperty
                    .getValue(relationship)));
        }

        Collection<Attribute<? extends Serializable>> attributes = relationship.getAttributes();

        if (attributes != null && !attributes.isEmpty()) {
            EntityManager em = getEntityManager();

            for (Attribute<? extends Serializable> attribute : attributes) {
                // remove the attributes to persist them again. Only the current attribute, not all.
                List<?> results = findRelationshipAttributes(relationship, attribute);

                for (Object object : results) {
                    em.remove(object);
                }

                storeRelationshipAttribute(identity, attribute);
            }

            removeAttributes(relationship, identity);
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
    private void populateIdentityTypeAttributes(IdentityType identityType, Object entity) {
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
            List<?> attributes = findAllIdentityTypeAttributes(entity);

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
    private void populateRelationshipAttributes(Relationship relationshipType, Object relationship) {
        if (getConfig().getRelationshipAttributeClass() != null) {
            List<?> results = findRelationshipAttributes(relationship);

            if (!results.isEmpty()) {
                for (Object object : results) {
                    Property<Object> attributeNameProperty = getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
                    Property<Object> attributeValueProperty = getConfig().getModelProperty(
                            PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

                    String attribName = (String) attributeNameProperty.getValue(object);
                    Serializable attribValue = (Serializable) Base64.decodeToObject(attributeValueProperty.getValue(object).toString()) ;

                    List<Property<Serializable>> attributeProperties = PropertyQueries
                            .<Serializable> createQuery(relationshipType.getClass())
                            .addCriteria(new AnnotatedPropertyCriteria(RelationshipAttribute.class)).getResultList();

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

    private void addRelationship(Relationship relationship) {
        relationship.setId(getContext().getIdGenerator().generate());

        Object entity = null;

        try {
            entity = getConfig().getRelationshipClass().newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(getConfig().getRelationshipClass().getName(), e);
        }

        getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).setValue(entity, relationship.getId());
        getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).setValue(entity, relationship.getClass().getName());

        List<Property<IdentityType>> props = PropertyQueries.<IdentityType> createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

        EntityManager em = getEntityManager();

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
                    identityObject = lookupIdentityObjectById(identityType.getId());
                } catch (IdentityManagementException ignore) {
                    // if the identity object does not exists, use only its id.
                }

                if (identityObject != null) {
                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).setValue(relationshipIdentity,
                            identityObject);
                }

                getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_ID).setValue(relationshipIdentity,
                        identityType.getId());

                getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).setValue(relationshipIdentity,
                        prop.getName());
                getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).setValue(relationshipIdentity,
                        entity);
            }

            em.persist(relationshipIdentity);
        }

        updateRelationshipAttributes(relationship, entity);
    }

    private void removeIdentityType(AttributedType value) {
        IdentityType identityType = (IdentityType) value;

        Object entity = lookupIdentityObjectById(identityType.getId());

        EntityManager em = getEntityManager();

        IdentityTypeHandler<IdentityType> handler = IdentityTypeHandlerFactory.getHandler(identityType.getClass());

        handler.remove(entity, identityType, this);

        // Remove credentials
        removeCredentials(entity);
        // Remove attributes
        removeIdentityTypeAttributes(entity);
        // Remove relationships
        removeIdentityTypeRelationships(entity);

        // Remove the identity object itself
        em.remove(entity);
        em.flush();

        IdentityType removedIdentityType = handler.createIdentityType(entity, this);

        AbstractBaseEvent event = handler.raiseDeletedEvent(identityType);
        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
        getContext().getEventBridge().raiseEvent(event);
    }

    private void removeRelationship(Relationship relationship) {
        if (relationship.getId() == null) {
            DefaultRelationshipQuery<?> query = null;

            if (GroupRole.class.isInstance(relationship)) {
                GroupRole groupRole = (GroupRole) relationship;

                query = new DefaultRelationshipQuery<GroupRole>(GroupRole.class, this);

                query.setParameter(GroupRole.ASSIGNEE, groupRole.getAssignee());
                query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                query.setParameter(GroupRole.ROLE, groupRole.getRole());
            } else if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                query = new DefaultRelationshipQuery<Grant>(Grant.class, this);

                query.setParameter(Grant.ASSIGNEE, grant.getAssignee());
                query.setParameter(Grant.ROLE, grant.getRole());
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                query = new DefaultRelationshipQuery<GroupMembership>(GroupMembership.class, this);

                query.setParameter(GroupMembership.MEMBER, groupMembership.getMember());
                query.setParameter(GroupMembership.GROUP, groupMembership.getGroup());
            }

            @SuppressWarnings("unchecked")
            List<Relationship> result = (List<Relationship>) fetchQueryResults(query, true);

            if (result.size() == 1) {
                relationship = result.get(0);
            } else if (result.size() > 1) {
                throw MESSAGES.relationshipAmbiguosFound(relationship);
            }
        }

        Object entity = lookupRelationshipObjectById(relationship.getId());

        List<?> childRelationships = findChildRelationships(relationship);

        EntityManager em = getEntityManager();

        for (Object object : childRelationships) {
            em.remove(object);
        }

        Object[] attributes = relationship.getAttributes().toArray();

        for (Object object : attributes) {
            Attribute<?> attribute = (Attribute<?>) object;
            relationship.removeAttribute(attribute.getName());
        }

        removeAttributes(relationship, entity);

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
    private List<?> findChildRelationships(Relationship relationship) {
        EntityManager em = getEntityManager();

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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query, boolean matchExactGroup) {
        List<T> result = new ArrayList<T>();

        EntityManager em = getEntityManager();

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

                queryResult = findIdentityTypeRelationships(identityId);
            }
        } else {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipClass());
            Root<?> root = criteria.from(getConfig().getRelationshipClass());

            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).getName()),
                    query.getRelationshipType().getName()));

            Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_ID);
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

                        identityType = getContext().getIdentityManager().lookupIdentityById(identityType.getClass(),
                                identityType.getId());

                        if (identityType != null) {
                            List<Object> objects = new ArrayList<Object>();

                            objects.add(identityType.getId());

                            if (Group.class.isInstance(identityType) && !matchExactGroup) {
                                List<Group> groupParents = getParentGroups((Group) identityType);

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
                            conjunction.getExpressions().add(
                                    builder.in(fromProject.get(identityProperty.getName())).value(objects));

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
            result.add((T) convertToRelationshipType(relationshipObject));
        }

        return result;
    }

    private List<Group> getParentGroups(Group identityType) {
        IdentityQuery<Group> query = getContext().getIdentityManager().createIdentityQuery(Group.class);

        query.setParameter(Group.HAS_MEMBER, identityType);

        return query.getResultList();
    }

    private void createDefaultRealm() {
        createPartition(new Realm(Realm.DEFAULT_REALM));
    }

    private Realm convertPartitionEntityToRealm(Object partitionObject) {
        Realm realm = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Realm.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);

                realm = new Realm(nameProperty.getValue(partitionObject).toString());

                realm.setId(idProperty.getValue(partitionObject).toString());
            }
        }

        return realm;
    }

    private Object lookupPartitionEntityByName(Class<? extends Partition> partitionType, String name) {
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        Class<?> partitionClass = getConfig().getPartitionClass();

        CriteriaQuery<?> criteria = builder.createQuery(partitionClass);
        Root<?> root = criteria.from(partitionClass);

        Predicate whereType = builder.equal(root.get(getConfig().getModelProperty(PropertyType.PARTITION_TYPE).getName()),
                partitionType.getName());
        Predicate whereName = builder
                .equal(root.get(getConfig().getModelProperty(PropertyType.PARTITION_NAME).getName()), name);

        criteria.where(whereName, whereType);

        Object partitionObject = null;

        try {
            partitionObject = entityManager.createQuery(criteria).getSingleResult();
        } catch (NonUniqueResultException nuoe) {
            throw new IdentityManagementException("Abiguous Tier found with the given name [" + name + "]");
        } catch (NoResultException ignore) {
        }

        return partitionObject;
    }

    private List<?> getChildPartitions(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getPartitionClass());
        Root<?> root = criteria.from(getConfig().getPartitionClass());

        Predicate wherePartition = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.PARTITION_PARENT).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }

    private List<?> getIdentityTypesForPartition(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());

        Predicate wherePartition = builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }

    private Tier convertPartitionEntityToTier(Object partitionObject) {
        Tier tier = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Tier.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getConfig().getModelProperty(PropertyType.PARTITION_NAME);
                Property<Object> parentProperty = getConfig().getModelProperty(PropertyType.PARTITION_PARENT);

                Object parentTierObject = parentProperty.getValue(partitionObject);

                if (parentTierObject != null) {
                    tier = new Tier(nameProperty.getValue(partitionObject).toString(),
                            convertPartitionEntityToTier(parentTierObject));
                } else {
                    tier = new Tier(nameProperty.getValue(partitionObject).toString());
                }

                tier.setId(idProperty.getValue(partitionObject).toString());
            }
        }

        return tier;
    }

    private <T extends CredentialStorage> T convertToCredentialStorage(Object instance, Class<T> storageClass) {
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

            EntityManager em = getEntityManager();

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
    private <T> Object retrieveLastCredentialEntity(Agent agent, Class<T> storageClass) {
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(builder.lessThanOrEqualTo(root.<Date> get(effectiveProperty.getName()), new Date()));

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

    private void removeCredentials(Object object) {
        if (getConfig().getCredentialClass() != null) {
            EntityManager em = getEntityManager();
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

    private void cacheIdentityType(IdentityType updatedIdentityType) {
        if (User.class.isInstance(updatedIdentityType)) {
            getContext().getCache().putUser((Realm) updatedIdentityType.getPartition(), (User) updatedIdentityType);
        } else if (Agent.class.isInstance(updatedIdentityType)) {
            getContext().getCache().putAgent((Realm) updatedIdentityType.getPartition(), (Agent) updatedIdentityType);
        } else if (Role.class.isInstance(updatedIdentityType)) {
            getContext().getCache().putRole(updatedIdentityType.getPartition(), (Role) updatedIdentityType);
        } else if (Group.class.isInstance(updatedIdentityType)) {
            getContext().getCache().putGroup(updatedIdentityType.getPartition(), (Group) updatedIdentityType);
        }
    }



}