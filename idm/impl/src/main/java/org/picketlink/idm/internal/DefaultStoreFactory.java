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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default StoreFactory implementation. This factory is pre-configured to be able to create instances of the following built-in
 * IdentityStore implementations based on the corresponding IdentityStoreConfiguration:
 *
 * JPAIdentityStore - JPAIdentityStoreConfiguration LDAPIdentityStore - LDAPConfiguration FileBasedIdentityStore -
 * FileIdentityStoreConfiguration
 *
 *
 * @author Shane Bryzak
 */
@SuppressWarnings("rawtypes")
public class DefaultStoreFactory implements StoreFactory {

    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    private Map<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>> storesCache = new HashMap<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>>();

    private Map<String, Set<IdentityStoreConfiguration>> realmStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Realm> configuredRealms = new HashMap<String, Realm>();

    private Map<String, Set<IdentityStoreConfiguration>> tierStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Tier> configuredTiers = new HashMap<String, Tier>();

    public DefaultStoreFactory(IdentityConfiguration identityConfig) {
        this.identityConfigMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        this.identityConfigMap.put(LDAPIdentityStoreConfiguration.class, LDAPIdentityStore.class);
        this.identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);

        Map<FeatureGroup, IdentityStoreConfiguration<?>> supportedFeatures = new HashMap<FeatureGroup, IdentityStoreConfiguration<?>>();

        for (IdentityStoreConfiguration<?> config : identityConfig.getConfiguredStores()) {
            LOGGER.identityManagerInitConfigForRealms(config, config.getRealms());

            Map<FeatureGroup, Set<FeatureOperation>> storeFeatures = config.getFeatureSet().getSupportedFeatures();

            // let's check for duplicated features
            for (Entry<FeatureGroup, Set<FeatureOperation>> entry : storeFeatures.entrySet()) {
                FeatureGroup feature = entry.getKey();
                IdentityStoreConfiguration<?> storeConfigForFeature = supportedFeatures.get(feature);

                if (storeConfigForFeature == null) {
                    supportedFeatures.put(feature, config);
                } else {
                    throw MESSAGES.configurationAmbiguosFeatureForStore(feature, storeConfigForFeature, config);
                }
            }

            config.init();

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
        }
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

    public Tier getTier(String id) {
        if (configuredTiers.containsKey(id)) {
            return configuredTiers.get(id);
        } else if (realmStores.containsKey(id)) {
            Tier tier = new Tier(id);
            configuredTiers.put(id, tier);
            return tier;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentityStoreConfiguration> IdentityStore<T> createIdentityStore(T config, SecurityContext context) {
        for (Class<? extends IdentityStoreConfiguration> cc : this.identityConfigMap.keySet()) {
            if (cc.isInstance(config)) {
                IdentityStore<T> identityStore = (IdentityStore<T>) this.storesCache.get(cc);

                if (identityStore == null) {
                    Class<? extends IdentityStore<?>> identityStoreClass = this.identityConfigMap.get(cc);

                    try {
                        identityStore = (IdentityStore<T>) identityStoreClass.newInstance();
                        identityStore.setup(config);
                    } catch (Exception e) {
                        throw MESSAGES.instantiationError(identityStoreClass.getName(), e);
                    }

                    this.storesCache.put(cc, identityStore);
                }

                return identityStore;
            }
        }

        throw MESSAGES.storeConfigUnsupportedConfiguration(config);
    }

    @Override
    public void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore<?>> storeClass) {
        this.identityConfigMap.put(configClass, (Class<? extends IdentityStore<?>>) storeClass);
    }

    @Override
    public boolean isFeatureSupported(Partition partition, FeatureGroup feature, FeatureOperation operation,
            Class<? extends Relationship> relationshipClass) {
        return lookupConfigForFeature(partition, feature, operation, relationshipClass) != null;
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
                    if (cfg.getFeatureSet().supportsRelationship(relationshipClass)) {
                        if (cfg.getFeatureSet().supportsRelationshipFeature(relationshipClass, operation)) {
                            config = cfg;
                            break;
                        }
                    } else {
                        isUnsupportedRelationship = true;
                    }
                } else if (cfg.getFeatureSet().supports(feature, operation)) {
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

        IdentityStoreConfiguration<?> config = lookupConfigForFeature(context.getPartition(), feature, operation,
                relationshipClass);

        final IdentityStore<? extends IdentityStoreConfiguration<?>> store = createIdentityStore(config, context);

        for (ContextInitializer initializer : config.getContextInitializers()) {
            initializer.initContextForStore(context, store);
        }

        LOGGER.debugf("Performing operation [%s.%s] on IdentityStore [%s] using Partition [%s]", feature, operation, store,
                context.getPartition());

        return store;
    }
}
