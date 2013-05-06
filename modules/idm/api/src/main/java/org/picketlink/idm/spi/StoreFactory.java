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

package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Tier;

/**
 * Creates IdentityStore instances based on a provided configuration
 *
 * @author Shane Bryzak
 *
 */
public interface StoreFactory {
    /**
     * Creates an instance of an IdentityStore using the provided configuration
     *
     * @param config
     * @return
     */
    <T extends IdentityStoreConfiguration> IdentityStore<T> createIdentityStore(T config, SecurityContext context);

    /**
     * Maps specific implementations of IdentityStoreConfiguration to a corresponding
     * IdentityStore implementation.
     *
     * @param configClass
     * @param storeClass
     */
    void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore<?>> storeClass);

    /**
     *
     * @param id
     * @return
     */
    Realm getRealm(String id);

    /**
     *
     * @param id
     * @return
     */
    Tier getTier(String id);

    /**
     * Returns true if the specified feature (and optional relationship class) is supported by the specified
     * Partition's configuration
     *
     * @param partition
     * @param feature
     * @param operation
     * @param relationshipClass
     * @return
     */
    boolean isFeatureSupported(Partition partition, FeatureGroup feature, FeatureOperation operation,
            Class<? extends Relationship> relationshipClass);

    /**
     *
     * @param context
     * @param feature
     * @param operation
     * @return
     */
    IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature,
            FeatureOperation operation);

    /**
     *
     * @param context
     * @param feature
     * @param operation
     * @param relationshipClass
     * @return
     */
    IdentityStore<?> getStoreForFeature(SecurityContext context, FeatureGroup feature,
            FeatureOperation operation, Class<? extends Relationship> relationshipClass);
}
