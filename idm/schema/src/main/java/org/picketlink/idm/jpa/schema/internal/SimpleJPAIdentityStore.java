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

import java.util.ArrayList;
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

import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.credential.PasswordCredential;
import org.picketlink.idm.jpa.schema.DatabaseGroup;
import org.picketlink.idm.jpa.schema.DatabaseMembership;
import org.picketlink.idm.jpa.schema.DatabaseRole;
import org.picketlink.idm.jpa.schema.DatabaseUser;
import org.picketlink.idm.jpa.schema.DatabaseUserAttribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.query.Range;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * An implementation of IdentityStore backed by a JPA datasource
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SimpleJPAIdentityStore implements IdentityStore {

    private static final String PASSWORD_ATTRIBUTE_NAME = "password";
    private JPATemplate jpaTemplate;

    @Override
    public void createUser(IdentityStoreInvocationContext ctx, User user) {
        User newUser = null;
        
        if (!(user instanceof DatabaseUser)) {
            newUser = new DatabaseUser(user.getId());
        
            newUser.setFirstName(user.getFirstName());
            newUser.setLastName(user.getLastName());
            newUser.setEmail(user.getEmail());
            
            for (String attribName : user.getAttributes().keySet()) {
                newUser.setAttribute(attribName, user.getAttribute(attribName));
            }
        } else {
            newUser = user;
        }
                
        persist(newUser);
    }

    @Override
    public void removeUser(IdentityStoreInvocationContext ctx, final User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User identifier nor provided.");
        }
        remove(user);
    }

    @Override
    public User getUser(IdentityStoreInvocationContext ctx, final String name) {
        return (User) findIdentityTypeByKey("id", name, NamedQueries.USER_LOAD_BY_KEY);
    }

    @Override
    public Group createGroup(IdentityStoreInvocationContext ctx, String name, Group parent) {
        DatabaseGroup newGroup = new DatabaseGroup(name);

        newGroup.setParentGroup((DatabaseGroup) parent);

        persist(newGroup);

        return newGroup;
    }

    @Override
    public void removeGroup(IdentityStoreInvocationContext ctx, Group group) {
        if (group.getId() == null) {
            throw new IllegalArgumentException("Group identifier not provided.");
        }

        remove(group);
    }

    @Override
    public Group getGroup(IdentityStoreInvocationContext ctx, String group) {
        return (Group) findIdentityTypeByKey("name", group, NamedQueries.GROUP_LOAD_BY_KEY);
    }

    @Override
    public Role createRole(IdentityStoreInvocationContext ctx, String name) {
        DatabaseRole newRole = new DatabaseRole(name);

        persist(newRole);

        return newRole;
    }

    @Override
    public void removeRole(IdentityStoreInvocationContext ctx, Role role) {
        if (role.getName() == null) {
            throw new IllegalArgumentException("Role name not provided.");
        }

        remove(role);
    }

    @Override
    public Role getRole(IdentityStoreInvocationContext ctx, String role) {
        return (Role) findIdentityTypeByKey("name", role, NamedQueries.ROLE_LOAD_BY_KEY);
    }

    @Override
    public Membership createMembership(IdentityStoreInvocationContext ctx, Role role, User user, Group group) {
        DatabaseUser dbUser = (DatabaseUser) getUser(ctx, user.getId());
        DatabaseRole dbRole = (DatabaseRole) getRole(ctx, role.getName());
        DatabaseGroup dbGroup = (DatabaseGroup) getGroup(ctx, group.getName());
        
        DatabaseMembership newMembership = new DatabaseMembership(dbRole, dbUser, dbGroup);

        dbUser.getMemberships().add(newMembership);

        persist(newMembership);

        return newMembership;
    }

    @Override
    public void removeMembership(IdentityStoreInvocationContext ctx, Role role, User user, Group group) {
        Membership membership = getMembership(ctx, role, user, group);

        if (membership != null) {
            remove(membership);
        }
    }

    @Override
    public Membership getMembership(IdentityStoreInvocationContext ctx, final Role role, final User user, final Group group) {
        return (Membership) executeOperation(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                Query query = entityManager.createNamedQuery(NamedQueries.MEMBERSHIP_LOAD_BY_KEY);

                query.setParameter("role", role);
                query.setParameter("user", user);
                query.setParameter("group", group);

                Membership loadedMembership = null;

                try {
                    loadedMembership = (Membership) query.getSingleResult();
                } catch (NoResultException nre) {
                    // TODO: what to do when this happens
                }

                return loadedMembership;
            }
        });
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.idm.spi.IdentityStore#executeQuery(org.picketlink.idm.query.UserQuery,
     * org.picketlink.idm.query.Range)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<User> executeQuery(IdentityStoreInvocationContext ctx, final UserQuery query, Range range) {
        return (List<User>) this.jpaTemplate.execute(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<DatabaseUser> criteriaQuery = criteriaBuilder.createQuery(DatabaseUser.class);

                Root<DatabaseUser> user = criteriaQuery.from(DatabaseUser.class);

                user.alias("resultClass");
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates for some basic informations
                if (query.getName() != null) {
                    predicates.add(criteriaBuilder.equal(user.get("id"), query.getName()));
                }

                if (query.getEmail() != null) {
                    predicates.add(criteriaBuilder.equal(user.get("email"), query.getEmail()));
                }

                if (query.getFirstName() != null) {
                    predicates.add(criteriaBuilder.equal(user.get("firstName"), query.getFirstName()));
                }

                if (query.getLastName() != null) {
                    predicates.add(criteriaBuilder.equal(user.get("lastName"), query.getLastName()));
                }

                predicates.add(criteriaBuilder.equal(user.get("enabled"), query.getEnabled()));

                Join<DatabaseUser, DatabaseMembership> join = null;

                if (query.getRole() != null || query.getRelatedGroup() != null) {
                    join = user.join("memberships");
                }

                // predicates for the role
                if (query.getRole() != null) {
                    Join<DatabaseMembership, DatabaseRole> joinRole = join.join("role");
                    predicates.add(criteriaBuilder.equal(joinRole.get("name"), query.getRole().getName()));
                }

                // predicates for the group
                if (query.getRelatedGroup() != null) {
                    Join<DatabaseMembership, DatabaseGroup> joinGroup = join.join("group");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("name"), query.getRelatedGroup().getName()));
                }

                // predicates for the attributes
                if (query.getAttributeFilters() != null) {
                    Set<Entry<String, String[]>> entrySet = query.getAttributeFilters().entrySet();

                    for (Entry<String, String[]> entry : entrySet) {
                        Join<DatabaseUser, DatabaseUserAttribute> joinAttr = user.join("ownerAttributes");

                        Predicate conjunction = criteriaBuilder.conjunction();
                        conjunction.getExpressions().add(criteriaBuilder.equal(joinAttr.get("name"), entry.getKey()));
                        conjunction.getExpressions().add(joinAttr.get("value").in((Object[]) entry.getValue()));
                        predicates.add(conjunction);
                    }
                }

                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

                TypedQuery<DatabaseUser> resultQuery = entityManager.createQuery(criteriaQuery);

                return resultQuery.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Group> executeQuery(IdentityStoreInvocationContext ctx, final GroupQuery query, Range range) {
        return (List<Group>) this.jpaTemplate.execute(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<DatabaseGroup> criteriaQuery = criteriaBuilder.createQuery(DatabaseGroup.class);

                Root<DatabaseGroup> group = criteriaQuery.from(DatabaseGroup.class);

                group.alias("resultClass");
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates for some basic informations
                if (query.getName() != null) {
                    predicates.add(criteriaBuilder.equal(group.get("name"), query.getName()));
                }

                if (query.getId() != null) {
                    predicates.add(criteriaBuilder.equal(group.get("id"), query.getId()));
                }

                // predicates for the parent group
                if (query.getParentGroup() != null) {
                    Join<DatabaseGroup, DatabaseGroup> joinParentGroup = group.join("parentGroup");
                    predicates.add(criteriaBuilder.equal(joinParentGroup.get("id"), query.getParentGroup().getId()));
                }

                Join<DatabaseGroup, DatabaseMembership> join = null;

                if (query.getRelatedUser() != null || query.getRole() != null) {
                    join = group.join("memberships");
                }

                // predicates for the role
                if (query.getRole() != null) {
                    Join<DatabaseMembership, DatabaseRole> joinRole = join.join("role");
                    predicates.add(criteriaBuilder.equal(joinRole.get("name"), query.getRole().getName()));
                }

                // predicates for the user
                if (query.getRelatedUser() != null) {
                    Join<DatabaseMembership, DatabaseUser> joinGroup = join.join("user");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("id"), query.getRelatedUser().getId()));
                }

                // predicates for the attributes
                if (query.getAttributeFilters() != null) {
                    Set<Entry<String, String[]>> entrySet = query.getAttributeFilters().entrySet();

                    for (Entry<String, String[]> entry : entrySet) {
                        Join<DatabaseGroup, DatabaseUserAttribute> joinAttr = group.join("ownerAttributes");

                        Predicate conjunction = criteriaBuilder.conjunction();
                        conjunction.getExpressions().add(criteriaBuilder.equal(joinAttr.get("name"), entry.getKey()));
                        conjunction.getExpressions().add(joinAttr.get("value").in((Object[]) entry.getValue()));
                        predicates.add(conjunction);
                    }
                }

                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

                TypedQuery<DatabaseGroup> resultQuery = entityManager.createQuery(criteriaQuery);

                return resultQuery.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Role> executeQuery(IdentityStoreInvocationContext ctx, final RoleQuery query, Range range) {
        return (List<Role>) this.jpaTemplate.execute(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<DatabaseRole> criteriaQuery = criteriaBuilder.createQuery(DatabaseRole.class);

                Root<DatabaseRole> role = criteriaQuery.from(DatabaseRole.class);

                role.alias("resultClass");
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates for some basic informations
                if (query.getName() != null) {
                    predicates.add(criteriaBuilder.equal(role.get("name"), query.getName()));
                }

                Join<DatabaseRole, DatabaseMembership> join = null;

                if (query.getGroup() != null || query.getOwner() != null) {
                    join = role.join("memberships");
                }

                // predicates for the group
                if (query.getGroup() != null) {
                    Join<DatabaseMembership, DatabaseGroup> joinGroup = join.join("group");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("id"), query.getGroup().getId()));
                }

                // predicates for the owner
                if (query.getOwner() != null) {
                    Join<DatabaseMembership, DatabaseUser> joinUser = join.join("user");
                    predicates.add(criteriaBuilder.equal(joinUser.get("key"), query.getOwner().getKey()));
                }

                if (query.getAttributeFilters() != null) {
                    Set<Entry<String, String[]>> entrySet = query.getAttributeFilters().entrySet();

                    for (Entry<String, String[]> entry : entrySet) {
                        Join<DatabaseRole, DatabaseUserAttribute> joinAttr = role.join("ownerAttributes");

                        Predicate conjunction = criteriaBuilder.conjunction();
                        conjunction.getExpressions().add(criteriaBuilder.equal(joinAttr.get("name"), entry.getKey()));
                        conjunction.getExpressions().add(joinAttr.get("value").in((Object[]) entry.getValue()));
                        predicates.add(conjunction);
                    }
                }

                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

                TypedQuery<DatabaseRole> resultQuery = entityManager.createQuery(criteriaQuery);

                return resultQuery.getResultList();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Membership> executeQuery(IdentityStoreInvocationContext ctx, final MembershipQuery query, Range range) {
        return (List<Membership>) this.jpaTemplate.execute(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<DatabaseMembership> criteriaQuery = criteriaBuilder.createQuery(DatabaseMembership.class);

                Root<DatabaseMembership> membership = criteriaQuery.from(DatabaseMembership.class);

                membership.alias("resultClass");
                criteriaQuery.distinct(true);

                List<Predicate> predicates = new ArrayList<Predicate>();

                // predicates for the group
                if (query.getGroup() != null) {
                    Join<DatabaseMembership, DatabaseGroup> joinGroup = membership.join("group");
                    predicates.add(criteriaBuilder.equal(joinGroup.get("id"), query.getGroup().getId()));
                }

                if (query.getRole() != null) {
                    Join<DatabaseMembership, DatabaseRole> joinRole = membership.join("role");
                    predicates.add(criteriaBuilder.equal(joinRole.get("name"), query.getRole().getName()));
                }

                if (query.getUser() != null) {
                    Join<DatabaseMembership, DatabaseUser> joinUser = membership.join("user");
                    predicates.add(criteriaBuilder.equal(joinUser.get("id"), query.getUser().getId()));
                }

                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

                TypedQuery<DatabaseMembership> resultQuery = entityManager.createQuery(criteriaQuery);

                return resultQuery.getResultList();
            }
        });
    }

    @Override
    public void setAttribute(IdentityStoreInvocationContext ctx, IdentityType identityType, String name, String[] values) {
        if (identityType instanceof User) {
            User user = (User) identityType;
            DatabaseUser databaseUser = (DatabaseUser) getUser(ctx, user.getId());

            databaseUser.setAttribute(name, values);
        } else if (identityType instanceof Role) {
            Role role = (Role) identityType;
            DatabaseRole databaseRole = (DatabaseRole) getRole(ctx, role.getName());

            databaseRole.setAttribute(name, values);
        } else if (identityType instanceof Group) {
            Group group = (Group) identityType;
            DatabaseGroup databaseGroup = (DatabaseGroup) getGroup(ctx, group.getName());

            databaseGroup.setAttribute(name, values);
        } else {
            throwsNotSupportedIdentityType(identityType);
        }
    }

    @Override
    public void removeAttribute(IdentityStoreInvocationContext ctx, IdentityType identityType, String name) {
        if (identityType instanceof User) {
            User user = (User) identityType;
            DatabaseUser databaseUser = (DatabaseUser) getUser(ctx, user.getId());

            if (databaseUser != null) {
                databaseUser.removeAttribute(name);
            }
        } else if (identityType instanceof Role) {
            Role role = (Role) identityType;
            DatabaseRole databaseRole = (DatabaseRole) getRole(ctx, role.getName());

            if (databaseRole != null) {
                databaseRole.removeAttribute(name);
            }
        } else if (identityType instanceof Group) {
            Group group = (Group) identityType;
            DatabaseGroup databaseGroup = (DatabaseGroup) getGroup(ctx, group.getName());

            if (databaseGroup != null) {
                databaseGroup.removeAttribute(name);
            }
        } else {
            throwsNotSupportedIdentityType(identityType);
        }
    }

    @Override
    public String[] getAttributeValues(IdentityStoreInvocationContext ctx, IdentityType identityType, String name) {
        if (identityType instanceof DatabaseUser) {
            User user = (User) identityType;
            DatabaseUser databaseUser = (DatabaseUser) getUser(ctx, user.getId());

            if (databaseUser != null) {
                return databaseUser.getAttributeValues(name);
            }
        } else if (identityType instanceof Role) {
            Role role = (Role) identityType;
            DatabaseRole databaseRole = (DatabaseRole) getRole(ctx, role.getName());

            if (databaseRole != null) {
                return databaseRole.getAttributeValues(name);
            }
        } else if (identityType instanceof Group) {
            Group group = (Group) identityType;
            DatabaseGroup databaseGroup = (DatabaseGroup) getGroup(ctx, group.getName());

            if (databaseGroup != null) {
                return databaseGroup.getAttributeValues(name);
            }
        } else {
            throwsNotSupportedIdentityType(identityType);
        }

        return null;
    }

    @Override
    public Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, IdentityType identityType) {
        if (identityType instanceof DatabaseUser) {
            DatabaseUser user = (DatabaseUser) identityType;
            DatabaseUser DatabaseUser = (DatabaseUser) getUser(ctx, user.getId());

            if (DatabaseUser != null) {
                return DatabaseUser.getAttributes();
            }
        } else if (identityType instanceof DatabaseRole) {
            DatabaseRole role = (DatabaseRole) identityType;
            DatabaseRole databaseRole = (DatabaseRole) getRole(ctx, role.getName());

            if (databaseRole != null) {
                return databaseRole.getAttributes();
            }
        } else if (identityType instanceof DatabaseGroup) {
            DatabaseGroup group = (DatabaseGroup) identityType;
            DatabaseGroup databaseGroup = (DatabaseGroup) getGroup(ctx, group.getName());

            if (databaseGroup != null) {
                return databaseGroup.getAttributes();
            }
        } else {
            throwsNotSupportedIdentityType(identityType);
        }

        return null;
    }

    public void setJpaTemplate(JPATemplate jpaTemplate) {
        this.jpaTemplate = jpaTemplate;
    }

    /**
     * <p>
     * Executes the {@link JPACallback} instance.
     * </p>
     *
     * @param callback
     * @return
     */
    private Object executeOperation(JPACallback callback) {
        return this.jpaTemplate.execute(callback);
    }

    /**
     * <p>
     * Persists a specific instance.
     * </p>
     *
     * @param entity
     */
    private void persist(final Object entity) {
        JPACallback callback = new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                entityManager.persist(entity);
                return null;
            }
        };

        executeOperation(callback);
    }

    /**
     * <p>
     * Removes a specific instance.
     * </p>
     *
     * @param entity
     */
    private void remove(final Object entity) {
        executeOperation(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                entityManager.remove(entity);
                return null;
            }
        });
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
        return (IdentityType) executeOperation(new JPACallback() {

            @Override
            public Object execute(EntityManager entityManager) {
                Query query = entityManager.createNamedQuery(namedQueryName);

                query.setParameter(idFieldName, idValue);

                Object loadedUser = null;

                try {
                    loadedUser = query.getSingleResult();
                } catch (NoResultException nre) {
                    // TODO: what to do when this happens
                } catch (NonUniqueResultException nure) {
                 // TODO: what to do when this happens
                }

                return loadedUser;
            }
        });
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.idm.spi.IdentityStore#validateCredential(org.picketlink.idm.model.User, org.picketlink.idm.credential.Credential)
     */
    @Override
    public boolean validateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential passwordCredential = (PasswordCredential) credential;
            String providedPassword = passwordCredential.getPassword();
            String expectedPassword = user.getAttribute(PASSWORD_ATTRIBUTE_NAME);
            
            return expectedPassword != null && providedPassword != null && providedPassword.equals(expectedPassword);
        } else {
            throwsNotSupportedCredentialType(credential);
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.idm.spi.IdentityStore#updateCredential(org.picketlink.idm.model.User, org.picketlink.idm.credential.Credential)
     */
    @Override
    public void updateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential) {
        if (credential instanceof PasswordCredential) {
            PasswordCredential passwordCredential = (PasswordCredential) credential;
            
            user.setAttribute(PASSWORD_ATTRIBUTE_NAME, passwordCredential.getPassword());
        } else {
            throwsNotSupportedCredentialType(credential);
        }
    }
    
    /**
     * <p>Helper method to throws a {@link IllegalArgumentException} when the specified {@link Credential} is not supported.</p>
     * TODO: when using JBoss Logging this method should be removed.
     * 
     * @param credential
     * @return
     */
    private void throwsNotSupportedCredentialType(Credential credential) throws IllegalArgumentException {
        throw new IllegalArgumentException("Credential type not supported: " + credential.getClass());
    }

    /**
     * <p>
     * Helper method to throws a {@link IllegalArgumentException} when the specified {@link IdentityType} is not supported.
     * </p>
     * TODO: when using JBoss Logging this method should be removed.
     * 
     * @param credential
     * @return
     */
    private void throwsNotSupportedIdentityType(IdentityType identityType) throws IllegalArgumentException {
        throw new IllegalArgumentException("IdentityType not supported: " + identityType.getClass());
    }
}