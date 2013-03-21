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
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.internal.EEJPAContextInitializer;
import org.picketlink.internal.EESecurityContextFactory;
import org.picketlink.internal.SecuredIdentityManager;


/**
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {
    private IdentityConfiguration identityConfig;

    private IdentityManagerFactory factory;

    @Inject Event<IdentityConfigurationEvent> identityConfigEvent;

    @Inject EESecurityContextFactory icf;

    @Inject EEJPAContextInitializer jpaContextInitializer;

    @Inject
    public void init() {
        identityConfig = new IdentityConfiguration();
        identityConfig.addContextInitializer(jpaContextInitializer);

        identityConfigEvent.fire(new IdentityConfigurationEvent(identityConfig));

        factory = new DefaultIdentityManagerFactory(identityConfig, icf);
    }

    @Produces 
    public IdentityManager createIdentityManager() {
        return new SecuredIdentityManager(factory.createIdentityManager());
    }

}
