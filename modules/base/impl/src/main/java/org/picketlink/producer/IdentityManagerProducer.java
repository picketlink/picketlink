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

package org.picketlink.producer;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.PartitionManagerCreateEvent;
import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.PermissionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.permission.acl.spi.PermissionHandler;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.IdentityStoreAutoConfiguration;
import org.picketlink.internal.PicketLinkExtension;
import org.picketlink.internal.SecuredIdentityManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.picketlink.BaseLog.ROOT_LOGGER;

/**
 * <p>This bean is responsible for initializing the PicketLink IDM subsystem as well produce some core components
 * such as:</p>
 *
 * <ul>
 * <li>An application scoped {@link PartitionManager}.</li>
 * <li>A request scoped {@link IdentityManager}.</li>
 * <li>A request scoped {@link RelationshipManager}.</li>
 * </ul>
 *
 * <p>The configuration is built depending on the existence of any {@link IdentityConfiguration} produced by the
 * application. If any configuration is found, it will be used. Otherwise the default configuration will be used.</p>
 *
 * <p>It's also possible to observe a specific event during the startup of the PicketLink IDM subsystem. In such
 * situations the application can provide any additional information as a last attempt before the subsystem is fully
 * initialized. See {@link IdentityConfigurationEvent}.
 * </p>
 *
 * <p>The default configuration is provided by the {@link IdentityStoreAutoConfiguration} bean, only if no specific
 * configuration is provided by the application.</p>
 *
 * <p>After the creation of the {@link PartitionManager} an {@link PartitionManagerCreateEvent} is fired to perform
 * any initialization before starting producing partition manager instances. Usually, the initialization will perform
 * validations against the stored state, create default partitions, etc. If no partition was created during the
 * initialization a default partition is always created if any of the provided configuration supports that.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
@ApplicationScoped
public class IdentityManagerProducer {

    @Inject
    private Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    private Instance<PermissionHandler> permissionHandlerInstance;

    @Inject
    @PicketLink
    private Instance<PartitionManager> partitionManagerInstance;

    @Inject
    private CDIEventBridge eventBridge;

    @Inject
    private IdentityStoreAutoConfiguration autoConfig;

    @Inject
    private PicketLinkExtension picketLinkExtension;

    @Inject
    @PicketLink
    private Instance<Partition> defaultPartition;

    private PartitionManager partitionManager;

    @Inject
    public void init() {
        ROOT_LOGGER.picketlinkBootstrap();

        if (isPartitionManagerProduced()) {
            this.partitionManager = this.partitionManagerInstance.get();
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("PartitionManager provided by the application.");
            }
        } else {
            this.partitionManager = createEmbeddedPartitionManager();
        }
    }

    @Produces
    public PartitionManager createPartitionManager() {
        return partitionManager;
    }

    /**
     * <p>{@link IdentityManager} instances are produced accordingly to the current {@link Partition} in use. If no
     * partition is provided, the default partition will be used.</p>
     *
     * @return
     */
    @Produces
    @RequestScoped
    public IdentityManager createIdentityManager() {
        if (defaultPartition.isUnsatisfied() || defaultPartition.get() == null) {
            return new SecuredIdentityManager(this.partitionManager.createIdentityManager());
        }

        return new SecuredIdentityManager(this.partitionManager.createIdentityManager(defaultPartition.get()));
    }

    @Produces
    @RequestScoped
    public RelationshipManager createRelationshipManager() {
        return this.partitionManager.createRelationshipManager();
    }

    @Produces
    @RequestScoped
    public PermissionManager createPermissionManager() {
        return this.partitionManager.createPermissionManager();
    }

    private boolean isPartitionManagerProduced() {
        return !this.partitionManagerInstance.isUnsatisfied();
    }

    private IdentityConfigurationBuilder createIdentityConfigurationBuilder() {
        IdentityConfigurationBuilder builder;
        List<IdentityConfiguration> configurations = getIdentityConfiguration();

        if (configurations.isEmpty()) {
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("IdentityConfiguration not provided by the application, creating a default IdentityConfigurationBuilder.");
            }

            builder = new IdentityConfigurationBuilder();
        } else {
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Found IdentityConfiguration from the environment. Creating a IdentityConfigurationBuilder with them.");
            }

            builder = new IdentityConfigurationBuilder(configurations);
        }

        this.eventBridge.fireEvent(new IdentityConfigurationEvent(builder));

        if (!builder.isConfigured()) {
            configureDefaults(builder);
        }

        return builder;
    }

    /**
     * <p>Returns all configurations produced by the application.</p>
     *
     * @return
     */
    private List<IdentityConfiguration> getIdentityConfiguration() {
        List<IdentityConfiguration> configurations = new ArrayList<IdentityConfiguration>();

        if (!this.identityConfigInstance.isUnsatisfied()) {
            for (Iterator<IdentityConfiguration> iterator = this.identityConfigInstance.iterator(); iterator.hasNext(); ) {
                configurations.add(iterator.next());
            }
        } else {
            configurations.addAll(this.picketLinkExtension.getSecurityConfiguration().getIdentityConfigurations());
        }

        return configurations;
    }

    private void configureDefaults(IdentityConfigurationBuilder builder) {
        if (ROOT_LOGGER.isDebugEnabled()) {
            ROOT_LOGGER.debugf("No configuration provided by the application. Configuring defaults.");
        }

        this.autoConfig.configure(builder);
    }

    private PartitionManager createEmbeddedPartitionManager() {
        if (ROOT_LOGGER.isDebugEnabled()) {
            ROOT_LOGGER.debugf("Creating PartitionManager.");
        }

        IdentityConfigurationBuilder builder = createIdentityConfigurationBuilder();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll(), this.eventBridge, getPermissionHandlers());

        this.eventBridge.fireEvent(new PartitionManagerCreateEvent(partitionManager));

        createDefaultPartition(partitionManager);

        return partitionManager;
    }

    private List<PermissionHandler> getPermissionHandlers() {
        List<PermissionHandler> permissionHandlers = new ArrayList<PermissionHandler>();

        if (!this.permissionHandlerInstance.isUnsatisfied()) {
            for (Iterator<PermissionHandler> iterator = this.permissionHandlerInstance.iterator(); iterator.hasNext(); ) {
                permissionHandlers.add(iterator.next());
            }
        }

        return permissionHandlers;
    }

    private void createDefaultPartition(PartitionManager partitionManager) {
        Realm defaultPartition = null;

        if (isPartitionSupported(partitionManager)) {
            if (partitionManager.getPartitions(Partition.class).isEmpty()) {
                if (ROOT_LOGGER.isDebugEnabled()) {
                    ROOT_LOGGER.debugf("Creating default partition using [%s] and name [%s].", Realm.class, Realm.DEFAULT_REALM);
                }

                defaultPartition = new Realm(Realm.DEFAULT_REALM);

                partitionManager.add(defaultPartition);
            } else {
                if (ROOT_LOGGER.isDebugEnabled()) {
                    ROOT_LOGGER.debugf("Found existing partitions. The default partition was not created.");
                }
            }
        }

        if (defaultPartition == null) {
            ROOT_LOGGER.warn("No default partition was created. You may want to create one before start managing your identity types.");
        }
    }

    private boolean isPartitionSupported(final PartitionManager partitionManager) {
        for (IdentityConfiguration configuration : partitionManager.getConfigurations()) {
            if (configuration.supportsPartition()) {
                for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                    if (storeConfig.supportsType(Realm.class, IdentityStoreConfiguration.IdentityOperation.create)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
