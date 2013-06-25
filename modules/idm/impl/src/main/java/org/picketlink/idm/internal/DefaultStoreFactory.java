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
import org.picketlink.idm.config.JPAIdentityStoreConfigurationOld;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityStore;
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

    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    private Map<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>> storesCache = new HashMap<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>>();

    private Map<String, Set<IdentityStoreConfiguration>> realmStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Realm> configuredRealms = new HashMap<String, Realm>();

    private Map<String, Set<IdentityStoreConfiguration>> tierStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    private Map<String, Tier> configuredTiers = new HashMap<String, Tier>();

    public DefaultStoreFactory(IdentityConfiguration identityConfig) {
        this.identityConfigMap.put(JPAIdentityStoreConfigurationOld.class, JPAIdentityStore.class);
        this.identityConfigMap.put(LDAPIdentityStoreConfiguration.class, LDAPIdentityStore.class);
        this.identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);

        Map<FeatureGroup, IdentityStoreConfiguration> supportedFeatures = new HashMap<FeatureGroup, IdentityStoreConfiguration>();
        Map<Class<? extends IdentityType>, IdentityStoreConfiguration> supportedIdentityTypes = new HashMap<Class<? extends IdentityType>, IdentityStoreConfiguration>();
        Map<Class<? extends Relationship>, IdentityStoreConfiguration> supportedRelationships = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();

        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            LOGGER.identityManagerInitConfigForRealms(config, config.getRealms());

            // let's check for duplicated features
            for (Entry<FeatureGroup, Set<FeatureOperation>> entry : config.getSupportedFeatures().entrySet()) {
                FeatureGroup feature = entry.getKey();

                IdentityStoreConfiguration storeConfigForFeature = supportedFeatures.get(feature);

                // attributes can be stored for each store.
                if (!FeatureGroup.attribute.equals(feature)) {
                    if (storeConfigForFeature == null) {
                        supportedFeatures.put(feature, config);
                    } else {
                        throw MESSAGES.configurationAmbiguousFeatureForStore(feature, storeConfigForFeature, config);
                    }
                }
            }

            // let's check for duplicated identity types
            for (Entry<Class<? extends IdentityType>, Set<FeatureOperation>> entry : config.getSupportedIdentityTypes().entrySet()) {
                Class<? extends IdentityType> identityType = entry.getKey();

                IdentityStoreConfiguration storeConfigForFeature = supportedIdentityTypes.get(identityType);

                if (storeConfigForFeature == null) {
                    supportedIdentityTypes.put(identityType, config);
                } else {
                    throw MESSAGES.configurationAmbiguousIdentityTypeForStore(identityType, storeConfigForFeature, config);
                }
            }

            // let's check for duplicated relationship types
            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : config.getSupportedRelationships().entrySet()) {
                Class<? extends Relationship> relationship = entry.getKey();

                IdentityStoreConfiguration storeConfigForFeature = supportedRelationships.get(relationship);

                if (storeConfigForFeature == null) {
                    supportedRelationships.put(relationship, config);
                } else {
                    throw MESSAGES.configurationAmbiguousRelationshipForStore(relationship, storeConfigForFeature, config);
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
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature, FeatureOperation operation) {
        return getStoreForFeature(context, feature, operation, null);
    }

    @Override
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureOperation operation, Class<?> type) {
        return getStoreForFeature(context, null, operation, type);
    }

    private IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature, FeatureOperation operation, Class<?> identityType) {
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
                identityType);

        final IdentityStore<? extends IdentityStoreConfiguration> store = createIdentityStore(config, context);

        for (ContextInitializer initializer : config.getContextInitializers()) {
            initializer.initContextForStore(context, store);
        }

        LOGGER.debugf("Performing operation [%s.%s] on IdentityStore [%s] using Partition [%s]", feature, operation, store,
                context.getPartition());

        return store;
    }

    private IdentityStoreConfiguration lookupConfigForFeature(Partition partition, FeatureGroup feature,
                                                              FeatureOperation operation, Class<?> type) {

        Set<IdentityStoreConfiguration> configs = null;

        if (Realm.class.isInstance(partition)) {
            configs = realmStores.get(partition.getId());
        } else if (Tier.class.isInstance(partition)) {
            configs = tierStores.get(partition.getId());
        }

        Class<? extends Relationship> relationshipClass = null;
        Class<? extends IdentityType> identityTypeClass = null;

        if (type != null) {
            if (Relationship.class.isAssignableFrom(type)) {
                relationshipClass = (Class<? extends Relationship>) type;
            } else if (IdentityType.class.isAssignableFrom(type)) {
                identityTypeClass = (Class<? extends IdentityType>) type;
            }
        }

        IdentityStoreConfiguration config = null;

        if (configs != null) {
            for (IdentityStoreConfiguration cfg : configs) {
                if (relationshipClass != null) {
                    if (cfg.supportsRelationship(relationshipClass, null)) {
                        if (cfg.supportsRelationship(relationshipClass, operation)) {
                            return cfg;
                        }
                    }
                } else if (identityTypeClass != null) {
                    if (cfg.supportsIdentityType(identityTypeClass, null)) {
                        if (cfg.supportsIdentityType(identityTypeClass, operation)) {
                            return cfg;
                        }
                    }
                } else if (cfg.supportsFeature(feature, operation)) {
                    return cfg;
                }
            }

            LOGGER.identityManagerUnsupportedOperation(feature, operation);

            if (relationshipClass != null) {
                throw MESSAGES.storeConfigUnsupportedRelationshipType(relationshipClass, operation);
            } else if (identityTypeClass != null) {
                throw MESSAGES.storeConfigUnsupportedIdentityType(identityTypeClass, operation);
            } else {
                throw MESSAGES.storeConfigUnsupportedOperation(feature, operation, feature, operation);
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
