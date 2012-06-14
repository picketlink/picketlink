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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class DefaultPicketLinkLogger implements PicketLinkLogger {

    private Logger logger = Logger.getLogger(PicketLinkLogger.class.getPackage().getName());

    DefaultPicketLinkLogger() {

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
    public IllegalArgumentException shouldNotBeTheSame(String string) {
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
        return new ProcessingException(ErrorCodes.PROCESSING_EXCEPTION + t.getMessage());
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
        if (logger.isTraceEnabled()) {
            logger.trace("SAML Response Document=" + samlResponseDocumentAsString);
        }
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureError(java.lang.Throwable)
     */
    @Override
    public ProcessingException signatureError(Throwable e) {
        return new ProcessingException(ErrorCodes.SIGNING_PROCESS_FAILURE, e);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#sendingXACMLDecisionQuery(java.lang.String)
     */
    @Override
    public void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument) {
        if (logger.isDebugEnabled()) {
            logger.debug("Sending XACML Decision Query::" + xacmlDecisionQueryDocument);
        }
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#nullValue(java.lang.String)
     */
    @Override
    public RuntimeException nullValue(String nullValue) {
        return new RuntimeException(ErrorCodes.NULL_VALUE + "Did not find Response node");
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
        logger.info(auditEvent);
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
        logger.trace("getPublicKey::Keystore is null. so setting it up");
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
        logger.trace("No public key found for alias=" + alias);
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
        logger.trace("Looking for Parser for :" + qname);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#receivedXACMLMessage(java.lang.String)
     */
    @Override
    public void receivedXACMLMessage(String asString) {
        logger.debug("Received Message::" + asString);
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
        logger.debug("Security Token registry option not specified: Issued Tokens will not be persisted!");
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
        logger.debug("Revocation registry option not specified: cancelled ids will not be persisted!");
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
        return new ProcessingException(ErrorCodes.CLASS_NOT_LOADED);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotCreateInstance(java.lang.String, java.lang.Exception)
     */
    @Override
    public ProcessingException couldNotCreateInstance(String fqn, Throwable t) {
        return new ProcessingException(ErrorCodes.CANNOT_CREATE_INSTANCE, t);
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
        logger.trace(directory + " does not exist. Hence creating.");
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
        logger.trace("Persisted into " + path);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataPersistTrustedMap(java.lang.String)
     */
    @Override
    public void metaDataPersistTrustedMap(String path) {
        logger.trace("Persisted trusted map into " + path);
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
        logger.trace("Now=" + now + " ::notBefore=" + notBefore + "::notOnOrAfter="
                        + notOnOrAfter);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpired(java.lang.String)
     */
    @Override
    public void assertionExpired(String id) {
        logger.info("Assertion has expired with id=" + id);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unknownObjectType(java.lang.Object)
     */
    @Override
    public RuntimeException unknownObjectType(Object attrValue) {
        return new RuntimeException(ErrorCodes.UNKNOWN_OBJECT_TYPE + attrValue);
    }
 
}