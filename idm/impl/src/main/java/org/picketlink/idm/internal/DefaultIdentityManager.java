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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.config.StoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
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

    private static final long serialVersionUID = -2835518073812662628L;

    private Map<String, Map<Feature, IdentityStoreConfiguration>> realmStores = new HashMap<String, Map<Feature, IdentityStoreConfiguration>>();

    private PartitionStoreConfiguration partitionStoreConfig;

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
                new Class[] { IdentityManager.class }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(METHOD_CREATE_CONTEXT)) {
                            IdentityStoreInvocationContext ctx = proxied.createContext();
                            ctx.setRealm(realm);
                            return ctx;
                        } else {
                            return method.invoke(proxied, args);
                        }
                    }
                });
    }

    @Override
    public IdentityManager forTier(final Tier tier) {
        final DefaultIdentityManager proxied = this;

        return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] { IdentityManager.class }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.equals(METHOD_CREATE_CONTEXT)) {
                            IdentityStoreInvocationContext ctx = proxied.createContext();
                            ctx.setTier(tier);
                            return ctx;
                        } else {
                            return method.invoke(proxied, args);
                        }
                    }
                });
    }

    @Override
    public void bootstrap(IdentityConfiguration identityConfig, IdentityStoreInvocationContextFactory contextFactory) {
        if (identityConfig == null) {
            throw new IllegalArgumentException("identityConfig is null");
        }
        if (contextFactory == null) {
            throw new IllegalArgumentException("contextFactory is null");
        }
        for (StoreConfiguration config : identityConfig.getConfiguredStores()) {

            config.init();

            if (IdentityStoreConfiguration.class.isInstance(config)) {
                IdentityStoreConfiguration identityStoreConfig = (IdentityStoreConfiguration) config;
                if (identityStoreConfig.getFeatureSet() == null) {
                    throw new SecurityConfigurationException(
                            "A feature set has not been configured for IdentityStoreConfiguration: " + config);
                }

                Map<Feature, IdentityStoreConfiguration> featureToStoreMap;

                String realm = identityStoreConfig.getRealm();
                if (realm == null || realm.isEmpty()) {
                    realm = Realm.DEFAULT_REALM;
                }

                if (realmStores.containsKey(realm)) {
                    featureToStoreMap = realmStores.get(realm);
                } else {
                    featureToStoreMap = new HashMap<Feature, IdentityStoreConfiguration>();
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
        Map<Feature, IdentityStoreConfiguration> featureToStoreMap = realmStores.get(realm);

        if (featureToStoreMap.containsKey(feature)) {
            config = featureToStoreMap.get(feature);
        } else if (featureToStoreMap.containsKey(Feature.all)) {
            config = featureToStoreMap.get(Feature.all);
        } else {
            throw new UnsupportedOperationException("The requested identity management feature [" + feature.toString()
                    + "] has not been configured.");
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
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }
            feature = Feature.createGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.createRole;
        } else if (Agent.class.isInstance(identityType)) {
            feature = Feature.createAgent;
        } else if (Relationship.class.isInstance(identityType)) {
            feature = Feature.createAgent;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType:" + identityType.getClass().getName());
        }

        getContextualStoreForFeature(ctx, feature).add(identityType);
    }

    @Override
    public void add(Relationship relationship) {
        Feature feature = Feature.createRelationship;

        IdentityStoreInvocationContext ctx = createContext();

        getContextualStoreForFeature(ctx, feature).add(relationship);
    }

    @Override
    public void update(IdentityType identityType) {
        if (identityType.getId() == null) {
            throw new IdentityManagementException("No identifier was specified.");
        }
        
        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        if (User.class.isInstance(identityType)) {
            feature = Feature.updateUser;
        } else if (Agent.class.isInstance(identityType)) {
            feature = Feature.updateAgent;
        } else if (Group.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.updateGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.updateRole;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType");
        }

        getContextualStoreForFeature(createContext(), feature).update(identityType);
    }

    @Override
    public void update(Relationship relationship) {
        Feature feature = Feature.updateRelationship;

        IdentityStoreInvocationContext ctx = createContext();

        getContextualStoreForFeature(ctx, feature).update(relationship);
    }

    @Override
    public void remove(IdentityType identityType) {
        if (identityType.getId() == null) {
            throw new IdentityManagementException("No identifier was specified.");
        }

        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        if (User.class.isInstance(identityType)) {
            feature = Feature.deleteUser;
        } else if (Agent.class.isInstance(identityType)) {
            feature = Feature.deleteAgent;
        } else if (Group.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.deleteGroup;
        } else if (Role.class.isInstance(identityType)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }

            feature = Feature.deleteRole;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType");
        }

        getContextualStoreForFeature(ctx, feature).remove(identityType);
    }

    @Override
    public void remove(Relationship relationship) {
        Feature feature = Feature.deleteRelationship;

        IdentityStoreInvocationContext ctx = createContext();

        getContextualStoreForFeature(ctx, feature).remove(relationship);
    }

    public Agent getAgent(String id) {
        return getContextualStoreForFeature(createContext(), Feature.readAgent).getAgent(id);
    }

    @Override
    public User getUser(String id) {
        return getContextualStoreForFeature(createContext(), Feature.readUser).getUser(id);
    }

    @Override
    public Group getGroup(String groupId) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                    + "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(groupId);
    }

    @Override
    public Group getGroup(String groupName, Group parent) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                    + "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(groupName, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        return getGroupMembership(identityType, group) != null;
    }

    private GroupMembership getGroupMembership(IdentityType identityType, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, identityType);
        query.setParameter(GroupMembership.GROUP, group);
        
        List<GroupMembership> result = query.getResultList();
        
        GroupMembership groupMembership = null;
        
        if (!result.isEmpty()) {
            groupMembership = result.get(0);
        }
        
        return groupMembership;
    }

    @Override
    public void addToGroup(IdentityType member, Group group) {
        if (getGroupMembership(member, group) == null) {
            add(new GroupMembership(member, group));
        }
    }

    @Override
    public void removeFromGroup(IdentityType identityType, Group group) {
        GroupMembership groupMembership = getGroupMembership(identityType, group);
        
        if (groupMembership != null) {
            getContextualStoreForFeature(createContext(), Feature.deleteRelationship).remove(groupMembership);
        }
    }

    @Override
    public Role getRole(String name) {
        IdentityStoreInvocationContext ctx = createContext();
        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the "
                    + "scope of a Realm or a Tier, however both have been set.");
        }
        return getContextualStoreForFeature(ctx, Feature.readRole).getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {
        return getGroupRole(identityType, role, group) != null;
    }

    private GroupRole getGroupRole(IdentityType identityType, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.MEMBER, identityType);
        query.setParameter(GroupRole.ROLE, role);
        query.setParameter(GroupRole.GROUP, group);
        
        List<GroupRole> result = query.getResultList();
        
        GroupRole groupRole = null;
        
        if (!result.isEmpty()) {
            groupRole = result.get(0);
        }
        return groupRole;
    }

    @Override
    public void grantGroupRole(IdentityType member, Role role, Group group) {
        if (getGroupRole(member, role, group) == null) {
            add(new GroupRole(member, group, role));            
        }
    }

    @Override
    public void revokeGroupRole(IdentityType identityType, Role role, Group group) {
        GroupRole groupRole = getGroupRole(identityType, role, group);
        
        if (groupRole != null) {
            getContextualStoreForFeature(createContext(), Feature.deleteRelationship).remove(groupRole);
        }
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        return getGrant(identityType, role) != null;
    }

    private Grant getGrant(IdentityType identityType, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, identityType);
        query.setParameter(Grant.ROLE, role);
        
        List<Grant> result = query.getResultList();
        
        Grant grant = null;
        
        if (!result.isEmpty()) {
            grant = result.get(0);
        }
        return grant;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        if (getGrant(identityType, role) == null) {
            add(new Grant(identityType, role));    
        }
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        Grant grant = getGrant(identityType, role);
        
        if (grant != null) {
            getContextualStoreForFeature(createContext(), Feature.deleteRelationship).remove(grant);
        }
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), Feature.manageCredentials);
        store.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) {
        updateCredential(agent, value, new Date(), null);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), Feature.manageCredentials);
        store.updateCredential(agent, credential, effectiveDate, expiryDate);
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
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery<T>(identityType, getContextualStoreForFeature(createContext(), Feature.readUser));
    }
    
    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return new DefaultRelationshipQuery<T>(relationshipType, getContextualStoreForFeature(createContext(), Feature.readRelationship));
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