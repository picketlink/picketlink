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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * A simple implementation of {@link IdentityStoreInvocationContextFactory} that has no dependencies on JPA
 * @author anil saldhana
 * @since Dec 13, 2012
 */
public class SimpleIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {
    private EventBridge eventBridge;
    private CredentialHandlerFactory credentialHandlerFactory = new DefaultCredentialHandlerFactory();
    private IdentityCache identityCache = new DefaultIdentityCache();
    private IdGenerator idGenerator = new DefaultIdGenerator();

    @Override
    public IdentityStoreInvocationContext createContext(IdentityManager identityManager) {
        return new IdentityStoreInvocationContext(identityManager, this.identityCache, this.eventBridge, this.credentialHandlerFactory, this.idGenerator);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore store) {
    }

}
