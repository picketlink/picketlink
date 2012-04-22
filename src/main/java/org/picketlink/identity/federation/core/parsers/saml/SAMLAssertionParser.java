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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAMLParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedAssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.w3c.dom.Element;

/**
 * Parse the saml assertion
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAMLAssertionParser implements ParserNamespaceSupport
{
   private final String ASSERTION = JBossSAMLConstants.ASSERTION.get();

   public AssertionType fromElement(Element element) throws ConfigurationException, ProcessingException,
         ParsingException
   {
      XMLEventReader xmlEventReader = StaxParserUtil.getXMLEventReader(DocumentUtil.getNodeAsStream(element));
      return (AssertionType) parse(xmlEventReader);
   }

   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
      String startElementName = StaxParserUtil.getStartElementName(startElement);
      if (startElementName.equals(JBossSAMLConstants.ENCRYPTED_ASSERTION.get()))
      {
         Element domElement = StaxParserUtil.getDOMElement(xmlEventReader);

         EncryptedAssertionType encryptedAssertion = new EncryptedAssertionType();
         encryptedAssertion.setEncryptedElement(domElement);
         return encryptedAssertion;
      }

      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

      //Special case: Encrypted Assertion 
      StaxParserUtil.validate(startElement, ASSERTION);
      AssertionType assertion = parseBaseAttributes(startElement);

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
               throw new RuntimeException(ErrorCodes.UNKNOWN_END_ELEMENT + endElementTag);
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
            continue;
         }

         if (JBossSAMLConstants.ISSUER.get().equalsIgnoreCase(tag))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            String issuerValue = StaxParserUtil.getElementText(xmlEventReader);
            NameIDType issuer = new NameIDType();
            issuer.setValue(issuerValue);

            assertion.setIssuer(issuer);
         }
         else if (JBossSAMLConstants.SUBJECT.get().equalsIgnoreCase(tag))
         {
            SAMLSubjectParser subjectParser = new SAMLSubjectParser();
            assertion.setSubject((SubjectType) subjectParser.parse(xmlEventReader));
         }
         else if (JBossSAMLConstants.CONDITIONS.get().equalsIgnoreCase(tag))
         {
            SAMLConditionsParser conditionsParser = new SAMLConditionsParser();
            ConditionsType conditions = (ConditionsType) conditionsParser.parse(xmlEventReader);

            assertion.setConditions(conditions);
         }
         else if (JBossSAMLConstants.AUTHN_STATEMENT.get().equalsIgnoreCase(tag))
         {
            AuthnStatementType authnStatementType = SAMLParserUtil.parseAuthnStatement(xmlEventReader);
            assertion.addStatement(authnStatementType);
         }
         else if (JBossSAMLConstants.ATTRIBUTE_STATEMENT.get().equalsIgnoreCase(tag))
         {
            AttributeStatementType attributeStatementType = SAMLParserUtil.parseAttributeStatement(xmlEventReader);
            assertion.addStatement(attributeStatementType);
         }
         else if (JBossSAMLConstants.STATEMENT.get().equalsIgnoreCase(tag))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

            String xsiTypeValue = StaxParserUtil.getXSITypeValue(startElement);
            if (xsiTypeValue.contains(JBossSAMLConstants.XACML_AUTHZ_DECISION_STATEMENT_TYPE.get()))
            {
               XACMLAuthzDecisionStatementType authZStat = new XACMLAuthzDecisionStatementType();

               startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
               tag = StaxParserUtil.getStartElementName(startElement);

               if (tag.contains(JBossSAMLConstants.RESPONSE.get()))
               {
                  authZStat.setResponse(getXACMLResponse(xmlEventReader));
                  startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
                  //There may be request also
                  tag = StaxParserUtil.getStartElementName(startElement);
                  if (tag.contains(JBossSAMLConstants.REQUEST.get()))
                  {
                     authZStat.setRequest(getXACMLRequest(xmlEventReader));
                  }
               }

               EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
               StaxParserUtil.validate(endElement, JBossSAMLConstants.STATEMENT.get());
               assertion.addStatement(authZStat);
            }
            else
               throw new RuntimeException(ErrorCodes.UNKNOWN_XSI + xsiTypeValue);
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + tag + "::location=" + peekedElement.getLocation());
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

   private AssertionType parseBaseAttributes(StartElement nextElement) throws ParsingException
   {
      Attribute idAttribute = nextElement.getAttributeByName(new QName(JBossSAMLConstants.ID.get()));
      String id = StaxParserUtil.getAttributeValue(idAttribute);

      Attribute versionAttribute = nextElement.getAttributeByName(new QName(JBossSAMLConstants.VERSION.get()));
      String version = StaxParserUtil.getAttributeValue(versionAttribute);
      StringUtil.match(JBossSAMLConstants.VERSION_2_0.get(), version);

      Attribute issueInstantAttribute = nextElement
            .getAttributeByName(new QName(JBossSAMLConstants.ISSUE_INSTANT.get()));
      XMLGregorianCalendar issueInstant = XMLTimeUtil.parse(StaxParserUtil.getAttributeValue(issueInstantAttribute));

      return new AssertionType(id, issueInstant);
   }

   @SuppressWarnings("unchecked")
   private ResponseType getXACMLResponse(XMLEventReader xmlEventReader) throws ParsingException
   {
      Element xacmlResponse = StaxParserUtil.getDOMElement(xmlEventReader);
      //xacml request
      String xacmlPath = "org.jboss.security.xacml.core.model.context";
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(xacmlPath);
         Unmarshaller un = jaxb.createUnmarshaller();
         un.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
         JAXBElement<ResponseType> jaxbResponseType = (JAXBElement<ResponseType>) un.unmarshal(DocumentUtil
               .getNodeAsStream(xacmlResponse));
         return jaxbResponseType.getValue();
      }
      catch (Exception e)
      {
         throw new ParsingException(e);
      }
   }

   @SuppressWarnings("unchecked")
   private RequestType getXACMLRequest(XMLEventReader xmlEventReader) throws ParsingException
   {
      Element xacmlRequest = StaxParserUtil.getDOMElement(xmlEventReader);
      //xacml request
      String xacmlPath = "org.jboss.security.xacml.core.model.context";
      try
      {
         JAXBContext jaxb = JAXBContext.newInstance(xacmlPath);
         Unmarshaller un = jaxb.createUnmarshaller();
         un.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
         JAXBElement<RequestType> jaxbRequestType = (JAXBElement<RequestType>) un.unmarshal(DocumentUtil
               .getNodeAsStream(xacmlRequest));
         return jaxbRequestType.getValue();
      }
      catch (Exception e)
      {
         throw new ParsingException(e);
      }
   }
}