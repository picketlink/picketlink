package org.picketlink.idm.jpa.internal;

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_EFFECTIVE_DATE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_EXPIRY_DATE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_TYPE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_PARTITION;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import org.picketlink.idm.internal.util.IDMUtil;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
 * 
 * @author Shane Bryzak
 */
@CredentialHandlers({ PasswordCredentialHandler.class, X509CertificateCredentialHandler.class })
public class JPAIdentityStore implements IdentityStore<JPAIdentityStoreConfiguration>, CredentialStore {

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

    public void setup(JPAIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
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
    public void add(IdentityType identityType) {
        checkInvalidIdentityType(identityType);

        if (lookupIdentityObjectById(identityType) != null) {
            throw new IdentityManagementException("IdentityType already exists.");
        }

        try {
            IdentityTypeHandler<IdentityType> identityTypeManager = getConfig().getIdentityTypeManager(identityType.getClass());

            Object identity = identityTypeManager.createIdentityInstance(getContext().getRealm(), identityType, this);

            EntityManager em = getEntityManager();

            em.persist(identity);
            em.flush();

            updateAttributes(identityType, identity);

            AbstractBaseEvent event = identityTypeManager.raiseCreatedEvent(identityType, this);

            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
            getContext().getEventBridge().raiseEvent(event);
        } catch (Exception ex) {
            throw new IdentityManagementException("Exception while creating IdentityType [" + identityType + "].", ex);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        checkInvalidIdentityType(identityType);

        IdentityTypeHandler<IdentityType> identityTypeManager = getConfig().getIdentityTypeManager(identityType.getClass());

        Object identity = getIdentityObject(identityType);

        identityTypeManager.populateIdentityInstance(getContext().getRealm(), identity, identityType, this);

        updateAttributes(identityType, identity);

        EntityManager em = getEntityManager();

        em.merge(identity);
        em.flush();

        AbstractBaseEvent event = identityTypeManager.raiseUpdatedEvent(identityType, this);

        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
        getContext().getEventBridge().raiseEvent(event);
    }

    @Override
    public void remove(IdentityType identityType) {
        checkInvalidIdentityType(identityType);

        EntityManager em = getEntityManager();
        
        IdentityTypeHandler<IdentityType> identityTypeManager = getConfig().getIdentityTypeManager(identityType.getClass());

        Object identity = getIdentityObject(identityType);

        identityTypeManager.remove(identity, identityType, this);

        // Remove credentials
        removeCredentials(identity);

        // Remove attributes
        removeAttributes(identity);

        // Remove memberships - this takes a little more work because the identity may be
        // a member, a role or a group
        removeMemberships(identity);

        // Remove the identity object itself
        em.remove(identity);
        em.flush();

        AbstractBaseEvent event = identityTypeManager.raiseDeletedEvent(identityType, this);

        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
        getContext().getEventBridge().raiseEvent(event);
    }

    @Override
    public User getUser(String id) {
        if (id == null) {
            return null;
        }

        // Check the cache first
        User user = getContext().getCache().lookupUser(context.getRealm(), id);

        // If the cache doesn't have a reference to the User, we have to look up it's identity object
        // and create a User instance based on it
        if (user == null) {
            DefaultIdentityQuery<User> defaultIdentityQuery = new DefaultIdentityQuery(User.class, this);

            defaultIdentityQuery.setParameter(User.ID, id);

            List<User> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                user = resultList.get(0);
            }

            getContext().getCache().putUser(context.getRealm(), user);
        }

        return user;
    }

    private void configurePartition(Partition partition, Object identity, IdentityType identityType) {
        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {
            // TODO implement cache support for partitions
            Object partitionInstance = getModelProperty(Object.class, identity, PROPERTY_IDENTITY_PARTITION);
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
    public Agent getAgent(String id) {
        if (id == null) {
            return null;
        }

        // Check the cache first
        Realm partition = context.getRealm();
        Agent agent = getContext().getCache().lookupAgent(partition, id);

        // If the cache doesn't have a reference to the User, we have to look up it's identity object
        // and create a User instance based on it
        if (agent == null) {
            DefaultIdentityQuery<Agent> defaultIdentityQuery = new DefaultIdentityQuery(Agent.class, this);

            defaultIdentityQuery.setParameter(Agent.ID, id);

            List<Agent> resultList = defaultIdentityQuery.getResultList();

            if (!resultList.isEmpty()) {
                agent = resultList.get(0);
            } else {
                agent = getUser(id);
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
                String discriminator = getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getValue(identity)
                        .toString();
                IdentityTypeHandler<? extends IdentityType> identityTypeManager = getConfig().getIdentityTypeStores().get(
                        discriminator);

                T identityType = (T) identityTypeManager.createIdentityType(getContext().getRealm(), identity, this);

                configurePartition(getContext().getRealm(), identity, identityType);
                populateAttributes(identityType, identity);

                result.add(identityType);
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error executing query.", e);
        }

        return result;
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        Property<Object> memberModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
        Property<Object> roleModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
        Property<Object> groupModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);
        SimpleGroupRole groupRole = null;

        if (member instanceof Agent) {
            Role storedRole = null;
            Object identityRole = null;

            if (role != null) {
                storedRole = getRole(role.getName());
                identityRole = lookupIdentityObjectById(storedRole);
            }

            Agent storedAgent = null;
            Object identityUser = null;

            if (member != null) {
                storedAgent = getAgent(((Agent) member).getId());
                identityUser = lookupIdentityObjectById(storedAgent);
            }

            Group storedGroup = null;
            Object identityGroup = null;

            if (group != null) {
                storedGroup = getGroup(group.getName());
                identityGroup = lookupIdentityObjectById(storedGroup);
            }

            Object membership = null;

            try {
                membership = getConfig().getMembershipClass().newInstance();
            } catch (Exception e) {
                throw new IdentityManagementException("Could not create membership type instance.", e);
            }

            if (storedRole != null && storedGroup != null) {
                try {
                    memberModelProperty.setValue(membership, identityUser);
                    roleModelProperty.setValue(membership, identityRole);
                    groupModelProperty.setValue(membership, identityGroup);
                } catch (Exception e) {
                }
            } else {
                if (storedRole != null) {
                    memberModelProperty.setValue(membership, identityUser);
                    roleModelProperty.setValue(membership, identityRole);
                } else {
                    memberModelProperty.setValue(membership, identityUser);
                    groupModelProperty.setValue(membership, identityGroup);
                }
            }

            getEntityManager().persist(membership);
            getEntityManager().flush();

            groupRole = new SimpleGroupRole(storedAgent, storedRole, storedGroup);
        } else if (member instanceof Group) {
            // TODO implement
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("The member parameter must be an instance of User or Group");
        }

        return groupRole;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        Property<Object> memberModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
        Property<Object> roleModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
        Property<Object> groupModelProperty = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getMembershipClass());
        Root<?> root = criteria.from(getConfig().getMembershipClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object identityUser = lookupIdentityObjectById(member);

        predicates.add(builder.equal(root.get(memberModelProperty.getName()), identityUser));

        if (group != null && role != null) {
            Object identityRole = lookupIdentityObjectById(role);
            Object identityGroup = lookupIdentityObjectById(group);

            predicates.add(builder.equal(root.get(roleModelProperty.getName()), identityRole));
            predicates.add(builder.equal(root.get(groupModelProperty.getName()), identityGroup));
        } else {
            if (role != null) {
                Object identityRole = lookupIdentityObjectById(role);

                predicates.add(builder.equal(root.get(roleModelProperty.getName()), identityRole));
            }

            if (group != null) {
                Object identityGroup = lookupIdentityObjectById(group);

                predicates.add(builder.equal(root.get(groupModelProperty.getName()), identityGroup));
            }
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> resultList = em.createQuery(criteria).getResultList();

        for (Object object : resultList) {
            em.remove(object);
        }

        em.flush();
    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
        GroupRole groupRole = null;

        List<?> resultList = Collections.emptyList();

        DefaultIdentityQuery<IdentityType> defaultIdentityQuery = new DefaultIdentityQuery(member.getClass(), this);

        defaultIdentityQuery.setParameter(IdentityType.HAS_GROUP_ROLE, new SimpleGroupRole(member, role, group));

        resultList = defaultIdentityQuery.getResultList();

        if (!resultList.isEmpty()) {
            Agent storedAgent = getAgent(((Agent) member).getId());
            Role storedRole = null;
            Group storedGroup = null;

            if (role != null) {
                storedRole = getRole(role.getName());
            }

            if (group != null) {
                storedGroup = getGroup(group.getName());
            }

            groupRole = new SimpleGroupRole(storedAgent, storedRole, storedGroup);
        }

        return groupRole;
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

        Property<Object> attributeNameProperty = getConfig().getAttributeNameProperty();
        Property<Object> attributeIdentityProperty = getConfig().getAttributeIdentityProperty();
        Property<Object> attributeValueProperty = getConfig().getAttributeValueProperty();

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

        List<?> storedAttributes = findAttributes(identity);

        for (Object attribute : storedAttributes) {
            String attributeName = getConfig().getAttributeNameProperty().getValue(attribute).toString();

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

    private List<?> findAttributes(IdentityType identityType, String idValue, Attribute<? extends Serializable> userAttribute) {
        Property<Object> attributeIdentityProperty = getConfig().getAttributeIdentityProperty();

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        if (IDMUtil.isAgentType(identityType.getClass())) {
            predicates.add(builder.equal(join.get(getConfig().getIdentityIdProperty().getName()), idValue));
        } else {
            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), idValue));
        }

        predicates.add(builder.equal(root.get(getConfig().getAttributeNameProperty().getName()), userAttribute.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private List<?> findAttributes(Object object) {
        Class<?> attributeClass = getConfig().getAttributeClass();
        String identityProperty = getConfig().getAttributeIdentityProperty().getName();

        EntityManager em = getEntityManager();
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(attributeClass);
        Root<?> root = criteria.from(attributeClass);

        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(identityProperty), object));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    protected Object lookupIdentityObjectById(IdentityType identityType) {
        String id = getIdentifierValue(identityType);

        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        predicates.add(builder.equal(root.get(getConfig().getDiscriminatorProperty().getName()), getConfig()
                .getIdentityDiscriminator(identityType.getClass())));

        if (IDMUtil.isUserType(identityType.getClass()) || IDMUtil.isAgentType(identityType.getClass())) {
            predicates.add(builder.equal(root.get(getConfig().getIdentityIdProperty().getName()), id));
        } else if (IDMUtil.isGroupType(identityType.getClass()) || IDMUtil.isRoleType(identityType.getClass())
                || IDMUtil.isRelationshipType(identityType.getClass())) {
            predicates.add(builder.equal(root.get(getConfig().getIdentityNameProperty().getName()), id));
        } else {
            throw new SecurityException("Could not lookup identity by id - unsupported IdentityType ["
                    + identityType.getClass().getName() + "]");
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            return null;
        }

        if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new SecurityException("Error looking up identity by id - ambiguous identities found for id: [" + id + "]");
        }

    }

    private void removeMemberships(Object object) {
        EntityManager em = getEntityManager();

        if (getConfig().getMembershipClass() != null) {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getMembershipClass());
            Root<?> root = criteria.from(getConfig().getMembershipClass());
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_MEMBER).getName()), object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

            criteria = builder.createQuery(getConfig().getMembershipClass());
            root = criteria.from(getConfig().getMembershipClass());
            predicates.clear();
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_GROUP).getName()), object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

            criteria = builder.createQuery(getConfig().getMembershipClass());
            root = criteria.from(getConfig().getMembershipClass());
            predicates.clear();
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_ROLE).getName()), object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

        }
    }

    private void removeAttributes(Object object) {
        EntityManager em = getEntityManager();

        if (getConfig().getAttributeClass() != null) {
            List<?> results = findAttributes(object);
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
            predicates
                    .add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY).getName()), object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
            
            for (Object credential : results) {
                CriteriaQuery<?> attributeCriteria = builder.createQuery(getConfig().getCredentialAttributeClass());
                Root<?> attributeRoot = attributeCriteria.from(getConfig().getCredentialAttributeClass());
                List<Predicate> attributePredicates = new ArrayList<Predicate>();

                Property<Object> attributeCredential = getConfig().getModelProperty(
                        JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE);

                attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), credential));

                List<?> attributes = em.createQuery(attributeCriteria).getResultList();
                
                for (Object attribute : attributes) {
                    em.remove(attribute);
                }
                
                em.remove(credential);
            }
        }
    }

    <P> P getModelProperty(Class<P> propertyType, Object instance, String propertyName) {
        @SuppressWarnings("unchecked")
        Property<P> property = (Property<P>) getConfig().getModelProperty(propertyName);
        return property == null ? null : property.getValue(instance);
    }

    void setModelProperty(Object instance, String propertyName, Object value) {
        setModelProperty(instance, propertyName, value, false);
    }

    void setModelProperty(Object instance, String propertyName, Object value, boolean required) {
        if (getConfig().isModelPropertySet(propertyName)) {
            getConfig().getModelProperty(propertyName).setValue(instance, value);
        } else if (required) {
            throw new IdentityManagementException("Model property [" + propertyName + "] has not been configured.");
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
                        List<?> results = findAttributes(identityType, getIdentifierValue(identityType), userAttribute);

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

    /**
     * <p>
     * Resolves the value of the identifier for the given {@link IdentityType}.
     * </p>
     * 
     * @param identityType
     * @return
     */
    private String getIdentifierValue(IdentityType identityType) {
        String value = null;

        if (IDMUtil.isUserType(identityType.getClass())) {
            value = ((User) identityType).getId();
        } else if (IDMUtil.isAgentType(identityType.getClass())) {
            value = ((Agent) identityType).getId();
        } else if (IDMUtil.isRoleType(identityType.getClass())) {
            value = ((Role) identityType).getName();
        } else if (IDMUtil.isGroupType(identityType.getClass())) {
            value = ((Group) identityType).getName();
        } else if (IDMUtil.isRelationshipType(identityType.getClass())) {
            value = ((Relationship) identityType).getName();
        }

        return value;
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
    private void populateAttributes(IdentityType identityType, Object identity) {
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

                Join identityPropertyJoin = attributeClassRoot.join(getConfig().getAttributeIdentityProperty().getName());
                String propertyNameToJoin = getConfig().getIdentityIdProperty().getName();

                if (IDMUtil.isRoleType(identityType.getClass()) || IDMUtil.isGroupType(identityType.getClass())
                        || IDMUtil.isRelationshipType(identityType.getClass())) {
                    propertyNameToJoin = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName();
                }

                predicates.add(builder.equal(identityPropertyJoin.get(propertyNameToJoin), getIdentifierValue(identityType)));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

                if (!results.isEmpty()) {
                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getConfig().getAttributeNameProperty();
                        Property<Object> attributeValueProperty = getConfig().getAttributeValueProperty();

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

    private Partition convertPartitionEntityToPartition(Object instance) {
        return null;
    }

    Object lookupPartitionObject(Partition partition) {
        // TODO implement realm lookup
        return null;
    }

    /**
     * <p>
     * Checks if the provided {@link IdentityType} instance is valid or not null. An exception will be thrown when the
     * validation fails.
     * </p>
     * 
     * @param identityType
     * @throws IdentityManagementException
     */
    private void checkInvalidIdentityType(IdentityType identityType) throws IdentityManagementException {
        if (identityType == null) {
            throw new IdentityManagementException("The provided IdentityType instance is invalid or was null.");
        }
    }

    /**
     * <p>
     * Returns a {@link Object} for the Identity Class used to store {@link IdentityType} instances. If no instance was found an
     * exception will be thrown.
     * </p>
     * 
     * @param identityTremoype
     * @return
     * @throws IdentityManagementException
     */
    private Object getIdentityObject(IdentityType identityType) throws IdentityManagementException {
        Object identity = lookupIdentityObjectById(identityType);

        if (identity == null) {
            throw new IdentityManagementException("The provided IdentityType instance does not exists.");
        }

        return identity;
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
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EFFECTIVE_DATE);
        Property<Object> expiryProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EXPIRY_DATE);
        
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

        Object agentInstance = lookupIdentityObjectById(agent);
        
        identityTypeProperty.setValue(newCredential, agentInstance);
        typeProperty.setValue(newCredential, storage.getClass().getName());
        effectiveProperty.setValue(newCredential, effectiveDate);
        expiryProperty.setValue(newCredential, storage.getExpiryDate());

        em.persist(newCredential);

        List<Property<Object>> annotatedTypes = PropertyQueries.createQuery(storage.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(Stored.class)).getResultList();

        Property<Object> attributeName = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE_NAME);
        Property<Object> attributeValue = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE_VALUE);
        Property<Object> attributeType = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE_TYPE);
        Property<Object> attributeCredential = getConfig().getModelProperty(
                JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE);

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
                attributeType.setValue(newCredentialAttribute, property.getJavaClass().getName());
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
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_TYPE);
        Property<Object> effectiveProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EFFECTIVE_DATE);
        Property<Object> expiryProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EXPIRY_DATE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(agent);

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
            
            Property<Object> effectiveProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EFFECTIVE_DATE);
            Property<Object> expiryProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_EXPIRY_DATE);
            
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

            Property<Object> attributeCredential = getConfig().getModelProperty(
                    JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE);

            attributePredicates.add(builder.equal(attributeRoot.get(attributeCredential.getName()), instance));

            attributeCriteria.where(attributePredicates.toArray(new Predicate[attributePredicates.size()]));
            
            List<?> attributes = em.createQuery(attributeCriteria).getResultList();

            Property<Object> attributeName = getConfig().getModelProperty(
                    JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE_NAME);
            Property<Object> attributeValue = getConfig().getModelProperty(
                    JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_ATTRIBUTE_VALUE);

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
        Property<Object> identityTypeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY);
        Property<Object> typeProperty = getConfig().getModelProperty(PROPERTY_CREDENTIAL_TYPE);

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
        Root<?> root = criteria.from(getConfig().getCredentialClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Object agentInstance = lookupIdentityObjectById(agent);

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

}