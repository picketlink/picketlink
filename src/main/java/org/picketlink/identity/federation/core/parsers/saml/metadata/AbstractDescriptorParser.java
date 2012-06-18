/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.picketlink.identity.federation.core.parsers.saml.metadata;

import org.picketlink.identity.federation.core.exceptions.ParsingException;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

/**
 * <p>Abstract entity descriptor parser, which provides common parser functionality</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AbstractDescriptorParser {

    protected XMLEventReader filterWhiteSpaceCharacters(XMLEventReader xmlEventReader) throws ParsingException {

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

        try {
            xmlEventReader = xmlInputFactory.createFilteredReader(xmlEventReader, new EventFilter() {
               public boolean accept(XMLEvent xmlEvent) {
                   // We are going to disregard characters that are new line and whitespace
                   if (xmlEvent.isCharacters()) {
                       Characters chars = xmlEvent.asCharacters();
                       String data = chars.getData();
                       data = valid(data) ? data.trim() : null;
                       return valid(data);
                   } else {
                       return xmlEvent.isStartElement() || xmlEvent.isEndElement();
                   }
               }

               private boolean valid(String str) {
                   return str != null && str.length() > 0;
               }
            });
            return xmlEventReader;
        } catch (XMLStreamException e) {
            throw new ParsingException(e);
        }
    }

}
