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
package org.picketlink.identity.federation.core.parsers.saml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.w3c.dom.Element;

/**
 * Parse the SAML Response
 *
 * @author Anil.Saldhana@redhat.com
 * @since July 1, 2011
 */
public class SAMLArtifactResponseParser extends SAMLStatusResponseTypeParser implements ParserNamespaceSupport {
    private final String ARTIFACT_RESPONSE = JBossSAMLConstants.ARTIFACT_RESPONSE.get();

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        // Get the startelement
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, ARTIFACT_RESPONSE);

        ArtifactResponseType response = (ArtifactResponseType) parseBaseAttributes(startElement);

        while (xmlEventReader.hasNext()) {
            // Let us peek at the next start element
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (JBossSAMLConstants.ISSUER.get().equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                NameIDType issuer = new NameIDType();
                issuer.setValue(StaxParserUtil.getElementText(xmlEventReader));
                response.setIssuer(issuer);
            } else if (JBossSAMLConstants.SIGNATURE.get().equals(elementName)) {
                Element sig = StaxParserUtil.getDOMElement(xmlEventReader);
                response.setSignature(sig);
            } else if (JBossSAMLConstants.AUTHN_REQUEST.get().equals(elementName)) {
                SAMLAuthNRequestParser authnParser = new SAMLAuthNRequestParser();
                AuthnRequestType authn = (AuthnRequestType) authnParser.parse(xmlEventReader);
                response.setAny(authn);
            } else if (JBossSAMLConstants.RESPONSE.get().equals(elementName)) {
                SAMLResponseParser authnParser = new SAMLResponseParser();
                ResponseType authn = (ResponseType) authnParser.parse(xmlEventReader);
                response.setAny(authn);
            } else if (JBossSAMLConstants.STATUS.get().equals(elementName)) {
                response.setStatus(parseStatus(xmlEventReader));
            } else
                throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + elementName + "::location="
                        + startElement.getLocation());
        }

        return response;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI())
                && ARTIFACT_RESPONSE.equals(qname.getLocalPart());
    }

    /**
     * Parse the attributes at the response element
     *
     * @param startElement
     * @return
     * @throws ConfigurationException
     */
    protected StatusResponseType parseBaseAttributes(StartElement startElement) throws ParsingException {
        ArtifactResponseType response = new ArtifactResponseType(super.parseBaseAttributes(startElement));
        return response;
    }
}