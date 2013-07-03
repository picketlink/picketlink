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

import java.util.ArrayList;
import java.util.List;
import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfiguration;
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

    private final List<IdentityConfiguration> configurations;
    private final StoreSelector storeSelector;
    private final IdentityConfiguration partitionConfiguration;

    public DefaultPartitionManager(IdentityConfiguration configuration) {
        if (configuration == null) {
            throw MESSAGES.nullArgument("At least one IdentityConfiguration must be provided");
        }

        this.configurations = new ArrayList<IdentityConfiguration>();
        this.configurations.add(configuration);

        this.partitionConfiguration = configuration;

        StoreSelector providedSelector = configuration.getStoresConfiguration().getStoreSelector();

        if (providedSelector == null) {
            providedSelector = new DefaultStoreSelector(configuration.getStoresConfiguration());
        }

        this.storeSelector = providedSelector;
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
        return new ContextualIdentityManager(createIdentityContext(), this.storeSelector);
    }

    @Override
    public void add(Partition partition) {
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

        this.storeSelector.getStoreForPartitionOperation().add(partition, identityContext);
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        return (T) this.storeSelector.getStoreForPartitionOperation().get(partitionClass, name, createIdentityContext());
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

}