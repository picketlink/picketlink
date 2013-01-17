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
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.event.AbstractBaseEvent;
import org.picketlink.idm.internal.util.Base64;
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
        PartitionStore<JPAPartitionStoreConfiguration> {

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

    private JPAPartitionStoreConfiguration partitionConfig;

    /**
     * The invocation context
     */
    private IdentityStoreInvocationContext context;

    public void setup(JPAIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    private JPAStoreConfiguration getCurrentConfig() {
        if (this.config != null) {
            return this.config;
        }

        return this.partitionConfig;
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

            if (lookupIdentityObjectById(identityType.getId()) != null) {
                throw new IdentityManagementException("IdentityType already exists.");
            }

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

            if (realm == null) {
                realm = new Realm(Realm.DEFAULT_REALM);
                createPartition(realm);
                context.setRealm(getRealm(Realm.DEFAULT_REALM));
            }
        }

        return realm;
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
            removeCredentials(identityObject);

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

    private void configurePartition(Partition partition, Object identity, IdentityType identityType) {
        if (getConfig().isModelPropertySet(PropertyType.IDENTITY_PARTITION)) {
            // TODO implement cache support for partitions
            Object partitionInstance = config.getModelProperty(PropertyType.IDENTITY_PARTITION).getValue(identity);
            identityType.setPartition(convertPartitionEntityToPartition(partitionInstance));
        } else {
            identityType.setPartition(partition);
        }
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

    private void removeCredentials(Object object) {
        EntityManager em = getEntityManager();

        if (getConfig().getCredentialClass() != null) {
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

                List<?> attributes = em.createQuery(attributeCriteria).getResultList();

                for (Object attribute : attributes) {
                    em.remove(attribute);
                }

                em.remove(credential);
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

    protected Partition convertPartitionEntityToPartition(Object partitionObject) {
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        String type = typeProperty.getValue(partitionObject).toString();

        Partition partition = null;

        if (Realm.class.getName().equals(type)) {
            partition = convertPartitionEntityToRealm(partitionObject);
        } else if (Tier.class.getName().equals(type)) {
            partition = convertPartitionEntityToTier(partitionObject);
        } else {
            throw new IdentityManagementException("Unsupported Partition type [" + type + "].");
        }

        return partition;
    }

    Object lookupPartitionObject(Partition partition) {
        EntityManager entityManager = getEntityManager();
        return entityManager.find(getCurrentConfig().getPartitionClass(), partition.getId());
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        CredentialHandler handler = getContext().getCredentialValidator(credentials.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for validating Credentials of type [" + credentials.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.validate(credentials, this);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public void storeCredential(Agent agent, CredentialStorage storage) {
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);
        Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

        Object lastCredential = retrieveCurrentCredentialEntity(agent, storage.getClass());

        EntityManager em = getEntityManager();

        if (lastCredential != null) {
            expiryProperty.setValue(lastCredential, new Date());
            em.merge(lastCredential);
        }

        Object newCredential = null;

        try {
            newCredential = getConfig().getCredentialClass().newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Could not instantiate credential class ["
                    + getConfig().getCredentialClass().getName() + "].", e);
        }

        Date effectiveDate = storage.getEffectiveDate();

        if (effectiveDate == null) {
            effectiveDate = new Date();
        }

        Object agentInstance = lookupIdentityObjectById(agent.getId());

        identityTypeProperty.setValue(newCredential, agentInstance);
        typeProperty.setValue(newCredential, storage.getClass().getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        em.persist(newCredential);

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        Property<Object> attributeName = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_NAME);
        Property<Object> attributeValue = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_VALUE);
        Property<Object> attributeCredential = getConfig().getModelProperty(PropertyType.CREDENTIAL_ATTRIBUTE_CREDENTIAL);

        for (Property<Object> property : annotatedTypes) {
            if (property.getJavaClass().equals(String.class)) {
                Object newCredentialAttribute = null;

                try {
                    newCredentialAttribute = this.getConfig().getCredentialAttributeClass().newInstance();
                } catch (Exception e) {
                    throw new IdentityManagementException("Could not instantiate credential attribute class ["
                            + getConfig().getCredentialAttributeClass().getName() + "].", e);
                }

                attributeName.setValue(newCredentialAttribute, property.getName());
                attributeValue.setValue(newCredentialAttribute, Base64.encodeObject((Serializable) property.getValue(storage)));
                attributeCredential.setValue(newCredentialAttribute, newCredential);

                em.persist(newCredentialAttribute);
            }
        }

        em.flush();
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass) {
        Object lastCredential = retrieveCurrentCredentialEntity(agent, storageClass);

        return (T) convertToCredentialStorage(lastCredential, storageClass);
    }

    private <T> Object retrieveCurrentCredentialEntity(Agent agent, Class<T> storageClass) {
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EFFECTIVE_DATE);
        Property<Object> expiryProperty = getConfig().getModelProperty(PropertyType.CREDENTIAL_EXPIRY_DATE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(agent.getId());

        predicates.add(builder.equal(root.get(identityTypeProperty.getName()), agentInstance));
        predicates.add(builder.equal(root.get(typeProperty.getName()), storageClass.getName()));

        Predicate conjunction = builder.conjunction();

        conjunction.getExpressions().add(
                builder.or(builder.greaterThanOrEqualTo(root.<Date> get(expiryProperty.getName()), new Date()),
                        builder.isNull(root.<Date> get(expiryProperty.getName()))));

        predicates.add(conjunction);

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        criteria.orderBy(builder.desc(root.get(effectiveProperty.getName())));

        Object lastCredential = null;

        try {
            List<?> result = em.createQuery(criteria).getResultList();

            if (!result.isEmpty()) {
                lastCredential = result.get(0);
            }
        } catch (NoResultException ignore) {
        } catch (Exception e) {
            throw new IdentityManagementException("Could not query credentials.", e);
        }
        return lastCredential;
    }

    private CredentialStorage convertToCredentialStorage(Object instance, Class<? extends CredentialStorage> storageClass) {
        CredentialStorage storage = null;

        if (instance != null) {
            try {
                storage = storageClass.newInstance();
            } catch (Exception e) {
                throw new IdentityManagementException("Could not instantiate storage class [" + storageClass.getName() + "].",
                        e);
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

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass) {
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
            storages.add((T) convertToCredentialStorage(object, storageClass));
        }

        return storages;
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
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void createPartition(Partition partition) {
        Property<Object> idProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_ID);
        Property<Object> nameProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_NAME);
        Property<Object> typeProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_TYPE);

        Class<?> partitionClass = getCurrentConfig().getPartitionClass();
        Object partitionObject = null;

        try {
            partitionObject = partitionClass.newInstance();
        } catch (Exception e) {
            throw new IdentityManagementException("Could not instantiate Partition class [" + partitionClass.getName() + "]");
        }

        String id = this.context.getIdGenerator().generate();

        partition.setId(id);

        idProperty.setValue(partitionObject, partition.getId());
        nameProperty.setValue(partitionObject, partition.getName());
        typeProperty.setValue(partitionObject, partition.getClass().getName());

        if (Tier.class.isInstance(partition)) {
            Tier tier = (Tier) partition;
            Tier parentTier = tier.getParent();

            if (parentTier != null) {
                Property<Object> parentProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_PARENT);
                parentProperty.setValue(partitionObject, lookupPartitionObject(parentTier));
            }
        }

        EntityManager em = getEntityManager();

        em.persist(partitionObject);
        em.flush();
    }

    private JPAPartitionStoreConfiguration getPartitionConfig() {
        return this.partitionConfig;
    }

    @Override
    public void removePartition(Partition partition) {
        if (partition.getId() == null) {
            throw new IdentityManagementException("No identifier provided.");
        }

        Object partitionObject = lookupPartitionObject(partition);

        if (partitionObject == null) {
            throw new IdentityManagementException("No Partition found with the given id [" + partition.getId() + "].");
        }

        EntityManager entityManager = getEntityManager();

        List<?> associatedIdentityTypes = getIdentityTypesForPartition(partitionObject);

        if (!associatedIdentityTypes.isEmpty()) {
            throw new IdentityManagementException(
                    "Partition could not be removed. There are IdentityTypes associated with it. Remove them first.");
        }

        List<?> childPartitions = getChildPartitions(partitionObject);

        if (!childPartitions.isEmpty()) {
            throw new IdentityManagementException(
                    "Partition could not be removed. There are child partitions associated with it. Remove them first.");
        }

        entityManager.remove(partitionObject);
        entityManager.flush();
    }

    private List<?> getIdentityTypesForPartition(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getCurrentConfig().getIdentityClass());
        Root<?> root = criteria.from(getCurrentConfig().getIdentityClass());

        Predicate wherePartition = builder.equal(
                root.get(getCurrentConfig().getModelProperty(PropertyType.IDENTITY_PARTITION).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }

    private List<?> getChildPartitions(Object partitionObject) {
        EntityManager entityManager = getEntityManager();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getCurrentConfig().getPartitionClass());
        Root<?> root = criteria.from(getCurrentConfig().getPartitionClass());

        Predicate wherePartition = builder.equal(
                root.get(getCurrentConfig().getModelProperty(PropertyType.PARTITION_PARENT).getName()), partitionObject);

        criteria.where(wherePartition);

        return entityManager.createQuery(criteria).getResultList();
    }

    @Override
    public Realm getRealm(String realmName) {
        return convertPartitionEntityToRealm(lookupPartitionEntityByName(Realm.class, realmName));
    }

    private Realm convertPartitionEntityToRealm(Object partitionObject) {
        Realm realm = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Realm.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_NAME);

                realm = new Realm(nameProperty.getValue(partitionObject).toString());

                realm.setId(idProperty.getValue(partitionObject).toString());
            }
        }

        return realm;
    }

    private Tier convertPartitionEntityToTier(Object partitionObject) {
        Tier tier = null;

        if (partitionObject != null) {
            Property<Object> typeProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_TYPE);

            if (Tier.class.getName().equals(typeProperty.getValue(partitionObject).toString())) {
                Property<Object> idProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_ID);
                Property<Object> nameProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_NAME);
                Property<Object> parentProperty = getCurrentConfig().getModelProperty(PropertyType.PARTITION_PARENT);

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

    @Override
    public Tier getTier(String tierName) {
        return convertPartitionEntityToTier(lookupPartitionEntityByName(Tier.class, tierName));
    }

    private Object lookupPartitionEntityByName(Class<? extends Partition> partitionType, String name) {
        if (name == null) {
            throw new IdentityManagementException("Tier name was not provided.");
        }

        EntityManager entityManager = getEntityManager();
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        Class<?> partitionClass = getCurrentConfig().getPartitionClass();

        CriteriaQuery<?> criteria = builder.createQuery(partitionClass);
        Root<?> root = criteria.from(partitionClass);

        Predicate whereType = builder.equal(
                root.get(getCurrentConfig().getModelProperty(PropertyType.PARTITION_TYPE).getName()), partitionType.getName());
        Predicate whereName = builder.equal(
                root.get(getCurrentConfig().getModelProperty(PropertyType.PARTITION_NAME).getName()), name);

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

    @Override
    public void setup(JPAPartitionStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.partitionConfig = config;
        this.context = context;
    }

}