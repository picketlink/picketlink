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
package org.picketlink.identity.federation.core.parsers.saml.xacml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.saml.SAMLRequestAbstractParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.protocol.XACMLAuthzDecisionQueryType;
import org.w3c.dom.Element;

/**
 * Parse the XACML Elements as specified by the SAML-XACML Profile.
 * @author Anil.Saldhana@redhat.com
 * @since Dec 16, 2010
 */
public class SAMLXACMLRequestParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport
{
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      String tag = StaxParserUtil.getStartElementName(startElement);
      if (tag.equals(JBossSAMLConstants.REQUEST_ABSTRACT.get()))
      {
         String xsiTypeValue = StaxParserUtil.getXSITypeValue(startElement);
         if (xsiTypeValue.contains(JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY_TYPE.get()))
         {
            return parseXACMLAuthzDecisionQuery(startElement, xmlEventReader);
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_XSI + xsiTypeValue);
      }
      else if (tag.equals(JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY.get()))
      {
         return parseXACMLAuthzDecisionQuery(startElement, xmlEventReader);
      }

      throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + tag + "::location=" + startElement.getLocation());
   }

   public boolean supports(QName qname)
   {
      return false;
   }

   @SuppressWarnings("unchecked")
   private XACMLAuthzDecisionQueryType parseXACMLAuthzDecisionQuery(StartElement startElement,
         XMLEventReader xmlEventReader) throws ParsingException
   {
      super.parseRequiredAttributes(startElement);

      XACMLAuthzDecisionQueryType xacmlQuery = new XACMLAuthzDecisionQueryType(id, issueInstant);
      super.parseBaseAttributes(startElement, xacmlQuery);

      String inputContextOnly = StaxParserUtil.getAttributeValue(startElement,
            JBossSAMLConstants.INPUT_CONTEXT_ONLY.get());
      if (inputContextOnly != null)
      {
         xacmlQuery.setInputContextOnly(Boolean.parseBoolean(inputContextOnly));
      }
      String returnContext = StaxParserUtil.getAttributeValue(startElement, JBossSAMLConstants.RETURN_CONTEXT.get());
      if (returnContext != null)
      {
         xacmlQuery.setReturnContext(Boolean.parseBoolean(returnContext));
      }

      //Go thru the children
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) xmlEvent;
            if (!(StaxParserUtil.matches(endElement, JBossSAMLConstants.REQUEST_ABSTRACT.get()) || StaxParserUtil
                  .matches(endElement, JBossSAMLConstants.XACML_AUTHZ_DECISION_QUERY.get())))
               throw new ParsingException(ErrorCodes.EXPECTED_END_TAG + "RequestAbstract or XACMLAuthzDecisionQuery");
            break;
         }
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         super.parseCommonElements(startElement, xmlEventReader, xacmlQuery);
         String tag = StaxParserUtil.getStartElementName(startElement);

         if (tag.equals(JBossSAMLConstants.REQUEST.get()))
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
               RequestType req = jaxbRequestType.getValue();
               xacmlQuery.setRequest(req);
            }
            catch (Exception e)
            {
               throw new ParsingException(e);
            }
         }
      }
      return xacmlQuery;
   }
}