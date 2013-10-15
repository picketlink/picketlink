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
package org.picketlink.identity.federation.core.parsers.wsa;

import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.ws.addressing.AttributedURIType;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>
 * Able to parse the WS-Addressing pieces in WS-T RST.
 * <p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSAddressingParser extends AbstractParser {

    public static final String ENDPOINT_REFERENCE = "EndpointReference";

    public static final String ADDRESS = "Address";

    /**
     * @see {@link org.picketlink.common.parsers.ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;

                String elementName = StaxParserUtil.getStartElementName(startElement);
                if (elementName.equalsIgnoreCase(ENDPOINT_REFERENCE)) {
                    startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                    StaxParserUtil.validate(startElement, ENDPOINT_REFERENCE);

                    // Lets get the wsa:Address
                    startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                    StaxParserUtil.validate(startElement, ADDRESS);

                    if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                        throw logger.parserExpectedTextValue("endpointURI");

                    String endpointURI = StaxParserUtil.getElementText(xmlEventReader);

                    AttributedURIType attributedURI = new AttributedURIType();
                    attributedURI.setValue(endpointURI);
                    EndpointReferenceType reference = new EndpointReferenceType();
                    reference.setAddress(attributedURI);

                    // Lets get the end element
                    xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
                    EndElement endElement = (EndElement) xmlEvent;
                    StaxParserUtil.validate(endElement, ENDPOINT_REFERENCE);

                    return reference;
                }
            } else {
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        throw logger.parserFailed(WSTrustConstants.WSA_NS);
    }

    /**
     * @see {@link org.picketlink.common.parsers.ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return WSTrustConstants.WSA_NS.equals(qname.getNamespaceURI());
    }
}