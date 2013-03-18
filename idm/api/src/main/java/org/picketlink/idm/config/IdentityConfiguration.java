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

import java.util.ArrayList;
import java.util.List;

import org.picketlink.idm.spi.ContextInitializer;

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
 */
public class IdentityConfiguration {

    private List<IdentityStoreConfiguration> configuredStores = new ArrayList<IdentityStoreConfiguration>();

    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();

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
     * Registers a {@link IdentityStoreConfiguration}.
     * </p>
     *
     * @param config
     */
    public void addStoreConfiguration(IdentityStoreConfiguration config) {
        this.configuredStores.add(config);
    }

    public List<ContextInitializer> getContextInitializers() {
        return contextInitializers;
    }

    public void addContextInitializer(ContextInitializer contextInitializer) {
        this.contextInitializers.add(contextInitializer);
    }
}
