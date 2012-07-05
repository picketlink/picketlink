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

    PicketLinkLoggerMessages ROOT_LOGGER = Logger.getMessageLogger(PicketLinkLoggerMessages.class,
            PicketLinkLoggerMessages.class.getPackage().getName());

    PicketLinkLoggerMessages AUDIT_LOGGER = Logger.getMessageLogger(PicketLinkLoggerMessages.class,
            PicketLinkLoggerMessages.class.getPackage().getName() + ".audit");

    /* INFO */
    
    @LogMessage(level = Level.INFO)
    @Message(id = 200, value = "[PicketLink Audit] %s")
    void auditEvent(String auditEvent);

    @LogMessage(level = Level.INFO)
    @Message(id = 201, value = "Keystore is null. so setting it up")
    void keyStoreSetup();

    @LogMessage(level = Level.INFO)
    @Message(id = 202, value = "No public key found for alias = %s")
    void keyStoreNullPublicKeyForAlias(String alias);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 203, value = "Assertion has expired with id=%s")
    void samlAssertionExpired(String id);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 204, value = "%s configuration file loaded")
    void stsConfigurationFileLoaded(String fileName);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 205, value = "Service Provider is setting the CanonicalizationMethod on XMLSignatureUtil: %s")
    void samlSPSettingCanonicalizationMethod(String canonicalizationMethod);
    
    @LogMessage(level = Level.INFO)
    @Message(id = 206, value = "Identity Provider is setting the CanonicalizationMethod on XMLSignatureUtil: %s")
    void samlIDPSettingCanonicalizationMethod(String canonicalizationMethod);

    @LogMessage(level = Level.INFO)
    @Message(id = 207, value = "Did not find picketlink-sts.xml. We will install default configuration")
    void samlIDPInstallingDefaultSTSConfig();

    @LogMessage(level = Level.INFO)
    @Message(id = 208, value = "Cannot dispatch to the logout page: no request dispatcher: %s")
    void samlSPCouldNotDispatchToLogoutPage(String logOutPage);

    @LogMessage(level = Level.INFO)
    @Message(id = 209, value = "Using logger implementation: %s")
    void usingLoggerImplementation(String className);

    /* WARN */

    @LogMessage(level = Level.WARN)
    @Message(id = 210, value = "%s is not an instance of SecurityTokenRegistry - using default registry")
    void stsTokenRegistryInvalidType(String tokenRegistryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 211, value = "Error instantiating token registry class - using default registry")
    void stsTokenRegistryInstantiationError();

    @LogMessage(level = Level.WARN)
    @Message(id = 212, value = "Revocation registry option not specified: cancelled ids will not be persisted!")
    void stsRevocationRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 213, value = "%s is not an instance of RevocationRegistry - using default registry")
    void stsRevocationRegistryInvalidType(String registryOption);

    @LogMessage(level = Level.WARN)
    @Message(id = 214, value = "Error instantiating revocation registry class - using default registry")
    void stsRevocationRegistryInstantiationError();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 215, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundTCL(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 216, value = "%s configuration file not found using TCCL")
    void stsConfigurationFileNotFoundClassLoader(String fileName);

    @LogMessage(level = Level.WARN)
    @Message(id = 217, value = "%s configuration file not found using URL. Using default configuration values")
    void stsUsingDefaultConfiguration(String fileName);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 218, value = "Secret key could not be encrypted because the endpoint's PKC has not been specified")
    void stsSecretKeyNotEncrypted();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 219, value = "SAML Assertion has been found to have no expiration: ID = %s")
    void authSAMLAssertionWithoutExpiration(String id);

    @LogMessage(level = Level.WARN)
    @Message(id = 220, value = "Attribute provider not installed: %s is not an instance of SAML20TokenAttributeProvider")
    void stsWrongAttributeProviderTypeNotInstalled(String attributeProviderClassName);

    @LogMessage(level = Level.WARN)
    @Message(id = 221, value = "Error instantiating attribute provider")
    void stsAttributeProviderInstantiationError(@Cause Throwable t);

    @LogMessage(level = Level.WARN)
    @Message(id = 222, value = "Assertion Element = %s")
    void samlAssertion(String nodeAsString);

    @LogMessage(level = Level.WARN)
    @Message(id = 223, value = "Active Session Count = %s")
    void samlIdentityServerActiveSessionCount(int activeSessionCount);

    @LogMessage(level = Level.WARN)
    @Message(id = 224, value = "Session Created with id = %s ::active session count = %s")
    void samlIdentityServerSessionCreated(String id, int activeSessionCount);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 225, value = "Session Destroyed with id = %s ::active session count = %s")
    void samlIdentityServerSessionDestroyed(String id, int activeSessionCount);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 226, value = "Was not able to create security token. Just sending message without binary token")
    void jbossWSUnableToCreateSecurityToken();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 227, value = "Unable to set the Identity Participant Stack Class. Will just use the default")
    void samlIDPUnableToSetParticipantStackUsingDefault(@Cause Throwable t);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 228, value = "Security Token digital signature has NOT been verified. Either the STS has been configured not to sign tokens or the STS key pair has not been properly specified.")
    void stsSecurityTokenSignatureNotVerified();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 229, value = "Security token should be encrypted but no encrypting key could be found")
    void stsSecurityTokenShouldBeEncrypted();

    @LogMessage(level = Level.WARN)
    @Message(id = 230, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void stsTokenRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 231, value = "Lifetime has not been specified. Using the default timeout value.")
    void stsTokenTimeoutNotSpecified();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 264, value = "Security Token with id = %s has already been persisted.")
    void samlSecurityTokenAlreadyPersisted(String id);

    @LogMessage(level = Level.WARN)
    @Message(id = 265, value = "Security Token with id = %s was not found in the registry.")
    void samlSecurityTokenNotFoundInRegistry(String id);

    /* ERROR */
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 232, value = "Exception loading the identity providers")
    void samlMetaDataIdentityProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 233, value = "Exception loading the service providers")
    void samlMetaDataServiceProviderLoadingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 234, value = "Cannot validate signature of assertion")
    void signatureAssertionValidationError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 235, value = "Exception in getting TrustKeyManager")
    void trustKeyManagerCreationError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 236, value = "Cannot get schema")
    void xmlCouldNotGetSchema(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 237, value = "Exception initializing role generator")
    void samlHandlerRoleGeneratorSetupError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 238, value = "AttributeManager set to %s")
    void samlHandlerAttributeSetup(String name);

    @LogMessage(level = Level.ERROR)
    @Message(id = 239, value = "Key Pair cannot be found")
    void samlHandlerKeyPairNotFound();

    @LogMessage(level = Level.ERROR)
    @Message(id = 240, value = "Error when trying to sign message for redirection")
    void samlHandlerErrorSigningRedirectBindingMessage(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 241, value = "Error validating signature")
    void samlHandlerErrorValidatingSignature(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 242, value = "Error in base64 decoding saml message")
    void samlBase64DecodingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 243, value = "Exception in parsing saml message")
    void samlParsingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 244, value = "Mapping Context returned is null")
    void attributeManagerMappingContextNull();

    @LogMessage(level = Level.ERROR)
    @Message(id = 245, value = "Exception in attribute mapping")
    void attributeManagerError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 246, value = "Could not obtain security context.")
    void couldNotObtainSecurityContext();
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 247, value = "SAML Assertion parsing failed")
    void authSAMLAssertionParsingFailed(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 248, value = "Unable to issue assertion")
    void authSAMLAssertionIssuingFailed(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 249, value = "Unable to create binary token")
    void jbossWSUnableToCreateBinaryToken(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 250, value = "Exception writing SOAP Message")
    void jbossWSUnableToWriteSOAPMessage(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 251, value = "Exception using backup method to get op name")
    void jbossWSErrorGettingOperationname(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 252, value = "Exception handling saml 11 use case")
    void samlIDPHandlingSAML11Error(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 253, value = "Exception in processing request")
    void samlIDPRequestProcessingError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 254, value = "Exception dealing with handler configuration")
    void samlHandlerConfigurationError(@Cause Throwable t);
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 255, value = "Falling back on local Form Authentication if available")
    void samlSPFallingBackToLocalFormAuthentication();

    @LogMessage(level = Level.ERROR)
    @Message(id = 256, value = "Unable to obtain the IDP SSO Descriptor from metadata")
    void samlSPUnableToGetIDPDescriptorFromMetadata();
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 257, value = "Verification of InResponseTo failed. InResponseTo from SAML response is %s. Value of request Id from HTTP session is %s")
    void samlHandlerFailedInResponseToVerification(String inResponseTo, String authnRequestId);

    @LogMessage(level = Level.ERROR)
    @Message(id = 258, value = "Exception in processing authentication")
    void samlHandlerAuthenticationError(@Cause Throwable t);
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 259, value = "The provider %s could not be added")
    void jceProviderCouldNotBeLoaded(String name, @Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 260, value = "Error parsing the response from the IDP. Check the strict post binding configuration on both IDP and SP side.")
    void samlResponseFromIDPParsingFailed();

    @LogMessage(level = Level.ERROR)
    @Message(id = 261, value = "Error during the logout process.")
    void samlLogoutError(@Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 262, value = "Could not forward to error page: %s")
    void samlErrorPageForwardError(String errorPage, @Cause Throwable t);

    @LogMessage(level = Level.ERROR)
    @Message(id = 263, value = "Service Provider could not handle the request.")
    void samlSPHandleRequestError(@Cause Throwable t);
    
}