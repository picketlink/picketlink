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

import static org.picketlink.config.http.OutboundRedirectConfiguration.Condition;

/**
 * <p>Provides a set of options to configure how responses should be handled for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class OutboundRedirectConfigurationBuilder extends AbstractPathConfigurationChildBuilder {

    private String redirectUrl;
    private Condition condition;

    OutboundRedirectConfigurationBuilder(PathConfigurationBuilder parentBuilder, String redirectUrl) {
        super(parentBuilder);
        this.redirectUrl = redirectUrl;
    }

    /**
     * <p>Redirects to a given url only if a request was forbidden. In other words, when a request is not authorized.</p>
     *
     * @return
     */
    public OutboundRedirectConfigurationBuilder whenForbidden() {
        this.condition = Condition.FORBIDDEN;
        return this;
    }

    /**
     * <p>Redirects to a given url if there was any error during the request processing.</p>
     *
     * @return
     */
    public OutboundRedirectConfigurationBuilder whenError() {
        this.condition = Condition.ERROR;
        return this;
    }

    @Override
    protected OutboundRedirectConfiguration create() {
        return new OutboundRedirectConfiguration(this.redirectUrl, this.condition);
    }
}