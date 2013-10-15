/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.api.saml.v2.request;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

/**
 * API for SAML2 Request
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 5, 2009
 */
public class SAML2Request {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private SAMLDocumentHolder samlDocumentHolder = null;

    private String nameIDFormat = JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get();

    /**
     * Set the NameIDFormat
     *
     * @param nameIDFormat
     */
    public void setNameIDFormat(String nameIDFormat) {
        this.nameIDFormat = nameIDFormat;
    }

    /**
     * Create an authentication request
     *
     * @param id
     * @param assertionConsumerURL
     * @param destination
     * @param issuerValue
     *
     * @return
     *
     * @throws ConfigurationException
     */
    public AuthnRequestType createAuthnRequestType(String id, String assertionConsumerURL, String destination,
                                                   String issuerValue) throws ConfigurationException {
        XMLGregorianCalendar issueInstant = XMLTimeUtil.getIssueInstant();

        AuthnRequestType authnRequest = new AuthnRequestType(id, issueInstant);
        authnRequest.setAssertionConsumerServiceURL(URI.create(assertionConsumerURL));
        authnRequest.setProtocolBinding(URI.create(JBossSAMLConstants.HTTP_POST_BINDING.get()));
        if (destination != null) {
            authnRequest.setDestination(URI.create(destination));
        }

        // Create an issuer
        NameIDType issuer = new NameIDType();
        issuer.setValue(issuerValue);

        authnRequest.setIssuer(issuer);

        // Create a default NameIDPolicy
        NameIDPolicyType nameIDPolicy = new NameIDPolicyType();
        nameIDPolicy.setAllowCreate(Boolean.TRUE);
        nameIDPolicy.setFormat(URI.create(this.nameIDFormat));

        authnRequest.setNameIDPolicy(nameIDPolicy);

        return authnRequest;
    }

    /**
     * Get AuthnRequestType from a file
     *
     * @param fileName file with the serialized AuthnRequestType
     *
     * @return AuthnRequestType
     *
     * @throws ParsingException
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws IllegalArgumentException if the input fileName is null IllegalStateException if the InputStream from the
     * fileName
     * is null
     */
    public AuthnRequestType getAuthnRequestType(String fileName) throws ConfigurationException, ProcessingException,
            ParsingException {
        if (fileName == null)
            throw logger.nullArgumentError("fileName");
        URL resourceURL = SecurityActions.loadResource(getClass(), fileName);
        if (resourceURL == null)
            throw logger.resourceNotFound(fileName);

        InputStream is = null;
        try {
            is = resourceURL.openStream();
        } catch (IOException e) {
            throw logger.processingError(e);
        }
        return getAuthnRequestType(is);
    }

    /**
     * Get the Underlying SAML2Object from the input stream
     *
     * @param is
     *
     * @return
     *
     * @throws IOException
     * @throws ParsingException
     */
    public SAML2Object getSAML2ObjectFromStream(InputStream is) throws ConfigurationException, ParsingException,
            ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlDocument = DocumentUtil.getDocument(is);

        SAMLParser samlParser = new SAMLParser();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);
        SAML2Object requestType = (SAML2Object) samlParser.parse(DocumentUtil.getNodeAsStream(samlDocument));

        samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
        return requestType;
    }

    /**
     * Get a Request Type from Input Stream
     *
     * @param is
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws
     * @throws IllegalArgumentException inputstream is null
     */
    public RequestAbstractType getRequestType(InputStream is) throws ParsingException, ConfigurationException,
            ProcessingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlDocument = DocumentUtil.getDocument(is);

        SAMLParser samlParser = new SAMLParser();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);
        RequestAbstractType requestType = (RequestAbstractType) samlParser.parse(DocumentUtil.getNodeAsStream(samlDocument));

        samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
        return requestType;
    }

    /**
     * Get the AuthnRequestType from an input stream
     *
     * @param is Inputstream containing the AuthnRequest
     *
     * @return
     *
     * @throws ParsingException
     * @throws ProcessingException
     * @throws ConfigurationException
     * @throws IllegalArgumentException inputstream is null
     */
    public AuthnRequestType getAuthnRequestType(InputStream is) throws ConfigurationException, ProcessingException,
            ParsingException {
        if (is == null)
            throw logger.nullArgumentError("InputStream");

        Document samlDocument = DocumentUtil.getDocument(is);

        SAMLParser samlParser = new SAMLParser();
        JAXPValidationUtil.checkSchemaValidation(samlDocument);

        AuthnRequestType requestType = (AuthnRequestType) samlParser.parse(DocumentUtil.getNodeAsStream(samlDocument));
        samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
        return requestType;
    }

    /**
     * Get the parsed {@code SAMLDocumentHolder}
     *
     * @return
     */
    public SAMLDocumentHolder getSamlDocumentHolder() {
        return samlDocumentHolder;
    }

    /**
     * Create a Logout Request
     *
     * @param issuer
     *
     * @return
     *
     * @throws ConfigurationException
     */
    public LogoutRequestType createLogoutRequest(String issuer) throws ConfigurationException {
        LogoutRequestType lrt = new LogoutRequestType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());

        // Create an issuer
        NameIDType issuerNameID = new NameIDType();
        issuerNameID.setValue(issuer);

        lrt.setIssuer(issuerNameID);

        return lrt;
    }

    /**
     * Return the DOM object
     *
     * @param rat
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public Document convert(RequestAbstractType rat) throws ProcessingException, ConfigurationException, ParsingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(bos));
        if (rat instanceof AuthnRequestType) {
            writer.write((AuthnRequestType) rat);
        } else if (rat instanceof LogoutRequestType) {
            writer.write((LogoutRequestType) rat);
        }

        return DocumentUtil.getDocument(new String(bos.toByteArray()));
    }

    /**
     * Convert a SAML2 Response into a Document
     *
     * @param responseType
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public Document convert(ResponseType responseType) throws ProcessingException, ParsingException, ConfigurationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(responseType);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        return DocumentUtil.getDocument(bis);
    }

    /**
     * Marshall the AuthnRequestType to an output stream
     *
     * @param requestType
     * @param os
     *
     * @throws SAXException
     */
    public void marshall(RequestAbstractType requestType, OutputStream os) throws ProcessingException {
        SAMLRequestWriter samlRequestWriter = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(os));
        if (requestType instanceof AuthnRequestType) {
            samlRequestWriter.write((AuthnRequestType) requestType);
        } else if (requestType instanceof LogoutRequestType) {
            samlRequestWriter.write((LogoutRequestType) requestType);
        } else
            throw logger.unsupportedType(requestType.getClass().getName());
    }

    /**
     * Marshall the AuthnRequestType to a writer
     *
     * @param requestType
     * @param writer
     *
     * @throws SAXException
     */
    public void marshall(RequestAbstractType requestType, Writer writer) throws ProcessingException {
        SAMLRequestWriter samlRequestWriter = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(writer));
        if (requestType instanceof AuthnRequestType) {
            samlRequestWriter.write((AuthnRequestType) requestType);
        } else if (requestType instanceof LogoutRequestType) {
            samlRequestWriter.write((LogoutRequestType) requestType);
        } else
            throw logger.unsupportedType(requestType.getClass().getName());
    }
}