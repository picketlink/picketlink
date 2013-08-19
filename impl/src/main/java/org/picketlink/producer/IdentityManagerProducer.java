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
import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.IdentityStoreAutoConfiguration;
import org.picketlink.internal.SecuredIdentityManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.picketlink.idm.config.IdentityStoreConfiguration.*;

/**
 * <p>This bean is responsible for initializing the PicketLink IDM subsystem as well produce some core components
 * such as:</p>
 *
 * <ul>
 *     <li>An application scoped {@link PartitionManager}.</li>
 *     <li>A request scoped {@link IdentityManager}.</li>
 *     <li>A request scoped {@link RelationshipManager}.</li>
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
 * <p>After the creation of the {@link PartitionManager} a default partition is always created if any of the provided
 * configuration supports that. This is very useful for most use cases where only a single partition is necessary.</p>
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {

    @Inject
    private Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    private Event<IdentityConfigurationEvent> identityConfigEvent;

    @Inject
    private CDIEventBridge eventBridge;

    @Inject
    private IdentityStoreAutoConfiguration autoConfig;

    @Inject
    @PicketLink
    private Instance<Partition> defaultPartition;

    private PartitionManager partitionManager;

    @Inject
    public void init() {
        IdentityConfigurationBuilder builder = createIdentityConfigurationBuilder();

        List<IdentityConfiguration> configurations = builder.buildAll();

        this.partitionManager = new DefaultPartitionManager(configurations, this.eventBridge);

        if (isPartitionSupported(configurations)) {
            createDefaultPartition(this.partitionManager);
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
        } else {
            return new SecuredIdentityManager(this.partitionManager.createIdentityManager(defaultPartition.get()));
        }
    }

    @Produces
    @RequestScoped
    public RelationshipManager createRelationshipManager() {
        return this.partitionManager.createRelationshipManager();
    }

    private boolean isPartitionSupported(final List<IdentityConfiguration> configurations) {
        for (IdentityConfiguration configuration : configurations) {
            for (IdentityStoreConfiguration storeConfig : configuration.getStoreConfiguration()) {
                if (storeConfig.supportsPartition()
                        && storeConfig.supportsType(Realm.class, IdentityOperation.create)) {
                    return true;
                }
            }
        }

        return false;
    }

    private IdentityConfigurationBuilder createIdentityConfigurationBuilder() {
        IdentityConfigurationBuilder builder;
        List<IdentityConfiguration> configurations = getIdentityConfiguration();

        if (configurations.isEmpty()) {
            builder = new IdentityConfigurationBuilder();
        } else {
            builder = new IdentityConfigurationBuilder(configurations);
        }

        this.identityConfigEvent.fire(new IdentityConfigurationEvent(builder));

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
            for (Iterator<IdentityConfiguration> iterator = this.identityConfigInstance.iterator(); iterator.hasNext();) {
                configurations.add(iterator.next());
            }
        }

        return configurations;
    }

    private void configureDefaults(IdentityConfigurationBuilder builder) {
        this.autoConfig.configure(builder);
    }

    private void createDefaultPartition(PartitionManager partitionManager) {
        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }
    }

}
