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
package org.picketlink.identity.federation.core.parsers.saml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
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