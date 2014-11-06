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
package org.picketlink.http.internal.cors.util;

/**
 * Thrown on a property parse exception. Intended to report missing or invalid properties.
 *
 * @author Giriraj Sharma
 */
public class PropertyParseException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The key of the property that caused the exception, {@code null} if unknown or not applicable.
     */
    private final String propertyKey;

    /**
     * The value of the property that caused the exception, {@code null} if unknown or not applicable.
     */
    private final String propertyValue;

    /**
     * Creates a new property parse exception with the specified message.
     *
     * @param message The exception message.
     */
    public PropertyParseException(final String message) {

        super(message);
        propertyKey = null;
        propertyValue = null;
    }

    /**
     * Creates a new property parse exception with the specified message and property key.
     *
     * @param message The exception message.
     * @param propertyKey The key of the property that caused the exception, {@code null} if unknown or not applicable.
     */
    public PropertyParseException(final String message, final String propertyKey) {

        super(message);
        this.propertyKey = propertyKey;
        propertyValue = null;
    }

    /**
     * Creates a new property parse exception with the specified message, property key and property value.
     *
     * @param message The exception message.
     * @param propertyKey The key of the property that caused the exception, {@code null} if unknown or not applicable.
     * @param propertyValue The value of the property that caused the exception, {@code null} if unknown or not applicable.
     */
    public PropertyParseException(final String message, final String propertyKey, final String propertyValue) {

        super(message);
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    /**
     * Returns the key of the property that caused the exception, {@code null} if unknown or not applicable.
     *
     * @return The key of the offending property.
     */
    public String getPropertyKey() {

        return propertyKey;
    }

    /**
     * Returns the value of the property that caused the exception, {@code null} if unknown or not applicable.
     *
     * @return The value of the offending property.
     */
    public String getPropertyValue() {

        return propertyValue;
    }
}
