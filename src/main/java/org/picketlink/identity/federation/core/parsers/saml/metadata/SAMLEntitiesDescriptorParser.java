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
package org.picketlink.identity.federation.core.parsers.saml.metadata;

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
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.ExtensionsType;
import org.w3c.dom.Element;

/**
 * Parse the SAML Entities Descriptor
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLEntitiesDescriptorParser implements ParserNamespaceSupport
{
   private final String EDT = JBossSAMLConstants.ENTITIES_DESCRIPTOR.get();

   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, EDT);

      EntitiesDescriptorType entitiesDescriptorType = new EntitiesDescriptorType();

      //Parse the attributes 
      Attribute validUntil = startElement.getAttributeByName(new QName(JBossSAMLConstants.VALID_UNTIL.get()));
      if (validUntil != null)
      {
         String validUntilValue = StaxParserUtil.getAttributeValue(validUntil);
         entitiesDescriptorType.setValidUntil(XMLTimeUtil.parse(validUntilValue));
      }

      Attribute id = startElement.getAttributeByName(new QName(JBossSAMLConstants.ID.get()));
      if (id != null)
      {
         entitiesDescriptorType.setID(StaxParserUtil.getAttributeValue(id));
      }

      Attribute name = startElement.getAttributeByName(new QName(JBossSAMLConstants.NAME.get()));
      if (name != null)
      {
         entitiesDescriptorType.setName(StaxParserUtil.getAttributeValue(name));
      }

      Attribute cacheDuration = startElement.getAttributeByName(new QName(JBossSAMLConstants.CACHE_DURATION.get()));
      if (cacheDuration != null)
      {
         entitiesDescriptorType.setCacheDuration(XMLTimeUtil.parseAsDuration(StaxParserUtil
               .getAttributeValue(cacheDuration)));
      }

      //Get the Child Elements
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            StaxParserUtil.validate((EndElement) xmlEvent, EDT);
            StaxParserUtil.getNextEndElement(xmlEventReader);
            break;
         }
         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.ENTITY_DESCRIPTOR.get().equals(localPart))
         {
            SAMLEntityDescriptorParser entityParser = new SAMLEntityDescriptorParser();
            entitiesDescriptorType.addEntityDescriptor(entityParser.parse(xmlEventReader));
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            entitiesDescriptorType.setExtensions(parseExtensions(xmlEventReader));
         }
         else if (JBossSAMLConstants.ENTITIES_DESCRIPTOR.get().equalsIgnoreCase(localPart))
         {
            SAMLEntitiesDescriptorParser parser = new SAMLEntitiesDescriptorParser();
            entitiesDescriptorType.addEntityDescriptor(parser.parse(xmlEventReader));
         }
         else if (localPart.equals(JBossSAMLConstants.SIGNATURE.get()))
         {
            entitiesDescriptorType.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart + " ::location=" + startElement.getLocation());
      }
      return entitiesDescriptorType;
   }

   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get()) && localPart.equals(EDT);
   }

   private ExtensionsType parseExtensions(XMLEventReader xmlEventReader) throws ParsingException
   {
      ExtensionsType extensions = new ExtensionsType();
      Element extElement = StaxParserUtil.getDOMElement(xmlEventReader);
      extensions.setElement(extElement);
      return extensions;
   }
}