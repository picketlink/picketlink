/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.jpa.schema.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.jpa.schema.AbstractDatabaseAttribute;
import org.picketlink.idm.jpa.schema.AbstractDatabaseIdentityType;
import org.picketlink.idm.jpa.schema.DatabaseGroup;
import org.picketlink.idm.jpa.schema.DatabaseGroupAttribute;
import org.picketlink.idm.jpa.schema.DatabaseMembership;
import org.picketlink.idm.jpa.schema.DatabaseRole;
import org.picketlink.idm.jpa.schema.DatabaseRoleAttribute;
import org.picketlink.idm.jpa.schema.DatabaseUser;
import org.picketlink.idm.jpa.schema.DatabaseUserAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.IdentityType.AttributeParameter;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An implementation of IdentityStore backed by a JPA datasource
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SimpleJPAIdentityStore extends AbstractIdentityStore<SimpleJPAIdentityStoreConfiguration> {
    
    public static final String INVOCATION_CTX_ENTITY_MANAGER = "CTX_ENTITY_MANAGER";
    private SimpleJPAIdentityStoreConfiguration config;
    private IdentityStoreInvocationContext context;
    
    @Override
    public void add(IdentityType identityType) {
        IdentityType toStoreInstance = null;

        if (isUserType(identityType.getClass())) {
            User user = (User) identityType;
            User newUser = null;

            if (!(user instanceof DatabaseUser)) {
                newUser = new DatabaseUser(user.getId());

                newUser.setFirstName(user.getFirstName());
                newUser.setLastName(user.getLastName());
                newUser.setEmail(user.getEmail());

                for (Attribute<? extends Serializable> attribute : user.getAttributes()) {
                    newUser.setAttribute(attribute);
                }
            } else {
                newUser = user;
            }

            toStoreInstance = newUser;
        } else if (isGroupType(identityType.getClass())) {
            Group group = (Group) identityType;
            DatabaseGroup newGroup = new DatabaseGroup(group.getName());

            if (group.getParentGroup() != null) {
                newGroup.setParentGroup(getStoredGroup(group.getParentGroup().getName()));                
            }

            toStoreInstance = newGroup;
        } else if (isRoleType(identityType.getClass())) {
            Role role = (Role) identityType;
            DatabaseRole newRole = new DatabaseRole(role.getName());

            toStoreInstance = newRole;
        }

        persist(toStoreInstance);
    }

    @Override
    public void remove(IdentityType identityType) {
        AbstractDatabaseIdentityType<?> toRemove = null;
        
        if (isUserType(identityType.getClass())) {
            User user = (User) identityType;

            if (user.getId() == null) {
                throw new IllegalArgumentException("User identifier nor provided.");
            }
            
            toRemove = getStoredUser(user.getId());
        } else if (isGroupType(identityType.getClass())) {
            Group group = (Group) identityType;

            if (group.getId() == null) {
                throw new IllegalArgumentException("Group identifier not provided.");
            }
            
            toRemove = getStoredGroup(group.getName());
        } else if (isRoleType(identityType.getClass())) {
            Role role = (Role) identityType;

            if (role.getName() == null) {
                throw new IllegalArgumentException("Role name not provided.");
            }
            
            toRemove = getStoredRole(role.getName());
        }
        
        getEntityManager().remove(toRemove);
        getEntityManager().flush();
    }

    @Override
    public User getUser(final String name) {
        User storedUser = getStoredUser(name);
        
        if (storedUser == null) {
            return null;
        }
        
        User user = new SimpleUser(name);
        
        user.setEmail(storedUser.getEmail());
        user.setCreatedDate(storedUser.getCreatedDate());
        user.setEnabled(storedUser.isEnabled());
        user.setExpirationDate(storedUser.getExpirationDate());
        user.setFirstName(storedUser.getFirstName());
        user.setLastName(storedUser.getLastName());
        
        Collection<Attribute<? extends Serializable>> attributes = storedUser.getAttributes();
        
        for (Attribute<? extends Serializable> attribute : attributes) {
            user.setAttribute(attribute);
        }
        
        return user;
    }

    private DatabaseUser getStoredUser(final String name) {
        return (DatabaseUser) findIdentityTypeByKey("id", name, NamedQueries.USER_LOAD_BY_KEY);
    }

    @Override
    public Group getGroup(String name) {
        DatabaseGroup storedGroup = getStoredGroup(name);
        
        if (storedGroup == null) {
            return null;
        }
        
        SimpleGroup group = null;
        
        
        if (storedGroup.getParentGroup() == null) {
            group = new SimpleGroup(name);
        } else {
            group = new SimpleGroup(name, getGroup(storedGroup.getParentGroup().getName()));
        }
        
        group.setCreatedDate(storedGroup.getCreatedDate());
        group.setEnabled(storedGroup.isEnabled());
        group.setExpirationDate(storedGroup.getExpirationDate());
        
        Collection<Attribute<? extends Serializable>> attributes = storedGroup.getAttributes();
        
        for (Attribute<? extends Serializable> attribute : attributes) {
            group.setAttribute(attribute);
        }
        
        return group;
    }

    private DatabaseGroup getStoredGroup(String group) {
        return (DatabaseGroup) findIdentityTypeByKey("name", group, NamedQueries.GROUP_LOAD_BY_KEY);
    }

    @Override
    public Role getRole(String name) {
        DatabaseRole storedRole = getStoredRole(name);
        
        if (storedRole == null) {
            return null;
        }
        
        SimpleRole role = new SimpleRole(name);
        
        role.setCreatedDate(storedRole.getCreatedDate());
        role.setEnabled(storedRole.isEnabled());
        role.setExpirationDate(storedRole.getExpirationDate());
        
        Collection<Attribute<? extends Serializable>> attributes = storedRole.getAttributes();
        
        for (Attribute<? extends Serializable> attribute : attributes) {
            role.setAttribute(attribute);
        }
        
        return role;
    }

    private DatabaseRole getStoredRole(String role) {
        return (DatabaseRole) findIdentityTypeByKey("name", role, NamedQueries.ROLE_LOAD_BY_KEY);
    }

    @Override
    public GroupRole createMembership(IdentityType member, Group group, Role role) {
        if (member instanceof User) {
            DatabaseUser dbUser = (DatabaseUser) getStoredUser(((User) member).getId());
            DatabaseRole dbRole = null;
            DatabaseGroup dbGroup = null;
            
            if (role != null) {
                dbRole = (DatabaseRole) getStoredRole(role.getName());
            }
            
            if (group != null) {
                dbGroup = (DatabaseGroup) getStoredGroup(group.getName());
            }
            
            DatabaseMembership newMembership = new DatabaseMembership(dbUser, dbGroup, dbRole);

//            dbUser.getMemberships().add(newMembership);

            persist(newMembership);

            return newMembership;
        } else {
            throw new UnsupportedOperationException("Only members of type User are supported by this implementation.");
        }
    }

    @Override
    public void removeMembership(IdentityType member, Group group, Role role) {
        GroupRole membership = getMembership(member, group, role);

        if (membership != null) {
            getEntityManager().remove(membership);
            getEntityManager().flush();
        }
    }

    @Override
    public GroupRole getMembership(final IdentityType member, final Group group, final Role role) {
        Query query = null;
        DatabaseUser storedMember = null;
        DatabaseRole storedRole = null;
        DatabaseGroup storedGroup = null;
        
        if (member != null) {
            storedMember = getStoredUser(((User) member).getId());
        }

        if (role != null) {
            storedRole = getStoredRole(role.getName());
        }

        if (group != null) {
            storedGroup = getStoredGroup(group.getName());
        }

        String namedQuery = null;
        
        if (storedRole != null && storedGroup != null) {
            namedQuery = org.picketlink.idm.jpa.schema.NamedQueries.MEMBERSHIP_LOAD_BY_ALL;
        } else {
            if (storedRole != null) {
                namedQuery = org.picketlink.idm.jpa.schema.NamedQueries.MEMBERSHIP_LOAD_BY_MEMBER_ROLE;
            }
            
            if (storedGroup != null) {
                namedQuery = org.picketlink.idm.jpa.schema.NamedQueries.MEMBERSHIP_LOAD_BY_MEMBER_GROUP;
            }
        }
        
        query = getEntityManager().createNamedQuery(namedQuery);
        
        query.setParameter("member", storedMember);
        
        if (storedRole != null) {
            query.setParameter("role", storedRole);    
        }
        
        if (storedGroup != null) {
            query.setParameter("group", storedGroup);    
        }
        
        GroupRole loadedMembership = null;

        try {
            loadedMembership = (GroupRole) query.getSingleResult();
        } catch (NoResultException nre) {
        }

        return loadedMembership;
    }

    @Override
    public <T extends IdentityType> List<T> fetchQueryResults(final IdentityQuery<T> identityQuery) {
        EntityManager entityManager = getEntityManager();
        
        if (isUserType(identityQuery.getIdentityType())) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatabaseUser> criteriaQuery = criteriaBuilder.createQuery(DatabaseUser.class);

            Root<DatabaseUser> user = criteriaQuery.from(DatabaseUser.class);

            user.alias("resultClass");
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<Predicate>();

            Map<QueryParameter, Object[]> parameters = identityQuery.getParameters();
            Set<Entry<QueryParameter, Object[]>> parametersEntrySet = parameters.entrySet();

            for (Entry<QueryParameter, Object[]> entry : parametersEntrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] value = entry.getValue();

                if (User.ID.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(user.get("id"), value[0]));
                }

                if (User.EMAIL.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(user.get("email"), value[0]));
                }

                if (User.FIRST_NAME.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(user.get("firstName"), value[0]));
                }

                if (User.LAST_NAME.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(user.get("lastName"), value[0]));
                }

                if (IdentityType.ENABLED.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(user.get("enabled"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_DATE)) {
                    predicates.add(criteriaBuilder.equal(user.get("createdDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
                    predicates.add(criteriaBuilder.equal(user.get("expirationDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(user.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(user.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(user.<Date>get("expirationDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(user.<Date>get("expirationDate"), (Date) value[0]));
                }
                
                if (queryParameter.equals(IdentityType.HAS_GROUP_ROLE)) {
                    for (Object object : value) {
                        GroupRole groupRole = (GroupRole) object;

                        Subquery subquery = criteriaQuery.subquery(DatabaseMembership.class);
                        Root fromProject = subquery.from(DatabaseMembership.class);
                        Subquery select = subquery.select(fromProject.get("member"));

                        Predicate conjunction = criteriaBuilder.conjunction();

                        conjunction.getExpressions().add(
                                criteriaBuilder.equal(fromProject.get("member"), user));
                        
                        if (groupRole.getMember() != null) {
                            conjunction.getExpressions().add(
                                    criteriaBuilder.equal(fromProject.get("member"), getStoredUser(((User) groupRole.getMember()).getId())));
                        }

                        if (groupRole.getRole() != null) {
                            conjunction.getExpressions().add(
                                    criteriaBuilder.equal(fromProject.get("role"), getStoredRole(groupRole.getRole().getName())));
                        }

                        if (groupRole.getGroup() != null) {
                            conjunction.getExpressions().add(
                                    criteriaBuilder.equal(fromProject.get("group"), getStoredGroup(groupRole.getGroup().getName())));
                        }

                        subquery.where(conjunction);

                        predicates.add(criteriaBuilder.in(user).value(subquery));
                    }
                }

                Join<DatabaseUser, DatabaseMembership> join = null;

                if (IdentityType.MEMBER_OF.equals(queryParameter) || IdentityType.HAS_ROLE.equals(queryParameter)
                        || IdentityType.HAS_GROUP_ROLE.equals(queryParameter)) {
                    join = user.join("memberships");
                }

                // predicates for the role
                if (IdentityType.HAS_ROLE.equals(queryParameter)) {
                    for (Object object : value) {

                        Subquery subquery = criteriaQuery.subquery(DatabaseMembership.class);
                        Root fromProject = subquery.from(DatabaseMembership.class);
                        Subquery select = subquery.select(fromProject.get("member"));

                        Predicate conjunction = criteriaBuilder.conjunction();

                        conjunction.getExpressions().add(
                                criteriaBuilder.equal(fromProject.get("member"), user));
                        conjunction.getExpressions().add(
                                criteriaBuilder.equal(fromProject.get("role"), getStoredRole(object.toString())));

                        subquery.where(conjunction);
                        
                        predicates.add(criteriaBuilder.in(user).value(subquery));
                    }
                }

                // predicates for the group
                if (IdentityType.MEMBER_OF.equals(queryParameter)) {
                    for (Object object : value) {

                        Subquery subquery = criteriaQuery.subquery(DatabaseMembership.class);
                        Root fromProject = subquery.from(DatabaseMembership.class);
                        Subquery select = subquery.select(fromProject.get("member"));

                        Predicate conjunction = criteriaBuilder.conjunction();

                        conjunction.getExpressions().add(
                                criteriaBuilder.equal(fromProject.get("member"), user));
                        conjunction.getExpressions().add(
                                criteriaBuilder.equal(fromProject.get("group"), getStoredGroup(object.toString())));

                        subquery.where(conjunction);
                        
                        predicates.add(criteriaBuilder.in(user).value(subquery));
                    }
                }

                // predicates for the attributes
                if (IdentityType.AttributeParameter.class.isInstance(queryParameter)) {
                    IdentityType.AttributeParameter attribute = (AttributeParameter) queryParameter;
                    
                    Subquery subquery = criteriaQuery.subquery(DatabaseUserAttribute.class);
                    Root fromProject = subquery.from(DatabaseUserAttribute.class);
                    Subquery<DatabaseUser> select = subquery.select(fromProject.get("user"));

                    Predicate conjunction = criteriaBuilder.conjunction();

                    conjunction.getExpressions().add(
                            criteriaBuilder.equal(fromProject.get("name"), attribute.getName()));
                    conjunction.getExpressions().add(
                            (fromProject.get("value").in((Object[]) value)));

                    subquery.where(conjunction);

                    subquery.groupBy(subquery.getSelection()).having(
                            criteriaBuilder.equal(criteriaBuilder.count(subquery.getSelection()), value.length));

                    predicates.add(criteriaBuilder.in(user).value(select));
                }
            }

            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            TypedQuery<DatabaseUser> resultQuery = entityManager.createQuery(criteriaQuery);

            return (List<T>) resultQuery.getResultList();
        } else if (isGroupType(identityQuery.getIdentityType())) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatabaseGroup> criteriaQuery = criteriaBuilder.createQuery(DatabaseGroup.class);

            Root<DatabaseGroup> group = criteriaQuery.from(DatabaseGroup.class);

            group.alias("resultClass");
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<Predicate>();
            
            Map<QueryParameter, Object[]> parameters = identityQuery.getParameters();
            Set<Entry<QueryParameter, Object[]>> parametersEntrySet = parameters.entrySet();

            for (Entry<QueryParameter, Object[]> entry : parametersEntrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] value = entry.getValue();
                
                // predicates for some basic informations
                if (Group.NAME.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(group.get("name"), value[0]));
                }
                
                if (IdentityType.ENABLED.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(group.get("enabled"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_DATE)) {
                    predicates.add(criteriaBuilder.equal(group.get("createdDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
                    predicates.add(criteriaBuilder.equal(group.get("expirationDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(group.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(group.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(group.<Date>get("expirationDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(group.<Date>get("expirationDate"), (Date) value[0]));
                }

                if (Group.ID.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(group.get("id"), value[0]));
                }

                // predicates for the parent group
                if (Group.PARENT.equals(queryParameter)) {
                    Join<DatabaseGroup, DatabaseGroup> joinParentGroup = group.join("parentGroup");
                    predicates.add(criteriaBuilder.equal(joinParentGroup.get("id"), value[0]));
                }

                Join<DatabaseGroup, DatabaseMembership> join = null;

                if (IdentityType.HAS_MEMBER.equals(queryParameter) || IdentityType.HAS_ROLE.equals(queryParameter)) {
                    join = group.join("memberships");
                }

                // predicates for the role
                if (IdentityType.HAS_ROLE.equals(queryParameter)) {
                    Join<DatabaseMembership, DatabaseRole> joinRole = join.join("role");
                    predicates.add(criteriaBuilder.equal(joinRole.get("name"), value[0]));
                }

                // predicates for the user
                if (IdentityType.HAS_MEMBER.equals(queryParameter)) {
                    Join<DatabaseMembership, DatabaseUser> joinGroup = join.join("member");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("id"), value[0]));
                }

                // predicates for the attributes
                if (IdentityType.AttributeParameter.class.isInstance(queryParameter)) {
                    IdentityType.AttributeParameter attribute = (AttributeParameter) queryParameter;
                    
                    Subquery subquery = criteriaQuery.subquery(DatabaseGroupAttribute.class);
                    Root fromProject = subquery.from(DatabaseGroupAttribute.class);
                    Subquery<DatabaseGroup> select = subquery.select(fromProject.get("group"));

                    Predicate conjunction = criteriaBuilder.conjunction();

                    conjunction.getExpressions().add(
                            criteriaBuilder.equal(fromProject.get("name"), attribute.getName()));
                    conjunction.getExpressions().add(
                            (fromProject.get("value").in((Object[]) value)));

                    subquery.where(conjunction);

                    subquery.groupBy(subquery.getSelection()).having(
                            criteriaBuilder.equal(criteriaBuilder.count(subquery.getSelection()), value.length));

                    predicates.add(criteriaBuilder.in(group).value(select));
                }
            }
            
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            TypedQuery<DatabaseGroup> resultQuery = entityManager.createQuery(criteriaQuery);

            return (List<T>) resultQuery.getResultList();
        } else if (isRoleType(identityQuery.getIdentityType())) {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<DatabaseRole> criteriaQuery = criteriaBuilder.createQuery(DatabaseRole.class);

            Root<DatabaseRole> role = criteriaQuery.from(DatabaseRole.class);

            role.alias("resultClass");
            criteriaQuery.distinct(true);

            List<Predicate> predicates = new ArrayList<Predicate>();
            
            Map<QueryParameter, Object[]> parameters = identityQuery.getParameters();
            Set<Entry<QueryParameter, Object[]>> parametersEntrySet = parameters.entrySet();

            for (Entry<QueryParameter, Object[]> entry : parametersEntrySet) {
                QueryParameter queryParameter = entry.getKey();
                Object[] value = entry.getValue();
                
                // predicates for some basic informations
                if (Role.NAME.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(role.get("name"), value[0]));
                }
                
                if (IdentityType.ENABLED.equals(queryParameter)) {
                    predicates.add(criteriaBuilder.equal(role.get("enabled"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_DATE)) {
                    predicates.add(criteriaBuilder.equal(role.get("createdDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
                    predicates.add(criteriaBuilder.equal(role.get("expirationDate"), value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(role.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(role.<Date>get("createdDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                    predicates.add(criteriaBuilder.greaterThan(role.<Date>get("expirationDate"), (Date) value[0]));
                }

                if (queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                    predicates.add(criteriaBuilder.lessThan(role.<Date>get("expirationDate"), (Date) value[0]));
                }

                Join<DatabaseRole, DatabaseMembership> join = null;

                if (Role.GROUP_ROLE_OF.equals(queryParameter) || Role.HAS_ROLE.equals(queryParameter)) {
                    join = role.join("memberships");
                }

                // predicates for the group
                if (Role.GROUP_ROLE_OF.equals(queryParameter)) {
                    Join<DatabaseMembership, DatabaseGroup> joinGroup = join.join("group");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("id"), value[0]));
                }

                // predicates for the owner
                if (Role.HAS_ROLE.equals(queryParameter)) {
                    Join<DatabaseMembership, DatabaseUser> joinUser = join.join("member");
                    predicates.add(criteriaBuilder.equal(joinUser.get("id"), value[0]));
                }
                
                if (IdentityType.AttributeParameter.class.isInstance(queryParameter)) {
                    IdentityType.AttributeParameter attribute = (AttributeParameter) queryParameter;
                    
                    Subquery subquery = criteriaQuery.subquery(DatabaseRoleAttribute.class);
                    Root fromProject = subquery.from(DatabaseRoleAttribute.class);
                    Subquery<DatabaseRole> select = subquery.select(fromProject.get("role"));

                    Predicate conjunction = criteriaBuilder.conjunction();

                    conjunction.getExpressions().add(
                            criteriaBuilder.equal(fromProject.get("name"), attribute.getName()));
                    conjunction.getExpressions().add(
                            (fromProject.get("value").in((Object[]) value)));

                    subquery.where(conjunction);

                    subquery.groupBy(subquery.getSelection()).having(
                            criteriaBuilder.equal(criteriaBuilder.count(subquery.getSelection()), value.length));

                    predicates.add(criteriaBuilder.in(role).value(select));
                }
            }
            
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            TypedQuery<DatabaseRole> resultQuery = entityManager.createQuery(criteriaQuery);

            return (List<T>) resultQuery.getResultList();
        }
        
        return Collections.emptyList();
    }

    /**
     * <p>
     * Persists a specific instance.
     * </p>
     * 
     * @param entity
     */
    private void persist(final Object entity) {
        getEntityManager().persist(entity);
        getEntityManager().flush();
    }

    /**
     * <p>
     * Find a instance with the given name and using the specified named query.
     * </p>
     * 
     * @param idValue
     * @param namedQueryName
     * @return
     */
    private IdentityType findIdentityTypeByKey(final String idFieldName, final String idValue, final String namedQueryName) {
        Query query = getEntityManager().createNamedQuery(namedQueryName);

        query.setParameter(idFieldName, idValue);

        IdentityType loadedUser = null;

        try {
            loadedUser = (IdentityType) query.getSingleResult();
        } catch (NoResultException nre) {
            // TODO: what to do when this happens
        } catch (NonUniqueResultException nure) {
            // TODO: what to do when this happens
        }

        return loadedUser;
    }

    @Override
    public void setup(SimpleJPAIdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
    }

    @Override
    public SimpleJPAIdentityStoreConfiguration getConfig() {
        return this.config;
    }

    @Override
    public IdentityStoreInvocationContext getContext() {
        return this.context;
    }

    @Override
    public void update(IdentityType identityType) {
        if (isUserType(identityType.getClass())) {
            User updatedUser = (User) identityType;
            DatabaseUser storedUser = getStoredUser(updatedUser.getId());
            
            storedUser.setEmail(updatedUser.getEmail());
            storedUser.setFirstName(updatedUser.getFirstName());
            storedUser.setLastName(updatedUser.getLastName());
            storedUser.setKey(updatedUser.getKey());
            storedUser.setEnabled(updatedUser.isEnabled());
            storedUser.setExpirationDate(updatedUser.getExpirationDate());
            
            updateAttributes(identityType, storedUser);
            getEntityManager().merge(storedUser);
            getEntityManager().flush();
        } else if (isRoleType(identityType.getClass())) {
            Role updatedRole = (Role) identityType;
            DatabaseRole storedRole = getStoredRole(updatedRole.getName());
            
            storedRole.setKey(updatedRole.getKey());
            storedRole.setEnabled(updatedRole.isEnabled());
            storedRole.setExpirationDate(updatedRole.getExpirationDate());
            
            updateAttributes(identityType, storedRole);
            getEntityManager().merge(storedRole);
            getEntityManager().flush();
        } else if (isGroupType(identityType.getClass())) {
            Group updatedGroup = (Group) identityType;
            DatabaseGroup storedGroup = (DatabaseGroup) getStoredGroup(updatedGroup.getName());
            
            storedGroup.setKey(updatedGroup.getKey());
            storedGroup.setEnabled(updatedGroup.isEnabled());
            storedGroup.setExpirationDate(updatedGroup.getExpirationDate());
            
            updateAttributes(identityType, storedGroup);
            getEntityManager().merge(storedGroup);
            getEntityManager().flush();
        }
    }

    @Override
    public Agent getAgent(String id) {
        return getUser(id);
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
    public <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeAttribute(IdentityType identityType, String attributeName) {
        // TODO Auto-generated method stub
        
    }

    protected EntityManager getEntityManager() {
        if (!getContext().isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw new IllegalStateException("Error while trying to determine EntityManager - context parameter not set.");
        }

        return (EntityManager) getContext().getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }
    
    private void updateAttributes(IdentityType identityType, AbstractDatabaseIdentityType<?> identity) {
        EntityManager em = getEntityManager();

        removeAllAttributes(identity);
        
        if (identityType.getAttributes() != null && !identityType.getAttributes().isEmpty()) {
            List<String> attributesToRetain = new ArrayList<String>();

            for (Attribute<? extends Serializable> userAttribute : identityType.getAttributes()) {
                identity.setAttribute(userAttribute);
            }
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
    private void removeAttributes(AbstractDatabaseIdentityType<?> identity, List<String> attributesToRetain) {
        StringBuffer attributeNames = new StringBuffer();

        for (String string : attributesToRetain) {
            if (attributeNames.length() != 0) {
                attributeNames.append(",");
            }

            attributeNames.append("'").append(string).append("'");
        }

        Collection<? extends AbstractDatabaseAttribute<?>> attributes = (Collection<? extends AbstractDatabaseAttribute<?>>) identity.getOwnerAttributes();
        
        for (AbstractDatabaseAttribute<?> attribute : new ArrayList<AbstractDatabaseAttribute<?>>(attributes)) {
            if (!attributesToRetain.contains(attribute.getName())) {
                identity.removeAttribute(attribute.getName());
                getEntityManager().remove(attribute);
                getEntityManager().flush();
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
    @SuppressWarnings("unchecked")
    private void removeAllAttributes(AbstractDatabaseIdentityType<?> identity) {
        removeAttributes(identity, Collections.<String>emptyList());
    }
    
}