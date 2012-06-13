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
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@MessageLogger(projectCode = "PLFED")
public interface PicketLinkLoggerMessages extends BasicLogger {

    PicketLinkLoggerMessages ROOT_LOGGER = Logger.getMessageLogger(PicketLinkLoggerMessages.class, PicketLinkLoggerMessages.class.getPackage().getName());

    @LogMessage (level=Level.TRACE)
    @Message(id = 70, value = "SAML Response Document: %s")
    void samlResponseDocument(String samlResponseDocumentAsString);

    @LogMessage (level=Level.DEBUG)
    @Message(id = 71, value = "Sending XACML Decision Query:: %s")
    void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument);

    @LogMessage (level=Level.INFO)
    @Message(id = 72, value = "PicketLink Audit Event raised:: %s")
    void auditEvent(String auditEvent);

    @LogMessage (level=Level.INFO)
    @Message(id = 73, value = "Keystore is null. so setting it up")
    void keyStoreSetup();

    @LogMessage (level=Level.INFO)
    @Message(id = 74, value = "No public key found for alias = %s")
    void keyStoreNullPublicKeyForAlias(String alias);
    
}