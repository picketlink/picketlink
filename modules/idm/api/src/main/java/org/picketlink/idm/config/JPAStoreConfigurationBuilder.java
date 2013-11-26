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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * <p>{@link IdentityStoreConfigurationBuilder} implementation which knows how to build a
 * {@link JPAIdentityStoreConfiguration}.</p>
 *
 * @author Pedro Igor
 */
public class JPAStoreConfigurationBuilder extends
        IdentityStoreConfigurationBuilder<JPAIdentityStoreConfiguration, JPAStoreConfigurationBuilder> {

    private final Set<Class<?>> mappedEntities = new HashSet<Class<?>>();

    public JPAStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    @Override
    protected JPAIdentityStoreConfiguration create() {
        return new JPAIdentityStoreConfiguration(
                this.mappedEntities,
                getSupportedTypes(),
                getUnsupportedTypes(),
                getContextInitializers(),
                getCredentialHandlerProperties(),
                getCredentialHandlers(),
                isSupportAttributes(),
                isSupportCredentials(),
                isSupportPermissions());
    }

    @Override
    protected void validate() {
        super.validate();
    }

    @Override
    protected JPAStoreConfigurationBuilder readFrom(JPAIdentityStoreConfiguration configuration) {
        super.readFrom(configuration);

        for (Class<?> entityType: configuration.getEntityTypes()) {
            mappedEntity(entityType);
        }

        return this;
    }

    /**
     * <p>Configures the given mapped entities.</p>
     *
     * @param mappedEntity
     * @return
     */
    public IdentityStoreConfigurationBuilder mappedEntity(Class<?>... mappedEntity) {
        this.mappedEntities.addAll(Arrays.asList(mappedEntity));
        return this;
    }

    protected Set<Class<?>> getMappedEntities() {
        return this.mappedEntities;
    }
}