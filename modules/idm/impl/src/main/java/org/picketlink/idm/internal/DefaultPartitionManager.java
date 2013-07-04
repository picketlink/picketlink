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

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

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
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.DefaultIdGenerator;
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
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.file.internal.FileIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;extends Serializable 
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;

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
     *
     */
    private final EventBridge eventBridge;

    /**
     *
     */
    private final IdGenerator idGenerator;

    /**
     *
     */
    private final Collection<IdentityConfiguration> configurations;

    /**
     *
     */
    private final Map<Partition,IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition,IdentityConfiguration>();

    /**
     * The created store instances for each identity configuration
     */
    private final Map<IdentityConfiguration,Map<IdentityStoreConfiguration,IdentityStore<?>>> stores;

    /**
     *
     */
    private final IdentityConfiguration partitionManagementConfig;

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
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
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

        if (configurations.size() == 1) {
            this.partitionManagementConfig = this.configurations.iterator().next();
        } else {
            IdentityConfiguration partitionCfg = null;
            search:
            for (IdentityConfiguration config : configurations) {
                for (IdentityStoreConfiguration storeConfig : config.getStoresConfiguration().getConfigurations()) {
                    if (storeConfig.supportsType(Partition.class, IdentityOperation.create)) {
                        partitionCfg = config;
                        break search;
                    }
                }
            }
            // There may be no configuration that supports partition management, in which case the partitionManagementConfig
            // field will be null and partition management operations will not be supported
            this.partitionManagementConfig = partitionCfg;
        }

        Map<IdentityConfiguration,Map<IdentityStoreConfiguration,IdentityStore<?>>> configuredStores =
                new HashMap<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>>();

        for (IdentityConfiguration config : configurations) {
            Map<IdentityStoreConfiguration,IdentityStore<?>> storeMap = new HashMap<IdentityStoreConfiguration,IdentityStore<?>>();

            for (IdentityStoreConfiguration storeConfig : config.getStoresConfiguration().getConfigurations()) {
                @SuppressWarnings("rawtypes")
                Class<? extends IdentityStore> storeClass = config.getStoresConfiguration().getIdentityStores().get(storeConfig);
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
                    store = (T) FileIdentityStore.class.newInstance();
                } else if (JPAIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                    store = (T) JPAIdentityStore.class.newInstance();
                } else if (LDAPIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                    store = (T) LDAPIdentityStore.class.newInstance();
                } else {
                    throw new IdentityManagementException("Unknown IdentityStore class for configuration [" + storeConfiguration + "].");
                }
                return null;
            } else {
                store = storeClass.newInstance();
            }
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

    private synchronized IdentityConfiguration lookupPartitionConfiguration(Partition partition) {
        if (!partitionConfigurations.containsKey(partition)) {

            IdentityContext context = createIdentityContext();
            PartitionStore<?> store = getStoreForPartitionOperation(context);
            partitionConfigurations.put(partition, getConfigurationByName(store.getConfigurationName(context, partition)));
        }

        return partitionConfigurations.get(partition);
    }

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
        IdentityContext context = createIdentityContext();
        return getStoreForPartitionOperation(context).<T>get(context, partitionClass, name);
    }

    public void add(Partition partition) {
        add(partition, null);
    }

    @Override
    public void add(Partition partition, String configurationName) {
        checkPartitionManagementSupported();
        IdentityContext context = createIdentityContext();
        if (StringUtil.isNullOrEmpty(configurationName)) {
            getStoreForPartitionOperation(context).add(context, partition, getDefaultConfigurationName());
        } else {
            getStoreForPartitionOperation(context).add(context, partition, configurationName);
        }
    }

    @Override
    public void update(Partition partition) {
        checkPartitionManagementSupported();
        IdentityContext context = createIdentityContext();
        getStoreForPartitionOperation(context).update(context, partition);
    }

    @Override
    public void remove(Partition partition) {
        checkPartitionManagementSupported();
        IdentityContext context = createIdentityContext();
        getStoreForPartitionOperation(context).remove(context, partition);
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
            throw new UnsupportedOperationException("Partition management is not supported by the current configuration");
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
    public boolean isMember(IdentityContext identityContext, IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addToGroup(IdentityContext identityContext, Agent agent, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFromGroup(IdentityContext identityContext, Agent member, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasGroupRole(IdentityContext identityContext, IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantGroupRole(IdentityContext identityContext, IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revokeGroupRole(IdentityContext identityContext, IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasRole(IdentityContext identityContext, IdentityType identityType, Role role) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantRole(IdentityContext identityContext, IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revokeRole(IdentityContext identityContext, IdentityType identityType, Role role) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(IdentityContext identityContext, Class<T> relationshipType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(IdentityContext identityContext, Class<T> storeType,
                                                                       Class<? extends AttributedType> type, IdentityOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStore<?> getStoreForCredentialOperation(IdentityContext identityContext, Class<?> credentialClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(IdentityContext identityContext, Class<? extends Relationship> relationshipClass,
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

}