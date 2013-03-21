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

import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.spi.SecurityContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * <p>
 * Defines the runtime configuration for Identity Management.
 * </p>
 * <p>
 * You should use this class to provide all necessary configuration for the identity stores that should be supported by the
 * IdentityManager.
 * </p>
 *
 * @author Shane Bryzak
 */
public class IdentityConfiguration {

    private static final String DEFAULT_IDENTITY_MANAGER_FACTORY_IMPL = "org.picketlink.idm.internal.DefaultIdentityManagerFactory";

    private List<IdentityStoreConfiguration<?>> configuredStores = new ArrayList<IdentityStoreConfiguration<?>>();
    private SecurityContextFactory securityContextFactory;
    private StoreFactory storeFactory;

    /**
     * <p>
     * Returns a {@link FileIdentityStoreConfiguration} instance with all configuration options for the file identity store. For
     * every invocation a new instance will be returned and added to the configuration.
     * </p>
     *
     * @return
     */
    public FileIdentityStoreConfiguration fileStore() {
        FileIdentityStoreConfiguration storeConfig = new FileIdentityStoreConfiguration();

        this.configuredStores.add(storeConfig);

        return storeConfig;
    }

    /**
     * <p>
     * Returns a {@link JPAIdentityStoreConfiguration} instance with all configuration options for the JPA identity store. For
     * every invocation a new instance will be returned and added to the configuration.
     * </p>
     *
     * @return
     */
    public JPAIdentityStoreConfiguration jpaStore() {
        JPAIdentityStoreConfiguration storeConfig = new JPAIdentityStoreConfiguration();

        this.configuredStores.add(storeConfig);

        return storeConfig;
    }

    /**
     * <p>
     * Returns a {@link JPAIdentityStoreConfiguration} instance with all configuration options for the JPA identity store. For
     * every invocation a new instance will be returned and added to the configuration.
     * </p>
     *
     * @return
     */
    public LDAPIdentityStoreConfiguration ldapStore() {
        LDAPIdentityStoreConfiguration storeConfig = new LDAPIdentityStoreConfiguration();

        this.configuredStores.add(storeConfig);

        return storeConfig;
    }

    /**
     * <p>
     * Sets the {@link SecurityContextFactory} that should be used. If not specified, the implementation will use the default
     * one.
     * </p>
     *
     * @param securityContextFactory
     * @return
     */
    public IdentityConfiguration contextFactory(SecurityContextFactory securityContextFactory) {
        this.securityContextFactory = securityContextFactory;
        return this;
    }

    /**
     * <p>
     * Sets the {@link StoreFactory} that should be used. If not specified, the implementation will use the default one.
     * </p>
     *
     * @param storeFactory
     * @return
     */
    public IdentityConfiguration storeFactory(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
        return this;
    }

    /**
     * <p>
     * Returns all registered {@link IdentityStoreConfiguration} instances.
     * </p>
     *
     * @return
     */
    public List<IdentityStoreConfiguration<?>> getConfiguredStores() {
        return Collections.unmodifiableList(this.configuredStores);
    }

    /**
     * <p>
     * Registers a {@link IdentityStoreConfiguration}. This method can be used to provide other
     * {@link IdentityStoreConfiguration} for identity stores that are not provided by default.
     * </p>
     *
     * @param config
     */
    public void addConfig(IdentityStoreConfiguration<?> config) {
        this.configuredStores.add(config);
    }

    /**
     * <p>
     * Builds and returns a new {@link IdentityManagerFactory} instance considering all configurations provided.
     * </p>
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public IdentityManagerFactory buildIdentityManagerFactory() {
        IdentityManagerFactory identityManagerFactory = null;

        try {
            Class<IdentityManagerFactory> implementationClass = (Class<IdentityManagerFactory>) Class
                    .forName(DEFAULT_IDENTITY_MANAGER_FACTORY_IMPL);

            if (this.securityContextFactory != null && this.storeFactory != null) {
                identityManagerFactory = implementationClass.getConstructor(
                        new Class[] { IdentityConfiguration.class, SecurityContextFactory.class, StoreFactory.class })
                        .newInstance(new Object[] { this, this.securityContextFactory, this.storeFactory });
            } else if (this.securityContextFactory != null) {
                identityManagerFactory = implementationClass.getConstructor(
                        new Class[] { IdentityConfiguration.class, SecurityContextFactory.class }).newInstance(
                        new Object[] { this, this.securityContextFactory });
            } else if (this.storeFactory != null) {
                identityManagerFactory = implementationClass.getConstructor(
                        new Class[] { IdentityConfiguration.class, StoreFactory.class }).newInstance(
                        new Object[] { this, this.storeFactory });
            } else {
                identityManagerFactory = implementationClass.getConstructor(IdentityConfiguration.class).newInstance(this);
            }
        } catch (Exception e) {
            MESSAGES.configurationCouldNotCreateIdentityManagerFactoryImpl(DEFAULT_IDENTITY_MANAGER_FACTORY_IMPL, e);
        }

        return identityManagerFactory;
    }
}