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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.IdentityStore;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * <p>Holds all the configuration for identity stores.</p>
 *
 * @author Pedro Igor
 *
 */
public class IdentityStoresConfiguration {

    private final List<IdentityStoreConfiguration> configurations;
    private final Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> identityStores;

    public IdentityStoresConfiguration(List<IdentityStoreConfiguration> configurations) {
        this(configurations,
                Collections.<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>>emptyMap());
    }

    public IdentityStoresConfiguration(
            List<IdentityStoreConfiguration> configurations,
            Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> identityStores) {
        this.configurations = unmodifiableList(configurations);
        this.identityStores = unmodifiableMap(identityStores);
    }

    public List<IdentityStoreConfiguration> getConfigurations() {
        return this.configurations;
    }

    public Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> getIdentityStores() {
        return this.identityStores;
    }

    public IdentityStoreConfiguration forType(Class<? extends AttributedType> type, IdentityOperation operation) {
        for (IdentityStoreConfiguration storeConfiguration : getConfigurations()) {
            if (storeConfiguration.supportsType(type, operation)) {
                return storeConfiguration;
            }
        }

        return null;
    }


}
