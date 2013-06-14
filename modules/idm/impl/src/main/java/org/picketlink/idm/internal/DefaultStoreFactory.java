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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.StoreFactory;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default StoreFactory implementation. This factory is pre-configured to be able to create instances of the following built-in
 * IdentityStore implementations based on the corresponding IdentityStoreConfiguration:
 * <p/>
 * JPAIdentityStore - JPAIdentityStoreConfiguration LDAPIdentityStore - LDAPConfiguration FileBasedIdentityStore -
 * FileIdentityStoreConfiguration
 *
 * @author Shane Bryzak
 */
public class DefaultStoreFactory implements StoreFactory {

    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap = new ConcurrentHashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    private Map<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>> storesCache = new ConcurrentHashMap<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>>();

    private Map<String, Set<IdentityStoreConfiguration>> realmStores = new ConcurrentHashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Realm> configuredRealms = new HashMap<String, Realm>();

    private Map<String, Set<IdentityStoreConfiguration>> tierStores = new ConcurrentHashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Tier> configuredTiers = new ConcurrentHashMap<String, Tier>();

    private List<IdentityStoreConfiguration> configs = new ArrayList<IdentityStoreConfiguration>();

    public DefaultStoreFactory(IdentityConfiguration identityConfig) {
        this.identityConfigMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        this.identityConfigMap.put(LDAPIdentityStoreConfiguration.class, LDAPIdentityStore.class);
        this.identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);

        Map<FeatureGroup, IdentityStoreConfiguration> supportedFeatures = new HashMap<FeatureGroup, IdentityStoreConfiguration>();

        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            LOGGER.identityManagerInitConfigForRealms(config, config.getRealms());
            configs.add(config);

            Map<FeatureGroup, Set<FeatureOperation>> storeFeatures = config.getSupportedFeatures();

            // let's check for duplicated features
            for (Entry<FeatureGroup, Set<FeatureOperation>> entry : storeFeatures.entrySet()) {
                FeatureGroup feature = entry.getKey();

                // attributes can be stored for each store.
                if (!FeatureGroup.attribute.equals(feature)) {
                    IdentityStoreConfiguration storeConfigForFeature = supportedFeatures.get(feature);

                    if (storeConfigForFeature == null) {
                        supportedFeatures.put(feature, config);
                    } else {
                        throw MESSAGES.configurationAmbiguousFeatureForStore(feature, storeConfigForFeature, config);
                    }
                }
            }

            config.init();

            // let's configure the provided partitions
            for (String realm : config.getRealms()) {
                getConfigs(realmStores, realm).add(config);
            }

            for (String tier : config.getTiers()) {
                getConfigs(tierStores, tier).add(config);
            }

            // If no realms or tiers have been configured, treat this configuration as the default realm config
            if (config.getRealms().isEmpty() && config.getTiers().isEmpty()) {
                getConfigs(realmStores, Realm.DEFAULT_REALM).add(config);
            }

            // let's add any additional/custom store
            if (identityConfig.getAdditionalIdentityStores() != null) {
                Set<Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>>> entrySet = identityConfig.getAdditionalIdentityStores().entrySet();

                for (Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> entry : entrySet) {
                    this.identityConfigMap.put(entry.getKey(), (Class<? extends IdentityStore<?>>) entry.getValue());
                }
            }
            initializeIdentityStores();
        }
    }

    protected void initializeIdentityStores() {
        for (IdentityStoreConfiguration config : configs) {
            for (Class<? extends IdentityStoreConfiguration> cc : this.identityConfigMap.keySet()) {
                if (cc.isInstance(config)) {
                    IdentityStore identityStore = this.storesCache.get(cc);

                    if (identityStore == null) {
                        Class<? extends IdentityStore<?>> identityStoreClass = this.identityConfigMap.get(cc);

                        try {
                            identityStore = identityStoreClass.newInstance();
                            identityStore.setup(config);
                        } catch (Exception e) {
                            throw MESSAGES.instantiationError(identityStoreClass.getName(), e);
                        }

                        this.storesCache.put(cc, identityStore);
                    }
                }
            }
        }
    }


    @Override
    public Realm getRealm(String id) {
        if (configuredRealms.containsKey(id)) {
            return configuredRealms.get(id);
        } else if (realmStores.containsKey(id)) {
            Realm realm = new Realm(id);
            configuredRealms.put(id, realm);
            return realm;
        } else {
            return null;
        }
    }

    @Override
    public Realm createRealm(SecurityContext context, String id) {
        Realm realm = getRealm(id);
        if (realm != null) return realm;
        IdentityStoreConfiguration config = getPartitionStoreConfig();
        if (config == null) return null;
        PartitionStore store = getPartitionStore(context, config);
        if (store == null) return null;
        realm = new Realm(id);
        store.createPartition(context, realm);
        configuredRealms.put(id, realm);
        getConfigs(realmStores, id).add(config);
        return realm;
    }

    @Override
    public void deleteRealm(SecurityContext context, Realm realm) {
        realmStores.remove(realm.getId());
        configuredRealms.remove(realm.getId());
        for (IdentityStoreConfiguration config : configs) {
            for (Map.Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> entry : this.identityConfigMap.entrySet()) {
                if (entry.getKey().isInstance(config) && PartitionStore.class.isAssignableFrom(entry.getValue())) {
                    PartitionStore store = getPartitionStore(context, config);
                    store.removePartition(context, realm);
               }
            }
        }
    }

    @Override
    public Realm findRealm(SecurityContext context, String id) {
        Realm realm = getRealm(id);
        if (realm != null) return realm;
        for (IdentityStoreConfiguration config : configs) {
            for (Map.Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> entry : this.identityConfigMap.entrySet()) {
                if (entry.getKey().isInstance(config) && PartitionStore.class.isAssignableFrom(entry.getValue())) {
                    PartitionStore store = getPartitionStore(context, config);
                    Partition partition = store.findPartition(context, id);
                    if (partition instanceof Realm) {
                        realm = (Realm) partition;
                        configuredRealms.put(id, realm);
                        getConfigs(realmStores, id).add(config);
                        return realm;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Tier getTier(String id) {
        if (configuredTiers.containsKey(id)) {
            return configuredTiers.get(id);
        } else if (tierStores.containsKey(id)) {
            Tier tier = new Tier(id);
            configuredTiers.put(id, tier);
            return tier;
        } else {
            return null;
        }
    }

    @Override
    public Tier findTier(SecurityContext context, String id) {
        Tier tier = getTier(id);
        if (tier != null) return null;
        for (IdentityStoreConfiguration config : configs) {
            for (Map.Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> entry : this.identityConfigMap.entrySet()) {
                if (entry.getKey().isInstance(config) && PartitionStore.class.isAssignableFrom(entry.getValue())) {
                    PartitionStore store = getPartitionStore(context, config);
                    Partition partition = store.findPartition(context, id);
                    if (partition instanceof Tier) {
                        tier = (Tier) partition;
                        configuredTiers.put(id, tier);
                        getConfigs(tierStores, id).add(config);
                        return tier;
                    }
                }
            }
        }
        return null;
    }

    private PartitionStore getPartitionStore(SecurityContext context, IdentityStoreConfiguration config) {
        IdentityStore identityStore = createIdentityStore(config, null);
        PartitionStore store = (PartitionStore) identityStore;
        for (ContextInitializer initializer : config.getContextInitializers()) {
            initializer.initContextForStore(context, identityStore);
        }
        return store;
    }

    @Override
    public Tier createTier(SecurityContext context, String id) {
        Tier tier = getTier(id);
        if (tier != null) return null;
        IdentityStoreConfiguration config = getPartitionStoreConfig();
        // todo jboss logging-ise this
        if (config == null) throw new RuntimeException("failed to find a store that can handle creation of partitions");
        PartitionStore store = getPartitionStore(context, config);
        tier = new Tier(id);
        store.createPartition(context, tier);
        configuredTiers.put(id, tier);
        getConfigs(tierStores, id).add(config);
        return tier;
    }

    protected IdentityStoreConfiguration getPartitionStoreConfig() {
        for (IdentityStoreConfiguration config : configs) {
            for (Map.Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> entry : this.identityConfigMap.entrySet()) {
                if (entry.getKey().isInstance(config) && PartitionStore.class.isAssignableFrom(entry.getValue())) {
                    return config;
                }
            }
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityStoreConfiguration> IdentityStore<T> createIdentityStore(T config, SecurityContext context) {
        for (Class<? extends IdentityStoreConfiguration> cc : this.identityConfigMap.keySet()) {
            if (cc.isInstance(config)) {
                IdentityStore<T> identityStore = (IdentityStore<T>) this.storesCache.get(cc);
                return identityStore;
            }
        }
        throw MESSAGES.storeConfigUnsupportedConfiguration(config);
    }

    @Override
    public boolean isFeatureSupported(Partition partition, FeatureGroup feature, FeatureOperation operation,
                                      Class<? extends Relationship> relationshipClass) {
        return lookupConfigForFeature(partition, feature, operation, relationshipClass) != null;
    }

    @Override
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature, FeatureOperation operation) {
        return getStoreForFeature(context, feature, operation, null);
    }

    @Override
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature, FeatureOperation operation,
                                               Class<? extends Relationship> relationshipClass) {
        if (Realm.class.isInstance(context.getPartition())) {
            Realm realm = (Realm) context.getPartition();
            if (!realmStores.containsKey(realm.getId())) {
                LOGGER.identityManagerRealmNotConfigured(realm.getId());
                throw MESSAGES.storeConfigRealmNotConfigured(realm.getId());
            }
        } else if (Tier.class.isInstance(context.getPartition())) {
            Tier tier = (Tier) context.getPartition();
            if (!tierStores.containsKey(tier.getId())) {
                LOGGER.identityManagerTierNotConfigured(tier.getId());
                throw MESSAGES.storeConfigTierNotConfigured(tier.getId());
            }
        }

        IdentityStoreConfiguration config = lookupConfigForFeature(context.getPartition(), feature, operation,
                relationshipClass);

        final IdentityStore<? extends IdentityStoreConfiguration> store = createIdentityStore(config, context);

        for (ContextInitializer initializer : config.getContextInitializers()) {
            initializer.initContextForStore(context, store);
        }

        LOGGER.debugf("Performing operation [%s.%s] on IdentityStore [%s] using Partition [%s]", feature, operation, store,
                context.getPartition());

        return store;
    }

    private IdentityStoreConfiguration lookupConfigForFeature(Partition partition, FeatureGroup feature,
                                                              FeatureOperation operation, Class<? extends Relationship> relationshipClass) {

        Set<IdentityStoreConfiguration> configs = null;

        if (Realm.class.isInstance(partition)) {
            configs = realmStores.get(partition.getId());
        } else if (Tier.class.isInstance(partition)) {
            configs = tierStores.get(partition.getId());
        }

        IdentityStoreConfiguration config = null;

        if (configs != null) {
            boolean isUnsupportedRelationship = false;

            for (IdentityStoreConfiguration cfg : configs) {
                if (relationshipClass != null) {
                    if (cfg.supportsRelationship(relationshipClass, null)) {
                        if (cfg.supportsRelationship(relationshipClass, operation)) {
                            config = cfg;
                            break;
                        }
                    } else {
                        isUnsupportedRelationship = true;
                    }
                } else if (cfg.supportsFeature(feature, operation)) {
                    config = cfg;
                    break;
                }
            }

            if (config == null) {
                LOGGER.identityManagerUnsupportedOperation(feature, operation);

                if (isUnsupportedRelationship) {
                    throw MESSAGES.storeConfigUnsupportedRelationshipType(relationshipClass);
                } else {
                    throw MESSAGES.storeConfigUnsupportedOperation(feature, operation, feature, operation);
                }
            }
        }

        return config;
    }

    private Set<IdentityStoreConfiguration> getConfigs(Map<String, Set<IdentityStoreConfiguration>> stores, String key) {
        if (stores.containsKey(key)) {
            return stores.get(key);
        } else {
            Set<IdentityStoreConfiguration> configs = new HashSet<IdentityStoreConfiguration>();
            stores.put(key, configs);
            return configs;
        }
    }

}
