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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;


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
    public IllegalArgumentException nullArgument(String argument) {
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
    public RuntimeException nullValue(String nullValue) {
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
}