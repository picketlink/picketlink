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
package org.picketlink.identity.federation.core.parsers.wsse;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.ws.wss.secext.AttributedString;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.ReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>
 * Parses the WS-Security elements that can be part of the WS-T RST
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSSecurityParser extends AbstractParser {

    /**
     * @see {@link org.picketlink.common.parsers.ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;

                String elementName = StaxParserUtil.getStartElementName(startElement);
                if (elementName.equalsIgnoreCase(WSTrustConstants.WSSE.USERNAME_TOKEN)) {
                    startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

                    UsernameTokenType userNameToken = new UsernameTokenType();

                    // Get the Id attribute
                    QName idQName = new QName(WSTrustConstants.WSU_NS, WSTrustConstants.WSSE.ID);
                    Attribute idAttribute = startElement.getAttributeByName(idQName);

                    if (idAttribute == null)
                        throw logger.parserRequiredAttribute("Id");

                    userNameToken.setId(StaxParserUtil.getAttributeValue(idAttribute));

                    startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

                    if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                        throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "userName");

                    String userName = StaxParserUtil.getElementText(xmlEventReader);

                    AttributedString attributedString = new AttributedString();
                    attributedString.setValue(userName);

                    userNameToken.setUsername(attributedString);

                    // Get the end element
                    EndElement onBehalfOfEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                    StaxParserUtil.validate(onBehalfOfEndElement, WSTrustConstants.WSSE.USERNAME_TOKEN);

                    return userNameToken;
                } else if (elementName.equals(WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE)) {
                    return parseSecurityTokenReference(xmlEventReader);
                }
            } else {
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        throw logger.parserFailed(WSTrustConstants.WSSE_NS);
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        String nsURI = qname.getNamespaceURI();

        return WSTrustConstants.WSSE_NS.equals(nsURI);
    }

    private SecurityTokenReferenceType parseSecurityTokenReference(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE);

        SecurityTokenReferenceType securityTokenRef = new SecurityTokenReferenceType();

        // Get the Token Type attribute
        QName tokenType = new QName(WSTrustConstants.WSSE11_NS, WSTrustConstants.TOKEN_TYPE);
        Attribute tokenTypeAttr = startElement.getAttributeByName(tokenType);
        if (tokenTypeAttr != null) {
            tokenType = new QName(WSTrustConstants.WSSE11_NS, WSTrustConstants.TOKEN_TYPE, tokenTypeAttr.getName().getPrefix());
            securityTokenRef.addOtherAttribute(tokenType, StaxParserUtil.getAttributeValue(tokenTypeAttr));
        }

        XMLEvent xmlEvent = null;
        EndElement endElement = null;
        String tag = null;

        while (xmlEventReader.hasNext()) {
            xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent instanceof EndElement) {
                endElement = (EndElement) xmlEvent;
                tag = StaxParserUtil.getEndElementName(endElement);
                if (tag.equals(WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE)) {
                    endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                    break;
                } else if (tag.equals(WSTrustConstants.WSSE.REFERENCE)) {
                    endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                    continue;
                } else
                    throw logger.parserUnknownEndElement(tag);
            }

            startElement = (StartElement) xmlEvent;
            tag = StaxParserUtil.getStartElementName(startElement);
            if (tag.equals(WSTrustConstants.WSSE.KEY_IDENTIFIER)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                KeyIdentifierType keyIdentifierType = new KeyIdentifierType();

                Attribute valueTypeAttr = startElement.getAttributeByName(new QName(WSTrustConstants.VALUE_TYPE));
                if (valueTypeAttr != null)
                    keyIdentifierType.setValueType(StaxParserUtil.getAttributeValue(valueTypeAttr));
                keyIdentifierType.setValue(StaxParserUtil.getElementText(xmlEventReader));
                securityTokenRef.addAny(keyIdentifierType);
            } else if (tag.equals(WSTrustConstants.WSSE.REFERENCE)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                ReferenceType referenceType = new ReferenceType();

                Attribute valueTypeAttr = startElement.getAttributeByName(new QName(WSTrustConstants.VALUE_TYPE));
                if (valueTypeAttr != null) {
                    referenceType.setValueType(StaxParserUtil.getAttributeValue(valueTypeAttr));
                }

                Attribute uriAttr = startElement.getAttributeByName(new QName(WSTrustConstants.WSSE.URI));
                if (uriAttr != null) {
                    referenceType.setURI(StaxParserUtil.getAttributeValue(uriAttr));
                }
                securityTokenRef.addAny(referenceType);
            }
        }

        return securityTokenRef;
    }
}