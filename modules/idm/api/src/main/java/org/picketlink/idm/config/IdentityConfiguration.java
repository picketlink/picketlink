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

import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContextFactory;
import org.picketlink.idm.spi.StoreFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Defines the runtime configuration for Identity Management.
 * </p>
 * <p>
 * You should use this class to provide all necessary configuration for the identity stores that should be supported by the
 * IdentityManager.
 * </p>
 *
 * @author Shane Bryzak
 *
 */
public class IdentityConfiguration {

    private final List<IdentityStoreConfiguration> configuredStores = new ArrayList<IdentityStoreConfiguration>();
    private final SecurityContextFactory securityContextFactory;
    private final StoreFactory storeFactory;
    private final Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> additionalIdentityStores;

    IdentityConfiguration(List<IdentityStoreConfiguration> storesConfiguration, StoreFactory storeFactory,
            SecurityContextFactory securityContextFactory,
            Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> additionalIdentityStores) {
        this.configuredStores.addAll(storesConfiguration);
        this.additionalIdentityStores = additionalIdentityStores;
        this.storeFactory = storeFactory;
        this.securityContextFactory = securityContextFactory;
    }

    /**
     * <p>
     * Returns all registered {@link IdentityStoreConfiguration} instances.
     * </p>
     *
     * @return
     */
    public List<IdentityStoreConfiguration> getConfiguredStores() {
        return Collections.unmodifiableList(this.configuredStores);
    }

    /**
     * <p>
     * Returns any additional mapping for identity stores.
     * </p>
     *
     * @return
     */
    public Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> getAdditionalIdentityStores() {
        return Collections.unmodifiableMap(this.additionalIdentityStores);
    }

    public StoreFactory getStoreFactory() {
        return this.storeFactory;
    }

    public SecurityContextFactory getSecurityContextFactory() {
        return this.securityContextFactory;
    }

}