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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.parsers.util.SAML11ParserUtil;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AttributeQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthenticationQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthorizationDecisionQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11RequestType;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * Parse the SAML2 AuthnRequest
 *
 * @author Anil.Saldhana@redhat.com
 * @since June 24, 2011
 */
public class SAML11RequestParser implements ParserNamespaceSupport {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    protected SAML11RequestType parseRequiredAttributes(StartElement startElement) throws ParsingException {
        Attribute idAttr = startElement.getAttributeByName(new QName(SAML11Constants.REQUEST_ID));
        if (idAttr == null)
            throw logger.parserRequiredAttribute(SAML11Constants.REQUEST_ID);

        String id = StaxParserUtil.getAttributeValue(idAttr);

        Attribute issueInstantAttr = startElement.getAttributeByName(new QName(SAML11Constants.ISSUE_INSTANT));
        if (issueInstantAttr == null)
            throw logger.parserRequiredAttribute(SAML11Constants.ISSUE_INSTANT);
        XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(issueInstantAttr));
        return new SAML11RequestType(id, issueInstant);
    }

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        // Get the startelement
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, SAML11Constants.REQUEST);

        SAML11RequestType request = parseRequiredAttributes(startElement);

        while (xmlEventReader.hasNext()) {
            // Let us peek at the next start element
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;

            String elementName = StaxParserUtil.getStartElementName(startElement);

            if (SAML11Constants.ATTRIBUTE_QUERY.equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                SAML11AttributeQueryType query = SAML11ParserUtil.parseSAML11AttributeQuery(xmlEventReader);
                request.setQuery(query);
            } else if (SAML11Constants.AUTHENTICATION_QUERY.equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                SAML11AuthenticationQueryType query = SAML11ParserUtil.parseSAML11AuthenticationQuery(xmlEventReader);
                request.setQuery(query);
            } else if (SAML11Constants.ASSERTION_ARTIFACT.equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                request.addAssertionArtifact(StaxParserUtil.getElementText(xmlEventReader));
            } else if (SAML11Constants.AUTHORIZATION_DECISION_QUERY.equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                SAML11AuthorizationDecisionQueryType query = SAML11ParserUtil
                        .parseSAML11AuthorizationDecisionQueryType(xmlEventReader);
                request.setQuery(query);
            } else if (elementName.equals(JBossSAMLConstants.SIGNATURE.get())) {
                request.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
            } else if (SAML11Constants.ASSERTION_ID_REF.equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                request.addAssertionIDRef(StaxParserUtil.getElementText(xmlEventReader));
            } else
                throw logger.parserUnknownStartElement(elementName, startElement.getLocation());
        }
        return request;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI());
    }
}