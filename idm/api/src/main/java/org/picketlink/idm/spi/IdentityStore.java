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
package org.picketlink.idm.spi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.credential.Credential;
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

/**
 * IdentityStore representation providing minimal SPI
 *
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public interface IdentityStore {

    /**
     * This enum defines the individual features that an IdentityStore implementation or
     * instance may support.
     */
    public enum Feature { createUser, readUser, updateUser, deleteUser, 
                          createGroup, readGroup, updateGroup, deleteGroup,
                          createRole, readRole, updateRole, deleteRole,
                          createMembership, readMembership, updateMembership, deleteMembership,
                          all }

    /**
     * Indicates which particular features this IdentityStore supports.
     * 
     * @return A Set containing the features supported by this IdentityStore.
     */
    Set<Feature> getFeatureSet();

    // User

    /**
     * Persists the specified User 
     * 
     * @param ctx
     * @param user
     */
    void createUser(IdentityStoreInvocationContext ctx, User user);

    /**
     * Removes the specified User from persistent storage.
     * 
     * @param ctx
     * @param user
     */
    void removeUser(IdentityStoreInvocationContext ctx, User user);

    /**
     * Returns the User with the specified id value. 
     * 
     * @param ctx
     * @param id
     * @return
     */
    User getUser(IdentityStoreInvocationContext ctx, String id);

    // Group

    /**
     * Creates a new persistent Group
     * 
     * @param ctx
     * @param name The name of the group.
     * @param parent The parent group.  If the group to be created has no parent, then pass null.
     * @return
     */
    Group createGroup(IdentityStoreInvocationContext ctx, String name, Group parent);

    /**
     * Removes the specified Group from persistent storage.
     * 
     * @param ctx
     * @param group The Group to remove
     */
    void removeGroup(IdentityStoreInvocationContext ctx, Group group);

    /**
     * Returns the specified Group
     * 
     * @param ctx
     * @param name The name of the Group to return
     * @return
     */
    Group getGroup(IdentityStoreInvocationContext ctx, String name);

    // Role

    /**
     * Creates a new persistent Role
     * 
     * @param ctx
     * @param name The name of the Role to create
     * @return
     */
    Role createRole(IdentityStoreInvocationContext ctx, String name);

    /**
     * Removes the specified Role from persistent storage
     * 
     * @param ctx
     * @param role The Role instance to remove
     */
    void removeRole(IdentityStoreInvocationContext ctx, Role role);

    /**
     * Returns the specified role
     * 
     * @param ctx
     * @param name The name of the Role to return
     * @return A Role instance, or null if the Role with the specified name wasn't found
     */
    Role getRole(IdentityStoreInvocationContext ctx, String name);

    // Memberships

    /**
     * Creates a new persistent Membership. The member parameter may be an instance of a User or a Role.
     * 
     * @param ctx
     * @param member The User or Group to become a member
     * @param group The Group instance that the User or Group will become a member of
     * @param role The Role instance that the User or Group will become a member of
     * @return A Membership instance representing the new membership.
     */
    Membership createMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role);

    /**
     * Removes a Membership from persistent storage 
     * 
     * @param ctx
     * @param member The member to remove
     * @param group The Group of the membership
     * @param role The Role of the membership
     */
    void removeMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role);

    /**
     * Returns the specified Membership
     * 
     * @param ctx
     * @param member
     * @param group
     * @param role
     * @return
     */
    Membership getMembership(IdentityStoreInvocationContext ctx, IdentityType member, Group group, Role role);

    // Queries

    List<User> executeQuery(IdentityStoreInvocationContext ctx, UserQuery query, Range range);

    List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range);

    List<Role> executeQuery(IdentityStoreInvocationContext ctx, RoleQuery query, Range range);

    List<Membership> executeQuery(IdentityStoreInvocationContext ctx, MembershipQuery query, Range range);


    // Credential management

    /**
     * Validates a credential for the specified User 
     * 
     * @param ctx
     * @param user
     * @param credential
     * @return
     */
    boolean validateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential);

    /**
     * Updates a credential for the specified User 
     * 
     * @param ctx
     * @param user
     * @param credential
     */
    void updateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential);    

    // Attributes

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values. Null value or empty array will
     * remove attribute.
     *
     * @param identity
     * @param name of attribute
     * @param values to be set
     */
    void setAttribute(IdentityStoreInvocationContext ctx, IdentityType identity, String name, String[] values);

    /**
     * @param identity Remove attribute for the specified IdentityType
     *
     * @param name of attribute
     */
    void removeAttribute(IdentityStoreInvocationContext ctx, IdentityType identity, String name);

    /**
     * @param identity
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(IdentityStoreInvocationContext ctx, IdentityType identity, String name);

    /**
     * @param identity
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, IdentityType identity);

}
