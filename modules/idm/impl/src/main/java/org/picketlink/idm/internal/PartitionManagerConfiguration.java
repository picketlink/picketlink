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

import org.picketlink.idm.DefaultIdGenerator;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.permission.acl.spi.PermissionHandler;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.IDMLog.ROOT_LOGGER;

/**
 * @author pedroigor
 */
class PartitionManagerConfiguration {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";

    /**
     * A collection of all identity configurations.  Each configuration has a unique name.
     */
    private final Collection<IdentityConfiguration> configurations;

    /**
     * The IdentityConfiguration that is responsible for managing partition CRUD operations.  It is possible for this
     * value to be null, in which case partition management will not be supported.
     */
    private final IdentityConfiguration partitionManagementConfig;
    /**
     * The IdentityConfiguration that is responsible for managing attributes.  It is possible for this
     * value to be null, in which case attribute management will not be supported.
     */
    private final IdentityConfiguration attributeManagementConfig;
    private final EventBridge eventBridge;
    private final IdGenerator idGenerator;

    /**
     * Used for querying chained privileges
     */
    private PrivilegeChainQuery privilegeChainQuery = new PrivilegeChainQuery();

    /**
     * Permission handler policy
     */
    private PermissionHandlerPolicy permissionHandlerPolicy;


    public PartitionManagerConfiguration(Collection<IdentityConfiguration> configurations,
                                         Collection<PermissionHandler> permissionHandlers,
                                         EventBridge eventBridge, IdGenerator idGenerator) {
        ROOT_LOGGER.partitionManagerBootstrap();

        if (configurations == null || configurations.isEmpty()) {
            throw MESSAGES.configNoIdentityConfigurationProvided();
        }

        this.configurations = Collections.unmodifiableCollection(configurations);

        IdentityConfiguration partitionCfg = null;
        IdentityConfiguration attributeCfg = null;

        for (IdentityConfiguration config : configurations) {
            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                if (storeConfig.supportsPartition()) {
                    partitionCfg = config;
                }

                if (storeConfig.supportsAttribute()) {
                    attributeCfg = config;
                }
            }
        }

        // There may be no configuration that supports partition management, in which case the partitionManagementConfig
        // field will be null and partition management operations will not be supported
        this.partitionManagementConfig = partitionCfg;
        this.attributeManagementConfig = attributeCfg;

        this.permissionHandlerPolicy = new PermissionHandlerPolicy(null);

        if (permissionHandlers != null) {
            for (PermissionHandler handler : permissionHandlers) {
                this.permissionHandlerPolicy.registerHandler(handler);
            }
        }

        for (IdentityConfiguration config : configurations) {
            for (IdentityStoreConfiguration storeConfig : config.getStoreConfiguration()) {
                // Register all known relationship types so that the privilege chain query can determine inherited privileges
                for (Class<? extends AttributedType> supportedType : storeConfig.getSupportedTypes().keySet()) {
                    if (Relationship.class.isAssignableFrom(supportedType)) {
                        this.privilegeChainQuery.registerRelationshipType((Class<Relationship>) supportedType);
                    }
                }
            }
        }

        if (eventBridge == null) {
            this.eventBridge = new EventBridge() {
                public void raiseEvent(Object event) { /* no-op */}
            };
        } else {
            this.eventBridge = eventBridge;
        }

        if (idGenerator == null) {
            this.idGenerator = new DefaultIdGenerator();
        } else {
            this.idGenerator = idGenerator;
        }

        logConfiguration(this.configurations);
    }

    private void logConfiguration(final Collection<IdentityConfiguration> configurations) {
        for (IdentityConfiguration identityConfiguration : configurations) {
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debug("  Identity Management Configuration: [");
                ROOT_LOGGER.debugf("    Name: %s", identityConfiguration.getName());
                ROOT_LOGGER.debugf("    Identity Store Configuration: %s", identityConfiguration.getStoreConfiguration());
                ROOT_LOGGER.debugf("    Supports Partition: %s", this.partitionManagementConfig != null && this.partitionManagementConfig.equals(identityConfiguration));
                ROOT_LOGGER.debugf("    Supports Attribute: %s", this.attributeManagementConfig != null && this.attributeManagementConfig.equals(identityConfiguration));
                ROOT_LOGGER.debugf("    Supports Credential: %s", identityConfiguration.supportsCredential());
                ROOT_LOGGER.debugf("    Supports Permission: %s", identityConfiguration.supportsPermission());

                List<Class<?>> supportedTypes = new ArrayList<Class<?>>();

                for (IdentityStoreConfiguration storeConfiguration : identityConfiguration.getStoreConfiguration()) {
                    supportedTypes.addAll(storeConfiguration.getSupportedTypes().keySet());
                }

                ROOT_LOGGER.debugf("    Supported Types: %s", supportedTypes);
                ROOT_LOGGER.debug("  ]");
            }
        }
    }

    IdentityConfiguration getConfigurationByName(String name) {
        for (IdentityConfiguration config : this.configurations) {
            if (name.equals(config.getName())) {
                return config;
            }
        }

        throw MESSAGES.partitionNoConfigurationFound(name);
    }

    String getDefaultConfigurationName() {
        // If there is a configuration with the default configuration name, return that name
        for (IdentityConfiguration config : configurations) {
            if (DEFAULT_CONFIGURATION_NAME.equals(config.getName())) {
                return DEFAULT_CONFIGURATION_NAME;
            }
        }

        // Otherwise return the first configuration found
        return configurations.iterator().next().getName();
    }

    boolean supportsPartition() {
        return this.partitionManagementConfig != null;
    }

    Collection<IdentityConfiguration> getConfigurations() {
        return this.configurations;
    }

    public IdentityConfiguration getPartitionManagementConfig() {
        return this.partitionManagementConfig;
    }

    public IdentityConfiguration getAttributeManagementConfig() {
        return this.attributeManagementConfig;
    }

    public PermissionHandlerPolicy getPermissionHandlerPolicy() {
        return this.permissionHandlerPolicy;
    }

    public PrivilegeChainQuery getPrivilegeChainQuery() {
        return this.privilegeChainQuery;
    }

    public EventBridge getEventBridge() {
        return this.eventBridge;
    }

    public IdGenerator getIdGenerator() {
        return this.idGenerator;
    }
}
