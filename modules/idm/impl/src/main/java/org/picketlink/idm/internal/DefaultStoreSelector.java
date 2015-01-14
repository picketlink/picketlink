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

import org.picketlink.idm.config.AbstractIdentityStoreConfiguration;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;
import org.picketlink.idm.config.JDBCIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.TokenStoreConfiguration;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.file.internal.FileIdentityStore;
import org.picketlink.idm.internal.util.RelationshipMetadata;
import org.picketlink.idm.jdbc.internal.JDBCIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.IdentityPartition;
import org.picketlink.idm.permission.acl.spi.PermissionStore;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;
import org.picketlink.idm.token.internal.TokenIdentityStore;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation.create;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation.read;
import static org.picketlink.idm.util.IDMUtil.isTypeSupported;
import static org.picketlink.idm.util.IDMUtil.toSet;

/**
 * @author pedroigor
 */
public class DefaultStoreSelector implements StoreSelector {

    private final PartitionManagerConfiguration configuration;

    /**
     * Cache for relationship metadata
     */
    private RelationshipMetadata relationshipMetadata = new RelationshipMetadata();

    /**
     * Each partition is governed by a specific IdentityConfiguration, indicated by this Map.  Every
     * IdentityConfiguration instance will also be found in the configurations property above.
     */
    private final Map<Partition, IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition, IdentityConfiguration>();

    /**
     * The store instances for each IdentityConfiguration, mapped by their corresponding IdentityStoreConfiguration
     */
    private final Map<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>> stores;

    private final Map<String, Map<Class<? extends IdentityType>, Set<IdentityStoreConfiguration>>> identityQueryStoresCache = new HashMap<String, Map<Class<? extends IdentityType>, Set<IdentityStoreConfiguration>>>();
    private final Map<String, Map<Class<?>, IdentityStoreConfiguration>> credentialStoresCache = new HashMap<String, Map<Class<?>, IdentityStoreConfiguration>>();

    public DefaultStoreSelector(PartitionManagerConfiguration configuration) {
        this.configuration = configuration;

        Map<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>> configuredStores =
                new HashMap<IdentityConfiguration, Map<IdentityStoreConfiguration, IdentityStore<?>>>();

        for (IdentityConfiguration config : this.configuration.getConfigurations()) {
            Map<IdentityStoreConfiguration, IdentityStore<?>> storeMap = new HashMap<IdentityStoreConfiguration, IdentityStore<?>>();

            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                storeMap.put(storeConfig, createIdentityStore(storeConfig));
            }

            configuredStores.put(config, Collections.unmodifiableMap(storeMap));
        }

        this.stores = Collections.unmodifiableMap(configuredStores);
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(IdentityContext context, Class<T> storeType,
                                                                       Class<? extends AttributedType> type, IdentityOperation operation) {
        checkSupportedTypes(context.getPartition(), type);

        IdentityConfiguration identityConfiguration = getConfigurationForPartition(context, context.getPartition());
        T identityStore = lookupStore(context, identityConfiguration, type, operation);

        if (identityStore == null) {
            throw MESSAGES.attributedTypeUnsupportedOperation(type, operation, type, operation);
        }

        return identityStore;
    }

    @Override
    public Set<IdentityStore<?>> getStoresForIdentityQuery(final IdentityContext context, final Class<? extends IdentityType> identityType) {
        IdentityConfiguration identityConfiguration = getConfigurationForPartition(context, context.getPartition());
        Map<Class<? extends IdentityType>, Set<IdentityStoreConfiguration>> cachedStoresForType = this.identityQueryStoresCache.get(context.getPartition().getName());

        if (cachedStoresForType != null) {
            Set<IdentityStoreConfiguration> storeConfigs = cachedStoresForType.get(identityType);

            if (storeConfigs != null) {
                Set<IdentityStore<?>> identityStores = new HashSet<IdentityStore<?>>();

                for (IdentityStoreConfiguration storeConfig : storeConfigs) {
                    identityStores.add(getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig));
                }


                return identityStores;
            }
        }

        Set<IdentityStore<?>> identityStores = new HashSet<IdentityStore<?>>();
        Set<IdentityStoreConfiguration> identityStoresConfig = new HashSet<IdentityStoreConfiguration>();

        cachedStoresForType = new HashMap<Class<? extends IdentityType>, Set<IdentityStoreConfiguration>>();
        cachedStoresForType.put(identityType, identityStoresConfig);
        this.identityQueryStoresCache.put(context.getPartition().getName(), cachedStoresForType);

        for (IdentityStoreConfiguration storeConfig : identityConfiguration.getStoreConfiguration()) {
            if (storeConfig.supportsType(identityType, read)) {
                identityStores.add(getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig));
                identityStoresConfig.add(storeConfig);
            }
        }

        if (identityStores.isEmpty()) {
            throw MESSAGES.attributedTypeUnsupportedOperation(identityType, read, identityType, read);
        }

        return identityStores;
    }

    @Override
    public <T extends CredentialStore<?>> T getStoreForCredentialOperation(IdentityContext context, Class<?> credentialClass) {
        T store = null;
        IdentityConfiguration identityConfiguration = getConfigurationForPartition(context, context.getPartition());

        if (identityConfiguration == null) {
            for (IdentityConfiguration configuration : this.configuration.getConfigurations()) {
                for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                    if (storeConfig.supportsCredential()) {
                        identityConfiguration = configuration;
                    }
                }
            }
        }

        if (identityConfiguration != null) {
            Map<Class<?>, IdentityStoreConfiguration> cachedStoresForType = this.credentialStoresCache.get(context.getPartition().getName());

            if (cachedStoresForType != null) {
                IdentityStoreConfiguration storeConfig = cachedStoresForType.get(credentialClass);

                if (storeConfig != null) {
                    return getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig);
                }
            }

            if (identityConfiguration.supportsCredential()) {
                for (IdentityStoreConfiguration storeConfig : identityConfiguration.getStoreConfiguration()) {
                    if (storeConfig.supportsCredential()) {
                        for (@SuppressWarnings("rawtypes") Class<? extends CredentialHandler> handlerClass : storeConfig.getCredentialHandlers()) {
                            if (handlerClass.isAnnotationPresent(SupportsCredentials.class)) {
                                for (Class<?> cls : handlerClass.getAnnotation(SupportsCredentials.class).credentialClass()) {
                                    if (cls.isAssignableFrom(credentialClass)) {
                                        IdentityStore<?> identityStore = getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig);

                                        try {
                                            store = (T) identityStore;
                                        } catch (ClassCastException cce) {
                                            throw MESSAGES.storeUnexpectedType(CredentialStore.class, identityStore.getClass());
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

            cachedStoresForType = new HashMap<Class<?>, IdentityStoreConfiguration>();
            cachedStoresForType.put(credentialClass, store.getConfig());
            this.credentialStoresCache.put(context.getPartition().getName(), cachedStoresForType);
        }

        if (store == null) {
            throw MESSAGES.credentialNoStoreForCredentials(credentialClass);
        }

        return store;
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(IdentityContext context, Class<? extends Relationship> relationshipClass,
                                                             Relationship relationship, IdentityOperation operation) {
        Set<Partition> partitions = getRelationshipPartitions(relationship);
        IdentityStore<?> store = null;

        // Check if the partition can manage its own relationship
        if (partitions.size() == 1) {
            IdentityConfiguration config = getConfigurationForPartition(context, partitions.iterator().next());

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
                IdentityConfiguration config = getConfigurationForPartition(context, partition);

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
            for (IdentityConfiguration cfg : this.configuration.getConfigurations()) {
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
            for (IdentityConfiguration config : this.configuration.getConfigurations()) {
                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass) ||
                        config.getRelationshipPolicy().isSelfRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, create) || Relationship.class.equals(relationshipClass)) {
                            identityStores.add(getIdentityStoreAndInitializeContext(context, config, storeConfig));
                        }
                    }
                }
            }
        } else {
            for (Partition partition : partitions) {
                IdentityConfiguration config = getConfigurationForPartition(context, partition);

                if (config.getRelationshipPolicy().isGlobalRelationshipSupported(relationshipClass)) {
                    for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                        if (storeConfig.supportsType(relationshipClass, create) || Relationship.class.equals(relationshipClass)) {
                            identityStores.add(getIdentityStoreAndInitializeContext(context, config, storeConfig));
                        }
                    }
                }
            }
        }

        if (identityStores.isEmpty()) {
            throw MESSAGES.attributedTypeUnsupportedOperation(relationshipClass, read, relationshipClass, read);
        }

        return identityStores;
    }

    @Override
    public <T extends PartitionStore<?>> T getStoreForPartitionOperation(IdentityContext context, Class<? extends Partition> partitionClass) {
        IdentityConfiguration partitionManagementConfig = this.configuration.getPartitionManagementConfig();
        Map<IdentityStoreConfiguration, IdentityStore<?>> configStores = stores.get(partitionManagementConfig);

        for (IdentityStoreConfiguration cfg : configStores.keySet()) {
            if (cfg.supportsType(partitionClass, create)) {
                T store = getIdentityStoreAndInitializeContext(context, partitionManagementConfig, cfg);

                if (!PartitionStore.class.isInstance(store)) {
                    throw MESSAGES.storeUnexpectedType(store.getClass(), PartitionStore.class);
                }

                return store;
            }
        }

        throw MESSAGES.storeNotFound(PartitionStore.class, partitionClass);
    }

    @Override
    public <T extends AttributeStore<?>> T getStoreForAttributeOperation(IdentityContext context) {
        IdentityConfiguration attributeManagementConfig = this.configuration.getAttributeManagementConfig();

        if (attributeManagementConfig != null) {
            Map<IdentityStoreConfiguration, IdentityStore<?>> configStores = stores.get(attributeManagementConfig);

            for (IdentityStoreConfiguration cfg : configStores.keySet()) {
                if (cfg.supportsAttribute()) {
                    T store = getIdentityStoreAndInitializeContext(context, attributeManagementConfig, cfg);

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
        IdentityConfiguration identityConfiguration = getConfigurationForPartition(context, context.getPartition());
        Map<IdentityStoreConfiguration, IdentityStore<?>> storesConfig = this.stores.get(identityConfiguration);
        Set<CredentialStore<?>> credentialStores = new HashSet<CredentialStore<?>>();

        if (storesConfig != null) {
            for (IdentityStoreConfiguration storeConfig : storesConfig.keySet()) {
                if (storeConfig.supportsCredential()) {
                    for (Class<? extends CredentialHandler> credentialHandler : storeConfig.getCredentialHandlers()) {
                        SupportsCredentials supportedCredentials = credentialHandler.getAnnotation(SupportsCredentials.class);

                        if (supportedCredentials != null) {
                            if (supportedCredentials.credentialStorage().equals(storageClass)) {
                                CredentialStore<?> credentialStore = (CredentialStore<?>) getIdentityStoreAndInitializeContext(context, identityConfiguration, storeConfig);
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
        IdentityConfiguration identityConfiguration = getConfigurationForPartition(context, context.getPartition());

        if (identityConfiguration == null) {
            for (IdentityConfiguration configuration : this.configuration.getConfigurations()) {
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

        return null;
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
            } else if (TokenStoreConfiguration.class.isInstance(storeConfiguration)) {
                storeClass = (Class<T>) TokenIdentityStore.class;
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

    public <T extends IdentityStore<?>> T lookupStore(IdentityContext context, IdentityConfiguration configuration,
                                                      Class<? extends AttributedType> type, IdentityOperation operation) {
        for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
            if (storeConfig.supportsType(type, operation)) {
                return getIdentityStoreAndInitializeContext(context, configuration, storeConfig);
            }
        }

        return null;
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

    IdentityConfiguration getConfigurationForPartition(IdentityContext identityContext, Partition partition) {
        IdentityConfiguration partitionManagementConfig = this.configuration.getPartitionManagementConfig();

        if (partitionManagementConfig == null) {
            Collection<IdentityConfiguration> configurations = this.configuration.getConfigurations();

            if (configurations.size() == 1) {
                return configurations.iterator().next();
            }
        }

        if (!this.partitionConfigurations.containsKey(partition)) {
            PartitionStore<?> store = getStoreForPartitionOperation(identityContext, partition.getClass());
            partitionConfigurations.put(partition, this.configuration.getConfigurationByName(store.getConfigurationName(identityContext, partition)));
        }

        IdentityConfiguration identityConfiguration = partitionConfigurations.get(partition);

        if (identityConfiguration == null) {
            throw MESSAGES.partitionReferencesInvalidConfiguration(partition);
        }

        return identityConfiguration;
    }

    private Set<Partition> getRelationshipPartitions(Relationship relationship) {
        return this.relationshipMetadata.getRelationshipPartitions(relationship);
    }
}
