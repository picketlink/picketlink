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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * HTTP response wrapper that preserves the CORS response headers on {@link javax.servlet.ServletResponse#reset()}. Some web
 * applications and frameworks (e.g. RestEasy) reset the servlet response when a HTTP 4xx error is produced; this wrapper
 * ensures previously set CORS headers survive such a reset.
 *
 * @author Giriraj Sharma
 */
public class CORSResponseWrapper extends HttpServletResponseWrapper {

    /**
     * The names of the CORS response headers to preserve.
     */
    public static final Set<String> RESPONSE_HEADER_NAMES;

    static {
        Set<String> headerNames = new HashSet<String>();
        headerNames.add(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN);
        headerNames.add(HeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        headerNames.add(HeaderName.ACCESS_CONTROL_EXPOSE_HEADERS);
        headerNames.add(HeaderName.VARY);
        RESPONSE_HEADER_NAMES = Collections.unmodifiableSet(headerNames);
    }

    /**
     * Creates a new CORS response wrapper for the specified HTTP servlet response.
     *
     * @param response The HTTP servlet response.
     */
    public CORSResponseWrapper(final HttpServletResponse response) {

        super(response);
    }

    @Override
    public void reset() {

        Map<String, String> corsHeaders = new HashMap<String, String>();

        for (String headerName : getHeaderNames()) {
            if (RESPONSE_HEADER_NAMES.contains(headerName)) {
                // save
                corsHeaders.put(headerName, getHeader(headerName));
            }
        }

        super.reset();

        for (String headerName : corsHeaders.keySet()) {
            setHeader(headerName, corsHeaders.get(headerName));
        }
    }
}
