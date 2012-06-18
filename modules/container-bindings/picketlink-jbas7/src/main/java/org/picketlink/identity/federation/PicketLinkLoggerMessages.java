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
    @Message(id = 328, value = "Unable to set the Identity Participant Stack Class. Will just use the default")
    void samlIDPUnableToSetParticipantStackUsingDefault(@Cause Throwable t);
    
    @LogMessage(level = Level.WARN)
    @Message(id = 234, value = "Security Token digital signature has NOT been verified. Either the STS has been configured not to sign tokens or the STS key pair has not been properly specified.")
    void stsSecurityTokenSignatureNotVerified();
    
    @LogMessage(level = Level.WARN)
    @Message(id = 241, value = "Security token should be encrypted but no encrypting key could be found")
    void stsSecurityTokenShouldBeEncrypted();

    @LogMessage(level = Level.WARN)
    @Message(id = 207, value = "Security Token registry option not specified: Issued Tokens will not be persisted!")
    void stsTokenRegistryNotSpecified();

    @LogMessage(level = Level.WARN)
    @Message(id = 231, value = "Lifetime has not been specified. Using the default timeout value.")
    void stsTokenTimeoutNotSpecified();

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
    
    @LogMessage(level = Level.ERROR)
    @Message(id = 229, value = "The provider %s could not be added")
    void jceProviderCouldNotBeLoaded(String name, @Cause Throwable t);
    
}