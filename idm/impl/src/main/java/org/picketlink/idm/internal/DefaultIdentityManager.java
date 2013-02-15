/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.idm.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.Feature;
import org.picketlink.idm.config.StoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
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
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default implementation of the IdentityManager interface
 * 
 * @author Shane Bryzak
 * @author anil saldhana
 */
public class DefaultIdentityManager implements IdentityManager {

    private static final long serialVersionUID = -2835518073812662628L;

    private Map<String, Map<Feature, Set<IdentityStoreConfiguration>>> realmStores = new HashMap<String, Map<Feature, Set<IdentityStoreConfiguration>>>();

    private StoreFactory storeFactory = new DefaultStoreFactory();

    private IdentityStoreInvocationContextFactory contextFactory;

    private ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
    private ThreadLocal<Tier> currentTier = new ThreadLocal<Tier>();

    @Override
    public IdentityManager forRealm(final Realm realm) {
        final DefaultIdentityManager proxied = this;
        final Tier tier = currentTier.get();
        return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] { IdentityManager.class }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        try {
                            currentRealm.set(realm);
                            currentTier.set(tier);
                            result = method.invoke(proxied, args);
                        } catch (Exception e) {
                            if (e.getCause() != null) {
                                throw e.getCause();
                            }

                            throw e;
                        } finally {
                            currentRealm.remove();
                            currentTier.remove();
                        }

                        return result;
                    }
                });
    }

    @Override
    public IdentityManager forTier(final Tier tier) {
        final DefaultIdentityManager proxied = this;
        final Realm realm = currentRealm.get();

        return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] { IdentityManager.class }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        try {
                            currentRealm.set(realm);
                            currentTier.set(tier);
                            result = method.invoke(proxied, args);
                        } catch (Exception e) {
                            if (e.getCause() != null) {
                                throw e.getCause();
                            }

                            throw e;
                        } finally {
                            currentRealm.remove();
                            currentTier.remove();
                        }

                        return result;
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

                Map<Feature, Set<IdentityStoreConfiguration>> featureToStoreMap;

                String realm = identityStoreConfig.getRealm();
                if (realm == null || realm.isEmpty()) {
                    realm = Realm.DEFAULT_REALM;
                }

                if (realmStores.containsKey(realm)) {
                    featureToStoreMap = realmStores.get(realm);
                } else {
                    featureToStoreMap = new HashMap<Feature, Set<IdentityStoreConfiguration>>();
                    realmStores.put(realm, featureToStoreMap);
                }

                for (Feature f : Feature.values()) {
                    if (identityStoreConfig.getFeatureSet().supports(f)) {
                        if (!featureToStoreMap.containsKey(f)) {
                            featureToStoreMap.put(f, new HashSet<IdentityStoreConfiguration>());
                        }
                        featureToStoreMap.get(f).add(identityStoreConfig);
                    }
                }
            }
        }

        this.contextFactory = contextFactory;
    }

    @Override
    public void setIdentityStoreFactory(StoreFactory factory) {
        this.storeFactory = factory;
    }

    @Override
    public void add(IdentityType identityType) {
        Feature feature;

        IdentityStoreInvocationContext ctx = createContext();

        Realm realm = ctx.getRealm();

        if (realm == null) {
            realm = new Realm(Realm.DEFAULT_REALM);
        }

        Partition currentPartition = realm;

        if (ctx.getTier() != null) {
            currentPartition = ctx.getTier();
        }

        if (Agent.class.isInstance(identityType)) {
            feature = Feature.createAgent;

            Agent newAgent = (Agent) identityType;

            if (newAgent.getLoginName() == null) {
                throw new IdentityManagementException("No login name was provided.");
            }

            if (User.class.isInstance(newAgent)) {
                feature = Feature.createUser;

                if (getUser(newAgent.getLoginName()) != null) {
                    throw new IdentityManagementException("User already exists with the given login name ["
                            + newAgent.getLoginName() + "] for the given Realm [" + realm.getName() + "]");
                }
            } else {
                if (getAgent(newAgent.getLoginName()) != null) {
                    throw new IdentityManagementException("Agent already exists with the given login name ["
                            + newAgent.getLoginName() + "] for the given Realm [" + realm.getName() + "]");
                }
            }
        } else if (Group.class.isInstance(identityType)) {
            Group newGroup = (Group) identityType;

            if (newGroup.getName() == null) {
                throw new IdentityManagementException("No name was provided.");
            }

            if (getGroup(newGroup.getPath()) != null) {
                throw new IdentityManagementException("Group already exists with the given name [" + newGroup.getName()
                        + "] for the given Partition [" + currentPartition.getName() + "]");
            }

            if (newGroup.getParentGroup() != null) {
                if (lookupIdentityById(Group.class, newGroup.getParentGroup().getId()) == null) {
                    throw new IdentityManagementException("No parent group found with the given id ["
                            + newGroup.getParentGroup().getId() + "] for the given Partition [" + currentPartition.getName()
                            + "].");
                }
            }

            feature = Feature.createGroup;
        } else if (Role.class.isInstance(identityType)) {
            Role newRole = (Role) identityType;

            if (newRole.getName() == null) {
                throw new IdentityManagementException("No name was provided.");
            }

            if (getRole(newRole.getName()) != null) {
                throw new IdentityManagementException("Role already exists with the given name [" + newRole.getName()
                        + "] for the given Partition [" + currentPartition.getName() + "]");
            }

            feature = Feature.createRole;
        } else if (Relationship.class.isInstance(identityType)) {
            feature = Feature.createRelationship;
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
        checkIfIdentityTypeExists(identityType);

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
            throw new IdentityManagementException("No identifier provided.");
        }

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw new IdentityManagementException("No IdentityType found with the given id [" + identityType.getId() + "].");
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

    public Agent getAgent(String loginName) {
        return getContextualStoreForFeature(createContext(), Feature.readAgent).getAgent(loginName);
    }

    @Override
    public User getUser(String loginName) {
        return getContextualStoreForFeature(createContext(), Feature.readUser).getUser(loginName);
    }

    @Override
    public Group getGroup(String name) {
        if (name == null) {
            return null;
        }

        IdentityStoreInvocationContext ctx = createContext();
        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(name);
    }

    @Override
    public Group getGroup(String name, Group parent) {
        if (name == null || parent == null) {
            return null;
        }

        if (lookupIdentityById(Group.class, parent.getId()) == null) {
            throw new IdentityManagementException("No parent group found with the given id [" + parent.getId() + "]");
        }

        IdentityStoreInvocationContext ctx = createContext();

        if (ctx.getRealm() != null && ctx.getTier() != null) {
            throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                    + "scope of a Realm or a Tier, however both have been set.");
        }

        return getContextualStoreForFeature(ctx, Feature.readGroup).getGroup(name, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        checkNotNull(identityType);
        checkNotNull(group);

        return getGroupMembership(identityType, group) != null;
    }

    @Override
    public void addToGroup(Agent member, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(group);

        add(new GroupMembership(member, group));
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(group);

        getContextualStoreForFeature(createContext(), Feature.deleteRelationship).remove(new GroupMembership(member, group));
    }

    @Override
    public Role getRole(String name) {
        return getContextualStoreForFeature(createContext(), Feature.readRole).getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType identityType, Role role, Group group) {
        checkNotNull(identityType);
        checkNotNull(role);
        checkNotNull(group);

        return getGroupRole(identityType, role, group) != null;
    }

    @Override
    public void grantGroupRole(Agent member, Role role, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(role);
        checkIfIdentityTypeExists(group);

        if (getGroupRole(member, role, group) == null) {
            add(new GroupRole(member, group, role));
        }
    }

    @Override
    public void revokeGroupRole(Agent member, Role role, Group group) {
        GroupRole groupRole = getGroupRole(member, role, group);

        if (groupRole != null) {
            getContextualStoreForFeature(createContext(), Feature.deleteRelationship).remove(groupRole);
        }
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        checkNotNull(identityType);
        checkNotNull(role);

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
        if (!(Agent.class.isInstance(identityType) || Group.class.isInstance(identityType))) {
            throw new IdentityManagementException("Only Agent and Group types are supported for this relationship type.");
        }

        if (lookupIdentityById(IdentityType.class, identityType.getId()) == null) {
            throw new IdentityManagementException("No IdentityType found with the given id [" + identityType.getId() + "]");
        }

        if (role == null) {
            throw new IdentityManagementException("You must assign a role for this relationship type.");
        }

        if (lookupIdentityById(Role.class, role.getId()) == null) {
            throw new IdentityManagementException("No Role was found with the given id [" + role.getId() + "]");
        }

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

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery<T>(identityType, getContextualStoreForFeature(createContext(), Feature.readUser));
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return new DefaultRelationshipQuery<T>(relationshipType, getContextualStoreForFeature(createContext(),
                Feature.readRelationship));
    }

    @Override
    public void createRealm(Realm realm) {
        checkCreateNullPartition(realm);
        checkCreateNullPartitionName(realm);
        getContextualPartitionStore().createPartition(realm);
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        if (identityType == null) {
            throw new IdentityManagementException("You must provide the IdentityType class.");
        }

        if (id == null) {
            throw new IdentityManagementException("Could not lookup with a null identifier.");
        }

        IdentityQuery<T> query = createIdentityQuery(identityType);

        query.setParameter(IdentityType.ID, id);

        List<T> result = query.getResultList();

        T identity = null;

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw new IdentityManagementException("Ambiguous IdentityType for identifier [" + id + "].");
            } else {
                identity = result.get(0);
            }
        }

        return identity;
    }

    @Override
    public void removeRealm(Realm realm) {
        if (realm == null) {
            throw new IdentityManagementException("You must provide a non-nul Realm instance.");
        }

        if (getRealm(realm.getName()) == null) {
            throw new IdentityManagementException("No Realm with the given name [" + realm.getName() + "] was found.");
        }

        getContextualPartitionStore().removePartition(realm);
    }

    @Override
    public Realm getRealm(String name) {
        return getContextualPartitionStore().getRealm(name);
    }

    @Override
    public void createTier(Tier tier) {
        checkCreateNullPartition(tier);
        checkCreateNullPartitionName(tier);
        getContextualPartitionStore().createPartition(tier);
    }

    @Override
    public void removeTier(Tier tier) {
        if (tier == null) {
            throw new IdentityManagementException("You must provide a non-nul Tier instance.");
        }

        if (getTier(tier.getName()) == null) {
            throw new IdentityManagementException("No Tier with the given name [" + tier.getName() + "] was found.");
        }

        getContextualPartitionStore().removePartition(tier);
    }

    @Override
    public Tier getTier(String id) {
        return getContextualPartitionStore().getTier(id);
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {

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

    private void checkCreateNullPartitionName(Partition partition) {
        if (partition.getName() == null) {
            throw new IdentityManagementException("Realm name must not be null");
        }
    }

    private void checkCreateNullPartition(Partition partition) {
        if (partition == null) {
            throw new IdentityManagementException("Partition must not be null.");
        }
    }

    private PartitionStore getContextualPartitionStore() {
        @SuppressWarnings("unchecked")
        final IdentityStore<IdentityStoreConfiguration> store = (IdentityStore<IdentityStoreConfiguration>) getContextualStoreForFeature(
                createPartitionContext(), Feature.managePartitions);

        if (PartitionStore.class.isInstance(store)) {
            return (PartitionStore) store;
        }

        throw new IdentityManagementException("No PartitionStore configured.");
    }

    private IdentityStore<?> getContextualStoreForFeature(IdentityStoreInvocationContext ctx, Feature feature) {
        return getContextualStoreForFeature(ctx, feature, null);
    }

    private IdentityStore<?> getContextualStoreForFeature(final IdentityStoreInvocationContext ctx, Feature feature,
            Class<? extends Relationship> relationshipClass) {
        String realm = (ctx.getRealm() != null) ? ctx.getRealm().getName() : Realm.DEFAULT_REALM;

        if (!realmStores.containsKey(realm)) {
            throw new SecurityException("The specified realm '" + realm + "' has not been configured.");
        }

        IdentityStoreConfiguration config = null;
        Map<Feature, Set<IdentityStoreConfiguration>> featureToStoreMap = realmStores.get(realm);

        Set<IdentityStoreConfiguration> stores;

        if (featureToStoreMap.containsKey(feature)) {
            stores = featureToStoreMap.get(feature);
        } else if (featureToStoreMap.containsKey(Feature.all)) {
            stores = featureToStoreMap.get(Feature.all);
        } else {
            throw new SecurityConfigurationException("No identity store configuration found for requested feature [" + feature
                    + "]");
        }

        if (stores.size() > 1) {
            throw new SecurityConfigurationException("Ambiguous security configuration - multiple identity stores have been "
                    + "configured for feature [" + feature + "]");
        } else {
            config = stores.iterator().next();
        }

        if (config == null) {
            throw new SecurityConfigurationException("No identity store configuration found for requested feature [" + feature
                    + "]");
        }

        if (relationshipClass != null) {
            if (!config.getFeatureSet().supportsRelationship(relationshipClass)) {
                throw new SecurityConfigurationException("No identity store configuration found for requested feature ["
                        + feature + "] with relationship type [" + relationshipClass.getName() + "]");
            }
        }

        @SuppressWarnings("unchecked")
        final IdentityStore<IdentityStoreConfiguration> store = storeFactory.createIdentityStore(config, ctx);

        this.contextFactory.initContextForStore(ctx, store);

        store.setup(config, ctx);

        return store;
    }

    private IdentityStoreInvocationContext createContext() {
        IdentityStoreInvocationContext context = this.contextFactory.createContext();

        context.setRealm(currentRealm.get());
        context.setTier(currentTier.get());

        return context;
    }

    private IdentityStoreInvocationContext createPartitionContext() {
        return this.contextFactory.createContext();
    }

    private void checkIfIdentityTypeExists(IdentityType identityType) {
        checkNotNull(identityType);

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw new IdentityManagementException("No IdentityType [" + identityType.getClass().getName()
                    + "] found with the given id [" + identityType + "]");
        }
    }

    private void checkNotNull(IdentityType identityType) {
        if (identityType == null) {
            throw new IdentityManagementException("You must provide a non-null IdentityType.");
        }
    }
}