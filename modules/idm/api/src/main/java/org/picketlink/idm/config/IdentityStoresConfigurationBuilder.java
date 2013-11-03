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

import org.picketlink.idm.config.annotation.MethodConfigID;
import org.picketlink.idm.config.annotation.ParameterConfigID;
import org.picketlink.idm.model.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>A class used to build the configuration for identity stores. Only a single configuration can exists for a given
 * identity store.</p>
 *
 * @author Pedro Igor
 */
public class IdentityStoresConfigurationBuilder
        extends AbstractIdentityConfigurationChildBuilder<List<? extends IdentityStoreConfiguration>>
        implements IdentityStoreConfigurationChildBuilder {

    private final List<IdentityStoreConfigurationBuilder<?, ?>> identityStoresConfiguration;
    private final Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>> supportedStoreBuilders;
    private final Set<Class<? extends Relationship>> globalRelationships;
    private final Set<Class<? extends Relationship>> selfRelationships;

    protected IdentityStoresConfigurationBuilder(NamedIdentityConfigurationBuilder builder) {
        super(builder);
        this.identityStoresConfiguration = new ArrayList<IdentityStoreConfigurationBuilder<?, ?>>();
        this.supportedStoreBuilders = new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStoreConfigurationBuilder<?, ?>>>();
        this.globalRelationships = new HashSet<Class<? extends Relationship>>();
        this.selfRelationships = new HashSet<Class<? extends Relationship>>();

        this.supportedStoreBuilders.put(FileIdentityStoreConfiguration.class, FileStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(JPAIdentityStoreConfiguration.class, JPAStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(LDAPIdentityStoreConfiguration.class, LDAPStoreConfigurationBuilder.class);
        this.supportedStoreBuilders.put(JDBCIdentityStoreConfiguration.class, JDBCStoreConfigurationBuilder.class);
    }

    /**
     * <p>Configures a file-based identity store for this configuration.</p>
     *
     * @return
     */
    @Override
    public FileStoreConfigurationBuilder file() {
        return forIdentityStoreConfig(FileIdentityStoreConfiguration.class, true);
    }

    /**
     * Configures a JDBC based Identity Store
     * @return
     */
    public JDBCStoreConfigurationBuilder jdbc(){
        return forIdentityStoreConfig(JDBCIdentityStoreConfiguration.class, true);
    }

    /**
     * <p>Configures a jpa-based identity store for this configuration.</p>
     *
     * @return
     */
    @Override
    public JPAStoreConfigurationBuilder jpa() {
        return forIdentityStoreConfig(JPAIdentityStoreConfiguration.class, true);
    }

    /**
     * <p>Configures a ldap-based identity store for this configuration.</p>
     *
     * @return
     */
    @Override
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
     *
     * @return
     */
    @MethodConfigID(name = "customIdentityStore")
    public <T extends IdentityStoreConfigurationBuilder<?, ?>> T add(
            @ParameterConfigID(name = "identityStoreConfigurationClass") Class<? extends IdentityStoreConfiguration> identityStoreConfiguration,
            @ParameterConfigID(name = "builderClass") Class<T> builder) {
        this.supportedStoreBuilders.put(identityStoreConfiguration, builder);
        return forIdentityStoreConfig(identityStoreConfiguration, true);
    }

    @Override
    protected List<? extends IdentityStoreConfiguration> create() {
        List<IdentityStoreConfiguration> configurations = new ArrayList<IdentityStoreConfiguration>();
        IdentityStoreConfiguration partitionStoreConfig = null;

        for (IdentityStoreConfigurationBuilder<?, ?> storeConfigurationBuilder : this.identityStoresConfiguration) {
            IdentityStoreConfiguration storeConfiguration = storeConfigurationBuilder.create();

            if (storeConfiguration.supportsPartition()) {
                if (partitionStoreConfig != null) {
                    throw MESSAGES.configStoreMultiplePartitionConfigExists(partitionStoreConfig, storeConfiguration);
                }

                partitionStoreConfig = storeConfiguration;
            }

            for (Class<? extends Relationship> relType : storeConfigurationBuilder.getGlobalRelationshipTypes()) {
                this.globalRelationships.add(relType);
            }

            for (Class<? extends Relationship> relType : storeConfigurationBuilder.getSelfRelationshipTypes()) {
                this.selfRelationships.add(relType);
            }

            configurations.add(storeConfiguration);
        }

        return configurations;
    }

    @Override
    protected void validate() {
        if (this.identityStoresConfiguration.isEmpty()) {
            throw MESSAGES.configStoreNoIdentityStoreConfigProvided();
        }

        for (IdentityStoreConfigurationBuilder<?, ?> currentConfiguration : this.identityStoresConfiguration) {
            currentConfiguration.validate();

            for (Class<?> type: currentConfiguration.getSupportedTypes().keySet()) {
                for (IdentityStoreConfigurationBuilder<?, ?> storeConfiguration: this.identityStoresConfiguration) {
                    if (!storeConfiguration.equals(currentConfiguration)) {
                        for (Class<?> storeType: storeConfiguration.getSupportedTypes().keySet()) {
                            if (storeType.isAssignableFrom(type)) {
                                throw MESSAGES.configStoreDuplicatedSupportedType(type);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected IdentityStoresConfigurationBuilder readFrom(List<? extends IdentityStoreConfiguration> fromConfiguration) {
        if (fromConfiguration == null) {
            throw MESSAGES.nullArgument("Configurations to read");
        }

        for (IdentityStoreConfiguration identityStoreConfiguration : fromConfiguration) {
            IdentityStoreConfigurationBuilder<IdentityStoreConfiguration, ?> storeConfigBuilder = forIdentityStoreConfig(
                    identityStoreConfiguration.getClass(), true);
            storeConfigBuilder.readFrom(identityStoreConfiguration);
        }

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
            throw MESSAGES.instantiationError(builderType, e);
        }

        this.identityStoresConfiguration.add(instance);

        return instance;
    }

    protected Set<Class<? extends Relationship>> getGlobalRelationships() {
        return this.globalRelationships;
    }

    protected Set<Class<? extends Relationship>> getSelfRelationships() {
        return this.selfRelationships;
    }
}