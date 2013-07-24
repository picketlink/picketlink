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
import java.util.List;
import org.picketlink.idm.event.EventBridge;

/**
 * <p>
 * This class should be used as the start point to build an {@link IdentityConfiguration} instance.
 * </p>
 *
 * @author Pedro Igor
 */
public class IdentityConfigurationBuilder extends Builder<List<IdentityConfiguration>> implements IdentityConfigurationChildBuilder {

    private final List<NamedIdentityConfigurationBuilder> namedIdentityConfigurationBuilders;
    private EventBridge eventBridge;

    public IdentityConfigurationBuilder() {
        this.namedIdentityConfigurationBuilders = new ArrayList<NamedIdentityConfigurationBuilder>();
    }

    /**
     * <p>Creates a new instance reading all the configuration from a previously created list of {@link IdentityConfiguration}.</p>
     *
     * @param configurations
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    public IdentityConfigurationBuilder(List<IdentityConfiguration> configurations) throws SecurityConfigurationException {
        this();
        readFrom(configurations);
    }

    /**
     * <p>Creates a named configuration.</p>
     *
     * @param configurationName
     * @return
     */
    public NamedIdentityConfigurationBuilder named(String configurationName) {
        NamedIdentityConfigurationBuilder namedIdentityConfiguration = new NamedIdentityConfigurationBuilder(configurationName, this);

        this.namedIdentityConfigurationBuilders.add(namedIdentityConfiguration);

        return namedIdentityConfiguration;
    }

    @Override
    public IdentityConfiguration build() throws SecurityConfigurationException {
        List<IdentityConfiguration> identityConfigurations = create();

        if (identityConfigurations.size() > 1) {
            throw new SecurityConfigurationException("You have provided more than one configuration. Use the buildAll method instead.");
        }

        return identityConfigurations.get(0);
    }

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
            throw new SecurityConfigurationException("You must provide at least one configuration.");
        }

        for (NamedIdentityConfigurationBuilder identityConfigBuilder : this.namedIdentityConfigurationBuilders) {
            identityConfigBuilder.validate();
        }
    }

    @Override
    protected List<IdentityConfiguration> create() throws SecurityConfigurationException {
        validate();

        List<IdentityConfiguration> configurations = new ArrayList<IdentityConfiguration>();

        for (NamedIdentityConfigurationBuilder identityConfigBuilder : this.namedIdentityConfigurationBuilders) {
            IdentityConfiguration configuration = identityConfigBuilder.create();

            if (configurations.contains(configuration)) {
                throw new SecurityConfigurationException("Multiple configuration with the same name [" + configuration.getName() + "].");
            }

            configurations.add(configuration);
        }

        return configurations;
    }

    @Override
    protected Builder<List<IdentityConfiguration>> readFrom(List<IdentityConfiguration> fromConfiguration) throws SecurityConfigurationException {
        if (fromConfiguration == null || fromConfiguration.isEmpty()) {
            throw new SecurityConfigurationException("No configuration provided to read from.");
        }

        for (IdentityConfiguration identityConfiguration: fromConfiguration) {
            named(identityConfiguration.getName()).readFrom(identityConfiguration);
        }

        return this;
    }

}