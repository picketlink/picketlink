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
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_END_ELEMENT;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_TAG;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAML11ParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthorizationDecisionStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionsType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.w3c.dom.Element;

/**
 * Parse the saml assertion
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAML11AssertionParser implements ParserNamespaceSupport
{
   private final String ASSERTION = JBossSAMLConstants.ASSERTION.get();

   public SAML11AssertionType fromElement(Element element) throws ConfigurationException, ProcessingException,
         ParsingException
   {
      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(element));
      return (SAML11AssertionType) parse(xmlEventReader);
   }

   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);

      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

      //Special case: Encrypted Assertion 
      StaxParserUtil.validate(startElement, ASSERTION);
      SAML11AssertionType assertion = parseBaseAttributes(startElement);

      Attribute issuerAttribute = startElement.getAttributeByName(new QName(SAML11Constants.ISSUER));
      String issuer = StaxParserUtil.getAttributeValue(issuerAttribute);
      assertion.setIssuer(issuer);

      //Peek at the next event
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;

         if (xmlEvent instanceof EndElement)
         {
            xmlEvent = StaxParserUtil.getNextEvent(xmlEventReader);
            EndElement endElement = (EndElement) xmlEvent;
            String endElementTag = StaxParserUtil.getEndElementName(endElement);
            if (endElementTag.equals(JBossSAMLConstants.ASSERTION.get()))
               break;
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + endElementTag);
         }

         StartElement peekedElement = null;

         if (xmlEvent instanceof StartElement)
         {
            peekedElement = (StartElement) xmlEvent;
         }
         else
         {
            peekedElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         }
         if (peekedElement == null)
            break;

         String tag = StaxParserUtil.getStartElementName(peekedElement);

         if (tag.equals(JBossSAMLConstants.SIGNATURE.get()))
         {
            assertion.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
         }
         else if (JBossSAMLConstants.ISSUER.get().equalsIgnoreCase(tag))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            issuer = StaxParserUtil.getElementText(xmlEventReader);

            assertion.setIssuer(issuer);
         }
         else if (JBossSAMLConstants.SUBJECT.get().equalsIgnoreCase(tag))
         {
            SAML11SubjectParser subjectParser = new SAML11SubjectParser();
            SAML11SubjectType subject = (SAML11SubjectType) subjectParser.parse(xmlEventReader);
            SAML11SubjectStatementType subStat = new SAML11SubjectStatementType();
            subStat.setSubject(subject);
         }
         else if (JBossSAMLConstants.CONDITIONS.get().equalsIgnoreCase(tag))
         {
            startElement = (StartElement) xmlEvent;

            SAML11ConditionsType conditions = SAML11ParserUtil.parseSAML11Conditions(xmlEventReader);
            assertion.setConditions(conditions);
         }
         else if (SAML11Constants.AUTHENTICATION_STATEMENT.equals(tag))
         {
            startElement = (StartElement) xmlEvent;
            SAML11AuthenticationStatementType authStat = SAML11ParserUtil.parseAuthenticationStatement(xmlEventReader);
            assertion.add(authStat);
         }
         else if (SAML11Constants.ATTRIBUTE_STATEMENT.equalsIgnoreCase(tag))
         {
            SAML11AttributeStatementType attributeStatementType = SAML11ParserUtil
                  .parseSAML11AttributeStatement(xmlEventReader);
            assertion.add(attributeStatementType);
         }
         else if (SAML11Constants.AUTHORIZATION_DECISION_STATEMENT.equalsIgnoreCase(tag))
         {
            SAML11AuthorizationDecisionStatementType authzStat = SAML11ParserUtil
                  .parseSAML11AuthorizationDecisionStatement(xmlEventReader);
            assertion.add(authzStat);
         }
         else
            throw new RuntimeException(UNKNOWN_TAG + tag + "::location=" + peekedElement.getLocation());
      }
      return assertion;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get())
            && localPart.equals(JBossSAMLConstants.ASSERTION.get());
   }

   private SAML11AssertionType parseBaseAttributes(StartElement nextElement) throws ParsingException
   {
      Attribute idAttribute = nextElement.getAttributeByName(new QName(SAML11Constants.ASSERTIONID));
      if (idAttribute == null)
         throw new ParsingException(REQD_ATTRIBUTE + "AssertionID");
      String id = StaxParserUtil.getAttributeValue(idAttribute);

      Attribute majVersionAttribute = nextElement.getAttributeByName(new QName(SAML11Constants.MAJOR_VERSION));
      String majVersion = StaxParserUtil.getAttributeValue(majVersionAttribute);
      StringUtil.match("1", majVersion);

      Attribute minVersionAttribute = nextElement.getAttributeByName(new QName(SAML11Constants.MINOR_VERSION));
      String minVersion = StaxParserUtil.getAttributeValue(minVersionAttribute);
      StringUtil.match("1", minVersion);

      Attribute issueInstantAttribute = nextElement
            .getAttributeByName(new QName(JBossSAMLConstants.ISSUE_INSTANT.get()));
      XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(issueInstantAttribute));

      return new SAML11AssertionType(id, issueInstant);
   }
}