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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Provides a set of options regarding the request headers for a specific request.</p>
 *
 * @author Pedro Igor
 */
public class InboundHeaderConfigurationBuilder extends AbstractPathConfigurationChildBuilder {

    private final Map<String, String[]> headers;

    InboundHeaderConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
        this.headers = new HashMap<String, String[]>();
    }

    /**
     * <p>The name of the header used to identify the source of the request. For insance, usefull to identify
     * whether a request came or not from an Ajax client.</p>
     *
     * <p>This method uses the <code>@{link InboundHeaderConfiguration.X_REQUESTED_WITH_HEADER_NAME}</code>.</p>
     *
     * @param value
     * @return
     */
    public InboundHeaderConfigurationBuilder requestedWith(String value) {
        return header(InboundHeaderConfiguration.X_REQUESTED_WITH_HEADER_NAME, value);
    }

    /**
     * <p>Defines a header that will be used to identify requests for a specific path.</p>
     *
     * <p>When a header is defined, it will be used to match a specific request.</p>
     *
     * @param name
     * @param value
     * @return
     */
    public InboundHeaderConfigurationBuilder header(String name, String... value) {
        this.headers.put(name, value);
        return this;
    }

    InboundHeaderConfiguration create(PathConfiguration pathConfiguration) {
        return new InboundHeaderConfiguration(pathConfiguration, this.headers);
    }
}