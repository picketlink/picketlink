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
package org.picketlink.identity.federation.core.parsers.wsse;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.ws.wss.secext.AttributedString;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;

/**
 * <p>
 * Parses the WS-Security elements that can be part
 * of the WS-T RST
 * </p>
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSSecurityParser extends AbstractParser
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
            if (elementName.equalsIgnoreCase(WSTrustConstants.WSSE.USERNAME_TOKEN))
            {
               startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

               UsernameTokenType userNameToken = new UsernameTokenType();

               //Get the Id attribute
               QName idQName = new QName(WSTrustConstants.WSU_NS, WSTrustConstants.WSSE.ID);
               Attribute idAttribute = startElement.getAttributeByName(idQName);

               if (idAttribute == null)
                  throw new RuntimeException(ErrorCodes.REQD_ATTRIBUTE + "Id");

               userNameToken.setId(StaxParserUtil.getAttributeValue(idAttribute));

               startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

               if (!StaxParserUtil.hasTextAhead(xmlEventReader))
                  throw new ParsingException(ErrorCodes.EXPECTED_TEXT_VALUE + "userName");

               String userName = StaxParserUtil.getElementText(xmlEventReader);

               AttributedString attributedString = new AttributedString();
               attributedString.setValue(userName);

               userNameToken.setUsername(attributedString);

               //Get the end element
               EndElement onBehalfOfEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(onBehalfOfEndElement, WSTrustConstants.WSSE.USERNAME_TOKEN);

               return userNameToken;
            }
            else if (elementName.equals(WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE))
            {
               return parseSecurityTokenReference(xmlEventReader);
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

      return WSTrustConstants.WSSE_NS.equals(nsURI);
   }

   private SecurityTokenReferenceType parseSecurityTokenReference(XMLEventReader xmlEventReader)
         throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE);

      SecurityTokenReferenceType securityTokenRef = new SecurityTokenReferenceType();

      //Get the Token Type attribute
      QName tokenType = new QName(WSTrustConstants.WSSE11_NS, WSTrustConstants.TOKEN_TYPE);
      Attribute tokenTypeAttr = startElement.getAttributeByName(tokenType);
      if (tokenTypeAttr != null)
      {
         tokenType = new QName(WSTrustConstants.WSSE11_NS, WSTrustConstants.TOKEN_TYPE, tokenTypeAttr.getName()
               .getPrefix());
         securityTokenRef.addOtherAttribute(tokenType, StaxParserUtil.getAttributeValue(tokenTypeAttr));
      }

      XMLEvent xmlEvent = null;
      EndElement endElement = null;
      String tag = null;

      while (xmlEventReader.hasNext())
      {
         xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            endElement = (EndElement) xmlEvent;
            tag = StaxParserUtil.getEndElementName(endElement);
            if (tag.equals(WSTrustConstants.WSSE.SECURITY_TOKEN_REFERENCE))
            {
               endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               break;
            }
            else
               throw new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT + tag);
         }

         startElement = (StartElement) xmlEvent;
         tag = StaxParserUtil.getStartElementName(startElement);
         if (tag.equals(WSTrustConstants.WSSE.KEY_IDENTIFIER))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            KeyIdentifierType keyIdentifierType = new KeyIdentifierType();

            Attribute valueTypeAttr = startElement.getAttributeByName(new QName(WSTrustConstants.VALUE_TYPE));
            if (valueTypeAttr != null)
               keyIdentifierType.setValueType(StaxParserUtil.getAttributeValue(valueTypeAttr));
            keyIdentifierType.setValue(StaxParserUtil.getElementText(xmlEventReader));
            securityTokenRef.addAny(keyIdentifierType);
         }
      }

      return securityTokenRef;
   }
}