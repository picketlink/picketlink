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
package org.picketlink.identity.federation.core.parsers.wsp;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.ParserController;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.wspolicy.WSPolicyConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.ws.policy.AppliesTo;

/**
 * <p>
 * Parses the WS-Policy elements that can be part
 * of the WS-T RST
 * </p>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSPolicyParser extends AbstractParser
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

         if (xmlEvent instanceof StartElement)
         {
            StartElement startElement = (StartElement) xmlEvent;

            String elementName = StaxParserUtil.getStartElementName(startElement);
            if (elementName.equalsIgnoreCase(WSPolicyConstants.APPLIES_TO))
            {
               //Get the AppliesTo element
               startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

               AppliesTo appliesTo = new AppliesTo();

               //Now we do not do anything to the applies to element.  We go further
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
         }
         else
         {
            StaxParserUtil.getNextEvent(xmlEventReader);
         }
      }
      throw new RuntimeException(ErrorCodes.FAILED_PARSING);
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();

      return WSTrustConstants.WSP_NS.equals(nsURI);
   }
}