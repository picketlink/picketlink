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
package org.picketlink.idm.config;

import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Represents a configuration for a specific {@link org.picketlink.idm.spi.IdentityStore}.
 * </p>
 *
 * @author Anil Saldhana
 * @author Shane Bryzak
 * @since Sep 6, 2012
 */
public interface IdentityStoreConfiguration {

    /**
     * Returns a List of the configured context initializers for this configuration.  Each
     * context initializer performs a specific initialization task for the IdentityContext before
     * it is passed to the identity store, for example setting references to system resources (such as
     * an EntityManager) required by the identity store to perform its identity operations.
     *
     * @return
     */
    List<ContextInitializer> getContextInitializers();

    Map<Class<? extends AttributedType>, Set<IdentityOperation>> getUnsupportedTypes();

    Map<Class<? extends AttributedType>, Set<IdentityOperation>> getSupportedTypes();

    /**
     * <p>Indicates if ad-hoc attributes are supported.</p>
     *
     * @return
     */
    boolean supportsAttribute();

    /**
     * <p>Indicates if credentials are supported.</p>
     *
     * @return
     */
    boolean supportsCredential();

    /**
     * <p>Supported operations for @{AttributedType} types.</p>
     */
    enum IdentityOperation {
        create, read, update, delete
    }

    /**
     * <p>
     * Adds a {@link ContextInitializer} instance which will be used to initialize {@link IdentityContext}s for
     * this configuration.
     * </p>
     *
     */
    void addContextInitializer(ContextInitializer contextInitializer);

    /**
     * <p>
     * Initialize the specified {@link IdentityContext}
     * </p>
     *
     * @param context The {@link IdentityContext} to initialize
     */
    void initializeContext(IdentityContext context, IdentityStore<?> store);

    /**
     * <p>Returns a {@link List} of the {@link CredentialHandler} types configured.</p>
     *
     * @return
     */
    List<Class<? extends CredentialHandler>> getCredentialHandlers();

    /**
     * <p>Allows credential handler behaviour to be customized via a set of property values</p>
     *
     * @return
     */
    Map<String, Object> getCredentialHandlerProperties();

    /**
     * <p>Checks if the configuration supports the given {@link AttributedType} and {@link IdentityOperation}.</p>
     *
     * @param type
     * @param operation
     * @return
     */
    boolean supportsType(Class<? extends AttributedType> type, IdentityOperation operation);

    /**
     * <p>Indicates if this configuration supports partition storage.</p>
     *
     * @return
     */
    boolean supportsPartition();

    /**
     * Indicates whether this configuration supports the storing of resource permissions
     *
     * @return
     */
    boolean supportsPermissions();

    /**
     * <p>Returns the {@link IdentityStore} type associated with this configuration.</p>
     *
     * @return
     */
    Class<? extends IdentityStore> getIdentityStoreType();
}