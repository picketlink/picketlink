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
package org.picketlink.identity.federation.core.parsers.wsp;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.parsers.ParserController;
import org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants;
import org.picketlink.identity.federation.ws.policy.AppliesTo;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>
 * Parses the WS-Policy elements that can be part of the WS-T RST
 * </p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSPolicyParser extends AbstractParser {

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;

                String elementName = StaxParserUtil.getStartElementName(startElement);
                if (elementName.equalsIgnoreCase(WSPolicyConstants.APPLIES_TO)) {
                    // Get the AppliesTo element
                    startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

                    AppliesTo appliesTo = new AppliesTo();

                    // Now we do not do anything to the applies to element. We go further
                    startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);

                    QName qname = startElement.getName();
                    ParserNamespaceSupport parser = ParserController.get(qname);
                    if (parser == null)
                        throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + qname);

                    Object parsedObject = parser.parse(xmlEventReader);
                    appliesTo.addAny(parsedObject);

                    EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
                    StaxParserUtil.validate(endElement, WSPolicyConstants.APPLIES_TO);
                    return appliesTo;
                }
            } else {
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        throw logger.parserFailed(WSTrustConstants.WSP_NS);
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        String nsURI = qname.getNamespaceURI();

        return WSTrustConstants.WSP_NS.equals(nsURI) || WSTrustConstants.WSP_15_NS.equals(nsURI);
    }
}