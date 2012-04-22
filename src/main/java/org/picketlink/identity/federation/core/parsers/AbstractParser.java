/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.parsers;

import static org.picketlink.identity.federation.core.ErrorCodes.NULL_INPUT_STREAM;

import java.io.InputStream;

import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;

/**
 * Base class for parsers
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public abstract class AbstractParser implements ParserNamespaceSupport
{
   /**
    * Parse an InputStream for payload
    * @param configStream
    * @return
    * @throws {@link IllegalArgumentException}
    * @throws {@link IllegalArgumentException} when the configStream is null
    */
   public Object parse(InputStream configStream) throws ParsingException
   {
      if (configStream == null)
         throw new IllegalArgumentException(NULL_INPUT_STREAM);

      XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(configStream);

      try
      {
         xmlEventReader = xmlInputFactory.createFilteredReader(xmlEventReader, new EventFilter()
         {
            public boolean accept(XMLEvent xmlEvent)
            {
               //We are going to disregard characters that are new line and whitespace
               if (xmlEvent.isCharacters())
               {
                  Characters chars = xmlEvent.asCharacters();
                  String data = chars.getData();
                  data = valid(data) ? data.trim() : null;
                  return valid(data);
               }
               else
               {
                  return xmlEvent.isStartElement() || xmlEvent.isEndElement();
               }
            }

            private boolean valid(String str)
            {
               return str != null && str.length() > 0;
            }
         });
      }
      catch (XMLStreamException e)
      {
         throw new ParsingException(e);
      }

      return parse(xmlEventReader);
   }

}