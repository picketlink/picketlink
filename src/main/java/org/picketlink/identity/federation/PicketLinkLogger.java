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
import java.security.Principal;
import java.util.Date;

import javax.security.auth.login.LoginException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.stream.Location;
import javax.xml.ws.WebServiceException;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.IssuerNotTrustedException;
import org.picketlink.identity.federation.core.saml.v2.exceptions.SignatureValidationException;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.w3c.dom.Element;

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
    IllegalArgumentException nullArgumentError(String argument);

    /**
     * <p>Creates an {@link IllegalArgumentException} for arguments that should not be the same.</p>
     * 
     * @param string
     * @return
     */
    IllegalArgumentException shouldNotBeTheSameError(String string);

    /**
     * <p>Creates an {@link ProcessingException} for resources that are not found.</p>
     * 
     * @param resource
     * @return
     */
    ProcessingException resourceNotFound(String resource);

    /**
     * <p>Creates an {@link ProcessingException} for generics processing errors.</p>
     * @param message 
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

    /**
     * <p>Creates a {@link ProcessingException} for exceptions raised during signature processing.</p>
     * 
     * @param e
     * @return
     */
    XMLSignatureException signatureError(Throwable e);

    /**
     * <p>Logs a XACML decision query document.</p>
     * 
     * @param xacmlDecisionQueryDocument
     */
    void xacmlSendingDecisionQuery(String xacmlDecisionQueryDocument);

    /**
     * <p>Creates a {@link RuntimeException} for null values.</p>
     * 
     * @param nullValue
     * @return
     */
    RuntimeException nullValueError(String nullValue);

    /**
     * <p>Creates a {@link RuntimeException} for not implemented methods or features.</p>
     * @param string 
     * 
     * @return
     */
    RuntimeException notImplementedYet(String string);

    /**
     * <p>Creates a {@link ConfigurationException} for exceptions raised during the PicketLink Audit configuration.
     * 
     * @param t
     * @return
     */
    ConfigurationException auditConfigurationError(Throwable t);

    /**
     * <p>Creates a {@link IllegalStateException} for the case the Audit Manager is null.</p>
     * 
     * @return
     */
    IllegalStateException auditNullAuditManager();

    /**
     * <p>Indicates if the logging level is set to INFO.</p>
     * 
     * @return
     */
    boolean isInfoEnabled();

    /**
     * <p>Logs a PicketLink Audit Event.</p>
     * 
     * @param auditEvent
     */
    void auditEvent(String auditEvent);

    /**
     * <p>Creates a {@link RuntimeException} for missing values.</p>
     * 
     * @param string
     * @return
     */
    RuntimeException injectedValueMissing(String value);

    /**
     * <p>Logs a message during the KeyStore setup.</p>
     */
    void keyStoreSetup();

    /**
     * <p>Creates a {@link IllegalStateException} for the case where the KeyStore is null.</p>
     * 
     * @return
     */
    IllegalStateException keyStoreNullStore();

    /**
     * <p>Logs a message for the cases where no public key was found for a given alias.</p>
     * 
     * @param alias
     */
    void keyStoreNullPublicKeyForAlias(String alias);

    /**
     * <p>Creates a {@link TrustKeyConfigurationException} for exceptions raised during the KeyStore configuration.</p>
     * 
     * @param t
     * @return
     */
    TrustKeyConfigurationException keyStoreConfigurationError(Throwable t);

    /**
     * <p>Creates a {@link TrustKeyConfigurationException} for exceptions raised during the KeyStore processing.</p>
     * 
     * @param t
     * @return
     */
    TrustKeyProcessingException keyStoreProcessingError(Throwable t);

    /**
     * @param domain
     * @return
     */
    IllegalStateException keyStoreMissingDomainAlias(String domain);

    /**
     * <p>Creates a {@link RuntimeException} for the case where the signing key password is null.</p>
     * 
     * @return
     */
    RuntimeException keyStoreNullSigningKeyPass();

    /**
     * <p>Creates a {@link RuntimeException} for the case where key store are not located.</p>
     * 
     * @param keyStore
     * @return
     */
    RuntimeException keyStoreNotLocated(String keyStore);

    /**
     * <p>Creates a {@link IllegalStateException} for the case where the alias is null.</p>
     * 
     * @return
     */
    IllegalStateException keyStoreNullAlias();

    /**
     * <p>Creates a {@link RuntimeException} for the case where parser founds a unknown end element.</p>
     * 
     * @param endElementName
     * @return
     */
    RuntimeException parserUnknownEndElement(String endElementName);

    /**
     * @param tag
     * @param location
     * @return
     */
    RuntimeException parserUnknownTag(String tag, Location location);

    /**
     * @param string
     * @return
     */
    ParsingException parserRequiredAttribute(String string);

    /**
     * @param elementName
     * @param location
     * @return
     */
    RuntimeException parserUnknownStartElement(String elementName, Location location);

    /**
     * @return
     */
    IllegalStateException parserNullStartElement();

    /**
     * @param xsiTypeValue
     * @return
     */
    ParsingException parserUnknownXSI(String xsiTypeValue);

    /**
     * @param string
     * @return
     */
    ParsingException parserExpectedEndTag(String tagName);

    /**
     * @param e
     * @return
     */
    ParsingException parserException(Throwable t);

    /**
     * @param string
     * @return
     */
    ParsingException parserExpectedTextValue(String string);

    /**
     * @param expectedXsi
     * @return
     */
    RuntimeException parserExpectedXSI(String expectedXsi);

    /**
     * @param tag
     * @param foundElementTag
     * @return
     */
    RuntimeException parserExpectedTag(String tag, String foundElementTag);

    /**
     * @param elementName 
     * @return
     */
    RuntimeException parserFailed(String elementName);

    /**
     * @return
     */
    ParsingException parserUnableParsingNullToken();

    /**
     * @param t
     * @return
     */
    ParsingException parserError(Throwable t);

    /**
     * @param qname
     */
    void xmlLookingParserForElement(QName qname);

    /**
     * @param asString
     */
    void xacmlReceivedMessage(String asString);

    /**
     * 
     * @param e
     * @return
     */
    RuntimeException xacmlPDPMessageProcessingError(Throwable t);

    /**
     * @param policyConfigFileName
     * @return
     */
    IllegalStateException fileNotLocated(String policyConfigFileName);

    /**
     * @param string
     * @return
     */
    IllegalStateException optionNotSet(String option);

    /**
     * 
     */
    void stsTokenRegistryNotSpecified();

    /**
     * @param tokenRegistryOption
     */
    void stsTokenRegistryInvalidType(String tokenRegistryOption);

    /**
     * 
     */
    void stsTokenRegistryInstantiationError();

    /**
     * 
     */
    void stsRevocationRegistryNotSpecified();

    /**
     * @param registryOption
     */
    void stsRevocationRegistryInvalidType(String registryOption);

    /**
     * 
     */
    void stsRevocationRegistryInstantiationError();

    /**
     * @return
     */
    ProcessingException samlAssertionExpiredError();

    /**
     * @return
     */
    ProcessingException assertionInvalidError();

    /**
     * @param name
     * @return
     */
    RuntimeException writerUnknownTypeError(String name);

    /**
     * @param string
     * @return
     */
    ProcessingException writerNullValueError(String value);

    /**
     * @param value
     * @return
     */
    RuntimeException writerUnsupportedAttributeValueError(String value);

    /**
     * @return
     */
    IllegalArgumentException issuerInfoMissingStatusCodeError();

    /**
     * @param fqn
     * @return
     */
    ProcessingException classNotLoadedError(String fqn);

    /**
     * @param fqn
     * @param e
     * @return
     */
    ProcessingException couldNotCreateInstance(String fqn, Throwable t);

    /**
     * @param property
     * @return
     */
    RuntimeException systemPropertyMissingError(String property);

    /**
     * @param directory
     */
    void samlMetaDataStoreDirectoryCreation(String directory);

    /**
     * @param t
     */
    void samlMetaDataIdentityProviderLoadingError(Throwable t);

    /**
     * @param t
     */
    void samlMetaDataServiceProviderLoadingError(Throwable t);

    /**
     * @param path
     */
    void samlMetaDataPersistEntityDescriptor(String path);

    /**
     * @param path
     */
    void samlMetaDataPersistTrustedMap(String path);

    /**
     * @param t
     */
    void signatureAssertionValidationError(Throwable t);

    /**
     * @param now
     * @param notBefore
     * @param notOnOrAfter
     */
    void samlAssertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);

    /**
     * @param id
     */
    void samlAssertionExpired(String id);

    /**
     * @param attrValue
     * @return
     */
    RuntimeException unknownObjectType(Object attrValue);

    /**
     * @param e
     * @return
     */
    ConfigurationException configurationError(Throwable t);

    /**
     * @param message
     */
    void trace(String message);

    /**
     * @param algo
     * @return
     */
    RuntimeException signatureUnknownAlgo(String algo);

    /**
     * @param message
     * @return
     */
    IllegalArgumentException invalidArgumentError(String message);

    /**
     * 
     */
    void stsCreatingDefaultSTSConfig();

    /**
     * @param fileName
     */
    void stsLoadingConfiguration(String fileName);

    /**
     * @param configuration
     * @param protocolContext
     * @return
     */
    ProcessingException stsNoTokenProviderError(String configuration, String protocolContext);

    /**
     * @param message
     */
    void debug(String message);

    /**
     * @param fileName
     */
    void stsConfigurationFileNotFoundTCL(String fileName);

    /**
     * @param fileName
     */
    void stsConfigurationFileNotFoundClassLoader(String fileName);

    /**
     * @param fileName
     */
    void stsUsingDefaultConfiguration(String fileName);

    /**
     * @param fileName
     */
    void stsConfigurationFileLoaded(String fileName);

    /**
     * @param t
     * @return
     */
    ConfigurationException stsConfigurationFileParsingError(Throwable t);

    /**
     * @param message
     * @return
     */
    IOException notSerializableError(String message);

    /**
     * 
     */
    void trustKeyManagerCreationError(Throwable t);

    /**
     * @param message
     */
    void info(String message);

    /**
     * @param message
     */
    void error(String message);

    /**
     * @param t
     */
    void xmlCouldNotGetSchema(Throwable t);

    /**
     * @return
     */
    boolean isTraceEnabled();

    /**
     * @return
     */
    boolean isDebugEnabled();

    /**
     * @param name
     * @param t
     */
    void jceProviderCouldNotBeLoaded(String name, Throwable t);

    /**
     * @return
     */
    ProcessingException writerInvalidKeyInfoNullContentError();

    /**
     * @param first
     * @param second
     * @return
     */
    RuntimeException notEqualError(String first, String second);

    /**
     * @param message
     * @return
     */
    IllegalArgumentException wrongTypeError(String message);

    /**
     * @param certAlgo
     * @return
     */
    RuntimeException encryptUnknownAlgoError(String certAlgo);

    /**
     * @param element
     * @return
     */
    IllegalStateException domMissingDocElementError(String element);

    /**
     * @param element
     * @return
     */
    IllegalStateException domMissingElementError(String element);

    /**
     * @return
     */
    WebServiceException stsWSInvalidTokenRequestError();

    /**
     * @param t
     * @return
     */
    WebServiceException stsWSError(Throwable t);

    /**
     * @param t
     * @return
     */
    WebServiceException stsWSConfigurationError(Throwable t);

    /**
     * @param requestType
     * @return
     */
    WSTrustException stsWSInvalidRequestTypeError(String requestType);

    /**
     * @param t
     * @return
     */
    WebServiceException stsWSHandlingTokenRequestError(Throwable t);

    /**
     * @param t
     * @return
     */
    WebServiceException stsWSResponseWritingError(Throwable t);

    /**
     * @param t
     * @return
     */
    RuntimeException stsUnableToConstructKeyManagerError(Throwable t);

    /**
     * @param serviceName
     * @param t
     * @return
     */
    RuntimeException stsPublicKeyError(String serviceName, Throwable t);

    /**
     * @param t
     * @return
     */
    RuntimeException stsSigningKeyPairError(Throwable t);

    /**
     * @param t
     * @return
     */
    RuntimeException stsPublicKeyCertError(Throwable t);

    /**
     * @param callerPrincipal
     */
    void samlIssuingTokenForPrincipal(Principal callerPrincipal);

    /**
     * 
     */
    void stsTokenTimeoutNotSpecified();

    /**
     * @param dialect
     */
    void wsTrustClaimsDialectProcessorNotFound(String dialect);

    /**
     * @param t
     * @return
     */
    WSTrustException wsTrustCombinedSecretKeyError(Throwable t);

    /**
     * @return
     */
    WSTrustException wsTrustClientPublicKeyError();

    /**
     * @param t
     * @return
     */
    WSTrustException stsError(Throwable t);

    /**
     * @param details
     */
    void stsValidatingTokenForRenewal(String details);

    /**
     * @param message
     * @param t
     * @return
     */
    XMLSignatureException signatureInvalidError(String message, Throwable t);

    /**
     * 
     */
    void stsSecurityTokenSignatureNotVerified();

    /**
     * @param details
     */
    void stsStartedValidationForRequest(String details);

    /**
     * @param nodeAsString
     */
    void signatureValidatingDocument(String nodeAsString);

    /**
     * 
     */
    void stsDelegatingValidationToTokenProvider();

    /**
     * @param namespaceURI
     */
    void signatureElementToBeSigned(String namespaceURI);

    /**
     * @param nodeAsString
     */
    void signatureSignedElement(String nodeAsString);

    /**
     * @param e
     * @return
     */
    RuntimeException encryptProcessError(Throwable t);

    /**
     * @param alias
     */
    void pkiLocatingPublic(String alias);

    /**
     * 
     */
    void stsSecurityTokenShouldBeEncrypted();

    /**
     * @param requestType
     */
    void stsReceivedRequestType(String requestType);

    /**
     * 
     */
    void stsKeyTypeNotFoundUsingDefaultBearer();

    /**
     * @param kEY_SIZE
     */
    void stsKeySizeNotFoundUsingDefault(long kEY_SIZE);

    /**
     * @param password
     * @return
     */
    RuntimeException unableToDecodePasswordError(String password);

    /**
     * @param configFile
     * @return
     */
    IllegalStateException couldNotLoadProperties(String configFile);

    /**
     * @param any
     */
    void stsUnableToParseOnBehalfType(Object type);

    /**
     * @param t
     * @return
     */
    WSTrustException stsKeyInfoTypeCreationError(Throwable t);

    /**
     * 
     */
    void stsSecretKeyNotEncrypted();

    /**
     * @return
     */
    LoginException authCouldNotIssueSAMLToken();

    /**
     * @param t
     * @return
     */
    LoginException authLoginError(Throwable t);

    /**
     * @param samlCredential
     */
    void authAddedSAMLCredential(SamlCredential samlCredential);

    /**
     * 
     */
    void authUserNameFromCallbackIsNull();

    /**
     * 
     */
    void authPasswordFromCallbackIsNull();

    /**
     * @param e
     * @return
     */
    IllegalStateException authCouldNotCreateWSTrustClient(Throwable t);

    /**
     * @param id
     */
    void samlAssertionWithoutExpiration(String id);

    /**
     * @param token
     * @return
     */
    LoginException authCouldNotValidateSAMLToken(Element token);

    /**
     * @param result
     */
    void authSAMLValidationResult(boolean result);

    /**
     * @return
     */
    LoginException authCouldNotLocateSecurityToken();

    /**
     * @return
     */
    ProcessingException wsTrustNullCancelTargetError();

    /**
     * @param t
     * @return
     */
    ProcessingException samlAssertionMarshallError(Throwable t);

    /**
     * @return
     */
    ProcessingException wsTrustNullRenewTargetError();

    /**
     * @param t
     * @return
     */
    ProcessingException samlAssertionUnmarshallError(Throwable t);

    /**
     * @return
     */
    ProcessingException samlAssertionRevokedCouldNotRenew(String id);

    /**
     * 
     */
    void samlAssertionStartingValidation();

    /**
     * @return
     */
    ProcessingException wsTrustNullValidationTargetError();

    /**
     * 
     */
    void stsNoAttributeProviderSet();

    /**
     * @param attributeProviderClassName
     */
    void stsWrongAttributeProviderTypeNotInstalled(String attributeProviderClassName);

    /**
     * @param t
     */
    void attributeProviderInstationError(Throwable t);

    /**
     * @param nodeAsString
     */
    void samlAssertion(String nodeAsString);

    /**
     * @param dce
     * @return
     */
    RuntimeException wsTrustUnableToGetDataTypeFactory(Throwable t);

    /**
     * @return
     */
    ProcessingException wsTrustValidationStatusCodeMissing();

    /**
     * @param activeSessionCount
     */
    void samlIdentityServerActiveSessionCount(int activeSessionCount);

    /**
     * @param id
     * @param activeSessionCount
     */
    void samlIdentityServerSessionCreated(String id, int activeSessionCount);

    /**
     * @param id
     * @param activeSessionCount
     */
    void samlIdentityServerSessionDestroyed(String id, int activeSessionCount);

    /**
     * @param name
     * @return
     */
    RuntimeException unknowCredentialType(String name);

    /**
     * @param name
     */
    void samlHandlerRoleGeneratorSetup(String name);

    /**
     * @param t
     */
    void samlHandlerRoleGeneratorSetupError(Throwable t);

    /**
     * @param name
     */
    void samlHandlerAttributeSetup(String name);

    /**
     * @return
     */
    RuntimeException samlHandlerAssertionNotFound();

    /**
     * @return
     */
    ProcessingException samlHandlerAuthnRequestIsNull();

    /**
     * @param destination
     */
    void destination(String destination);

    /**
     * @param t
     */
    void samlHandlerAuthenticationError(Throwable t);

    /**
     * @return
     */
    IllegalArgumentException samlHandlerNoAssertionFromIDP();

    /**
     * @return
     */
    ProcessingException samlHandlerNullEncryptedAssertion();

    /**
     * @return
     */
    SecurityException samlHandlerIDPAuthenticationFailedError();

    /**
     * @param aee
     * @return
     */
    ProcessingException assertionExpiredError(AssertionExpiredException aee);

    /**
     * @param roles
     */
    void invalidRole(String roles);

    /**
     * @param attrValue
     * @return
     */
    RuntimeException unsupportedRoleType(Object attrValue);

    /**
     * @param authnRequestId
     */
    void samlHandlerSavedAuthnRequestIdIntoSession(String authnRequestId);

    /**
     * @param inResponseTo
     */
    void samlHandlerSuccessfulInResponseToValidation(String inResponseTo);

    /**
     * @param inResponseTo
     * @param authnRequestId
     */
    void samlHandlerFailedInResponseToVerification(String inResponseTo, String authnRequestId);

    /**
     * @return
     */
    ProcessingException samlHandlerFailedInResponseToVerificarionError();

    /**
     * @param domainsTrusted
     * @param issuerDomain
     */
    void samlTrustedDomains(String domainsTrusted, String issuerDomain);

    /**
     * @param uriBit
     */
    void samlTrustedDomainCheck(String uriBit);

    /**
     * @param uriBit
     * @param issuerDomain
     */
    void samlHandlerTrustedDomainMatched(String uriBit, String issuerDomain);

    /**
     * @param issuer
     * @return
     */
    IssuerNotTrustedException samlIssuerNotTrustedError(String issuer);

    /**
     * @param e
     * @return
     */
    IssuerNotTrustedException samlIssuerNotTrustedException(Throwable t);

    /**
     * @param domainsTrusted
     * @param issuerDomain
     */
    void samlHandlerDomainsTrustedBySP(String domainsTrusted, String issuerDomain);

    /**
     * @return
     */
    ConfigurationException samlHandlerTrustElementMissingError();

    /**
     * @return
     */
    ProcessingException samlHandlerIdentityServerNotFoundError();

    /**
     * @return
     */
    ProcessingException samlHandlerPrincipalNotFoundError();

    /**
     * @param originalIssuer
     */
    void samlHandlerGeneratingSuccessStatusResponse(String originalIssuer);

    /**
     * 
     */
    void samlHandlerNoDocumentToSign();

    /**
     * 
     */
    void samlHandlerNoResponseDocumentFound();

    /**
     * 
     */
    void samlHandlerSigningDocumentForPOSTBinding();

    /**
     * 
     */
    void samlHandlerSigningDocumentForRedirectBinding();

    /**
     * 
     */
    void samlHandlerKeyPairNotFound();

    /**
     * @return
     */
    ProcessingException samlHandlerKeyPairNotFoundError();

    /**
     * @param t
     */
    void samlHandlerErrorSigningRedirectBindingMessage(Throwable t);

    /**
     * @param t
     * @return
     */
    RuntimeException samlHandlerSigningRedirectBindingMessageError(Throwable t);

    /**
     * @param method
     */
    void samlHandlerValidatingResponseForHTTPMethod(String method);

    /**
     * @return
     */
    SignatureValidationException samlHandlerSignatureValidationFailed();

    /**
     * @param t
     */
    void samlHandlerErrorValidatingSignature(Throwable t);

    /**
     * @return
     */
    ProcessingException samlHandlerInvalidSignatureError();

    /**
     * @return
     */
    ProcessingException samlHandlerSignatureNotPresentError();

    /**
     * @param t
     * @return
     */
    ProcessingException samlHandlerSignatureValidationError(Throwable t);

    /**
     * 
     */
    void samlIDPUserClosedBrowserCancelingToken();

    /**
     * @param t
     */
    void error(Throwable t);

    /**
     * @param handlers
     */
    void samlHandlerList(String handlers);

    /**
     * @param handlerClassName
     */
    void samlHandlerFinishedProcessing(String handlerClassName);

    /**
     * @param t
     * @return
     */
    RuntimeException samlHandlerChainProcessingError(Throwable t);

    /**
     * @return
     */
    TrustKeyConfigurationException trustKeyManagerMissing();

    /**
     * @param samlRequestDocument
     */
    void samlRequestDocument(String samlRequestDocument);

    /**
     * @param rte
     */
    void samlBase64DecodingError(Throwable t);

    /**
     * @param t
     */
    void samlParsingError(Throwable t);

    /**
     * @param t
     */
    void trace(Throwable t);

    /**
     * 
     */
    void mappingContextNull();

    /**
     * @param t
     */
    void attributeManagerError(Throwable t);

    /**
     * 
     */
    void couldNotObtainSecurityContext();

    /**
     * @param size
     */
    void attributeManagerMapSize(int size);

    /**
     * 
     */
    void authenticationSubjectNotFound();

    /**
     * @param tokenRoleAttributeName
     * @param attributes
     */
    void returningAttributeStatement(String tokenRoleAttributeName, String attributes);

    /**
     * @param t
     * @return
     */
    LoginException authFailedToCreatePrincipal(Throwable t);

    /**
     * @param class1 
     * @return
     */
    LoginException authSharedCredentialIsNotSAMLCredential(String className);

    /**
     * @return
     */
    LoginException authSTSConfigFileNotFound();

    /**
     * @param t
     * @return
     */
    LoginException authErrorHandlingCallback(Throwable t);

    /**
     * 
     */
    void authPerformingLocalValidation();

    /**
     * 
     */
    void authSuccessfulLocalValidation();

    /**
     * 
     */
    void authLocalValidationDisabledCheckSTS();

    /**
     * @return
     */
    LoginException authInvalidSAMLAssertionBySTS();

    /**
     * @param t
     * @return
     */
    LoginException authAssertionValidationError(Throwable t);

    /**
     * @param date
     * @param expiryDate
     */
    void authCreatingCacheEntry(Date date, Date expiryDate);

    /**
     * @param t
     * @return
     */
    LoginException authFailedToParseSAMLAssertion(Throwable t);

    /**
     * @param samlAssertion
     */
    void authSAMLAssertionToGetRolesFrom(String samlAssertion);

    void initializedWith(String string);

    void authSharedTokenNotFound(String name, String sharedToken);

    void authMappedRoles(String roles);

    void authMappedPrincipal(String principal);

    /**
     * @param t
     */
    void authSAMLAssertionPasingFailed(Throwable t);

    void determinedSecurityDomain(String securityDomain);

    void cacheWillExpireForPrincipal(int seconds, String principal);

    LoginException authNullKeyStoreFromSecurityDomainError(String name);

    LoginException authNullKeyStoreAliasFromSecurityDomainError(String name);

    LoginException authNoCertificateFoundForAliasError(String alias, String name);

    LoginException authSAMLInvalidSignatureError();

    LoginException authSAMLAssertionExpiredError();

    /**
     * @param propertiesFile
     */
    void authConstructingSTSClientInterceptor(String propertiesFile);

    /**
     * @param string
     */
    void authRetrievedSecurityContextFromInvocation(String string);

    /**
     * @param principalName
     */
    void authInvokingSTSForSAMLAssertion(String principalName);

    /**
     * @param principalName
     */
    void authSAMLAssertionObtainedForPrincipal(String principalName);

    /**
     * @param t
     */
    void authSAMLAssertionIssuingFailed(Throwable t);

    /**
     * 
     */
    void jbossWSHandlingOutboundMessage();

    /**
     * 
     */
    void jbossWSHandlingInboundMessage();
    
    /**
     * @param t
     */
    void jbossWSUnableToCreateBinaryToken(Throwable t);

    /**
     * 
     */
    void jbossWSUnableToCreateSecurityToken();

    /**
     * @param ignore
     */
    void jbossWSUnableToWriteSOAPMessage(Throwable t);

    /**
     * @param headerValue
     */
    void jbossWSHeaderValueIdentified(String headerValue);

    /**
     * @param cookie
     */
    void jbossWSCookieValueIdentified(String cookie);

    /**
     * @param assertionAsString
     */
    void jbossWSSAMLAssertionFoundInPayload(String assertionAsString);

    /**
     * @param string
     */
    void jbossWSRoleKeysExtractRolesFromAssertion(String string);

    /**
     * @param roles
     */
    void jbossWSRolesInAssertion(String roles);

    /**
     * 
     */
    void jbossWSNoRolesFoundInAssertion();

    /**
     * 
     */
    void jbossWSNoAssertionsFound();

    /**
     * @param principal
     * @param subject
     */
    void jbosswsSuccessfullyAuthenticatedPrincipal(String principal, String subject);

    /**
     * @return
     */
    RuntimeException jbossWSUnableToLoadJBossWSSEConfigError();

    /**
     * @return
     */
    RuntimeException jbossWSAuthorizationFailed();

    /**
     * @param t
     */
    void jbossWSErrorGettingOperationName(Throwable t);

}