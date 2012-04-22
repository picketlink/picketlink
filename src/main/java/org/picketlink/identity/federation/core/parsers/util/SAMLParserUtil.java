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
package org.picketlink.identity.federation.core.parsers.util;

import static org.picketlink.identity.federation.core.ErrorCodes.REQD_ATTRIBUTE;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_END_ELEMENT;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_TAG;
import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_XSI;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextClassRefType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextDeclRefType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnContextType.AuthnContextTypeSequence;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectLocalityType;

/**
 * Utility methods for SAML Parser
 * @author Anil.Saldhana@redhat.com
 * @since Nov 4, 2010
 */
public class SAMLParserUtil
{
   /**
    * Parse an {@code AttributeStatementType}
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static AttributeStatementType parseAttributeStatement(XMLEventReader xmlEventReader) throws ParsingException
   {
      AttributeStatementType attributeStatementType = new AttributeStatementType();

      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      String ATTRIBSTATEMT = JBossSAMLConstants.ATTRIBUTE_STATEMENT.get();
      StaxParserUtil.validate(startElement, ATTRIBSTATEMT);

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.ATTRIBUTE_STATEMENT.get());
            break;
         }
         //Get the next start element
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         String tag = startElement.getName().getLocalPart();
         if (JBossSAMLConstants.ATTRIBUTE.get().equals(tag))
         {
            AttributeType attribute = parseAttribute(xmlEventReader);
            attributeStatementType.addAttribute(new ASTChoiceType(attribute));
         }
         else
            throw new RuntimeException(UNKNOWN_TAG + tag + "::Location=" + startElement.getLocation());
      }
      return attributeStatementType;
   }

   /**
    * Parse an {@code AttributeType}
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static AttributeType parseAttribute(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.ATTRIBUTE.get());
      AttributeType attributeType = null;

      Attribute name = startElement.getAttributeByName(new QName(JBossSAMLConstants.NAME.get()));
      if (name == null)
         throw new RuntimeException(REQD_ATTRIBUTE + "Name");
      attributeType = new AttributeType(StaxParserUtil.getAttributeValue(name));

      parseAttributeType(xmlEventReader, startElement, JBossSAMLConstants.ATTRIBUTE.get(), attributeType);

      return attributeType;
   }

   /**
    * Parse an {@code AttributeType}
    * @param xmlEventReader 
    * @throws ParsingException
    */
   public static void parseAttributeType(XMLEventReader xmlEventReader, StartElement startElement, String rootTag,
         AttributeType attributeType) throws ParsingException
   {
      //Look for X500 Encoding
      QName x500EncodingName = new QName(JBossSAMLURIConstants.X500_NSURI.get(), JBossSAMLConstants.ENCODING.get(),
            JBossSAMLURIConstants.X500_PREFIX.get());
      Attribute x500EncodingAttr = startElement.getAttributeByName(x500EncodingName);

      if (x500EncodingAttr != null)
      {
         attributeType.getOtherAttributes().put(x500EncodingAttr.getName(),
               StaxParserUtil.getAttributeValue(x500EncodingAttr));
      }

      Attribute friendlyName = startElement.getAttributeByName(new QName(JBossSAMLConstants.FRIENDLY_NAME.get()));
      if (friendlyName != null)
         attributeType.setFriendlyName(StaxParserUtil.getAttributeValue(friendlyName));

      Attribute nameFormat = startElement.getAttributeByName(new QName(JBossSAMLConstants.NAME_FORMAT.get()));
      if (nameFormat != null)
         attributeType.setNameFormat(StaxParserUtil.getAttributeValue(nameFormat));

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            if (StaxParserUtil.matches(end, rootTag))
               break;
         }
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         String tag = StaxParserUtil.getStartElementName(startElement);

         if (JBossSAMLConstants.ATTRIBUTE.get().equals(tag))
            break;

         if (JBossSAMLConstants.ATTRIBUTE_VALUE.get().equals(tag))
         {
            Object attributeValue = parseAttributeValue(xmlEventReader);
            attributeType.addAttributeValue(attributeValue);
         }
         else
            throw new RuntimeException(UNKNOWN_TAG + tag + "::Location=" + startElement.getLocation());
      }
   }

   /**
    * Parse Attribute value
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static Object parseAttributeValue(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.ATTRIBUTE_VALUE.get());

      Attribute type = startElement.getAttributeByName(new QName(JBossSAMLURIConstants.XSI_NSURI.get(), "type", "xsi"));
      if (type == null)
      {
         return StaxParserUtil.getElementText(xmlEventReader);
      }

      String typeValue = StaxParserUtil.getAttributeValue(type);
      if (typeValue.contains(":string"))
      {
         return StaxParserUtil.getElementText(xmlEventReader);
      }
      else if (typeValue.contains(":anyType"))
      {
         //TODO: for now assume that it is a text value that can be parsed and set as the attribute value
         return StaxParserUtil.getElementText(xmlEventReader);
      }

      throw new RuntimeException(UNKNOWN_XSI + typeValue);
   }

   /**
    * Parse the AuthnStatement inside the assertion
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static AuthnStatementType parseAuthnStatement(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      String AUTHNSTATEMENT = JBossSAMLConstants.AUTHN_STATEMENT.get();
      StaxParserUtil.validate(startElement, AUTHNSTATEMENT);

      Attribute authnInstant = startElement.getAttributeByName(new QName("AuthnInstant"));
      if (authnInstant == null)
         throw new RuntimeException(REQD_ATTRIBUTE + "AuthnInstant");

      XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(authnInstant));
      AuthnStatementType authnStatementType = new AuthnStatementType(issueInstant);

      Attribute sessionIndex = startElement.getAttributeByName(new QName("SessionIndex"));
      if (sessionIndex != null)
         authnStatementType.setSessionIndex(StaxParserUtil.getAttributeValue(sessionIndex));

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
            if (endElementTag.equals(AUTHNSTATEMENT))
               break;
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + endElementTag);
         }
         startElement = null;

         if (xmlEvent instanceof StartElement)
         {
            startElement = (StartElement) xmlEvent;
         }
         else
         {
            startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         }
         if (startElement == null)
            break;

         String tag = StaxParserUtil.getStartElementName(startElement);

         if (JBossSAMLConstants.SUBJECT_LOCALITY.get().equals(tag))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            SubjectLocalityType subjectLocalityType = new SubjectLocalityType();
            Attribute address = startElement.getAttributeByName(new QName(JBossSAMLConstants.ADDRESS.get()));
            if (address != null)
            {
               subjectLocalityType.setAddress(StaxParserUtil.getAttributeValue(address));
            }
            Attribute dns = startElement.getAttributeByName(new QName(JBossSAMLConstants.DNS_NAME.get()));
            if (dns != null)
            {
               subjectLocalityType.setDNSName(StaxParserUtil.getAttributeValue(dns));
            }
            authnStatementType.setSubjectLocality(subjectLocalityType);
            StaxParserUtil.validate(StaxParserUtil.getNextEndElement(xmlEventReader),
                  JBossSAMLConstants.SUBJECT_LOCALITY.get());
         }
         else if (JBossSAMLConstants.AUTHN_CONTEXT.get().equals(tag))
         {
            authnStatementType.setAuthnContext(parseAuthnContextType(xmlEventReader));
         }
         else
            throw new RuntimeException(UNKNOWN_TAG + tag + "::Location=" + startElement.getLocation());

      }

      return authnStatementType;
   }

   /**
    * Parse the AuthnContext Type inside the AuthnStatement
    * @param xmlEventReader
    * @return
    * @throws ParsingException 
    */
   public static AuthnContextType parseAuthnContextType(XMLEventReader xmlEventReader) throws ParsingException
   {
      AuthnContextType authnContextType = new AuthnContextType();

      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.AUTHN_CONTEXT.get());

      //Get the next start element
      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      String tag = startElement.getName().getLocalPart();

      if (JBossSAMLConstants.AUTHN_CONTEXT_DECLARATION_REF.get().equals(tag))
      {
         String text = StaxParserUtil.getElementText(xmlEventReader);

         AuthnContextDeclRefType aAuthnContextDeclType = new AuthnContextDeclRefType(URI.create(text));
         authnContextType.addURIType(aAuthnContextDeclType);
         EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
         StaxParserUtil.validate(endElement, JBossSAMLConstants.AUTHN_CONTEXT.get());
      }
      else if (JBossSAMLConstants.AUTHN_CONTEXT_CLASS_REF.get().equals(tag))
      {
         String text = StaxParserUtil.getElementText(xmlEventReader);

         AuthnContextClassRefType aAuthnContextClassRefType = new AuthnContextClassRefType(URI.create(text));
         AuthnContextTypeSequence authnContextSequence = authnContextType.new AuthnContextTypeSequence();
         authnContextSequence.setClassRef(aAuthnContextClassRefType);

         authnContextType.setSequence(authnContextSequence);
         EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
         StaxParserUtil.validate(endElement, JBossSAMLConstants.AUTHN_CONTEXT.get());
      }
      else
         throw new RuntimeException(UNKNOWN_TAG + tag + "::Location=" + startElement.getLocation());

      return authnContextType;
   }

   /**
    * Parse a {@code NameIDType}
    * @param xmlEventReader
    * @return
    * @throws ParsingException
    */
   public static NameIDType parseNameIDType(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement nameIDElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      NameIDType nameID = new NameIDType();

      Attribute nameQualifier = nameIDElement.getAttributeByName(new QName(JBossSAMLConstants.NAME_QUALIFIER.get()));
      if (nameQualifier != null)
      {
         nameID.setNameQualifier(StaxParserUtil.getAttributeValue(nameQualifier));
      }

      Attribute format = nameIDElement.getAttributeByName(new QName(JBossSAMLConstants.FORMAT.get()));
      if (format != null)
      {
         nameID.setFormat(URI.create(StaxParserUtil.getAttributeValue(format)));
      }

      Attribute spProvidedID = nameIDElement.getAttributeByName(new QName(JBossSAMLConstants.SP_PROVIDED_ID.get()));
      if (spProvidedID != null)
      {
         nameID.setSPProvidedID(StaxParserUtil.getAttributeValue(spProvidedID));
      }

      Attribute spNameQualifier = nameIDElement
            .getAttributeByName(new QName(JBossSAMLConstants.SP_NAME_QUALIFIER.get()));
      if (spNameQualifier != null)
      {
         nameID.setSPNameQualifier(StaxParserUtil.getAttributeValue(spNameQualifier));
      }

      String nameIDValue = StaxParserUtil.getElementText(xmlEventReader);
      nameID.setValue(nameIDValue);

      return nameID;
   }

   /**
    * Parse a space delimited list of strings
    * @param startElement
    * @return
    */
   public static List<String> parseProtocolEnumeration(StartElement startElement)
   {
      List<String> protocolEnum = new ArrayList<String>();
      Attribute proto = startElement
            .getAttributeByName(new QName(JBossSAMLConstants.PROTOCOL_SUPPORT_ENUMERATION.get()));
      String val = StaxParserUtil.getAttributeValue(proto);
      if (StringUtil.isNotNull(val))
      {
         StringTokenizer st = new StringTokenizer(val);
         while (st.hasMoreTokens())
         {
            protocolEnum.add(st.nextToken());
         }

      }
      return protocolEnum;
   }
}