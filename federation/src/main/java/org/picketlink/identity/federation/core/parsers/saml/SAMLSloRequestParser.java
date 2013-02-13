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

import static org.picketlink.common.constants.JBossSAMLConstants.LOGOUT_REQUEST;
import static org.picketlink.common.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAMLParserUtil;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;

/**
 * Parse the Single Log Out requests
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 3, 2010
 */
public class SAMLSloRequestParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport {
    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        // Get the startelement
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, LOGOUT_REQUEST.get());

        LogoutRequestType logoutRequest = parseBaseAttributes(startElement);

        while (xmlEventReader.hasNext()) {
            // Let us peek at the next start element
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            String elementName = StaxParserUtil.getStartElementName(startElement);

            parseCommonElements(startElement, xmlEventReader, logoutRequest);

            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (startElement == null)
                break;
            elementName = StaxParserUtil.getStartElementName(startElement);

            if (JBossSAMLConstants.SESSION_INDEX.get().equals(elementName)) {
                startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
                logoutRequest.getSessionIndex().add(StaxParserUtil.getElementText(xmlEventReader));
            } else if (JBossSAMLConstants.NAMEID.get().equals(elementName)) {
                NameIDType nameID = SAMLParserUtil.parseNameIDType(xmlEventReader);
                logoutRequest.setNameID(nameID);
            } else if (JBossSAMLConstants.ISSUER.get().equals(elementName)) {
                continue;
            } else if (JBossSAMLConstants.SIGNATURE.get().equals(elementName)) {
                continue;
            } else
                throw logger.parserUnknownTag(elementName, startElement.getLocation());
        }
        return logoutRequest;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return PROTOCOL_NSURI.get().equals(qname.getNamespaceURI()) && LOGOUT_REQUEST.equals(qname.getLocalPart());
    }

    /**
     * Parse the attributes at the log out request element
     *
     * @param startElement
     * @return
     * @throws ParsingException
     */
    private LogoutRequestType parseBaseAttributes(StartElement startElement) throws ParsingException {
        super.parseRequiredAttributes(startElement);
        LogoutRequestType logoutRequest = new LogoutRequestType(id, issueInstant);
        // Let us get the attributes
        super.parseBaseAttributes(startElement, logoutRequest);

        Attribute reason = startElement.getAttributeByName(new QName(JBossSAMLConstants.REASON.get()));
        if (reason != null)
            logoutRequest.setReason(StaxParserUtil.getAttributeValue(reason));

        Attribute notOnOrAfter = startElement.getAttributeByName(new QName(JBossSAMLConstants.NOT_ON_OR_AFTER.get()));
        if (notOnOrAfter != null)
            logoutRequest.setNotOnOrAfter(XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(notOnOrAfter)));
        return logoutRequest;
    }
}