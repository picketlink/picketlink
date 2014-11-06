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

/**
 * CORS origin denied (not allowed) exception.
 *
 * @author Giriraj Sharma
 */
public class CORSOriginDeniedException extends CORSException {

    private static final long serialVersionUID = 1L;
    /**
     * The request origin.
     */
    private final Origin requestOrigin;

    /**
     * Creates a new CORS origin denied exception with the specified message.
     *
     * @param message The message.
     */
    public CORSOriginDeniedException(final String message) {

        this(message, null);
    }

    /**
     * Creates a new CORS origin denied exception with the specified message and request origins.
     *
     * @param message The message.
     * @param requestOrigin The request origin, {@code null} if unknown.
     */
    public CORSOriginDeniedException(final String message, final Origin requestOrigin) {

        super(message);

        this.requestOrigin = requestOrigin;
    }

    /**
     * Gets the request origin.
     *
     * @return The request origin, {@code null} if unknown or not set.
     */
    public Origin getRequestOrigin() {

        return requestOrigin;
    }
}
