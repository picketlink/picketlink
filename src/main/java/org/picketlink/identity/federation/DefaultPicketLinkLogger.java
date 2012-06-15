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

import static org.picketlink.identity.federation.core.ErrorCodes.EXPECTED_TAG;
import static org.picketlink.identity.federation.core.ErrorCodes.REQD_ATTRIBUTE;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_START_ELEMENT;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_TAG;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.login.LoginException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
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
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultPicketLinkLogger implements PicketLinkLogger {

    private Logger logger = Logger.getLogger(PicketLinkLogger.class.getPackage().getName());

    DefaultPicketLinkLogger() {

    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#info(java.lang.String)
     */
    @Override
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);            
        }
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#debug(java.lang.String)
     */
    @Override
    public void debug(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);            
        }
    }

    private void debug(String message, Throwable t) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, t);            
        }
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#trace(java.lang.String)
     */
    @Override
    public void trace(String message) {
        if (logger.isTraceEnabled()) {
            logger.trace(message);            
        }
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#error(java.lang.Throwable)
     */
    @Override
    public void error(Throwable t) {
        logger.error("Unexpected error", t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.picketlink.identity.federation.PicketLinkLogger#nullArgument(java.lang.String)
     */
    @Override
    public IllegalArgumentException nullArgumentError(String argument) {
        return new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + argument);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#shouldNotBeTheSame(java.lang.String)
     */
    @Override
    public IllegalArgumentException shouldNotBeTheSameError(String string) {
        return new IllegalArgumentException(ErrorCodes.SHOULD_NOT_BE_THE_SAME
                + "Only one of isSigningKey and isEncryptionKey should be true");
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#resourceNotFound(java.lang.String)
     */
    @Override
    public ProcessingException resourceNotFound(String resource) {
        return new ProcessingException(ErrorCodes.RESOURCE_NOT_FOUND + resource + " could not be loaded");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#processingError(java.lang.Throwable)
     */
    @Override
    public ProcessingException processingError(Throwable t) {
        return new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unsupportedType(java.lang.String)
     */
    @Override
    public RuntimeException unsupportedType(String name) {
        return new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlResponseDocument(java.lang.String)
     */
    @Override
    public void samlResponseDocument(String samlResponseDocumentAsString) {
        this.trace("SAML Response Document=" + samlResponseDocumentAsString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureError(java.lang.Throwable)
     */
    @Override
    public XMLSignatureException signatureError(Throwable e) {
        return new XMLSignatureException(ErrorCodes.SIGNING_PROCESS_FAILURE, e);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#sendingXACMLDecisionQuery(java.lang.String)
     */
    @Override
    public void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument) {
        this.debug("Sending XACML Decision Query::" + xacmlDecisionQueryDocument);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#nullValue(java.lang.String)
     */
    @Override
    public RuntimeException nullValueError(String nullValue) {
        return new RuntimeException(ErrorCodes.NULL_VALUE + nullValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notImplementedYet()
     */
    @Override
    public RuntimeException notImplementedYet(String feature) {
        return new RuntimeException(ErrorCodes.NOT_IMPLEMENTED_YET + feature);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditConfigurationError(javax.naming.NamingException)
     */
    @Override
    public ConfigurationException auditConfigurationError(Throwable t) {
        return new ConfigurationException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditNullAuditManager()
     */
    @Override
    public IllegalStateException auditNullAuditManager() {
        return new IllegalStateException(ErrorCodes.AUDIT_MANAGER_NULL);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditEvent(java.lang.String)
     */
    @Override
    public void auditEvent(String auditEvent) {
        this.info(auditEvent);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#injectedValueMissing(java.lang.String)
     */
    @Override
    public RuntimeException injectedValueMissing(String value) {
        return new RuntimeException(ErrorCodes.INJECTED_VALUE_MISSING + value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keystoreSetup()
     */
    @Override
    public void keyStoreSetup() {
        this.trace("getPublicKey::Keystore is null. so setting it up");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullStore()
     */
    @Override
    public IllegalStateException keyStoreNullStore() {
        return new IllegalStateException(ErrorCodes.KEYSTOREKEYMGR_NULL_KEYSTORE);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullPublicKeyForAlias(java.lang.String)
     */
    @Override
    public void keyStoreNullPublicKeyForAlias(String alias) {
        this.trace("No public key found for alias=" + alias);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreConfigurationError(java.lang.Throwable)
     */
    @Override
    public TrustKeyConfigurationException keyStoreConfigurationError(Throwable t) {
        return new TrustKeyConfigurationException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreProcessingError(java.lang.Throwable)
     */
    @Override
    public TrustKeyProcessingException keyStoreProcessingError(Throwable t) {
        return new TrustKeyProcessingException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreMissingDomainAlias(java.lang.String)
     */
    @Override
    public IllegalStateException keyStoreMissingDomainAlias(String domain) {
        return new IllegalStateException(ErrorCodes.KEYSTOREKEYMGR_DOMAIN_ALIAS_MISSING + domain);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullSigningKeyPass()
     */
    @Override
    public RuntimeException keyStoreNullSigningKeyPass() {
        return new RuntimeException(ErrorCodes.KEYSTOREKEYMGR_NULL_SIGNING_KEYPASS);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNotLocated(java.lang.String)
     */
    @Override
    public RuntimeException keyStoreNotLocated(String keyStore) {
        return new RuntimeException(ErrorCodes.KEYSTOREKEYMGR_KEYSTORE_NOT_LOCATED + keyStore);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullAlias()
     */
    @Override
    public IllegalStateException keyStoreNullAlias() {
        return new IllegalStateException(ErrorCodes.KEYSTOREKEYMGR_NULL_ALIAS);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownEndElement(java.lang.String)
     */
    @Override
    public RuntimeException parserUnknownEndElement(String endElementName) {
        return new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT + endElementName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parseUnknownTag(java.lang.String, javax.xml.stream.Location)
     */
    @Override
    public RuntimeException parserUnknownTag(String tag, Location location) {
        return new RuntimeException(UNKNOWN_TAG + tag + "::location=" + location);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parseRequiredAttribute(java.lang.String)
     */
    @Override
    public ParsingException parserRequiredAttribute(String string) {
        return new ParsingException(REQD_ATTRIBUTE + "AssertionID");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownStartElement(java.lang.String, javax.xml.stream.Location)
     */
    @Override
    public RuntimeException parserUnknownStartElement(String elementName, Location location) {
        return new RuntimeException(UNKNOWN_START_ELEMENT + elementName + "::location=" + location);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserNullStartElement()
     */
    @Override
    public IllegalStateException parserNullStartElement() {
        return new IllegalStateException(ErrorCodes.NULL_START_ELEMENT);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownXSI(java.lang.String)
     */
    @Override
    public ParsingException parserUnknownXSI(String xsiTypeValue) {
        return new ParsingException(ErrorCodes.UNKNOWN_XSI + xsiTypeValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedEndTag(java.lang.String)
     */
    @Override
    public ParsingException parserExpectedEndTag(String tagName) {
        return new ParsingException(ErrorCodes.EXPECTED_END_TAG + "RequestAbstract or XACMLAuthzDecisionQuery");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserException(java.lang.Exception)
     */
    @Override
    public ParsingException parserException(Throwable t) {
        return new ParsingException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedTextValue(java.lang.String)
     */
    @Override
    public ParsingException parserExpectedTextValue(String string) {
        return new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "SigningAlias");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedXSI(java.lang.String)
     */
    @Override
    public RuntimeException parserExpectedXSI(String expectedXsi) {
        return new RuntimeException(expectedXsi);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedTag(java.lang.String, java.lang.String)
     */
    @Override
    public RuntimeException parserExpectedTag(String tag, String foundElementTag) {
        return new RuntimeException(EXPECTED_TAG + tag + ">.  Found <" + foundElementTag + ">");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserFailed()
     */
    @Override
    public RuntimeException parserFailed(String elementName) {
        return new RuntimeException(ErrorCodes.FAILED_PARSING + elementName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnableParsingNullToken()
     */
    @Override
    public ParsingException parserUnableParsingNullToken() {
        return new ParsingException(ErrorCodes.UNABLE_PARSING_NULL_TOKEN);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserError(java.lang.Exception)
     */
    @Override
    public ParsingException parserError(Throwable t) {
        return new ParsingException(ErrorCodes.PARSING_ERROR + t.getMessage(), t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#lookingParserForElement(javax.xml.namespace.QName)
     */
    @Override
    public void lookingParserForElement(QName qname) {
        this.trace("Looking for Parser for :" + qname);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#receivedXACMLMessage(java.lang.String)
     */
    @Override
    public void receivedXACMLMessage(String asString) {
        this.debug("Received Message::" + asString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#pdpMessageProcessingError(java.lang.Exception)
     */
    @Override
    public RuntimeException pdpMessageProcessingError(Throwable t) {
        return new RuntimeException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#fileNotLocated(java.lang.String)
     */
    @Override
    public IllegalStateException fileNotLocated(String policyConfigFileName) {
        return new IllegalStateException(ErrorCodes.FILE_NOT_LOCATED + policyConfigFileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#optionNotSet(java.lang.String)
     */
    @Override
    public IllegalStateException optionNotSet(String option) {
        return new IllegalStateException(ErrorCodes.OPTION_NOT_SET + option);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#securityTokenRegistryNotSpecified()
     */
    @Override
    public void securityTokenRegistryNotSpecified() {
        this.debug("Security Token registry option not specified: Issued Tokens will not be persisted!");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#securityTokenRegistryInvalidType(java.lang.String)
     */
    @Override
    public void securityTokenRegistryInvalidType(String tokenRegistryOption) {
        logger.warn(tokenRegistryOption + " is not an instance of SecurityTokenRegistry - using default registry");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#securityTokenRegistryInstantiationError()
     */
    @Override
    public void securityTokenRegistryInstantiationError() {
        logger.warn("Error instantiating token registry class - using default registry");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryNotSpecified()
     */
    @Override
    public void revocationRegistryNotSpecified() {
        this.debug("Revocation registry option not specified: cancelled ids will not be persisted!");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryInvalidType(java.lang.String)
     */
    @Override
    public void revocationRegistryInvalidType(String registryOption) {
        logger.warn(registryOption + " is not an instance of RevocationRegistry - using default registry");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryInstantiationError()
     */
    @Override
    public void revocationRegistryInstantiationError() {
        logger.warn("Error instantiating revocation registry class - using default registry");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpiredError()
     */
    @Override
    public ProcessingException assertionExpiredError() {
        return new ProcessingException(ErrorCodes.EXPIRED_ASSERTION);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionInvalidError()
     */
    @Override
    public ProcessingException assertionInvalidError() {
        return new ProcessingException(ErrorCodes.INVALID_ASSERTION);
    }

    @Override
    public RuntimeException writerUnknownTypeError(String name) {
        return new RuntimeException(ErrorCodes.WRITER_UNKNOWN_TYPE + name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerNullValueError(java.lang.String)
     */
    @Override
    public ProcessingException writerNullValueError(String value) {
        return new ProcessingException(ErrorCodes.WRITER_NULL_VALUE + value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerUnsupportedAttributeValueError(java.lang.String)
     */
    @Override
    public RuntimeException writerUnsupportedAttributeValueError(String value) {
        return new RuntimeException(ErrorCodes.WRITER_UNSUPPORTED_ATTRIB_VALUE + value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#issuerInfoMissingStatusCodeError()
     */
    @Override
    public IllegalArgumentException issuerInfoMissingStatusCodeError() {
        return new IllegalArgumentException(ErrorCodes.ISSUER_INFO_MISSING_STATUS_CODE);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#classNotLoadedError(java.lang.String)
     */
    @Override
    public ProcessingException classNotLoadedError(String fqn) {
        return new ProcessingException(ErrorCodes.CLASS_NOT_LOADED + fqn);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotCreateInstance(java.lang.String, java.lang.Exception)
     */
    @Override
    public ProcessingException couldNotCreateInstance(String fqn, Throwable t) {
        return new ProcessingException(ErrorCodes.CANNOT_CREATE_INSTANCE + fqn, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#systemPropertyMissingError(java.lang.String)
     */
    @Override
    public RuntimeException systemPropertyMissingError(String property) {
        return new RuntimeException(ErrorCodes.SYSTEM_PROPERTY_MISSING + property);
    }

    @Override
    public void metaDataStoreDirectoryCreation(String directory) {
        this.trace(directory + " does not exist. Hence creating.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataIdentityProviderLoadingError(java.lang.Exception)
     */
    @Override
    public void metaDataIdentityProviderLoadingError(Throwable t) {
        logger.error("Exception loading the identity providers:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataServiceProviderLoadingError(java.lang.Throwable)
     */
    @Override
    public void metaDataServiceProviderLoadingError(Throwable t) {
        logger.error("Exception loading the service providers:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataPersistEntityDescriptor(java.lang.String)
     */
    @Override
    public void metaDataPersistEntityDescriptor(String path) {
        this.trace("Persisted into " + path);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataPersistTrustedMap(java.lang.String)
     */
    @Override
    public void metaDataPersistTrustedMap(String path) {
        this.trace("Persisted trusted map into " + path);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureAssertionValidationError(java.lang.Exception)
     */
    @Override
    public void signatureAssertionValidationError(Throwable t) {
        logger.error("Cannot validate signature of assertion", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionConditions(java.lang.String, java.lang.String, javax.xml.datatype.XMLGregorianCalendar)
     */
    @Override
    public void assertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter) {
        this.trace("Now=" + now + " ::notBefore=" + notBefore + "::notOnOrAfter="
                        + notOnOrAfter);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpired(java.lang.String)
     */
    @Override
    public void assertionExpired(String id) {
        this.info("Assertion has expired with id=" + id);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unknownObjectType(java.lang.Object)
     */
    @Override
    public RuntimeException unknownObjectType(Object attrValue) {
        return new RuntimeException(ErrorCodes.UNKNOWN_OBJECT_TYPE + attrValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#configurationError(javax.xml.parsers.ParserConfigurationException)
     */
    @Override
    public ConfigurationException configurationError(Throwable t) {
        return new ConfigurationException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureUnknownAlgo(java.lang.String)
     */
    @Override
    public RuntimeException signatureUnknownAlgo(String algo) {
        return new RuntimeException(ErrorCodes.UNKNOWN_SIG_ALGO + algo);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#invalidArgumentError(java.lang.String)
     */
    @Override
    public IllegalArgumentException invalidArgumentError(String message) {
        return new IllegalArgumentException(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#creatingDefaultSTSConfig()
     */
    @Override
    public void stsCreatingDefaultSTSConfig() {
        this.debug("[InstallDefaultConfiguration] Configuration is null. Creating a new configuration");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsLoadingConfiguration(java.lang.String)
     */
    @Override
    public void stsLoadingConfiguration(String fileName) {
        this.debug("[InstallDefaultConfiguration] Configuration file name=" + fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsNoTokenProviderError(java.lang.String)
     */
    @Override
    public ProcessingException stsNoTokenProviderError(String configuration, String protocolContext) {
        return new ProcessingException(ErrorCodes.STS_NO_TOKEN_PROVIDER + configuration + "][ProtoCtx=" + protocolContext
                    + "]");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileNotFoundTCL(java.lang.String)
     */
    @Override
    public void stsConfigurationFileNotFoundTCL(String fileName) {
        logger.warn(fileName + " configuration file not found using TCCL");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileNotFoundClassLoader(java.lang.String)
     */
    @Override
    public void stsConfigurationFileNotFoundClassLoader(String fileName) {
        logger.warn(fileName + " configuration file not found using class loader");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsUsingDefaultConfiguration(java.lang.String)
     */
    @Override
    public void stsUsingDefaultConfiguration(String fileName) {
        logger.warn(fileName + " configuration file not found using URL. Using default configuration values");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileLoaded(java.lang.String)
     */
    @Override
    public void stsConfigurationFileLoaded(String fileName) {
        this.info(fileName + " configuration file loaded");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileParsingError(java.lang.Throwable)
     */
    @Override
    public ConfigurationException stsConfigurationFileParsingError(Throwable t) {
        return new ConfigurationException(ErrorCodes.STS_CONFIGURATION_FILE_PARSING_ERROR, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notSerializableError(java.lang.String)
     */
    @Override
    public IOException notSerializableError(String message) {
        return new IOException(ErrorCodes.NOT_SERIALIZABLE + message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#trustKeyCreationError()
     */
    @Override
    public void trustKeyManagerCreationError(Throwable t) {
        logger.error("Exception in getting TrustKeyManager:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#error(java.lang.String)
     */
    @Override
    public void error(String message) {
        logger.error(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotGetXMLSchema(java.lang.Throwable)
     */
    @Override
    public void couldNotGetXMLSchema(Throwable t) {
        logger.error("Cannot get schema", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#jceProviderCouldNotBeLoaded(java.lang.Throwable)
     */
    @Override
    public void jceProviderCouldNotBeLoaded(String name, Throwable t) {
        this.debug("The provider " + name + " could not be added: " + t.getMessage(), t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerInvalidKeyInfoNullContent()
     */
    @Override
    public ProcessingException writerInvalidKeyInfoNullContentError() {
        return new ProcessingException(ErrorCodes.WRITER_INVALID_KEYINFO_NULL_CONTENT);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notEqualError(java.lang.String, java.lang.String)
     */
    @Override
    public RuntimeException notEqualError(String first, String second) {
        return new RuntimeException(ErrorCodes.NOT_EQUAL + first + " and " + second);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wrongTypeError(java.lang.String)
     */
    @Override
    public IllegalArgumentException wrongTypeError(String message) {
        return new IllegalArgumentException(ErrorCodes.WRONG_TYPE + "xmlSource should be a stax source");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#encryptUnknownAlgoError(java.lang.String)
     */
    @Override
    public RuntimeException encryptUnknownAlgoError(String certAlgo) {
        return new RuntimeException(ErrorCodes.UNKNOWN_ENC_ALGO + certAlgo);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#domMissingDocElementError(java.lang.String)
     */
    @Override
    public IllegalStateException domMissingDocElementError(String element) {
        return new IllegalStateException(ErrorCodes.DOM_MISSING_DOC_ELEMENT + element);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#domMissingElementError(java.lang.String)
     */
    @Override
    public IllegalStateException domMissingElementError(String element) {
        return new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT + element);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSInvalidTokenRequestError()
     */
    @Override
    public WebServiceException stsWSInvalidTokenRequestError() {
        return new WebServiceException(ErrorCodes.STS_INVALID_TOKEN_REQUEST);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSError(java.lang.Throwable)
     */
    @Override
    public WebServiceException stsWSError(Throwable t) {
        return new WebServiceException("Security Token Service Exception", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSConfigurationError(java.lang.Throwable)
     */
    @Override
    public WebServiceException stsWSConfigurationError(Throwable t) {
        return new WebServiceException(ErrorCodes.STS_CONFIGURATION_EXCEPTION, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSInvalidRequestTypeError(java.lang.String)
     */
    @Override
    public WSTrustException stsWSInvalidRequestTypeError(String requestType) {
        return new WSTrustException(ErrorCodes.STS_INVALID_REQUEST_TYPE + requestType);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSHandlingTokenRequestError(java.lang.Throwable)
     */
    @Override
    public WebServiceException stsWSHandlingTokenRequestError(Throwable t) {
        return new WebServiceException(ErrorCodes.STS_EXCEPTION_HANDLING_TOKEN_REQ + t.getMessage(), t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSResponseWritingError(java.lang.Throwable)
     */
    @Override
    public WebServiceException stsWSResponseWritingError(Throwable t) {
        return new WebServiceException(ErrorCodes.STS_RESPONSE_WRITING_ERROR + t.getMessage(), t);
    }

    @Override
    public RuntimeException stsUnableToConstructKeyManagerError(Throwable t) {
        return new RuntimeException(ErrorCodes.STS_UNABLE_TO_CONSTRUCT_KEYMGR, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsPublicKeyError(java.lang.String, java.lang.Throwable)
     */
    @Override
    public RuntimeException stsPublicKeyError(String serviceName, Throwable t) {
        return new RuntimeException(ErrorCodes.STS_PUBLIC_KEY_ERROR + serviceName, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsSigningKeyPairError(java.lang.Exception)
     */
    @Override
    public RuntimeException stsSigningKeyPairError(Throwable t) {
        return new RuntimeException(ErrorCodes.STS_SIGNING_KEYPAIR_ERROR, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsPublicKeyCertError(java.lang.Throwable)
     */
    @Override
    public RuntimeException stsPublicKeyCertError(Throwable t) {
        return new RuntimeException(ErrorCodes.STS_PUBLIC_KEY_CERT, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#issuingTokenForPrincipal(java.security.Principal)
     */
    @Override
    public void issuingTokenForPrincipal(Principal callerPrincipal) {
        this.trace("Issuing token for principal " + callerPrincipal);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#tokenTimeoutNotSpecified()
     */
    @Override
    public void tokenTimeoutNotSpecified() {
        this.debug("Lifetime has not been specified. Using the default timeout value.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#claimsDialectProcessorNotFound(java.lang.String)
     */
    @Override
    public void claimsDialectProcessorNotFound(String dialect) {
        this.debug("Claims have been specified in the request but no processor was found for dialect "
                        + dialect);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsCombinedSecretKeyError(java.lang.Throwable)
     */
    @Override
    public WSTrustException wsTrustCombinedSecretKeyError(Throwable t) {
        return new WSTrustException(ErrorCodes.STS_COMBINED_SECRET_KEY_ERROR, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsClientPublicKeyError()
     */
    @Override
    public WSTrustException wsTrustClientPublicKeyError() {
        return new WSTrustException(ErrorCodes.STS_CLIENT_PUBLIC_KEY_ERROR);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsError(java.lang.Throwable)
     */
    @Override
    public WSTrustException stsError(Throwable t) {
        return new WSTrustException(t.getMessage(), t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsValidatingTokenForRenewal(java.lang.String)
     */
    @Override
    public void stsValidatingTokenForRenewal(String details) {
        this.trace("Validating token for renew request " + details);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureInvalidError(java.lang.String, java.lang.Throwable)
     */
    @Override
    public XMLSignatureException signatureInvalidError(String message, Throwable t) {
        return new XMLSignatureException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsSecurityTokenSignatureNotVerified()
     */
    @Override
    public void stsSecurityTokenSignatureNotVerified() {
        this.trace("Security Token digital signature has NOT been verified. Either the STS has been configured"
                        + "not to sign tokens or the STS key pair has not been properly specified.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsStartedValidationForRequest(java.lang.String)
     */
    @Override
    public void stsStartedValidationForRequest(String details) {
        this.trace("Started validation for request " + details);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureValidatingDocument(java.lang.String)
     */
    @Override
    public void signatureValidatingDocument(String nodeAsString) {
        this.trace("Going to validate signature for:" + nodeAsString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsDelegatingValidationToTokenProvider()
     */
    @Override
    public void stsDelegatingValidationToTokenProvider() {
        this.trace("Delegating token validation to token provider");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureElementToBeSigned(java.lang.String)
     */
    @Override
    public void signatureElementToBeSigned(String namespaceURI) {
        this.trace("NamespaceURI of element to be signed:" + namespaceURI);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureSignedElement(java.lang.String)
     */
    @Override
    public void signatureSignedElement(String nodeAsString) {
        this.trace("Signed Element:" + nodeAsString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#encryptProcessError(java.lang.Throwable)
     */
    @Override
    public RuntimeException encryptProcessError(Throwable t) {
        return new RuntimeException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#pkiLocatingPublic(java.lang.String)
     */
    @Override
    public void pkiLocatingPublic(String alias) {
        this.trace("Locating public key for " + alias);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsSecurityTokenShouldBeEncrypted()
     */
    @Override
    public void stsSecurityTokenShouldBeEncrypted() {
        logger.warn("Security token should be encrypted but no encrypting key could be found");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsReceivedRequestType(java.lang.String)
     */
    @Override
    public void stsReceivedRequestType(String requestType) {
        this.debug("STS received request of type " + requestType);
    }

    @Override
    public void stsKeyTypeNotFoundUsingDefaultBearer() {
        this.debug("No key type could be found in the request. Using the default BEARER type.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsKeySizeNotFoundUsingDefault(long)
     */
    @Override
    public void stsKeySizeNotFoundUsingDefault(long kEY_SIZE) {
        this.debug("No key size could be found in the request. Using the default size. (" + kEY_SIZE + ")");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsUnableToDecodePasswordError(java.lang.String)
     */
    @Override
    public RuntimeException unableToDecodePasswordError(String password) {
        return new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to decode password:" + password);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotLoadProperties(java.lang.String)
     */
    @Override
    public IllegalStateException couldNotLoadProperties(String configFile) {
        return new IllegalStateException(ErrorCodes.PROCESSING_EXCEPTION + "Could not load properties from "
                        + configFile);
    }

    /**
     * @param type
     */
    @Override
    public void stsUnableToParseOnBehalfType(Object type) {
        this.debug("Unable to parse the contents of the OnBehalfOfType: " + type);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsKeyInfoTypeCreationError(java.lang.Throwable)
     */
    @Override
    public WSTrustException stsKeyInfoTypeCreationError(Throwable t) {
        return new WSTrustException(ErrorCodes.PROCESSING_EXCEPTION + "Error creating KeyInfoType", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsSecretKeyNotEncrypted()
     */
    @Override
    public void stsSecretKeyNotEncrypted() {
        logger.warn("Secret key could not be encrypted because the endpoint's PKC has not been specified");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authCouldNotIssueSAMLToken()
     */
    @Override
    public LoginException authCouldNotIssueSAMLToken() {
        return new LoginException(ErrorCodes.PROCESSING_EXCEPTION + "Could not issue a SAML Security Token");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authLoginError(java.lang.Throwable)
     */
    @Override
    public LoginException authLoginError(Throwable t) {
        LoginException loginException = new LoginException("Error during login/authentication");
        
        loginException.initCause(t);
        
        return loginException;
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authAddedCredential(org.picketlink.identity.federation.core.wstrust.SamlCredential)
     */
    @Override
    public void authAddedSAMLCredential(SamlCredential samlCredential) {
        logger.debug("Added SAML Credential :" + samlCredential);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authUserNameFromCallbackIsNull()
     */
    @Override
    public void authUserNameFromCallbackIsNull() {
        trace("UserName from callback is null");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authPasswordFromCallbackIsNull()
     */
    @Override
    public void authPasswordFromCallbackIsNull() {
        trace("Password from callback is null");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authCouldNotCreateWSTrustClient(java.lang.Throwable)
     */
    @Override
    public IllegalStateException authCouldNotCreateWSTrustClient(Throwable t) {
        return new IllegalStateException(ErrorCodes.PROCESSING_EXCEPTION + "Could not create WSTrustClient:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authSAMLAssertionWithoutExpiration(java.lang.String)
     */
    @Override
    public void authSAMLAssertionWithoutExpiration(String id) {
        logger.warn("SAML Assertion has been found to have no expiration: ID = " + id);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authCouldNotValidateSAMLToken(org.w3c.dom.Element)
     */
    @Override
    public LoginException authCouldNotValidateSAMLToken(Element token) {
        return new LoginException(ErrorCodes.PROCESSING_EXCEPTION + "Could not validate the SAML Security Token :"
                        + token);
    }

    @Override
    public void authSAMLValidationResult(boolean result) {
        debug("SAML Token Validation result: " + result);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#authCouldNotLocateSecurityToken()
     */
    @Override
    public LoginException authCouldNotLocateSecurityToken() {
        return new LoginException(ErrorCodes.NULL_VALUE + "Could not locate a Security Token from the callback.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wsTrustNullCancelTargetError()
     */
    @Override
    public ProcessingException wsTrustNullCancelTargetError() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Invalid cancel request: missing required CancelTarget");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#saml11MarshallError(java.lang.Throwable)
     */
    @Override
    public ProcessingException samlAssertionMarshallError(Throwable t) {
        return new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Failed to marshall SAMLV1.1 assertion", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wsTrustNullRenewTargetError()
     */
    @Override
    public ProcessingException wsTrustNullRenewTargetError() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Invalid renew request: missing required RenewTarget");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#saml11UnmarshallError(java.lang.Throwable)
     */
    @Override
    public ProcessingException samlAssertionUnmarshallError(Throwable t) {
        return new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + "Error unmarshalling assertion", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlAssertionRevokedCouldNotRenew()
     */
    @Override
    public ProcessingException samlAssertionRevokedCouldNotRenew(String id) {
        return new ProcessingException(ErrorCodes.ASSERTION_RENEWAL_EXCEPTION + "SAMLV1.1 Assertion with id "
                    + id + " has been canceled and cannot be renewed");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlStartingValidation()
     */
    @Override
    public void samlAssertionStartingValidation() {
        trace("SAML token validation started");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wsTrustNullValidationTargetError()
     */
    @Override
    public ProcessingException wsTrustNullValidationTargetError() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Bad validate request: missing required ValidateTarget");
    }

    @Override
    public void stsNoAttributeProviderSet() {
        debug("No attribute provider set");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWrongAttributeProviderTypeNotInstalled(java.lang.String)
     */
    @Override
    public void stsWrongAttributeProviderTypeNotInstalled(String attributeProviderClassName) {
        logger.warn("Attribute provider not installed: " + attributeProviderClassName
                            + "is not an instance of SAML20TokenAttributeProvider");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsAttributeProviderInstationError(java.lang.Throwable)
     */
    @Override
    public void attributeProviderInstationError(Throwable t) {
        logger.warn("Error instantiating attribute provider: " + t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlAssertion(java.lang.String)
     */
    @Override
    public void samlAssertion(String nodeAsString) {
        trace("SAML Assertion Element=" + nodeAsString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wsTrustUnableToGetDataTypeFactory(javax.xml.datatype.DatatypeConfigurationException)
     */
    @Override
    public RuntimeException wsTrustUnableToGetDataTypeFactory(Throwable t) {
        return new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to get DatatypeFactory instance", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wsTrustValidationStatusCodeMissing()
     */
    @Override
    public ProcessingException wsTrustValidationStatusCodeMissing() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Validation status code is missing");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#identityServerActiveSessionCount(int)
     */
    @Override
    public void identityServerActiveSessionCount(int activeSessionCount) {
        info("Active Session Count=" + activeSessionCount);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#identityServerSessionCreated(java.lang.String, int)
     */
    @Override
    public void identityServerSessionCreated(String id, int activeSessionCount) {
        trace("Session Created with id=" + id + "::active session count=" + activeSessionCount);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#identityServerSessionDestroyed(java.lang.String, int)
     */
    @Override
    public void identityServerSessionDestroyed(String id, int activeSessionCount) {
        trace("Session Destroyed with id=" + id + "::active session count=" + activeSessionCount);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unknowCredentialType(java.lang.String)
     */
    @Override
    public RuntimeException unknowCredentialType(String name) {
        return new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + "Unknown credential type:" + name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerRoleGeneratorSetup(java.lang.String)
     */
    @Override
    public void samlHandlerRoleGeneratorSetup(String name) {
        trace("RoleGenerator set to " + name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerRoleGeneratorSetupError(java.lang.Throwable)
     */
    @Override
    public void samlHandlerRoleGeneratorSetupError(Throwable t) {
        logger.error("Exception initializing role generator:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerAttributeSetup(java.lang.String)
     */
    @Override
    public void samlHandlerAttributeSetup(String name) {
        logger.trace("AttributeManager set to " + name);
    }

    @Override
    public RuntimeException samlHandlerAssertionNotFound() {
        return new RuntimeException(ErrorCodes.NULL_VALUE + "Assertion not found in the handler request");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerAuthnRequestIsNull()
     */
    @Override
    public ProcessingException samlHandlerAuthnRequestIsNull() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "AuthnRequest is null");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#destination(java.lang.String)
     */
    @Override
    public void destination(String destination) {
        trace(destination);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerAuthenticationError(java.lang.Throwable)
     */
    @Override
    public void samlHandlerAuthenticationError(Throwable t) {
        logger.error("Exception in processing authentication:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerNoAssertionFromIDP()
     */
    @Override
    public IllegalArgumentException samlHandlerNoAssertionFromIDP() {
        return new IllegalArgumentException(ErrorCodes.NULL_VALUE + "No assertions in reply from IDP");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerNullEncryptedAssertion()
     */
    @Override
    public ProcessingException samlHandlerNullEncryptedAssertion() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Null encrypted assertion element");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerIDPAuthenticationFailedError()
     */
    @Override
    public SecurityException samlHandlerIDPAuthenticationFailedError() {
        return new SecurityException(ErrorCodes.IDP_AUTH_FAILED + "IDP forbid the user");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpiredError(org.picketlink.identity.federation.core.saml.v2.exceptions.AssertionExpiredException)
     */
    @Override
    public ProcessingException assertionExpiredError(AssertionExpiredException aee) {
        return new ProcessingException(new ProcessingException(ErrorCodes.EXPIRED_ASSERTION + "Assertion has expired", aee));
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#invalidRole(java.lang.String)
     */
    @Override
    public void invalidRole(String roles) {
        trace(roles);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unsupportedRoleType(java.lang.Object)
     */
    @Override
    public RuntimeException unsupportedRoleType(Object attrValue) {
        return new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + "Unknown role object type : " + attrValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSavedAuthnRequestIdIntoSession(java.lang.String)
     */
    @Override
    public void samlHandlerSavedAuthnRequestIdIntoSession(String authnRequestId) {
        trace("ID of authentication request " + authnRequestId + " saved into HTTP session.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSuccessfulInResponseToValidation(java.lang.String)
     */
    @Override
    public void samlHandlerSuccessfulInResponseToValidation(String inResponseTo) {
        trace("Successful verification of InResponseTo for request " + inResponseTo);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerFailedInResponseToVerification(java.lang.String, java.lang.String)
     */
    @Override
    public void samlHandlerFailedInResponseToVerification(String inResponseTo, String authnRequestId) {
        trace("Verification of InResponseTo failed. InResponseTo from SAML response is " + inResponseTo
                    + ". Value of request Id from HTTP session is " + authnRequestId);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerFailedInResponseToVerificarionError()
     */
    @Override
    public ProcessingException samlHandlerFailedInResponseToVerificarionError() {
        return new ProcessingException(ErrorCodes.AUTHN_REQUEST_ID_VERIFICATION_FAILED);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerDomainsTrustedByIDP(java.lang.String, java.lang.String)
     */
    @Override
    public void samlHandlerDomainsTrustedByIDP(String domainsTrusted, String issuerDomain) {
        trace("Domains that IDP trusts=" + domainsTrusted + " and issuer domain=" + issuerDomain);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerTrustDomainCheck(java.lang.String)
     */
    @Override
    public void samlHandlerTrustDomainCheck(String uriBit) {
        trace("Matching uri bit=" + uriBit);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerTrustedDomainMatched(java.lang.String, java.lang.String)
     */
    @Override
    public void samlHandlerTrustedDomainMatched(String uriBit, String issuerDomain) {
        trace("Matched " + uriBit + " trust for " + issuerDomain);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerIssuerNotTrustedError(java.lang.String)
     */
    @Override
    public IssuerNotTrustedException samlHandlerIssuerNotTrustedError(String issuer) {
        return new IssuerNotTrustedException("Issuer not Trusted by the IDP: " + issuer);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerIssuerNotTrustedError(java.lang.Throwable)
     */
    @Override
    public IssuerNotTrustedException samlHandlerIssuerNotTrustedError(Throwable t) {
        return new IssuerNotTrustedException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerDomainsTrustedBySP(java.lang.String, java.lang.String)
     */
    @Override
    public void samlHandlerDomainsTrustedBySP(String domainsTrusted, String issuerDomain) {
        trace("Domains that SP trusts=" + domainsTrusted + " and issuer domain=" + issuerDomain);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerTrustElementMissingError()
     */
    @Override
    public ConfigurationException samlHandlerTrustElementMissingError() {
        return new ConfigurationException(ErrorCodes.NULL_VALUE + "trust element missing");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerIdentityServerNotFound()
     */
    @Override
    public ProcessingException samlHandlerIdentityServerNotFoundError() {
        return new ProcessingException(ErrorCodes.NULL_VALUE + "Identity Server not found");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerPrincipalNotFoundError()
     */
    @Override
    public ProcessingException samlHandlerPrincipalNotFoundError() {
        return new ProcessingException(ErrorCodes.PRINCIPAL_NOT_FOUND);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerGeneratingSuccessStatusResponse(java.lang.String)
     */
    @Override
    public void samlHandlerGeneratingSuccessStatusResponse(String originalIssuer) {
        trace("Generating Success Status Response for " + originalIssuer);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerNoDocumentToSign()
     */
    @Override
    public void samlHandlerNoDocumentToSign() {
        trace("No document generated in the handler chain. Cannot generate signature");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerNoResponseDocumentFound()
     */
    @Override
    public void samlHandlerNoResponseDocumentFound() {
        trace("No response document found");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSigningDocumentForPOSTBinding()
     */
    @Override
    public void samlHandlerSigningDocumentForPOSTBinding() {
        trace("Going to sign response document with POST binding type");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSigningDocumentForRedirectBinding()
     */
    @Override
    public void samlHandlerSigningDocumentForRedirectBinding() {
        trace("Going to sign response document with REDIRECT binding type");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerKeyPairNotFound()
     */
    @Override
    public void samlHandlerKeyPairNotFound() {
        trace("Key Pair cannot be found");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerKeyPairNotFoundError()
     */
    @Override
    public ProcessingException samlHandlerKeyPairNotFoundError() {
        return new ProcessingException("Key Pair cannot be found");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerErrorSigningRedirectBindingMessage(java.lang.Throwable)
     */
    @Override
    public void samlHandlerErrorSigningRedirectBindingMessage(Throwable t) {
        logger.error("Error when trying to sign message for redirection", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSigningRedirectBindingMessageError(org.picketlink.identity.federation.core.exceptions.ConfigurationException)
     */
    @Override
    public RuntimeException samlHandlerSigningRedirectBindingMessageError(Throwable t) {
        return new RuntimeException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerValidatingResponseForHTTPMethod(java.lang.String)
     */
    @Override
    public void samlHandlerValidatingResponseForHTTPMethod(String method) {
        trace("HTTP method for validating response: " + method);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureValidationError()
     */
    @Override
    public SignatureValidationException samlHandlerSignatureValidationFailed() {
        return new SignatureValidationException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation Failed");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerErrorValidatingSignature(java.lang.Throwable)
     */
    @Override
    public void samlHandlerErrorValidatingSignature(Throwable t) {
        logger.error("Error validating signature:", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerInvalidSignatureError()
     */
    @Override
    public ProcessingException samlHandlerInvalidSignatureError() {
        return new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Error validating signature.");
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlHandlerSignatureNorPresentError()
     */
    @Override
    public ProcessingException samlHandlerSignatureNorPresentError() {
        return new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed. Signature is not present. Check if the IDP is supporting signatures.");
    }

    @Override
    public ProcessingException samlHandlerSignatureValidationError(Throwable t) {
        return new ProcessingException(ErrorCodes.INVALID_DIGITAL_SIGNATURE + "Signature Validation failed", t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlIDPUserClosedBrowserCancelingToken()
     */
    @Override
    public void samlIDPUserClosedBrowserCancelingToken() {
        trace("User has closed the browser. So we proceed to cancel the STS issued token.");
    }

}