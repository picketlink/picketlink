package org.picketlink.idm.jpa.internal;

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * Implementation of IdentityStore that stores its state in a relational
 * database.
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

    protected EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw new IllegalStateException("Error while trying to determine EntityManager - context parameter not set.");
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    private Object lookupPartitionObject(Partition partition) {
        // TODO implement realm lookup
        return null;
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

    @Override
    public void add(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            try {
                User user = (User) identityType;

                // Create the identity entity instance first
                Object identity = getConfig().getIdentityClass().newInstance();

                setModelProperty(identity, PROPERTY_IDENTITY_ID, user.getId(), true);
                setModelProperty(identity, PROPERTY_IDENTITY_DISCRIMINATOR, getConfig().getIdentityTypeUser(), true);
                setModelProperty(identity, PROPERTY_IDENTITY_KEY, user.getKey(), true);

                setModelProperty(identity, PROPERTY_USER_FIRST_NAME, user.getFirstName());
                setModelProperty(identity, PROPERTY_USER_LAST_NAME, user.getLastName());
                setModelProperty(identity, PROPERTY_USER_EMAIL, user.getEmail());

                if (getContext().getRealm() != null) {
                    setModelProperty(identity, PROPERTY_IDENTITY_PARTITION, lookupPartitionObject(getContext().getRealm()));
                }

                EntityManager em = getEntityManager();

                // Create any related entities that may be containers for attribute values -
                // we are not setting the attribute values themselves, just creating the entities
                // that they are contained in, and setting a reference to those entities from within
                // the identity object.
                for (String attribName : getConfig().getAttributeProperties().keySet()) {
                    MappedAttribute attrib = getConfig().getAttributeProperties().get(attribName);
                    if (attrib.getIdentityProperty() != null && attrib.getIdentityProperty().getValue(identity) == null) {
                        Object instance = attrib.getIdentityProperty().getJavaClass().newInstance();
                        attrib.getIdentityProperty().setValue(identity, instance);

                        em.persist(instance);
                    }
                }

                em.persist(identity);

                UserCreatedEvent event = new UserCreatedEvent(user);
                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
                getContext().getEventBridge().raiseEvent(event);

                if (user.getAttributes() != null && !user.getAttributes().isEmpty()) {
                    for (Attribute<? extends Serializable> attrib : user.getAttributes()) {
                        setAttribute(user, attrib);
                    }
                }

                em.flush();

            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating user", ex);
            }
        }
    }

    private Object lookupIdentityObjectById(Class<? extends IdentityType> cls, String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(builder.equal(
                root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_ID).getName()), 
                id));

        if (User.class.equals(cls)) {
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()), 
                    getConfig().getIdentityTypeUser()));
        } else if (Group.class.equals(cls)) {
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()), 
                    getConfig().getIdentityTypeGroup()));
        } else if (Role.class.equals(cls)) {
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()), 
                    getConfig().getIdentityTypeRole()));
        } else {
            throw new SecurityException("Could not lookup identity by id - unsupported IdentityType [" 
                    + cls.getName() + "]");
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new SecurityException("Error looking up identity by id - ambiguous identities found for id: [" + 
                    id + "]");
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
        predicates.add(builder.equal(
                root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_KEY).getName()), 
                key));

        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {
            // We need to determine what type of key we're dealing with.. if it's a User key, then
            // we need to set the Realm value
            if (key.startsWith(User.KEY_PREFIX)) {
                if (getContext().getRealm() == null) {
                    throw new SecurityException("Cannot look up User key without a provided realm.");
                }

                predicates.add(builder.equal(
                        root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                        lookupPartitionObject(getContext().getRealm())));

            // Otherwise if it's a group or role key, we need to set either the realm or the tier
            } else if (key.startsWith(Group.KEY_PREFIX) || key.startsWith(Role.KEY_PREFIX)) {
                if (getContext().getRealm() != null && getContext().getTier() != null) {
                    throw new SecurityException(
                            "Ambiguous lookup for key [" + key + 
                            "] - both Realm and Tier have been specified in context.");
                }

                if (getContext().getRealm() != null) {
                    predicates.add(builder.equal(
                            root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                            lookupPartitionObject(getContext().getRealm())));
                } else if (getContext().getTier() != null) {
                    predicates.add(builder.equal(
                            root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_PARTITION).getName()),
                            lookupPartitionObject(getContext().getTier())));
                } else {
                    throw new SecurityException("Cannot look up key [" + key + "] without a provided realm or tier.");
                }
            }
        }

        criteria.where(predicates.toArray(new Predicate[predicates.size()]));

        List<?> results = em.createQuery(criteria).getResultList();

        if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new SecurityException("Error looking up identity by key - ambiguous identities found for key: [" + 
                    key + "]");
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
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_CREDENTIAL_IDENTITY).getName()), 
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }
        }

        // Remove attributes
        if (getConfig().getAttributeClass() != null) {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
            Root<?> root = criteria.from(getConfig().getAttributeClass());
            List<Predicate> predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_ATTRIBUTE_IDENTITY).getName()), 
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
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
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_MEMBER).getName()), 
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            List<?> results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

            criteria = builder.createQuery(getConfig().getMembershipClass());
            root = criteria.from(getConfig().getMembershipClass());
            predicates.clear();
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_GROUP).getName()), 
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

            criteria = builder.createQuery(getConfig().getMembershipClass());
            root = criteria.from(getConfig().getMembershipClass());
            predicates.clear();
            predicates.add(builder.equal(
                    root.get(getConfig().getModelProperty(PROPERTY_MEMBERSHIP_ROLE).getName()), 
                    object));
            criteria.where(predicates.toArray(new Predicate[predicates.size()]));

            results = em.createQuery(criteria).getResultList();
            for (Object result : results) {
                em.remove(result);
            }

        }

        // Remove the identity object itself
        em.remove(object);
    }

    @Override
    public void remove(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;
            Object entity = lookupIdentityObjectById(User.class, user.getId());
            removeIdentityObject(entity);

            UserDeletedEvent event = new UserDeletedEvent(user);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            Object entity = lookupIdentityObjectByKey(group.getKey());
            removeIdentityObject(entity);

            GroupDeletedEvent event = new GroupDeletedEvent(group);
            event.getContext().setValue(EVENT_CONTEXT_GROUP_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            Object entity = lookupIdentityObjectByKey(role.getKey());
            removeIdentityObject(entity);

            RoleDeletedEvent event = new RoleDeletedEvent(role);
            event.getContext().setValue(EVENT_CONTEXT_ROLE_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        }
    }

    @Override
    public boolean validateCredential(User user, Credential credential) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        // TODO Auto-generated method stub

    }

    @Override
    public User getUser(String id) {
        // Check the cache first
        User user = getContext().getCache().lookupUser(context.getRealm(), id);

        // If the cache doesn't have a reference to the User, we have to look up it's identity object
        // and create a User instance based on it
        if (user == null) {
            Object instance = lookupIdentityObjectById(User.class, id);

            user = new SimpleUser(id);
            user.setFirstName(getModelProperty(String.class, instance, PROPERTY_USER_FIRST_NAME));
            user.setLastName(getModelProperty(String.class, instance, PROPERTY_USER_LAST_NAME));
            user.setEmail(getModelProperty(String.class, instance, PROPERTY_USER_EMAIL));

            // TODO we need to also set attribute values
            //user.setAttribute(attribute);

            getContext().getCache().putUser(context.getRealm(), user);
        }

        return user;
    }

    @Override
    public Group getGroup(String groupId) {
        Partition partition;

        if (getContext().getRealm() != null && getContext().getTier() != null) {
            throw new SecurityException("Ambiguous context state while looking up group - both realm and tier have been set.");
        } else if (getContext().getRealm() != null) {
            partition = getContext().getRealm();
        } else if (getContext().getTier() != null) {
            partition = getContext().getTier();
        } else {
            throw new SecurityException("Error while looking up group - context defines no realm or tier");
        }

        // Check the cache first
        Group group = getContext().getCache().lookupGroup(partition, groupId);

        // If the cache doesn't have a reference to the Group, we have to look up it's identity object
        // and create a Group instance based on it
        if (group == null) {
            Object instance = lookupIdentityObjectById(Group.class, groupId);

            group = convertGroupEntityToGroup(partition, instance);

            // TODO we need to also set attribute values
            //group.setAttribute(attribute);

            getContext().getCache().putGroup(context.getRealm(), group);
        }

        return group;
    }

    private Group convertGroupEntityToGroup(Partition partition, Object instance) {
        String name = getModelProperty(String.class, instance, PROPERTY_IDENTITY_NAME);

        Object parentInstance = getModelProperty(Object.class, instance, PROPERTY_PARENT_GROUP);

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
            Object partitionInstance = getModelProperty(Object.class, instance, PROPERTY_IDENTITY_PARTITION);
            group.setPartition(convertPartitionEntityToPartition(partitionInstance));

        } else {
            group.setPartition(partition);
        }

        return group;
    }
    
    private Role convertRoleEntityToRole(Partition partition, Object instance) {
        String name = getModelProperty(String.class, instance, PROPERTY_IDENTITY_NAME);

        SimpleRole role = new SimpleRole(name);

        if (getConfig().isModelPropertySet(PROPERTY_IDENTITY_PARTITION)) {

            // TODO implement cache support for partitions
            Object partitionInstance = getModelProperty(Object.class, instance, PROPERTY_IDENTITY_PARTITION);
            role.setPartition(convertPartitionEntityToPartition(partitionInstance));

        } else {
            role.setPartition(partition);
        }

        return role;
    }

    private Partition convertPartitionEntityToPartition(Object instance) {
        // TODO implement this
        return null;
    }

    @Override
    public Role getRole(String name) {
        Partition partition;

        if (getContext().getRealm() != null && getContext().getTier() != null) {
            throw new SecurityException("Ambiguous context state while looking up role - both realm and tier have been set.");
        } else if (getContext().getRealm() != null) {
            partition = getContext().getRealm();
        } else if (getContext().getTier() != null) {
            partition = getContext().getTier();
        } else {
            throw new SecurityException("Error while looking up role - context defines no realm or tier");
        }

        // Check the cache first
        Role role = getContext().getCache().lookupRole(partition, name);

        // If the cache doesn't have a reference to the Role, we have to look up it's identity object
        // and create a Role instance based on it
        if (role == null) {
            Object instance = lookupIdentityObjectByKey(String.format("%s%s", Role.KEY_PREFIX, name));

            role = convertRoleEntityToRole(partition, instance);

            // TODO we need to also set attribute values
            //group.setAttribute(attribute);

            getContext().getCache().putRole(context.getRealm(), role);
        }

        return role;

    }

    @Override
    public void setAttribute(IdentityType identity, Attribute<? extends Serializable> attrib) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttribute(IdentityType identity, String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO implement this
        return null;
    }

    @Override
    public Membership createMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public Membership getMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IdentityType> fetchQueryResults(Map<QueryParameter, Object> parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Group getGroup(String name, Group parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateRole(Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateGroup(Group group) {

    }

    @Override
    public void update(IdentityType identityType) {
        // TODO Auto-generated method stub
        
    }

}
