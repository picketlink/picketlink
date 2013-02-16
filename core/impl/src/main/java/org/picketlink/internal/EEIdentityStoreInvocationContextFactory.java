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

package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.picketlink.annotations.PicketLink;
import org.picketlink.idm.DefaultIdentityCache;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.internal.DefaultIdGenerator;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class EEIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {

    @Inject @PicketLink Instance<EntityManager> entityManagerInstance;
    
    @Inject CDIEventBridge cdiEventBridge;
    
    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;
    
    public EEIdentityStoreInvocationContextFactory() {
        credentialHandlerFactory = new DefaultCredentialHandlerFactory();
        identityCache = new DefaultIdentityCache();
        idGenerator = new DefaultIdGenerator();
    }

    @Override
    public IdentityStoreInvocationContext createContext() {
        return new IdentityStoreInvocationContext(this.identityCache, cdiEventBridge, credentialHandlerFactory, idGenerator);
    }

    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (entityManagerInstance.isUnsatisfied()) {
                throw new SecurityConfigurationException("To use JPAIdentityStore you must provide an EntityManager producer method " +
                        "qualified with @org.picketlink.annotations.PicketLink.");
            } else if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, entityManagerInstance.get());
            }
        }
    }

}
