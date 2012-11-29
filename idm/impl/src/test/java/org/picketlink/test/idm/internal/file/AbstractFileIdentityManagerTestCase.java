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

package org.picketlink.test.idm.internal.file;

import java.util.Set;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;
import org.picketlink.idm.spi.StoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * <p>
 * Base class for testing the {@link FileBasedIdentityStore}.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public abstract class AbstractFileIdentityManagerTestCase {

    private IdentityManager identityManager;

    protected IdentityManager getIdentityManager() {
        if (this.identityManager == null) {
            final FileBasedIdentityStore store = new FileBasedIdentityStore();

            IdentityConfiguration identityConfig = new IdentityConfiguration();

            // TODO implement FileBasedIdentityStoreConfiguration in org.picketlink.idm.config package in API
            //identityConfig.addStoreConfiguration(new FileBasedIdentityStoreConfiguration());

            this.identityManager = new DefaultIdentityManager();

            // TODO this hack is a workaround until the configuration stuff is implemented
            // hack start
            IdentityStoreConfiguration storeConfig = new IdentityStoreConfiguration() {

                @Override
                public void init() throws SecurityConfigurationException {
                    // TODO Auto-generated method stub
                    
                }

                @Override
                public Set<Feature> getFeatureSet() {
                    // TODO Auto-generated method stub
                    return null;
                }};
            identityConfig.addStoreConfiguration(storeConfig);
            this.identityManager.setIdentityStoreFactory(new StoreFactory() {

                @Override
                public IdentityStore createIdentityStore(IdentityStoreConfiguration config, IdentityStoreInvocationContext ctx) {
                    return store;
                }

                @Override
                public void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
                        Class<? extends IdentityStore> storeClass) {
                    // no-op
                }

                @Override
                public PartitionStore createPartitionStore(PartitionStoreConfiguration config) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void mapPartitionConfiguration(Class<? extends PartitionStoreConfiguration> configClass,
                        Class<? extends PartitionStore> storeClass) {
                    // TODO Auto-generated method stub
                    
                }
            });
            // hack end

            this.identityManager.bootstrap(identityConfig,
                    new DefaultIdentityStoreInvocationContextFactory(null));
        }

        return this.identityManager;
    }
}
