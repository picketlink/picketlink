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

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@MessageBundle(projectCode = "PLFED")
public interface PicketLinkMessages {

    PicketLinkMessages MESSAGES = Messages.getBundle(PicketLinkMessages.class);

    @Message(id = 78, value = "Null Parameter: %s")
    IllegalArgumentException nullArgument(String argument);

    @Message(id = 16, value = "Should not be the same: %s")
    IllegalArgumentException shouldNotBeTheSame(String message);

    @Message(id = 18, value = "Resource not found: %s")
    ProcessingException resourceNotFound(String fileName);

    @Message(id = 102, value = "Processing Exception.")
    ProcessingException processingError(@Cause Throwable t);

    @Message(id = 69, value = "Parser: Type not supported: %s")
    RuntimeException unsupportedType(String name);

}