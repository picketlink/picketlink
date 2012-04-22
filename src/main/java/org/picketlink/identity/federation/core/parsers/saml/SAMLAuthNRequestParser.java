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
import javax.xml.stream.events.StartElement;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestedAuthnContextType;

/**
 * Parse the SAML2 AuthnRequest
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLAuthNRequestParser extends SAMLRequestAbstractParser implements ParserNamespaceSupport
{
   /**
    * @see {@link ParserNamespaceSupport#parse(XMLEventReader)}
    */
   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      //Get the startelement
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.AUTHN_REQUEST.get());

      AuthnRequestType authnRequest = parseBaseAttributes(startElement);

      while (xmlEventReader.hasNext())
      {
         //Let us peek at the next start element
         startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         super.parseCommonElements(startElement, xmlEventReader, authnRequest);

         String elementName = StaxParserUtil.getStartElementName(startElement);

         if (JBossSAMLConstants.NAMEID_POLICY.get().equals(elementName))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            authnRequest.setNameIDPolicy(getNameIDPolicy(startElement));
         }
         else if (JBossSAMLConstants.SUBJECT.get().equals(elementName))
         {
            authnRequest.setSubject(getSubject(xmlEventReader));
         }
         else if (JBossSAMLConstants.CONDITIONS.get().equals(elementName))
         {
            authnRequest.setConditions((ConditionsType) (new SAMLConditionsParser()).parse(xmlEventReader));
         }
         else if (JBossSAMLConstants.REQUESTED_AUTHN_CONTEXT.get().equals(elementName))
         {
            authnRequest.setRequestedAuthnContext(getRequestedAuthnContextType(xmlEventReader));
         }
         else if (JBossSAMLConstants.ISSUER.get().equals(elementName))
         {
            continue;
         }
         else if (JBossSAMLConstants.SIGNATURE.get().equals(elementName))
         {
            continue;
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + elementName + "::location="
                  + startElement.getLocation());
      }
      return authnRequest;
   }

   /**
    * @see {@link ParserNamespaceSupport#supports(QName)}
    */
   public boolean supports(QName qname)
   {
      return JBossSAMLURIConstants.PROTOCOL_NSURI.get().equals(qname.getNamespaceURI());
   }

   /**
    * Parse the attributes at the authnrequesttype element
    * @param startElement
    * @return 
    * @throws ParsingException 
    */
   private AuthnRequestType parseBaseAttributes(StartElement startElement) throws ParsingException
   {
      super.parseRequiredAttributes(startElement);
      AuthnRequestType authnRequest = new AuthnRequestType(id, issueInstant);
      //Let us get the attributes
      super.parseBaseAttributes(startElement, authnRequest);

      Attribute assertionConsumerServiceURL = startElement.getAttributeByName(new QName(
            JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE_URL.get()));
      if (assertionConsumerServiceURL != null)
      {
         String uri = StaxParserUtil.getAttributeValue(assertionConsumerServiceURL);
         authnRequest.setAssertionConsumerServiceURL(URI.create(uri));
      }

      Attribute assertionConsumerServiceIndex = startElement.getAttributeByName(new QName(
            JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE_INDEX.get()));
      if (assertionConsumerServiceIndex != null)
         authnRequest.setAssertionConsumerServiceIndex(Integer.parseInt(StaxParserUtil
               .getAttributeValue(assertionConsumerServiceIndex)));

      Attribute protocolBinding = startElement.getAttributeByName(new QName(JBossSAMLConstants.PROTOCOL_BINDING.get()));
      if (protocolBinding != null)
         authnRequest.setProtocolBinding(URI.create(StaxParserUtil.getAttributeValue(protocolBinding)));

      Attribute providerName = startElement.getAttributeByName(new QName(JBossSAMLConstants.PROVIDER_NAME.get()));
      if (providerName != null)
         authnRequest.setProviderName(StaxParserUtil.getAttributeValue(providerName));

      Attribute forceAuthn = startElement.getAttributeByName(new QName(JBossSAMLConstants.FORCE_AUTHN.get()));
      if (forceAuthn != null)
      {
         authnRequest.setForceAuthn(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(forceAuthn)));
      }

      Attribute isPassive = startElement.getAttributeByName(new QName(JBossSAMLConstants.IS_PASSIVE.get()));
      if (isPassive != null)
      {
         authnRequest.setIsPassive(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(isPassive)));
      }

      Attribute attributeConsumingServiceIndex = startElement.getAttributeByName(new QName(
            JBossSAMLConstants.ATTRIBUTE_CONSUMING_SERVICE_INDEX.get()));
      if (attributeConsumingServiceIndex != null)
         authnRequest.setAttributeConsumingServiceIndex(Integer.parseInt(StaxParserUtil
               .getAttributeValue(attributeConsumingServiceIndex)));

      return authnRequest;
   }

   /**
    * Get the NameIDPolicy
    * @param startElement
    * @return
    */
   private NameIDPolicyType getNameIDPolicy(StartElement startElement)
   {
      NameIDPolicyType nameIDPolicy = new NameIDPolicyType();
      Attribute format = startElement.getAttributeByName(new QName(JBossSAMLConstants.FORMAT.get()));
      if (format != null)
         nameIDPolicy.setFormat(URI.create(StaxParserUtil.getAttributeValue(format)));

      Attribute allowCreate = startElement.getAttributeByName(new QName(JBossSAMLConstants.ALLOW_CREATE.get()));
      if (allowCreate != null)
         nameIDPolicy.setAllowCreate(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(allowCreate)));

      return nameIDPolicy;
   }

   private RequestedAuthnContextType getRequestedAuthnContextType(XMLEventReader xmlEventReader)
         throws ParsingException
   {
      RequestedAuthnContextType ract = new RequestedAuthnContextType();
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.REQUESTED_AUTHN_CONTEXT.get());

      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      String elName = StaxParserUtil.getStartElementName(startElement);

      if (elName.equals(JBossSAMLConstants.AUTHN_CONTEXT_CLASS_REF.get()))
      {
         String value = StaxParserUtil.getElementText(xmlEventReader);
         ract.addAuthnContextClassRef(value);
      }
      else
         throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + elName);

      return ract;
   }
}