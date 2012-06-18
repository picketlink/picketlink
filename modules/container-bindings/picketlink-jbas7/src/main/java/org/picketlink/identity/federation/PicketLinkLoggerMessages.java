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

import java.util.Date;

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
    
    /* INFO */
    
    @LogMessage(level = Level.INFO)
    @Message(id = 202, value = "PicketLink Audit Event raised: %s")
    void auditEvent(String auditEvent);

    @LogMessage(level = Level.INFO)
    @Message(id = 203, value = "Keystore is null. so setting it up")
    void keyStoreSetup();

    @LogMessage(level = Level.INFO)
    @Message(id = 204, value = "No public key found for alias = %s")
    void keyStoreNullPublicKeyForAlias(String alias);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 220, value = "Assertion has expired with id=%s")
    void samlAssertionExpired(String id);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 226, value = "%s configuration file loaded")
    void stsConfigurationFileLoaded(String fileName);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 334, value = "Service Provider is setting the CanonicalizationMethod on XMLSignatureUtil: %s")
    void samlSPSettingCanonicalizationMethod(String canonicalizationMethod);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 330, value = "Identity Provider is setting the CanonicalizationMethod on XMLSignatureUtil: %s")
    void samlIDPSettingCanonicalizationMethod(String canonicalizationMethod);

    @LogMessage(level = Level.INFO)
    @Message(id = 331, value = "Did not find picketlink-sts.xml. We will install default configuration")
    void samlIDPInstallingDefaultSTSConfig();

    @LogMessage(level = Level.INFO)
    @Message(id = 335, value = "Cannot dispatch to the logout page: no request dispatcher: %s")
    void samlSPCouldNotDispatchToLogoutPage(String logOutPage);
    
    /* WARN */

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
    
    @LogMessage(level = Level.WARN)
    @Message(id = 223, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundTCL(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 224, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundClassLoader(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 225, value = "%s configuration file not found using URL. Using default configuration values")
    void stsUsingDefaultConfiguration(String fileName);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 246, value = "Secret key could not be encrypted because the endpoint's PKC has not been specified")
    void stsSecretKeyNotEncrypted();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 250, value = "SAML Assertion has been found to have no expiration: ID = %s")
    void authSAMLAssertionWithoutExpiration(String id);

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
    
    @LogMessage(level = Level.WARN)
    @Message(id = 265, value = "Session Destroyed with id = %s ::active session count = %s")
    void samlIdentityServerSessionDestroyed(String id, int activeSessionCount);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 313, value = "Was not able to create security token. Just sending message without binary token")
    void jbossWSUnableToCreateSecurityToken();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 327, value = "Token issuer is not a valid URL: %s. Using the requester address instead.")
    void samlIDPIssuerIsNotValidURLUsingRemoteAddr(String issuer, @Cause Throwable t);

    @LogMessage(level = Level.WARN)
    @Message(id = 328, value = "Unable to set the Identity Participant Stack Class. Will just use the default")
    void samlIDPUnableToSetParticipantStackUsingDefault(@Cause Throwable t);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 234, value = "Security Token digital signature has NOT been verified. Either the STS has been configured not to sign tokens or the STS key pair has not been properly specified.")
    void stsSecurityTokenSignatureNotVerified();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 241, value = "Security token should be encrypted but no encrypting key could be found")
    void stsSecurityTokenShouldBeEncrypted();

    
    /* ERROR */
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 214, value = "Exception loading the identity providers")
    void samlMetaDataIdentityProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 215, value = "Exception loading the service providers")
    void samlMetaDataServiceProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 218, value = "Cannot validate signature of assertion")
    void signatureAssertionValidationError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 227, value = "Exception in getting TrustKeyManager")
    void trustKeyManagerCreationError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 228, value = "Cannot get schema")
    void xmlCouldNotGetSchema(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 260, value = "Exception initializing role generator")
    void samlHandlerRoleGeneratorSetupError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 261, value = "AttributeManager set to %s")
    void samlHandlerAttributeSetup(String name);

    @LogMessage(level = Level.ERROR)
    @Message(id = 278, value = "Key Pair cannot be found")
    void samlHandlerKeyPairNotFound();

    @LogMessage(level = Level.ERROR)
    @Message(id = 279, value = "Error when trying to sign message for redirection")
    void samlHandlerErrorSigningRedirectBindingMessage(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 281, value = "Error validating signature")
    void samlHandlerErrorValidatingSignature(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 286, value = "Error in base64 decoding saml message")
    void samlBase64DecodingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 287, value = "Exception in parsing saml message")
    void samlParsingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 288, value = "Mapping Context returned is null")
    void attributeManagerMappingContextNull();

    @LogMessage(level = Level.ERROR)
    @Message(id = 289, value = "Exception in attribute mapping")
    void attributeManagerError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 290, value = "Could not obtain security context.")
    void couldNotObtainSecurityContext();
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 303, value = "SAML Assertion parsing failed")
    void authSAMLAssertionParsingFailed(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 310, value = "Unable to issue assertion")
    void authSAMLAssertionIssuingFailed(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 312, value = "Unable to create binary token")
    void jbossWSUnableToCreateBinaryToken(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 314, value = "Exception writing SOAP Message")
    void jbossWSUnableToWriteSOAPMessage(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 324, value = "Exception using backup method to get op name")
    void jbossWSErrorGettingOperationname(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 325, value = "Exception handling saml 11 use case")
    void samlIDPHandlingSAML11Error(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 326, value = "Exception in processing request")
    void samlIDPRequestProcessingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 329, value = "Exception dealing with handler configuration")
    void samlHandlerConfigurationError(@Cause Throwable t);
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 332, value = "Falling back on local Form Authentication if available")
    void samlSPFallingBackToLocalFormAuthentication();

    @LogMessage(level = Level.ERROR)
    @Message(id = 333, value = "Unable to obtain the IDP SSO Descriptor from metadata")
    void samlSPUnableToGetIDPDescriptorFromMetadata();
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 268, value = "Verification of InResponseTo failed. InResponseTo from SAML response is %s. Value of request Id from HTTP session is %s")
    void samlHandlerFailedInResponseToVerification(String inResponseTo, String authnRequestId);

    @LogMessage(level = Level.ERROR)
    @Message(id = 263, value = "Exception in processing authentication")
    void samlHandlerAuthenticationError(@Cause Throwable t);

    /* TRACE */
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 200, value = "SAML Response Document: %s")
    void samlResponseDocument(String samlResponseDocumentAsString);
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 205, value = "Looking for parser for element: %s")
    void xmllookingParserForElement(QName qname);

    @LogMessage(level = Level.TRACE)
    @Message(id = 219, value = "Now=%s ::notBefore=%s ::notOnOrAfter=%s")
    void samlAssertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 236, value = "Going to validate signature for: %s")
    void signatureValidatingDocument(String nodeAsString);

    @LogMessage(level = Level.TRACE)
    @Message(id = 270, value = "Matching uri bit = %s")
    void samlHandlerTrustDomainCheck(String uriBit);

    @LogMessage(level = Level.TRACE)
    @Message(id = 271, value = "Matched %s trust for %s")
    void samlHandlerTrustedDomainMatched(String uriBit, String issuerDomain);

    @LogMessage(level = Level.TRACE)
    @Message(id = 311, value = "Handling Outbound Message")
    void jbossWSHandlingOutboundMessage();

    @LogMessage(level = Level.TRACE)
    @Message(id = 315, value = "Header value has been identified %s")
    void jbossWSHeaderValueIdentified(String headerValue);

    @LogMessage(level = Level.TRACE)
    @Message(id = 316, value = "Cookie value has been identified %s")
    void jbossWSCookieValueIdentified(String cookie);

    @LogMessage(level = Level.TRACE)
    @Message(id = 317, value = "Handling Inbound Message")
    void jbossWSHandlingInboundMessage();

    @LogMessage(level = Level.TRACE)
    @Message(id = 318, value = "Assertion included in SOAP payload: %s")
    void jbossWSSAMLAssertionFoundInPayload(String assertionAsString);

    @LogMessage(level = Level.TRACE)
    @Message(id = 319, value = "Rolekeys to extract roles from the assertion: %s")
    void jbossWSRoleKeysExtractRolesFromAssertion(String string);

    @LogMessage(level = Level.TRACE)
    @Message(id = 320, value = "Roles in the assertion: %s")
    void jbossWSRolesInAssertion(String roles);

    @LogMessage(level = Level.TRACE)
    @Message(id = 321, value = "Did not find roles in the assertion")
    void jbossWSNoRolesFoundInAssertion();

    @LogMessage(level = Level.TRACE)
    @Message(id = 322, value = "We did not find any assertion")
    void jbossWSNoAssertionsFound();

    @LogMessage(level = Level.TRACE)
    @Message(id = 323, value = "Successfully Authenticated:Principal= %s ::subject = %s")
    void jbossWSSuccessfullyAuthenticatedPrincipal(String principal, String subject);
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 304, value = "Determined Security Domain = %s")
    void determinedSecurityDomain(String securityDomain);

    @LogMessage(level = Level.TRACE)
    @Message(id = 305, value = "Will expire from cache in %s seconds, principal = %s")
    void cacheWillExpireForPrincipal(int seconds, String principal);

    @LogMessage(level = Level.TRACE)
    @Message(id = 306, value = "Constructing STSClientInterceptor using %s as the configuration file")
    void authConstructingSTSClientInterceptor(String propertiesFile);

    @LogMessage(level = Level.TRACE)
    @Message(id = 307, value = "Retrieved SecurityContext from invocation: %s")
    void authRetrievedSecurityContextFromInvocation(String string);

    @LogMessage(level = Level.TRACE)
    @Message(id = 308, value = "Invoking token service to get SAML assertion for %s")
    void authInvokingSTSForSAMLAssertion(String principalName);

    @LogMessage(level = Level.TRACE)
    @Message(id = 309, value = "SAML assertion for %s successfully obtained")
    void authSAMLAssertionObtainedForPrincipal(String principalName);
    
    @LogMessage(level = Level.TRACE)
    @Message(id = 291, value = "Final attribute map size: %s")
    void attributeManagerMapSize(int size);

    @LogMessage(level = Level.TRACE)
    @Message(id = 292, value = "No authentication Subject found, cannot provide any user roles!")
    void authenticationSubjectNotFound();

    @LogMessage(level = Level.TRACE)
    @Message(id = 294, value = "Local Validation is being Performed")
    void authPerformingLocalValidation();

    @LogMessage(level = Level.TRACE)
    @Message(id = 295, value = "Local Validation passed.")
    void authSuccessfulLocalValidation();

    @LogMessage(level = Level.TRACE)
    @Message(id = 296, value = "Local Validation is disabled. Verifying with STS")
    void authLocalValidationDisabledCheckSTS();

    @LogMessage(level = Level.TRACE)
    @Message(id = 297, value = "Creating Cache Entry for JBoss at [%s] , with expiration set to SAML expiry = %s")
    void authCreatingCacheEntry(Date date, Date expiryDate);

    @LogMessage(level = Level.TRACE)
    @Message(id = 298, value = "Assertion from where roles will be sought = %s")
    void authSAMLAssertionToGetRolesFrom(String samlAssertion);

    @LogMessage(level = Level.TRACE)
    @Message(id = 299, value = "Initialized with %s")
    void initializedWith(String string);

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
    
    /** DEBUG **/
    
    @LogMessage(level = Level.DEBUG)
    @Message(id = 301, value = "Mapped roles to %s")
    void authMappedRoles(String roles);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 302, value = "Mapped principal = %s")
    void authMappedPrincipal(String principal);
    
    @LogMessage(level = Level.DEBUG)
    @Message(id = 293, value = "Returning an AttributeStatement with a [%s] attribute containing: %s")
    void returningAttributeStatement(String tokenRoleAttributeName, String attributes);
    
    @LogMessage(level = Level.DEBUG)
    @Message(id = 247, value = "Added Credential %s")
    void authAddedSAMLCredential(SamlCredential samlCredential);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 229, value = "The provider %s could not be added")
    void jceProviderCouldNotBeLoaded(String name, @Cause Throwable t);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 201, value = "Sending XACML Decision Query: %s")
    void xacmlSendingDecisionQuery(String xacmlDecisionQueryDocument);
    
    @LogMessage(level = Level.DEBUG)
    @Message(id = 206, value = "XACML Received Message: %s")
    void xacmlReceivedMessage(String asString);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 207, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void stsTokenRegistryNotSpecified();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 221, value = "[InstallDefaultConfiguration] Configuration is null. Creating a new configuration")
    void stsCreatingDefaultSTSConfig();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 222, value = "[InstallDefaultConfiguration] Configuration file name=%s")
    void stsLoadingConfiguration(String fileName);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 231, value = "Lifetime has not been specified. Using the default timeout value.")
    void stsTokenTimeoutNotSpecified();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 232, value = "Claims have been specified in the request but no processor was found for dialect %s")
    void wsTrustClaimsDialectProcessorNotFound(String dialect);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 243, value = "No key type could be found in the request. Using the default BEARER type.")
    void stsKeyTypeNotFoundUsingDefaultBearer();

    @LogMessage(level = Level.DEBUG)
    @Message(id = 244, value = "No key size could be found in the request. Using the default size. (%s)")
    void stsKeySizeNotFoundUsingDefault(long keySize);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 245, value = "Unable to parse the contents of the OnBehalfOfType: %s")
    void stsUnableToParseOnBehalfType(Object type);

    @LogMessage(level = Level.DEBUG)
    @Message(id = 251, value = "SAML Token Validation result: %s")
    void authSAMLValidationResult(boolean result);
    
    @LogMessage(level = Level.DEBUG)
    @Message(id = 300, value = "Did not find a token %s under %s in the map")
    void authSharedTokenNotFound(String name, String sharedToken);
}