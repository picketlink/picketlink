package org.picketlink.idm.jpa.internal;

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_VALUE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ENABLED;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ID;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_KEY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_PARTITION;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_EMAIL;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_FIRST_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_LAST_NAME;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.GroupUpdatedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
import org.picketlink.idm.event.RoleUpdatedEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.event.UserUpdatedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleGroupRole;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * Implementation of IdentityStore that stores its state in a relational database.
 * 
 * @author Shane Bryzak
 */
public class JPAIdentityStore implements IdentityStore<JPAIdentityStoreConfiguration> {

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
        try {
            Object identity = getConfig().getIdentityClass().newInstance();
            EntityManager em = getEntityManager();

            populateIdentityInstance(identity, identityType);

            em.persist(identity);
            em.flush();

            if (isUserType(identityType.getClass())) {
                UserCreatedEvent event = new UserCreatedEvent((User) identityType);
                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
                getContext().getEventBridge().raiseEvent(event);
            }
        } catch (Exception ex) {
            throw new IdentityManagementException("Exception while creating IdentityType [" + identityType + "].", ex);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        Object identity = lookupIdentityObjectById(identityType);

        populateIdentityInstance(identity, identityType);

        updateAttributes(identityType, identity);

        EntityManager em = getEntityManager();

        em.merge(identity);
        em.flush();

        if (isUserType(identityType.getClass())) {
            UserUpdatedEvent event = new UserUpdatedEvent((User) identityType);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (isRoleType(identityType.getClass())) {
            RoleUpdatedEvent event = new RoleUpdatedEvent((Role) identityType);
            event.getContext().setValue(EVENT_CONTEXT_ROLE_ENTITY, identity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (isGroupType(identityType.getClass())) {
            GroupUpdatedEvent event = new GroupUpdatedEvent((Group) identityType);
            event.getContext().setValue(EVENT_CONTEXT_GROUP_ENTITY, identity);
            getContext().getEventBridge().raiseEvent(event);
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        EntityManager em = getEntityManager();

        Object entity = lookupIdentityObjectById(identityType);

        if (isGroupType(identityType.getClass())) {
            Group group = (Group) identityType;

            if (entity != null) {
                disassociateChilds(group);
            }
        }

        removeIdentityObject(entity);

        em.flush();

        if (isUserType(identityType.getClass())) {
            User user = (User) identityType;

            UserDeletedEvent event = new UserDeletedEvent(user);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (isRoleType(identityType.getClass())) {
            Role role = (Role) identityType;

            RoleDeletedEvent event = new RoleDeletedEvent(role);
            event.getContext().setValue(EVENT_CONTEXT_ROLE_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (isGroupType(identityType.getClass())) {
            Group group = (Group) identityType;

            GroupDeletedEvent event = new GroupDeletedEvent(group);
            event.getContext().setValue(EVENT_CONTEXT_GROUP_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        }
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
            Object identity = lookupIdentityObjectById(new SimpleUser(id));

            if (identity != null) {
                user = (User) createFromIdentityInstance(identity);
                getContext().getCache().putUser(context.getRealm(), user);
            }
        }

        return user;
    }

    @Override
    public Group getGroup(String groupId) {
        if (groupId == null) {
            return null;
        }

        // Check the cache first
        Group group = getContext().getCache().lookupGroup(context.getRealm(), groupId);

        if (group == null) {
            Object identity = lookupIdentityObjectById(new SimpleGroup(groupId));

            if (identity != null) {
                group = convertGroupEntityToGroup(context.getRealm(), identity);

                populateAttributes(group, identity);
            }

            getContext().getCache().putGroup(context.getRealm(), group);
        }

        return group;
    }

    @Override
    public Group getGroup(String name, Group parent) {
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
        Role role = getContext().getCache().lookupRole(context.getRealm(), name);

        // If the cache doesn't have a reference to the Role, we have to look up it's identity object
        // and create a Role instance based on it
        if (role == null) {
            Object identity = lookupIdentityObjectById(new SimpleRole(name));

            if (identity != null) {
                role = convertRoleEntityToRole(context.getRealm(), identity);

                populateAttributes(role, identity);
            }

            getContext().getCache().putRole(context.getRealm(), role);
        }

        return role;

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        EntityManager em = getEntityManager();
        JPACriteriaQueryBuilder criteriaBuilder = new JPACriteriaQueryBuilder(this, identityQuery);

        List<Predicate> predicates = criteriaBuilder.getPredicates();

        CriteriaQuery<?> criteria = criteriaBuilder.getCriteria();

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> queryResult = em.createQuery(criteria).getResultList();
        List<T> result = new ArrayList<T>();

        for (Object identity : queryResult) {
            T identityType = (T) createFromIdentityInstance(identity);

            result.add(identityType);
        }

        return result;
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
    public void updateCredential(Agent agent, Object credential) {
        CredentialHandler handler = getContext().getCredentialUpdater(credential.getClass(), this);
        if (handler == null) {
            throw new SecurityConfigurationException(
                    "No suitable CredentialHandler available for updating Credentials of type [" + credential.getClass()
                            + "] for IdentityStore [" + this.getClass() + "]");
        }
        handler.update(agent, credential, this);
    }

    @Override
    public Agent getAgent(String id) {
        throw createNotImplementedYetException();
    }

    @Override
    public void setAttribute(IdentityType identity, Attribute<? extends Serializable> providedAttrib) {
        throw createNotImplementedYetException();
    }

    @Override
    public void removeAttribute(IdentityType identity, String name) {
        throw createNotImplementedYetException();
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        throw createNotImplementedYetException();
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
        Property<Object> roleModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
        Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);
        SimpleGroupRole groupRole = null;
        
        if (member instanceof User) {
            Role storedRole = null;
            Object identityRole = null;

            if (role != null) {
                storedRole = getRole(role.getName());
                identityRole = lookupIdentityObjectById(storedRole);
            }

            User storedUser = null;
            Object identityUser = null;

            if (member != null) {
                storedUser = getUser(((User) member).getId());
                identityUser = lookupIdentityObjectById(storedUser);
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
            
            groupRole = new SimpleGroupRole(storedUser, storedRole, storedGroup);
        } else if (member instanceof Group) {
            // FIXME implement Group membership, or return null
            return null;
        } else {
            throw new IllegalArgumentException("The member parameter must be an instance of User or Group");
        }
        
        return groupRole;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
        Property<Object> roleModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
        Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);
        
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
        Property<Object> memberModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER);
        Property<Object> roleModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE);
        Property<Object> groupModelProperty = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP);
        GroupRole groupRole = null;

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
        
        if (resultList.isEmpty()) {
            return null;
        }
        
        if (resultList.size() == 1) {
            User storedUser = getUser(((User) member).getId());
            Role storedRole = null;
            Group storedGroup = null;
            
            if (role != null) {
                storedRole = getRole(role.getName());
            }

            if (group != null) {
                storedGroup = getGroup(group.getName());
            }

            groupRole = new SimpleGroupRole(storedUser, storedRole, storedGroup);
        }

        return groupRole;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw createNotImplementedYetException();
    }

    private IdentityManagementException createNotImplementedYetException() {
        return new IdentityManagementException("Not implemented yet.");
    }

    public <T extends CredentialStorage> void storeCredential(Agent agent, T storage) {
        throw createNotImplementedYetException();
    }

    public <T extends CredentialStorage> T retrieveCredential(Agent agent, Class<T> storageClass) {
        throw createNotImplementedYetException();
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

        Property<Object> attributeNameProperty = getAttributeNameProperty();
        Property<Object> attributeIdentityProperty = getAttributeIdentityProperty();
        Property<Object> attributeValueProperty = getAttributeValueProperty();

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
            String attributeName = getAttributeNameProperty().getValue(attribute).toString();

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
        removeAttributes(identity, Collections.EMPTY_LIST);
    }

    private Property<Object> getAttributeValueProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_VALUE);
    }

    private List<?> findAttributes(IdentityType identityType, String idValue, Attribute<? extends Serializable> userAttribute) {
        Property<Object> attributeIdentityProperty = getAttributeIdentityProperty();

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root.join(attributeIdentityProperty.getName());

        if (isUserType(identityType.getClass())) {
            predicates.add(builder.equal(join.get(getIdentityIdProperty().getName()), idValue));
        } else {
            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), idValue));
        }

        predicates.add(builder.equal(root.get(getAttributeNameProperty().getName()), userAttribute.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private List<?> findAttributes(Object object) {
        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
        Root<?> root = criteria.from(getConfig().getAttributeClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(builder.equal(root.get(getAttributeIdentityProperty().getName()), object));
        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        return em.createQuery(criteria).getResultList();
    }

    private Property<Object> getIdentityIdProperty() {
        return getConfig().getModelProperty(PROPERTY_IDENTITY_ID);
    }

    private Property<Object> getAttributeIdentityProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_IDENTITY);
    }

    private Property<Object> getAttributeNameProperty() {
        return getConfig().getModelProperty(PROPERTY_ATTRIBUTE_NAME);
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

        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                getIdentityDiscriminator(identityType.getClass())));

        if (isUserType(identityType.getClass())) {
            predicates.add(builder.equal(root.get(getIdentityIdProperty().getName()), id));
        } else if (isGroupType(identityType.getClass()) || isRoleType(identityType.getClass())) {
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), id));
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

    private Object lookupIdentityObjectByKey(String key) {
        if (key == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_KEY).getName()), key));

        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {
            // We need to determine what type of key we're dealing with.. if it's a User key, then
            // we need to set the Realm value
            if (key.startsWith(User.KEY_PREFIX)) {
                if (getContext().getRealm() == null) {
                    throw new SecurityException("Cannot look up User key without a provided realm.");
                }

                predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                        lookupPartitionObject(getContext().getRealm())));

                // Otherwise if it's a group or role key, we need to set either the realm or the tier
            } else if (key.startsWith(Group.KEY_PREFIX) || key.startsWith(Role.KEY_PREFIX)) {
                if (getContext().getRealm() != null && getContext().getTier() != null) {
                    throw new SecurityException("Ambiguous lookup for key [" + key
                            + "] - both Realm and Tier have been specified in context.");
                }

                if (getContext().getRealm() != null) {
                    predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                            lookupPartitionObject(getContext().getRealm())));
                } else if (getContext().getTier() != null) {
                    predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                            lookupPartitionObject(getContext().getTier())));
                } else {
                    throw new SecurityException("Cannot look up key [" + key + "] without a provided realm or tier.");
                }
            }
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.isEmpty()) {
            return null;
        }

        if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new SecurityException("Error looking up identity by key - ambiguous identities found for key: [" + key + "]");
        }
    }

    private void removeIdentityObject(Object object) {
        EntityManager em = getEntityManager();

        // Remove credentials
        if (getConfig().getCredentialClass() != null) {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getCredentialClass());
            Root<?> root = criteria.from(getConfig().getCredentialClass());
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates
                    .add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY).getName()), object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }
        }

        // Remove attributes
        if (getConfig().getAttributeClass() != null) {
            List<?> results = findAttributes(object);
            for (Object result : results) {
                em.remove(result);
            }
        }

        // Remove memberships - this takes a little more work because the identity may be
        // a member, a role or a group
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

        // Remove the identity object itself
        em.remove(object);
    }

    private <P> P getModelProperty(Class<P> propertyType, Object instance, String propertyName) {
        @SuppressWarnings("unchecked")
        Property<P> property = (Property<P>) getConfig().getModelProperty(propertyName);
        return property == null ? null : property.getValue(instance);
    }

    private void setModelProperty(Object instance, String propertyName, Object value) {
        setModelProperty(instance, propertyName, value, false);
    }

    private void setModelProperty(Object instance, String propertyName, Object value, boolean required) {
        if (getConfig().isModelPropertySet(propertyName)) {
            getConfig().getModelProperty(propertyName).setValue(instance, value);
        } else if (required) {
            throw new SecurityException("Model property [" + propertyName + "] has not been configured.");
        }
    }

    private String getIdentityDiscriminator(Class<? extends IdentityType> identityType) {
        return getConfig().getIdentityTypeDiscriminator(identityType);
    }

    /**
     * <p>
     * Populates the given {@link Object} argument representing a Identity Class (from the config) with the information from the
     * specified {@link IdentityType}.
     * </p>
     * 
     * @param toIdentity
     * @param fromIdentityType
     */
    private void populateIdentityInstance(Object toIdentity, IdentityType fromIdentityType) {
        // populate the common properties from IdentityType
        setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, getIdentityDiscriminator(fromIdentityType.getClass()),
                true);
        setModelProperty(toIdentity, PROPERTY_IDENTITY_KEY, fromIdentityType.getKey(), true);
        setModelProperty(toIdentity, PROPERTY_IDENTITY_ENABLED, fromIdentityType.isEnabled());
        setModelProperty(toIdentity, PROPERTY_IDENTITY_CREATED, fromIdentityType.getCreatedDate());
        setModelProperty(toIdentity, PROPERTY_IDENTITY_EXPIRES, fromIdentityType.getExpirationDate());

        if (getContext().getRealm() != null) {
            setModelProperty(toIdentity, PROPERTY_IDENTITY_PARTITION, lookupPartitionObject(getContext().getRealm()));
        }

        // IdentityType specific properties
        if (isUserType(fromIdentityType.getClass())) {
            User fromUser = (User) fromIdentityType;

            // user properties
            setModelProperty(toIdentity, PROPERTY_IDENTITY_ID, fromUser.getId(), true);
            setModelProperty(toIdentity, PROPERTY_USER_FIRST_NAME, fromUser.getFirstName());
            setModelProperty(toIdentity, PROPERTY_USER_LAST_NAME, fromUser.getLastName());
            setModelProperty(toIdentity, PROPERTY_USER_EMAIL, fromUser.getEmail());
        } else if (isRoleType(fromIdentityType.getClass())) {
            Role fromRole = (Role) fromIdentityType;
            setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME, fromRole.getName());
        } else if (isGroupType(fromIdentityType.getClass())) {
            Group fromGroup = (Group) fromIdentityType;

            setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME, fromGroup.getName());

            if (fromGroup.getParentGroup() != null) {
                Object parentIdentity = lookupIdentityObjectById(fromGroup.getParentGroup());

                if (parentIdentity == null) {
                    add(fromGroup.getParentGroup());
                    parentIdentity = lookupIdentityObjectById(fromGroup.getParentGroup());
                }

                setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP, parentIdentity, true);
            }
        }
    }

    private boolean isGroupType(Class<? extends IdentityType> identityType) {
        return Group.class.isAssignableFrom(identityType);
    }

    private boolean isRoleType(Class<? extends IdentityType> identityType) {
        return Role.class.isAssignableFrom(identityType);
    }

    private boolean isUserType(Class<? extends IdentityType> identityType) {
        return User.class.isAssignableFrom(identityType);
    }

    private void updateAttributes(IdentityType identityType, Object identity) {
        EntityManager em = getEntityManager();

        if (identityType.getAttributes() != null && !identityType.getAttributes().isEmpty()) {
            List<String> attributesToRetain = new ArrayList<String>();

            for (Attribute<? extends Serializable> userAttribute : identityType.getAttributes()) {
                attributesToRetain.add(userAttribute.getName());

                try {
                    MappedAttribute mappedAttribute = getConfig().getAttributeProperties().get(userAttribute.getName());

                    // of the attribute was mapped as a property of the identity class
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
     * Disassociates the given {@link Group} from its childs.
     * </p>
     * 
     * @param group
     */
    private void disassociateChilds(Group group) {
        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        Join<?, ?> join = root
                .join(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP).getName());

        predicates.add(builder.equal(
                join.get(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME).getName()),
                group.getName()));

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> resultList = em.createQuery(criteria).getResultList();

        for (Object object : resultList) {
            getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP).setValue(object, null);
            em.merge(object);
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

        if (isUserType(identityType.getClass())) {
            value = ((User) identityType).getId();
        } else if (isRoleType(identityType.getClass())) {
            value = ((Role) identityType).getName();
        } else if (isGroupType(identityType.getClass())) {
            value = ((Group) identityType).getName();
        }

        return value;
    }

    /**
     * <p>
     * Given the provided {@link Object} instance which is a instance of the IdentityClass, creates its corresponding
     * {@link IdentityType}.
     * </p>
     * 
     * @param identity
     * @return
     */
    private IdentityType createFromIdentityInstance(Object identity) {
        String discriminator = getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getValue(identity).toString();
        IdentityType identityType = null;
        String idValue = null;

        if (discriminator.equals(getConfig().getIdentityTypeUser())) {
            idValue = getIdentityIdProperty().getValue(identity).toString();

            User user = new SimpleUser(idValue);

            user.setFirstName(getModelProperty(String.class, identity, PROPERTY_USER_FIRST_NAME));
            user.setLastName(getModelProperty(String.class, identity, PROPERTY_USER_LAST_NAME));
            user.setEmail(getModelProperty(String.class, identity, PROPERTY_USER_EMAIL));

            identityType = user;
        } else if (discriminator.equals(getConfig().getIdentityTypeRole())) {
            idValue = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getValue(identity).toString();

            Role role = convertRoleEntityToRole(this.context.getRealm(), identity);

            identityType = role;
        } else if (discriminator.equals(getConfig().getIdentityTypeGroup())) {
            idValue = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getValue(identity).toString();

            Group group = convertGroupEntityToGroup(this.context.getRealm(), identity);

            identityType = group;
        }

        identityType.setEnabled(getModelProperty(Boolean.class, identity, PROPERTY_IDENTITY_ENABLED));
        identityType.setExpirationDate(getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        identityType.setCreatedDate(getModelProperty(Date.class, identity,
                JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));

        populateAttributes(identityType, identity);

        return identityType;
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

                Join identityPropertyJoin = attributeClassRoot.join(getAttributeIdentityProperty().getName());
                String propertyNameToJoin = getIdentityIdProperty().getName();

                if (isRoleType(identityType.getClass()) || isGroupType(identityType.getClass())) {
                    propertyNameToJoin = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName();
                }

                predicates.add(builder.equal(identityPropertyJoin.get(propertyNameToJoin), getIdentifierValue(identityType)));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

                if (!results.isEmpty()) {
                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getAttributeNameProperty();
                        Property<Object> attributeValueProperty = getAttributeValueProperty();

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

    private Group convertGroupEntityToGroup(Partition partition, Object identity) {
        String name = getModelProperty(String.class, identity, PROPERTY_IDENTITY_NAME);

        Object parentInstance = getModelProperty(Object.class, identity, PROPERTY_PARENT_GROUP);

        SimpleGroup group = null;

        if (parentInstance != null) {
            String parentId = getModelProperty(String.class, parentInstance, PROPERTY_IDENTITY_ID);

            Group parent = getContext().getCache().lookupGroup(partition, parentId);

            if (parent == null) {
                parent = convertGroupEntityToGroup(partition, parentInstance);
                getContext().getCache().putGroup(partition, parent);
            }

            group = new SimpleGroup(name, parent);
        } else {
            group = new SimpleGroup(name);
        }

        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {

            // TODO implement cache support for partitions
            Object partitionInstance = getModelProperty(Object.class, identity, PROPERTY_IDENTITY_PARTITION);
            group.setPartition(convertPartitionEntityToPartition(partitionInstance));

        } else {
            group.setPartition(partition);
        }

        group.setEnabled(getModelProperty(Boolean.class, identity, PROPERTY_IDENTITY_ENABLED));
        group.setExpirationDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        group.setCreatedDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));

        return group;
    }

    private Role convertRoleEntityToRole(Partition partition, Object identity) {
        String name = getModelProperty(String.class, identity, PROPERTY_IDENTITY_NAME);

        SimpleRole role = new SimpleRole(name);

        role.setEnabled(getModelProperty(Boolean.class, identity, PROPERTY_IDENTITY_ENABLED));
        role.setExpirationDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        role.setCreatedDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));

        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {

            // TODO implement cache support for partitions
            Object partitionInstance = getModelProperty(Object.class, identity, PROPERTY_IDENTITY_PARTITION);
            role.setPartition(convertPartitionEntityToPartition(partitionInstance));

        } else {
            role.setPartition(partition);
        }

        return role;
    }

    private Partition convertPartitionEntityToPartition(Object instance) {
        return null;
    }

    private Object lookupPartitionObject(Partition partition) {
        // TODO implement realm lookup
        return null;
    }
}
