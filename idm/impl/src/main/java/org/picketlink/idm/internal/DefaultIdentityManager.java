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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.config.StoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default implementation of the IdentityManager interface
 *
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {

    private Map<String,Map<Feature,IdentityStoreConfiguration>> realmStores = new HashMap<String,Map<Feature,IdentityStoreConfiguration>>();

    private PartitionStoreConfiguration partitionStoreConfig;

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
        if(identityConfig == null){
            throw new IllegalArgumentException("identityConfig is null");
        }
        if(contextFactory == null){
            throw new IllegalArgumentException("contextFactory is null");
        }
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
                partitionStoreConfig = (PartitionStoreConfiguration) config;
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
    public void add(IdentityType identityType) {
        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        if (User.class.isInstance(identityType)) {
            feature = Feature.createUser;
        } else if (Group.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }
            feature = Feature.createGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.createRole;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType");
        }

        getContextualStoreForFeature(ctx, feature).add(identityType);
    }

    @Override
    public void update(IdentityType identityType) {
        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        if (User.class.isInstance(identityType)) {
            feature = Feature.updateUser;
        } else if (Group.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.updateGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.updateRole;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType");
        }

        getContextualStoreForFeature(createContext(), feature).update(identityType);
    }

    @Override
    public void remove(IdentityType identityType) {
        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        if (User.class.isInstance(identityType)) {
            feature = Feature.deleteUser;
        } else if (Group.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.deleteGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the " +
                        "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.deleteRole;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType");
        }

        getContextualStoreForFeature(ctx, feature).remove(identityType);
    }

    public Agent getAgent(String id) {
        return getContextualStoreForFeature(createContext(), Feature.readUser).getAgent(id);
    }

    @Override
    public User getUser(String id) {
        return getContextualStoreForFeature(createContext(), Feature.readUser).getUser(id);
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
        return getContextualStoreForFeature(createContext(), Feature.createMembership).getMembership(identityType, group, null) != null;
    }

    @Override
    public void addToGroup(IdentityType identityType, Group group) {
        getContextualStoreForFeature(createContext(), Feature.readRole).createMembership(identityType, group, null);
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group) {
        getContextualStoreForFeature(createContext(), Feature.readRole).removeMembership(identityType, group, null);
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

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {
        return getContextualStoreForFeature(createContext(), Feature.createMembership)
                .getMembership(identityType, group, role) != null;
    }

    @Override
    public void grantGroupRole(IdentityType identityType, Role role, Group group) {
        getContextualStoreForFeature(createContext(), Feature.createMembership)
        .createMembership(identityType, group, role);
    }

    @Override
    public void revokeGroupRole(IdentityType identityType, Role role, Group group) {
        getContextualStoreForFeature(createContext(), Feature.createMembership)
            .removeMembership(identityType, group, role);
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        return getContextualStoreForFeature(createContext(), Feature.createMembership)
                .getMembership(identityType, null, role) != null;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        getContextualStoreForFeature(createContext(), Feature.createMembership)
            .createMembership(identityType, null, role);
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        getContextualStoreForFeature(createContext(), Feature.deleteMembership)
            .removeMembership(identityType, null, role);
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), 
                Feature.manageCredentials);
        store.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object credential) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), 
                Feature.manageCredentials);
        store.updateCredential(agent, credential);
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
    public <T extends IdentityType> IdentityQuery<T> createQuery(Class<T> identityType) {
        return new DefaultIdentityQuery<T>(identityType, getContextualStoreForFeature(createContext(), Feature.createMembership));
    }

    @Override
    public void createRealm(Realm realm) {
        storeFactory.createPartitionStore(partitionStoreConfig).createPartition(realm);
    }

    @Override
    public void removeRealm(Realm realm) {
        storeFactory.createPartitionStore(partitionStoreConfig).removePartition(realm);
    }

    @Override
    public Realm getRealm(String name) {
        return storeFactory.createPartitionStore(partitionStoreConfig).getRealm(name);
    }

    @Override
    public void createTier(Tier tier) {
        storeFactory.createPartitionStore(partitionStoreConfig).createPartition(tier);
    }

    @Override
    public void removeTier(Tier tier) {
        storeFactory.createPartitionStore(partitionStoreConfig).removePartition(tier);
    }

    @Override
    public Tier getTier(String id) {
        return storeFactory.createPartitionStore(partitionStoreConfig).getTier(id);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {
        
    }

}