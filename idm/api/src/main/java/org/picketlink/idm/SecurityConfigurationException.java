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
package org.picketlink.idm;

/**
 * This exception is thrown when a problem is found with the Security API configuration
 *
 */
public class SecurityConfigurationException extends SecurityException {
    private static final long serialVersionUID = -8895836939958745981L;

    public SecurityConfigurationException() {
        super();
    }

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityConfigurationException(String message) {
        super(message);
    }

    public SecurityConfigurationException(Throwable cause) {
        super(cause);
    }

}
