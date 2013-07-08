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

/**
 * <p>
 * This class should be used as the start point to build an {@link IdentityConfiguration} instance.
 * </p>
 *
 * @author Pedro Igor
 */
public class IdentityConfigurationBuilder implements IdentityConfigurationChildBuilder {

    private final List<NamedIdentityConfigurationBuilder> namedIdentityConfigurationBuilders;

    public IdentityConfigurationBuilder() {
        this.namedIdentityConfigurationBuilders = new ArrayList<NamedIdentityConfigurationBuilder>();
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
    public IdentityConfiguration build() {
        validate();

        if (this.namedIdentityConfigurationBuilders.size() > 1) {
            throw new SecurityConfigurationException("You have provided more than one configuration. Use the buildAll method instead.");
        }

        return this.namedIdentityConfigurationBuilders.get(0).create();
    }

    @Override
    public List<IdentityConfiguration> buildAll() {
        validate();

        List<IdentityConfiguration> configurations = new ArrayList<IdentityConfiguration>();

        for (NamedIdentityConfigurationBuilder identityConfigBuilder: this.namedIdentityConfigurationBuilders) {
            IdentityConfiguration configuration = identityConfigBuilder.create();

            if (configurations.contains(configuration)) {
                throw new SecurityConfigurationException("Multiple configuration with the same name [" + configuration.getName() + "].");
            }

            configurations.add(configuration);
        }

        return configurations;
    }

    private void validate() {
        if (this.namedIdentityConfigurationBuilders.isEmpty()) {
            throw new SecurityConfigurationException("You must provide at least one configuration.");
        }

        for (NamedIdentityConfigurationBuilder identityConfigBuilder: this.namedIdentityConfigurationBuilders) {
            identityConfigBuilder.validate();
        }
    }

}