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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.RoleDeletedEvent;
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
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
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
        try {
            Object identity = getConfig().getIdentityClass().newInstance();
            EntityManager em = getEntityManager();

            setModelProperty(identity, PROPERTY_IDENTITY_CREATED, identityType.getCreatedDate());

            if (User.class.isInstance(identityType)) {

                User user = (User) identityType;

                populateIdentityInstance(identity, user);

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

                UserCreatedEvent event = new UserCreatedEvent(user);
                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
                getContext().getEventBridge().raiseEvent(event);

                if (user.getAttributes() != null && !user.getAttributes().isEmpty()) {
                    for (Attribute<? extends Serializable> attrib : user.getAttributes()) {
                        setAttribute(user, attrib);
                    }
                }
            } else if (Role.class.isInstance(identityType)) {
                try {
                    Role role = (Role) identityType;

                    populateIdentityInstance(identity, role);
                } catch (Exception ex) {
                    throw new IdentityManagementException("Exception while creating user", ex);
                }
            } else if (Group.class.isInstance(identityType)) {
                try {
                    Group group = (Group) identityType;

                    populateIdentityInstance(identity, group);

                    if (group.getParentGroup() != null) {
                        Object parentIdentity = lookupIdentityObjectById(Group.class, group.getParentGroup().getName());

                        if (parentIdentity == null) {
                            add(group.getParentGroup());
                            parentIdentity = lookupIdentityObjectById(Group.class, group.getParentGroup().getName());
                        }

                        setModelProperty(identity, JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP, parentIdentity, true);
                    }
                } catch (Exception ex) {
                    throw new IdentityManagementException("Exception while creating user", ex);
                }
            }

            em.persist(identity);
            em.flush();
        } catch (Exception ex) {
            throw new IdentityManagementException("Exception while creating user", ex);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        if (User.class.isInstance(identityType)) {
            try {
                User user = (User) identityType;

                // Create the identity entity instance first
                Object identity = lookupIdentityObjectById(User.class, user.getId());

                populateIdentityInstance(identity, user);

                updateAttributes(user, user.getId(), identity);

                EntityManager em = getEntityManager();

                em.merge(identity);
                em.flush();

                UserUpdatedEvent event = new UserUpdatedEvent(user);
                event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, identity);
                getContext().getEventBridge().raiseEvent(event);
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating user", ex);
            }
        } else if (Role.class.isInstance(identityType)) {
            try {
                Role role = (Role) identityType;

                // Create the identity entity instance first
                Object identity = lookupIdentityObjectByKey(String.format("%s%s", Role.KEY_PREFIX, role.getName()));

                populateIdentityInstance(identity, role);

                updateAttributes(role, role.getName(), identity);

                EntityManager em = getEntityManager();

                em.merge(identity);
                em.flush();
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating user", ex);
            }
        } else if (Group.class.isInstance(identityType)) {
            try {
                Group group = (Group) identityType;

                Group storedGroup = getGroup(group.getName());

                // Create the identity entity instance first
                Object identity = lookupIdentityObjectByKey(storedGroup.getKey());

                populateIdentityInstance(identity, group);

                updateAttributes(group, group.getName(), identity);

                EntityManager em = getEntityManager();

                em.merge(identity);
                em.flush();
            } catch (Exception ex) {
                throw new IdentityManagementException("Exception while creating user", ex);
            }
        }
    }

    private void updateAttributes(IdentityType identityType, String idValue, Object identity) {
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
                        List<?> results = findAttributes(identityType, idValue, userAttribute);

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

    @Override
    public void remove(IdentityType identityType) {
        EntityManager em = getEntityManager();

        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;
            Object entity = lookupIdentityObjectById(User.class, user.getId());
            removeIdentityObject(entity);

            UserDeletedEvent event = new UserDeletedEvent(user);
            event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            Object entity = lookupIdentityObjectById(Group.class, group.getName());

            if (entity != null) {
                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
                Root<?> root = criteria.from(getConfig().getIdentityClass());
                List<Predicate> predicates = new ArrayList<Predicate>();

                Join<?, ?> join = root.join(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP)
                        .getName());

                predicates.add(builder.equal(
                        join.get(getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME).getName()),
                        group.getName()));

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> resultList = em.createQuery(criteria).getResultList();

                for (Object object : resultList) {
                    getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_PARENT_GROUP).setValue(object, null);
                    em.merge(object);
                }

                removeIdentityObject(entity);

                GroupDeletedEvent event = new GroupDeletedEvent(group);
                event.getContext().setValue(EVENT_CONTEXT_GROUP_ENTITY, entity);
                getContext().getEventBridge().raiseEvent(event);
            }

        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            Object entity = lookupIdentityObjectByKey(role.getKey());
            removeIdentityObject(entity);

            RoleDeletedEvent event = new RoleDeletedEvent(role);
            event.getContext().setValue(EVENT_CONTEXT_ROLE_ENTITY, entity);
            getContext().getEventBridge().raiseEvent(event);
        }

        em.flush();
    }

    @Override
    public Agent getAgent(String id) {
        // TODO implement
        return null;
    }

    @Override
    public User getUser(String id) {
        // Check the cache first
        User user = getContext().getCache().lookupUser(context.getRealm(), id);

        // If the cache doesn't have a reference to the User, we have to look up it's identity object
        // and create a User instance based on it
        if (user == null) {
            Object identity = lookupIdentityObjectById(User.class, id);

            if (identity != null) {
                user = (User) createFromIdentityInstance(identity);
                getContext().getCache().putUser(context.getRealm(), user);
            }
        }

        return user;
    }

    private IdentityType createFromIdentityInstance(Object identity) {
        String discriminator = getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getValue(identity).toString();
        IdentityType identityType = null;
        String idValue = null;
        
        if (discriminator.equals(getConfig().getIdentityTypeUser())) {
            idValue = getIdentityIdProperty().getValue(identity).toString();

            User user = new SimpleUser(idValue);

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
        identityType.setExpirationDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES));
        identityType.setCreatedDate(getModelProperty(Date.class, identity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED));
        
        populateAttributes(identityType, idValue, identity);

        return identityType;
    }

    private void populateAttributes(IdentityType identityType, String idValue, Object identity) {
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

                        mappedName = annotation.name();

                        field.setAccessible(true);

                        value = field.get(identity);
                    }

                    identityType.setAttribute(new Attribute<Serializable>(mappedName, (Serializable) value));
                }
            }

            if (getConfig().getAttributeClass() != null) {
                EntityManager em = getEntityManager();

                CriteriaBuilder builder = em.getCriteriaBuilder();
                CriteriaQuery<?> criteria = builder.createQuery(getConfig().getAttributeClass());
                Root<?> root = criteria.from(getConfig().getAttributeClass());
                List<Predicate> predicates = new ArrayList<Predicate>();

                String name = getAttributeIdentityProperty().getName();

                Join join = root.join(name);

                if (User.class.isInstance(identityType)) {
                    User user = (User) identityType;

                    predicates.add(builder.equal(join.get(getIdentityIdProperty().getName()), user.getId()));
                } else if (Role.class.isInstance(identityType)) {
                    Role role = (Role) identityType;

                    predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                            role.getName()));
                } else if (Group.class.isInstance(identityType)) {
                    Group group = (Group) identityType;

                    predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()),
                            group.getName()));
                }

                criteria.where(predicates.toArray(new Predicate[predicates.size()]));

                List<?> results = em.createQuery(criteria).getResultList();

                if (!results.isEmpty()) {
                    Map<String, List<Serializable>> attributes = new HashMap<String, List<Serializable>>();

                    for (Object object : results) {
                        Property<Object> attributeNameProperty = getAttributeNameProperty();
                        Property<Object> attributeValueProperty = getAttributeValueProperty();

                        String attribName = (String) attributeNameProperty.getValue(object);
                        Serializable attribValue = (Serializable) attributeValueProperty.getValue(object);

                        List<Serializable> attributeValues = attributes.get(attribName);

                        if (attributeValues == null) {
                            attributeValues = new ArrayList<Serializable>();
                            attributes.put(attribName, attributeValues);
                        }

                        attributeValues.add(attribValue);
                    }

                    Set<Entry<String, List<Serializable>>> entrySet = attributes.entrySet();

                    for (Entry<String, List<Serializable>> entry : entrySet) {
                        List<Serializable> values = entry.getValue();
                        Serializable value = null;

                        if (values.size() > 1) {
                            value = values.toArray(new String[values.size()]);
                        } else {
                            value = values.get(0);
                        }

                        identityType.setAttribute(new Attribute<Serializable>(entry.getKey(), value));
                    }
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error setting attribute.", e);
        }
    }

    @Override
    public Group getGroup(String groupId) {
        // Check the cache first
        Group group = getContext().getCache().lookupGroup(context.getRealm(), groupId);

        if (group == null) {
            Object identity = lookupIdentityObjectById(Group.class, groupId);

            if (identity != null) {
                group = convertGroupEntityToGroup(context.getRealm(), identity);

                populateAttributes(group, group.getName(), identity);
            }

            getContext().getCache().putGroup(context.getRealm(), group);
        }

        return group;
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
        // TODO implement this
        return null;
    }

    @Override
    public Role getRole(String name) {
        // Check the cache first
        Role role = getContext().getCache().lookupRole(context.getRealm(), name);

        // If the cache doesn't have a reference to the Role, we have to look up it's identity object
        // and create a Role instance based on it
        if (role == null) {
            Object identity = lookupIdentityObjectByKey(String.format("%s%s", Role.KEY_PREFIX, name));

            if (identity != null) {
                role = convertRoleEntityToRole(context.getRealm(), identity);

                populateAttributes(role, role.getName(), identity);
            }

            getContext().getCache().putRole(context.getRealm(), role);
        }

        return role;

    }

    @Override
    public void setAttribute(IdentityType identity, Attribute<? extends Serializable> providedAttrib) {
        EntityManager em = getEntityManager();

        try {
            for (String attribName : getConfig().getAttributeProperties().keySet()) {
                MappedAttribute attrib = getConfig().getAttributeProperties().get(attribName);
                if (attrib.getIdentityProperty() != null && attrib.getIdentityProperty().getValue(identity) == null) {
                    Object instance = attrib.getIdentityProperty().getJavaClass().newInstance();
                    attrib.getIdentityProperty().setValue(identity, instance);

                    em.persist(instance);
                } else {
                    attrib.getAttributeProperty().setValue(identity, providedAttrib.getValue());
                }
            }
        } catch (Exception e) {
            throw new IdentityManagementException("Error setting attribute [" + providedAttrib + "] for [" + identity + "]", e);
        }
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
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public GroupRole getMembership(IdentityType member, Group group, Role role) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery) {
        EntityManager em = getEntityManager();
        Class<?> identityClass = getConfig().getIdentityClass();
        Class<T> typeClass = identityQuery.getIdentityType();
        
        Set<Entry<QueryParameter, Object[]>> parametersEntrySet = identityQuery.getParameters().entrySet();
        boolean hasCustomAttributes = false;
        
        for (Entry<QueryParameter, Object[]> entry : parametersEntrySet) {
            if (IdentityType.AttributeParameter.class.isInstance(entry.getKey())) {
                hasCustomAttributes = true;
            }
        }
        
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = null;
        Root<?> root = null;
        Root<?> attributeRoot = null;
        List<Predicate> predicates = new ArrayList<Predicate>();
        
        hasCustomAttributes = false;
        
        if (!hasCustomAttributes) {
            criteria = builder.createQuery(identityClass);
            root = criteria.from(identityClass);
        } else {
            criteria = builder.createQuery(getConfig().getAttributeClass());
            
            attributeRoot = criteria.from(getConfig().getAttributeClass());
            
            root = criteria.from(identityClass);
            
            predicates.add(builder.equal(attributeRoot.get(getAttributeIdentityProperty().getName()), root));
        }
        
        String discriminator = null;
        
        for (Entry<QueryParameter, Object[]> entry : parametersEntrySet) {
            QueryParameter queryParameter = entry.getKey();
            Object[] parameterValues = entry.getValue();
            String propertyName = null;
            String comparationType = "eq";

            if (User.class.isAssignableFrom(typeClass)) {
                discriminator = getConfig().getIdentityTypeUser();

                if (queryParameter.equals(User.ID)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_IDENTITY_ID).getName();
                }

                if (queryParameter.equals(User.FIRST_NAME)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_USER_FIRST_NAME).getName();
                }

                if (queryParameter.equals(User.LAST_NAME)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_USER_LAST_NAME).getName();
                }

                if (queryParameter.equals(User.EMAIL)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_USER_EMAIL).getName();
                }
            }
            
            if (Role.class.isAssignableFrom(typeClass)) {
                discriminator = getConfig().getIdentityTypeRole();

                if (queryParameter.equals(Role.NAME)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName();
                }

            }
            
            if (Group.class.isAssignableFrom(typeClass)) {
                discriminator = getConfig().getIdentityTypeGroup();

                if (queryParameter.equals(Group.NAME)) {
                    propertyName = getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName();
                }

            }

            if (queryParameter.equals(IdentityType.ENABLED)) {
                propertyName = getConfig().getModelProperty(PROPERTY_IDENTITY_ENABLED).getName();
            }

            if (queryParameter.equals(IdentityType.CREATED_DATE) || queryParameter.equals(IdentityType.CREATED_AFTER)
                    || queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                propertyName = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_CREATED).getName();
                
                if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                    comparationType = "gt";
                }

                if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                    comparationType = "lt";
                }
            }

            if (queryParameter.equals(IdentityType.EXPIRY_DATE) || queryParameter.equals(IdentityType.EXPIRY_AFTER)
                    || queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                propertyName = getConfig().getModelProperty(JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_EXPIRES).getName();
                
                if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                    comparationType = "gt";
                }

                if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                    comparationType = "lt";
                }
            }
            
            if (queryParameter instanceof IdentityType.AttributeParameter) {
                AttributeParameter customParameter = (AttributeParameter) queryParameter;
                
                Subquery<?> subquery = criteria.subquery(getConfig().getAttributeClass());
                Root fromProject = subquery.from(getConfig().getAttributeClass());
                Subquery<?> select = subquery.select(fromProject.get(getAttributeIdentityProperty().getName()));

                Predicate conjunction = builder.conjunction();
                
                conjunction.getExpressions().add(builder.equal(fromProject.get(getAttributeNameProperty().getName()), customParameter.getName()));
                conjunction.getExpressions().add((fromProject.get(getAttributeValueProperty().getName()).in((Object[]) parameterValues)));
                
                subquery.where(conjunction);
                
                subquery.groupBy(subquery.getSelection()).having(builder.equal(builder.count(subquery.getSelection()), parameterValues.length));

                predicates.add(builder.in(root).value(subquery));
            } else if (queryParameter.equals(Group.PARENT)) {
                Join<Object, Object> join = root.join(getConfig().getModelProperty(PROPERTY_PARENT_GROUP).getName());
                
                predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), parameterValues[0]));
            } else {
                if (comparationType.equals("eq")) {
                    predicates.add(builder.equal(root.get(propertyName), parameterValues[0]));
                } else if (comparationType.equals("gt")) {
                    predicates.add(builder.greaterThan(root.<Date> get(propertyName), (Date) parameterValues[0]));
                } else if (comparationType.equals("lt")) {
                    predicates.add(builder.lessThan(root.<Date> get(propertyName), (Date) parameterValues[0]));
                }
            }
        }
        
        predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                discriminator));
        
        criteria.where(predicates.toArray(new Predicate[predicates.size()]));
        
        List<?> queryResult = em.createQuery(criteria).getResultList();
        List<T> result = new ArrayList<T>();

        for (Object identity : queryResult) {
            T identityType = null;
            
            if (User.class.isAssignableFrom(typeClass)) {
                identityType = (T) createFromIdentityInstance(identity);
            } else if (Role.class.isAssignableFrom(typeClass)) {
                identityType = (T) createFromIdentityInstance(identity);
            } else if (Group.class.isAssignableFrom(typeClass)) {
                identityType = (T) createFromIdentityInstance(identity);
            }
            
            result.add(identityType);
        }

        return result;
    }

    @Override
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        throw new org.picketlink.idm.SecurityException("Not yet implemented") {};
    }

    @Override
    public Group getGroup(String name, Group parent) {
        Group group = getGroup(name);

        if (group.getParentGroup() == null || !group.getParentGroup().getName().equals(parent.getName())) {
            group = null;
        }

        return group;
    }

    public <T extends CredentialStorage> void storeCredential(Agent agent, T storage) {
        // TODO Auto-generated method stub

    }

    public <T extends CredentialStorage> T retrieveCredential(Agent agent, Class<T> storageClass) {
        // TODO Auto-generated method stub
        return null;
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

    private void populateIdentityInstance(Object toIdentity, IdentityType identityType) {
        setModelProperty(toIdentity, PROPERTY_IDENTITY_KEY, identityType.getKey(), true);
        setModelProperty(toIdentity, PROPERTY_IDENTITY_ENABLED, identityType.isEnabled());
        setModelProperty(toIdentity, PROPERTY_IDENTITY_EXPIRES, identityType.getExpirationDate());

        if (User.class.isAssignableFrom(identityType.getClass())) {
            User fromUser = (User) identityType;

            // user properties
            setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, getConfig().getIdentityTypeUser(), true);
            setModelProperty(toIdentity, PROPERTY_IDENTITY_ID, fromUser.getId(), true);
            setModelProperty(toIdentity, PROPERTY_USER_FIRST_NAME, fromUser.getFirstName());
            setModelProperty(toIdentity, PROPERTY_USER_LAST_NAME, fromUser.getLastName());
            setModelProperty(toIdentity, PROPERTY_USER_EMAIL, fromUser.getEmail());
        } else if (Role.class.isAssignableFrom(identityType.getClass())) {
            Role fromRole = (Role) identityType;

            setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, getConfig().getIdentityTypeRole(), true);
            setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME, fromRole.getName());
        } else if (Group.class.isAssignableFrom(identityType.getClass())) {
            Group fromGroup = (Group) identityType;

            setModelProperty(toIdentity, PROPERTY_IDENTITY_DISCRIMINATOR, getConfig().getIdentityTypeGroup(), true);
            setModelProperty(toIdentity, JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_NAME, fromGroup.getName());
        }

        if (getContext().getRealm() != null) {
            setModelProperty(toIdentity, PROPERTY_IDENTITY_PARTITION, lookupPartitionObject(getContext().getRealm()));
        }
    }

    private void storeAttribute(Object identity, Attribute<? extends Serializable> userAttribute)
            throws InstantiationException, IllegalAccessException {
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

        // recreate the attributes
        for (Object attribValue : values) {
            Object newInstance = getConfig().getAttributeClass().newInstance();

            attributeNameProperty.setValue(newInstance, userAttribute.getName());
            attributeValueProperty.setValue(newInstance, attribValue);
            attributeIdentityProperty.setValue(newInstance, identity);

            getEntityManager().persist(newInstance);
        }
    }

    private void removeAttributes(Object identity, List<String> attributesToRetain) {
        StringBuffer attributeNames = new StringBuffer();

        for (String string : attributesToRetain) {
            if (attributeNames.length() != 0) {
                attributeNames.append(",");
            }

            attributeNames.append("'").append(string).append("'");
        }

        Property<Object> attributeNameProperty = getAttributeNameProperty();
        Property<Object> attributeIdentityProperty = getAttributeIdentityProperty();

        Query query = getEntityManager().createQuery(
                "delete from " + getConfig().getAttributeClass().getSimpleName() + " where "
                        + attributeIdentityProperty.getName() + " = ? and " + attributeNameProperty.getName() + " not in ("
                        + attributeNames.toString() + ")");

        query.setParameter(1, identity);

        query.executeUpdate();
    }

    private void removeAllAttributes(Object identity) {
        String attributeClassName = getConfig().getAttributeClass().getName();
        String identityPropertyName = getAttributeIdentityProperty().getName();

        Query query = getEntityManager().createQuery(
                "delete from " + attributeClassName + " where " + identityPropertyName + " = ?");

        query.setParameter(1, identity);

        query.executeUpdate();
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

        if (User.class.isInstance(identityType)) {
            predicates.add(builder.equal(join.get(getIdentityIdProperty().getName()), idValue));
        } else {
            predicates.add(builder.equal(join.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), idValue));
        }

        predicates.add(builder.equal(root.get(getAttributeNameProperty().getName()), userAttribute.getName()));

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

    private Object lookupIdentityObjectById(Class<? extends IdentityType> cls, String id) {
        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<?> criteria = builder.createQuery(getConfig().getIdentityClass());
        Root<?> root = criteria.from(getConfig().getIdentityClass());
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (User.class.equals(cls)) {
            predicates.add(builder.equal(root.get(getIdentityIdProperty().getName()), id));
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                    getConfig().getIdentityTypeUser()));
        } else if (Group.class.equals(cls)) {
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), id));
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                    getConfig().getIdentityTypeGroup()));
        } else if (Role.class.equals(cls)) {
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_NAME).getName()), id));
            predicates.add(builder.equal(root.get(getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).getName()),
                    getConfig().getIdentityTypeRole()));
        } else {
            throw new SecurityException("Could not lookup identity by id - unsupported IdentityType [" + cls.getName() + "]");
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
}
