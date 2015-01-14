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

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.event.PartitionCreatedEvent;
import org.picketlink.idm.event.PartitionDeletedEvent;
import org.picketlink.idm.event.PartitionUpdatedEvent;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.permission.acl.spi.PermissionHandler;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;

/**
 * <p>Provides partition management functionality, and partition-specific {@link IdentityManager} instances.</p>
 *
 * <p>Before using this factory you need a valid {@link IdentityConfiguration}, usually created using the
 * {@link org.picketlink.idm.config.IdentityConfigurationBuilder}.</p>
 *
 * <p>This class is thread safe, and is intended to be used as an application-scoped component.</p>
 *
 * @author Shane Bryzak
 */
public class DefaultPartitionManager extends AbstractAttributedTypeManager<Partition> implements PartitionManager {

    private static final long serialVersionUID = 1L;

    private static final Realm DEFAULT_REALM = new Realm(Realm.DEFAULT_REALM);

    public DefaultPartitionManager(IdentityConfiguration configuration) {
        this(Arrays.asList(configuration));
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations) {
        this(configurations, null, null);
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations, EventBridge eventBridge,
            Collection<PermissionHandler> permissionHandlers) {
        this(configurations, eventBridge, permissionHandlers, null);
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations, EventBridge eventBridge,
                                   Collection<PermissionHandler> permissionHandlers, IdGenerator idGenerator) {
        super(new PartitionManagerConfiguration(configurations, permissionHandlers, eventBridge, idGenerator));
    }

    @Override
    public IdentityManager createIdentityManager() throws IdentityManagementException {
        return createIdentityManager(DEFAULT_REALM);
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) throws IdentityManagementException {
        if (partition == null) {
            if (!getConfiguration().supportsPartition()) {
                return createIdentityManager();
            }

            throw MESSAGES.nullArgument("Partition");
        }

        Partition storedPartition = getStoredPartition(partition);

        try {
            return new ContextualIdentityManager(storedPartition, this);
        } catch (Exception e) {
            throw MESSAGES.partitionCouldNotCreateIdentityManager(storedPartition, e);
        }
    }

    @Override
    public PermissionManager createPermissionManager() {
        return createPermissionManager(DEFAULT_REALM);
    }

    @Override
    public PermissionManager createPermissionManager(Partition partition) throws IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        Partition storedPartition = getStoredPartition(partition);

        try {
            return new ContextualPermissionManager(storedPartition, this);
        } catch (Exception ex) {
            throw MESSAGES.partitionCouldNotCreatePermissionManager(storedPartition);
        }
    }

    @Override
    public RelationshipManager createRelationshipManager() {
        return new ContextualRelationshipManager(this);
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        if (partitionClass == null) {
            throw MESSAGES.nullArgument("Partition class");
        }

        if (isNullOrEmpty(name)) {
            throw MESSAGES.nullArgument("Partition name");
        }

        if (!getConfiguration().supportsPartition()) {
            return (T) createDefaultPartition();
        }

        try {
            IdentityContext identityContext = getIdentityContext();
            T partition = getStoreSelector().getStoreForPartitionOperation(identityContext, partitionClass).<T>get(identityContext, partitionClass, name);

            if (partition != null) {
                loadAttributes(identityContext, (T) partition);
            }

            return partition;
        } catch (Exception e) {
            throw MESSAGES.partitionGetFailed(partitionClass, name, e);
        }
    }

    @Override
    public <T extends Partition> List<T> getPartitions(Class<T> partitionClass) {
        if (partitionClass == null) {
            throw MESSAGES.nullArgument("Partition class");
        }

        List<T> partitions = new ArrayList<T>();

        if (!getConfiguration().supportsPartition()) {
            partitions.add((T) createDefaultPartition());
        } else {
            try {
                IdentityContext identityContext = getIdentityContext();

                partitions.addAll(getStoreSelector().getStoreForPartitionOperation(identityContext, partitionClass).<T>get(identityContext, partitionClass));

                for (T partition : partitions) {
                    loadAttributes(identityContext, partition);
                }
            } catch (Exception e) {
                throw MESSAGES.partitionGetFailed(partitionClass, "not specified", e);
            }
        }

        return partitions;
    }

    @Override
    public <T extends Partition> T lookupById(final Class<T> partitionClass, final String id) {
        if (partitionClass == null) {
            throw MESSAGES.nullArgument("Partition class");
        }

        if (isNullOrEmpty(id)) {
            throw MESSAGES.nullArgument("Partition identifier");
        }

        if (!getConfiguration().supportsPartition()) {
            return (T) createDefaultPartition();
        }

        try {
            IdentityContext identityContext = getIdentityContext();
            T partition = getStoreSelector().getStoreForPartitionOperation(identityContext, partitionClass).<T>lookupById(identityContext, partitionClass, id);

            if (partition != null) {
                loadAttributes(identityContext, (T) partition);
            }

            return partition;
        } catch (Exception e) {
            throw MESSAGES.partitionGetFailed(partitionClass, id, e);
        }
    }

    @Override
    protected void fireAttributedTypeAddedEvent(Partition attributedType) {

    }

    @Override
    protected void doAdd(Partition attributedType) {

    }

    @Override
    public void add(Partition attributedType) throws IdentityManagementException {
        add(attributedType, null);
    }

    @Override
    public void add(Partition partition, String configurationName) throws IdentityManagementException {
        checkPartitionManagementSupported();

        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (isNullOrEmpty(configurationName)) {
            configurationName = getConfiguration().getDefaultConfigurationName();
        }

        if (getConfiguration().getConfigurationByName(configurationName) != null) {
            checkUniqueness(partition);

            try {
                IdentityContext identityContext = getIdentityContext();

                getStoreSelector().getStoreForPartitionOperation(identityContext, partition.getClass()).add(identityContext, partition, configurationName);

                addAttributes(identityContext, partition);

                fireEvent(new PartitionCreatedEvent(partition, this));
            } catch (Exception e) {
                throw MESSAGES.partitionAddFailed(partition, configurationName, e);
            }
        }
    }

    @Override
    protected void fireAttributedTypeUpdatedEvent(Partition attributedType) {
        fireEvent(new PartitionUpdatedEvent(attributedType, this));
    }

    @Override
    protected void doUpdate(Partition attributedType) {
        checkPartitionManagementSupported();

        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForPartitionOperation(identityContext, attributedType.getClass()).update(identityContext, attributedType);
    }

    @Override
    protected void fireAttributedTypeRemovedEvent(Partition attributedType) {
        fireEvent(new PartitionDeletedEvent(attributedType, this));
    }

    @Override
    protected void doRemove(Partition attributedType) {
        checkPartitionManagementSupported();
        IdentityManager identityManager = createIdentityManager(attributedType);
        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        IdentityQuery<IdentityType> query = queryBuilder.createIdentityQuery(IdentityType.class);

        for (IdentityType identityType : query.getResultList()) {
            identityManager.remove(identityType);
        }

        IdentityContext identityContext = getIdentityContext();

        getStoreSelector().getStoreForPartitionOperation(identityContext, attributedType.getClass()).remove(identityContext, attributedType);
    }

    @Override
    protected void checkUniqueness(Partition attributedType) throws IdentityManagementException {
        Partition storedPartition = getPartition(attributedType.getClass(), attributedType.getName());

        if (storedPartition != null) {
            throw MESSAGES.partitionAlreadyExistsWithName(attributedType.getClass(), storedPartition.getName());
        }
    }

    @Override
    protected void checkIfExists(Partition partition) throws IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (lookupById(partition.getClass(), partition.getId()) == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }
    }

    @Override
    public Collection<IdentityConfiguration> getConfigurations() {
        return getConfiguration().getConfigurations();
    }

    private void checkPartitionManagementSupported() throws OperationNotSupportedException {
        if (!getConfiguration().supportsPartition()) {
            throw MESSAGES.partitionManagementNoSupported(Partition.class, IdentityOperation.create);
        }
    }

    private Partition createDefaultPartition() {
        Partition storedPartition = new Realm(Realm.DEFAULT_REALM);

        storedPartition.setId(Realm.DEFAULT_REALM);

        return storedPartition;
    }

    private Partition getStoredPartition(final Partition partition) {
        Partition storedPartition;

        if (getConfiguration().supportsPartition()) {
            storedPartition = getPartition(partition.getClass(), partition.getName());
        } else {
            storedPartition = createDefaultPartition();
        }

        if (storedPartition == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }

        return storedPartition;
    }

    private <T extends Partition> void loadAttributes(final IdentityContext context, final T partition) {
        AttributeStore<?> attributeStore = getStoreSelector().getStoreForAttributeOperation(context);

        if (attributeStore != null) {
            attributeStore.loadAttributes(context, partition);
        }
    }

    @Override
    public IdentityContext getIdentityContext() {
        return createIdentityContext(null, getConfiguration().getEventBridge(), getConfiguration().getIdGenerator());
    }
}