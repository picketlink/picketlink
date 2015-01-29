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
package org.picketlink.http.internal.cors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.config.http.CORSConfiguration;
import org.picketlink.config.http.PathConfiguration;

/**
 * <p>
 * A default implementation of {@link org.picketlink.http.cors.CORSPathAuthorizer}.
 * </p>
 *
 * @author Giriraj Sharma
 */
public class CORSRequestsPathAuthorizer extends AbstractCORSPathAuthorizer {

    @Override
    protected boolean doAuthorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {

        CORSConfiguration corsConfiguration = pathConfiguration.getCORSConfiguration();

        CORSRequestType type = CORSRequestType.detect(request);
        CORS cors = new CORS(corsConfiguration);

        if (type.equals(CORSRequestType.ACTUAL)) {
            // Simple / actual CORS request
            cors.handleActualRequest(corsConfiguration, request, response);

        } else if (type.equals(CORSRequestType.PREFLIGHT)) {

            // Preflight CORS request
            cors.handlePreflightRequest(corsConfiguration, request, response);

        } else if (corsConfiguration.isGenericHttpRequestsAllowed()) {
            // Not a CORS request, allow it through

        } else {
            // Generic HTTP requests denied
            throw new RuntimeException("Generic HTTP requests not allowed");
        }

        return true;
    }
}
