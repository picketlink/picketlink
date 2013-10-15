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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

/**
 * Stax parser for the wst:RenewTarget element
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 13, 2010
 */
public class WSTRenewTargetParser implements ParserNamespaceSupport {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        RenewTargetType renewTargetType = new RenewTargetType();

        StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
        // null start element indicates that the original token hasn't been specified.
        if (startElement == null) {
            throw logger.parserUnableParsingNullToken();
        }

        // this is an unknown type - parse using the transformer.
        try {
            renewTargetType.add(StaxParserUtil.getDOMElement(xmlEventReader));
        } catch (Exception e) {
            throw logger.parserError(e);
        }
        return renewTargetType;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        String nsURI = qname.getNamespaceURI();
        String localPart = qname.getLocalPart();

        return WSTrustConstants.BASE_NAMESPACE.equals(nsURI) && WSTrustConstants.RENEW_TARGET.equals(localPart);
    }
}