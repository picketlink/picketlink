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
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * Used to pass contextual state to an IdentityStore during an identity management operation.
 *
 * @author Shane Bryzak
 *
 */
public class IdentityStoreInvocationContext {

    private static final ThreadLocal<IdentityStoreInvocationContext> contextThreadLocal = new ThreadLocal<IdentityStoreInvocationContext>();

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
    private Realm realm;

    /**
     *
     */
    private Tier tier;

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
    private Map<String, Object> parameters = new HashMap<String, Object>();

    private IdentityManager identityManager;

    public IdentityStoreInvocationContext(IdentityManager identityManager, IdentityCache cache, EventBridge eventBridge, CredentialHandlerFactory factory,
            IdGenerator idGenerator) {
        this.identityManager = identityManager;
        this.cache = cache;
        this.eventBridge = eventBridge;
        this.credentialHandlerFactory = factory;
        this.idGenerator = idGenerator;
    }

    public static void set(IdentityStoreInvocationContext context) {
        contextThreadLocal.set(context);
    }

    public static void remove() {
        contextThreadLocal.remove();
    }

    public static IdentityStoreInvocationContext get() {
        return contextThreadLocal.get();
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
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            return this.parameters.get(paramName);
        }

        return currentContext.getParameter(paramName);
    }

    /**
     * Returns a boolean indicating whether the parameter with the specified name has been set
     *
     * @param paramName
     * @return
     */
    public boolean isParameterSet(String paramName) {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            return this.parameters.containsKey(paramName);
        }

        return currentContext.isParameterSet(paramName);
    }

    /**
     * Sets a parameter value
     *
     * @param paramName
     * @param value
     */
    public void setParameter(String paramName, Object value) {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            this.parameters.put(paramName, value);
        } else {
            currentContext.setParameter(paramName, value);
        }
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
     * Return the active Realm for this context
     *
     * @return
     */
    public Realm getRealm() {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            return this.realm;
        }

        return currentContext.getRealm();
    }

    /**
     * Sets the active Realm for this context
     *
     * @param realm
     */
    public void setRealm(Realm realm) {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            this.realm = realm;
        } else {
            currentContext.setRealm(realm);
        }
    }

    /**
     * Return the active Tier for this context
     *
     * @return
     */
    public Tier getTier() {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            return this.tier;
        }

        return currentContext.getTier();
    }

    /**
     * Sets the active Tier for this context
     *
     * @param tier
     */
    public void setTier(Tier tier) {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            this.tier = tier;
        } else {
            currentContext.setTier(tier);
        }
    }

    /**
     * <p>
     * Returns the current {@link Partition}. It can be a {@link Realm} or a {@link Tier}. If the {@link Tier} is setted it will
     * be returned otherwise the {@link Realm}.
     * </p>
     *
     * @return
     */
    public Partition getPartition() {
        IdentityStoreInvocationContext currentContext = getCurrentContext();

        if (currentContext == this || currentContext == null) {
            Partition partition = getTier();

            if (partition == null) {
                partition = getRealm();
            }

            return partition;
        }

        Partition partition = getCurrentContext().getTier();

        if (partition == null) {
            partition = getCurrentContext().getRealm();
        }

        return partition;
    }

    public IdentityManager getIdentityManager() {
        return this.identityManager;
    }

    private IdentityStoreInvocationContext getCurrentContext() {
        return contextThreadLocal.get();
    }

}