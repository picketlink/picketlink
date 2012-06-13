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

import javax.xml.stream.Location;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyConfigurationException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyProcessingException;

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
    IllegalArgumentException nullArgument(String argument);

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
    ProcessingException signatureError(Throwable e);

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
    RuntimeException nullValue(String nullValue);

    /**
     * <p>Creates a {@link RuntimeException} for not implemented methods or features.</p>
     * 
     * @return
     */
    RuntimeException notImplementedYet();

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
}