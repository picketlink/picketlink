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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.picketlink.idm.model.Partition;
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

    private static final long serialVersionUID = 666601082732493295L;

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

            PartitionStore<?> store = getStoreForPartitionOperation();
            partitionConfigurations.put(partition, getConfigurationByName(store.getConfigurationName(createIdentityContext(), partition)));
        }

        return partitionConfigurations.get(partition);
    }

    private IdentityContext createIdentityContext() {
        // TODO implement this
        return null;
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
            //FIXME
//            return new ContextualIdentityManager(eventBridge, idGenerator, partition, this);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateContextualIdentityManager(partition);
        }

        return null;
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        return getStoreForPartitionOperation().<T>get(createIdentityContext(), partitionClass, name);
    }

    public void add(Partition partition) {
        add(partition, null);
    }

    @Override
    public void add(Partition partition, String configurationName) {
        if (StringUtil.isNullOrEmpty(configurationName)) {
            getStoreForPartitionOperation().add(createIdentityContext(), partition, getDefaultConfigurationName());
        } else {
            getStoreForPartitionOperation().add(createIdentityContext(), partition, configurationName);
        }
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

    @Override
    public void update(Partition partition) {
        getStoreForPartitionOperation().update(createIdentityContext(), partition);
    }

    @Override
    public void remove(Partition partition) {
        getStoreForPartitionOperation().remove(createIdentityContext(), partition);
    }

    @Override
    public void add(Relationship relationship) throws IdentityManagementException {
        // TODO Auto-generated method stub

    }

    @Override
    public void update(Relationship relationship) {
        // TODO Auto-generated method stub

    }

    @Override
    public void remove(Relationship relationship) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addToGroup(Agent agent, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub

    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        // TODO Auto-generated method stub

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
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(Class<T> storeType, Partition partition,
                                                                       Class<? extends AttributedType> type, IdentityOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStore<?> getStoreForCredentialOperation(Class<?> credentialClass, Partition partition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(Class<? extends Relationship> relationshipClass,
                                                             Set<Partition> partitions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PartitionStore<?> getStoreForPartitionOperation() {
        // TODO Auto-generated method stub
        return null;
    }

}