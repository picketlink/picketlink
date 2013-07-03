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

/**
 * <p>
 * This class should be used as the start point to build an {@link IdentityConfiguration} instance.
 * </p>
 *
 * @author Pedro Igor
 */
public class IdentityConfigurationBuilder implements IdentityConfigurationChildBuilder {

    private final IdentityStoresConfigurationBuilder identityStoresConfigurationBuilder;
    private String name = "default";

    public IdentityConfigurationBuilder() {
        this.identityStoresConfigurationBuilder = new IdentityStoresConfigurationBuilder(this);
    }

    /**
     * <p>
     * You may use this constructor to provided a previously created {@link IdentityConfiguration}. The same configuration will
     * be used and validations will be executed when building a new {@link IdentityConfiguration}.
     * </p>
     *
     * @param from
     */
    public IdentityConfigurationBuilder(IdentityConfiguration from) {
        this();
        //TODO: must be able to read custom identity stores from the configuration.
        this.identityStoresConfigurationBuilder.readFrom(from.getStoresConfiguration());
    }

    @Override
    public IdentityStoresConfigurationBuilder stores() {
        return this.identityStoresConfigurationBuilder;
    }

    @Override
    public IdentityConfiguration build() {
        validate();

        return new IdentityConfiguration(this.name, this.identityStoresConfigurationBuilder.create());
    }

    @Override
    public IdentityConfiguration build(String name) {
        this.name = name;
        return build();
    }

    private void validate() {
        this.identityStoresConfigurationBuilder.validate();
    }

}