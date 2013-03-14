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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;

/**
 * A default implementation of IdentityStoreInvocationContextFactory.
 *
 * @author Shane Bryzak
 * @author Anil Saldhana
 */
public class DefaultIdentityStoreInvocationContextFactory implements IdentityStoreInvocationContextFactory {
    private EntityManagerFactory emf;
    private EventBridge eventBridge;
    private CredentialHandlerFactory credentialHandlerFactory;
    private IdentityCache identityCache;
    private IdGenerator idGenerator;

    // FIXME Bad!! we can't do this, this class is multi-threaded!
    private EntityManager entityManager;

    public static DefaultIdentityStoreInvocationContextFactory DEFAULT = new DefaultIdentityStoreInvocationContextFactory(null, new DefaultCredentialHandlerFactory());

    public DefaultIdentityStoreInvocationContextFactory(){
        this.eventBridge = new EventBridge() {

            @Override
            public void raiseEvent(Object event) {
                // by default do nothing
            }
        };
        this.credentialHandlerFactory = new DefaultCredentialHandlerFactory();
        this.identityCache = new DefaultIdentityCache();
        this.idGenerator = new DefaultIdGenerator();
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf){
        this();
        this.emf = emf;
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf) {
        this(emf);
        this.credentialHandlerFactory = chf;
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache) {
        this(emf, chf);
        this.identityCache = identityCache;
    }

    public DefaultIdentityStoreInvocationContextFactory(EntityManagerFactory emf, CredentialHandlerFactory chf, IdentityCache identityCache,
                                                        EventBridge eventBridge, IdGenerator idGenerator) {
        this(emf, chf, identityCache);
        this.idGenerator = idGenerator;

        if (eventBridge != null) {
            this.eventBridge = eventBridge;
        }
    }

    @Override
    public IdentityStoreInvocationContext createContext(IdentityManager identityManager) {
        return new IdentityStoreInvocationContext(identityManager, this.identityCache, this.eventBridge, this.credentialHandlerFactory, this.idGenerator);
    }

    @Override
    public void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore<?> store) {
        if (store instanceof JPAIdentityStore) {
            if (!ctx.isParameterSet(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER)) {
                ctx.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER, getEntityManager());
            }
        }
    }

    public EntityManager getEntityManager(){
        if(entityManager == null){
            entityManager = emf.createEntityManager();
        }
        return entityManager;
    }

    public void setEntityManager(EntityManager em){
        this.entityManager = em;
    }

}