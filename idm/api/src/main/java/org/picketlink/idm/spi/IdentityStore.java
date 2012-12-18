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
import java.util.List;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * IdentityStore representation providing minimal SPI
 *
 * @author Boleslaw Dawidowicz
 * @author Shane Bryzak
 */
public interface IdentityStore<T extends IdentityStoreConfiguration> {

    /**
     * This enum defines the individual features that an IdentityStore implementation or
     * instance may support.
     * 
     *  // TODO document each enum here 
     */
    public enum Feature { createUser, readUser, updateUser, deleteUser, 
                          createGroup, readGroup, updateGroup, deleteGroup,
                          createRole, readRole, updateRole, deleteRole,
                          createMembership, readMembership, updateMembership, deleteMembership,
                          readAttribute, updateAttribute, deleteAttribute,
                          manageCredentials,
                          supportsTiers, supportsRealms, disableRole, disableGroup, disableUser,
                          createAgent,
                          all }

    /**
     * Sets the configuration and context in which the IdentityStore will execute its operations
     * 
     * @param config
     * @param context
     */
    void setup(T config, IdentityStoreInvocationContext context);

    /**
     * Returns the configuration for this IdentityStore instance
     * 
     * @return
     */
    T getConfig();

    /**
     * Returns the current context for this IdentityStore instance
     * 
     * @return
     */
    IdentityStoreInvocationContext getContext();

    // General

    /**
     * Persists the specified IdentityType
     * 
     * @param identityType
     */
    void add(IdentityType identityType);

    /**
     * Updates the specified IdentityType
     * 
     * @param identityType
     */
    void update(IdentityType identityType);

    /**
     * Removes the specified IdentityType
     * 
     * @param identityType
     */
    void remove(IdentityType identityType);

    // Agent

    Agent getAgent(String id);

    // User

    /**
     * Returns the User with the specified id value. 
     * 
     * @param ctx
     * @param id
     * @return
     */
    User getUser(String id);

    // Group

    /**
     * Returns the Group with the specified Group ID.
     * 
     * @param ctx
     * @param groupId
     * @return
     */
    Group getGroup(String groupId);

    /**
     * Returns the Group with the specified name and parent group
     * 
     * @param ctx
     * @param name The name of the Group to return
     * @return
     */
    Group getGroup(String name, Group parent);

    // Role

    /**
     * Returns the specified role
     * 
     * @param ctx
     * @param name The name of the Role to return
     * @return A Role instance, or null if the Role with the specified name wasn't found
     */
    Role getRole(String name);

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
    GroupRole createMembership(IdentityType member, Group group, Role role);

    /**
     * Removes a Membership from persistent storage 
     * 
     * @param ctx
     * @param member The member to remove
     * @param group The Group of the membership
     * @param role The Role of the membership
     */
    void removeMembership(IdentityType member, Group group, Role role);

    /**
     * Returns the specified Membership
     * 
     * @param ctx
     * @param member
     * @param group
     * @param role
     * @return
     */
    GroupRole getMembership(IdentityType member, Group group, Role role);

    // Identity query

    <T extends IdentityType> List<T> fetchQueryResults(IdentityQuery<T> identityQuery);

    <T extends IdentityType> int countQueryResults(IdentityQuery<T> identityQuery);
    // Attributes

    /**
     * Sets the specified Attribute value for the specified IdentityType
     * 
     * @param ctx
     * @param identityType
     * @param attribute
     */
    void setAttribute(IdentityType identityType, 
            Attribute<? extends Serializable> attribute);

    /**
     * Returns the Attribute value with the specified name, for the specified IdentityType
     * @param ctx
     * @param identityType
     * @param attributeName
     * @return
     */
    <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName);

    /**
     * Removes the specified Attribute value, for the specified IdentityType
     * 
     * @param ctx
     * @param identityType
     * @param attributeName
     */
    void removeAttribute(IdentityType identityType, String attributeName);

    // Credentials

    /**
     * Validates the specified credentials.  Each IdentityStore implementation typically supports
     * a concrete set of Credentials types, and will generally obtain a CredentialHandler instance
     * from the IdentityStoreInvocationContext to process credential validation.
     * 
     * @param credentials
     */
    void validateCredentials(Credentials credentials);

    /**
     * Updates the specified credential value for the specified Agent.
     * 
     * @param agent
     * @param credential
     */
    void updateCredential(Agent agent, Object credential);
    
    /**
     * <p>Stores a credential using the given {@link CredentialStorage}.</p>
     * 
     * @param agent
     * @param storage
     */
    <C extends CredentialStorage> void storeCredential(Agent agent, C storage);
    
    /**
     * <p>Retrieves a credential for the given {@link Agent} and {@link CredentialStorage} type.
     * 
     * @param agent
     * @param storageClass
     * @return
     */
    <C extends CredentialStorage> C retrieveCredential(Agent agent, Class<C> storageClass);

}
