package org.picketlink.idm.jpa.internal;

import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_ATTRIBUTE_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_CREDENTIAL_IDENTITY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_DISCRIMINATOR;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_ID;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_IDENTITY_KEY;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_GROUP;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_MEMBER;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_MEMBERSHIP_ROLE;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_EMAIL;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_FIRST_NAME;
import static org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.PROPERTY_USER_LAST_NAME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.event.GroupDeletedEvent;
import org.picketlink.idm.event.UserCreatedEvent;
import org.picketlink.idm.event.UserDeletedEvent;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration.MappedAttribute;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
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

    private JPAIdentityStoreConfiguration config;
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

    @Override
    public Set<Feature> getFeatureSet() {
        //return featureSet;

        // TODO implement this!!
        Set<Feature> features = new HashSet<Feature>();
        features.add(Feature.all);
        return features;
    }

    @Override
    public void createUser(User user) {
        try {
            // Create the identity instance first
            Object identity = getConfig().getIdentityClass().newInstance();

            getConfig().getModelProperty(PROPERTY_IDENTITY_ID).setValue(identity, user.getId());

            getConfig().getModelProperty(PROPERTY_IDENTITY_DISCRIMINATOR).setValue(identity, getConfig().getIdentityTypeUser());

            getConfig().getModelProperty(PROPERTY_IDENTITY_KEY).setValue(identity, user.getKey());

            if (getConfig().isModelPropertySet(PROPERTY_USER_FIRST_NAME)) {
                getConfig().getModelProperty(PROPERTY_USER_FIRST_NAME).setValue(identity, user.getFirstName());
            }

            if (getConfig().isModelPropertySet(PROPERTY_USER_LAST_NAME)) {
                getConfig().getModelProperty(PROPERTY_USER_LAST_NAME).setValue(identity, user.getLastName());
            }

            if (getConfig().isModelPropertySet(PROPERTY_USER_EMAIL)) {
                getConfig().getModelProperty(PROPERTY_USER_EMAIL).setValue(identity, user.getEmail());
            }

            EntityManager em = getEntityManager();

            // Create any related entities that may be containers for attribute values
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

    private Object lookupIdentityObjectByKey(String key) {
        final String annotatedEntityName = getConfig().getIdentityClass().getAnnotation(Entity.class).name();
        final String entityName = ("".equals(annotatedEntityName) ? 
                getConfig().getIdentityClass().getSimpleName() : annotatedEntityName);

        return getEntityManager().createQuery("select i from " +
                entityName + " i where i." +
                getConfig().getModelProperty(PROPERTY_IDENTITY_KEY).getName() +
                " = :key")
                .setParameter("key", key)
                .getSingleResult();
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
    public void removeUser(User user) {
        Object entity = lookupIdentityObjectByKey(user.getKey());
        removeIdentityObject(entity);

        UserDeletedEvent event = new UserDeletedEvent(user);
        event.getContext().setValue(EVENT_CONTEXT_USER_ENTITY, entity);
        getContext().getEventBridge().raiseEvent(event);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeGroup(Group group) {
        Object entity = lookupIdentityObjectByKey(group.getKey());
        removeIdentityObject(entity);

        GroupDeletedEvent event = new GroupDeletedEvent(group);
        event.getContext().setValue(EVENT_CONTEXT_GROUP_ENTITY, entity);
        getContext().getEventBridge().raiseEvent(event);
    }

    @Override
    public Group getGroup(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeRole(Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public Role getRole(String name) {
        // TODO Auto-generated method stub
        return null;
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
    public void updateUser(User user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createGroup(Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Group getGroup(String name, Group parent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createRole(Role role) {
        // TODO Auto-generated method stub
        
    }

}
