/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.saml.v2.writers;

import org.jboss.security.xacml.core.model.context.ObjectFactory;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.common.util.StringUtil;
import org.picketlink.identity.federation.core.util.JAXBUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType;
import org.picketlink.identity.federation.saml.v2.protocol.AttributeQueryType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnContextComparisonType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestedAuthnContextType;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import static org.picketlink.common.constants.JBossSAMLConstants.*;
import static org.picketlink.common.constants.JBossSAMLURIConstants.*;

/**
 * Writes a SAML2 Request Type to Stream
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLRequestWriter extends BaseWriter {
    public SAMLRequestWriter(XMLStreamWriter writer) {
        super(writer);
    }

    /**
     * Write a {@code AuthnRequestType } to stream
     *
     * @param request
     * @param out
     * @throws ProcessingException
     */
    public void write(AuthnRequestType request) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.AUTHN_REQUEST.get(), PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
        StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

        // Attributes
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), request.getID());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), request.getVersion());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), request.getIssueInstant().toString());

        URI destination = request.getDestination();
        if (destination != null)
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination.toASCIIString());

        String consent = request.getConsent();
        if (StringUtil.isNotNull(consent))
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

        URI assertionURL = request.getAssertionConsumerServiceURL();
        if (assertionURL != null)
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE_URL.get(),
                    assertionURL.toASCIIString());

        Boolean forceAuthn = request.isForceAuthn();
        if (forceAuthn != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.FORCE_AUTHN.get(), forceAuthn.toString());
        }

        Boolean isPassive = request.isIsPassive();
        if (isPassive != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.IS_PASSIVE.get(), isPassive.toString());
        }

        URI protocolBinding = request.getProtocolBinding();
        if (protocolBinding != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.PROTOCOL_BINDING.get(), protocolBinding.toString());
        }

        Integer assertionIndex = request.getAssertionConsumerServiceIndex();
        if (assertionIndex != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE_INDEX.get(),
                    assertionIndex.toString());
        }

        Integer attrIndex = request.getAttributeConsumingServiceIndex();
        if (attrIndex != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.ATTRIBUTE_CONSUMING_SERVICE_INDEX.get(), attrIndex.toString());
        }
        String providerName = request.getProviderName();
        if (StringUtil.isNotNull(providerName)) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.PROVIDER_NAME.get(), providerName);
        }

        NameIDType issuer = request.getIssuer();
        if (issuer != null) {
            write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
        }

        Element sig = request.getSignature();
        if (sig != null) {
            StaxUtil.writeDOMElement(writer, sig);
        }

        NameIDPolicyType nameIDPolicy = request.getNameIDPolicy();
        if (nameIDPolicy != null) {
            write(nameIDPolicy);
        }

        RequestedAuthnContextType requestedAuthnContext = request.getRequestedAuthnContext();
        if (requestedAuthnContext != null) {
            write(requestedAuthnContext);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * Write a {@code LogoutRequestType} to stream
     *
     * @param logOutRequest
     * @param out
     * @throws ProcessingException
     */
    public void write(LogoutRequestType logOutRequest) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.LOGOUT_REQUEST.get(), PROTOCOL_NSURI.get());

        StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
        StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

        // Attributes
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), logOutRequest.getID());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), logOutRequest.getVersion());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), logOutRequest.getIssueInstant().toString());

        URI destination = logOutRequest.getDestination();
        if (destination != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination.toASCIIString());
        }

        String consent = logOutRequest.getConsent();
        if (StringUtil.isNotNull(consent))
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

        NameIDType issuer = logOutRequest.getIssuer();
        write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));

        Element signature = logOutRequest.getSignature();
        if (signature != null) {
            StaxUtil.writeDOMElement(writer, signature);
        }

        NameIDType nameID = logOutRequest.getNameID();
        if (nameID != null) {
            write(nameID, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.NAMEID.get(), ASSERTION_PREFIX));
        }
        
        List<String> sessionIndexes = logOutRequest.getSessionIndex();
        
        for (String sessionIndex : sessionIndexes) {
            StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SESSION_INDEX.get(), PROTOCOL_NSURI.get());

            StaxUtil.writeCharacters(writer, sessionIndex);

            StaxUtil.writeEndElement(writer);
            StaxUtil.flush(writer);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * Write a {@code NameIDPolicyType} to stream
     *
     * @param nameIDPolicy
     * @throws ProcessingException
     */
    public void write(NameIDPolicyType nameIDPolicy) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.NAMEID_POLICY.get(), PROTOCOL_NSURI.get());

        URI format = nameIDPolicy.getFormat();
        if (format != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.FORMAT.get(), format.toASCIIString());
        }

        String spNameQualifier = nameIDPolicy.getSPNameQualifier();
        if (StringUtil.isNotNull(spNameQualifier)) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.SP_NAME_QUALIFIER.get(), spNameQualifier);
        }

        Boolean allowCreate = nameIDPolicy.isAllowCreate();
        if (allowCreate != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.ALLOW_CREATE.get(), allowCreate.toString());
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    /**
     * Write a {@code RequestedAuthnContextType} to stream
     *
     * @param requestedAuthnContextType
     * @throws ProcessingException
     */
    public void write(RequestedAuthnContextType requestedAuthnContextType) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.REQUESTED_AUTHN_CONTEXT.get(), PROTOCOL_NSURI.get());

        AuthnContextComparisonType comparison = requestedAuthnContextType.getComparison();

        if (comparison != null) {
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.COMPARISON.get(), comparison.value());
        }

        List<String> authnContextClassRef = requestedAuthnContextType.getAuthnContextClassRef();

        if (authnContextClassRef != null && !authnContextClassRef.isEmpty()) {
            for (String classRef: authnContextClassRef) {
                StaxUtil.writeStartElement(writer, ASSERTION_PREFIX, JBossSAMLConstants.AUTHN_CONTEXT_CLASS_REF.get(), ASSERTION_NSURI.get());
                StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());
                StaxUtil.writeCharacters(writer, classRef);
                StaxUtil.writeEndElement(writer);
            }
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    public void write(ArtifactResolveType request) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.ARTIFACT_RESOLVE.get(), PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());
        StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

        // Attributes
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), request.getID());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), request.getVersion());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), request.getIssueInstant().toString());

        URI destination = request.getDestination();
        if (destination != null)
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination.toASCIIString());

        String consent = request.getConsent();
        if (StringUtil.isNotNull(consent))
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

        NameIDType issuer = request.getIssuer();
        if (issuer != null) {
            write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
        }
        Element sig = request.getSignature();
        if (sig != null) {
            StaxUtil.writeDOMElement(writer, sig);
        }
        String artifact = request.getArtifact();
        if (StringUtil.isNotNull(artifact)) {
            StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.ARTIFACT.get(), PROTOCOL_NSURI.get());
            StaxUtil.writeCharacters(writer, artifact);
            StaxUtil.writeEndElement(writer);
        }
        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    public void write(AttributeQueryType request) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.ATTRIBUTE_QUERY.get(), PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());
        StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

        // Attributes
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), request.getID());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), request.getVersion());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), request.getIssueInstant().toString());

        URI destination = request.getDestination();
        if (destination != null)
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination.toASCIIString());

        String consent = request.getConsent();
        if (StringUtil.isNotNull(consent))
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

        NameIDType issuer = request.getIssuer();
        if (issuer != null) {
            write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
        }
        Element sig = request.getSignature();
        if (sig != null) {
            StaxUtil.writeDOMElement(writer, sig);
        }
        SubjectType subject = request.getSubject();
        if (subject != null) {
            write(subject);
        }
        List<AttributeType> attributes = request.getAttribute();
        for (AttributeType attr : attributes) {
            write(attr);
        }
        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }

    public void write(XACMLAuthzDecisionQueryType xacmlQuery) throws ProcessingException {
        StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.REQUEST_ABSTRACT.get(), PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
        StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());

        StaxUtil.writeNameSpace(writer, XACML_SAML_PROTO_PREFIX, JBossSAMLURIConstants.XACML_SAML_PROTO_NSURI.get());
        StaxUtil.writeDefaultNameSpace(writer, JBossSAMLURIConstants.XACML_NSURI.get());

        // Attributes
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), xacmlQuery.getID());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), xacmlQuery.getVersion());
        StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), xacmlQuery.getIssueInstant().toString());

        StaxUtil.writeAttribute(writer, new QName(JBossSAMLURIConstants.XACML_SAML_PROTO_NSURI.get(),
                JBossSAMLConstants.INPUT_CONTEXT_ONLY.get(), XACML_SAML_PROTO_PREFIX), "true");

        StaxUtil.writeAttribute(writer, new QName(JBossSAMLURIConstants.XACML_SAML_PROTO_NSURI.get(),
                JBossSAMLConstants.RETURN_CONTEXT.get(), XACML_SAML_PROTO_PREFIX), "true");

        StaxUtil.writeNameSpace(writer, JBossSAMLURIConstants.XSI_PREFIX.get(), JBossSAMLURIConstants.XSI_NSURI.get());
        StaxUtil.writeNameSpace(writer, "xs", JBossSAMLURIConstants.XMLSCHEMA_NSURI.get());

        StaxUtil.writeAttribute(writer, JBossSAMLURIConstants.XSI_NSURI.get(), "type",
                "xacml-samlp:XACMLAuthzDecisionQueryType");

        URI destination = xacmlQuery.getDestination();
        if (destination != null)
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination.toASCIIString());

        String consent = xacmlQuery.getConsent();
        if (StringUtil.isNotNull(consent))
            StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

        NameIDType issuer = xacmlQuery.getIssuer();
        if (issuer != null) {
            write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
        }

        RequestType xacmlRequest = xacmlQuery.getRequest();

        ObjectFactory of = new ObjectFactory();

        StringWriter sw = new StringWriter();
        try {
            Marshaller m = JAXBUtil.getMarshaller(RequestType.class.getPackage().getName());
            m.marshal(of.createRequest(xacmlRequest), sw);
        } catch (JAXBException e) {
            throw logger.processingError(e);
        }

        try {
            Document xacmlDoc = DocumentUtil.getDocument(sw.toString());
            StaxUtil.writeDOMNode(writer, xacmlDoc.getDocumentElement());
        } catch (ConfigurationException e) {
            throw logger.processingError(e);
        } catch (ParsingException e) {
            throw logger.processingError(e);
        }

        StaxUtil.writeEndElement(writer);
        StaxUtil.flush(writer);
    }
}