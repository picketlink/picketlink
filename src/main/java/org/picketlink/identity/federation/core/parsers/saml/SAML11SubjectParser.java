/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.parsers.saml;

import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_END_ELEMENT;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_TAG;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAML11ParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType.SAML11SubjectTypeChoice;

/**
 * Parse the saml subject
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAML11SubjectParser implements ParserNamespaceSupport
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StaxParserUtil.getNextEvent(xmlEventReader);

      SAML11SubjectType subject = new SAML11SubjectType();

      // Peek at the next event
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) xmlEvent;
            if (StaxParserUtil.matches(endElement, JBossSAMLConstants.SUBJECT.get()))
            {
               endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               break;
            }
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + StaxParserUtil.getEndElementName(endElement));
         }

         StartElement peekedElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (peekedElement == null)
            break;

         String tag = StaxParserUtil.getStartElementName(peekedElement);

         if (SAML11Constants.NAME_IDENTIFIER.equalsIgnoreCase(tag))
         {
            peekedElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            String val = StaxParserUtil.getElementText(xmlEventReader);
            SAML11NameIdentifierType nameID = new SAML11NameIdentifierType(val);
            Attribute formatAtt = peekedElement.getAttributeByName(new QName(SAML11Constants.FORMAT));
            if (formatAtt != null)
            {
               nameID.setFormat(URI.create(StaxParserUtil.getAttributeValue(formatAtt)));
            }

            Attribute nameQAtt = peekedElement.getAttributeByName(new QName(SAML11Constants.NAME_QUALIFIER));
            if (nameQAtt != null)
            {
               nameID.setNameQualifier(StaxParserUtil.getAttributeValue(nameQAtt));
            }

            SAML11SubjectTypeChoice subChoice = new SAML11SubjectTypeChoice(nameID);
            subject.setChoice(subChoice);
         }
         else if (JBossSAMLConstants.SUBJECT_CONFIRMATION.get().equalsIgnoreCase(tag))
         {
            SAML11SubjectConfirmationType subjectConfirmationType = SAML11ParserUtil
                  .parseSAML11SubjectConfirmation(xmlEventReader);
            subject.setSubjectConfirmation(subjectConfirmationType);
         }
         else
            throw new RuntimeException(UNKNOWN_TAG + tag + "::location=" + peekedElement.getLocation());
      }
      return subject;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get())
            && localPart.equals(JBossSAMLConstants.SUBJECT.get());
   }

}