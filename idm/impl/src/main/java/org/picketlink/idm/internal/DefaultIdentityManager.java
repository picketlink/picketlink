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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;
import org.picketlink.idm.spi.IdentityStoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * Default implementation of the IdentityManager interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {

    private Map<Feature,IdentityStore> featureToStoreMap = new HashMap<Feature,IdentityStore>();

    private PasswordEncoder passwordEncoder;

    private IdentityStoreFactory storeFactory = new DefaultIdentityStoreFactory();

    private IdentityStoreInvocationContextFactory contextFactory;

    @Override
    public void bootstrap(IdentityConfiguration identityConfig, IdentityStoreInvocationContextFactory contextFactory) {
        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            IdentityStore store = storeFactory.createIdentityStore(config);

            for (Feature f : store.getFeatureSet()) {
                featureToStoreMap.put(f, store);
            }
        }

        this.contextFactory = contextFactory;
    }

    @Override
    public void setIdentityStoreFactory(IdentityStoreFactory factory) {
        this.storeFactory = factory;
    }

    private IdentityStore getStoreForFeature(Feature feature) {
        if (featureToStoreMap.containsKey(feature)) {
            return featureToStoreMap.get(feature);
        } else if (featureToStoreMap.containsKey(Feature.all)) {
            return featureToStoreMap.get(Feature.all);
        } else {
            throw new UnsupportedOperationException("This identity management feature is not available");
        }
    }

    @Override
    public void createUser(User user) {
        IdentityStore store = getStoreForFeature(Feature.createUser);
        store.createUser(getContextFactory().getContext(store), user);
    }

    @Override
    public void removeUser(User user) {
        IdentityStore store = getStoreForFeature(Feature.deleteUser);
        store.removeUser(getContextFactory().getContext(store), user);
    }

    @Override
    public void updateUser(User user) {
        IdentityStore store = getStoreForFeature(Feature.updateUser);
        store.updateUser(getContextFactory().getContext(store), user);
    }

    @Override
    public User getUser(String name) {
        IdentityStore store = getStoreForFeature(Feature.readUser);
        return store.getUser(getContextFactory().getContext(store), name);
    }

    @Override
    public void createGroup(Group group) {
        IdentityStore store = getStoreForFeature(Feature.createGroup);
        store.createGroup(getContextFactory().getContext(store), group);
    }

    @Override
    public void removeGroup(Group group) {
        IdentityStore store = getStoreForFeature(Feature.deleteGroup);
        store.removeGroup(getContextFactory().getContext(store), group);
    }

    @Override
    public Group getGroup(String groupId) {
        IdentityStore store = getStoreForFeature(Feature.readGroup);
        return store.getGroup(getContextFactory().getContext(store), groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        IdentityStore store = getStoreForFeature(Feature.readGroup);
        return store.getGroup(getContextFactory().getContext(store), groupName, parent);
    }

    public boolean isMember(IdentityType identityType, Group group) {
        // FIXME implement this
        return false;
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
    public void createRole(Role role) {
        IdentityStore store = getStoreForFeature(Feature.createRole);
        store.createRole(getContextFactory().getContext(store), role);
    }

    @Override
    public void removeRole(Role role) {
        IdentityStore store = getStoreForFeature(Feature.deleteRole);
        store.removeRole(getContextFactory().getContext(store), role);
    }

    @Override
    public Role getRole(String name) {
        IdentityStore store = getStoreForFeature(Feature.readRole);
        return store.getRole(getContextFactory().getContext(store), name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.IdentityManager#hasRole(org.picketlink.idm.model.Role, org.picketlink.idm.model.IdentityType, org.picketlink.idm.model.Group)
     */
    @Override
    public boolean hasRole(IdentityType identityType, Role role, Group group) {
        //TODO: the MembershipQuery defines only a setUser. Need more discussion about others IdentityTypes.
        if (!(identityType instanceof User)) {
            throw new IllegalArgumentException("For now only the User type is supported as the IdentityType argument.");
        }
        /*
        MembershipQuery query = createMembershipQuery();
        
        query.setRole(role);
        query.setGroup(group);
        query.setUser((User) identityType);
        
        return !query.executeQuery().isEmpty();*/

        // TODO rewrite this implementation to use the IdentityCache instead of a query

        return false;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role, Group group) {
        IdentityStore store = getStoreForFeature(Feature.createMembership);
        store.createMembership(getContextFactory().getContext(store), identityType, group, role);
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role, Group group) {
        throw new RuntimeException();
    }

    @Override
    public boolean hasApplicationRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantApplicationRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revokeApplicationRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean validateCredential(User user, Credential credential) {
        IdentityStore store = getStoreForFeature(Feature.validateCredential);
        return store.validateCredential(getContextFactory().getContext(store), user, credential);
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        IdentityStore store = getStoreForFeature(Feature.validateCredential);
        store.updateCredential(getContextFactory().getContext(store), user, credential);
    }

    public void setEnabled(IdentityType identityType, boolean enabled) {
        throw new RuntimeException();
    }

    public void setExpirationDate(IdentityType identityType, Date expirationDate) {
        throw new RuntimeException();
    }

    public IdentityStoreInvocationContextFactory getContextFactory() {
        return contextFactory;
    }

    @Override
    public IdentityType lookupIdentityByKey(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    public void test() {
        List<User> users = this.<User>createQuery()
                .setParameter(User.MEMBER_OF.group("Head Office").role("Admin"), true)
                .setParameter(User.MEMBER_OF.group("Springfield"), false)
                .setParameter(User.MEMBER_OF.role("Contractor"), false)
                .getResultList();
    }

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        // TODO implement this
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO implement this
        return null;
    }

}