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
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.EEJPAContextInitializer;
import org.picketlink.internal.IdentityStoreAutoConfiguration;
import org.picketlink.internal.SecuredIdentityManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.picketlink.idm.config.IdentityStoreConfiguration.*;

/**
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";

    @Inject
    private Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    private Event<IdentityConfigurationEvent> identityConfigEvent;

    @Inject
    private EEJPAContextInitializer jpaContextInitializer;

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
        IdentityConfigurationBuilder builder;

        if (!identityConfigInstance.isUnsatisfied()) {
            IdentityConfiguration identityConfiguration = identityConfigInstance.get();
            builder = new IdentityConfigurationBuilder(Arrays.asList(identityConfiguration));
        } else if (identityConfigInstance.isAmbiguous()) {
            throw new SecurityConfigurationException("Multiple IdentityConfiguration beans found, can not "
                    + "configure IdentityManagerFactory");
        } else {
            builder = new IdentityConfigurationBuilder();
        }

        this.identityConfigEvent.fire(new IdentityConfigurationEvent(builder));

        if (!builder.isConfigured()) {
            loadAutoConfig(builder);
        }

        List<IdentityConfiguration> configurations = builder.buildAll();

        this.partitionManager = new DefaultPartitionManager(configurations, this.eventBridge);

        if (isPartitionSupported(configurations)) {
            if (this.partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
                this.partitionManager.add(new Realm(Realm.DEFAULT_REALM));
            }
        }
    }

    private void loadAutoConfig(IdentityConfigurationBuilder builder) {
        if (this.autoConfig.isConfigured()) {
            Class<?>[] entities = new Class[this.autoConfig.getEntities().size()];
            this.autoConfig.getEntities().toArray(entities);
            builder.named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                    .jpa()
                    .mappedEntity(entities)
                    .addContextInitializer(this.jpaContextInitializer)
                    .supportAllFeatures();
        } else {
            builder.named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                    .file()
                    .supportAllFeatures();
        }
    }

    @Produces
    public PartitionManager createPartitionManager() {
        return partitionManager;
    }

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

}
