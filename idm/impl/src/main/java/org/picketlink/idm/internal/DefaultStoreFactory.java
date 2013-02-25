/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.idm.internal;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default StoreFactory implementation. This factory is pre-configured to be able to create instances of the following built-in
 * IdentityStore implementations based on the corresponding IdentityStoreConfiguration:
 * 
 * JPAIdentityStore - JPAIdentityStoreConfiguration 
 * LDAPIdentityStore - LDAPConfiguration
 * FileBasedIdentityStore - FileIdentityStoreConfiguration
 * 
 * 
 * @author Shane Bryzak
 */
public class DefaultStoreFactory implements StoreFactory {

    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap = 
            new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    public DefaultStoreFactory() {
        identityConfigMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        identityConfigMap.put(LDAPIdentityStoreConfiguration.class, LDAPIdentityStore.class);
        identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);
    }

    @Override
    public IdentityStore<?> createIdentityStore(IdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        for (Class<? extends IdentityStoreConfiguration> cc : identityConfigMap.keySet()) {
            if (cc.isInstance(config)) {
                try {
                    IdentityStore<?> identityStore = (IdentityStore<?>) identityConfigMap
                            .get(cc).newInstance();
                    return identityStore;
                } catch (InstantiationException e) {
                    throw new SecurityConfigurationException("Exception while creating new IdentityStore instance", e);
                } catch (IllegalAccessException e) {
                    throw new SecurityConfigurationException("Exception while creating new IdentityStore instance", e);
                }
            }
        }

        throw new IllegalArgumentException(
                "The IdentityStoreConfiguration specified is not supported by this IdentityStoreFactory implementation");
    }

    @Override
    public void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore<?>> storeClass) {
        identityConfigMap.put(configClass, (Class<? extends IdentityStore<?>>) storeClass);
    }
}
