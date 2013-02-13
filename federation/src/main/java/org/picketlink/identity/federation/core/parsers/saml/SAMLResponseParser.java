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

import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedAssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.w3c.dom.Element;

/**
 * Parse the SAML Response
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLResponseParser extends SAMLStatusResponseTypeParser implements ParserNamespaceSupport {
    private final String RESPONSE = JBossSAMLConstants.RESPONSE.get();

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        // Get the startelement
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, RESPONSE);

        ResponseType response = (ResponseType) parseBaseAttributes(startElement);

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
            } else if (JBossSAMLConstants.ASSERTION.get().equals(elementName)) {
                SAMLAssertionParser assertionParser = new SAMLAssertionParser();
                response.addAssertion(new RTChoiceType((AssertionType) assertionParser.parse(xmlEventReader)));
            } else if (JBossSAMLConstants.STATUS.get().equals(elementName)) {
                response.setStatus(parseStatus(xmlEventReader));
            } else if (JBossSAMLConstants.ENCRYPTED_ASSERTION.get().equals(elementName)) {
                Element encryptedAssertion = StaxParserUtil.getDOMElement(xmlEventReader);
                response.addAssertion(new RTChoiceType(new EncryptedAssertionType(encryptedAssertion)));
            } else
                throw logger.parserUnknownTag(elementName, startElement.getLocation());
        }

        return response;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI())
                && RESPONSE.equals(qname.getLocalPart());
    }

    /**
     * Parse the attributes at the response element
     *
     * @param startElement
     * @return
     * @throws ConfigurationException
     */
    protected StatusResponseType parseBaseAttributes(StartElement startElement) throws ParsingException {
        ResponseType response = new ResponseType(super.parseBaseAttributes(startElement));
        return response;
    }
}