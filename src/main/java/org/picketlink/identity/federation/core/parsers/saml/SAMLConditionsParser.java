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
package org.picketlink.identity.federation.core.parsers.saml;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;

/**
 * Parse the <conditions> in the saml assertion
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class SAMLConditionsParser implements ParserNamespaceSupport
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      //We are entering this method with <conditions> as the next start element
      //and we have to exit after seeing the </conditions> end tag

      StartElement conditionsElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(conditionsElement, JBossSAMLConstants.CONDITIONS.get());

      ConditionsType conditions = new ConditionsType();

      String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();

      QName notBeforeQName = new QName("", JBossSAMLConstants.NOT_BEFORE.get());
      QName notBeforeQNameWithNS = new QName(assertionNS, JBossSAMLConstants.NOT_BEFORE.get());

      QName notAfterQName = new QName("", JBossSAMLConstants.NOT_ON_OR_AFTER.get());
      QName notAfterQNameWithNS = new QName(assertionNS, JBossSAMLConstants.NOT_ON_OR_AFTER.get());

      Attribute notBeforeAttribute = conditionsElement.getAttributeByName(notBeforeQName);
      if (notBeforeAttribute == null)
         notBeforeAttribute = conditionsElement.getAttributeByName(notBeforeQNameWithNS);

      Attribute notAfterAttribute = conditionsElement.getAttributeByName(notAfterQName);
      if (notAfterAttribute == null)
         notAfterAttribute = conditionsElement.getAttributeByName(notAfterQNameWithNS);

      if (notBeforeAttribute != null)
      {
         String notBeforeValue = StaxParserUtil.getAttributeValue(notBeforeAttribute);
         conditions.setNotBefore(XMLTimeUtil.parse(notBeforeValue));
      }

      if (notAfterAttribute != null)
      {
         String notAfterValue = StaxParserUtil.getAttributeValue(notAfterAttribute);
         conditions.setNotOnOrAfter(XMLTimeUtil.parse(notAfterValue));
      }

      //Let us find additional elements

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);

         if (xmlEvent instanceof EndElement)
         {
            EndElement nextEndElement = (EndElement) xmlEvent;
            if (StaxParserUtil.matches(nextEndElement, JBossSAMLConstants.CONDITIONS.get()))
            {
               nextEndElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               break;
            }
            else
               throw new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT
                     + StaxParserUtil.getEndElementName(nextEndElement));
         }

         String tag = null;

         if (xmlEvent instanceof StartElement)
         {
            StartElement peekedElement = (StartElement) xmlEvent;
            tag = StaxParserUtil.getStartElementName(peekedElement);
         }

         if (JBossSAMLConstants.AUDIENCE_RESTRICTION.get().equals(tag))
         {
            AudienceRestrictionType audienceRestriction = getAudienceRestriction(xmlEventReader);
            conditions.addCondition(audienceRestriction);
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + tag + "::location=" + xmlEvent.getLocation());
      }
      return conditions;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get())
            && localPart.equals(JBossSAMLConstants.CONDITIONS.get());
   }

   /**
    * Parse the <audiencerestriction/> element
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   private AudienceRestrictionType getAudienceRestriction(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement audienceRestElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.matches(audienceRestElement, JBossSAMLConstants.AUDIENCE_RESTRICTION.get());

      AudienceRestrictionType audience = new AudienceRestrictionType();

      while (xmlEventReader.hasNext())
      {
         StartElement audienceElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         if (!StaxParserUtil.matches(audienceElement, JBossSAMLConstants.AUDIENCE.get()))
            break;

         if (!StaxParserUtil.hasTextAhead(xmlEventReader))
            throw new ParsingException(ErrorCodes.EXPECTED_TAG + "audienceValue");

         String audienceValue = StaxParserUtil.getElementText(xmlEventReader);
         audience.addAudience(URI.create(audienceValue));

         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) xmlEvent;
            if (StaxParserUtil.matches(endElement, JBossSAMLConstants.AUDIENCE_RESTRICTION.get()))
            {
               StaxParserUtil.getNextEvent(xmlEventReader); //Just get the end element
               break;
            }
            else
               throw new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT + StaxParserUtil.getEndElementName(endElement));
         }
      }
      return audience;
   }
}