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
import org.picketlink.idm.config.JDBCIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.file.internal.FileIdentityStore;
import org.picketlink.idm.internal.util.RelationshipMetadata;
import org.picketlink.idm.jdbc.internal.JDBCIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.IdentityPartition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.permission.acl.spi.PermissionHandler;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;
import org.picketlink.idm.permission.acl.spi.PermissionStore;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;

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

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.IDMLog.ROOT_LOGGER;
import static org.picketlink.idm.util.IDMUtil.isTypeSupported;
import static org.picketlink.idm.util.IDMUtil.toSet;

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
    /**
     * The IdentityConfiguration that is responsible for managing attributes.  It is possible for this
     * value to be null, in which case attribute management will not be supported.
     */
    private final IdentityConfiguration attributeManagementConfig;
    /**
     * The event bridge allows events to be "bridged" to an event bus, such as the CDI event bus
     */
    private EventBridge eventBridge;
    /**
     * The ID generator is responsible for generating unique identifier values
     */
    private IdGenerator idGenerator;

    /**
     * Cache for relationship metadata
     */
    private RelationshipMetadata relationshipMetadata = new RelationshipMetadata();

    /**
     * Used for querying chained privileges
     */
    private PrivilegeChainQuery privilegeChainQuery = new PrivilegeChainQuery();

    /**
     * Permission handler policy
     */
    private PermissionHandlerPolicy permissionHandlerPolicy;

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
        if (configurations == null || configurations.isEmpty()) {
            throw MESSAGES.configNoIdentityConfigurationProvided();
        }

        ROOT_LOGGER.partitionManagerBootstrap();

        try {
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

            permissionHandlerPolicy = new PermissionHandlerPolicy(null);
            if (permissionHandlers != null) {
                for (PermissionHandler handler : permissionHandlers) {
                    permissionHandlerPolicy.registerHandler(handler);
                }
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

            logConfiguration(this.configurations);

            Map<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>> configuredStores =
                    new HashMap<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>>();

            for (IdentityConfiguration config : configurations) {
                Map<IdentityStoreConfiguration, IdentityStore<?>> storeMap = new HashMap<IdentityStoreConfiguration, IdentityStore<?>>();

                for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                    storeMap.put(storeConfig, createIdentityStore(storeConfig));
                }

                configuredStores.put(config, Collections.unmodifiableMap(storeMap));

                // Register all known relationship types so that the privilege chain query can determine inherited privileges
                for (Class<? extends Relationship> relationshipType : config.getRegisteredRelationshipTypes()) {
                    privilegeChainQuery.registerRelationshipType(relationshipType);
                }
            }

            stores = Collections.unmodifiableMap(configuredStores);
        } catch (Exception e) {
            throw MESSAGES.partitionManagerInitializationFailed(this.getClass(), e);
        }
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

        Partition storedPartition = getStoredPartition(partition);

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

        Partition storedPartition = getStoredPartition(partition);

        try {
            return new ContextualPermissionManager(storedPartition, eventBridge, idGenerator,
                    permissionHandlerPolicy, this);
        } catch (Exception ex) {
            throw MESSAGES.partitionCouldNotCreatePermissionManager(storedPartition);
        }
    }

    @Override
    public RelationshipManager createRelationshipManager() {
        return new ContextualRelationshipManager(eventBridge, idGenerator, this, privilegeChainQuery);
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
            T partition = getStoreForPartitionOperation(context, partitionClass).<T>get(context, partitionClass, name);

            if (partition != null) {
                loadAttributes(context, (T) partition);
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

        if (partitionManagementConfig == null) {
            partitions.add((T) createDefaultPartition());
        } else {
            try {
                IdentityContext context = createIdentityContext();

                partitions.addAll(getStoreForPartitionOperation(context, partitionClass).<T>get(context, partitionClass));

                for (T partition : partitions) {
                    loadAttributes(context, partition);
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

        if (partitionManagementConfig == null) {
            return (T) createDefaultPartition();
        }

        try {
            IdentityContext context = createIdentityContext();
            T partition = getStoreForPartitionOperation(context, partitionClass).<T>lookupById(context, partitionClass, id);

            if (partition != null) {
                loadAttributes(context, (T) partition);
            }

            return partition;
        } catch (Exception e) {
            throw MESSAGES.partitionGetFailed(partitionClass, id, e);
        }
    }

    public void add(Partition partition) throws IdentityManagementException {
        add(partition, null);
    }

    @Override
    public void add(Partition partition, String configurationName) throws IdentityManagementException {
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

                getStoreForPartitionOperation(context, partition.getClass()).add(context, partition, configurationName);

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
    public void update(Partition partition) throws IdentityManagementException {
        checkPartitionManagementSupported();
        checkIfPartitionExists(partition);

        try {
            IdentityContext context = createIdentityContext();
            getStoreForPartitionOperation(context, partition.getClass()).update(context, partition);

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
    public void remove(Partition partition) throws IdentityManagementException {
        checkPartitionManagementSupported();
        checkIfPartitionExists(partition);

        try {
            IdentityContext context = createIdentityContext();

            AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

            if (attributeStore != null) {
                Partition storedType = lookupById(partition.getClass(), partition.getId());

                IdentityManager identityManager = createIdentityManager(storedType);
                IdentityQuery<IdentityType> query = identityManager.createIdentityQuery(IdentityType.class);

                for (IdentityType identityType : query.getResultList()) {
                    identityManager.remove(identityType);
                }

                for (Attribute<? extends Serializable> attribute : storedType.getAttributes()) {
                    attributeStore.removeAttribute(context, storedType, attribute.getName());
                }
            }

            getStoreForPartitionOperation(context, partition.getClass()).remove(context, partition);
        } catch (Exception e) {
            throw MESSAGES.partitionRemoveFailed(partition, e);
        }
    }

    @Override
    public Collection<IdentityConfiguration> getConfigurations() {
        return this.configurations;
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

    @Override
    public Set<IdentityStore<?>> getStoresForIdentityQuery(final IdentityContext context, final Class<? extends IdentityType> identityType) {
        Set<IdentityStore<?>> identityStores = new HashSet<IdentityStore<?>>();

        for (IdentityConfiguration configuration : this.configurations) {
            for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                if (storeConfig.supportsType(identityType, IdentityOperation.read) || IdentityType.class.equals(identityType)) {
                    identityStores.add(getIdentityStoreAndInitializeContext(context, configuration, storeConfig));
                }
            }
        }

        if (identityStores.isEmpty()) {
            throw MESSAGES.attributedTypeUnsupportedOperation(identityType, IdentityOperation.read, identityType, IdentityOperation.read);
        }

        return identityStores;
    }

    public <T extends IdentityStore<?>> T lookupStore(IdentityContext context, IdentityConfiguration configuration,
                                                      Class<? extends AttributedType> type, IdentityOperation operation) {
        for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
            if (storeConfig.supportsType(type, operation)) {
                return getIdentityStoreAndInitializeContext(context, configuration, storeConfig);
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
            for (IdentityConfiguration configuration : this.configurations) {
                for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                    if (storeConfig.supportsCredential()) {
                        identityConfiguration = configuration;
                    }
                }
            }
        }

        if (identityConfiguration != null && identityConfiguration.supportsCredential()) {
            for (IdentityStoreConfiguration storeConfig : identityConfiguration.getStoreConfiguration()) {
                if (storeConfig.supportsCredential()) {
                    for (@SuppressWarnings("rawtypes") Class<? extends CredentialHandler> handlerClass : storeConfig.getCredentialHandlers()) {
                        if (handlerClass.isAnnotationPresent(SupportsCredentials.class)) {
                            for (Class<?> cls : handlerClass.getAnnotation(SupportsCredentials.class).credentialClass()) {
                                if (cls.isAssignableFrom(credentialClass)) {
                                    IdentityStore<?> identityStore = null;
                                    try {
                                        store = getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig);
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
            IdentityConfiguration config;

            if (this.partitionManagementConfig != null) {
                config = getConfigurationForPartition(partitions.iterator().next());
            } else {
                config = this.configurations.iterator().next();
            }

            if (config.getRelationshipPolicy().isSelfRelationshipSupported(relationshipClass)) {
                for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                    if (storeConfig.supportsType(relationshipClass, operation)) {
                        store = getIdentityStoreAndInitializeContext(context, config, storeConfig);
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
                            store = getIdentityStoreAndInitializeContext(context, config, storeConfig);
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
                            store = getIdentityStoreAndInitializeContext(context, cfg, storeConfig);
                        }
                    }
                }
            }
        }

        if (store == null) {
            throw MESSAGES.attributedTypeUnsupportedOperation(relationshipClass, operation, relationshipClass, operation);
        }

        return store;
    }

    @Override
    public Set<IdentityStore<?>> getStoresForRelationshipQuery(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                               Set<Partition> partitions) {
        Set<IdentityStore<?>> identityStores = new HashSet<IdentityStore<?>>();

        // If _no_ parameters have been specified for the query at all, we return all stores that support the
        // specified relationship class
        if (partitions.isEmpty()) {
            for (IdentityConfiguration config : configurations) {
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass) ||
                        config.getRelationshipPolicy().isSelfRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, IdentityOperation.create) || Relationship.class.equals(relationshipClass)) {
                            identityStores.add(getIdentityStoreAndInitializeContext(context, config, storeConfig));
                        }
                    }
                }
            }
        } else {
            for (Partition partition : partitions) {
                IdentityConfiguration config = getConfigurationForPartition(partition);
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, IdentityOperation.create) || Relationship.class.equals(relationshipClass)) {
                            identityStores.add(getIdentityStoreAndInitializeContext(context, config, storeConfig));
                        }
                    }
                }
            }
        }

        if (identityStores.isEmpty()) {
            throw MESSAGES.attributedTypeUnsupportedOperation(relationshipClass, IdentityOperation.read, relationshipClass, IdentityOperation.read);
        }

        return identityStores;
    }

    @Override
    public <T extends PartitionStore<?>> T getStoreForPartitionOperation(IdentityContext context, Class<? extends Partition> partitionClass) {
        Map<IdentityStoreConfiguration, IdentityStore<?>> configStores = stores.get(this.partitionManagementConfig);

        for (IdentityStoreConfiguration cfg : configStores.keySet()) {
            if (cfg.supportsType(partitionClass, IdentityOperation.create)) {
                T store = getIdentityStoreAndInitializeContext(context, this.partitionManagementConfig, cfg);

                if (!PartitionStore.class.isInstance(store)) {
                    throw MESSAGES.storeUnexpectedType(store.getClass(), PartitionStore.class);
                }

                return store;
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
                    T store = getIdentityStoreAndInitializeContext(context, this.attributeManagementConfig, cfg);

                    if (!AttributeStore.class.isInstance(store)) {
                        throw MESSAGES.storeUnexpectedType(store.getClass(), AttributeStore.class);
                    }

                    return store;
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
            for (IdentityStore identityStore : storesConfig.values()) {
                if (CredentialStore.class.isInstance(identityStore) && identityStore.getConfig().supportsCredential()) {
                    CredentialStore<?> credentialStore = (CredentialStore<?>) identityStore;

                    for (Class<? extends CredentialHandler> credentialHandler : credentialStore.getConfig().getCredentialHandlers()) {
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

    @Override
    public PermissionStore getStoreForPermissionOperation(IdentityContext context) {

        IdentityConfiguration identityConfiguration = null;

        if (this.partitionManagementConfig != null) {
            identityConfiguration = getConfigurationForPartition(context.getPartition());
        } else if (this.configurations.size() == 1) {
            identityConfiguration = this.configurations.iterator().next();
        }

        if (identityConfiguration == null) {
            for (IdentityConfiguration configuration : this.configurations) {
                for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                    if (storeConfig.supportsPermissions()) {
                        return (PermissionStore) getIdentityStoreAndInitializeContext(context, configuration, storeConfig);
                    }
                }
            }
        } else {
            for (IdentityStoreConfiguration storeConfig : identityConfiguration.getStoreConfiguration()) {
                if (storeConfig.supportsPermissions()) {
                    return (PermissionStore) getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig);
                }
            }
        }

        throw MESSAGES.permissionUnsupportedOperation();
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
            } else if (JDBCIdentityStoreConfiguration.class.isInstance(storeConfiguration)) {
                storeClass = (Class<T>) JDBCIdentityStore.class;
            }
        }

        if (storeClass == null) {
            throw MESSAGES.configUnknownStoreForConfiguration(storeConfiguration);
        }

        try {
            if (storeConfiguration instanceof AbstractIdentityStoreConfiguration) {
                ((AbstractIdentityStoreConfiguration) storeConfiguration).setIdentityStoreType(storeClass);
            }

            T store = storeClass.newInstance();

            store.setup(storeConfiguration);

            return store;
        } catch (Exception ex) {
            throw MESSAGES.configCouldNotCreateStore(storeClass, storeConfiguration, ex);
        }
    }

    private IdentityConfiguration getConfigurationByName(String name) {
        for (IdentityConfiguration config : this.configurations) {
            if (name.equals(config.getName())) {
                return config;
            }
        }

        throw MESSAGES.partitionNoConfigurationFound(name);
    }

    private IdentityConfiguration getConfigurationForPartition(Partition partition) {
        if (!this.partitionConfigurations.containsKey(partition)) {
            IdentityContext context = createIdentityContext();
            PartitionStore<?> store = getStoreForPartitionOperation(context, partition.getClass());

            partitionConfigurations.put(partition, getConfigurationByName(store.getConfigurationName(context, partition)));
        }

        IdentityConfiguration identityConfiguration = partitionConfigurations.get(partition);

        if (identityConfiguration == null) {
            throw MESSAGES.partitionReferencesInvalidConfiguration(partition);
        }

        return identityConfiguration;
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
                    throw MESSAGES.partitionUnsupportedType(partition, type);
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

    private Partition getStoredPartition(final Partition partition) {
        Partition storedPartition;

        if (this.partitionManagementConfig != null) {
            storedPartition = getPartition(partition.getClass(), partition.getName());
        } else {
            storedPartition = createDefaultPartition();
        }

        if (storedPartition == null) {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }

        return storedPartition;
    }

    /**
     * <p>Returns a {@link IdentityStore} instance considering the given {@link IdentityConfiguration} and {@link
     * IdentityStoreConfiguration}.</p>
     *
     * <p>Before returning the instance, the {@link IdentityContext} is initialized.</p>
     *
     * @param context
     * @param configuration
     * @param storeConfig
     * @param <T>
     *
     * @return
     */
    private <T extends IdentityStore<?>> T getIdentityStoreAndInitializeContext(final IdentityContext context, final IdentityConfiguration configuration, final IdentityStoreConfiguration storeConfig) {
        IdentityStore<?> store = this.stores.get(configuration).get(storeConfig);

        storeConfig.initializeContext(context, store);

        return (T) store;
    }

    private <T extends Partition> void loadAttributes(final IdentityContext context, final T partition) {
        AttributeStore<?> attributeStore = getStoreForAttributeOperation(context);

        if (attributeStore != null) {
            attributeStore.loadAttributes(context, partition);
        }
    }

    private void logConfiguration(final Collection<IdentityConfiguration> configurations) {
        for (IdentityConfiguration identityConfiguration : configurations) {
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debug("  Identity Management Configuration: [");
                ROOT_LOGGER.debugf("    Name: %s", identityConfiguration.getName());
                ROOT_LOGGER.debugf("    Identity Store Configuration: %s", identityConfiguration.getStoreConfiguration());
                ROOT_LOGGER.debugf("    Supports Partition: %s", this.partitionManagementConfig != null && this.partitionManagementConfig.equals(identityConfiguration));
                ROOT_LOGGER.debugf("    Supports Attribute: %s", this.attributeManagementConfig != null && this.attributeManagementConfig.equals(identityConfiguration));
                ROOT_LOGGER.debugf("    Supports Credential: %s", identityConfiguration.supportsCredential());

                List<Class<?>> supportedTypes = new ArrayList<Class<?>>();

                for (IdentityStoreConfiguration storeConfiguration : identityConfiguration.getStoreConfiguration()) {
                    supportedTypes.addAll(storeConfiguration.getSupportedTypes().keySet());
                }

                ROOT_LOGGER.debugf("    Supported Types: %s", supportedTypes);
                ROOT_LOGGER.debug("  ]");
            }
        }
    }
}