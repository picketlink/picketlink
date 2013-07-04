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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.file.internal.FileIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultRelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Provides partition management functionality, and partition-specific {@link IdentityManager} instances.
 *
 * Before using this factory you need a valid {@link IdentityConfiguration}, usually created using the
 * {@link org.picketlink.idm.config.IdentityConfigurationBuilder}.
 * </p>
 *
 * @author Shane Bryzak
 */
public class DefaultPartitionManager implements PartitionManager, StoreSelector {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";

    /**
     * The event bridge allows events to be "bridged" to an event bus, such as the CDI event bus
     */
    private final EventBridge eventBridge;

    /**
     * The ID generator is responsible for generating unique identifier values
     */
    private final IdGenerator idGenerator;

    /**
     * A collection of all identity configurations.  Each configuration has a unique name.
     */
    private final Collection<IdentityConfiguration> configurations;

    /**
     * Each partition is governed by a specific IdentityConfiguration, indicated by this Map.  Every IdentityConfiguration instance
     * will also be found in the configurations property above.
     */
    private final Map<Partition,IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition,IdentityConfiguration>();

    /**
     * The store instances for each IdentityConfiguration, mapped by their corresponding IdentityStoreConfiguration
     */
    private final Map<IdentityConfiguration,Map<IdentityStoreConfiguration,IdentityStore<?>>> stores;

    /**
     * The IdentityConfiguration that is responsible for managing partition CRUD operations.  It is possible for this
     * value to be null, in which case partition management will not be supported.
     */
    private final IdentityConfiguration partitionManagementConfig;

    /**
     * This Map stores the set of identity properties for each relationship type, so that they are not required to be
     * queried for every single relationship operation.  The properties are populated at runtime.
     */
    private Map<Class<? extends Relationship>, Set<Property<? extends IdentityType>>> relationshipIdentityProperties =
            new ConcurrentHashMap<Class<? extends Relationship>, Set<Property<? extends IdentityType>>>();


    public DefaultPartitionManager(IdentityConfiguration configuration) {
        this(Arrays.asList(configuration), null, null);
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations) {
        this(configurations, null, null);
    }

    /**
     *
     * @param configurations
     * @param eventBridge
     * @param idGenerator
     */
    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations, EventBridge eventBridge,
                                   IdGenerator idGenerator) {
        LOGGER.identityManagerBootstrapping();

        if (configurations == null || configurations.isEmpty()) {
            throw new IllegalArgumentException("At least one IdentityConfiguration must be provided");
        }

        this.configurations = Collections.unmodifiableCollection(configurations);

        if (eventBridge != null) {
            this.eventBridge = eventBridge;
        } else {
            this.eventBridge = new EventBridge() { public void raiseEvent(Object event) { /* no-op */}};
        }

        if (idGenerator != null) {
            this.idGenerator = idGenerator;
        } else {
            this.idGenerator = new DefaultIdGenerator();
        }

        IdentityConfiguration partitionCfg = null;
        search:
        for (IdentityConfiguration config : configurations) {
            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                if (storeConfig.supportsType(Partition.class, IdentityOperation.create)) {
                    partitionCfg = config;
                    break search;
                }
            }
        }
        // There may be no configuration that supports partition management, in which case the partitionManagementConfig
        // field will be null and partition management operations will not be supported
        this.partitionManagementConfig = partitionCfg;

        Map<IdentityConfiguration,Map<IdentityStoreConfiguration,IdentityStore<?>>> configuredStores =
                new HashMap<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>>();

        for (IdentityConfiguration config : configurations) {
            Map<IdentityStoreConfiguration,IdentityStore<?>> storeMap = new HashMap<IdentityStoreConfiguration,IdentityStore<?>>();

            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                @SuppressWarnings("rawtypes")
                Class<? extends IdentityStore> storeClass = storeConfig.getIdentityStoreType();
                storeMap.put(storeConfig, createIdentityStore(storeClass, storeConfig));
            }

            configuredStores.put(config, Collections.unmodifiableMap(storeMap));
        }

        stores = Collections.unmodifiableMap(configuredStores);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends IdentityStore> T createIdentityStore(Class<T> storeClass, IdentityStoreConfiguration storeConfiguration) {
        T store = null;

        try {
            if (storeClass == null) {
                // If no store class is configured, default to the built-in types for known configurations
                if (FileIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                    storeClass = (Class<T>) FileIdentityStore.class;
                } else if (JPAIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                    storeClass = (Class<T>) JPAIdentityStore.class;
                } else if (LDAPIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                    storeClass = (Class<T>) LDAPIdentityStore.class;
                } else {
                    throw new IdentityManagementException("Unknown IdentityStore class for configuration [" + storeConfiguration + "].");
                }
            }

            store = storeClass.newInstance();
        } catch (Exception ex) {
            throw new IdentityManagementException("Error while creating IdentityStore instance for configuration [" +
                    storeConfiguration + "].", ex);
        }

        store.setup(storeConfiguration);

        return store;
    }

    private IdentityConfiguration getConfigurationByName(String name) {
        for (IdentityConfiguration config : configurations) {
            if (name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    private IdentityConfiguration getConfigurationForPartition(Partition partition) {
        if (partitionConfigurations.containsKey(partition)) {
            return partitionConfigurations.get(partition);
        } else {
            return lookupPartitionConfiguration(partition);
        }
    }

    private IdentityConfiguration lookupPartitionConfiguration(Partition partition) {
        if (!partitionConfigurations.containsKey(partition)) {

            IdentityContext context = createIdentityContext();
            PartitionStore<?> store = getStoreForPartitionOperation(context);
            partitionConfigurations.put(partition, getConfigurationByName(store.getConfigurationName(context, partition)));
        }

        return partitionConfigurations.get(partition);
    }

    @Override
    public IdentityContext createIdentityContext() {
        return new IdentityContext() {
            private Map<String,Object> params = new HashMap<String,Object>();
            @Override
            public Object getParameter(String paramName) {
                return params.get(paramName);
            }

            @Override
            public boolean isParameterSet(String paramName) {
                return params.containsKey(paramName);
            }

            @Override
            public void setParameter(String paramName, Object value) {
                params.put(paramName, value);
            }

            @Override
            public EventBridge getEventBridge() {
                return eventBridge;
            }

            @Override
            public IdGenerator getIdGenerator() {
                return idGenerator;
            }

            @Override
            public Partition getPartition() {
                return null;
            }
        };
    }

    @Override
    public IdentityManager createIdentityManager() throws SecurityConfigurationException {
        Realm defaultRealm = getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            throw MESSAGES.configurationDefaultRealmNotDefined();
        }

        return createIdentityManager(defaultRealm);
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) throws SecurityConfigurationException, IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        try {
            return new ContextualIdentityManager(partition, this, eventBridge, idGenerator);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateContextualIdentityManager(partition);
        }
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        checkPartitionManagementSupported();

        try {
            IdentityContext context = createIdentityContext();
            return getStoreForPartitionOperation(context).<T>get(context, partitionClass, name);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not load partition for type [" + partitionClass.getName() + "] and name [" + name + "].");
        }
    }

    public void add(Partition partition) {
        add(partition, null);
    }

    @Override
    public void add(Partition partition, String configurationName) {
        checkPartitionManagementSupported();

        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (isNullOrEmpty(configurationName)) {
            configurationName = getDefaultConfigurationName();
        }

        if (getPartition(partition.getClass(), partition.getName()) != null) {
            throw IDMMessages.MESSAGES.partitionAlreadyExistsWithName(partition.getClass(), partition.getName());
        }

        try {
            IdentityContext context = createIdentityContext();

            getStoreForPartitionOperation(context).add(context, partition, configurationName);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not add partition [" + partition + "] using configuration [" + configurationName + ".", e);
        }
    }

    @Override
    public void update(Partition partition) {
        checkPartitionManagementSupported();

        try {
            IdentityContext context = createIdentityContext();
            getStoreForPartitionOperation(context).update(context, partition);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not update partition [" + partition + "].", e);
        }
    }

    @Override
    public void remove(Partition partition) {
        checkPartitionManagementSupported();

        try {
            IdentityContext context = createIdentityContext();
            getStoreForPartitionOperation(context).remove(context, partition);
        } catch (Exception e) {
            throw new IdentityManagementException("Could not remove partition [" + partition + "].", e);
        }
    }

    @Override
    public void add(IdentityContext context, Relationship relationship) throws IdentityManagementException {
        getStoreForRelationshipOperation(context, relationship.getClass(),
                getRelationshipPartitions(relationship)).add(context, relationship);
    }

    @Override
    public void update(IdentityContext context, Relationship relationship) {
        getStoreForRelationshipOperation(context, relationship.getClass(),
                getRelationshipPartitions(relationship)).update(context, relationship);
    }

    @Override
    public void remove(IdentityContext context, Relationship relationship) {
        getStoreForRelationshipOperation(context, relationship.getClass(),
                getRelationshipPartitions(relationship)).remove(context, relationship);
    }

    private Set<Partition> getRelationshipPartitions(Relationship relationship) {
        Set<Partition> partitions = new HashSet<Partition>();
        for (Property<? extends IdentityType> prop : getRelationshipIdentityProperties(relationship.getClass())) {
            IdentityType identity = prop.getValue(relationship);
            if (!partitions.contains(identity.getPartition())) {
                partitions.add(identity.getPartition());
            }
        }
        return partitions;
    }

    private Set<Property<? extends IdentityType>> getRelationshipIdentityProperties(
            Class<? extends Relationship> relationshipClass) {

        if (!relationshipIdentityProperties.containsKey(relationshipClass)) {
            ((ConcurrentHashMap<Class<? extends Relationship>, Set<Property<? extends IdentityType>>>)
                    relationshipIdentityProperties).putIfAbsent(relationshipClass,
                    queryRelationshipIdentityProperties(relationshipClass));
        }

        return relationshipIdentityProperties.get(relationshipClass);
    }

    private Set<Property<? extends IdentityType>> queryRelationshipIdentityProperties(Class<? extends Relationship> relationshipClass) {
        PropertyQuery<? extends IdentityType> query = PropertyQueries.createQuery(relationshipClass);
        query.addCriteria(new TypedPropertyCriteria(IdentityType.class));

        Set<Property<? extends IdentityType>> properties = new HashSet<Property<? extends IdentityType>>();
        for (Property<? extends IdentityType> prop : query.getResultList()) {
            properties.add(prop);
        }

        return Collections.unmodifiableSet(properties);
    }

    @Override
    public boolean isMember(IdentityContext context, IdentityType identity, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery(context, GroupMembership.class);
        query.setParameter(GroupMembership.MEMBER, identity);
        query.setParameter(GroupMembership.GROUP, group);
        return query.getResultCount() > 0;
    }

    @Override
    public void addToGroup(IdentityContext context, Account member, Group group) {
        add(context, new GroupMembership(member, group));
    }

    @Override
    public void removeFromGroup(IdentityContext context, Account member, Group group) {
        RelationshipQuery<GroupMembership> query = createRelationshipQuery(context, GroupMembership.class);
        query.setParameter(GroupMembership.MEMBER, member);
        query.setParameter(GroupMembership.GROUP, group);
        for (GroupMembership membership : query.getResultList()) {
            remove(context, membership);
        }
    }

    @Override
    public boolean hasGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(context, GroupRole.class);
        query.setParameter(GroupRole.ASSIGNEE, assignee);
        query.setParameter(GroupRole.GROUP, group);
        query.setParameter(GroupRole.ROLE, role);
        return query.getResultCount() > 0;
    }

    @Override
    public void grantGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group) {
        add(context, new GroupRole(assignee, group, role));
    }

    @Override
    public void revokeGroupRole(IdentityContext context, IdentityType assignee, Role role, Group group) {
        RelationshipQuery<GroupRole> query = createRelationshipQuery(context, GroupRole.class);
        query.setParameter(GroupRole.ASSIGNEE, assignee);
        query.setParameter(GroupRole.GROUP, group);
        query.setParameter(GroupRole.ROLE, role);
        for (GroupRole groupRole : query.getResultList()) {
            remove(context, groupRole);
        }
    }

    @Override
    public boolean hasRole(IdentityContext context, IdentityType assignee, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(context, Grant.class);
        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(GroupRole.ROLE, role);
        return query.getResultCount() > 0;
    }

    @Override
    public void grantRole(IdentityContext context, IdentityType assignee, Role role) {
        add(context, new Grant(assignee, role));
    }

    @Override
    public void revokeRole(IdentityContext context, IdentityType assignee, Role role) {
        RelationshipQuery<Grant> query = createRelationshipQuery(context, Grant.class);
        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(GroupRole.ROLE, role);
        for (Grant grant : query.getResultList()) {
            remove(context, grant);
        }
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(IdentityContext context, Class<T> relationshipClass) {
        return new DefaultRelationshipQuery<T>(context, relationshipClass, this);
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(IdentityContext context, Class<T> storeType,
                                                                       Class<? extends AttributedType> type, IdentityOperation operation) {
        for (IdentityStoreConfiguration storeConfig : getConfigurationForPartition(context.getPartition())
                .getStoreConfiguration()) {
            if (storeConfig.supportsType(type, operation)) {
                @SuppressWarnings("unchecked")
                T store = (T) stores.get(storeConfig);
                storeConfig.initializeContext(context, store);
                return store;
            }
        }

        throw new IdentityManagementException("No IdentityStore found for required type [" + type + "]");
    }

    @Override
    public IdentityStore<?> getStoreForCredentialOperation(IdentityContext context, Class<?> credentialClass) {
        IdentityConfiguration config = getConfigurationForPartition(context.getPartition());
        for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
            for (@SuppressWarnings("rawtypes") Class<? extends CredentialHandler> handlerClass : storeConfig.getCredentialHandlers()) {
                if (handlerClass.isAnnotationPresent(SupportsCredentials.class)) {
                    for (Class<?> cls : handlerClass.getAnnotation(SupportsCredentials.class).value()) {
                        if (cls.equals(credentialClass)) {
                            IdentityStore<?> store = stores.get(config).get(storeConfig);
                            storeConfig.initializeContext(context, store);
                            return store;
                        }
                    }
                }
            }
        }

        throw new IdentityManagementException("No IdentityStore found for credential class [" + credentialClass + "]");
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                             Set<Partition> partitions) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PartitionStore<?> getStoreForPartitionOperation(IdentityContext context) {
        Map<IdentityStoreConfiguration,IdentityStore<?>> configStores = stores.get(partitionManagementConfig);
        for (IdentityStoreConfiguration cfg : configStores.keySet()) {
            if (cfg.supportsType(Partition.class, IdentityOperation.create)) {
                PartitionStore<?> store = (PartitionStore<?>) configStores.get(cfg);
                cfg.initializeContext(context, store);
                return store;
            }
        }
        throw new IdentityManagementException("Could not locate PartitionStore");
    }

    private String getDefaultConfigurationName() {
        // If there is a configuration with the default configuration name, return that name
        for (IdentityConfiguration config : configurations) {
            if (DEFAULT_CONFIGURATION_NAME.equals(config.getName())) {
                return DEFAULT_CONFIGURATION_NAME;
            }
        }

        // Otherwise return the first configuration found
        return configurations.iterator().next().getName();
    }

    private void checkPartitionManagementSupported() {
        if (partitionManagementConfig == null) {
            throw new OperationNotSupportedException(
                    "Partition management is not supported by the current configuration", Partition.class, IdentityOperation.create);
        }
    }

}