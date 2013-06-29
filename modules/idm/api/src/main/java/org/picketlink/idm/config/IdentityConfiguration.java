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

package org.picketlink.idm.config;

import java.util.List;
import java.util.Map;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * <p>Consolidates all the configuration that should be used to initialize and start the IDM subsystem.</p>
 *
 * @author Shane Bryzak
 *
 */
public class IdentityConfiguration {

    private final List<IdentityStoreConfiguration> configuredStores;
    private final StoreSelector storeFactory;
    private final Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> additionalIdentityStores;

    IdentityConfiguration(List<IdentityStoreConfiguration> storesConfiguration, StoreSelector storeFactory,
            Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> additionalIdentityStores) {
        this.configuredStores = unmodifiableList(storesConfiguration);
        this.additionalIdentityStores = unmodifiableMap(additionalIdentityStores);
        this.storeFactory = storeFactory;
    }

    /**
     * <p>
     * Returns all registered {@link IdentityStoreConfiguration} instances.
     * </p>
     *
     * @return
     */
    public List<IdentityStoreConfiguration> getConfiguredStores() {
        return this.configuredStores;
    }

    /**
     * <p>
     * Returns any additional mapping for identity stores.
     * </p>
     *
     * @return
     */
    public Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> getAdditionalIdentityStores() {
        return this.additionalIdentityStores;
    }

    public StoreSelector getStoreFactory() {
        return this.storeFactory;
    }

}