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

/**
 * Enumeration of the CORS request types.
 *
 * @author Giriraj Sharma
 */
public enum CORSRequestType {

    /**
     * Simple / actual CORS request.
     */
    ACTUAL,

    /**
     * Preflight CORS request.
     */
    PREFLIGHT,

    /**
     * Other (non-CORS) request.
     */
    OTHER;

    /**
     * Detects the CORS type of the specified HTTP request.
     *
     * @param request The HTTP request to check. Must not be {@code null}.
     *
     * @return The CORS request type.
     */
    public static CORSRequestType detect(final HttpServletRequest request) {

        if (request.getHeader(HeaderName.ORIGIN) == null) {

            // All CORS request have an Origin header
            return OTHER;
        }

        // Some browsers include the Origin header even when submitting
        // from the same domain. This is legal according to RFC 6454,
        // section-7.3
        String serverOrigin = request.getScheme() + "://" + request.getHeader(HeaderName.HOST);

        if (request.getHeader(HeaderName.HOST) != null && request.getHeader(HeaderName.ORIGIN).equals(serverOrigin)) {
            return OTHER;
        }

        // We have a CORS request - determine type
        if (request.getHeader(HeaderName.ACCESS_CONTROL_REQUEST_METHOD) != null && request.getMethod() != null
                && request.getMethod().equalsIgnoreCase("OPTIONS")) {

            return PREFLIGHT;

        } else {

            return ACTUAL;
        }
    }
}
