/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.config.idm.parsers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParser extends AbstractParser {

    public static final String ROOT_ELEMENT = "PicketLinkIDM";

    public Object parse(XMLEventReader xmlEventReader) throws ParsingException {
        StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
        StaxParserUtil.validate(startElement, ROOT_ELEMENT);
        IDMType idmType = new IDMType();

        // parse the inner elements.
        while (xmlEventReader.hasNext()) {
            XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
            if (xmlEvent == null)
                break;
            if (xmlEvent instanceof EndElement) {
                EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
                String endElementName = StaxParserUtil.getEndElementName(endElement);
                if (endElementName.equals(ROOT_ELEMENT))
                    break;
//                else
//                    throw logger.parserUnknownEndElement(endElementName);
            }
            else {
                // TODO: temporary
                StaxParserUtil.getNextEvent(xmlEventReader);
            }
        }
        return idmType;
    }

    public boolean supports(QName qname) {
        return false;
    }


}
