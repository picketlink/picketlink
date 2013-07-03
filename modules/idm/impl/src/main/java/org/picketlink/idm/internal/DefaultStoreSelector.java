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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoresConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreSelector;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * Default StoreFactory implementation. This factory is pre-configured to be able to create instances of the following built-in
 * IdentityStore implementations based on the corresponding IdentityStoreConfiguration:
 * <p/>
 * JPAIdentityStore - JPAIdentityStoreConfiguration
 * LDAPIdentityStore - LDAPConfiguration
 * FileBasedIdentityStore - FileIdentityStoreConfiguration
 *
 * @author Shane Bryzak
 */
public class DefaultStoreSelector implements StoreSelector {

    private final Map<Class<? extends IdentityStore>, IdentityStore> storesCache;
    private final IdentityStoresConfiguration storesConfiguration;

    public DefaultStoreSelector(IdentityStoresConfiguration storesConfiguration) {
        this.storesConfiguration = storesConfiguration;
        this.storesCache = new ConcurrentHashMap<Class<? extends IdentityStore>, IdentityStore>();
    }

    @Override
    public <T extends IdentityStore<?>> T getStoreForIdentityOperation(Class<T> storeType, Partition partition, Class<? extends AttributedType> type, IdentityOperation operation) {
        T identityStore = null;

        IdentityStoreConfiguration selectedConfig = this.storesConfiguration.forType(type, operation);

        if (selectedConfig == null) {
            throw new IdentityManagementException("No store configuration found for type [" + type + "] and operation [" + operation + "].");
        }

        return createStore(storeType, selectedConfig);
    }

    @Override
    public IdentityStore<?> getStoreForCredentialOperation(Class<?> credentialClass, Partition partition) {
        return null;  //TODO: Implement getStoreForCredentialOperation
    }

    @Override
    public IdentityStore<?> getStoreForRelationshipOperation(Class<? extends Relationship> relationshipClass, Set<Partition> partitions) {
        return null;  //TODO: Implement getStoreForRelationshipOperation
    }

    @Override
    public PartitionStore<?> getStoreForPartitionOperation() {
        return null;  //TODO: Implement getStoreForPartitionOperation
    }

    private <T extends IdentityStore> T createStore(
            Class<T> expectedType,
            IdentityStoreConfiguration selectedConfig) {
        Class<T> storeType = getStoreType(expectedType, selectedConfig);

        T identityStore = (T) this.storesCache.get(storeType);

        if (identityStore == null) {
            try {
                identityStore = (T) storeType.newInstance();
                identityStore.setup(selectedConfig);
                this.storesCache.put(storeType, identityStore);
            } catch (Exception e) {
                throw new IdentityManagementException("Could not instantiate store type [" + storeType + "].", e);
            }
        }

        return identityStore;
    }

    private <T extends IdentityStore> Class<T> getStoreType(
            Class<T> expectedType,
            IdentityStoreConfiguration selectedConfig) {
        Class<T> storeType =
                (Class<T>) this.storesConfiguration.getIdentityStores().get(selectedConfig.getClass()); // this is safe

        if (storeType == null) {
            // let's check the built-in store types
            if (FileIdentityStoreConfiguration.class.equals(selectedConfig.getClass())) {
                storeType = (Class<T>) FileBasedIdentityStore.class;
            }
        }

        if (storeType == null) {
            throw new IdentityManagementException("No store type provided for configuration [" + selectedConfig + "].");
        }

        if (!expectedType.isAssignableFrom(storeType)) {
            throw new IdentityManagementException("Store type [" + storeType + "] is not assignable from expected type [" + expectedType + ".");
        }

        return storeType;
    }

}
