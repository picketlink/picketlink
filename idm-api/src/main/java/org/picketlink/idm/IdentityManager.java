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

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;

import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.query.UserQuery;

/**
 * IdentityManager
 */
public interface IdentityManager {
    // TODO: Javadocs

    // TODO: Exceptions

    // TODO: control hooks & events

    // TODO: linking identities

    // User

    User createUser(String name);

    User createUser(User user);

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

    UserQuery createUserQuery();

    GroupQuery createGroupQuery();

    RoleQuery createRoleQuery();

    MembershipQuery createMembershipQuery();

    // Password Management
    boolean validatePassword(User user, String password);

    void updatePassword(User user, String password);

    void setPasswordEncoder(PasswordEncoder encoder);

    // Certificate Management
    boolean validateCertificate(User user, X509Certificate certificate);

    boolean updateCertificate(User user, X509Certificate certificate);

    // User / Role / Group enablement / expiry

    void setEnabled(IdentityType identityType, boolean enabled);

    void setExpirationDate(IdentityType identityType, Date expirationDate);

    IdentityType lookupIdentityByKey(String key);
}
