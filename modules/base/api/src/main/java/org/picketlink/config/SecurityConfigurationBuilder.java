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
package org.picketlink.config;

import org.picketlink.config.http.HttpSecurityConfiguration;
import org.picketlink.idm.config.Builder;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A class used to build {@link org.picketlink.config.SecurityConfiguration} instances, providing a fluent API with some meaningful
 * methods.</p>
 *
 * <p>It can be initialized in two ways:</p>
 *
 * <ul>
 *     <li>Using the default constructor. In this case all the configuration must be done before invoking one of the build methods.</li>
 * </ul>
 *
 * @author Pedro Igor
 */
public class SecurityConfigurationBuilder extends Builder<SecurityConfiguration> implements SecurityConfigurationChildBuilder {

    private IdentityConfigurationBuilder identityConfigurationBuilder;
    private IdentityBeanConfigurationBuilder identityBeanConfigurationBuilder = new IdentityBeanConfigurationBuilder(this);
    private HttpSecurityBuilder httpSecurityBuilder;

    /**
     * <p>Convenience methods for Identity Management Configuration.</p>
     *
     * @return The builder responsible to provide all identity management configuration options.
     */
    @Override
    public IdentityConfigurationBuilder idmConfig() {
        if (this.identityConfigurationBuilder == null) {
            this.identityConfigurationBuilder = new IdentityConfigurationBuilder();
        }

        return this.identityConfigurationBuilder;
    }

    /**
     * <p>Convenience methods to configure behavior of the {@link org.picketlink.Identity} bean.</p>
     *
     * @return The builder responsible to provide the configuration options for the identity bean.
     */
    @Override
    public IdentityBeanConfigurationBuilder identity() {
        return this.identityBeanConfigurationBuilder;
    }

    /**
     * <p>Convenience methods to configure HTTP security.</p>
     *
     * @return The builder responsible to provide the configuration options for the identity bean.
     */
    @Override
    public HttpSecurityBuilder http() {
        if (this.httpSecurityBuilder == null) {
            this.httpSecurityBuilder = new HttpSecurityBuilder(this);
        }

        return this.httpSecurityBuilder;
    }

    /**
     * <p>Builds a {@link org.picketlink.config.SecurityConfiguration} instance.</p>
     *
     * @return The consolidated configuration.
     */
    @Override
    public SecurityConfiguration build() {
        return create();
    }

    @Override
    protected SecurityConfiguration create() throws SecurityConfigurationException {
        List<IdentityConfiguration> identityConfigurations = new ArrayList<IdentityConfiguration>();

        if (this.identityConfigurationBuilder != null) {
            identityConfigurations = this.identityConfigurationBuilder.buildAll();
        }

        HttpSecurityConfiguration httpSecurityConfiguration = null;

        if (this.httpSecurityBuilder != null) {
            httpSecurityConfiguration = this.httpSecurityBuilder.create();
        }

        return new SecurityConfiguration(identityConfigurations, this.identityBeanConfigurationBuilder.create(), httpSecurityConfiguration);
    }

    @Override
    protected void validate() throws SecurityConfigurationException {

    }

    @Override
    protected Builder<SecurityConfiguration> readFrom(SecurityConfiguration fromConfiguration) throws SecurityConfigurationException {
        return this;
    }
}
