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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * <p>
 * Represents a configuration for a specific IdentityStore.
 * </p>
 *
 * @author Anil Saldhana
 * @author Shane Bryzak
 * @since Sep 6, 2012
 */
public interface IdentityStoreConfiguration {

    /**
     * <p>
     * Initializes the store configuration with the specified {@link FeatureSet}.
     * </p>
     *
     * @throws SecurityConfigurationException
     */
    void init() throws SecurityConfigurationException;

    /**
     * <p>
     * Returns all configured realms.
     * </p>
     *
     * @return
     */
    Set<String> getRealms();

    /**
     *
     * @return
     */
    Set<String> getTiers();

    /**
     * <p>
     * Returns a {@link FeatureSet} describing the features supported by this identity store configuration.
     * </p>
     *
     * @return
     */
    FeatureSet getFeatureSet();

    /**
     * <p>
     * Returns all {@link ContextInitializer} instances configured for a specific identity store.
     * </p>
     *
     * @return
     */
    List<ContextInitializer> getContextInitializers();

    /**
     * <p>Returns a {@link List} of the {@link CredentialHandler} types configured.</p>
     *
     * @return
     */
    List<Class<? extends CredentialHandler>> getCredentialHandlers();

    /**
     * <p>Returns the configuration for the configured {@link CredentialHandler} types.</p>
     *
     * @return
     */
    Map<Class<? extends CredentialHandler>, Map<String, Object>> getCredentialHandlersConfig();
}