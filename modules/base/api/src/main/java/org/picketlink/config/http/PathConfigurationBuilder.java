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

/**
 * <p>A configuration builder with covenience methods to configure protected paths.</p>
 *
 * @author Pedro Igor
 */
public class PathConfigurationBuilder extends AbstracHttpSecurityConfigurationChildBuilder implements PathConfigurationChildBuilder {

    private final String groupName;
    private final String uri;
    private Boolean secured;
    private InboundConfigurationBuilder inboundConfigurationBuilder;
    private OutboundConfigurationBuilder outboundConfigurationBuilder;
    private final HttpSecurityBuilder parentBuilder;

    PathConfigurationBuilder(String groupName, String uri, Boolean secured, HttpSecurityBuilder parentBuilder) {
        super(parentBuilder);
        this.groupName = groupName;
        this.uri = uri;
        this.parentBuilder = parentBuilder;
        this.secured = secured;
    }

    /**
     * <p>Provides a set of options to configure how requests should be handled for a specific path.</p>
     *
     * @return
     */
    public InboundConfigurationBuilder inbound() {
        if (this.inboundConfigurationBuilder == null) {
            this.inboundConfigurationBuilder = new InboundConfigurationBuilder(this);
        }

        return this.inboundConfigurationBuilder;
    }

    /**
     * <p>Provides a set of options to configure how responses should be handled for a specific path.</p>
     *
     * @return
     */
    public OutboundConfigurationBuilder outbound() {
        if (this.outboundConfigurationBuilder == null) {
            this.outboundConfigurationBuilder = new OutboundConfigurationBuilder(this);
        }

        return this.outboundConfigurationBuilder;
    }

    /**
     * <p>Mark a specific path as not protected. When a path is not protected, no security enforcement is applied.</p>
     *
     * @return
     */
    public PathConfigurationBuilder unprotected() {
        this.secured = false;
        return this;
    }

    @Override
    protected PathConfiguration create() {
        PathConfiguration pathConfiguration = new PathConfiguration(
            this.groupName,
            this.uri,
            this.secured);

        InboundConfiguration inboundConfiguration = null;

        if (this.inboundConfigurationBuilder != null) {
            inboundConfiguration = this.inboundConfigurationBuilder.create(pathConfiguration);
        }

        OutboundConfiguration outboundConfiguration = null;

        if (this.outboundConfigurationBuilder != null) {
            outboundConfiguration = this.outboundConfigurationBuilder.create(pathConfiguration);
        }

        pathConfiguration.setInboundConfiguration(inboundConfiguration);
        pathConfiguration.setOutboundConfiguration(outboundConfiguration);

        return pathConfiguration;
    }
}
