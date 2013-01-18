package org.picketlink.idm.jpa.internal;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.PropertyType;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.AttributedType.AttributeParameter;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.model.annotation.RelationshipIdentity;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityTypeQueryParameter;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
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
@CredentialHandlers({ PasswordCredentialHandler.class, X509CertificateCredentialHandler.class })
public class JPAIdentityStore implements IdentityStore<JPAIdentityStoreConfiguration>, CredentialStore,
        PartitionStore {

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

                handler.onBeforeAdd(identityType, this);

                Object identity = handler.createIdentityInstance(identityType, this);

                EntityManager em = getEntityManager();

                em.persist(identity);
                em.flush();

                updateAttributes(identityType, identity);

                AbstractBaseEvent event = handler.raiseCreatedEvent(identityType, this);

                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
                getContext().getEventBridge().raiseEvent(event);
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating IdentityType [" + identityType + "].", ex);
            }
        } else if (value instanceof Relationship) {
            if (getConfig().getRelationshipClass() == null) {
                throw new IdentityManagementException("No Relationship class was provided. Relationships could not be stored.");
            }

            Relationship relationship = (Relationship) value;

            relationship.setId(getContext().getIdGenerator().generate());

            try {
                Object relationshipObject = getConfig().getRelationshipClass().newInstance();
                Class<? extends Relationship> relationshipClass = relationship.getClass();

                getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).setValue(relationshipObject, relationship.getId());
                getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).setValue(relationshipObject,
                        relationshipClass.getName());

                EntityManager em = getEntityManager();

                em.persist(relationshipObject);

                List<Property<IdentityType>> props = PropertyQueries.<IdentityType> createQuery(relationshipClass)
                        .addCriteria(new AnnotatedPropertyCriteria(RelationshipIdentity.class)).getResultList();

                for (Property<IdentityType> prop : props) {
                    Object relationshipIdentity = getConfig().getRelationshipIdentityClass().newInstance();

                    IdentityType identityType = prop.getValue(relationship);

                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).setValue(relationshipIdentity,
                            lookupIdentityObjectById(identityType.getId()));
                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR).setValue(relationshipIdentity,
                            prop.getName());
                    getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).setValue(
                            relationshipIdentity, relationshipObject);
                    em.persist(relationshipIdentity);
                }

                updateRelationshipAttributes(relationship, relationshipObject);
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating Relationship [" + relationship + "].", ex);
            }
        }
    }

    protected Realm getCurrentRealm() {
        Realm realm = context.getRealm();

        if (realm == null) {
            realm = getRealm(Realm.DEFAULT_REALM);
        }

        return realm;
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

    protected Partition getCurrentPartition() {
        Partition partition = getContext().getTier();

        if (partition == null) {
            partition = getCurrentRealm();
        }

        return partition;
    }

    @Override
    public void update(AttributedType value) {
        if (value == null) {
            throw new IllegalArgumentException("value passed to IdentityStore.update() may not be null");
        }

        if (value instanceof IdentityType) {
            IdentityType identityType = (IdentityType) value;

            IdentityTypeHandler<IdentityType> handler = getConfig().getHandler(identityType.getClass());

            Object identityObject = lookupIdentityObjectById(identityType.getId());
            if (identityObject == null) {
                throw new IdentityManagementException("The specified identity object [" + identityType.getId()
                        + "] does not exist.");
            }

            handler.populateIdentityInstance(identityObject, identityType, this);

            updateAttributes(identityType, identityObject);

            EntityManager em = getEntityManager();

            em.merge(identityObject);
            em.flush();

            AbstractBaseEvent event = handler.raiseUpdatedEvent(identityType, this);

            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identityObject);
            getContext().getEventBridge().raiseEvent(event);
        } else if (value instanceof Relationship) {
            Relationship relationship = (Relationship) value;

            Object relationshipObject = lookupRelationshipObjectById(relationship.getId());

            updateRelationshipAttributes(relationship, relationshipObject);

            EntityManager em = getEntityManager();

            em.merge(relationshipObject);
            em.flush();
        }

    }

    @Override
    public void remove(AttributedType value) {
        if (value instanceof IdentityType) {
            IdentityType identityType = (IdentityType) value;

            EntityManager em = getEntityManager();

            IdentityTypeHandler<IdentityType> handler = getConfig().getHandler(identityType.getClass());

            Object identityObject = lookupIdentityObjectById(identityType.getId());
            if (identityObject == null) {
                throw new IdentityManagementException("The specified identity object [" + identityType.getId()
                        + "] does not exist.");
            }

            handler.remove(identityObject, identityType, this);

            // Remove credentials
            this.credentialStore.removeCredentials(identityObject);

            // Remove attributes
            removeAttributes(identityObject);

            removeRelationships(identityObject);

            // Remove the identity object itself
            em.remove(identityObject);
            em.flush();

            AbstractBaseEvent event = handler.raiseDeletedEvent(identityType, this);

            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identityObject);
            getContext().getEventBridge().raiseEvent(event);
        } else if (value instanceof Relationship) {
            Relationship relationship = (Relationship) value;

            // First we build a list of all the relationships that the specified identity
            // is participating in
            if (getConfig().getRelationshipClass() != null) {
                EntityManager em = getEntityManager();

                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
                Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());
                List<Predicate> predicates = new ArrayList<Predicate>();
                Join<?, ?> join = root.join(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP)
                        .getName());

                predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName()),
                        relationship.getId()));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

                Set<Object> relationshipsToRemove = new HashSet<Object>();

                for (Object result : results) {
                    relationshipsToRemove.add(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP)
                            .getValue(result));
                }

                // Now that we have the list, we can iterate through and remove the records
                for (Object storedRelationship : relationshipsToRemove) {
                    // First we delete the attributes
                    criteria = builder.createQuery(getConfig().getRelationshipAttributeClass());
                    root = criteria.from(getConfig().getRelationshipAttributeClass());
                    predicates = new ArrayList<Predicate>();
                    predicates.add(builder.equal(
                            root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName()),
                            storedRelationship));
                    criteria.where(predicates.toArray(new Predicate[predicates.size()]));
                    results = em.createQuery(criteria).getResultList();

                    for (Object attribute : results) {
                        em.remove(attribute);
                    }

                    // Next we delete the relationship identities
                    criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
                    root = criteria.from(getConfig().getRelationshipIdentityClass());
                    predicates = new ArrayList<Predicate>();
                    predicates.add(builder.equal(
                            root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName()),
                            storedRelationship));
                    criteria.where(predicates.toArray(new Predicate[predicates.size()]));
                    results = em.createQuery(criteria).getResultList();

                    for (Object identityType : results) {
                        em.remove(identityType);
                    }

                    // Finally we delete the relationship itself
                    em.remove(storedRelationship);
                }
            }
        }
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
    public Group getGroup(String groupId) {
        if (groupId == null) {
            return null;
        }

        // Check the cache first
        Realm partition = context.getRealm();
        Group group = getContext().getCache().lookupGroup(partition, groupId);

        if (group == null) {
            DefaultIdentityQuery<Group> defaultIdentityQuery = new DefaultIdentityQuery(Group.class, this);

            defaultIdentityQuery.setParameter(Group.NAME, groupId);

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

        Group group = getGroup(name);

        if (group.getParentGroup() == null || !group.getParentGroup().getName().equals(parent.getName())) {
            group = null;
        }

        return group;
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
            DefaultIdentityQuery<Role> defaultIdentityQuery = new DefaultIdentityQuery(Role.class, this);

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
            DefaultIdentityQuery<Agent> defaultIdentityQuery = new DefaultIdentityQuery(Agent.class, this);

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

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        List<T> result = new ArrayList<T>();

        try {
            EntityManager em = getEntityManager();

            JPACriteriaQueryBuilder criteriaBuilder = new JPACriteriaQueryBuilder(this, identityQuery);

            List<Predicate> predicates = criteriaBuilder.getPredicates();

            CriteriaQuery<?> criteria = criteriaBuilder.getCriteria();

            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> queryResult = em.createQuery(criteria).getResultList();

            for (Object identity : queryResult) {
                T identityType = convertToIdentityType(identity);
                result.add(identityType);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error executing query.", e);
        }

        return result;
    }

    private <T extends IdentityType> T convertToIdentityType(Object identity) {
        String discriminator = getConfig().getModelProperty(PropertyType.IDENTITY_DISCRIMINATOR).getValue(identity).toString();
        IdentityTypeHandler<? extends IdentityType> identityTypeManager = getConfig().getIdentityTypeStores()
                .get(discriminator);

        T identityType = (T) identityTypeManager.createIdentityType(identity, this);

        populateIdentityTypeAttributes(identityType, identity);

        return identityType;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        // TODO implement
        throw new UnsupportedOperationException();
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
    
    protected EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw new IllegalStateException("Error while trying to determine EntityManager - context parameter not set.");
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    /**
     * <p>
     * Stores the specified {@link Attribute}.
     * </p>
     * 
     * @param identity
     * @param userAttribute
     */
    private void storeAttribute(Object identity, Attribute<? extends Serializable> userAttribute) {
        Object value = userAttribute.getValue();
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

                attributeNameProperty.setValue(newInstance, userAttribute.getName());
                attributeValueProperty.setValue(newInstance, attribValue);
                attributeIdentityProperty.setValue(newInstance, identity);

                getEntityManager().persist(newInstance);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error creating attributes.", e);
        }
    }

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
     * Removes all attributes for the given <code>identity</code> except to those whose names exists on provided {@link List}.
     * </p>
     * 
     * @param identity
     * @param attributesToRetain
     */
    private void removeAttributes(Object identity, List<String> attributesToRetain) {
        StringBuffer attributeNames = new StringBuffer();

        for (String string : attributesToRetain) {
            if (attributeNames.length() != 0) {
                attributeNames.append(",");
            }

            attributeNames.append("'").append(string).append("'");
        }

        List<?> storedAttributes = findIdentityTypeAttributes(identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getValue(attribute).toString();

            if (!attributesToRetain.contains(attributeName)) {
                getEntityManager().remove(attribute);
            }
        }
    }

    private void removeRelationshipAttributes(Object identity, List<String> attributesToRetain) {
        StringBuffer attributeNames = new StringBuffer();

        for (String string : attributesToRetain) {
            if (attributeNames.length() != 0) {
                attributeNames.append(",");
            }

            attributeNames.append("'").append(string).append("'");
        }

        List<?> storedAttributes = findRelationshipAttributes(identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getValue(attribute)
                    .toString();

            if (!attributesToRetain.contains(attributeName)) {
                getEntityManager().remove(attribute);
            }
        }
    }

    /**
     * <p>
     * Removes all attributes for the given <code>identity</code>.
     * </p>
     * 
     * @param identity
     */
    private void removeAllAttributes(Object identity) {
        removeAttributes(identity, Collections.<String> emptyList());
    }

    private void removeAllRelationshipAttributes(Object identity) {
        removeRelationshipAttributes(identity, Collections.<String> emptyList());
    }

    private List<?> findIdentityTypeAttributes(IdentityType identityType, String idValue,
            Attribute<? extends Serializable> userAttribute) {
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(PropertyType.ATTRIBUTE_IDENTITY);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), idValue));

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.ATTRIBUTE_NAME).getName()),
                userAttribute.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private List<?> findRelationshipAttributes(Relationship relationship, String idValue,
            Attribute<? extends Serializable> userAttribute) {
        Property<Object> attributeIdentityProperty = getConfig().getModelProperty(
                PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipAttributeClass());
        Root<?> root = criteria.from(getConfig().getRelationshipAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        predicates.add(builder.equal(join.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName()), idValue));

        predicates.add(builder.equal(
                root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_NAME).getName()),
                userAttribute.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private List<?> findIdentityTypeAttributes(Object object) {
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

    protected Object lookupIdentityObjectById(String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName()), id));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    protected Object lookupRelationshipObjectById(String id) {
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

    private void removeRelationships(Object identity) {
        EntityManager em = getEntityManager();

        // First we build a list of all the relationships that the specified identity
        // is participating in
        if (getConfig().getRelationshipClass() != null) {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
            Root<?> root = criteria.from(getConfig().getRelationshipIdentityClass());
            List<Predicate> predicates = new ArrayList<Predicate>();

            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY).getName()),
                    identity));

            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();

            Set<Object> relationshipsToRemove = new HashSet<Object>();

            for (Object result : results) {
                relationshipsToRemove.add(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP)
                        .getValue(result));
            }

            // Now that we have the list, we can iterate through and remove the records
            for (Object relationship : relationshipsToRemove) {
                // First we delete the attributes
                criteria = builder.createQuery(getConfig().getRelationshipAttributeClass());
                root = criteria.from(getConfig().getRelationshipAttributeClass());
                predicates = new ArrayList<Predicate>();
                predicates.add(builder.equal(
                        root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName()),
                        relationship));
                criteria.where(predicates.toArray(new Predicate[predicates.size()]));
                results = em.createQuery(criteria).getResultList();

                for (Object attribute : results) {
                    em.remove(attribute);
                }

                // Next we delete the relationship identities
                criteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
                root = criteria.from(getConfig().getRelationshipIdentityClass());
                predicates = new ArrayList<Predicate>();
                predicates.add(builder.equal(
                        root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP).getName()),
                        relationship));
                criteria.where(predicates.toArray(new Predicate[predicates.size()]));
                results = em.createQuery(criteria).getResultList();

                for (Object identityType : results) {
                    em.remove(identityType);
                }

                // Finally we delete the relationship itself
                em.remove(relationship);
            }
        }
    }

    private void removeAttributes(Object object) {
        EntityManager em = getEntityManager();

        if (getConfig().getAttributeClass() != null) {
            List<?> results = findIdentityTypeAttributes(object);
            for (Object result : results) {
                em.remove(result);
            }
        }
    }



    private void updateAttributes(IdentityType identityType, Object identity) {
        EntityManager em = getEntityManager();

        if (identityType.getAttributes() != null && !identityType.getAttributes().isEmpty()) {
            List<String> attributesToRetain = new ArrayList<String>();

            for (Attribute<? extends Serializable> userAttribute : identityType.getAttributes()) {
                attributesToRetain.add(userAttribute.getName());

                try {
                    MappedAttribute mappedAttribute = getConfig().getAttributeProperties().get(userAttribute.getName());

                    // if the attribute was mapped as a property of the identity class
                    if (mappedAttribute != null) {
                        for (String attribName : getConfig().getAttributeProperties().keySet()) {
                            MappedAttribute attrib = getConfig().getAttributeProperties().get(attribName);

                            if (userAttribute.getName().equals(attribName)) {
                                attrib.getAttributeProperty().setValue(identity, userAttribute.getValue());
                            }
                        }
                    } else {
                        // remove the attributes to persist them again. Only the current attribute, not all.
                        List<?> results = findIdentityTypeAttributes(identityType, identityType.getId(), userAttribute);

                        for (Object object : results) {
                            em.remove(object);
                        }

                        storeAttribute(identity, userAttribute);
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException("Error setting attribute [" + userAttribute + "] for [" + identity
                            + "]", e);
                }
            }

            // remove all attributes not present in the retain list.
            if (attributesToRetain.isEmpty()) {
                removeAllAttributes(identity);
            } else {
                removeAttributes(identity, attributesToRetain);
            }
        }
    }

    private void updateRelationshipAttributes(Relationship relationship, Object identity) {
        EntityManager em = getEntityManager();

        if (relationship.getAttributes() != null && !relationship.getAttributes().isEmpty()) {
            List<String> attributesToRetain = new ArrayList<String>();

            for (Attribute<? extends Serializable> userAttribute : relationship.getAttributes()) {
                attributesToRetain.add(userAttribute.getName());

                try {
                    MappedAttribute mappedAttribute = getConfig().getAttributeProperties().get(userAttribute.getName());

                    // if the attribute was mapped as a property of the identity class
                    if (mappedAttribute != null) {
                        for (String attribName : getConfig().getAttributeProperties().keySet()) {
                            MappedAttribute attrib = getConfig().getAttributeProperties().get(attribName);

                            if (userAttribute.getName().equals(attribName)) {
                                attrib.getAttributeProperty().setValue(identity, userAttribute.getValue());
                            }
                        }
                    } else {
                        // remove the attributes to persist them again. Only the current attribute, not all.
                        List<?> results = findRelationshipAttributes(relationship, relationship.getId(), userAttribute);

                        for (Object object : results) {
                            em.remove(object);
                        }

                        storeRelationshipAttribute(identity, userAttribute);
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException("Error setting attribute [" + userAttribute + "] for [" + identity
                            + "]", e);
                }
            }

            // remove all attributes not present in the retain list.
            if (attributesToRetain.isEmpty()) {
                removeAllRelationshipAttributes(identity);
            } else {
                removeRelationshipAttributes(identity, attributesToRetain);
            }
        }
    }

    /**
     * <p>
     * Populates the given {@link IdentityType} instance with the attributes associated with the given <code>identity</code>
     * argument.
     * </p>
     * 
     * @param identityType
     * @param identity
     */
    private void populateIdentityTypeAttributes(IdentityType identityType, Object identity) {
        try {
            for (MappedAttribute attrib : getConfig().getAttributeProperties().values()) {
                if (attrib.getIdentityProperty() != null && attrib.getIdentityProperty().getValue(identity) == null) {
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
                        value = field.get(identity);
                    }

                    identityType.setAttribute(new Attribute<Serializable>(mappedName, (Serializable) value));
                }
            }

            if (getConfig().getAttributeClass() != null) {
                EntityManager em = getEntityManager();

                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
                Root<?> attributeClassRoot = criteria.from(getConfig().getAttributeClass());
                List<Predicate> predicates = new ArrayList<Predicate>();

                Join identityPropertyJoin = attributeClassRoot.join(getConfig().getModelProperty(
                        PropertyType.ATTRIBUTE_IDENTITY).getName());
                String propertyNameToJoin = getConfig().getModelProperty(PropertyType.IDENTITY_ID).getName();

                predicates.add(builder.equal(identityPropertyJoin.get(propertyNameToJoin), identityType.getId()));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

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

    private void populateRelationshipAttributes(Relationship relationshipType, Object relationship) {
        try {
            if (getConfig().getRelationshipAttributeClass() != null) {
                EntityManager em = getEntityManager();

                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipAttributeClass());
                Root<?> attributeClassRoot = criteria.from(getConfig().getRelationshipAttributeClass());
                List<Predicate> predicates = new ArrayList<Predicate>();

                Join identityPropertyJoin = attributeClassRoot.join(getConfig().getModelProperty(
                        PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName());
                String propertyNameToJoin = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID).getName();

                predicates.add(builder.equal(identityPropertyJoin.get(propertyNameToJoin), relationshipType.getId()));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

                if (!results.isEmpty()) {
                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getConfig().getModelProperty(
                                PropertyType.RELATIONSHIP_ATTRIBUTE_NAME);
                        Property<Object> attributeValueProperty = getConfig().getModelProperty(
                                PropertyType.RELATIONSHIP_ATTRIBUTE_VALUE);

                        String attribName = (String) attributeNameProperty.getValue(object);
                        Serializable attribValue = (Serializable) attributeValueProperty.getValue(object);

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
        } catch (Exception e) {
            throw new IdentityManagementException("Error setting attribute.", e);
        }
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(RelationshipQuery<T> query) {
        List<T> result = new ArrayList<T>();
        Set<Entry<QueryParameter, Object[]>> parameters = query.getParameters().entrySet();

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getRelationshipClass());
        Root<?> root = criteria.from(getConfig().getRelationshipClass());

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PropertyType.RELATIONSHIP_CLASS).getName()), query
                .getRelationshipType().getName()));

        Property<Object> identityProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY);
        Property<Object> idProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_ID);
        Property<Object> descriptorProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_DESCRIPTOR);
        Property<Object> relationshipProperty = getConfig().getModelProperty(PropertyType.RELATIONSHIP_IDENTITY_RELATIONSHIP);

        for (Entry<QueryParameter, Object[]> entry : parameters) {
            QueryParameter queryParameter = entry.getKey();
            Object[] values = entry.getValue();

            if (entry.getKey() instanceof IdentityTypeQueryParameter) {
                IdentityTypeQueryParameter identityTypeParameter = (IdentityTypeQueryParameter) entry.getKey();

                for (Object object : values) {
                    IdentityType identityType = (IdentityType) object;
                    Object identityObject = lookupIdentityObjectById(identityType.getId());

                    Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipIdentityClass());
                    Root fromProject = subquery.from(getConfig().getRelationshipIdentityClass());
                    Subquery<?> select = subquery.select(fromProject.get(relationshipProperty.getName()));

                    Predicate conjunction = builder.conjunction();

                    conjunction.getExpressions().add(
                            builder.equal(fromProject.get(descriptorProperty.getName()), identityTypeParameter.getName()));
                    conjunction.getExpressions()
                            .add(builder.equal(fromProject.get(identityProperty.getName()), identityObject));

                    subquery.where(conjunction);

                    predicates.add(builder.in(root).value(subquery));
                }
            }

            if (queryParameter instanceof IdentityType.AttributeParameter) {
                AttributeParameter customParameter = (AttributeParameter) queryParameter;

                Subquery<?> subquery = criteria.subquery(getConfig().getRelationshipAttributeClass());
                Root fromProject = subquery.from(getConfig().getRelationshipAttributeClass());
                Subquery<?> select = subquery.select(fromProject.get(getConfig().getModelProperty(
                        PropertyType.RELATIONSHIP_ATTRIBUTE_RELATIONSHIP).getName()));

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
            CriteriaQuery<?> identityCriteria = builder.createQuery(getConfig().getRelationshipIdentityClass());
            Root<?> identityRoot = identityCriteria.from(getConfig().getRelationshipIdentityClass());

            identityCriteria.where(builder.equal(identityRoot.get(relationshipProperty.getName()), relationshipObject));

            List<?> identities = em.createQuery(identityCriteria).getResultList();

            T relationshipType = null;

            try {
                relationshipType = query.getRelationshipType().newInstance();
            } catch (Exception e) {
                throw new IdentityManagementException("Error creating Relationship instance for type ["
                        + query.getRelationshipType().getName() + "]");
            }

            List<Property<Object>> props = PropertyQueries.createQuery(query.getRelationshipType())
                    .addCriteria(new NamedPropertyCriteria("id")).getResultList();

            props.get(0).setValue(relationshipType, idProperty.getValue(relationshipObject));

            for (Object object : identities) {
                String descriptor = descriptorProperty.getValue(object).toString();

                props = PropertyQueries.createQuery(query.getRelationshipType())
                        .addCriteria(new NamedPropertyCriteria(descriptor)).getResultList();

                IdentityType identityType = convertToIdentityType(identityProperty.getValue(object));

                props.get(0).setValue(relationshipType, identityType);
            }

            populateRelationshipAttributes(relationshipType, relationshipObject);

            result.add(relationshipType);
        }

        return result;
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
    
}