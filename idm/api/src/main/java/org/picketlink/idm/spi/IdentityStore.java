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

import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Group;
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
 */
public interface IdentityStore {
    // TODO: Javadocs
    // TODO: Exceptions

    // TODO: control hooks, events
    // TODO: authentication, password strenght, salted password hashes

    // User

    void createUser(IdentityStoreInvocationContext ctx, User user);

    void removeUser(IdentityStoreInvocationContext ctx, User user);

    User getUser(IdentityStoreInvocationContext ctx, String id);

    // Group

    Group createGroup(IdentityStoreInvocationContext ctx, String name, Group parent);

    void removeGroup(IdentityStoreInvocationContext ctx, Group group);

    Group getGroup(IdentityStoreInvocationContext ctx, String name);

    // Role

    Role createRole(IdentityStoreInvocationContext ctx, String name);

    void removeRole(IdentityStoreInvocationContext ctx, Role role);

    Role getRole(IdentityStoreInvocationContext ctx, String name);

    // Memberships

    Membership createMembership(IdentityStoreInvocationContext ctx, Role role, User user, Group group);

    void removeMembership(IdentityStoreInvocationContext ctx, Role role, User user, Group group);

    Membership getMembership(IdentityStoreInvocationContext ctx, Role role, User user, Group group);

    // Queries

    List<User> executeQuery(IdentityStoreInvocationContext ctx, UserQuery query, Range range);

    List<Group> executeQuery(IdentityStoreInvocationContext ctx, GroupQuery query, Range range);

    List<Role> executeQuery(IdentityStoreInvocationContext ctx, RoleQuery query, Range range);

    List<Membership> executeQuery(IdentityStoreInvocationContext ctx, MembershipQuery query, Range range);
    

    // Credential management
    
    boolean validateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential);
    
    void updateCredential(IdentityStoreInvocationContext ctx, User user, Credential credential);    

    // Attributes

    // User

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values. Null value or empty array will
     * remove attribute.
     *
     * @param user
     * @param name of attribute
     * @param values to be set
     */
    void setAttribute(IdentityStoreInvocationContext ctx, User user, String name, String[] values);

    /**
     * @param user Remove attribute with given name
     *
     * @param name of attribute
     */
    void removeAttribute(IdentityStoreInvocationContext ctx, User user, String name);

    /**
     * @param user
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(IdentityStoreInvocationContext ctx, User user, String name);

    /**
     * @param user
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, User user);

    // Group

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values. Null value or empty array will
     * remove attribute.
     *
     * @param group
     * @param name of attribute
     * @param values to be set
     */
    void setAttribute(IdentityStoreInvocationContext ctx, Group group, String name, String[] values);

    /**
     * Remove attribute with given name
     *
     * @param group
     * @param name of attribute
     */
    void removeAttribute(IdentityStoreInvocationContext ctx, Group group, String name);

    /**
     * @param group
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(IdentityStoreInvocationContext ctx, Group group, String name);

    /**
     * @param group
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, Group group);

    // Role

    /**
     * Set attribute with given name and values. Operation will overwrite any previous values. Null value or empty array will
     * remove attribute.
     *
     * @param role
     * @param name of attribute
     * @param values to be set
     */
    void setAttribute(IdentityStoreInvocationContext ctx, Role role, String name, String[] values);

    /**
     * Remove attribute with given name
     *
     * @param role
     * @param name of attribute
     */
    void removeAttribute(IdentityStoreInvocationContext ctx, Role role, String name);

    /**
     * @param role
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(IdentityStoreInvocationContext ctx, Role role, String name);

    /**
     * @param role
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes(IdentityStoreInvocationContext ctx, Role role);

}
