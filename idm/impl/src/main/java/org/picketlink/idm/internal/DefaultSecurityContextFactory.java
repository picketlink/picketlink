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

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.SecurityContextFactory;

/**
 * A default implementation of SecurityContextFactory.
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 */
public class DefaultSecurityContextFactory implements SecurityContextFactory {
    private EventBridge eventBridge;
    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;

    public static DefaultSecurityContextFactory DEFAULT = new DefaultSecurityContextFactory();

    public DefaultSecurityContextFactory(){
        this.eventBridge = new EventBridge() {

            @Override
            public void raiseEvent(Object event) {
                // by default do nothing
            }
        };

        this.credentialHandlerFactory = new DefaultCredentialHandlerFactory();
        this.idGenerator = new DefaultIdGenerator();
    }

    public DefaultSecurityContextFactory(CredentialHandlerFactory chf) {
        this();
        this.credentialHandlerFactory = chf;
    }

    public DefaultSecurityContextFactory(CredentialHandlerFactory chf, IdentityCache identityCache) {
        this(chf);
        this.identityCache = identityCache;
    }

    public DefaultSecurityContextFactory(CredentialHandlerFactory chf, IdentityCache identityCache,
                                                        EventBridge eventBridge, IdGenerator idGenerator) {
        this(chf, identityCache);
        this.idGenerator = idGenerator;

        if (eventBridge != null) {
            this.eventBridge = eventBridge;
        }
    }

    @Override
    public SecurityContext createContext() {
        return new SecurityContext(this.identityCache, this.eventBridge, this.credentialHandlerFactory, this.idGenerator, null);
    }

    @Override
    public SecurityContext createContext(Partition partition) {
        return new SecurityContext(this.identityCache, this.eventBridge, this.credentialHandlerFactory, this.idGenerator,
                partition);
    }

    @Override
    public void initContextForStore(SecurityContext ctx, IdentityStore<?> store) {

    }

}