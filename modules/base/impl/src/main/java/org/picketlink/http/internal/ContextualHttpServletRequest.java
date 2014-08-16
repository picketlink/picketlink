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
package org.picketlink.http.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Pedro Igor
 */
public class ContextualHttpServletRequest extends HttpServletRequestWrapper {

    private int invalidationStatusCode = -1;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     *
     * @throws IllegalArgumentException if the request is null
     */
    public ContextualHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void invalidate(int statusCode) {
        this.invalidationStatusCode = statusCode;
    }

    public int getInvalidationStatusCode() {
        return this.invalidationStatusCode;
    }

    public boolean isValid() {
        return this.invalidationStatusCode == -1;
    }

    public boolean isForbidden() {
        return this.invalidationStatusCode == HttpServletResponse.SC_FORBIDDEN;
    }

    public boolean hasError() {
        return this.invalidationStatusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
