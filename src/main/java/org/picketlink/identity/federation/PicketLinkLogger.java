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

import javax.security.auth.login.LoginException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.ws.WebServiceException;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
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
    IllegalArgumentException shouldNotBeTheSame(String string);

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
    void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument);

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
    void lookingParserForElement(QName qname);

    /**
     * @param asString
     */
    void receivedXACMLMessage(String asString);

    /**
     * 
     * @param e
     * @return
     */
    RuntimeException pdpMessageProcessingError(Throwable t);

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
    void securityTokenRegistryNotSpecified();

    /**
     * @param tokenRegistryOption
     */
    void securityTokenRegistryInvalidType(String tokenRegistryOption);

    /**
     * 
     */
    void securityTokenRegistryInstantiationError();

    /**
     * 
     */
    void revocationRegistryNotSpecified();

    /**
     * @param registryOption
     */
    void revocationRegistryInvalidType(String registryOption);

    /**
     * 
     */
    void revocationRegistryInstantiationError();

    /**
     * @return
     */
    ProcessingException assertionExpiredError();

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
    void metaDataStoreDirectoryCreation(String directory);

    /**
     * @param t
     */
    void metaDataIdentityProviderLoadingError(Throwable t);

    /**
     * @param t
     */
    void metaDataServiceProviderLoadingError(Throwable t);

    /**
     * @param path
     */
    void metaDataPersistEntityDescriptor(String path);

    /**
     * @param path
     */
    void metaDataPersistTrustedMap(String path);

    /**
     * @param t
     */
    void signatureAssertionValidationError(Throwable t);

    /**
     * @param now
     * @param notBefore
     * @param notOnOrAfter
     */
    void assertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter);

    /**
     * @param id
     */
    void assertionExpired(String id);

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
    void couldNotGetXMLSchema(Throwable t);

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
    void issuingTokenForPrincipal(Principal callerPrincipal);

    /**
     * 
     */
    void tokenTimeoutNotSpecified();

    /**
     * @param dialect
     */
    void claimsDialectProcessorNotFound(String dialect);

    /**
     * @param t
     * @return
     */
    WSTrustException stsCombinedSecretKeyError(Throwable t);

    /**
     * @return
     */
    WSTrustException stsClientPublicKeyError();

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
    void authSAMLAssertionWithoutExpiration(String id);

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

}