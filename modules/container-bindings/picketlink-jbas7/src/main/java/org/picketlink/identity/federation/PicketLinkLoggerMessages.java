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

import java.security.Principal;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@MessageLogger(projectCode = "PLFED")
public interface PicketLinkLoggerMessages extends BasicLogger {

    PicketLinkLoggerMessages ROOT_LOGGER = Logger.getMessageLogger(PicketLinkLoggerMessages.class,
            PicketLinkLoggerMessages.class.getPackage().getName());

    @LogMessage(level = Level.TRACE)
    @Message(id = 200, value = "SAML Response Document: %s")
    void samlResponseDocument(String samlResponseDocumentAsString);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 201, value = "Sending XACML Decision Query: %s")
    void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument);

    @LogMessage(level = Level.INFO)
    @Message(id = 202, value = "PicketLink Audit Event raised: %s")
    void auditEvent(String auditEvent);

    @LogMessage(level = Level.INFO)
    @Message(id = 203, value = "Keystore is null. so setting it up")
    void keyStoreSetup();

    @LogMessage(level = Level.INFO)
    @Message(id = 204, value = "No public key found for alias = %s")
    void keyStoreNullPublicKeyForAlias(String alias);

    @LogMessage(level = Level.TRACE)
    @Message(id = 205, value = "Looking for parser for element: %s")
    void lookingParserForElement(QName qname);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 206, value = "XACML Received Message: %s")
    void receivedXACMLMessage(String asString);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 207, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void securityTokenRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 208, value = "%s is not an instance of SecurityTokenRegistry - using default registry")
    void securityTokenRegistryInvalidType(String tokenRegistryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 209, value = "Error instantiating token registry class - using default registry")
    void securityTokenRegistryInstantiationError();

    @LogMessage(level = Level.WARN)
    @Message(id = 210, value = "Revocation registry option not specified: cancelled ids will not be persisted!")
    void revocationRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 211, value = "%s is not an instance of RevocationRegistry - using default registry")
    void revocationRegistryInvalidType(String registryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 212, value = "Error instantiating revocation registry class - using default registry")
    void revocationRegistryInstantiationError();

    @LogMessage(level = Level.TRACE)
    @Message(id = 213, value = "%s does not exist. Hence creating.")
    void metaDataDirectoryCreation(String directory);

    @LogMessage(level = Level.ERROR)
    @Message(id = 214, value = "Exception loading the identity providers")
    void metaDataIdentityProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 215, value = "Exception loading the service providers")
    void metaDataServiceProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 216, value = "Persisted entity descriptor into %s")
    void metaDataPersistEntityDescriptor(String path);

    @LogMessage(level = Level.TRACE)
    @Message(id = 217, value = "Persisted trusted map into %s")
    void metaDataPersistTrustedMap(String path);

    @LogMessage(level = Level.ERROR)
    @Message(id = 218, value = "Cannot validate signature of assertion")
    void signatureAssertionValidationError(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 219, value = "Now=%s ::notBefore=%s ::notOnOrAfter=%s")
    void assertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);

    @LogMessage(level = Level.INFO)
    @Message(id = 220, value = "Assertion has expired with id=%s")
    void assertionExpired(String id);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 221, value = "[InstallDefaultConfiguration] Configuration is null. Creating a new configuration")
    void creatingDefaultSTSConfig();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 222, value = "[InstallDefaultConfiguration] Configuration file name=%s")
    void stsLoadingConfiguration(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 223, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundTCL(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 224, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundClassLoader(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 225, value = "%s configuration file not found using URL. Using default configuration values")
    void stsUsingDefaultConfiguration(String fileName);

    @LogMessage(level = Level.INFO)
    @Message(id = 226, value = "%s configuration file loaded")
    void stsConfigurationFileLoaded(String fileName);

    @LogMessage(level = Level.ERROR)
    @Message(id = 227, value = "Exception in getting TrustKeyManager")
    void trustKeyManagerCreationError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 228, value = "Cannot get schema")
    void couldNotGetXMLSchema(@Cause Throwable t);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 229, value = "The provider %s could not be added")
    void jceProviderCouldNotBeLoaded(String name, @Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 230, value = "Issuing token for principal %s")
    void issuingTokenForPrincipal(Principal callerPrincipal);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 231, value = "Lifetime has not been specified. Using the default timeout value.")
    void tokenTimeoutNotSpecified();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 232, value = "Claims have been specified in the request but no processor was found for dialect %s")
    void claimsDialectProcessorNotFound(String dialect);

    @LogMessage(level = Level.TRACE)
    @Message(id = 233, value = "Validating token for renew request %s")
    void stsValidatingTokenForRenewal(String details);

    @LogMessage(level = Level.TRACE)
    @Message(id = 234, value = "Security Token digital signature has NOT been verified. Either the STS has been configured not to sign tokens or the STS key pair has not been properly specified.")
    void stsSecurityTokenSignatureNotVerified();

    @LogMessage(level = Level.TRACE)
    @Message(id = 235, value = "Started validation for request %s")
    void stsStartedValidationForRequest(String details);

    @LogMessage(level = Level.TRACE)
    @Message(id = 236, value = "Going to validate signature for: %s")
    void signatureValidatingDocument(String nodeAsString);

    @LogMessage(level = Level.TRACE)
    @Message(id = 237, value = "Delegating token validation to token provider")
    void stsDelegatingValidationToTokenProvider();

    @LogMessage(level = Level.TRACE)
    @Message(id = 238, value = "NamespaceURI of element to be signed: %s")
    void signatureElementToBeSigned(String namespaceURI);

    @LogMessage(level = Level.TRACE)
    @Message(id = 239, value = "Signed Element: %s")
    void signatureSignedElement(String nodeAsString);

    @LogMessage(level = Level.TRACE)
    @Message(id = 240, value = "Locating public key for %s")
    void pkiLocatingPublicKey(String alias);

    @LogMessage(level = Level.TRACE)
    @Message(id = 241, value = "Security token should be encrypted but no encrypting key could be found")
    void stsSecurityTokenShouldBeEncrypted();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 242, value = "STS received request of type %s")
    void stsReceivedRequestType(String requestType);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 243, value = "No key type could be found in the request. Using the default BEARER type.")
    void stsKeyTypeNotFoundUsingDefaultBearer();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 244, value = "No key size could be found in the request. Using the default size. (%s)")
    void stsKeySizeNotFoundUsingDefault(long keySize);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 245, value = "Unable to parse the contents of the OnBehalfOfType: %s")
    void stsUnableToParseOnBehalfType(Object type);

    @LogMessage(level = Level.WARN)
    @Message(id = 246, value = "Secret key could not be encrypted because the endpoint's PKC has not been specified")
    void stsSecretKeyNotEncrypted();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 247, value = "Added Credential %s")
    void authAddedSAMLCredential(SamlCredential samlCredential);

    @LogMessage(level = Level.TRACE)
    @Message(id = 248, value = "UserName from callback is null")
    void userNameFromCallbackisNull();

    @LogMessage(level = Level.TRACE)
    @Message(id = 249, value = "Password from callback is null")
    void authPasswordFromCallbackIsNull();

    @LogMessage(level = Level.WARN)
    @Message(id = 250, value = "SAML Assertion has been found to have no expiration: ID = %s")
    void authSAMLAssertionWithoutExpiration(String id);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 251, value = "SAML Token Validation result: %s")
    void authSAMLValidationResult(boolean result);

}