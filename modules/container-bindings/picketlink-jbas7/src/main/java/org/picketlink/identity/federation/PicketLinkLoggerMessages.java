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
    void xacmlSendingDecisionQuery(String xacmlDecisionQueryDocument);

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
    void xmllookingParserForElement(QName qname);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 206, value = "XACML Received Message: %s")
    void xacmlReceivedMessage(String asString);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 207, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void stsTokenRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 208, value = "%s is not an instance of SecurityTokenRegistry - using default registry")
    void stsTokenRegistryInvalidType(String tokenRegistryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 209, value = "Error instantiating token registry class - using default registry")
    void stsTokenRegistryInstantiationError();

    @LogMessage(level = Level.WARN)
    @Message(id = 210, value = "Revocation registry option not specified: cancelled ids will not be persisted!")
    void stsRevocationRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 211, value = "%s is not an instance of RevocationRegistry - using default registry")
    void stsRevocationRegistryInvalidType(String registryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 212, value = "Error instantiating revocation registry class - using default registry")
    void stsRevocationRegistryInstantiationError();

    @LogMessage(level = Level.TRACE)
    @Message(id = 213, value = "%s does not exist. Hence creating.")
    void samlMetaDataDirectoryCreation(String directory);

    @LogMessage(level = Level.ERROR)
    @Message(id = 214, value = "Exception loading the identity providers")
    void samlMetaDataIdentityProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 215, value = "Exception loading the service providers")
    void samlMetaDataServiceProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 216, value = "Persisted entity descriptor into %s")
    void samlMetaDataPersistEntityDescriptor(String path);

    @LogMessage(level = Level.TRACE)
    @Message(id = 217, value = "Persisted trusted map into %s")
    void samlMetaDataPersistTrustedMap(String path);

    @LogMessage(level = Level.ERROR)
    @Message(id = 218, value = "Cannot validate signature of assertion")
    void signatureAssertionValidationError(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 219, value = "Now=%s ::notBefore=%s ::notOnOrAfter=%s")
    void samlAssertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);

    @LogMessage(level = Level.INFO)
    @Message(id = 220, value = "Assertion has expired with id=%s")
    void samlAssertionExpired(String id);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 221, value = "[InstallDefaultConfiguration] Configuration is null. Creating a new configuration")
    void stsCreatingDefaultSTSConfig();

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
    void xmlCouldNotGetSchema(@Cause Throwable t);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 229, value = "The provider %s could not be added")
    void jceProviderCouldNotBeLoaded(String name, @Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 230, value = "Issuing token for principal %s")
    void samlIssuingTokenForPrincipal(Principal callerPrincipal);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 231, value = "Lifetime has not been specified. Using the default timeout value.")
    void stsTokenTimeoutNotSpecified();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 232, value = "Claims have been specified in the request but no processor was found for dialect %s")
    void wsTrustClaimsDialectProcessorNotFound(String dialect);

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
    void authUserNameFromCallbackisNull();

    @LogMessage(level = Level.TRACE)
    @Message(id = 249, value = "Password from callback is null")
    void authPasswordFromCallbackIsNull();

    @LogMessage(level = Level.WARN)
    @Message(id = 250, value = "SAML Assertion has been found to have no expiration: ID = %s")
    void authSAMLAssertionWithoutExpiration(String id);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 251, value = "SAML Token Validation result: %s")
    void authSAMLValidationResult(boolean result);

    @LogMessage(level = Level.TRACE)
    @Message(id = 252, value = "SAML token validation started")
    void samlStartingValidation();

    @LogMessage(level = Level.TRACE)
    @Message(id = 253, value = "No attribute provider set")
    void stsNoAttributeProviderSet();

    @LogMessage(level = Level.WARN)
    @Message(id = 254, value = "Attribute provider not installed: %s is not an instance of SAML20TokenAttributeProvider")
    void stsWrongAttributeProviderTypeNotInstalled(String attributeProviderClassName);

    @LogMessage(level = Level.WARN)
    @Message(id = 255, value = "Error instantiating attribute provider")
    void stsAttributeProviderInstantiationError(@Cause Throwable t);

    @LogMessage(level = Level.WARN)
    @Message(id = 256, value = "Assertion Element = %s")
    void samlAssertion(String nodeAsString);

    @LogMessage(level = Level.WARN)
    @Message(id = 257, value = "Active Session Count = %s")
    void samlIdentityServerActiveSessionCount(int activeSessionCount);

    @LogMessage(level = Level.WARN)
    @Message(id = 258, value = "Session Created with id = %s ::active session count = %s")
    void samlIdentityServerSessionCreated(String id, int activeSessionCount);

    @LogMessage(level = Level.TRACE)
    @Message(id = 259, value = "RoleGenerator set to %s")
    void samlHandlerRoleGeneratorSetup(String name);

    @LogMessage(level = Level.ERROR)
    @Message(id = 260, value = "Exception initializing role generator")
    void samlHandlerRoleGeneratorSetupError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 261, value = "AttributeManager set to %s")
    void samlHandlerAttributeSetup(String name);

    @LogMessage(level = Level.TRACE)
    @Message(id = 262, value = "Destination = %s")
    void destination(String destination);

    @LogMessage(level = Level.TRACE)
    @Message(id = 263, value = "Exception in processing authentication")
    void samlHandlerAuthenticationError(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 264, value = "Invalid role: %s")
    void invalidRole(String roles);

    @LogMessage(level = Level.WARN)
    @Message(id = 265, value = "Session Destroyed with id = %s ::active session count = %s")
    void samlIdentityServerSessionDestroyed(String id, int activeSessionCount);

    @LogMessage(level = Level.TRACE)
    @Message(id = 266, value = "ID of authentication request %s saved into HTTP session.")
    void samlHandlerSavedAuthnRequestIdIntoSession(String authnRequestId);

    @LogMessage(level = Level.TRACE)
    @Message(id = 267, value = "Successful verification of InResponseTo for request %s")
    void samlHandlerSuccessfulInResponseToValidation(String inResponseTo);

    @LogMessage(level = Level.TRACE)
    @Message(id = 268, value = "Verification of InResponseTo failed. InResponseTo from SAML response is %s. Value of request Id from HTTP session is %s")
    void samlHandlerFailedInResponseToVerification(String inResponseTo, String authnRequestId);

    @LogMessage(level = Level.TRACE)
    @Message(id = 269, value = "Domains that IDP trusts = %s and issuer domain = %s")
    void samlHandlerDomainsTrustedByIDP(String domainsTrusted, String issuerDomain);

    @LogMessage(level = Level.TRACE)
    @Message(id = 270, value = "Matching uri bit = %s")
    void samlHandlerTrustDomainCheck(String uriBit);

    @LogMessage(level = Level.TRACE)
    @Message(id = 271, value = "Matched %s trust for %s")
    void samlHandlerTrustedDomainMatched(String uriBit, String issuerDomain);

    @LogMessage(level = Level.TRACE)
    @Message(id = 272, value = "Domains that SP trusts = %s and issuer domain = %s")
    void samlHandlerDomainsTrustedBySP(String domainsTrusted, String issuerDomain);

    @LogMessage(level = Level.TRACE)
    @Message(id = 273, value = "Generating Success Status Response for %s")
    void samlHandlerGeneratingSuccessStatusResponse(String originalIssuer);

    @LogMessage(level = Level.TRACE)
    @Message(id = 274, value = "No document generated in the handler chain. Cannot generate signature")
    void samlHandlerNoDocumentToSign();

    @LogMessage(level = Level.TRACE)
    @Message(id = 275, value = "No response document found")
    void samlHandlerNoResponseDocumentFound();

    @LogMessage(level = Level.TRACE)
    @Message(id = 276, value = "Going to sign response document with POST binding type")
    void samlHandlerSigningDocumentForPOSTBinding();

    @LogMessage(level = Level.TRACE)
    @Message(id = 277, value = "Going to sign response document with REDIRECT binding type")
    void samlHandlerSigningDocumentForRedirectBinding();

    @LogMessage(level = Level.ERROR)
    @Message(id = 278, value = "Key Pair cannot be found")
    void samlHandlerKeyPairNotFound();

    @LogMessage(level = Level.ERROR)
    @Message(id = 279, value = "Error when trying to sign message for redirection")
    void samlHandlerErrorSigningRedirectBindingMessage(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 280, value = "HTTP method for validating response: %s")
    void samlHandlerValidatingResponseForHTTPMethod(String method);

    @LogMessage(level = Level.ERROR)
    @Message(id = 281, value = "Error validating signature")
    void samlHandlerErrorValidatingSignature(@Cause Throwable t);

    @LogMessage(level = Level.TRACE)
    @Message(id = 282, value = "User has closed the browser. So we proceed to cancel the STS issued token.")
    void samlIDPUserClosedBrowserCancelingToken();

    @LogMessage(level = Level.TRACE)
    @Message(id = 283, value = "SAML Handlers are: %s")
    void samlHandlerList(String handlers);

    @LogMessage(level = Level.TRACE)
    @Message(id = 284, value = "Finished Processing handler: %s")
    void samlHandlerFinishedProcessing(String handlerClassName);

    @LogMessage(level = Level.TRACE)
    @Message(id = 285, value = "SAML Request Document: %s")
    void samlRequestDocument(String samlRequestDocument);

    @LogMessage(level = Level.ERROR)
    @Message(id = 286, value = "Error in base64 decoding saml message")
    void samlBase64DecodingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 287, value = "Exception in parsing saml message")
    void samlParsingError(@Cause Throwable t);
}