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
package org.picketlink.identity.federation.core.parsers.wst;

import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.AbstractParser;
import org.picketlink.common.util.StaxParserUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * Parser for WS-Trust payload
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 11, 2010
 */
public class WSTrustParser extends AbstractParser {

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

            if (xmlEvent instanceof StartElement) {
                StartElement startElement = (StartElement) xmlEvent;

                String elementName = StaxParserUtil.getStartElementName(startElement);
                if (elementName.equalsIgnoreCase(WSTrustConstants.RST_COLLECTION)) {
                    WSTRequestSecurityTokenCollectionParser wstrcoll = new WSTRequestSecurityTokenCollectionParser();
                    return wstrcoll.parse(xmlEventReader);
                } else if (elementName.equalsIgnoreCase(WSTrustConstants.RST)) {
                    WSTRequestSecurityTokenParser wst = new WSTRequestSecurityTokenParser();
                    return wst.parse(xmlEventReader);
                } else if (elementName.equalsIgnoreCase(WSTrustConstants.RSTR_COLLECTION)) {
                    WSTRequestSecurityTokenResponseCollectionParser wstrcoll = new WSTRequestSecurityTokenResponseCollectionParser();
                    return wstrcoll.parse(xmlEventReader);
                } else if (elementName.equalsIgnoreCase(WSTrustConstants.RSTR)) {
                    WSTRequestSecurityTokenResponseParser wst = new WSTRequestSecurityTokenResponseParser();
                    return wst.parse(xmlEventReader);
                }
                throw logger.parserFailed(elementName);
            } else {
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        throw logger.parserFailed(WSTrustConstants.BASE_NAMESPACE);
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return WSTrustConstants.BASE_NAMESPACE.equals(qname.getNamespaceURI());
    }
}