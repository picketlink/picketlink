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

import static org.picketlink.identity.federation.core.ErrorCodes.REQD_ATTRIBUTE;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_START_ELEMENT;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAML11ParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AttributeQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthenticationQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11AuthorizationDecisionQueryType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11RequestType;

/**
 * Parse the SAML2 AuthnRequest
 * @author Anil.Saldhana@redhat.com
 * @since June 24, 2011
 */
public class SAML11RequestParser implements ParserNamespaceSupport
{

   protected SAML11RequestType parseRequiredAttributes(StartElement startElement) throws ParsingException
   {
      Attribute idAttr = startElement.getAttributeByName(new QName(SAML11Constants.REQUEST_ID));
      if (idAttr == null)
         throw new RuntimeException(REQD_ATTRIBUTE + SAML11Constants.REQUEST_ID);

      String id = StaxParserUtil.getAttributeValue(idAttr);

      Attribute issueInstantAttr = startElement.getAttributeByName(new QName(SAML11Constants.ISSUE_INSTANT));
      if (issueInstantAttr == null)
         throw new RuntimeException(REQD_ATTRIBUTE + SAML11Constants.ISSUE_INSTANT);
      XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(issueInstantAttr));
      return new SAML11RequestType(id, issueInstant);
   }

   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      //Get the startelement
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, SAML11Constants.REQUEST);

      SAML11RequestType request = parseRequiredAttributes(startElement);

      while (xmlEventReader.hasNext())
      {
         //Let us peek at the next start element
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (startElement == null)
            break;

         String elementName = StaxParserUtil.getStartElementName(startElement);

         if (SAML11Constants.ATTRIBUTE_QUERY.equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            SAML11AttributeQueryType query = SAML11ParserUtil.parseSAML11AttributeQuery(xmlEventReader);
            request.setQuery(query);
         }
         else if (SAML11Constants.AUTHENTICATION_QUERY.equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            SAML11AuthenticationQueryType query = SAML11ParserUtil.parseSAML11AuthenticationQuery(xmlEventReader);
            request.setQuery(query);
         }
         else if (SAML11Constants.ASSERTION_ARTIFACT.equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            request.addAssertionArtifact(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (SAML11Constants.AUTHORIZATION_DECISION_QUERY.equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            SAML11AuthorizationDecisionQueryType query = SAML11ParserUtil
                  .parseSAML11AuthorizationDecisionQueryType(xmlEventReader);
            request.setQuery(query);
         }
         else if (elementName.equals(JBossSAMLConstants.SIGNATURE.get()))
         {
            request.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
         }
         else if (SAML11Constants.ASSERTION_ID_REF.equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            request.addAssertionIDRef(StaxParserUtil.getElementText(xmlEventReader));
         }
         else
            throw new RuntimeException(UNKNOWN_START_ELEMENT + elementName + "::location=" + startElement.getLocation());
      }
      return request;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI());
   }
}