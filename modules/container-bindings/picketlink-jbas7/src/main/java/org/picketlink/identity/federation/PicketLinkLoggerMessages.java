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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
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
    @Message(id = 200, value = "SAML Response Document: %s")
    void samlResponseDocument(String samlResponseDocumentAsString);

    @LogMessage (level=Level.DEBUG)
    @Message(id = 201, value = "Sending XACML Decision Query: %s")
    void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument);

    @LogMessage (level=Level.INFO)
    @Message(id = 202, value = "PicketLink Audit Event raised: %s")
    void auditEvent(String auditEvent);

    @LogMessage (level=Level.INFO)
    @Message(id = 203, value = "Keystore is null. so setting it up")
    void keyStoreSetup();

    @LogMessage (level=Level.INFO)
    @Message(id = 204, value = "No public key found for alias = %s")
    void keyStoreNullPublicKeyForAlias(String alias);

    @LogMessage (level=Level.TRACE)
    @Message(id = 205, value = "Looking for parser for element: %s")
    void lookingParserForElement(QName qname);

    @LogMessage (level=Level.DEBUG)
    @Message(id = 206, value = "XACML Received Message: %s")
    void receivedXACMLMessage(String asString);

    @LogMessage (level=Level.DEBUG)
    @Message(id = 207, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void securityTokenRegistryNotSpecified();

    @LogMessage (level=Level.WARN)
    @Message(id = 208, value = "%s is not an instance of SecurityTokenRegistry - using default registry")
    void securityTokenRegistryInvalidType(String tokenRegistryOption);

    @LogMessage (level=Level.WARN)
    @Message(id = 209, value = "Error instantiating token registry class - using default registry")
    void securityTokenRegistryInstantiationError();

    @LogMessage (level=Level.WARN)
    @Message(id = 210, value = "Revocation registry option not specified: cancelled ids will not be persisted!")
    void revocationRegistryNotSpecified();

    @LogMessage (level=Level.WARN)
    @Message(id = 211, value = "%s is not an instance of RevocationRegistry - using default registry")
    void revocationRegistryInvalidType(String registryOption);

    @LogMessage (level=Level.WARN)
    @Message(id = 212, value = "Error instantiating revocation registry class - using default registry")
    void revocationRegistryInstantiationError();

    @LogMessage (level=Level.TRACE)
    @Message(id = 213, value = "%s does not exist. Hence creating.")
    void metaDataDirectoryCreation(String directory);

    @LogMessage (level=Level.ERROR)
    @Message(id = 214, value = "Exception loading the identity providers")
    void metaDataIdentityProviderLoadingError(@Cause Throwable t);

    @LogMessage (level=Level.ERROR)
    @Message(id = 215, value = "Exception loading the service providers")
    void metaDataServiceProviderLoadingError(@Cause Throwable t);

    @LogMessage (level=Level.TRACE)
    @Message(id = 216, value = "Persisted entity descriptor into %s")
    void metaDataPersistEntityDescriptor(String path);

    @LogMessage (level = Level.TRACE)
    @Message (id = 217, value="Persisted trusted map into %s")
    void metaDataPersistTrustedMap(String path);

    @LogMessage (level=Level.ERROR)
    @Message(id = 218, value = "Cannot validate signature of assertion")
    void signatureAssertionValidationError(@Cause Throwable t);

    @LogMessage (level=Level.TRACE)
    @Message(id = 219, value = "Now=%s ::notBefore=%s ::notOnOrAfter=%s")
    void assertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);

    @LogMessage (level=Level.INFO)
    @Message(id = 220, value = "Assertion has expired with id=%s")
    void assertionExpired(String id);
    
}