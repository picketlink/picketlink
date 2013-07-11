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

import java.util.List;

/**
 * <p>Base class for {@link IdentityConfigurationChildBuilder}.</p>
 *
 * @author Pedro Igor
 *
 */
public abstract class AbstractIdentityConfigurationChildBuilder<T> extends Builder<T>
        implements IdentityConfigurationChildBuilder {

    private final IdentityConfigurationChildBuilder identityConfigurationBuilder;

    protected AbstractIdentityConfigurationChildBuilder(IdentityConfigurationChildBuilder builder) {
        this.identityConfigurationBuilder = builder;
    }

    @Override
    public NamedIdentityConfigurationBuilder named(String configurationName) {
        return this.identityConfigurationBuilder.named(configurationName);
    }

    @Override
    public IdentityConfiguration build() {
        return this.identityConfigurationBuilder.build();
    }

    @Override
    public List<IdentityConfiguration> buildAll() {
        return this.identityConfigurationBuilder.buildAll();
    }

}
