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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>A class used to build {@link IdentityConfiguration} instances, providing a fluent API with some meaningful
 * methods.</p> <p/> <p>It can be initialized in two ways:</p> <p/> <ul> <li>Using the default constructor. In this case
 * all the configuration must be done before invoking one of the build methods.</li> <li>Passing a {@link List} of
 * {@link IdentityConfiguration}. In this case the builder will be initialized with all the configuration read from the
 * provided configurations.</li> </ul> <p/> <p>Multiple configurations are supported and each one must have a unique
 * name. At least one configuration must be provided, otherwise the build methods will fail when invoked.</p>
 *
 * @author Pedro Igor
 */
public class IdentityConfigurationBuilder extends Builder<List<IdentityConfiguration>> implements IdentityConfigurationChildBuilder {

    private final Map<String, NamedIdentityConfigurationBuilder> namedIdentityConfigurationBuilders;

    public IdentityConfigurationBuilder() {
        this.namedIdentityConfigurationBuilders = new LinkedHashMap<String, NamedIdentityConfigurationBuilder>();
    }

    /**
     * <p>Creates a new instance reading all the configuration from a previously created list of {@link
     * IdentityConfiguration}.</p>
     *
     * @param configurations
     * @throws SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    public IdentityConfigurationBuilder(List<IdentityConfiguration> configurations) throws SecurityConfigurationException {
        this();
        readFrom(configurations);
    }

    /**
     * <p>Creates a new configuration.</p> <p>If a configuration with the given <code>configurationName</code> already
     * exists, this method will return the same instance instead of creating a new one.</p>
     *
     * @param configurationName
     * @return
     */
    public NamedIdentityConfigurationBuilder named(String configurationName) {
        // Check if config with this name is already here
        if (this.namedIdentityConfigurationBuilders.containsKey(configurationName)) {
            return this.namedIdentityConfigurationBuilders.get(configurationName);
        }

        NamedIdentityConfigurationBuilder namedIdentityConfiguration = new NamedIdentityConfigurationBuilder(configurationName, this);

        this.namedIdentityConfigurationBuilders.put(configurationName, namedIdentityConfiguration);

        return namedIdentityConfiguration;
    }

    /**
     * <p>Builds a single {@link IdentityConfiguration}.</p> <p/> <p>This method should be called when only a single
     * configuration was provided. Otherwise an exception will be thrown.</p> <p/> <p>For building multiple
     * configurations use the <code>buildAll</code> method instead.</p>
     *
     * @return
     * @throws SecurityConfigurationException if multiple configurations was defined, or if any validation check fails
     *                                        or if any error occurs when building the configuration.
     */
    @Override
    public IdentityConfiguration build() throws SecurityConfigurationException {
        if (this.namedIdentityConfigurationBuilders.size() > 1) {
            throw MESSAGES.configBuildMultipleConfigurationExists();
        }

        List<IdentityConfiguration> identityConfigurations = create();

        return identityConfigurations.get(0);
    }

    /**
     * <p>Builds a {@link List} of {@link IdentityConfiguration}.</p> <p/> <p>This method should be used when multiple
     * configurations exists.</p>
     *
     * @return
     * @throws SecurityConfigurationException if any validation check fails or if any error occurs when building the
     *                                        configuration.
     */
    @Override
    public List<IdentityConfiguration> buildAll() throws SecurityConfigurationException {
        return create();
    }

    /**
     * <p>Indicates if any configuration was already provided for this instance.</p>
     *
     * @return
     */
    public boolean isConfigured() {
        return !this.namedIdentityConfigurationBuilders.isEmpty();
    }

    @Override
    protected void validate() throws SecurityConfigurationException {
        if (this.namedIdentityConfigurationBuilders.isEmpty()) {
            throw MESSAGES.configNoConfigurationProvided();
        }

        for (NamedIdentityConfigurationBuilder identityConfigBuilder : this.namedIdentityConfigurationBuilders.values()) {
            try {
                identityConfigBuilder.validate();
            } catch (Exception e) {
                throw MESSAGES.configInvalidConfiguration(identityConfigBuilder.getName(), e);
            }
        }
    }

    @Override
    protected List<IdentityConfiguration> create() throws SecurityConfigurationException {
        List<IdentityConfiguration> configurations = new ArrayList<IdentityConfiguration>();

        try {
            validate();

            for (NamedIdentityConfigurationBuilder identityConfigBuilder : this.namedIdentityConfigurationBuilders.values()) {
                IdentityConfiguration configuration = identityConfigBuilder.create();

                if (configurations.contains(configuration)) {
                    throw MESSAGES.configMultipleConfigurationsFoundWithSameName(configuration.getName());
                }

                boolean supportCredentials = false;

                for (IdentityStoreConfiguration storeConfiguration : configuration.getStoreConfiguration()) {
                    if (storeConfiguration.supportsCredential()) {
                        if (supportCredentials) {
                            throw MESSAGES.configMultipleConfigurationsFoundWithCredentialSupport();
                        }
                        supportCredentials = true;
                    }
                }

                configurations.add(configuration);
            }
        } catch (Exception sce) {
            throw MESSAGES.configCouldNotCreateConfiguration(sce);
        }

        return configurations;
    }

    @Override
    protected Builder<List<IdentityConfiguration>> readFrom(List<IdentityConfiguration> fromConfiguration) throws SecurityConfigurationException {
        if (fromConfiguration == null || fromConfiguration.isEmpty()) {
            throw MESSAGES.nullArgument("Configuration to read from.");
        }

        for (IdentityConfiguration identityConfiguration : fromConfiguration) {
            named(identityConfiguration.getName()).readFrom(identityConfiguration);
        }

        return this;
    }

}