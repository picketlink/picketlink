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

import org.picketlink.idm.spi.RelationshipPolicy;

/**
 * <p>Consolidates all the configuration that should be used to initialize and start the IDM subsystem.</p>
 *
 * @author Shane Bryzak
 *
 */
public class IdentityConfiguration {

    private final String name;
    private final IdentityStoresConfiguration storesConfiguration;
    private final RelationshipPolicy relationshipPolicy;

    IdentityConfiguration(String name, IdentityStoresConfiguration storesConfiguration) {
        if (name == null) {
            throw new SecurityConfigurationException("You must specify a name for the IdentityConfiguration.");
        }

        this.name = name;
        this.storesConfiguration = storesConfiguration;
        this.relationshipPolicy = null;
    }

    public RelationshipPolicy getRelationshipPolicy() {
        return relationshipPolicy;
    }

    public String getName() {
        return this.name;
    }

    public IdentityStoresConfiguration getStoresConfiguration() {
        return this.storesConfiguration;
    }

    public boolean supportsPartition() {
        for (IdentityStoreConfiguration storeConfiguration: getStoresConfiguration().getConfigurations()) {
            if (storeConfiguration.supportsPartition()) {
                return true;
            }
        }

        return false;
    }
}