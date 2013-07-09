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
import org.picketlink.idm.spi.RelationshipPolicy;
import static java.util.Collections.unmodifiableList;

/**
 * <p>Consolidates all the configuration that should be used to initialize and start the IDM subsystem.</p>
 *
 * @author Shane Bryzak
 *
 */
public class IdentityConfiguration {

    private final String name;
    private final List<? extends IdentityStoreConfiguration> storeConfiguration;
    private final RelationshipPolicy relationshipPolicy;

    IdentityConfiguration(String name, List<? extends IdentityStoreConfiguration> identityStores, RelationshipPolicy relationshipPolicy) {
        if (name == null) {
            throw new SecurityConfigurationException("You must specify a name for the IdentityConfiguration.");
        }

        this.name = name;
        this.storeConfiguration = unmodifiableList(identityStores);
        this.relationshipPolicy = relationshipPolicy;
    }

    public RelationshipPolicy getRelationshipPolicy() {
        return relationshipPolicy;
    }

    public String getName() {
        return this.name;
    }

    public List<? extends IdentityStoreConfiguration> getStoreConfiguration() {
        return this.storeConfiguration;
    }

    public boolean supportsPartition() {
        for (IdentityStoreConfiguration storeConfiguration: getStoreConfiguration()) {
            if (storeConfiguration.supportsPartition()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityConfiguration other = (IdentityConfiguration) obj;

        return getName() != null && other.getName() != null && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}