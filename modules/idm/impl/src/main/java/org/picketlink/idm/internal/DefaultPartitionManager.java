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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoresConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Provides partition management functionality, and partition-specific {@link IdentityManager} instances.
 * <p/>
 * Before using this factory you need a valid {@link IdentityConfiguration}, usually created using the
 * {@link org.picketlink.idm.config.IdentityConfigurationBuilder}.
 * </p>
 *
 * @author Shane Bryzak
 */
public class DefaultPartitionManager implements PartitionManager {

    private final Map<String, IdentityConfiguration> configurations;
    private IdentityConfiguration partitionConfiguration;
    private final Map<String, StoreSelector> storeSelectors;

    public DefaultPartitionManager(List<IdentityConfiguration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            throw MESSAGES.nullArgument("At least one IdentityConfiguration must be provided");
        }

        this.configurations = new ConcurrentHashMap<String, IdentityConfiguration>();

        for (IdentityConfiguration identityConfiguration: configurations) {
            this.configurations.put(identityConfiguration.getName(), identityConfiguration);
        }

        this.storeSelectors = new ConcurrentHashMap<String, StoreSelector>();

        IdentityConfiguration providedPartitionConfig = null;

        for (IdentityConfiguration identityConfiguration: configurations) {
            if (identityConfiguration.supportsPartition()) {
                if (this.partitionConfiguration == null) {
                    this.partitionConfiguration = identityConfiguration;
                } else {
                    throw new IdentityManagementException("Only one configuration may support partition management.");
                }
            }

            IdentityStoresConfiguration storesConfiguration = identityConfiguration.getStoresConfiguration();
            StoreSelector storeSelector = storesConfiguration.getStoreSelector();

            if (storeSelector == null) {
                storeSelector = new DefaultStoreSelector(storesConfiguration);
            }

            this.storeSelectors.put(identityConfiguration.getName(), storeSelector);
        }
    }

    public DefaultPartitionManager(IdentityConfiguration configuration) {
        this(Arrays.asList(configuration));
    }

    @Override
    public IdentityManager createIdentityManager() throws SecurityConfigurationException {
        Realm defaultPartition = getPartition(Realm.class, Realm.DEFAULT_REALM);

        if (defaultPartition == null) {
            throw MESSAGES.partitionNotFoundWithName(Realm.class, Realm.DEFAULT_REALM);
        }

        return createIdentityManager(defaultPartition);
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) throws SecurityConfigurationException, IdentityManagementException {
        return new ContextualIdentityManager(createIdentityContext(), getStoreSelectorForPartitionConfig(partition));
    }

    @Override
    public void add(Partition partition, String... configurationName) {
        IdentityContext identityContext = createIdentityContext();

        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (isNullOrEmpty(partition.getName())) {
            throw new IdentityManagementException("Partitions must have a name.");
        }

        if (getPartition(partition.getClass(), partition.getName()) != null) {
            throw MESSAGES.partitionAlreadyExistsWithName(partition.getClass(), partition.getName());
        }

        getStoreForPartition().add(identityContext, partition, getConfigurationName(configurationName));
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        return (T) getStoreSelectorForPartitionConfig().getStoreForPartitionOperation().get(createIdentityContext(), partitionClass, name);
    }

    @Override
    public void update(Partition partition) {
        //TODO: Implement update
    }

    @Override
    public void remove(Partition partition) {
        //TODO: Implement remove
    }

    @Override
    public void add(Relationship relationship) throws IdentityManagementException {
        //TODO: Implement add
    }

    @Override
    public void update(Relationship relationship) {
        //TODO: Implement update
    }

    @Override
    public void remove(Relationship relationship) {
        //TODO: Implement remove
    }

    @Override
    public boolean isMember(IdentityType identityType, Group group) {
        return false;  //TODO: Implement isMember
    }

    @Override
    public void addToGroup(Agent agent, Group group) {
        //TODO: Implement addToGroup
    }

    @Override
    public void removeFromGroup(Agent member, Group group) {
        //TODO: Implement removeFromGroup
    }

    @Override
    public boolean hasGroupRole(IdentityType assignee, Role role, Group group) {
        return false;  //TODO: Implement hasGroupRole
    }

    @Override
    public void grantGroupRole(IdentityType assignee, Role role, Group group) {
        //TODO: Implement grantGroupRole
    }

    @Override
    public void revokeGroupRole(IdentityType assignee, Role role, Group group) {
        //TODO: Implement revokeGroupRole
    }

    @Override
    public boolean hasRole(IdentityType identityType, Role role) {
        return false;  //TODO: Implement hasRole
    }

    @Override
    public void grantRole(IdentityType identityType, Role role) {
        //TODO: Implement grantRole
    }

    @Override
    public void revokeRole(IdentityType identityType, Role role) {
        //TODO: Implement revokeRole
    }

    @Override
    public <T extends Relationship> RelationshipQuery<T> createRelationshipQuery(Class<T> relationshipType) {
        return null;  //TODO: Implement createRelationshipQuery
    }

    private IdentityContext createIdentityContext(final Partition... partition) {
        return new IdentityContext() {
            @Override
            public Object getParameter(String paramName) {
                return null;  //TODO: Implement getParameter
            }

            @Override
            public boolean isParameterSet(String paramName) {
                return false;  //TODO: Implement isParameterSet
            }

            @Override
            public void setParameter(String paramName, Object value) {
                //TODO: Implement setParameter
            }

            @Override
            public EventBridge getEventBridge() {
                return null;  //TODO: Implement getEventBridge
            }

            @Override
            public IdGenerator getIdGenerator() {
                return new DefaultIdGenerator();  //TODO: Implement getIdGenerator
            }

            @Override
            public Partition getPartition() {
                if (partition.length == 1) {
                    return partition[0];
                }

                return null;
            }
        };
    }

    private StoreSelector getStoreSelectorForPartitionConfig(Partition... partition) {
        StoreSelector partitionStoreSelector = getStoreSelector(this.partitionConfiguration.getName());

        if (partition.length == 0) {
            return partitionStoreSelector;
        } else {
            String configurationName =
                    partitionStoreSelector.getStoreForPartitionOperation().getConfigurationName(createIdentityContext(), partition[0]);

            if (isNullOrEmpty(configurationName)) {
                throw new IdentityManagementException("No configuration found for partition [" + partition[0] + "].");
            }

            return getStoreSelector(configurationName);
        }
    }

    private StoreSelector getStoreSelector(String... configurationName) {
        if (configurationName.length == 0) {
            if (this.storeSelectors.size() > 1) {
                throw new IdentityManagementException("Multiple StoreSelector found. You must provide a configuration name.");
            } else {
                return this.storeSelectors.values().iterator().next();
            }
        }

        StoreSelector storeSelector = this.storeSelectors.get(configurationName[0]);

        if (storeSelector == null) {
            throw new SecurityConfigurationException("Could not find StoreSelector for configuration [" + configurationName[0] + "].");
        }

        return storeSelector;
    }

    private String getConfigurationName(String[] configurationName) {
        String configName = null;

        if ((configurationName == null || configurationName.length == 0) && this.configurations.size() > 1) {
            throw new IdentityManagementException("Multiple configurations found. You must specify a configuration name.");
        }

        if (configurationName.length == 0) {
            configName = this.partitionConfiguration.getName();
        } else {
            configName = configurationName[0];
        }

        return configName;
    }

    private PartitionStore<?> getStoreForPartition() {
        return getStoreSelectorForPartitionConfig().getStoreForPartitionOperation();
    }

}