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
import java.util.Set;

import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default StoreFactory implementation. This factory is pre-configured to be able to create instances of the following built-in
 * IdentityStore implementations based on the corresponding IdentityStoreConfiguration:
 *
 * JPAIdentityStore - JPAIdentityStoreConfiguration
 * LDAPIdentityStore - LDAPConfiguration
 * FileBasedIdentityStore - FileIdentityStoreConfiguration
 *
 *
 * @author Shane Bryzak
 */
public class DefaultStoreFactory implements StoreFactory {

    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    private Map<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>> storesCache = new HashMap<Class<? extends IdentityStoreConfiguration>, IdentityStore<?>>();

    private Map<String, Set<IdentityStoreConfiguration>> realmStores = new HashMap<String, Set<IdentityStoreConfiguration>>();

    public DefaultStoreFactory(IdentityConfiguration identityConfig) {
        this.identityConfigMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        this.identityConfigMap.put(LDAPIdentityStoreConfiguration.class, LDAPIdentityStore.class);
        this.identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);

        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            LOGGER.identityManagerInitConfigForRealms(config, config.getRealms());

            config.init();

            for (String realm : config.getRealms()) {
                Set<IdentityStoreConfiguration> configs;

                if (this.realmStores.containsKey(realm)) {
                    configs = realmStores.get(realm);
                } else {
                    configs = new HashSet<IdentityStoreConfiguration>();
                    this.realmStores.put(realm, configs);
                }

                configs.add(config);
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
    public void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore<?>> storeClass) {
        this.identityConfigMap.put(configClass, (Class<? extends IdentityStore<?>>) storeClass);
    }

    @Override
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature,
            FeatureOperation operation) {
        return getStoreForFeature(context, feature, operation, null);
    }

    @Override
    public IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature,
            FeatureOperation operation, Class<? extends Relationship> relationshipClass) {
        String realmName = (context.getPartition() != null) ? context.getPartition().getName() : Realm.DEFAULT_REALM;

        if (!realmStores.containsKey(realmName)) {
            LOGGER.identityManagerRealmNotConfigured(realmName);
            throw MESSAGES.storeConfigRealmNotConfigured(realmName);
        }

        Set<IdentityStoreConfiguration> configs = realmStores.get(realmName);

        IdentityStoreConfiguration config = null;
        boolean supportedRelationshipClass = true;

        for (IdentityStoreConfiguration cfg : configs) {
            if (relationshipClass != null) {
                if (cfg.getFeatureSet().supportsRelationship(relationshipClass)) {
                    if (cfg.getFeatureSet().supportsRelationshipFeature(relationshipClass, operation)) {
                        config = cfg;
                        break;
                    }
                } else {
                    supportedRelationshipClass = false;
                }
            } else if (cfg.getFeatureSet().supports(feature, operation)) {
                config = cfg;
                break;
            }
        }

        if (config == null) {
            LOGGER.identityManagerUnsupportedOperation(feature, operation);

            if (!supportedRelationshipClass) {
                throw MESSAGES.storeConfigUnsupportedRelationshipType(relationshipClass);
            } else {
                throw MESSAGES.storeConfigUnsupportedOperation(feature, operation, feature, operation);
            }
        }

        final IdentityStore<? extends IdentityStoreConfiguration> store = createIdentityStore(config, context);

        LOGGER.debugf("Performing operation [%s.%s] on IdentityStore [%s] using Partition [%s]", feature, operation,
                store, context.getPartition());

        return store;
    }
}
