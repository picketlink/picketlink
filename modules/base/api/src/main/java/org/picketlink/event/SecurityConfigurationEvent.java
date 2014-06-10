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
package org.picketlink.event;

import org.picketlink.config.SecurityConfigurationBuilder;

/**
 * <p>This event is raised during PicketLink startup to allow applications to customize and provide their own
 * {@link org.picketlink.config.SecurityConfiguration}.</p>
 *
 * <p>Observers can handle this event in order to get any additional configuration added to the {@link
 * org.picketlink.config.SecurityConfigurationBuilder}</p> instance that will be used to build the final configuration.</p>
 *
 * <p>If there is no observer for this event or it does not provide any additional configuration, the default configuration will be used.</p>
 *
 * @author Pedro Igor
 */
public class SecurityConfigurationEvent {

    private final SecurityConfigurationBuilder securityConfigurationBuilder;

    public SecurityConfigurationEvent() {
        this.securityConfigurationBuilder = new SecurityConfigurationBuilder();
    }

    public SecurityConfigurationBuilder getBuilder() {
        return this.securityConfigurationBuilder;
    }
}
