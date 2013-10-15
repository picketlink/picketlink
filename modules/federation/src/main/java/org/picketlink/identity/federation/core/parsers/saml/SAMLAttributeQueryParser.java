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

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.parsers.util.SAMLParserUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AttributeQueryType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

/**
 * Parse the {@link org.picketlink.identity.federation.saml.v2.protocol.ArtifactResolveType}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jul 1, 2011
 */
public class SAMLAttributeQueryParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport {

    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        // Get the startelement
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, JBossSAMLConstants.ATTRIBUTE_QUERY.get());

        AttributeQueryType attributeQuery = parseBaseAttributes(startElement);

        while (xmlEventReader.hasNext()) {
            // Let us peek at the next start element
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            super.parseCommonElements(startElement, xmlEventReader, attributeQuery);
            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (JBossSAMLConstants.SUBJECT.get().equals(elementName)) {
                attributeQuery.setSubject(getSubject(xmlEventReader));
            } else if (JBossSAMLConstants.ATTRIBUTE.get().equals(elementName)) {
                attributeQuery.add(SAMLParserUtil.parseAttribute(xmlEventReader));
            } else if (JBossSAMLConstants.ISSUER.get().equals(elementName)) {
                continue;
            } else if (JBossSAMLConstants.SIGNATURE.get().equals(elementName)) {
                continue;
            } else
                throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + elementName + "::location="
                        + startElement.getLocation());
        }
        return attributeQuery;
    }

    public boolean supports(QName qname) {
        return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI());
    }

    /**
     * Parse the attributes at the authnrequesttype element
     *
     * @param startElement
     *
     * @return
     *
     * @throws ParsingException
     */
    private AttributeQueryType parseBaseAttributes(StartElement startElement) throws ParsingException {
        super.parseRequiredAttributes(startElement);
        AttributeQueryType authnRequest = new AttributeQueryType(id, issueInstant);
        // Let us get the attributes
        super.parseBaseAttributes(startElement, authnRequest);

        return authnRequest;
    }
}