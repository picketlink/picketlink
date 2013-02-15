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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
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
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
 * 
 * @author Shane Bryzak
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

    /**
     * The invocation context
     */
    private IdentityStoreInvocationContext context;

    private JPAPartitionStore partitionStore;
    private JPACredentialStore credentialStore;

    public void setup(JPAIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;

        this.partitionStore = new JPAPartitionStore(this);
        this.credentialStore = new JPACredentialStore(this);

        if (this.context.getRealm() == null) {
            this.context.setRealm(getRealm(Realm.DEFAULT_REALM));
        }
    }

    @Override
    public JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return context;
    }

    @Override
    public void add(AttributedType value) {
        if (value == null) {
            throw new IllegalArgumentException("value passed to IdentityStore.add() may not be null");
        }

        if (value instanceof IdentityType) {
            IdentityType identityType = (IdentityType) value;

            try {
                IdentityTypeHandler<IdentityType> handler = getConfig().getHandler(identityType.getClass());

                handler.validate(identityType, this);

                Object entity = handler.createEntity(identityType, this);

                EntityManager em = getEntityManager();

                em.persist(entity);
                em.flush();

                updateIdentityTypeAttributes(identityType, entity);

                AbstractBaseEvent event = handler.raiseCreatedEvent(identityType);
                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
                getContext().getEventBridge().raiseEvent(event);
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating IdentityType [" + identityType + "].", ex);
            }
        } else if (value instanceof Relationship) {
            if (getConfig().getRelationshipClass() == null) {
                throw new IdentityManagementException(
                        "No Relationship Entity class was provided. Relationships can not be stored.");
            }

            Relationship relationship = (Relationship) value;

            try {
                addRelationship(relationship);

                if (GroupRole.class.isInstance(relationship)) {
                    GroupRole groupRole = (GroupRole) relationship;

                    addRelationship(new Grant(groupRole.getMember(), groupRole.getRole()));
                    addRelationship(new GroupMembership(groupRole.getMember(), groupRole.getGroup()));
                }
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating Relationship [" + relationship + "].", ex);
            }
        }
    }

    private void addRelationship(Relationship relationship) {
        if (GroupMembership.class.isInstance(relationship)) {
            GroupMembership groupMembership = (GroupMembership) relationship;

            if (checkIfExists(groupMembership)) {
                return;
            }
        }

        relationship.setId(getContext().getIdGenerator().generate());

        Object entity = null;

        try {
            entity = getConfig().getRelationshipClass().newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Error instantiating relationship class ["
                    + getConfig().getRelationshipClass().getName() + "]", e);
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
                throw new IdentityManagementException("Error instantiating relationship identity class ["
                        + getConfig().getRelationshipIdentityClass().getName() + "]", e);
            }

            IdentityType identityType = prop.getValue(relationship);

            getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).setValue(relationshipIdentity,
                    lookupIdentityObjectById(identityType.getId()));
            getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).setValue(relationshipIdentity, prop.getName());
            getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP)
                    .setValue(relationshipIdentity, entity);
            em.persist(relationshipIdentity);
        }

        updateRelationshipAttributes(relationship, entity);
    }

    private boolean checkIfExists(GroupMembership groupMembership) {
        boolean has = false;

        RelationshipQuery<GroupMembership> query = new DefaultRelationshipQuery<GroupMembership>(GroupMembership.class, this);

        query.setParameter(GroupMembership.MEMBER, groupMembership.getMember());
        query.setParameter(GroupMembership.GROUP, groupMembership.getGroup());

        List<GroupMembership> result = fetchQueryResults(query, true);

        if (!result.isEmpty()) {
            if (result.get(0).getClass().equals(groupMembership.getClass())) {
                has = true;
            }
        }
        return has;
    }

    @Override
    public void createPartition(Partition partition) {
        this.partitionStore.createPartition(partition);
    }

    @Override
    public Realm getRealm(String realmName) {
        return this.partitionStore.getRealm(realmName);
    }

    @Override
    public Tier getTier(String tierName) {
        return this.partitionStore.getTier(tierName);
    }

    @Override
    public void removePartition(Partition partition) {
        this.partitionStore.removePartition(partition);
    }

    @Override
    public void update(AttributedType value) {
        if (value == null) {
            throw new IllegalArgumentException("value passed to IdentityStore.update() may not be null");
        }

        if (value instanceof IdentityType) {
            IdentityType identityType = (IdentityType) value;

            Object entity = lookupIdentityObjectById(identityType.getId());

            if (entity == null) {
                throw new IdentityManagementException("The specified identity object [" + identityType.getId()
                        + "] does not exist.");
            }

            IdentityTypeHandler<IdentityType> handler = getConfig().getHandler(identityType.getClass());

            handler.populateEntity(entity, identityType, this);

            updateIdentityTypeAttributes(identityType, entity);

            EntityManager em = getEntityManager();

            em.merge(entity);
            em.flush();

            AbstractBaseEvent event = handler.raiseUpdatedEvent(identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (value instanceof Relationship) {
            Relationship relationship = (Relationship) value;

            Object entity = lookupRelationshipObjectById(relationship.getId());

            if (entity == null) {
                throw new IdentityManagementException("The specified relationship object [" + relationship.getId()
                        + "] does not exist.");
            }

            updateRelationshipAttributes(relationship, entity);

            EntityManager em = getEntityManager();

            em.merge(entity);
            em.flush();
        }

    }

    @Override
    public void remove(AttributedType value) {
        if (value instanceof IdentityType) {
            removeIdentityType(value);
        } else if (value instanceof Relationship) {
            Relationship relationship = (Relationship) value;
            removeRelationship(relationship);
        }
    }

    private void removeIdentityType(AttributedType value) {
        IdentityType identityType = (IdentityType) value;

        Object entity = lookupIdentityObjectById(identityType.getId());

        EntityManager em = getEntityManager();

        IdentityTypeHandler<IdentityType> handler = getConfig().getHandler(identityType.getClass());

        handler.remove(entity, identityType, this);

        // Remove credentials
        this.credentialStore.removeCredentials(entity);
        // Remove attributes
        removeIdentityTypeAttributes(entity);
        // Remove relationships
        removeIdentityTypeRelationships(entity);

        // Remove the identity object itself
        em.remove(entity);
        em.flush();

        AbstractBaseEvent event = handler.raiseDeletedEvent(identityType);
        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
        getContext().getEventBridge().raiseEvent(event);
    }

    private void removeRelationship(Relationship relationship) {
        if (relationship.getId() == null) {
            DefaultRelationshipQuery<?> query = null;

            if (Grant.class.isInstance(relationship)) {
                Grant grant = (Grant) relationship;

                query = new DefaultRelationshipQuery<Grant>(Grant.class, this);

                query.setParameter(Grant.ASSIGNEE, grant.getAssignee());
                query.setParameter(Grant.ROLE, grant.getRole());
            } else if (GroupMembership.class.isInstance(relationship)) {
                GroupMembership groupMembership = (GroupMembership) relationship;

                query = new DefaultRelationshipQuery<GroupMembership>(GroupMembership.class, this);

                query.setParameter(GroupMembership.MEMBER, groupMembership.getMember());
                query.setParameter(GroupMembership.GROUP, groupMembership.getGroup());

                if (GroupRole.class.isInstance(relationship)) {
                    GroupRole groupRole = (GroupRole) groupMembership;

                    query.setParameter(GroupRole.MEMBER, groupRole.getMember());
                    query.setParameter(GroupRole.GROUP, groupRole.getGroup());
                    query.setParameter(GroupRole.ROLE, groupRole.getRole());
                }
            }

            @SuppressWarnings("unchecked")
            List<Relationship> result = (List<Relationship>) fetchQueryResults(query, true);

            if (result.isEmpty()) {
                throw new IdentityManagementException("No relationship found to remove.");
            } else if (result.size() > 1) {
                throw new IdentityManagementException("Ambiguos relationship found.");
            }

            relationship = result.get(0);
        }
        
        Object entity = lookupRelationshipObjectById(relationship.getId());

        if (entity == null) {
            throw new IdentityManagementException("The specified relationship object [" + relationship.getId()
                    + "] does not exist.");
        }

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

    @Override
    public User getUser(String loginName) {
        if (loginName == null) {
            return null;
        }

        // Check the cache first
        User user = getContext().getCache().lookupUser(context.getRealm(), loginName);

        // If the cache doesn't have a reference to the User, we have to look up it's identity object
        // and create a User instance based on it
        if (user == null) {
            DefaultIdentityQuery<User> defaultIdentityQuery = new DefaultIdentityQuery<User>(User.class, this);

            defaultIdentityQuery.setParameter(User.LOGIN_NAME, loginName);

            List<User> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                user = resultList.get(0);
            }

            getContext().getCache().putUser(context.getRealm(), user);
        }

        return user;
    }

    @Override
    public Group getGroup(String groupPath) {
        if (groupPath == null) {
            return null;
        }

        if (groupPath.indexOf('/') == -1) {
            groupPath = "/" + groupPath;
        }

        // Check the cache first
        Realm partition = context.getRealm();
        Group group = getContext().getCache().lookupGroup(partition, groupPath);

        if (group == null) {
            DefaultIdentityQuery<Group> defaultIdentityQuery = new DefaultIdentityQuery<Group>(Group.class, this);

            defaultIdentityQuery.setParameter(Group.PATH, groupPath);

            List<Group> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                group = resultList.get(0);
            }

            getContext().getCache().putGroup(partition, group);
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
            if (parent.getId() == null) {
                throw new IdentityManagementException("No identifier specified for the parent group.");
            }

            Object storedParent = lookupIdentityObjectById(parent.getId());

            if (storedParent == null
                    || !getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getValue(storedParent)
                            .equals(getConfig().getIdentityTypeGroup())) {
                throw new IdentityManagementException("No parent group found with the given identifier [" + parent.getId()
                        + "]");
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
        Realm partition = context.getRealm();
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

            getContext().getCache().putRole(partition, role);
        }

        return role;

    }

    @Override
    public Agent getAgent(String loginName) {
        if (loginName == null) {
            return null;
        }

        // Check the cache first
        Realm partition = context.getRealm();
        Agent agent = getContext().getCache().lookupAgent(partition, loginName);

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

            getContext().getCache().putAgent(partition, agent);
        }

        return agent;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        return fetchQueryResults(query, false);
    }

    private <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query, boolean matchExactGroup) {
        List<T> result = new ArrayList<T>();

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipClass());
        Root<?> root = criteria.from(getConfig().getRelationshipClass());

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).getName()), query
                .getRelationshipType().getName()));

        Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
        Property<Object> descriptorProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR);
        Property<Object> relationshipProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

        Set<Entry<QueryParameter, Object[]>> parameters = query.getParameters().entrySet();

        for (Entry<QueryParameter, Object[]> entry : parameters) {
            QueryParameter queryParameter = entry.getKey();
            Object[] values = entry.getValue();

            if (entry.getKey() instanceof RelationshipQueryParameter) {
                RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) entry.getKey();

                for (Object object : values) {
                    IdentityType identityType = (IdentityType) object;

                    if (identityType != null) {
                        Object identityObject = lookupIdentityObjectById(identityType.getId());

                        if (identityObject != null) {
                            List<Object> objects = new ArrayList<Object>();

                            objects.add(identityObject);

                            if (Group.class.isInstance(identityType) && !matchExactGroup) {
                                List<Group> groupParents = getParentGroups((Group) identityType);

                                for (Group group : groupParents) {
                                    objects.add(this.lookupIdentityObjectById(group.getId()));
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
            }

            if (queryParameter instanceof AttributeParameter) {
                AttributeParameter customParameter = (AttributeParameter) queryParameter;

                Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipAttributeClass());
                Root fromProject = subquery.from(getConfig().getRelationshipAttributeClass());

                subquery.select(fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP)
                        .getName()));

                Predicate conjunction = builder.conjunction();

                conjunction.getExpressions().add(
                        builder.equal(fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME)
                                .getName()), customParameter.getName()));
                conjunction.getExpressions().add(
                        (fromProject.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE).getName())
                                .in((Object[]) values)));

                subquery.where(conjunction);

                subquery.groupBy(subquery.getSelection()).having(
                        builder.equal(builder.count(subquery.getSelection()), values.length));

                predicates.add(builder.in(root).value(subquery));
            }
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> queryResult = em.createQuery(criteria).getResultList();

        for (Object relationshipObject : queryResult) {
            result.add((T) convertToRelationshipType(relationshipObject));
        }

        return result;
    }

    private List<Group> getParentGroups(Group identityType) {
        DefaultIdentityQuery<Group> query = new DefaultIdentityQuery<Group>(Group.class, this);

        query.setParameter(Group.HAS_MEMBER, identityType);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        List<T> result = new ArrayList<T>();

        try {
            EntityManager em = getEntityManager();

            JPACriteriaQueryBuilder criteriaBuilder = new JPACriteriaQueryBuilder(this, identityQuery);

            List<Predicate> predicates = criteriaBuilder.getPredicates();

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
                result.add((T) convertToIdentityType(identity));
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error executing query.", e);
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
    public void setAttribute(IdentityType identity, Attribute<? extends Serializable> providedAttrib) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(IdentityType identity, String name) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCredentials(agent, storageClass);
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        return this.credentialStore.retrieveCurrentCredential(agent, storageClass);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        this.credentialStore.storeCredential(agent, storage);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        this.credentialStore.updateCredential(agent, credential, effectiveDate, expiryDate);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        this.credentialStore.validateCredentials(credentials);
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
        Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
        Property<Object> idProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID);
        Property<Object> descriptorProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS);

        String typeName = typeProperty.getValue(relationshipObject).toString();
        T relationshipType = null;
        Class<?> relationshipClass = null;

        try {
            relationshipClass = Class.forName(typeName);
            relationshipType = (T) relationshipClass.newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating Relationship instance for type [" + typeName + "]");
        }

        List<Property<Object>> identityTypeIdProperty = PropertyQueries.createQuery(relationshipClass)
                .addCriteria(new NamedPropertyCriteria("id")).getResultList();

        identityTypeIdProperty.get(0).setValue(relationshipType, idProperty.getValue(relationshipObject));

        List<?> identities = findChildRelationships(relationshipType);

        for (Object object : identities) {
            String descriptor = descriptorProperty.getValue(object).toString();

            List<Property<Object>> identityTypeProperty = PropertyQueries.createQuery(relationshipClass)
                    .addCriteria(new NamedPropertyCriteria(descriptor)).getResultList();

            IdentityType identityType = convertToIdentityType(identityProperty.getValue(object));

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
        IdentityTypeHandler<? extends IdentityType> identityTypeManager = getConfig().getIdentityTypeStores()
                .get(discriminator);

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
     * @param entity
     * @param attribute
     */
    private void storeIdentityTypeAttribute(Object entity, Attribute<? extends Serializable> attribute) {
        Object value = attribute.getValue();

        if (value == null) {
            return;
        }

        Object[] values = null;

        if (value.getClass().isArray()) {
            values = (Object[]) value;
        } else {
            values = new Object[] { value };
        }

        Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME);
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);
        Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE);

        try {
            for (Object attribValue : values) {
                Object newInstance = getConfig().getAttributeClass().newInstance();

                attributeNameProperty.setValue(newInstance, attribute.getName());
                attributeValueProperty.setValue(newInstance, attribValue);
                attributeIdentityProperty.setValue(newInstance, entity);

                getEntityManager().persist(newInstance);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating attributes.", e);
        }
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
        Object value = userAttribute.getValue();
        Object[] values = null;

        if (value.getClass().isArray()) {
            values = (Object[]) value;
        } else {
            values = new Object[] { value };
        }

        Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(
                PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP);
        Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

        try {
            for (Object attribValue : values) {
                Object newInstance = getConfig().getRelationshipAttributeClass().newInstance();

                attributeNameProperty.setValue(newInstance, userAttribute.getName());
                attributeValueProperty.setValue(newInstance, attribValue);
                attributeIdentityProperty.setValue(newInstance, identity);

                getEntityManager().persist(newInstance);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating attributes.", e);
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
     * Returns all stored attributes for the given {@link Relationship} that matchs the {@link Attribute} name.
     * </p>
     * 
     * @param identityType
     * @param attribute
     * @return
     */
    private List<?> findIdentityTypeAttributes(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()),
                identityType.getId()));
        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getName()),
                attribute.getName()));

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

        List<String> partitionIds = new ArrayList<String>();

        partitionIds.add(getCurrentRealm().getId());
        partitionIds.add(getCurrentPartition().getId());

        predicates.add(builder.in(join.get(getConfig().getModelProperty(PropertyType.PARTITION_ID).getName())).value(
                partitionIds));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
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
            return null;
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
            List<?> results = findIdentityTypeRelationships(entity);

            Set<Object> relationshipsToRemove = new HashSet<Object>();

            for (Object result : results) {
                relationshipsToRemove.add(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP)
                        .getValue(result));
            }

            // Now that we have the list, we can iterate through and remove the records
            for (Object relationship : relationshipsToRemove) {
                remove(convertToRelationshipType(relationship));
            }
        }
    }

    /**
     * <p>
     * Returns all relationships associated with the given {@link IdentityType}.
     * </p>
     * 
     * @param identityTypeEntity
     * @return
     */
    private List<?> findIdentityTypeRelationships(Object identityTypeEntity) {
        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
        Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());

        criteria.where(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                identityTypeEntity));

        return em.createQuery(criteria).getResultList();

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
        Collection<Attribute<? extends Serializable>> attributes = identityType.getAttributes();

        if (attributes != null && !attributes.isEmpty()) {
            EntityManager em = getEntityManager();

            for (Attribute<? extends Serializable> attribute : attributes) {
                try {
                    MappedAttribute mappedAttribute = getConfig().getAttributeProperties().get(attribute.getName());

                    // if the attribute was mapped as a property of the identity class
                    if (mappedAttribute != null) {
                        for (String attribName : getConfig().getAttributeProperties().keySet()) {
                            MappedAttribute attrib = getConfig().getAttributeProperties().get(attribName);

                            if (attribute.getName().equals(attribName)) {
                                attrib.getAttributeProperty().setValue(entity, attribute.getValue());
                            }
                        }
                    } else {
                        // remove the attributes to persist them again. Only the current attribute, not all.
                        List<?> results = findIdentityTypeAttributes(identityType, attribute);

                        for (Object object : results) {
                            em.remove(object);
                        }

                        storeIdentityTypeAttribute(entity, attribute);
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException("Error setting attribute [" + attribute + "] for [" + entity + "]", e);
                }
            }

            removeAttributes(identityType, entity);
        } else {
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
                try {
                    // remove the attributes to persist them again. Only the current attribute, not all.
                    List<?> results = findRelationshipAttributes(relationship, attribute);

                    for (Object object : results) {
                        em.remove(object);
                    }

                    storeRelationshipAttribute(identity, attribute);
                } catch (Exception e) {
                    throw new IdentityManagementException("Error setting attribute [" + attribute + "] for [" + identity + "]",
                            e);
                }
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
        try {
            for (MappedAttribute attrib : getConfig().getAttributeProperties().values()) {
                if (attrib.getIdentityProperty() != null && attrib.getIdentityProperty().getValue(entity) == null) {
                    // TODO: need to deal with AttributeType
                } else {
                    Member member = attrib.getAttributeProperty().getMember();
                    String mappedName = null;
                    Object value = null;

                    if (member instanceof Field) {
                        Field field = (Field) member;
                        IDMAttribute annotation = field.getAnnotation(IDMAttribute.class);

                        field.setAccessible(true);

                        mappedName = annotation.name();
                        value = field.get(entity);
                    }

                    identityType.setAttribute(new Attribute<Serializable>(mappedName, (Serializable) value));
                }
            }

            if (getConfig().getAttributeClass() != null) {
                List<?> results = findAllIdentityTypeAttributes(entity);

                if (!results.isEmpty()) {
                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME);
                        Property<Object> attributeValueProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_VALUE);

                        String attribName = (String) attributeNameProperty.getValue(object);
                        Serializable attribValue = (Serializable) attributeValueProperty.getValue(object);

                        Attribute<Serializable> identityTypeAttribute = identityType.getAttribute(attribName);

                        if (identityTypeAttribute == null) {
                            identityTypeAttribute = new Attribute<Serializable>(attribName, attribValue);
                            identityType.setAttribute(identityTypeAttribute);
                        } else {
                            // if it is a multi-valued attribute
                            if (identityTypeAttribute.getValue() != null) {
                                String[] values = null;

                                if (identityTypeAttribute.getValue().getClass().isArray()) {
                                    values = (String[]) identityTypeAttribute.getValue();
                                } else {
                                    values = new String[1];
                                    values[0] = identityTypeAttribute.getValue().toString();
                                }

                                String[] newValues = Arrays.copyOf(values, values.length + 1);

                                newValues[newValues.length - 1] = attribValue.toString();

                                identityTypeAttribute.setValue(newValues);

                                identityType.setAttribute(identityTypeAttribute);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error setting attribute.", e);
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
        try {
            if (getConfig().getRelationshipAttributeClass() != null) {
                List<?> results = findRelationshipAttributes(relationship);

                if (!results.isEmpty()) {
                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getConfig().getModelProperty(
                                PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
                        Property<Object> attributeValueProperty = getConfig().getModelProperty(
                                PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

                        String attribName = (String) attributeNameProperty.getValue(object);
                        Serializable attribValue = (Serializable) attributeValueProperty.getValue(object);

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
                                    String[] values = null;

                                    if (identityTypeAttribute.getValue().getClass().isArray()) {
                                        values = (String[]) identityTypeAttribute.getValue();
                                    } else {
                                        values = new String[1];
                                        values[0] = identityTypeAttribute.getValue().toString();
                                    }

                                    String[] newValues = Arrays.copyOf(values, values.length + 1);

                                    newValues[newValues.length - 1] = attribValue.toString();

                                    identityTypeAttribute.setValue(newValues);

                                    relationshipType.setAttribute(identityTypeAttribute);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error setting attribute.", e);
        }
    }

    @Override
    public <T extends Relationship> int countQueryResults(RelationshipQuery<T> query) {
        // TODO: Implement
        throw new UnsupportedOperationException();
    }

    protected Partition convertPartitionEntityToPartition(Object partitionObject) {
        return this.partitionStore.convertPartitionEntityToPartition(partitionObject);
    }

    protected Object lookupPartitionObject(Partition partition) {
        return this.partitionStore.lookupPartitionObject(partition);
    }

    protected Realm getCurrentRealm() {
        return getContext().getRealm();
    }

    protected Partition getCurrentPartition() {
        return getContext().getPartition();
    }

    protected EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw new IllegalStateException("Error while trying to determine EntityManager - context parameter not set.");
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }
}