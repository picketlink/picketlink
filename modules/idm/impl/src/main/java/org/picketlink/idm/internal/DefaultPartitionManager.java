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

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.util.IDMUtil.isTypeSupported;
import static org.picketlink.idm.util.IDMUtil.toSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.AbstractIdentityStoreConfiguration;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.file.internal.FileIdentityStore;
import org.picketlink.idm.internal.util.RelationshipMetadata;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.IdentityPartition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.permission.spi.PermissionStore;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;

/**
 * Provides partition management functionality, and partition-specific {@link IdentityManager} instances. <p/> Before
 * using this factory you need a valid {@link IdentityConfiguration}, usually created using the {@link
 * org.picketlink.idm.config.IdentityConfigurationBuilder}. </p>
 * <p/>
 * This class is thread safe, and is intended to be used as an application-scoped component.
 *
 * @author Shane Bryzak
 */
public class DefaultPartitionManager implements PartitionManager, StoreSelector {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_CONFIGURATION_NAME = "default";

    private static final Realm DEFAULT_REALM = new Realm(Realm.DEFAULT_REALM);

    /**
     * A collection of all identity configurations.  Each configuration has a unique name.
     */
    private final Collection<IdentityConfiguration> configurations;
    /**
     * Each partition is governed by a specific IdentityConfiguration, indicated by this Map.  Every
     * IdentityConfiguration instance will also be found in the configurations property above.
     */
    private final Map<Partition, IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition, IdentityConfiguration>();
    /**
     * The store instances for each IdentityConfiguration, mapped by their corresponding IdentityStoreConfiguration
     */
    private final Map<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>> stores;
    /**
     * The IdentityConfiguration that is responsible for managing partition CRUD operations.  It is possible for this
     * value to be null, in which case partition management will not be supported.
     */
    private final IdentityConfiguration partitionManagementConfig;
    private final IdentityConfiguration attributeManagementConfig;
    /**
     * The event bridge allows events to be "bridged" to an event bus, such as the CDI event bus
     */
    private EventBridge eventBridge;
    /**
     * The ID generator is responsible for generating unique identifier values
     */
    private IdGenerator idGenerator;
    private RelationshipMetadata relationshipMetadata = new RelationshipMetadata();

    public DefaultPartitionManager(IdentityConfiguration configuration) {
        this(Arrays.asList(configuration));
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations) {
        this(configurations, null, null);
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations, EventBridge eventBridge) {
        this(configurations, eventBridge, null);
    }

    public DefaultPartitionManager(Collection<IdentityConfiguration> configurations, EventBridge eventBridge, IdGenerator idGenerator) {
        LOGGER.identityManagerBootstrapping();

        if (configurations == null || configurations.isEmpty()) {
            throw MESSAGES.configNoIdentityConfigurationProvided();
        }

        this.configurations = Collections.unmodifiableCollection(configurations);

        if (eventBridge != null) {
            this.eventBridge = eventBridge;
        } else {
            this.eventBridge = new EventBridge() {
                public void raiseEvent(Object event) { /* no-op */}
            };
        }

        if (idGenerator != null) {
            this.idGenerator = idGenerator;
        } else {
            this.idGenerator = new DefaultIdGenerator();
        }

        IdentityConfiguration partitionCfg = null;
        IdentityConfiguration attributeCfg = null;

        for (IdentityConfiguration config : configurations) {
            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                if (storeConfig.supportsPartition()) {
                    partitionCfg = config;
                }

                if (storeConfig.supportsAttribute()) {
                    attributeCfg = config;
                }
            }
        }

        // There may be no configuration that supports partition management, in which case the partitionManagementConfig
        // field will be null and partition management operations will not be supported
        this.partitionManagementConfig = partitionCfg;
        this.attributeManagementConfig = attributeCfg;

        Map<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>> configuredStores =
                new HashMap<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>>();

        for (IdentityConfiguration config : configurations) {
            Map<IdentityStoreConfiguration, IdentityStore<?>> storeMap = new HashMap<IdentityStoreConfiguration, IdentityStore<?>>();

            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                storeMap.put(storeConfig, createIdentityStore(storeConfig));
            }

            configuredStores.put(config, Collections.unmodifiableMap(storeMap));
        }

        stores = Collections.unmodifiableMap(configuredStores);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends IdentityStore> T createIdentityStore(IdentityStoreConfiguration storeConfiguration) {
        Class<T> storeClass = (Class<T>) storeConfiguration.getIdentityStoreType();

        if (storeClass == null) {
            // If no store class is configured, default to the built-in types for known configurations
            if (FileIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                storeClass = (Class<T>) FileIdentityStore.class;
            } else if (JPAIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                storeClass = (Class<T>) JPAIdentityStore.class;
            } else if (LDAPIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                storeClass = (Class<T>) LDAPIdentityStore.class;
            }
        }

        if (storeClass == null) {
            throw MESSAGES.configUnknownStoreForConfiguration(storeConfiguration);
        }

        T store = null;

        try {
            if (storeConfiguration instanceof AbstractIdentityStoreConfiguration) {
                ((AbstractIdentityStoreConfiguration) storeConfiguration).setIdentityStoreType(storeClass);
            }

            store = storeClass.newInstance();
        } catch (Exception ex) {
            throw MESSAGES.configCouldNotCreateStore(storeClass, storeConfiguration, ex);
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

        throw MESSAGES.partitionNoConfigurationFound(name);
    }

    private IdentityConfiguration getConfigurationForPartition(Partition partition) {
        IdentityConfiguration identityConfiguration = lookupPartitionConfiguration(partition);

        if (identityConfiguration == null) {
            throw MESSAGES.partitionReferencesInvalidConfiguration(partition);
        }

        return identityConfiguration;
    }

    private IdentityConfiguration lookupPartitionConfiguration(Partition partition) {
        if (!partitionConfigurations.containsKey(partition)) {
            IdentityContext context = createIdentityContext();
            PartitionStore<?> store = getStoreForPartitionOperation(context);
            partitionConfigurations.put(partition, getConfigurationByName(store.getConfigurationName(context, partition)));
        }

        return partitionConfigurations.get(partition);
    }

    private IdentityContext createIdentityContext() {
        return new AbstractIdentityContext(null, this.eventBridge, this.idGenerator) {
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
    public IdentityManager createIdentityManager() throws IdentityManagementException {
        return createIdentityManager(DEFAULT_REALM);
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) throws IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        Partition storedPartition = null;

        if (this.partitionManagementConfig != null) {
            storedPartition = getPartition(partition.getClass(), partition.getName());
        } else {
            storedPartition = createDefaultPartition();
        }

        if (storedPartition == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }

        try {
            return new ContextualIdentityManager(storedPartition, eventBridge, idGenerator, this, createRelationshipManager());
        } catch (Exception e) {
            throw MESSAGES.partitionCouldNotCreateIdentityManager(storedPartition);
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

        Partition storedPartition = null;
        if (this.partitionManagementConfig != null) {
            storedPartition = getPartition(partition.getClass(), partition.getName());
        } else {
            storedPartition = createDefaultPartition();
        }

        if (storedPartition == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }

        try {
            return new ContextualPermissionManager(storedPartition, eventBridge, idGenerator, this);
        } catch (Exception ex) {
            throw MESSAGES.partitionCouldNotCreatePermissionManager(storedPartition);
        }
    }

    @Override
    public RelationshipManager createRelationshipManager() {
        return new ContextualRelationshipManager(eventBridge, idGenerator, this);
    }

    @Override
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        if (partitionClass == null) {
            throw MESSAGES.nullArgument("Partition class");
        }

        if (isNullOrEmpty(name)) {
            throw MESSAGES.nullArgument("Partition name");
        }

        if (partitionManagementConfig == null) {
            return (T) createDefaultPartition();
        }

        try {
            IdentityContext context = createIdentityContext();
            T partition = getStoreForPartitionOperation(context).<T>get(context, partitionClass, name);

            if (partition != null) {
                AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

                if (attributeStore != null) {
                    attributeStore.loadAttributes(context, partition);
                }
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

        List<T> partitions = null;

        if (partitionManagementConfig == null) {
            partitions = new ArrayList<T>();

            partitions.add((T) createDefaultPartition());

            return partitions;
        }

        try {
            IdentityContext context = createIdentityContext();

            partitions = getStoreForPartitionOperation(context).<T>get(context, partitionClass);

            for (T partition: partitions) {
                AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

                if (attributeStore != null) {
                    attributeStore.loadAttributes(context, partition);
                }
            }

            return partitions;
        } catch (Exception e) {
            throw MESSAGES.partitionGetFailed(partitionClass, "not specified", e);
        }
    }

    @Override
    public <T extends Partition> T lookupById(final Class<T> partitionClass, final String id) {
        if (partitionClass == null) {
            throw MESSAGES.nullArgument("Partition class");
        }

        if (isNullOrEmpty(id)) {
            throw MESSAGES.nullArgument("Partition identifier");
        }

        if (partitionManagementConfig == null) {
            return (T) createDefaultPartition();
        }

        try {
            IdentityContext context = createIdentityContext();
            T partition = getStoreForPartitionOperation(context).<T>lookupById(context, partitionClass, id);

            if (partition != null) {
                AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

                if (attributeStore != null) {
                    attributeStore.loadAttributes(context, partition);
                }
            }

            return partition;
        } catch (Exception e) {
            throw MESSAGES.partitionGetFailed(partitionClass, id, e);
        }
    }

    public void add(Partition partition) throws IdentityManagementException, OperationNotSupportedException {
        add(partition, null);
    }

    @Override
    public void add(Partition partition, String configurationName) throws IdentityManagementException,
            OperationNotSupportedException {
        checkPartitionManagementSupported();

        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (isNullOrEmpty(configurationName)) {
            configurationName = getDefaultConfigurationName();
        }

        if (getConfigurationByName(configurationName) != null) {
            if (getPartition(partition.getClass(), partition.getName()) != null) {
                throw MESSAGES.partitionAlreadyExistsWithName(partition.getClass(), partition.getName());
            }

            try {
                IdentityContext context = createIdentityContext();

                getStoreForPartitionOperation(context).add(context, partition, configurationName);

                AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

                if (attributeStore != null) {
                    for (Attribute<? extends Serializable> attribute : partition.getAttributes()) {
                        attributeStore.setAttribute(context, partition, attribute);
                    }
                }
            } catch (Exception e) {
                throw MESSAGES.partitionAddFailed(partition, configurationName, e);
            }
        }
    }

    @Override
    public void update(Partition partition) throws IdentityManagementException, OperationNotSupportedException {
        checkPartitionManagementSupported();
        checkIfPartitionExists(partition);

        try {
            IdentityContext context = createIdentityContext();
            getStoreForPartitionOperation(context).update(context, partition);

            AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

            if (attributeStore != null) {
                Partition storedType = lookupById(partition.getClass(), partition.getId());

                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    if (partition.getAttribute(attribute.getName()) == null) {
                        attributeStore.removeAttribute(context, partition, attribute.getName());
                    }
                }

                for (Attribute<? extends Serializable> attribute : partition.getAttributes()) {
                    attributeStore.setAttribute(context, partition, attribute);
                }
            }
        } catch (Exception e) {
            throw MESSAGES.partitionUpdateFailed(partition, e);
        }
    }

    @Override
    public void remove(Partition partition) {
        checkPartitionManagementSupported();
        checkIfPartitionExists(partition);

        try {
            IdentityContext context = createIdentityContext();

            AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

            if (attributeStore != null) {
                Partition storedType = lookupById(partition.getClass(), partition.getId());

                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    attributeStore.removeAttribute(context, storedType, attribute.getName());
                }
            }

            getStoreForPartitionOperation(context).remove(context, partition);
        } catch (Exception e) {
            throw MESSAGES.partitionRemoveFailed(partition, e);
        }
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(IdentityContext context, Class<T> storeType,
                                                                       Class<? extends AttributedType> type, IdentityOperation operation) {
        checkSupportedTypes(context.getPartition(), type);

        IdentityConfiguration identityConfiguration = null;

        if (this.partitionManagementConfig != null) {
            identityConfiguration = getConfigurationForPartition(context.getPartition());
        } else if (this.configurations.size() == 1) {
            identityConfiguration = this.configurations.iterator().next();
        }

        T identityStore = null;

        if (identityConfiguration == null) {
            for (IdentityConfiguration configuration : this.configurations) {
                identityStore = lookupStore(context, configuration, type, operation);

                if (identityStore != null) {
                    break;
                }
            }
        } else {
            identityStore = lookupStore(context, identityConfiguration, type, operation);
        }

        if (identityStore == null) {
            throw MESSAGES.attributedTypeUnsupportedOperation(type, operation, type, operation);
        }

        return identityStore;
    }

    public <T extends IdentityStore<?>> T lookupStore(IdentityContext context, IdentityConfiguration configuration,
                                                      Class<? extends AttributedType> type, IdentityOperation operation) {
        for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
            if (storeConfig.supportsType(type, operation)) {
                @SuppressWarnings("unchecked")
                T store = (T) stores.get(configuration).get(storeConfig);
                storeConfig.initializeContext(context, store);
                return store;
            }
        }

        return null;
    }

    @Override
    public <T extends CredentialStore<?>> T getStoreForCredentialOperation(IdentityContext context, Class<?> credentialClass) {
        T store = null;

        IdentityConfiguration identityConfiguration = null;

        if (this.partitionManagementConfig != null) {
            identityConfiguration = getConfigurationForPartition(context.getPartition());
        } else {
            identityConfiguration = this.configurations.iterator().next();
        }

        for (IdentityStoreConfiguration storeConfig : identityConfiguration.getStoreConfiguration()) {
            for (@SuppressWarnings("rawtypes") Class<? extends CredentialHandler> handlerClass : storeConfig.getCredentialHandlers()) {
                if (handlerClass.isAnnotationPresent(SupportsCredentials.class)) {
                    for (Class<?> cls : handlerClass.getAnnotation(SupportsCredentials.class).credentialClass()) {
                        if (cls.isAssignableFrom(credentialClass)) {
                            IdentityStore<?> identityStore = null;
                            try {
                                identityStore = stores.get(identityConfiguration).get(storeConfig);
                                store = (T) identityStore;
                                storeConfig.initializeContext(context, store);
                            } catch (ClassCastException cce) {
                                throw MESSAGES.storeUnexpectedType(identityStore.getClass(), CredentialStore.class);
                            }

                            // if we found a specific handler for the credential, immediately return.
                            if (cls.equals(credentialClass)) {
                                return store;
                            }
                        }
                    }
                }
            }
        }

        if (store == null) {
            throw MESSAGES.credentialNoStoreForCredentials(credentialClass);
        }

        return store;
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                             Relationship relationship, IdentityOperation operation) {

        Set<Partition> partitions = relationshipMetadata.getRelationshipPartitions(relationship);

        IdentityStore<?> store = null;

        // Check if the partition can manage its own relationship
        if (partitions.size() == 1) {
            IdentityConfiguration config = null;

            if (this.partitionManagementConfig != null) {
                config = getConfigurationForPartition(partitions.iterator().next());
            } else {
                config = this.configurations.iterator().next();
            }

            if (config.getRelationshipPolicy().isSelfRelationshipSupported(relationshipClass)) {
                for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                    if (storeConfig.supportsType(relationshipClass, operation)) {
                        store = stores.get(config).get(storeConfig);
                        storeConfig.initializeContext(context, store);
                    }
                }
            }
        } else {
            // This is a multi-partition relationship - use the configuration that supports the global relationship type
            for (Partition partition : partitions) {
                IdentityConfiguration config = getConfigurationForPartition(partition);
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, operation)) {
                            store = stores.get(config).get(storeConfig);
                            storeConfig.initializeContext(context, store);
                        }
                    }
                }
            }
        }

        // If none of the participating partition configurations support the relationship, try to find another configuration
        // that supports the global relationship as a last ditch effort
        if (store == null) {
            for (IdentityConfiguration cfg : configurations) {
                if (cfg.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass)) {
                    // found one
                    for (IdentityStoreConfiguration storeConfig : cfg.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, operation)) {
                            store = stores.get(cfg).get(storeConfig);
                            storeConfig.initializeContext(context, store);
                        }
                    }
                }
            }
        }

        return store;
    }

    @Override
    public Set<IdentityStore<?>> getStoresForRelationshipQuery(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                               Set<Partition> partitions) {
        Set<IdentityStore<?>> result = new HashSet<IdentityStore<?>>();

        // If _no_ parameters have been specified for the query at all, we return all stores that support the
        // specified relationship class
        if (partitions.isEmpty()) {
            for (IdentityConfiguration config : configurations) {
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass) ||
                        config.getRelationshipPolicy().isSelfRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, IdentityOperation.create)) {
                            IdentityStore<?> store = stores.get(config).get(storeConfig);
                            storeConfig.initializeContext(context, store);
                            result.add(store);
                        }
                    }
                }
            }
        } else {
            for (Partition partition : partitions) {
                IdentityConfiguration config = getConfigurationForPartition(partition);
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, IdentityOperation.create)) {
                            IdentityStore<?> store = stores.get(config).get(storeConfig);
                            storeConfig.initializeContext(context, store);
                            result.add(store);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public <T extends PartitionStore<?>> T getStoreForPartitionOperation(IdentityContext context) {
        Map<IdentityStoreConfiguration, IdentityStore<?>> configStores = stores.get(partitionManagementConfig);

        for (IdentityStoreConfiguration cfg : configStores.keySet()) {
            if (cfg.supportsType(Partition.class, IdentityOperation.create)) {
                T store = null;

                try {
                    store = (T) configStores.get(cfg);
                    cfg.initializeContext(context, store);
                    return store;
                } catch (ClassCastException cce) {
                    throw MESSAGES.storeUnexpectedType(store.getClass(), PartitionStore.class);
                }
            }
        }

        throw MESSAGES.storeNotFound(PartitionStore.class);
    }

    @Override
    public <T extends AttributeStore<?>> T getStoreForAttributeOperation(IdentityContext context) {
        if (attributeManagementConfig != null) {
            Map<IdentityStoreConfiguration, IdentityStore<?>> configStores = stores.get(attributeManagementConfig);

            for (IdentityStoreConfiguration cfg : configStores.keySet()) {
                if (cfg.supportsAttribute()) {
                    T store = null;

                    try {
                        store = (T) configStores.get(cfg);
                        cfg.initializeContext(context, store);
                        return store;
                    } catch (ClassCastException cce) {
                        throw MESSAGES.storeUnexpectedType(store.getClass(), AttributeStore.class);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Set<CredentialStore<?>> getStoresForCredentialStorage(final IdentityContext context, Class<? extends CredentialStorage> storageClass) {
        Map<IdentityStoreConfiguration, IdentityStore<?>> storesConfig = this.stores.get(getConfigurationForPartition(context.getPartition()));

        Set<CredentialStore<?>> credentialStores = new HashSet<CredentialStore<?>>();

        if (storesConfig != null) {
            for (IdentityStore identityStore: storesConfig.values()) {
                if (CredentialStore.class.isInstance(identityStore)) {
                    CredentialStore<?> credentialStore = (CredentialStore<?>) identityStore;

                    for (Class<? extends CredentialHandler> credentialHandler: credentialStore.getConfig().getCredentialHandlers()) {
                        SupportsCredentials supportedCredentials = credentialHandler.getAnnotation(SupportsCredentials.class);

                        if (supportedCredentials != null) {
                            if (supportedCredentials.credentialStorage().equals(storageClass)) {
                                credentialStores.add(credentialStore);
                            }
                        }
                    }
                }
            }
        }

        return credentialStores;
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

    private void checkPartitionManagementSupported() throws OperationNotSupportedException {
        if (partitionManagementConfig == null) {
            throw MESSAGES.partitionManagementNoSupported(Partition.class, IdentityOperation.create);
        }
    }

    private void checkSupportedTypes(Partition partition, Class<? extends AttributedType> type) {
        if (partition != null) {
            if (IdentityType.class.isAssignableFrom(type)) {
                IdentityPartition identityPartition = partition.getClass().getAnnotation(IdentityPartition.class);

                if (identityPartition != null
                        && isTypeSupported((Class<? extends IdentityType>) type, toSet(identityPartition.supportedTypes()),
                        toSet(identityPartition.unsupportedTypes())) == -1) {
                    throw new IdentityManagementException("Partition [" + partition + "] does not support type [" + type + "].");
                }
            }
        }
    }

    private void checkIfPartitionExists(Partition partition) {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        if (lookupById(partition.getClass(), partition.getId()) == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }
    }

    private Partition createDefaultPartition() {
        Partition storedPartition = new Realm(Realm.DEFAULT_REALM);

        storedPartition.setId(Realm.DEFAULT_REALM);

        return storedPartition;
    }

    @Override
    public PermissionStore getStoreForPermissionOperation(IdentityContext context) {

        IdentityConfiguration identityConfiguration = null;

        if (this.partitionManagementConfig != null) {
            identityConfiguration = getConfigurationForPartition(context.getPartition());
        } else if (this.configurations.size() == 1) {
            identityConfiguration = this.configurations.iterator().next();
        }

        PermissionStore store = null;

        /*if (identityConfiguration == null) {
            for (IdentityConfiguration configuration : this.configurations) {
                store = lookupStore(context, configuration, type, operation);

                if (store != null) {
                    break;
                }
            }
        } else {
            store = lookupStore(context, identityConfiguration, type, operation);
        }

        if (store == null) {
            throw MESSAGES.attributedTypeUnsupportedOperation(type, operation, type, operation);
        }*/

        return store;
    }

}