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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;

/**
 * @author Pedro Igor
 *
 */
public class IdentityStoresConfigurationBuilder extends AbstractIdentityConfigurationChildBuilder<List<? extends IdentityStoreConfiguration>> implements
        Builder<List<? extends IdentityStoreConfiguration>> {

    private final List<AbstractIdentityStoreConfigurationBuilder<?, ?>> identityStoresConfiguration;
    private final Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>> supportedStoreBuilders;
    Map<Class<? extends Relationship>, IdentityStoreConfiguration> globalRelationships = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();
    Map<Class<? extends Relationship>, IdentityStoreConfiguration> selfRelationships = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();

    public IdentityStoresConfigurationBuilder(NamedIdentityConfigurationBuilder builder) {
        super(builder);
        this.identityStoresConfiguration = new ArrayList<AbstractIdentityStoreConfigurationBuilder<?, ?>>();
        this.supportedStoreBuilders = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>>();
        this.globalRelationships = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();
        this.selfRelationships = new HashMap<Class<? extends Relationship>, IdentityStoreConfiguration>();

        this.supportedStoreBuilders.put(FileIdentityStoreConfiguration.class, FileStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(JPAIdentityStoreConfiguration.class, JPAStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(LDAPIdentityStoreConfiguration.class, LDAPStoreConfigurationBuilder.class);
    }

    public FileStoreConfigurationBuilder file() {
        return forIdentityStoreConfig(FileIdentityStoreConfiguration.class, true);
    }

    public JPAStoreConfigurationBuilder jpa() {
        return forIdentityStoreConfig(JPAIdentityStoreConfiguration.class, true);
    }

    public LDAPStoreConfigurationBuilder ldap() {
        return forIdentityStoreConfig(LDAPIdentityStoreConfiguration.class, true);
    }

    /**
     * <p>Adds support for a custom {@link IdentityStore}.</p>
     *
     * @param identityStoreConfiguration
     * @param identityStore
     * @param builder
     * @param <T>
     * @return
     */
    public <T extends AbstractIdentityStoreConfigurationBuilder<?, ?>> T add(
            Class<? extends IdentityStoreConfiguration> identityStoreConfiguration, Class<T> builder) {
        this.supportedStoreBuilders.put(identityStoreConfiguration, builder);
        return forIdentityStoreConfig(identityStoreConfiguration, true);
    }

    @Override
    protected List<? extends IdentityStoreConfiguration> create() {
        List<IdentityStoreConfiguration> configurations = new ArrayList<IdentityStoreConfiguration>();

        boolean hasPartitionStore = false;
        Set<Class<? extends AttributedType>> supportedTypes = new HashSet<Class<? extends AttributedType>>();

        for (AbstractIdentityStoreConfigurationBuilder<?, ?> storeConfigurationBuilder : this.identityStoresConfiguration) {
            IdentityStoreConfiguration storeConfiguration = storeConfigurationBuilder.create();

            if (storeConfiguration.supportsPartition()) {
                if (hasPartitionStore) {
                    throw new SecurityConfigurationException("Only one store configuration must be able to store partitions.");
                }

                hasPartitionStore = true;
            }

            try {
                supportedTypes.addAll(storeConfigurationBuilder.getSupportedTypes().keySet());
            } catch (IllegalArgumentException iae) {
                throw new SecurityConfigurationException("Duplicated supported types found for [" + storeConfiguration + "].");
            }

            for (Class<? extends Relationship> relType: storeConfigurationBuilder.getGlobalRelationshipTypes()) {
                this.globalRelationships.put(relType, storeConfiguration);
            }

            for (Class<? extends Relationship> relType: storeConfigurationBuilder.getSelfRelationshipTypes()) {
                this.selfRelationships.put(relType, storeConfiguration);
            }

            configurations.add(storeConfiguration);
        }

        if (!hasPartitionStore) {
            throw new SecurityConfigurationException("At least one store configuration must support partitions.");
        }

        return configurations;
    }

    @Override
    protected void validate() {
        if (this.identityStoresConfiguration.isEmpty()) {
            throw new SecurityConfigurationException("You must configure at least one identity store.");
        }

        for (AbstractIdentityStoreConfigurationBuilder<?, ?> storeConfigurationBuilder : this.identityStoresConfiguration) {
            storeConfigurationBuilder.validate();
        }
    }

    @Override
    public IdentityStoresConfigurationBuilder readFrom(List<? extends IdentityStoreConfiguration> fromConfiguration) {
        if (fromConfiguration == null) {
            throw IDMMessages.MESSAGES.nullArgument("Configurations to read");
        }

        for (IdentityStoreConfiguration identityStoreConfiguration : fromConfiguration) {
            AbstractIdentityStoreConfigurationBuilder<IdentityStoreConfiguration, ?> storeConfigBuilder = forIdentityStoreConfig(
                    identityStoreConfiguration.getClass(), true);
            storeConfigBuilder.readFrom(identityStoreConfiguration);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    private <S extends AbstractIdentityStoreConfigurationBuilder<?, ?>> S forIdentityStoreConfig(
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

    public Map<Class<? extends Relationship>, IdentityStoreConfiguration> getGlobalRelationships() {
        return this.globalRelationships;
    }

    public Map<Class<? extends Relationship>, IdentityStoreConfiguration> getSelfRelationships() {
        return this.selfRelationships;
    }
}