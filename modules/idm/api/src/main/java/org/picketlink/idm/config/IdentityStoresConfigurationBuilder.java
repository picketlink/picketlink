/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.config;

import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pedro Igor
 *
 */
public class IdentityStoresConfigurationBuilder extends AbstractIdentityConfigurationChildBuilder implements
        Builder<IdentityStoresConfiguration> {

    private StoreFactory storeFactory;
    private List<IdentityStoreConfigurationBuilder<?, ?>> identityStoresConfiguration = new ArrayList<IdentityStoreConfigurationBuilder<?, ?>>();
    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>> supportedStoreBuilders = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>>();
    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> identityStores = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>>();

    public IdentityStoresConfigurationBuilder(IdentityConfigurationBuilder builder) {
        super(builder);

        this.supportedStoreBuilders.put(FileIdentityStoreConfiguration.class, FileStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(JPAIdentityStoreConfigurationOld.class, JPAStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(LDAPIdentityStoreConfiguration.class, LDAPStoreConfigurationBuilder.class);
    }

    public IdentityStoresConfigurationBuilder storeFactory(StoreFactory storeFactory) {
        this.storeFactory = storeFactory;
        return this;
    }

    public FileStoreConfigurationBuilder file() {
        return forIdentityStoreConfig(FileIdentityStoreConfiguration.class, true);
    }

    public JPAStoreConfigurationBuilder jpa() {
        return forIdentityStoreConfig(JPAIdentityStoreConfigurationOld.class, true);
    }

    public LDAPStoreConfigurationBuilder ldap() {
        return forIdentityStoreConfig(LDAPIdentityStoreConfiguration.class, true);
    }

    @Override
    public IdentityStoresConfiguration create() {
        List<IdentityStoreConfiguration> configurations = new ArrayList<IdentityStoreConfiguration>();

        for (IdentityStoreConfigurationBuilder<?, ?> storeConfigurationBuilder : this.identityStoresConfiguration) {
            configurations.add(storeConfigurationBuilder.create());
        }

        return new IdentityStoresConfiguration(configurations, this.storeFactory, this.identityStores);
    }

    @Override
    public void validate() {
        if (this.identityStoresConfiguration.isEmpty()) {
            throw new SecurityConfigurationException("You must configure at least one identity store.");
        }

        for (IdentityStoreConfigurationBuilder<?, ?> storeConfigurationBuilder : this.identityStoresConfiguration) {
            storeConfigurationBuilder.validate();
        }
    }

    @Override
    public IdentityStoresConfigurationBuilder readFrom(IdentityStoresConfiguration configuration) {
        if (configuration == null) {
            throw IDMMessages.MESSAGES.nullArgument("Configurations to read");
        }

        for (IdentityStoreConfiguration identityStoreConfiguration : configuration.getConfigurations()) {
            IdentityStoreConfigurationBuilder<IdentityStoreConfiguration, ?> storeConfigBuilder = forIdentityStoreConfig(
                    identityStoreConfiguration.getClass(), true);
            storeConfigBuilder.readFrom(identityStoreConfiguration);
        }

        storeFactory(configuration.getStoreFactory());

        return this;
    }

    @SuppressWarnings("unchecked")
    private <S extends IdentityStoreConfigurationBuilder<?, ?>> S forIdentityStoreConfig(
            Class<? extends IdentityStoreConfiguration> configurationType, boolean createIfNotExists) {
        Class<S> builderType = (Class<S>) this.supportedStoreBuilders.get(configurationType);

        for (IdentityStoreConfigurationBuilder<?, ?> registeredStoreConfig : this.identityStoresConfiguration) {
            if (registeredStoreConfig.getClass().equals(builderType)) {
                return (S) registeredStoreConfig;
            }
        }

        if (!createIfNotExists) {
            return null;
        }

        S instance;

        try {
            instance = builderType.getConstructor(IdentityStoresConfigurationBuilder.class).newInstance(this);
        } catch (Exception e) {
            throw new SecurityConfigurationException("Could not instantiate IdentityStoreConfigurationBuilder ["
                    + builderType.getName() + "]", e);
        }

        this.identityStoresConfiguration.add(instance);

        return instance;
    }

    public boolean isEmpty() {
        return this.identityStoresConfiguration.isEmpty();
    }

    public boolean isConfigured(Class<? extends IdentityStoreConfiguration> storeConfigType) {
        return forIdentityStoreConfig(storeConfigType, false) != null;
    }

    public <T extends IdentityStoreConfigurationBuilder<?, ?>> T add(Class<? extends IdentityStoreConfiguration> identityStoreConfiguration,
            Class<? extends IdentityStore<?>> identityStore, Class<T> builder) {
        this.identityStores.put(identityStoreConfiguration, identityStore);
        this.supportedStoreBuilders.put(identityStoreConfiguration, builder);

        return forIdentityStoreConfig(identityStoreConfiguration, true);
    }

}