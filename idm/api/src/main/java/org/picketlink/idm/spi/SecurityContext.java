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

package org.picketlink.idm.spi;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;

/**
 * Stores security related state for one or more identity management operations
 *
 * @author Shane Bryzak
 *
 */
public class SecurityContext {

    /**
     *
     */
    private IdentityCache cache;

    /**
     *
     */
    private EventBridge eventBridge;

    /**
     *
     */
    private Partition partition;

    /**
     *
     */
    private CredentialHandlerFactory credentialHandlerFactory;

    /**
     *
     */
    private IdGenerator idGenerator;

    /**
     *
     */
    private IdentityManager identityManager;

    /**
     *
     */
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public SecurityContext(IdentityCache cache, EventBridge eventBridge, CredentialHandlerFactory factory,
            IdGenerator idGenerator, Partition partition) {
        this.cache = cache;
        this.eventBridge = eventBridge;
        this.credentialHandlerFactory = factory;
        this.idGenerator = idGenerator;
        this.partition = partition;
    }

    /**
     * Returns a CredentialHandler instance capable of validating a credential of the specified Credentials class, for the
     * specified IdentityStore class
     *
     * @param credentialsClass
     * @param identityStoreClass
     * @return
     */
    public CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass, IdentityStore identityStore) {
        return credentialHandlerFactory.getCredentialValidator(credentialsClass, identityStore.getClass());
    }

    /**
     * Returns a CredentialHandler instance capable of updating a credential of the specified Credentials class, for the
     * specified IdentityStore class
     *
     * @param credentialClass
     * @param identityStoreClass
     * @return
     */
    public CredentialHandler getCredentialUpdater(Class<?> credentialClass, IdentityStore identityStore) {
        return credentialHandlerFactory.getCredentialUpdater(credentialClass, identityStore.getClass());
    }

    /**
     * Returns the cache for this invocation context.
     *
     * @return
     */
    public IdentityCache getCache() {
        return cache;
    }

    /**
     * Returns the parameter value with the specified name
     *
     * @return
     */
    public Object getParameter(String paramName) {
        return this.parameters.get(paramName);
    }

    /**
     * Returns a boolean indicating whether the parameter with the specified name has been set
     *
     * @param paramName
     * @return
     */
    public boolean isParameterSet(String paramName) {
        return this.parameters.containsKey(paramName);
    }

    /**
     * Sets a parameter value
     *
     * @param paramName
     * @param value
     */
    public void setParameter(String paramName, Object value) {
        this.parameters.put(paramName, value);
    }

    /**
     *
     * @return
     */
    public EventBridge getEventBridge() {
        return eventBridge;
    }

    /**
     *
     * @return
     */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    /**
     * Return the active Partition for this context
     *
     * @return
     */
    public Partition getPartition() {
        return partition;
    }

    public void setIdentityManager(IdentityManager identityManager) {
        this.identityManager = identityManager;
    }

    public IdentityManager getIdentityManager() {
        return identityManager;
    }

}