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

package org.picketlink.idm.internal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * @author Pedro Silva
 *
 */
public class ConfigurationBuilder<B extends ConfigurationBuilder<?>> {

    private Map<Class<? extends IdentityStoreConfiguration>, IdentityStoreConfigurationBuilder<?,?>> storeConfiguration = new HashMap<Class<? extends IdentityStoreConfiguration>, IdentityStoreConfigurationBuilder<?,?>>();

    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();

    private ConfigurationBuilder<?> builder;

    public ConfigurationBuilder() {
        this.builder = this;
    }

    protected ConfigurationBuilder(ConfigurationBuilder<?> builder) {
        this.builder = builder;
    }

    public FileIdentityStoreConfigurationBuilder fileStore() {
        FileIdentityStoreConfigurationBuilder config = null;

        if (!this.builder.storeConfiguration.containsKey(FileIdentityStoreConfiguration.class)) {
            config = new FileIdentityStoreConfigurationBuilder(this);
            this.builder.storeConfiguration.put(FileIdentityStoreConfiguration.class, config);
        }

        return config;
    }

    public JPAIdentityStoreConfigurationBuilder jpaStore() {
        JPAIdentityStoreConfigurationBuilder config = null;

        if (!this.builder.storeConfiguration.containsKey(JPAIdentityStoreConfiguration.class)) {
            config = new JPAIdentityStoreConfigurationBuilder(this);
            this.builder.storeConfiguration.put(JPAIdentityStoreConfiguration.class, config);
        }

        return config;
    }

    public LDAPIdentityStoreConfigurationBuilder ldapStore() {
        LDAPIdentityStoreConfigurationBuilder config = null;

        if (!this.builder.storeConfiguration.containsKey(LDAPIdentityStoreConfiguration.class)) {
            config = new LDAPIdentityStoreConfigurationBuilder(this);
            this.builder.storeConfiguration.put(LDAPIdentityStoreConfiguration.class, config);
        }

        return config;
    }

    public B addContextInitializer(ContextInitializer initializer) {
        this.builder.contextInitializers.add(initializer);
        return (B) this;
    }

    public IdentityManagerFactory buildIdentityManagerFactory() {
        IdentityConfiguration identityConfig = new IdentityConfiguration();

        Collection<IdentityStoreConfigurationBuilder<?,?>> storeConfigurations = this.builder.storeConfiguration.values();

        for (IdentityStoreConfigurationBuilder<?,?> storeConfigBuilder : storeConfigurations) {
            identityConfig.addStoreConfiguration(storeConfigBuilder.getConfiguration());
        }

        for (ContextInitializer contextInitializer : this.builder.contextInitializers) {
            identityConfig.addContextInitializer(contextInitializer);
        }

        return new DefaultIdentityManagerFactory(identityConfig);
    }

}
