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
package org.picketlink.idm.spi;

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