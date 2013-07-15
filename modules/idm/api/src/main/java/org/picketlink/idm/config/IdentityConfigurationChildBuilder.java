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
 * <p>Defines the top-level configuration methods for @{link Builder} implementations.</p>
 *
 * @author Pedro Igor
 *
 */
public interface IdentityConfigurationChildBuilder {

    /**
     * <p>Creates a named configuration.</p>
     *
     * @param configurationName
     * @return
     */
    NamedIdentityConfigurationBuilder named(String configurationName);

    /**
     * <p>Builds a single {@link IdentityConfiguration}. This method should be used when there is only a single configuration.</p>
     * <p>For multiple configurations see <code>buildAll</code>.</p>
     *
     * @return
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    IdentityConfiguration build() throws SecurityConfigurationException;

    /**
     * <p>Builds all named configurations.</p>
     *
     * @return
     * @throws  SecurityConfigurationException if any error occurs or for any invalid configuration
     */
    List<IdentityConfiguration> buildAll() throws SecurityConfigurationException;

}