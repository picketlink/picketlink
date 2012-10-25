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
package org.picketlink.idm.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.query.GroupQuery;
import org.picketlink.idm.query.MembershipQuery;
import org.picketlink.idm.query.RoleQuery;
import org.picketlink.idm.query.UserQuery;
import org.picketlink.idm.query.internal.DefaultGroupQuery;
import org.picketlink.idm.query.internal.DefaultMembershipQuery;
import org.picketlink.idm.query.internal.DefaultRoleQuery;
import org.picketlink.idm.query.internal.DefaultUserQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * Default implementation of the IdentityManager interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {
    private IdentityStore store = null;
    private PasswordEncoder passwordEncoder;

    public DefaultIdentityManager() {
    }

    public DefaultIdentityManager(IdentityStore theStore){
        this.store = theStore;
    }

    public void setIdentityStore(IdentityStore theStore) {
        this.store = theStore;
    }

    @Override
    public User createUser(String name) {
        ensureStoreExists();
        User user = new SimpleUser(name);
        store.createUser(getInvocationContext(store), user);
        return user;
    }

    @Override
    public void createUser(User user) {
        ensureStoreExists();
        store.createUser(getInvocationContext(store), user);
    }

    @Override
    public void removeUser(User user) {
        ensureStoreExists();
        store.removeUser(getInvocationContext(store), user);
    }

    @Override
    public void removeUser(String name) {
        ensureStoreExists();
        store.removeUser(getInvocationContext(store), getUser(name));
    }

    @Override
    public User getUser(String name) {
        ensureStoreExists();
        return store.getUser(getInvocationContext(store), name);
    }

    @Override
    public Collection<User> getAllUsers() {
        throw new RuntimeException();
    }

    @Override
    public Group createGroup(String id) {
        return store.createGroup(getInvocationContext(store), id, null);
    }

    @Override
    public Group createGroup(String id, Group parent) {
        ensureStoreExists();
        return store.createGroup(getInvocationContext(store), id, parent);
    }

    @Override
    public Group createGroup(String id, String parent) {
        ensureStoreExists();
        Group parentGroup = store.getGroup(getInvocationContext(store), parent);
        return store.createGroup(getInvocationContext(store), id, parentGroup);
    }

    @Override
    public void removeGroup(Group group) {
        ensureStoreExists();
        store.removeGroup(getInvocationContext(store), group);
    }

    @Override
    public void removeGroup(String groupId) {
        ensureStoreExists();
        store.removeGroup(getInvocationContext(store), getGroup(groupId));
    }

    @Override
    public Group getGroup(String groupId) {
        ensureStoreExists();
        return store.getGroup(getInvocationContext(store), groupId);
    }

    @Override
    public Group getGroup(String groupId, Group parent) {
        ensureStoreExists();
        return getGroup(groupId); // What about parent?
    }

    @Override
    public Collection<Group> getAllGroups() {
        throw new RuntimeException();
    }

    @Override
    public void addToGroup(IdentityType identityType, Group group) {
        throw new RuntimeException();
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group) {
        throw new RuntimeException();
    }

    @Override
    public Collection<IdentityType> getGroupMembers(Group group) {
        throw new RuntimeException();
    }

    @Override
    public Role createRole(String name) {
        ensureStoreExists();
        return store.createRole(getInvocationContext(store), name);
    }

    @Override
    public void removeRole(Role role) {
        ensureStoreExists();
        store.removeRole(getInvocationContext(store), role);
    }

    @Override
    public void removeRole(String name) {
        ensureStoreExists();
        store.removeRole(getInvocationContext(store), getRole(name));
    }

    @Override
    public Role getRole(String name) {
        ensureStoreExists();
        return store.getRole(getInvocationContext(store), name);
    }

    @Override
    public Collection<Role> getAllRoles() {
        throw new RuntimeException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Role> getRoles(IdentityType identityType, Group group) {
        RoleQuery query = createRoleQuery();

        // TODO: this should not happen because store impls must provide a valid instance. For now this is ignored and a empty
        // list is returned.
        if (query == null) {
            return Collections.EMPTY_LIST;
        }

        query.setGroup(group);
        query.setOwner(identityType);

        return query.executeQuery();
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.IdentityManager#hasRole(org.picketlink.idm.model.Role, org.picketlink.idm.model.IdentityType, org.picketlink.idm.model.Group)
     */
    @Override
    public boolean hasRole(Role role, IdentityType identityType, Group group) {
        //TODO: the MembershipQuery defines only a setUser. Need more discussion about others IdentityTypes.
        if (!(identityType instanceof User)) {
            throw new IllegalArgumentException("For now only the User type is supported as the IdentityType argument.");
        }
        
        MembershipQuery query = createMembershipQuery();
        
        query.setRole(role);
        query.setGroup(group);
        query.setUser((User) identityType);
        
        return !query.executeQuery().isEmpty();
    }

    @Override
    public void grantRole(Role role, IdentityType identityType, Group group) {
        this.store.createMembership(getInvocationContext(store), role, (User) identityType, group);
    }

    @Override
    public void revokeRole(Role role, IdentityType identityType, Group group) {
        throw new RuntimeException();
    }

    @Override
    public UserQuery createUserQuery() {
        return new DefaultUserQuery(this.store);
    }

    @Override
    public GroupQuery createGroupQuery() {
        return new DefaultGroupQuery(this.store);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.idm.IdentityManager#createMembershipQuery()
     */
    @Override
    public MembershipQuery createMembershipQuery() {
        return new DefaultMembershipQuery(this.store);
    }

    @Override
    public boolean validateCredential(User user, Credential credential) {
        return store.validateCredential(getInvocationContext(store), user, credential);
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        store.updateCredential(getInvocationContext(store), user, credential);
    }

    public void setEnabled(IdentityType identityType, boolean enabled) {
        throw new RuntimeException();
    }

    public void setExpirationDate(IdentityType identityType, Date expirationDate) {
        throw new RuntimeException();
    }

    private void ensureStoreExists() {
        if (store == null) {
            throw new RuntimeException("Identity Store has not been set");
        }
    }

    protected IdentityStoreInvocationContext getInvocationContext(IdentityStore store) {
        // FIXME implement this
        return null;
    }

    @Override
    public RoleQuery createRoleQuery() {
        return new DefaultRoleQuery(this.store);
    }

    @Override
    public IdentityType lookupIdentityByKey(String key) {
        // TODO Auto-generated method stub
        return null;
    }
}