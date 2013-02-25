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
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.internal.util.IDMUtil;
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

    private Map<String,Set<IdentityStoreConfiguration>> realmStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

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

        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {

            config.init();

            Set<IdentityStoreConfiguration> configs;

            Set<String> realms = new HashSet<String>();

            if (config.getRealms().isEmpty()) {
                realms.add(Realm.DEFAULT_REALM);
            } else {
                realms.addAll(config.getRealms());
            }

            for (String realm : realms) {
                if (realmStores.containsKey(realm)) {
                    configs = realmStores.get(realm);
                } else {
                    configs = new HashSet<IdentityStoreConfiguration>();
                    realmStores.put(realm, configs);
                }
                configs.add(config);
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
        if (identityType == null) {
            throw new IdentityManagementException("You can not add a null IdentityType instance.");
        }

        FeatureGroup feature;

        IdentityStoreInvocationContext ctx = createContext();

        Partition currentPartition = getCurrentPartition(ctx);

        if (Agent.class.isInstance(identityType)) {
            feature = FeatureGroup.agent;

            Agent newAgent = (Agent) identityType;

            if (newAgent.getLoginName() == null) {
                throw new IdentityManagementException("No login name was provided.");
            }

            if (User.class.isInstance(newAgent)) {
                feature = FeatureGroup.user;

                if (getUser(newAgent.getLoginName()) != null) {
                    throw new IdentityManagementException("User already exists with the given login name ["
                            + newAgent.getLoginName() + "] for the given Partition [" + currentPartition.getName() + "]");
                }
            } else {
                if (getAgent(newAgent.getLoginName()) != null) {
                    throw new IdentityManagementException("Agent already exists with the given login name ["
                            + newAgent.getLoginName() + "] for the given Realm [" + currentPartition.getName() + "]");
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

            feature = FeatureGroup.group;
        } else if (Role.class.isInstance(identityType)) {
            Role newRole = (Role) identityType;

            if (newRole.getName() == null) {
                throw new IdentityManagementException("No name was provided.");
            }

            if (getRole(newRole.getName()) != null) {
                throw new IdentityManagementException("Role already exists with the given name [" + newRole.getName()
                        + "] for the given Partition [" + currentPartition.getName() + "]");
            }

            feature = FeatureGroup.role;
        } else if (Relationship.class.isInstance(identityType)) {
            feature = FeatureGroup.relationship;
        } else {
            throw new IllegalArgumentException("Unsupported IdentityType:" + identityType.getClass().getName());
        }

        getContextualStoreForFeature(ctx, feature, FeatureOperation.create).add(identityType);
    }



    @Override
    public void add(Relationship relationship) {
        IdentityStoreInvocationContext ctx = createContext();

        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.create).add(relationship);
    }

    @Override
    public void update(IdentityType identityType) {
        checkIfIdentityTypeExists(identityType);

        getContextualStoreForFeature(createContext(), IDMUtil.getFeatureGroup(identityType), FeatureOperation.update).update(identityType);
    }

    @Override
    public void update(Relationship relationship) {
        IdentityStoreInvocationContext ctx = createContext();

        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.update).update(relationship);
    }

    @Override
    public void remove(IdentityType identityType) {
        checkIfIdentityTypeExists(identityType);

        FeatureGroup feature = IDMUtil.getFeatureGroup(identityType);

        IdentityStoreInvocationContext ctx = createContext();

        if (FeatureGroup.group.equals(feature)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Group may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }
        } else if (FeatureGroup.role.equals(feature)) {
            if (ctx.getRealm() != null && ctx.getTier() != null) {
                throw new IllegalStateException("Ambiguous context state - Role may only be managed in either the "
                        + "scope of a Realm or a Tier, however both have been set.");
            }
        }

        getContextualStoreForFeature(ctx, feature, FeatureOperation.delete).remove(identityType);
    }

    @Override
    public void remove(Relationship relationship) {
        IdentityStoreInvocationContext ctx = createContext();
        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.delete).remove(relationship);
    }

    public Agent getAgent(String loginName) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.agent, FeatureOperation.read).getAgent(loginName);
    }

    @Override
    public User getUser(String loginName) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.user, FeatureOperation.read).getUser(loginName);
    }

    @Override
    public Group getGroup(String name) {
        if (name == null) {
            return null;
        }

        IdentityStoreInvocationContext ctx = createContext();
        return getContextualStoreForFeature(ctx, FeatureGroup.group, FeatureOperation.read).getGroup(name);
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

        return getContextualStoreForFeature(ctx, FeatureGroup.group, FeatureOperation.read).getGroup(name, parent);
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

        if (getGroupMembership(member, group) == null) {
            add(new GroupMembership(member, group));
        }
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        checkIfIdentityTypeExists(member);
        checkIfIdentityTypeExists(group);

        getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.delete).remove(new GroupMembership(member, group));
    }

    @Override
    public Role getRole(String name) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.role, FeatureOperation.read).getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        checkNotNull(assignee);
        checkNotNull(role);
        checkNotNull(group);

        return getGroupRole(assignee, role, group) != null;
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        checkIfIdentityTypeExists(assignee);
        checkIfIdentityTypeExists(role);
        checkIfIdentityTypeExists(group);

        if (getGroupRole(assignee, role, group) == null) {
            add(new GroupRole(assignee, group, role));
        }
    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        checkIfIdentityTypeExists(assignee);
        checkIfIdentityTypeExists(role);
        checkIfIdentityTypeExists(group);

        getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.delete).remove(new GroupRole(assignee, group, role));
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        checkNotNull(identityType);
        checkNotNull(role);

        return getGrant(identityType, role) != null;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        if (!(Agent.class.isInstance(identityType) || Group.class.isInstance(identityType))) {
            throw new IdentityManagementException("Only Agent and Group types are supported for this relationship type.");
        }

        checkIfIdentityTypeExists(identityType);
        checkIfIdentityTypeExists(role);

        if (getGrant(identityType, role) == null) {
            add(new Grant(identityType, role));
        }
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        checkIfIdentityTypeExists(identityType);
        checkIfIdentityTypeExists(role);

        getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.delete).remove(new Grant(identityType, role));
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.credential, FeatureOperation.validate);
        store.validateCredentials(credentials);
    }

    @Override
    public void updateCredential(Agent agent, Object value) {
        updateCredential(agent, value, new Date(), null);
    }

    @Override
    public void updateCredential(Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.credential, FeatureOperation.update);
        store.updateCredential(agent, credential, effectiveDate, expiryDate);
    }

    @Override
    public <T extends IdentityType> IdentityQuery<T> createIdentityQuery(Class<T> identityType) {
        return new DefaultIdentityQuery<T>(identityType, getContextualStoreForFeature(createContext(), FeatureGroup.user, FeatureOperation.read));
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return new DefaultRelationshipQuery<T>(relationshipType, getContextualStoreForFeature(createContext(),
                FeatureGroup.relationship, FeatureOperation.read));
    }

    @Override
    public void createRealm(Realm realm) {
        checkCreateNullPartition(realm);
        checkCreateNullPartitionName(realm);

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.realm, FeatureOperation.create);
        if (store != null) {
            ((PartitionStore) store).createPartition(realm);
        }
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

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.realm, FeatureOperation.delete);
        if (store != null) {
            ((PartitionStore) store).removePartition(realm);
        }
    }

    @Override
    public Realm getRealm(String name) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.realm, FeatureOperation.read);
        return store != null ? ((PartitionStore) store).getRealm(name) : null;
    }

    @Override
    public void createTier(Tier tier) {
        checkCreateNullPartition(tier);
        checkCreateNullPartitionName(tier);
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.create);
        if (store != null) {
            ((PartitionStore) store).createPartition(tier);
        }
    }

    @Override
    public void removeTier(Tier tier) {
        if (tier == null) {
            throw new IdentityManagementException("You must provide a non-nul Tier instance.");
        }

        if (getTier(tier.getName()) == null) {
            throw new IdentityManagementException("No Tier with the given name [" + tier.getName() + "] was found.");
        }

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.delete);
        if (store != null) {
            ((PartitionStore) store).removePartition(tier);
        }
    }

    @Override
    public Tier getTier(String id) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.read);
        return store != null ? ((PartitionStore) store).getTier(id) : null;
    }

    @Override
    public void loadAttribute(IdentityType identityType, String attributeName) {

    }

    private GroupRole getGroupRole(IdentityType identityType, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(GroupRole.class);

        query.setParameter(GroupRole.ASSIGNEE, identityType);
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

    private IdentityStore<?> getContextualStoreForFeature(IdentityStoreInvocationContext ctx, FeatureGroup feature, FeatureOperation operation) {
        return getContextualStoreForFeature(ctx, feature, operation, null);
    }

    private IdentityStore<?> getContextualStoreForFeature(final IdentityStoreInvocationContext ctx, FeatureGroup feature, FeatureOperation operation,
            Class<? extends Relationship> relationshipClass) {
        String realm = (ctx.getRealm() != null) ? ctx.getRealm().getName() : Realm.DEFAULT_REALM;

        if (!realmStores.containsKey(realm)) {
            if (realmStores.isEmpty()) {
                throw new SecurityException("No identity stores have been configured.");
            } else {
                throw new SecurityException("The specified realm '" + realm + "' has not been configured.");
            }
        }

        Set<IdentityStoreConfiguration> configs = realmStores.get(realm);

        IdentityStoreConfiguration config = null;

        for (IdentityStoreConfiguration cfg : configs) {
            if (relationshipClass != null) {
                if (cfg.getFeatureSet().supportsRelationshipFeature(relationshipClass, operation)) {
                    config = cfg;
                    break;
                }
            } else if (cfg.getFeatureSet().supports(feature, operation)) {
                config = cfg;
                break;
            }
        }

        if (config == null) {
            throw new SecurityConfigurationException("No identity store configuration found for requested operation [" + 
                    feature.toString() + "." + operation.toString() + "]");
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
    
    private Partition getCurrentPartition(IdentityStoreInvocationContext ctx) {
        Realm realm = ctx.getRealm();

        if (realm == null) {
            realm = new Realm(Realm.DEFAULT_REALM);
        }

        Partition currentPartition = realm;

        if (ctx.getTier() != null) {
            currentPartition = ctx.getTier();
        }
        return currentPartition;
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
}