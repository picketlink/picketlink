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
package org.picketlink.config.http;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Provides a set of options to configure how responses should be handled for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class OutboundConfigurationBuilder extends AbstractPathConfigurationChildBuilder implements OutboundConfigurationChildBuilder {

    private final PathConfigurationBuilder parentBuilder;
    private final List<OutboundRedirectConfigurationBuilder> redirects = new ArrayList<OutboundRedirectConfigurationBuilder>();

    OutboundConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
        this.parentBuilder = parentBuilder;
    }

    /**
     * <p>Specifies a url that will be used to redirect the user after a specific path is processed.</p>
     *
     * <p>For instance, after a logout request you may use this method to redirect the user to a different url.</p>
     *
     * @param redirectUrl
     * @return
     */
    @Override
    public OutboundRedirectConfigurationBuilder redirectTo(String redirectUrl) {
        OutboundRedirectConfigurationBuilder builder = new OutboundRedirectConfigurationBuilder(this, redirectUrl);

        this.redirects.add(builder);

        return builder;
    }

    OutboundConfiguration create(PathConfiguration pathConfiguration) {
        List<OutboundRedirectConfiguration> redirectConfigurations = new ArrayList<OutboundRedirectConfiguration>();

        for (OutboundRedirectConfigurationBuilder builder : this.redirects) {
            redirectConfigurations.add(builder.create());
        }

        return new OutboundConfiguration(pathConfiguration, redirectConfigurations);
    }
}