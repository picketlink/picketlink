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

package org.picketlink.identity.federation;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;

/**
 * <p>This interface acts as a Logger Facade for PicketLink, from which exceptions and messages should be created or logged.</p>
 * <p>As PicketLink supports multiple containers and its versions, the main objective of this interface is to abstract the logging aspects from the code and provide different logging implementations
 * for each supported binding/container.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * @see {@link PicketLinkLoggerFactory}
 */
public interface PicketLinkLogger {

    /**
     * <p>Creates an {@link IllegalArgumentException} for null arguments.</p>
     * 
     * @param argument
     * @return
     */
    IllegalArgumentException nullArgument(String argument);

    /**
     * <p>Creates an {@link IllegalArgumentException} for arguments that should not be the same.</p>
     * 
     * @param string
     * @return
     */
    IllegalArgumentException shouldNotBeTheSame(String string);

    /**
     * <p>Creates an {@link ProcessingException} for resources that are not found.</p>
     * 
     * @param resource
     * @return
     */
    ProcessingException resourceNotFound(String resource);

    /**
     * <p>Creates an {@link ProcessingException} for generics processing errors.</p>
     * 
     * @param t
     * @return
     */
    ProcessingException processingError(Throwable t);

    /**
     * <p>Creates an {@link RuntimeException} for not supported types.</p>
     * 
     * @param name
     * @return
     */
    RuntimeException unsupportedType(String name);

    /**
     * <p>Logs the SAML Response document.</p>
     * 
     * @param samlResponseDocumentAsString
     */
    void samlResponseDocument(String samlResponseDocumentAsString);
}