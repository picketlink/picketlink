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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;

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
                          validateCredential, updateCredential,
                          readAttribute, updateAttribute, deleteAttribute,
                          all }

    /**
     * Indicates which particular features this IdentityStore supports.
     * 
     * @return A Set containing the features supported by this IdentityStore.
     */
    Set<Feature> getFeatureSet();

    void configure(IdentityStoreConfiguration config) throws SecurityConfigurationException;

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
     * Updates the persisted User details with those provided by the specified User
     * 
     * @param ctx
     * @param user
     */
    void updateUser(IdentityStoreInvocationContext ctx, User user);

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
     * @param group The group to create.
     * @param parent The parent group.  If the group to be created has no parent, then pass null.
     * @return
     */
    void createGroup(IdentityStoreInvocationContext ctx, Group group);

    /**
     * Removes the specified Group from persistent storage.
     * 
     * @param ctx
     * @param group The Group to remove
     */
    void removeGroup(IdentityStoreInvocationContext ctx, Group group);

    /**
     * Returns the Group with the specified Group ID.
     * 
     * @param ctx
     * @param groupId
     * @return
     */
    Group getGroup(IdentityStoreInvocationContext ctx, String groupId);

    /**
     * Returns the Group with the specified name and parent group
     * 
     * @param ctx
     * @param name The name of the Group to return
     * @return
     */
    Group getGroup(IdentityStoreInvocationContext ctx, String name, Group parent);

    // Role

    /**
     * Creates a new persistent Role
     * 
     * @param ctx
     * @param name The name of the Role to create
     * @return
     */
    void createRole(IdentityStoreInvocationContext ctx, Role role);

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

    // Identity query

    List<IdentityType> fetchQueryResults(Map<QueryParameter,Object> parameters);

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

    void setAttribute(IdentityStoreInvocationContext ctx, IdentityType identityType, 
            Attribute<? extends Serializable> attribute);

    <T extends Serializable> Attribute<T> getAttribute(IdentityStoreInvocationContext ctx, 
            IdentityType identityType, String attributeName);

    void removeAttribute(IdentityStoreInvocationContext ctx, IdentityType identityType, String attributeName);
}
