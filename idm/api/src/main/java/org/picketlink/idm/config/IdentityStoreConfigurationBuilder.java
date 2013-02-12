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
package org.picketlink.idm.config;

/**
 * Configuration Builder
 *
 * @author anil saldhana
 * @since Sep 6, 2012
 */
public abstract class IdentityStoreConfigurationBuilder {

    /**
     * Create a {@link IdentityStoreConfiguration} from the Fully Qualified Name of a Configuration Builder
     *
     * @param fqn
     * @return
     */
    public static IdentityStoreConfiguration config(String fqn) {
        try {
            Class<?> clazz = IdentityStoreConfigurationBuilder.class.getClassLoader().loadClass(fqn); // Need security actions
                                                                                                      // here
            return config(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a {@link IdentityStoreConfiguration} from the {@link Class} of a Configuration Builder
     *
     * @param clazz
     * @return
     */
    public static IdentityStoreConfiguration config(Class<?> clazz) {
        try {
            IdentityStoreConfigurationBuilder builder = (IdentityStoreConfigurationBuilder) clazz.newInstance();
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The {@link IdentityStoreConfiguration} that is built
     *
     * @return
     */
    public abstract IdentityStoreConfiguration build();
}