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
import org.picketlink.common.parsers.ParserNamespaceSupport;
import org.picketlink.common.util.StaxParserUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

/**
 * Parse the WS-Trust RequestSecurityTokenResponse Collection
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 11, 2010
 */
public class WSTRequestSecurityTokenResponseCollectionParser implements ParserNamespaceSupport {

    /**
     * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
     */
    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StaxParserUtil.getNextEvent(xmlEventReader);

        RequestSecurityTokenResponseCollection requestCollection = new RequestSecurityTokenResponseCollection();

        // Peek at the next event
        while (xmlEventReader.hasNext()) {
            StartElement peekedElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
            if (peekedElement == null)
                break;

            String tag = StaxParserUtil.getStartElementName(peekedElement);

            if (WSTrustConstants.RSTR.equalsIgnoreCase(tag)) {
                WSTRequestSecurityTokenResponseParser rstrParser = new WSTRequestSecurityTokenResponseParser();
                RequestSecurityTokenResponse rstr = (RequestSecurityTokenResponse) rstrParser.parse(xmlEventReader);
                requestCollection.addRequestSecurityTokenResponse(rstr);
            }
        }
        return requestCollection;
    }

    /**
     * @see {@link ParserNamespaceSupport#supports(QName)}
     */
    public boolean supports(QName qname) {
        return (qname.getNamespaceURI().equals(WSTrustConstants.BASE_NAMESPACE) && qname.getLocalPart().equals(
                WSTrustConstants.RSTR_COLLECTION));
    }
}