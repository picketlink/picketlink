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

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
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

    private Map<String, Set<IdentityStoreConfiguration>> realmStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    private StoreFactory storeFactory = new DefaultStoreFactory();

    private IdentityStoreInvocationContextFactory contextFactory;

    private ThreadLocal<Realm> currentRealm = new ThreadLocal<Realm>();
    private ThreadLocal<Tier> currentTier = new ThreadLocal<Tier>();

    @Override
    public void bootstrap(IdentityConfiguration identityConfig, IdentityStoreInvocationContextFactory contextFactory) {
        if (identityConfig == null) {
            throw MESSAGES.nullArgument("IdentityConfiguration");
        }

        if (contextFactory == null) {
            throw MESSAGES.nullArgument("IdentityStoreInvocationContextFactory");
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
    public IdentityManager forRealm(Realm realm) {
        if (realm != null) {
            final Realm storedRealm = getRealm(realm.getName());

            if (storedRealm != null) {
                final DefaultIdentityManager proxied = this;
                final Tier tier = currentTier.get();

                return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                        new Class[] { IdentityManager.class }, new InvocationHandler() {

                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                Object result = null;

                                try {
                                    currentRealm.set(storedRealm);
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
        }

        throw MESSAGES.couldNotCreateContextualIdentityManager(Realm.class);
    }

    @Override
    public IdentityManager forTier(Tier tier) {
        if (tier != null) {
            final Tier storedTier = getTier(tier.getName());

            if (storedTier != null) {
                final DefaultIdentityManager proxied = this;
                final Realm realm = currentRealm.get();

                return (IdentityManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                        new Class[] { IdentityManager.class }, new InvocationHandler() {

                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                Object result = null;

                                try {
                                    currentRealm.set(realm);
                                    currentTier.set(storedTier);
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
        }
        
        throw MESSAGES.couldNotCreateContextualIdentityManager(Tier.class);
    }

    @Override
    public void setIdentityStoreFactory(StoreFactory factory) {
        this.storeFactory = factory;
    }

    @Override
    public void add(IdentityType identityType) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        FeatureGroup feature;

        IdentityStoreInvocationContext ctx = createContext();

        Partition currentPartition = getCurrentPartition(ctx);

        if (Agent.class.isInstance(identityType)) {
            feature = FeatureGroup.agent;

            Agent newAgent = (Agent) identityType;

            if (StringUtil.isNullOrEmpty(newAgent.getLoginName())) {
                throw MESSAGES.nullArgument("User loginName");
            }

            if (User.class.isInstance(newAgent)) {
                feature = FeatureGroup.user;

                if (getUser(newAgent.getLoginName()) != null) {
                    throw MESSAGES.identityTypeAlreadyExists(newAgent.getClass(), newAgent.getLoginName(), currentPartition);
                }
            } else {
                if (getAgent(newAgent.getLoginName()) != null) {
                    throw MESSAGES.identityTypeAlreadyExists(newAgent.getClass(), newAgent.getLoginName(), currentPartition);
                }
            }
        } else if (Group.class.isInstance(identityType)) {
            Group newGroup = (Group) identityType;

            if (StringUtil.isNullOrEmpty(newGroup.getName())) {
                throw MESSAGES.nullArgument("Group name");
            }

            if (getGroup(newGroup.getPath()) != null) {
                throw MESSAGES.identityTypeAlreadyExists(newGroup.getClass(), newGroup.getName(), currentPartition);
            }

            if (newGroup.getParentGroup() != null) {
                if (lookupIdentityById(Group.class, newGroup.getParentGroup().getId()) == null) {
                    throw MESSAGES.groupParentNotFoundWithId(newGroup.getParentGroup().getId(), currentPartition);
                }
            }

            feature = FeatureGroup.group;
        } else if (Role.class.isInstance(identityType)) {
            Role newRole = (Role) identityType;

            if (StringUtil.isNullOrEmpty(newRole.getName())) {
                throw MESSAGES.nullArgument("Role name");
            }

            if (getRole(newRole.getName()) != null) {
                throw MESSAGES.identityTypeAlreadyExists(newRole.getClass(), newRole.getName(), currentPartition);
            }

            feature = FeatureGroup.role;
        } else {
            throw MESSAGES.identityTypeUnsupportedType(identityType.getClass());
        }
        
        try {
            getContextualStoreForFeature(ctx, feature, FeatureOperation.create).add(identityType);            
        } catch (Exception e) {
            throw MESSAGES.identityTypeAddFailed(identityType, e);
        }
    }

    @Override
    public void add(Relationship relationship) {
        try {
            getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.create,
                    relationship.getClass()).add(relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipAddFailed(relationship, e);
        }
    }

    @Override
    public void update(IdentityType identityType) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(identityType, ctx);

        try {
            getContextualStoreForFeature(ctx, IDMUtil.getFeatureGroup(identityType), FeatureOperation.update).update(identityType);    
        } catch (Exception e) {
            throw MESSAGES.identityTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void update(Relationship relationship) {
        try {
            getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.update,
                    relationship.getClass()).update(relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipUpdateFailed(relationship, e);
        }
    }

    @Override
    public void remove(IdentityType identityType) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(identityType, ctx);

        FeatureGroup feature = IDMUtil.getFeatureGroup(identityType);

        try {
            getContextualStoreForFeature(ctx, feature, FeatureOperation.delete).remove(identityType);            
        } catch (Exception e) {
            throw MESSAGES.identityTypeUpdateFailed(identityType, e);
        }
    }

    @Override
    public void remove(Relationship relationship) {
        if (relationship == null) {
            MESSAGES.nullArgument("Relationship");
        }

        try {
            getContextualStoreForFeature(createContext(), FeatureGroup.relationship, FeatureOperation.delete,
                    relationship.getClass()).remove(relationship);
        } catch (Exception e) {
            throw MESSAGES.relationshipRemoveFailed(relationship, e);
        }
    }

    public Agent getAgent(String loginName) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.agent, FeatureOperation.read).getAgent(loginName);
    }

    @Override
    public User getUser(String loginName) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.user, FeatureOperation.read).getUser(loginName);
    }

    @Override
    public Group getGroup(String path) {
        if (StringUtil.isNullOrEmpty(path)) {
            return null;
        }

        return getContextualStoreForFeature(createContext(), FeatureGroup.group, FeatureOperation.read).getGroup(path);
    }

    @Override
    public Group getGroup(String name, Group parent) {
        if (StringUtil.isNullOrEmpty(name) || parent == null) {
            return null;
        }

        IdentityStoreInvocationContext ctx = createContext();
        
        if (lookupIdentityById(Group.class, parent.getId()) == null) {
            throw MESSAGES.groupParentNotFoundWithId(parent.getId(), ctx.getPartition());
        }
        
        return getContextualStoreForFeature(ctx, FeatureGroup.group, FeatureOperation.read).getGroup(name, parent);
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        if (identityType == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (group == null) {
            MESSAGES.nullArgument("Group");
        }

        boolean isMember = false;

        if (Agent.class.isInstance(identityType)) {
            isMember = getGroupMembership(identityType, group) != null;
        } else if (Group.class.isInstance(identityType)) {
            Group memberGroup = (Group) identityType;

            if (memberGroup.getId() != null) {
                memberGroup = lookupIdentityById(Group.class, memberGroup.getId());

                if (memberGroup != null) {
                    isMember = memberGroup.getPath().contains(group.getPath());
                }
            }
        } else {
            throw MESSAGES.relationshipUnsupportedGroupMemberType(identityType);
        }

        return isMember;
    }

    @Override
    public void addToGroup(Agent member, Group group) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(member, ctx);
        checkIfIdentityTypeExists(group, ctx);

        if (getGroupMembership(member, group) == null) {
            add(new GroupMembership(member, group));
        }
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(member, ctx);
        checkIfIdentityTypeExists(group, ctx);

        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.delete, GroupMembership.class).remove(
                new GroupMembership(member, group));
    }

    @Override
    public Role getRole(String name) {
        return getContextualStoreForFeature(createContext(), FeatureGroup.role, FeatureOperation.read).getRole(name);
    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        if (assignee == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            MESSAGES.nullArgument("Role");
        }

        if (group == null) {
            MESSAGES.nullArgument("Group");
        }

        return getGroupRole(assignee, role, group) != null;
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(assignee, ctx);
        checkIfIdentityTypeExists(role, ctx);
        checkIfIdentityTypeExists(group, ctx);

        if (getGroupRole(assignee, role, group) == null) {
            add(new GroupRole(assignee, group, role));
        }
    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(assignee, ctx);
        checkIfIdentityTypeExists(role, ctx);
        checkIfIdentityTypeExists(group, ctx);

        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.delete, GroupRole.class).remove(
                new GroupRole(assignee, group, role));
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        if (identityType == null) {
            MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            MESSAGES.nullArgument("Role");
        }

        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        return getGrant(identityType, role) != null;
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        IdentityStoreInvocationContext ctx = createContext();

        checkIfIdentityTypeExists(identityType, ctx);
        checkIfIdentityTypeExists(role, ctx);

        if (getGrant(identityType, role) == null) {
            add(new Grant(identityType, role));
        }
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        IdentityStoreInvocationContext ctx = createContext();

        if (Role.class.isInstance(identityType)) {
            throw MESSAGES.relationshipUnsupportedGrantAssigneeType(identityType);
        }

        checkIfIdentityTypeExists(identityType, ctx);
        checkIfIdentityTypeExists(role, ctx);

        getContextualStoreForFeature(ctx, FeatureGroup.relationship, FeatureOperation.delete, Grant.class).remove(
                new Grant(identityType, role));
    }

    @Override
    public void validateCredentials(Credentials credentials) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.credential,
                FeatureOperation.validate);
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
        return new DefaultIdentityQuery<T>(identityType, getContextualStoreForFeature(createContext(), FeatureGroup.user,
                FeatureOperation.read));
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return new DefaultRelationshipQuery<T>(relationshipType, getContextualStoreForFeature(createContext(),
                FeatureGroup.relationship, FeatureOperation.read, relationshipType));
    }

    @Override
    public void createRealm(Realm realm) {
        if (realm == null) {
            throw MESSAGES.nullArgument("Realm");
        }

        if (realm.getName() == null) {
            throw MESSAGES.nullArgument("Realm name");
        }

        if (Realm.class.isInstance(realm)) {
            if (getRealm(realm.getName()) != null) {
                throw MESSAGES.partitionAlreadyExistsWithName(realm.getClass(), realm.getName());
            }
        }

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.realm, FeatureOperation.create);

        if (store != null) {
            ((PartitionStore) store).createPartition(realm);
        }
    }

    @Override
    public <T extends IdentityType> T lookupIdentityById(Class<T> identityType, String id) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType class");
        }

        if (id == null) {
            throw MESSAGES.nullArgument("Identifier");
        }

        IdentityQuery<T> query = createIdentityQuery(identityType);

        query.setParameter(IdentityType.ID, id);

        List<T> result = query.getResultList();

        T identity = null;

        if (!result.isEmpty()) {
            if (result.size() > 1) {
                throw MESSAGES.identityTypeAmbiguosFoundWithId(id);
            } else {
                identity = result.get(0);
            }
        }

        return identity;
    }

    @Override
    public void removeRealm(Realm realm) {
        if (realm == null) {
            throw MESSAGES.nullArgument("Realm");
        }

        Realm storedRealm = getRealm(realm.getName());

        if (storedRealm == null) {
            throw MESSAGES.partitionNotFoundWithName(realm.getClass(), realm.getName());
        }

        IdentityQuery<IdentityType> query = createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, storedRealm);

        if (!query.getResultList().isEmpty()) {
            throw MESSAGES.partitionCouldNotRemoveWithIdentityTypes(storedRealm);
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
        if (tier == null) {
            throw MESSAGES.nullArgument("Tier");
        }

        if (tier.getName() == null) {
            throw MESSAGES.nullArgument("Tier name");
        }
        
        if (Tier.class.isInstance(tier)) {
            if (getTier(tier.getName()) != null) {
                throw MESSAGES.partitionAlreadyExistsWithName(tier.getClass(), tier.getName());
            }
        }

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.create);

        if (store != null) {
            ((PartitionStore) store).createPartition(tier);
        }
    }

    @Override
    public void removeTier(Tier tier) {
        if (tier == null) {
            throw MESSAGES.nullArgument("Tier");
        }

        Tier storedTier = getTier(tier.getName());

        if (storedTier == null) {
            throw MESSAGES.partitionNotFoundWithName(tier.getClass(), tier.getName());
        }

        IdentityQuery<IdentityType> query = createIdentityQuery(IdentityType.class);

        query.setParameter(IdentityType.PARTITION, storedTier);

        if (!query.getResultList().isEmpty()) {
            throw MESSAGES.partitionCouldNotRemoveWithIdentityTypes(storedTier);
        }

        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.delete);

        if (store != null) {
            ((PartitionStore) store).removePartition(tier);
        }
    }

    @Override
    public Tier getTier(String name) {
        IdentityStore<?> store = getContextualStoreForFeature(createContext(), FeatureGroup.tier, FeatureOperation.read);

        return store != null ? ((PartitionStore) store).getTier(name) : null;
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

    private IdentityStore<?> getContextualStoreForFeature(IdentityStoreInvocationContext ctx, FeatureGroup feature,
            FeatureOperation operation) {
        return getContextualStoreForFeature(ctx, feature, operation, null);
    }

    private IdentityStore<?> getContextualStoreForFeature(final IdentityStoreInvocationContext ctx, FeatureGroup feature,
            FeatureOperation operation, Class<? extends Relationship> relationshipClass) {
        String realmName = (ctx.getRealm() != null) ? ctx.getRealm().getName() : Realm.DEFAULT_REALM;

        if (!realmStores.containsKey(realmName)) {
            throw MESSAGES.storeConfigRealmNotConfigured(realmName);
        }

        Set<IdentityStoreConfiguration> configs = realmStores.get(realmName);

        IdentityStoreConfiguration config = null;
        boolean supportedRelationshipClass = true;

        for (IdentityStoreConfiguration cfg : configs) {
            if (relationshipClass != null) {
                if (cfg.getFeatureSet().supportsRelationship(relationshipClass)) {
                    if (cfg.getFeatureSet().supportsRelationshipFeature(relationshipClass, operation)) {
                        config = cfg;
                        break;
                    }
                } else {
                    supportedRelationshipClass = false;
                }
            } else if (cfg.getFeatureSet().supports(feature, operation)) {
                config = cfg;
                break;
            }
        }

        if (config == null) {
            if (!supportedRelationshipClass) {
                throw MESSAGES.storeConfigUnsupportedRelationshipType(relationshipClass);
            } else {
                throw MESSAGES.storeConfigUnsupportedOperation(feature, operation, feature, operation);
            }
        }

        @SuppressWarnings("unchecked")
        final IdentityStore<IdentityStoreConfiguration> store = storeFactory.createIdentityStore(config, ctx);

        this.contextFactory.initContextForStore(ctx, store);

        store.setup(config, ctx);

        return store;
    }

    private IdentityStoreInvocationContext createContext() {
        IdentityStoreInvocationContext context = this.contextFactory.createContext(this);

        context.setRealm(currentRealm.get());
        context.setTier(currentTier.get());

        return context;
    }

    private void checkIfIdentityTypeExists(IdentityType identityType, IdentityStoreInvocationContext ctx) {
        if (identityType == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (lookupIdentityById(identityType.getClass(), identityType.getId()) == null) {
            throw MESSAGES.attributedTypeNotFoundWithId(identityType.getClass(), identityType.getId(), ctx.getPartition());
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