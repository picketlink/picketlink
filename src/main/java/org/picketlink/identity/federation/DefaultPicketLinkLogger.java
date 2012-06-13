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

import static org.picketlink.identity.federation.core.ErrorCodes.REQD_ATTRIBUTE;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_START_ELEMENT;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_TAG;

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
    public IllegalArgumentException nullArgument(String argument) {
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
    public RuntimeException notImplementedYet() {
        return new RuntimeException(ErrorCodes.NOT_IMPLEMENTED_YET);
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
}