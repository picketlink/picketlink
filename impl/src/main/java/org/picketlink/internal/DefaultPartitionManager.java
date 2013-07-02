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
package org.picketlink.internal;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.TypeOperation;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.internal.ContextualIdentityManager;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.RelationshipPolicy;
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
    private final Map<String,IdentityConfiguration> configurations;

    /**
     *
     */
    private final Map<Partition,IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition,IdentityConfiguration>();

    /**
     * 
     */
    private final RelationshipPolicy relationshipPolicy;

    /**
     *
     */
    private final IdentityConfiguration partitionManagementConfig;

    public DefaultPartitionManager(Map<String,IdentityConfiguration> configurations, RelationshipPolicy relationshipPolicy) {
        this(configurations, relationshipPolicy, null, null);
    }

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     */
    public DefaultPartitionManager(Map<String,IdentityConfiguration> configurations, RelationshipPolicy relationshipPolicy, 
            EventBridge eventBridge, IdGenerator idGenerator) {
        this(configurations, eventBridge, idGenerator, null);
    }

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     * @param partitionManagementConfigName
     */
    public DefaultPartitionManager(Map<String,IdentityConfiguration> configurations, RelationshipPolicy relationshipPolicy, 
            EventBridge eventBridge, IdGenerator idGenerator, String partitionManagementConfigName) {
        LOGGER.identityManagerBootstrapping();

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

        this.configurations = Collections.unmodifiableMap(configurations);
        this.relationshipPolicy = relationshipPolicy;

        if (!StringUtil.isNullOrEmpty(partitionManagementConfigName)) {
            this.partitionManagementConfig = configurations.get(partitionManagementConfigName);
        } else if (configurations.size() == 1) {
            this.partitionManagementConfig = configurations.get(configurations.keySet().iterator().next());
        } else {
            throw new IllegalArgumentException("The partitionManagementConfigName parameter must be specified " +
                    "when more than one configuration has been provided");
        }

        // TODO we're going to create all the identity stores here at initialization time.
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

            @SuppressWarnings("rawtypes")
            PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                    createIdentityContext(), Partition.class, TypeOperation.read);

            partitionConfigurations.put(partition, configurations.get(store.getConfigurationName(partition)));
        }
        return partitionConfigurations.get(partition);
    }

    private IdentityContext createIdentityContext() {
        // TODO implement this
        return null;
    }

    /**
     * <p>
     * Creates a {@link IdentityManager} instance using the default realm (<code>Realm.DEFAULT_REALM</code>).
     * </p>
     *
     * @return
     * @throws SecurityConfigurationException if the default realm was not configured.
     */
    public IdentityManager createIdentityManager() throws SecurityConfigurationException{
        Realm defaultRealm = getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            throw MESSAGES.configurationDefaultRealmNotDefined();
        }

        return createIdentityManager(defaultRealm);
    }

    /**
     * <p>
     * Creates a {@link IdentityManager} instance for the given {@link Partition}.
     * </p>
     *
     * @param partition
     * @return
     * @throws SecurityConfigurationException if the default realm was not configured.
     * @throws IdentityManagementException if provided a null partition or some error occurs during the creation..
     */
    public IdentityManager createIdentityManager(Partition partition) throws SecurityConfigurationException, IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        try {
            return new ContextualIdentityManager(eventBridge, idGenerator, partition, this);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateContextualIdentityManager(partition);
        }
    }

    /**
     * <p>Returns the {@link Partition} with the given name.</p>
     *
     * @param name
     * @return
     */
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.read);
        return store.<T>getPartition(partitionClass, name);
    }

    /**
     * Persists the specified partition, using the provided configuration name
     *
     * @param partition
     * @param configurationName
     */
    public void addPartition(Partition partition, String configurationName) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.create);
        store.addPartition(partition, configurationName);
    }

    /**
     * Updates the state of the specified partition.  The partition ID cannot be changed.
     *
     * @param partition
     */
    public void updatePartition(Partition partition) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.update);
        store.updatePartition(partition);
    }

    /**
     * Removes the specified partition
     *
     * @param partition
     */
    public void removePartition(Partition partition) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.delete);
        store.removePartition(partition);
    }

    @Override
    public <T extends IdentityStoreConfiguration> IdentityStore<T> createIdentityStore(T config, IdentityContext context) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForType(Class<T> storeType, IdentityContext context,
            Class<? extends AttributedType> type, TypeOperation operation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityStore<?> getStoreForCredential(IdentityContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}