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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.config.StoreConfiguration;
import org.picketlink.idm.credential.Credential;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;
import org.picketlink.idm.spi.StoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * Default implementation of the IdentityManager interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {

    private Map<String,Map<Feature,IdentityStoreConfiguration>> realmStores = new HashMap<String,Map<Feature,IdentityStoreConfiguration>>();

    private PartitionStoreConfiguration partitionStore;

    private PasswordEncoder passwordEncoder;

    private StoreFactory storeFactory = new DefaultStoreFactory();

    private IdentityStoreInvocationContextFactory contextFactory;

    private static Method METHOD_CREATE_CONTEXT;

    { 
        try {
            METHOD_CREATE_CONTEXT = DefaultIdentityManager.class.getDeclaredMethod("createContext");
        } catch (Exception e) {
            throw new RuntimeException("Error creating DefaultIdentityManager - createContext() method not available", e);
        } 
    };

    @Override
    public IdentityManager forRealm(final Realm realm) {
        final DefaultIdentityManager proxied = this;

        return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), 
                new Class[] {IdentityManager.class}, 
                new InvocationHandler(){

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(METHOD_CREATE_CONTEXT)) {
                            IdentityStoreInvocationContext ctx = proxied.createContext();
                            ctx.setRealm(realm);
                            return ctx;
                        }
                        else {
                            return method.invoke(proxied, args);
                        }
                    }});
    }

    @Override
    public IdentityManager forTier(final Tier tier) {
        final DefaultIdentityManager proxied = this;

        return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), 
                new Class[] {IdentityManager.class}, 
                new InvocationHandler(){

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(METHOD_CREATE_CONTEXT)) {
                            IdentityStoreInvocationContext ctx = proxied.createContext();
                            ctx.setTier(tier);
                            return ctx;
                        }
                        else {
                            return method.invoke(proxied, args);
                        }
                    }});
    }

    @Override
    public void bootstrap(IdentityConfiguration identityConfig, IdentityStoreInvocationContextFactory contextFactory) {
        for (StoreConfiguration config : identityConfig.getConfiguredStores()) {

            config.init();

            if (IdentityStoreConfiguration.class.isInstance(config)) {
                IdentityStoreConfiguration identityStoreConfig = (IdentityStoreConfiguration) config;
                if (identityStoreConfig.getFeatureSet() == null) {
                    throw new SecurityConfigurationException(
                            "A feature set has not been configured for IdentityStoreConfiguration: " +
                            config);
                }

                Map<Feature,IdentityStoreConfiguration> featureToStoreMap;

                String realm = identityStoreConfig.getRealm();
                if (realm == null || realm.isEmpty()) {
                    realm = Realm.DEFAULT_REALM;
                }

                if (realmStores.containsKey(realm)) {
                    featureToStoreMap = realmStores.get(realm);
                } else {
                    featureToStoreMap = new HashMap<Feature,IdentityStoreConfiguration>();
                    realmStores.put(realm, featureToStoreMap);
                }

                for (Feature f : identityStoreConfig.getFeatureSet()) {
                    featureToStoreMap.put(f, identityStoreConfig);
                }
            } else if (PartitionStoreConfiguration.class.isInstance(config)) {
                partitionStore = (PartitionStoreConfiguration) config;
            }
        }

        this.contextFactory = contextFactory;
    }

    @Override
    public void setIdentityStoreFactory(StoreFactory factory) {
        this.storeFactory = factory;
    }

    private IdentityStore<?> getContextualStoreForFeature(IdentityStoreInvocationContext ctx, Feature feature) {
        String realm = (ctx.getRealm() != null) ? ctx.getRealm().getName() : Realm.DEFAULT_REALM;

        if (!realmStores.containsKey(realm)) {
            throw new SecurityException("The specified realm '" + realm + "' has not been configured."); 
        }

        IdentityStoreConfiguration config = null;
        Map<Feature,IdentityStoreConfiguration> featureToStoreMap = realmStores.get(realm);

        if (featureToStoreMap.containsKey(feature)) {
            config = featureToStoreMap.get(feature);
        } else if (featureToStoreMap.containsKey(Feature.all)) {
            config = featureToStoreMap.get(Feature.all);
        } else {
            throw new UnsupportedOperationException("The requested identity management feature [" + 
                    feature.toString() + "] has not been configured.");
        }

        IdentityStore<?> store = storeFactory.createIdentityStore(config, ctx);
        getContextFactory().initContextForStore(ctx, store);
        return store;
    }

    private IdentityStoreInvocationContext createContext() {
        return getContextFactory().createContext();
    }

    @Override
    public void createUser(User user) {
        getContextualStoreForFeature(createContext(), Feature.createUser).createUser(user);
    }

    @Override
    public void removeUser(User user) {
        getContextualStoreForFeature(createContext(), Feature.deleteUser).removeUser(user);
    }

    @Override
    public void updateUser(User user) {
        getContextualStoreForFeature(createContext(), Feature.updateUser).updateUser(user);
    }

    @Override
    public User getUser(String name) {
        return getContextualStoreForFeature(createContext(), Feature.readUser).getUser(name);
    }

    @Override
    public void createGroup(Group group) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        getContextualStoreForFeature(ctx, Feature.createGroup).createGroup(group);
    }

    @Override
    public void removeGroup(Group group) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        getContextualStoreForFeature(ctx, Feature.deleteGroup).removeGroup(group);
    }

    @Override
    public Group getGroup(String groupId) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(groupName, parent);
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
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        getContextualStoreForFeature(ctx, Feature.createRole).createRole(role);
    }

    @Override
    public void removeRole(Role role) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        getContextualStoreForFeature(ctx, Feature.deleteRole).removeRole(role);
    }

    @Override
    public Role getRole(String name) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                    "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readRole).getRole(name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.IdentityManager#hasRole(org.picketlink.idm.model.Role, org.picketlink.idm.model.IdentityType, org.picketlink.idm.model.Group)
     */
    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {

        // TODO rewrite this implementation to use the IdentityCache instead of a query

        return false;
    }

    @Override
    public void grantGroupRole(IdentityType identityType, Role role, Group group) {
        getContextualStoreForFeature(createContext(), Feature.createMembership).createMembership(identityType, group, role);
    }

    @Override
    public void revokeGroupRole(IdentityType identityType, Role role, Group group) {
        throw new RuntimeException();
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean validateCredential(User user, Credential credential) {
        return getContextualStoreForFeature(createContext(), 
                Feature.validateCredential).validateCredential(user, credential);
    }

    @Override
    public void updateCredential(User user, Credential credential) {
        getContextualStoreForFeature(createContext(), Feature.validateCredential)
            .updateCredential(user, credential);
    }

    @Override
    public void setEnabled(IdentityType identityType, boolean enabled) {
        throw new RuntimeException();
    }

    @Override
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

    @Override
    public void setAttribute(IdentityType identityType, Attribute<? extends Serializable> attribute) {
        // TODO implement this
    }

    @Override
    public <T extends Serializable> Attribute<T> getAttribute(IdentityType identityType, String attributeName) {
        // TODO implement this
        return null;
    }

    @Override
    public void removeAttribute(IdentityType identityType, String attributeName) {
        // TODO implement this
    }

    @Override
    public void createRealm(Realm realm) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeRealm(Realm realm) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Realm getRealm(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void createTier(Tier tier) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeTier(Tier tier) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Tier getTier(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateRole(Role role) {
        
    }

}