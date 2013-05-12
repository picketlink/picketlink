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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.internal.EEJPAContextInitializer;
import org.picketlink.internal.EESecurityContextFactory;
import org.picketlink.internal.IdentityStoreAutoConfiguration;
import org.picketlink.internal.SecuredIdentityManager;

/**
 * 
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {

    @Inject
    Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    Event<IdentityConfigurationEvent> identityConfigEvent;

    @Inject
    EESecurityContextFactory icf;

    @Inject
    EEJPAContextInitializer jpaContextInitializer;

    @Inject
    IdentityStoreAutoConfiguration autoConfig;

    private IdentityManagerFactory factory;

    @Inject
    public void init() {
        IdentityConfigurationBuilder builder;

        if (!identityConfigInstance.isUnsatisfied()) {
            IdentityConfiguration identityConfiguration = identityConfigInstance.get();
            builder = new IdentityConfigurationBuilder(identityConfiguration);
        } else if (identityConfigInstance.isAmbiguous()) {
            throw new SecurityConfigurationException("Multiple IdentityConfiguration beans found, can not "
                    + "configure IdentityManagerFactory");
        } else {
            builder = new IdentityConfigurationBuilder();
        }

        this.identityConfigEvent.fire(new IdentityConfigurationEvent(builder));

        if (builder.stores().isEmpty()) {
            loadAutoConfig(builder);
        } else {
            if (builder.stores().isConfigured(JPAIdentityStoreConfiguration.class)) {
                builder.stores().jpa().addContextInitializer(this.jpaContextInitializer);
            }
        }

        builder.contextFactory(this.icf);

        this.factory = new DefaultIdentityManagerFactory(builder.build());
    }

    private void loadAutoConfig(IdentityConfigurationBuilder builder) {
        if (this.autoConfig.isConfigured()) {
            builder.stores().jpa().readFrom(this.autoConfig.getJPAConfiguration().create());
        } else {
            builder.stores().file().supportAllFeatures();
        }
    }

    @Produces
    @Dependent
    public IdentityManager createIdentityManager() {
        return new SecuredIdentityManager(this.factory.createIdentityManager());
    }

}
