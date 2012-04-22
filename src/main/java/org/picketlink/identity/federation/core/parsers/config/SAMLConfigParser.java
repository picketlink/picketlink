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
package org.picketlink.identity.federation.core.parsers.config;

import static org.picketlink.identity.federation.core.ErrorCodes.UNKNOWN_END_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.config.AuthPropertyType;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyProviderType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.MetadataProviderType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.handler.config.Handler;
import org.picketlink.identity.federation.core.handler.config.Handlers;
import org.picketlink.identity.federation.core.parsers.AbstractParser;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;

/**
 * Parse the SAML IDP/SP config as well as the handlers
 * @author Anil.Saldhana@redhat.com
 * @since Feb 4, 2011
 */
public class SAMLConfigParser extends AbstractParser
{
   public static final String IDP = "PicketLinkIDP";

   public static final String SP = "PicketLinkSP";

   public static final String IDENTITY_URL = "IdentityURL";

   public static final String SERVICE_URL = "ServiceURL";

   public static final String IDP_METADATA_FILE = "IDPMetadataFile";

   public static final String TRUST = "Trust";

   public static final String DOMAINS = "Domains";

   public static final String KEY_PROVIDER = "KeyProvider";

   public static final String META_PROVIDER = "MetaDataProvider";

   public static final String CLASS_NAME = "ClassName";

   public static final String CLASS = "class";

   public static final String AUTH = "Auth";

   public static final String KEY = "Key";

   public static final String VALUE = "Value";

   public static final String VALIDATING_ALIAS = "ValidatingAlias";

   public static final String ASSERTION_VALIDITY = "AssertionValidity";

   public static final String ROLE_GENERATOR = "RoleGenerator";

   public static final String ENCRYPT = "Encrypt";

   public static final String ATTRIBUTE_MANAGER = "AttributeManager";

   public static final String CANONICALIZATION_METHOD = "CanonicalizationMethod";

   public static final String HANDLERS = "Handlers";

   public static final String HANDLER = "Handler";

   public static final String OPTION = "Option";

   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.peekNextStartElement(xmlEventReader);

      if (StaxParserUtil.getStartElementName(startElement).equals(IDP))
         return parseIDPConfiguration(xmlEventReader);
      if (StaxParserUtil.getStartElementName(startElement).equals(SP))
         return parseSPConfiguration(xmlEventReader);

      return parseHandlers(xmlEventReader);
   }

   public boolean supports(QName qname)
   {
      return false;
   }

   protected Handlers parseHandlers(XMLEventReader xmlEventReader) throws ParsingException
   {
      Handlers handlers = new Handlers();

      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, HANDLERS);

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(HANDLERS))
               break;
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + endElementName);
         }

         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         String elementName = StaxParserUtil.getStartElementName(startElement);
         if (elementName.equals(HANDLER))
         {
            Handler handler = parseHandler(xmlEventReader, startElement);
            handlers.add(handler);
         }
      }

      return handlers;
   }

   protected IDPType parseIDPConfiguration(XMLEventReader xmlEventReader) throws ParsingException
   {
      IDPType idp = new IDPType();
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, IDP);

      // parse and set the root element attributes.
      QName attributeQName = new QName("", ASSERTION_VALIDITY);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         idp.setAssertionValidity(Long.parseLong(StaxParserUtil.getAttributeValue(attribute)));

      attributeQName = new QName("", ROLE_GENERATOR);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         idp.setRoleGenerator(StaxParserUtil.getAttributeValue(attribute));

      attributeQName = new QName("", ENCRYPT);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         idp.setEncrypt(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(attribute)));

      attributeQName = new QName("", CANONICALIZATION_METHOD);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         idp.setCanonicalizationMethod(StaxParserUtil.getAttributeValue(attribute));

      attributeQName = new QName("", ATTRIBUTE_MANAGER);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         idp.setAttributeManager(StaxParserUtil.getAttributeValue(attribute));

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(IDP))
               break;
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + endElementName);
         }

         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         String elementName = StaxParserUtil.getStartElementName(startElement);
         if (elementName.equals(IDENTITY_URL))
         {
            idp.setIdentityURL(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (elementName.equals(TRUST))
         {
            TrustType trustType = new TrustType();
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            StaxParserUtil.validate(startElement, DOMAINS);
            trustType.setDomains(StaxParserUtil.getElementText(xmlEventReader));
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, TRUST);
            idp.setTrust(trustType);
         }
         else if (elementName.equals(KEY_PROVIDER))
         {
            KeyProviderType keyProviderType = this.parseKeyProvider(xmlEventReader, startElement);
            idp.setKeyProvider(keyProviderType);
         }
         else if (elementName.equals(META_PROVIDER))
         {
            MetadataProviderType mdProviderType = parseMDProvider(xmlEventReader, startElement);
            idp.setMetaDataProvider(mdProviderType);
         }
      }
      return idp;
   }

   protected SPType parseSPConfiguration(XMLEventReader xmlEventReader) throws ParsingException
   {
      SPType sp = new SPType();
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, SP);

      QName attributeQName = new QName("", CANONICALIZATION_METHOD);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         sp.setCanonicalizationMethod(StaxParserUtil.getAttributeValue(attribute));

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = (EndElement) StaxParserUtil.getNextEvent(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(SP))
               break;
            else
               throw new RuntimeException(UNKNOWN_END_ELEMENT + endElementName);
         }

         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         if (startElement == null)
            break;
         String elementName = StaxParserUtil.getStartElementName(startElement);
         if (elementName.equals(IDENTITY_URL))
         {
            sp.setIdentityURL(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (elementName.equals(SERVICE_URL))
         {
            sp.setServiceURL(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (elementName.equals(IDP_METADATA_FILE))
         {
            sp.setIdpMetadataFile(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (elementName.equals(TRUST))
         {
            TrustType trustType = new TrustType();
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            StaxParserUtil.validate(startElement, DOMAINS);
            trustType.setDomains(StaxParserUtil.getElementText(xmlEventReader));
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, TRUST);
            sp.setTrust(trustType);
         }
         else if (elementName.equals(KEY_PROVIDER))
         {
            KeyProviderType keyProviderType = parseKeyProvider(xmlEventReader, startElement);
            sp.setKeyProvider(keyProviderType);
         }
         else if (elementName.equals(META_PROVIDER))
         {
            MetadataProviderType mdProviderType = parseMDProvider(xmlEventReader, startElement);
            sp.setMetaDataProvider(mdProviderType);
         }
      }
      return sp;
   }

   protected KeyProviderType parseKeyProvider(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      XMLEvent xmlEvent = null;
      KeyProviderType keyProviderType = new KeyProviderType();

      // parse and set the ClassName element attributes.
      QName attributeQName = new QName("", CLASS_NAME);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         keyProviderType.setClassName(StaxParserUtil.getAttributeValue(attribute));

      while (xmlEventReader.hasNext())
      {
         xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(KEY_PROVIDER))
               break;
            else
               continue;
         }
         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         String startElementName = StaxParserUtil.getStartElementName(startElement);
         if (startElementName.equals(AUTH))
         {
            AuthPropertyType auth = new AuthPropertyType();
            populateKeyValueType(auth, startElement);

            keyProviderType.add(auth);
         }
         else if (startElementName.equals(VALIDATING_ALIAS))
         {
            KeyValueType auth = new KeyValueType();
            populateKeyValueType(auth, startElement);

            keyProviderType.add(auth);
         }
      }
      return keyProviderType;
   }

   protected Handler parseHandler(XMLEventReader xmlEventReader, StartElement startElement) throws ParsingException
   {
      XMLEvent xmlEvent = null;
      Handler handlerType = new Handler();

      // parse and set the ClassName element attributes.
      QName attributeQName = new QName("", CLASS);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         handlerType.setClazz(StaxParserUtil.getAttributeValue(attribute));

      while (xmlEventReader.hasNext())
      {
         xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(HANDLER))
               break;
            else
               continue;
         }
         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         String startElementName = StaxParserUtil.getStartElementName(startElement);

         if (startElementName.equals(OPTION))
         {
            KeyValueType auth = new KeyValueType();
            populateKeyValueType(auth, startElement);

            handlerType.add(auth);
         }
      }
      return handlerType;
   }

   protected MetadataProviderType parseMDProvider(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      XMLEvent xmlEvent = null;
      MetadataProviderType metaProviderType = new MetadataProviderType();

      // parse and set the ClassName element attributes.
      QName attributeQName = new QName("", CLASS_NAME);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         metaProviderType.setClassName(StaxParserUtil.getAttributeValue(attribute));

      while (xmlEventReader.hasNext())
      {
         xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent == null)
            break;
         if (xmlEvent instanceof EndElement)
         {
            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            String endElementName = StaxParserUtil.getEndElementName(endElement);
            if (endElementName.equals(META_PROVIDER))
               break;
            else
               continue;
         }
         startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
         String startElementName = StaxParserUtil.getStartElementName(startElement);
         if (startElementName.equals(OPTION))
         {
            KeyValueType auth = new KeyValueType();
            populateKeyValueType(auth, startElement);

            metaProviderType.add(auth);
         }
      }
      return metaProviderType;
   }

   protected void populateKeyValueType(KeyValueType kvt, StartElement startElement)
   {
      QName attributeQName = new QName("", KEY);
      Attribute attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         kvt.setKey(StaxParserUtil.getAttributeValue(attribute));

      attributeQName = new QName("", OPTION);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         kvt.setKey(StaxParserUtil.getAttributeValue(attribute));

      attributeQName = new QName("", VALUE);
      attribute = startElement.getAttributeByName(attributeQName);
      if (attribute != null)
         kvt.setValue(StaxParserUtil.getAttributeValue(attribute));
   }
}