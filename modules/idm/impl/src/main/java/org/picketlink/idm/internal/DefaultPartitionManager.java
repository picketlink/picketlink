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
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
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
        this(configurations, relationshipPolicy, eventBridge, idGenerator, null);
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

        if (configurations == null || configurations.isEmpty()) {
            throw new IllegalArgumentException("At least one IdentityConfiguration must be provided");
        }

        this.configurations = Collections.unmodifiableMap(configurations);

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

    /**
     * Create a default relationship policy, using the first found store in the first found configuration
     *
     * @param configurations
     * @return
     */
    private RelationshipPolicy createDefaultRelationshipPolicy(Map<String,IdentityConfiguration> configurations) {
        Map<Class<? extends Relationship>, IdentityStoreConfiguration> relationshipConfig = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();

        // Get the first configuration
        IdentityConfiguration config = configurations.get(configurations.keySet().iterator().next());
        // Get the first store in that configuration
        IdentityStoreConfiguration storeConfig = config.getConfiguredStores().iterator().next();

        relationshipConfig.put(Relationship.class, storeConfig);
        return new RelationshipPolicy(relationshipConfig, relationshipConfig);
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
        return getStoreForPartitionOperation().<T>getPartition(partitionClass, name);
    }


    /**
     * 
     * @param partition
     */
    public void addPartition(Partition partition) {
        addPartition(partition, null);
    }

    /**
     * Persists the specified partition, using the provided configuration name
     *
     * @param partition
     * @param configurationName
     */
    public void addPartition(Partition partition, String configurationName) {
        if (configurationName == null) {
            getStoreForPartitionOperation().addPartition(partition, getDefaultConfigurationName());
        } else {
            getStoreForPartitionOperation().addPartition(partition, configurationName);
        }
    }

    private String getDefaultConfigurationName() {
        // TODO implement this
        return null;
    }

    /**
     * Updates the state of the specified partition.  The partition ID cannot be changed.
     *
     * @param partition
     */
    public void updatePartition(Partition partition) {
        getStoreForPartitionOperation().updatePartition(partition);
    }

    /**
     * Removes the specified partition
     *
     * @param partition
     */
    public void removePartition(Partition partition) {
        getStoreForPartitionOperation().removePartition(partition);
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