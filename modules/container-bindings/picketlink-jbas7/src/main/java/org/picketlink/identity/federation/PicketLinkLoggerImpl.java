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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.ws.WebServiceException;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;


/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public final class PicketLinkLoggerImpl implements PicketLinkLogger {

    PicketLinkLoggerImpl() {
        
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#nullArgument(java.lang.String)
     */
    public IllegalArgumentException nullArgumentError(String argument) {
        return PicketLinkMessages.MESSAGES.nullArgument(argument);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#shouldNotBeTheSame(java.lang.String)
     */
    public IllegalArgumentException shouldNotBeTheSame(String message) {
        return PicketLinkMessages.MESSAGES.shouldNotBeTheSame(message);
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#resourceNotFound(java.lang.String)
     */
    public ProcessingException resourceNotFound(String resource) {
        return PicketLinkMessages.MESSAGES.resourceNotFound(resource);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#processingError(java.lang.Throwable)
     */
    public ProcessingException processingError(Throwable t) {
        return PicketLinkMessages.MESSAGES.processingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unsupportedType(java.lang.String)
     */
    public RuntimeException unsupportedType(String name) {
        return PicketLinkMessages.MESSAGES.unsupportedType(name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#samlResponseDocument(java.lang.String)
     */
    public void samlResponseDocument(String samlResponseDocumentAsString) {
        PicketLinkLoggerMessages.ROOT_LOGGER.samlResponseDocument(samlResponseDocumentAsString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureError(java.lang.Throwable)
     */
    public ProcessingException signatureError(Throwable e) {
        return PicketLinkMessages.MESSAGES.signatureError(e);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#sendingXACMLDecisionQuery(java.lang.String)
     */
    public void sendingXACMLDecisionQuery(String xacmlDecisionQueryDocument) {
        PicketLinkLoggerMessages.ROOT_LOGGER.sendingXACMLDecisionQuery(xacmlDecisionQueryDocument);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#nullValue(java.lang.String)
     */
    public RuntimeException nullValueError(String nullValue) {
        return PicketLinkMessages.MESSAGES.nullValue(nullValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notImplementedYet()
     */
    public RuntimeException notImplementedYet(String feature) {
        return PicketLinkMessages.MESSAGES.notImplementedYet(feature);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditConfigurationError(javax.naming.NamingException)
     */
    public ConfigurationException auditConfigurationError(Throwable t) {
        return PicketLinkMessages.MESSAGES.auditConfigurationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditNullAuditManager()
     */
    public IllegalStateException auditNullAuditManager() {
        return PicketLinkMessages.MESSAGES.auditNullAuditManager();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return PicketLinkLoggerMessages.ROOT_LOGGER.isInfoEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#auditEvent(java.lang.String)
     */
    public void auditEvent(String auditEvent) {
        PicketLinkLoggerMessages.ROOT_LOGGER.auditEvent(auditEvent);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#injectedValueMissing(java.lang.String)
     */
    public RuntimeException injectedValueMissing(String value) {
        return PicketLinkMessages.MESSAGES.injectedValueMissing(value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keystoreSetup()
     */
    public void keyStoreSetup() {
        PicketLinkLoggerMessages.ROOT_LOGGER.keyStoreSetup();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullStore()
     */
    public IllegalStateException keyStoreNullStore() {
        return PicketLinkMessages.MESSAGES.keyStoreNullStore();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullPublicKeyForAlias(java.lang.String)
     */
    public void keyStoreNullPublicKeyForAlias(String alias) {
        PicketLinkLoggerMessages.ROOT_LOGGER.keyStoreNullPublicKeyForAlias(alias);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreConfigurationError(java.lang.Throwable)
     */
    public TrustKeyConfigurationException keyStoreConfigurationError(Throwable t) {
        return PicketLinkMessages.MESSAGES.keyStoreConfigurationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreProcessingError(java.lang.Throwable)
     */
    public TrustKeyProcessingException keyStoreProcessingError(Throwable t) {
        return PicketLinkMessages.MESSAGES.keyStoreProcessingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreMissingDomainAlias(java.lang.String)
     */
    public IllegalStateException keyStoreMissingDomainAlias(String domain) {
        return PicketLinkMessages.MESSAGES.keyStoreMissingDomainAlias(domain);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullSigningKeyPass()
     */
    public RuntimeException keyStoreNullSigningKeyPass() {
        return PicketLinkMessages.MESSAGES.keyStoreNullSigningKeyPass();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNotLocated(java.lang.String)
     */
    public RuntimeException keyStoreNotLocated(String keyStore) {
        return PicketLinkMessages.MESSAGES.keyStoreNotLocated(keyStore);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#keyStoreNullAlias()
     */
    public IllegalStateException keyStoreNullAlias() {
        return PicketLinkMessages.MESSAGES.keyStoreNullAlias();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownEndElement(java.lang.String)
     */
    public RuntimeException parserUnknownEndElement(String endElementName) {
        return PicketLinkMessages.MESSAGES.parserUnknownEndElement(endElementName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parseUnknownTag(java.lang.String, javax.xml.stream.Location)
     */
    public RuntimeException parserUnknownTag(String tag, Location location) {
        return PicketLinkMessages.MESSAGES.parseUnknownTag(tag, location);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parseRequiredAttribute(java.lang.String)
     */
    public ParsingException parserRequiredAttribute(String attribute) {
        return PicketLinkMessages.MESSAGES.parseRequiredAttribute(attribute);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownStartElement(java.lang.String, javax.xml.stream.Location)
     */
    public RuntimeException parserUnknownStartElement(String elementName, Location location) {
        return PicketLinkMessages.MESSAGES.parserUnknownStartElement(elementName, location);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserNullStartElement()
     */
    public IllegalStateException parserNullStartElement() {
        return PicketLinkMessages.MESSAGES.parserNullStartElement();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnknownXSI(java.lang.String)
     */
    public ParsingException parserUnknownXSI(String xsiTypeValue) {
        return PicketLinkMessages.MESSAGES.parserUnknownXSI(xsiTypeValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedEndTag(java.lang.String)
     */
    public ParsingException parserExpectedEndTag(String tagName) {
        return PicketLinkMessages.MESSAGES.parserExpectedEndTag(tagName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserException(java.lang.Throwable)
     */
    public ParsingException parserException(Throwable t) {
        return PicketLinkMessages.MESSAGES.parserException(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedTextValue(java.lang.String)
     */
    public ParsingException parserExpectedTextValue(String string) {
        return PicketLinkMessages.MESSAGES.parserExpectedTextValue(string);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedXSI(java.lang.String)
     */
    public RuntimeException parserExpectedXSI(String expectedXsi) {
        return PicketLinkMessages.MESSAGES.parserExpectedXSI(expectedXsi);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserExpectedTag(java.lang.String, java.lang.String)
     */
    public RuntimeException parserExpectedTag(String tag, String foundElementTag) {
        return PicketLinkMessages.MESSAGES.parserExpectedTag(tag, foundElementTag);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserFailed()
     */
    public RuntimeException parserFailed(String elementName) {
        return PicketLinkMessages.MESSAGES.parserFailed(elementName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserUnableParsingNullToken()
     */
    public ParsingException parserUnableParsingNullToken() {
        return PicketLinkMessages.MESSAGES.parserUnableParsingNullToken();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#parserError(java.lang.Throwable)
     */
    public ParsingException parserError(Throwable t) {
        return PicketLinkMessages.MESSAGES.parserError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#lookingParserForElement(javax.xml.namespace.QName)
     */
    public void lookingParserForElement(QName qname) {
        PicketLinkLoggerMessages.ROOT_LOGGER.lookingParserForElement(qname);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#receivedXACMLMessage(java.lang.String)
     */
    public void receivedXACMLMessage(String asString) {
        PicketLinkLoggerMessages.ROOT_LOGGER.receivedXACMLMessage(asString);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#pdpMessageProcessingError(java.lang.Throwable)
     */
    public RuntimeException pdpMessageProcessingError(Throwable t) {
        return PicketLinkMessages.MESSAGES.pdpMessageProcessingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#fileNotLocated(java.lang.String)
     */
    public IllegalStateException fileNotLocated(String policyConfigFileName) {
        return PicketLinkMessages.MESSAGES.fileNotLocated(policyConfigFileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#optionNotSet(java.lang.String)
     */
    public IllegalStateException optionNotSet(String option) {
        return PicketLinkMessages.MESSAGES.optionNotSet(option);
    }

    public void securityTokenRegistryNotSpecified() {
        PicketLinkLoggerMessages.ROOT_LOGGER.securityTokenRegistryNotSpecified();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#securityTokenRegistryInvalidType(java.lang.String)
     */
    public void securityTokenRegistryInvalidType(String tokenRegistryOption) {
        PicketLinkLoggerMessages.ROOT_LOGGER.securityTokenRegistryInvalidType(tokenRegistryOption);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#securityTokenRegistryInstantiationError()
     */
    public void securityTokenRegistryInstantiationError() {
        PicketLinkLoggerMessages.ROOT_LOGGER.securityTokenRegistryInstantiationError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryNotSpecified()
     */
    public void revocationRegistryNotSpecified() {
        PicketLinkLoggerMessages.ROOT_LOGGER.revocationRegistryNotSpecified();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryInvalidType(java.lang.String)
     */
    public void revocationRegistryInvalidType(String registryOption) {
        PicketLinkLoggerMessages.ROOT_LOGGER.revocationRegistryInvalidType(registryOption);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#revocationRegistryInstantiationError()
     */
    public void revocationRegistryInstantiationError() {
        PicketLinkLoggerMessages.ROOT_LOGGER.revocationRegistryInstantiationError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpiredError()
     */
    public ProcessingException assertionExpiredError() {
        return PicketLinkMessages.MESSAGES.assertionExpiredError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionInvalidError()
     */
    public ProcessingException assertionInvalidError() {
        return PicketLinkMessages.MESSAGES.assertionInvalidError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerUnknownTypeError(java.lang.String)
     */
    public RuntimeException writerUnknownTypeError(String name) {
        return PicketLinkMessages.MESSAGES.writerUnknownTypeError(name);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerNullValueError(java.lang.String)
     */
    public ProcessingException writerNullValueError(String value) {
        return PicketLinkMessages.MESSAGES.writerNullValueError(value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerUnsupportedAttributeValueError(java.lang.String)
     */
    public RuntimeException writerUnsupportedAttributeValueError(String value) {
        return PicketLinkMessages.MESSAGES.writerUnsupportedAttributeValueError(value);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#issuerInfoMissingStatusCodeError()
     */
    public IllegalArgumentException issuerInfoMissingStatusCodeError() {
        return PicketLinkMessages.MESSAGES.issuerInfoMissingStatusCodeError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#classNotLoadedError(java.lang.String)
     */
    public ProcessingException classNotLoadedError(String fqn) {
        return PicketLinkMessages.MESSAGES.classNotLoadedError(fqn);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotCreateInstance(java.lang.String, java.lang.Throwable)
     */
    public ProcessingException couldNotCreateInstance(String fqn, Throwable t) {
        return PicketLinkMessages.MESSAGES.couldNotCreateInstance(fqn, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#systemPropertyMissingError(java.lang.String)
     */
    public RuntimeException systemPropertyMissingError(String property) {
        return PicketLinkMessages.MESSAGES.systemPropertyMissingError(property);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metadataStoreDirectoryCreation(java.lang.String)
     */
    public void metaDataStoreDirectoryCreation(String directory) {
        PicketLinkLoggerMessages.ROOT_LOGGER.metaDataDirectoryCreation(directory);
    }

    public void metaDataIdentityProviderLoadingError(Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.metaDataIdentityProviderLoadingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataServiceProviderLoadingError(java.lang.Throwable)
     */
    public void metaDataServiceProviderLoadingError(Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.metaDataServiceProviderLoadingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataPersistEntityDescriptor(java.lang.String)
     */
    public void metaDataPersistEntityDescriptor(String path) {
        PicketLinkLoggerMessages.ROOT_LOGGER.metaDataPersistEntityDescriptor(path);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#metaDataPersistTrustedMap(java.lang.String)
     */
    public void metaDataPersistTrustedMap(String path) {
        PicketLinkLoggerMessages.ROOT_LOGGER.metaDataPersistTrustedMap(path);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureAssertionValidationError(java.lang.Throwable)
     */
    public void signatureAssertionValidationError(Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.signatureAssertionValidationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionConditions(java.lang.String, java.lang.String, javax.xml.datatype.XMLGregorianCalendar)
     */
    public void assertionConditions(String now, String notBefore, XMLGregorianCalendar notOnOrAfter) {
        PicketLinkLoggerMessages.ROOT_LOGGER.assertionConditions(now, notBefore, notOnOrAfter);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#assertionExpired(java.lang.String)
     */
    public void assertionExpired(String id) {
        PicketLinkLoggerMessages.ROOT_LOGGER.assertionExpired(id);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#unknownObjectType(java.lang.Object)
     */
    public RuntimeException unknownObjectType(Object attrValue) {
        return PicketLinkMessages.MESSAGES.unknownObjectType(attrValue);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#configurationError(java.lang.Throwable)
     */
    public ConfigurationException configurationError(Throwable t) {
        return PicketLinkMessages.MESSAGES.configurationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#trace(java.lang.String)
     */
    public void trace(String message) {
        PicketLinkLoggerMessages.ROOT_LOGGER.trace(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#signatureUnknownAlgo(java.lang.String)
     */
    public RuntimeException signatureUnknownAlgo(String algo) {
        return PicketLinkMessages.MESSAGES.signatureUnknownAlgo(algo);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#invalidArgumentError(java.lang.String)
     */
    public IllegalArgumentException invalidArgumentError(String message) {
        return PicketLinkMessages.MESSAGES.invalidArgumentError(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#creatingDefaultSTSConfig()
     */
    public void stsCreatingDefaultSTSConfig() {
        PicketLinkLoggerMessages.ROOT_LOGGER.creatingDefaultSTSConfig();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsLoadingConfiguration(java.lang.String)
     */
    public void stsLoadingConfiguration(String fileName) {
        PicketLinkLoggerMessages.ROOT_LOGGER.stsLoadingConfiguration(fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsNoTokenProviderError(java.lang.String, java.lang.String)
     */
    public ProcessingException stsNoTokenProviderError(String configuration, String protocolContext) {
        return PicketLinkMessages.MESSAGES.stsNoTokenProviderError(configuration, protocolContext);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#debug(java.lang.String)
     */
    public void debug(String message) {
        PicketLinkLoggerMessages.ROOT_LOGGER.debug(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileNotFoundTCL(java.lang.String)
     */
    public void stsConfigurationFileNotFoundTCL(String fileName) {
        PicketLinkLoggerMessages.ROOT_LOGGER.stsConfigurationFileNotFoundTCL(fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileNotFoundClassLoader(java.lang.String)
     */
    public void stsConfigurationFileNotFoundClassLoader(String fileName) {
        PicketLinkLoggerMessages.ROOT_LOGGER.stsConfigurationFileNotFoundClassLoader(fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsUsingDefaultConfiguration(java.lang.String)
     */
    public void stsUsingDefaultConfiguration(String fileName) {
        PicketLinkLoggerMessages.ROOT_LOGGER.stsUsingDefaultConfiguration(fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileLoaded(java.lang.String)
     */
    public void stsConfigurationFileLoaded(String fileName) {
        PicketLinkLoggerMessages.ROOT_LOGGER.stsConfigurationFileLoaded(fileName);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsConfigurationFileParsingError(java.lang.Throwable)
     */
    public ConfigurationException stsConfigurationFileParsingError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsConfigurationFileParsingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notSerializableError(java.lang.String)
     */
    public IOException notSerializableError(String message) {
        return PicketLinkMessages.MESSAGES.notSerializableError(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#trustKeyCreationError(java.lang.Throwable)
     */
    public void trustKeyManagerCreationError(Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.trustKeyManagerCreationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#info(java.lang.String)
     */
    public void info(String message) {
        PicketLinkLoggerMessages.ROOT_LOGGER.info(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#error(java.lang.String)
     */
    public void error(String message) {
        PicketLinkLoggerMessages.ROOT_LOGGER.error(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#couldNotGetXMLSchema(java.lang.Throwable)
     */
    public void couldNotGetXMLSchema(Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.couldNotGetXMLSchema(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return PicketLinkLoggerMessages.ROOT_LOGGER.isTraceEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return PicketLinkLoggerMessages.ROOT_LOGGER.isDebugEnabled();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#jceProviderCouldNotBeLoaded(java.lang.String, java.lang.Throwable)
     */
    public void jceProviderCouldNotBeLoaded(String name, Throwable t) {
        PicketLinkLoggerMessages.ROOT_LOGGER.jceProviderCouldNotBeLoaded(name, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#writerInvalidKeyInfoNullContent()
     */
    public ProcessingException writerInvalidKeyInfoNullContentError() {
        return PicketLinkMessages.MESSAGES.writerInvalidKeyInfoNullContentError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#notEqualError(java.lang.String, java.lang.String)
     */
    public RuntimeException notEqualError(String first, String second) {
        return PicketLinkMessages.MESSAGES.notEqualError(first, second);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#wrongTypeError(java.lang.String)
     */
    public IllegalArgumentException wrongTypeError(String message) {
        return PicketLinkMessages.MESSAGES.wrongTypeError(message);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#encryptUnknownAlgoError(java.lang.String)
     */
    public RuntimeException encryptUnknownAlgoError(String certAlgo) {
        return PicketLinkMessages.MESSAGES.encryptUnknownAlgoError(certAlgo);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#domMissingDocElementError(java.lang.String)
     */
    public IllegalStateException domMissingDocElementError(String element) {
        return PicketLinkMessages.MESSAGES.domMissingDocElementError(element);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#domMissingElementError(java.lang.String)
     */
    public IllegalStateException domMissingElementError(String element) {
        return PicketLinkMessages.MESSAGES.domMissingElementError(element);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSInvalidTokenRequestError()
     */
    public WebServiceException stsWSInvalidTokenRequestError() {
        return PicketLinkMessages.MESSAGES.stsWSInvalidTokenRequestError();
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSError(java.lang.Throwable)
     */
    public WebServiceException stsWSError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsWSError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSConfigurationError(java.lang.Throwable)
     */
    public WebServiceException stsWSConfigurationError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsWSConfigurationError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSInvalidRequestTypeError(java.lang.String)
     */
    public WSTrustException stsWSInvalidRequestTypeError(String requestType) {
        return PicketLinkMessages.MESSAGES.stsWSInvalidRequestTypeError(requestType);
    }

    public WebServiceException stsWSHandlingTokenRequestError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsWSHandlingTokenRequestError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsWSResponseWritingError(java.lang.Throwable)
     */
    public WebServiceException stsWSResponseWritingError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsWSResponseWritingError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsUnableToConstructKeyManagerError(java.lang.Throwable)
     */
    public RuntimeException stsUnableToConstructKeyManagerError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsUnableToConstructKeyManagerError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsPublicKeyError(java.lang.String, java.lang.Throwable)
     */
    public RuntimeException stsPublicKeyError(String serviceName, Throwable t) {
        return PicketLinkMessages.MESSAGES.stsPublicKeyError(serviceName, t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsSigningKeyPairError(java.lang.Throwable)
     */
    public RuntimeException stsSigningKeyPairError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsSigningKeyPairError(t);
    }

    /* (non-Javadoc)
     * @see org.picketlink.identity.federation.PicketLinkLogger#stsPublicKeyCertError(java.lang.Throwable)
     */
    public RuntimeException stsPublicKeyCertError(Throwable t) {
        return PicketLinkMessages.MESSAGES.stsPublicKeyCertError(t);
    }
}