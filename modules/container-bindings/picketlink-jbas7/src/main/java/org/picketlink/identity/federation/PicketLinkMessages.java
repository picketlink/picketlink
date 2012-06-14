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

import java.io.IOException;

import javax.xml.stream.Location;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Messages;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@MessageBundle(projectCode = "PLFED")
public interface PicketLinkMessages {

    PicketLinkMessages MESSAGES = Messages.getBundle(PicketLinkMessages.class);

    @Message (id = 5, value = "Error parsing the configuration file")
    ConfigurationException stsConfigurationFileParsingError(@Cause Throwable t);

    @Message (id = 13, value = "No Security Token Provider found in configuration:[%s][ProtoCtx=%s]")
    ProcessingException stsNoTokenProviderError(String configuration, String protocolContext);

    @Message(id = 16, value = "Should not be the same: %s")
    IllegalArgumentException shouldNotBeTheSame(String message);

    @Message(id = 18, value = "Resource not found: %s")
    ProcessingException resourceNotFound(String fileName);

    @Message (id = 28, value = "Audit Manager Is Not Set")
    IllegalStateException auditNullAuditManager();

    @Message (id = 55, value = "KeyStoreKeyManager : KeyStore is null")
    IllegalStateException keyStoreNullStore();

    @Message (id = 56, value = "KeyStoreKeyManager : Keystore not located: %s")
    RuntimeException keyStoreNotLocated(String keyStore);

    @Message (id = 57, value = "KeyStoreKeyManager : Signing Key Pass is null")
    RuntimeException keyStoreNullSigningKeyPass();

    @Message (id = 58, value = "KeyStoreKeyManager : Domain Alias missing for : %s")
    IllegalStateException keyStoreMissingDomainAlias(String domain);

    @Message (id = 59, value = "KeyStoreKeyManager : Alias is null")
    IllegalStateException keyStoreNullAlias();

    @Message (id = 60, value = "Parser : Expected start tag: %s ::Found <%s>")
    RuntimeException parserExpectedTag(String tag, String foundElementTag);

    @Message (id = 61, value = "Parser: Unknown End Element: %s")
    RuntimeException parserUnknownEndElement(String endElementName);

    @Message (id = 62, value = "Parser : Unknown tag: %s ::location= %s")
    RuntimeException parseUnknownTag(String tag, Location location);

    @Message (id = 63, value = "Parser: Required attribute missing: %s")
    ParsingException parseRequiredAttribute(String attribute);

    @Message (id = 64, value = "Parser: Unknown Start Element: %s ::location= %s")
    RuntimeException parserUnknownStartElement(String elementName, Location location);

    @Message (id = 65, value = "Parser : Unknown xsi:type= %s")
    ParsingException parserUnknownXSI(String xsiTypeValue);

    @Message (id = 66, value = "Parser : Expected end tag: %s")
    ParsingException parserExpectedEndTag(String tagName);

    @Message (id = 67, value = "Parser : Parsing has failed: %s")
    RuntimeException parserFailed(String elementName);

    @Message (id = 68, value = "Parser : Start Element is null")
    IllegalStateException parserNullStartElement();

    @Message(id = 69, value = "Parser: Type not supported: %s")
    RuntimeException unsupportedType(String name);

    @Message (id = 70, value = "Parser : Parsing exception.")
    ParsingException parserException(@Cause Throwable t);

    @Message (id = 71, value = "Parser: Expected text value: %s")
    ParsingException parserExpectedTextValue(String string);
    
    @Message (id = 72, value = "Parser: Expected xsi:type: %s")
    RuntimeException parserExpectedXSI(String expectedXsi);
    
    @Message (id = 73, value = "Parser: Unable to parse token request: security token is null")
    ParsingException parserUnableParsingNullToken();

    @Message (id = 74, value = "Parsing Error.")
    ParsingException parserError(@Cause Throwable t);

    @Message (id = 75, value = "File could not be located : %s")
    IllegalStateException fileNotLocated(String policyConfigFileName);

    @Message(id = 76, value = "Option not set: %s")
    IllegalStateException optionNotSet(String option);

    @Message (id = 77, value = "Injected Value Missing: %s")
    RuntimeException injectedValueMissing(String value);

    @Message(id = 78, value = "Null Parameter: %s")
    IllegalArgumentException nullArgument(String argument);
    
    @Message(id = 79, value = "Assertion has expired")
    ProcessingException assertionExpiredError();

    @Message(id = 80, value = "Invalid Assertion")
    ProcessingException assertionInvalidError();

    @Message(id = 81, value = "Writer: Unknown Type: %s")
    RuntimeException writerUnknownTypeError(String name);    

    @Message (id = 82, value = "Not Implemented Yet: %s")
    RuntimeException notImplementedYet(String feature);

    @Message(id = 83, value = "Writer: Null Value: %s")
    ProcessingException writerNullValueError(String value);

    @Message (id = 84, value = "Writer: Unsupported Attribute Value: %s")
    RuntimeException writerUnsupportedAttributeValueError(String value);

    @Message (id = 85, value = "IssuerInfo missing status code")
    IllegalArgumentException issuerInfoMissingStatusCodeError();

    @Message (id = 86, value = "Cannot create instance of: %s")
    ProcessingException couldNotCreateInstance(String fqn, @Cause Throwable t);

    @Message (id = 87, value = "System Property missing: %s")
    RuntimeException systemPropertyMissingError(String property);

    @Message (id = 89, value = "Unknown Object Type: %s")
    RuntimeException unknownObjectType(Object attrValue);

    @Message (id = 90, value = "Unknown Signature Algorithm: %s")
    RuntimeException signatureUnknownAlgo(String algo);

    @Message (id = 91, value = "Writer: Invalid KeyInfo object: content cannot be empty")
    ProcessingException writerInvalidKeyInfoNullContentError();

    @Message (id = 92, value = "Null Value: %s")
    RuntimeException nullValue(String nullValue);
    
    @Message (id = 93, value = "Not Serializable: %s")
    IOException notSerializableError(String message);
    
    @Message (id = 94, value = "Not equal: %s and %s")
    RuntimeException notEqualError(String first, String second);
    
    @Message (id = 95, value = "Wrong type: %s")
    IllegalArgumentException wrongTypeError(String message);

    @Message(id = 100, value = "Signing Process Failure")
    ProcessingException signatureError(@Cause Throwable e);

    @Message(id = 102, value = "Processing Exception")
    ProcessingException processingError(@Cause Throwable t);

    @Message (id = 103, value = "Error while configuring the audit capabilities")
    ConfigurationException auditConfigurationError(@Cause Throwable t);

    @Message (id = 104, value = "Class Not Loaded: %s")
    ProcessingException classNotLoadedError(String fqn);

    @Message (id = 105, value = "Configuration Exception")
    ConfigurationException configurationError(@Cause Throwable t);

    @Message (id = 106, value = "PDP : Error while processing the message.")
    RuntimeException pdpMessageProcessingError(@Cause Throwable t);

    @Message (id = 107, value = "Invalid Argument Exception: %s")
    IllegalArgumentException invalidArgumentError(String message);

    @Message (id = 108, value = "KeyStoreKeyManager : Processing error")
    TrustKeyProcessingException keyStoreProcessingError(@Cause Throwable t);

    @Message (id = 109, value = "KeyStoreKeyManager : Configuration error.")
    TrustKeyConfigurationException keyStoreConfigurationError(@Cause Throwable t);

}