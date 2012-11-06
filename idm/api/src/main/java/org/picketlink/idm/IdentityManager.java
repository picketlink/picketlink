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
package org.picketlink.idm;

import java.util.Collection;
import java.util.Date;

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityStoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * Manages all Identity Management related operations.
 * 
 * @author Shane Bryzak
 */
public interface IdentityManager {
    // TODO: Javadocs

    // TODO: Exceptions

    // TODO: control hooks & events

    // TODO: linking identities

    /**
     * This method must be invoked to set up the IdentityManager instance before any
     * identity management operations may be performed.
     * 
     * @param configuration
     */
    void bootstrap(IdentityConfiguration configuration, IdentityStoreInvocationContextFactory contextFactory);

    /**
     * Sets the IdentityStoreFactory implementation to be used to create IdentityStore instances
     * 
     * @param factory
     */
    void setIdentityStoreFactory(IdentityStoreFactory factory);


    // User

    User createUser(String name);

    void createUser(User user);

    void removeUser(User user);

    void removeUser(String name);

    User getUser(String name);

    Collection<User> getAllUsers();

    // Group

    Group createGroup(String id);

    Group createGroup(String id, Group parent);

    Group createGroup(String id, String parent);

    void removeGroup(Group group);

    void removeGroup(String groupId);

    Group getGroup(String groupId);

    Group getGroup(String groupId, Group parent);

    Collection<Group> getAllGroups();

    void addToGroup(IdentityType identityType, Group group);

    void removeFromGroup(IdentityType identityType, Group group);

    Collection<IdentityType> getGroupMembers(Group group);

    // Roles

    Role createRole(String name);

    void removeRole(Role role);

    void removeRole(String name);

    Role getRole(String name);

    Collection<Role> getAllRoles();

    Collection<Role> getRoles(IdentityType identityType, Group group);

    boolean hasRole(Role role, IdentityType identityType, Group group);

    void grantRole(Role role, IdentityType identityType, Group group);

    void revokeRole(Role role, IdentityType identityType, Group group);

    // Queries

    <T extends IdentityType> IdentityQuery<T> createQuery();

    // Credential management

    boolean validateCredential(User user, Credential credential);

    void updateCredential(User user, Credential credential);

    // User / Role / Group enablement / expiry

    void setEnabled(IdentityType identityType, boolean enabled);

    void setExpirationDate(IdentityType identityType, Date expirationDate);

    IdentityType lookupIdentityByKey(String key);
}
