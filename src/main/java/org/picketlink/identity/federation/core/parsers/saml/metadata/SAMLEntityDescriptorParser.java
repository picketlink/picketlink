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

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.ParserNamespaceSupport;
import org.picketlink.identity.federation.core.parsers.util.SAMLParserUtil;
import org.picketlink.identity.federation.core.parsers.util.StaxParserUtil;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.AttributeAuthorityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.AttributeConsumingServiceType;
import org.picketlink.identity.federation.saml.v2.metadata.ContactType;
import org.picketlink.identity.federation.saml.v2.metadata.ContactTypeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.ExtensionsType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyTypes;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedNameType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedURIType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.RequestedAttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.w3c.dom.Element;

/**
 * Parse the SAML Metadata element "EntityDescriptor"
 * @author Anil.Saldhana@redhat.com
 * @since Dec 14, 2010
 */
public class SAMLEntityDescriptorParser implements ParserNamespaceSupport
{
   private final String EDT = JBossSAMLConstants.ENTITY_DESCRIPTOR.get();

   public Object parse(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, EDT);

      Attribute entityID = startElement.getAttributeByName(new QName(JBossSAMLConstants.ENTITY_ID.get()));
      String entityIDValue = StaxParserUtil.getAttributeValue(entityID);
      EntityDescriptorType entityDescriptorType = new EntityDescriptorType(entityIDValue);

      Attribute validUntil = startElement.getAttributeByName(new QName(JBossSAMLConstants.VALID_UNTIL.get()));
      if (validUntil != null)
      {
         String validUntilValue = StaxParserUtil.getAttributeValue(validUntil);
         entityDescriptorType.setValidUntil(XMLTimeUtil.parse(validUntilValue));
      }

      Attribute id = startElement.getAttributeByName(new QName(JBossSAMLConstants.ID.get()));
      if (id != null)
      {
         entityDescriptorType.setID(StaxParserUtil.getAttributeValue(id));
      }

      Attribute cacheDuration = startElement.getAttributeByName(new QName(JBossSAMLConstants.CACHE_DURATION.get()));
      if (cacheDuration != null)
      {
         entityDescriptorType.setCacheDuration(XMLTimeUtil.parseAsDuration(StaxParserUtil
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

         if (JBossSAMLConstants.IDP_SSO_DESCRIPTOR.get().equals(localPart))
         {
            IDPSSODescriptorType idpSSO = parseIDPSSODescriptor(xmlEventReader);

            EDTDescriptorChoiceType edtDescChoice = new EDTDescriptorChoiceType(idpSSO);
            EDTChoiceType edtChoice = EDTChoiceType.oneValue(edtDescChoice);
            entityDescriptorType.addChoiceType(edtChoice);
         }
         else if (JBossSAMLConstants.SP_SSO_DESCRIPTOR.get().equals(localPart))
         {
            SPSSODescriptorType spSSO = parseSPSSODescriptor(xmlEventReader);

            EDTDescriptorChoiceType edtDescChoice = new EDTDescriptorChoiceType(spSSO);
            EDTChoiceType edtChoice = EDTChoiceType.oneValue(edtDescChoice);
            entityDescriptorType.addChoiceType(edtChoice);
         }
         else if (JBossSAMLConstants.ATTRIBUTE_AUTHORITY_DESCRIPTOR.get().equals(localPart))
         {
            AttributeAuthorityDescriptorType attrAuthority = parseAttributeAuthorityDescriptor(xmlEventReader);

            EDTDescriptorChoiceType edtDescChoice = new EDTDescriptorChoiceType(attrAuthority);
            EDTChoiceType edtChoice = EDTChoiceType.oneValue(edtDescChoice);
            entityDescriptorType.addChoiceType(edtChoice);
         }
         else if (JBossSAMLConstants.AUTHN_AUTHORITY_DESCRIPTOR.get().equals(localPart))
         {
            throw new ParsingException(ErrorCodes.UNSUPPORTED_TYPE + " AuthnAuthorityDescriptor");
         }
         else if (JBossSAMLConstants.AFFILIATION_DESCRIPTOR.get().equals(localPart))
         {
            throw new ParsingException(ErrorCodes.UNSUPPORTED_TYPE + " AffiliationDescriptor");
         }
         else if (JBossSAMLConstants.PDP_DESCRIPTOR.get().equals(localPart))
         {
            throw new ParsingException(ErrorCodes.UNSUPPORTED_TYPE + " PDPDescriptor");
         }
         else if (localPart.equals(JBossSAMLConstants.SIGNATURE.get()))
         {
            entityDescriptorType.setSignature(StaxParserUtil.getDOMElement(xmlEventReader));
         }
         else if (JBossSAMLConstants.ORGANIZATION.get().equals(localPart))
         {
            OrganizationType organization = parseOrganization(xmlEventReader);

            entityDescriptorType.setOrganization(organization);
         }
         else if (JBossSAMLConstants.CONTACT_PERSON.get().equals(localPart))
         {
            entityDescriptorType.addContactPerson(parseContactPerson(xmlEventReader));
         }
         else if (JBossSAMLConstants.ADDITIONAL_METADATA_LOCATION.get().equals(localPart))
         {
            throw new ParsingException(ErrorCodes.UNSUPPORTED_TYPE + " AdditionalMetadataLocation");
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            entityDescriptorType.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_START_ELEMENT + localPart + "::location="
                  + startElement.getLocation());
      }
      return entityDescriptorType;
   }

   public boolean supports(QName qname)
   {
      String nsURI = qname.getNamespaceURI();
      String localPart = qname.getLocalPart();

      return nsURI.equals(JBossSAMLURIConstants.ASSERTION_NSURI.get())
            && localPart.equals(JBossSAMLConstants.ENTITY_DESCRIPTOR.get());
   }

   private SPSSODescriptorType parseSPSSODescriptor(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.SP_SSO_DESCRIPTOR.get());

      List<String> protocolEnum = SAMLParserUtil.parseProtocolEnumeration(startElement);
      SPSSODescriptorType spSSODescriptor = new SPSSODescriptorType(protocolEnum);

      Attribute wantAssertionsSigned = startElement.getAttributeByName(new QName(
            JBossSAMLConstants.WANT_ASSERTIONS_SIGNED.get()));
      if (wantAssertionsSigned != null)
      {
         spSSODescriptor.setWantAssertionsSigned(Boolean.parseBoolean(StaxParserUtil
               .getAttributeValue(wantAssertionsSigned)));
      }
      Attribute wantAuthnSigned = startElement.getAttributeByName(new QName(JBossSAMLConstants.AUTHN_REQUESTS_SIGNED
            .get()));
      if (wantAuthnSigned != null)
      {
         spSSODescriptor
               .setAuthnRequestsSigned(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(wantAuthnSigned)));
      }

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.SP_SSO_DESCRIPTOR.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.ARTIFACT_RESOLUTION_SERVICE.get().equals(localPart))
         {
            IndexedEndpointType endpoint = parseArtifactResolutionService(xmlEventReader, startElement);
            spSSODescriptor.addArtifactResolutionService(endpoint);
         }
         else if (JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE.get().equals(localPart))
         {
            IndexedEndpointType endpoint = parseAssertionConsumerService(xmlEventReader, startElement);
            spSSODescriptor.addAssertionConsumerService(endpoint);
         }
         else if (JBossSAMLConstants.ATTRIBUTE_CONSUMING_SERVICE.get().equals(localPart))
         {
            AttributeConsumingServiceType attributeConsumer = parseAttributeConsumingService(xmlEventReader,
                  startElement);
            spSSODescriptor.addAttributeConsumerService(attributeConsumer);
         }
         else if (JBossSAMLConstants.SINGLE_LOGOUT_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.SINGLE_LOGOUT_SERVICE.get());

            spSSODescriptor.addSingleLogoutService(endpoint);
         }
         else if (JBossSAMLConstants.MANAGE_NAMEID_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.MANAGE_NAMEID_SERVICE.get());

            spSSODescriptor.addManageNameIDService(endpoint);
         }
         else if (JBossSAMLConstants.NAMEID_FORMAT.get().equalsIgnoreCase(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            spSSODescriptor.addNameIDFormat(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.KEY_DESCRIPTOR.get().equalsIgnoreCase(localPart))
         {
            KeyDescriptorType keyDescriptor = new KeyDescriptorType();
            String use = StaxParserUtil.getAttributeValue(startElement, "use");
            if (use != null)
               keyDescriptor.setUse(KeyTypes.fromValue(use));

            Element key = StaxParserUtil.getDOMElement(xmlEventReader);
            keyDescriptor.setKeyInfo(key);
            spSSODescriptor.addKeyDescriptor(keyDescriptor);
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            spSSODescriptor.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);
      }
      return spSSODescriptor;
   }

   private IDPSSODescriptorType parseIDPSSODescriptor(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.IDP_SSO_DESCRIPTOR.get());

      List<String> protocolEnum = SAMLParserUtil.parseProtocolEnumeration(startElement);
      IDPSSODescriptorType idpSSODescriptor = new IDPSSODescriptorType(protocolEnum);

      Attribute wantAuthnSigned = startElement.getAttributeByName(new QName(
            JBossSAMLConstants.WANT_AUTHN_REQUESTS_SIGNED.get()));
      if (wantAuthnSigned != null)
      {
         idpSSODescriptor.setWantAuthnRequestsSigned(Boolean.parseBoolean(StaxParserUtil
               .getAttributeValue(wantAuthnSigned)));
      }

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.IDP_SSO_DESCRIPTOR.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.ARTIFACT_RESOLUTION_SERVICE.get().equals(localPart))
         {
            IndexedEndpointType endpoint = parseArtifactResolutionService(xmlEventReader, startElement);
            idpSSODescriptor.addArtifactResolutionService(endpoint);
         }
         else if (JBossSAMLConstants.ASSERTION_ID_REQUEST_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.ASSERTION_ID_REQUEST_SERVICE.get());

            idpSSODescriptor.addAssertionIDRequestService(endpoint);
         }
         else if (JBossSAMLConstants.SINGLE_LOGOUT_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.SINGLE_LOGOUT_SERVICE.get());

            idpSSODescriptor.addSingleLogoutService(endpoint);
         }
         else if (JBossSAMLConstants.SINGLE_SIGNON_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.SINGLE_SIGNON_SERVICE.get());

            idpSSODescriptor.addSingleSignOnService(endpoint);
         }
         else if (JBossSAMLConstants.MANAGE_NAMEID_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.MANAGE_NAMEID_SERVICE.get());

            idpSSODescriptor.addManageNameIDService(endpoint);
         }
         else if (JBossSAMLConstants.NAMEID_MAPPING_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            EndpointType endpoint = getEndpointType(startElement);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.NAMEID_MAPPING_SERVICE.get());

            idpSSODescriptor.addNameIDMappingService(endpoint);
         }
         else if (JBossSAMLConstants.NAMEID_FORMAT.get().equalsIgnoreCase(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            idpSSODescriptor.addNameIDFormat(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.ATTRIBUTE.get().equalsIgnoreCase(localPart))
         {
            AttributeType attribute = SAMLParserUtil.parseAttribute(xmlEventReader);
            idpSSODescriptor.addAttribute(attribute);
         }
         else if (JBossSAMLConstants.KEY_DESCRIPTOR.get().equalsIgnoreCase(localPart))
         {
            KeyDescriptorType keyDescriptor = new KeyDescriptorType();
            String use = StaxParserUtil.getAttributeValue(startElement, "use");
            if (use != null && !use.isEmpty())
            {
               keyDescriptor.setUse(KeyTypes.fromValue(use));
            }

            Element key = StaxParserUtil.getDOMElement(xmlEventReader);
            keyDescriptor.setKeyInfo(key);
            idpSSODescriptor.addKeyDescriptor(keyDescriptor);
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            idpSSODescriptor.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);
      }
      return idpSSODescriptor;
   }

   private EndpointType getEndpointType(StartElement startElement)
   {
      Attribute bindingAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.BINDING.get()));
      String binding = StaxParserUtil.getAttributeValue(bindingAttr);

      Attribute locationAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.LOCATION.get()));
      String location = StaxParserUtil.getAttributeValue(locationAttr);

      EndpointType endpoint = new IndexedEndpointType(URI.create(binding), URI.create(location));
      Attribute responseLocation = startElement
            .getAttributeByName(new QName(JBossSAMLConstants.RESPONSE_LOCATION.get()));
      if (responseLocation != null)
      {
         endpoint.setResponseLocation(URI.create(StaxParserUtil.getAttributeValue(responseLocation)));
      }
      return endpoint;
   }

   private AttributeAuthorityDescriptorType parseAttributeAuthorityDescriptor(XMLEventReader xmlEventReader)
         throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.ATTRIBUTE_AUTHORITY_DESCRIPTOR.get());
      List<String> protocolEnum = SAMLParserUtil.parseProtocolEnumeration(startElement);
      AttributeAuthorityDescriptorType attributeAuthority = new AttributeAuthorityDescriptorType(protocolEnum);

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.ATTRIBUTE_AUTHORITY_DESCRIPTOR.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.ATTRIBUTE_SERVICE.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            Attribute bindingAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.BINDING.get()));
            String binding = StaxParserUtil.getAttributeValue(bindingAttr);

            Attribute locationAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.LOCATION.get()));
            String location = StaxParserUtil.getAttributeValue(locationAttr);

            IndexedEndpointType endpoint = new IndexedEndpointType(URI.create(binding), URI.create(location));

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.ATTRIBUTE_SERVICE.get());

            attributeAuthority.addAttributeService(endpoint);
         }
         else if (JBossSAMLConstants.KEY_DESCRIPTOR.get().equalsIgnoreCase(localPart))
         {
            KeyDescriptorType keyDescriptor = new KeyDescriptorType();
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

            Element key = StaxParserUtil.getDOMElement(xmlEventReader);
            keyDescriptor.setKeyInfo(key);

            EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(endElement, JBossSAMLConstants.KEY_DESCRIPTOR.get());

            attributeAuthority.addKeyDescriptor(keyDescriptor);
         }
         else if (JBossSAMLConstants.NAMEID_FORMAT.get().equalsIgnoreCase(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            attributeAuthority.addNameIDFormat(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            attributeAuthority.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);

      }
      return attributeAuthority;
   }

   private OrganizationType parseOrganization(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.ORGANIZATION.get());

      OrganizationType org = new OrganizationType();

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.ORGANIZATION.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.ORGANIZATION_NAME.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            LocalizedNameType localName = getLocalizedName(xmlEventReader, startElement);
            org.addOrganizationName(localName);
         }
         else if (JBossSAMLConstants.ORGANIZATION_DISPLAY_NAME.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            LocalizedNameType localName = getLocalizedName(xmlEventReader, startElement);
            org.addOrganizationDisplayName(localName);
         }
         else if (JBossSAMLConstants.ORGANIZATION_URL.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            Attribute lang = startElement.getAttributeByName(new QName(JBossSAMLURIConstants.XML.get(), "lang"));
            String langVal = StaxParserUtil.getAttributeValue(lang);
            LocalizedURIType localName = new LocalizedURIType(langVal);
            localName.setValue(URI.create(StaxParserUtil.getElementText(xmlEventReader)));
            org.addOrganizationURL(localName);
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            org.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);
      }
      return org;
   }

   private ContactType parseContactPerson(XMLEventReader xmlEventReader) throws ParsingException
   {
      StartElement startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.CONTACT_PERSON.get());

      Attribute attr = startElement.getAttributeByName(new QName(JBossSAMLConstants.CONTACT_TYPE.get()));
      if (attr == null)
         throw new ParsingException(ErrorCodes.REQD_ATTRIBUTE + "contactType");
      ContactType contactType = new ContactType(ContactTypeType.fromValue(StaxParserUtil.getAttributeValue(attr)));

      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.CONTACT_PERSON.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.COMPANY.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            contactType.setCompany(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.GIVEN_NAME.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            contactType.setGivenName(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.SURNAME.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            contactType.setSurName(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.EMAIL_ADDRESS.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            contactType.addEmailAddress(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.TELEPHONE_NUMBER.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            contactType.addTelephone(StaxParserUtil.getElementText(xmlEventReader));
         }
         else if (JBossSAMLConstants.EXTENSIONS.get().equalsIgnoreCase(localPart))
         {
            contactType.setExtensions(parseExtensions(xmlEventReader));
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);
      }
      return contactType;
   }

   private LocalizedNameType getLocalizedName(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      Attribute lang = startElement.getAttributeByName(new QName(JBossSAMLURIConstants.XML.get(), "lang"));
      String langVal = StaxParserUtil.getAttributeValue(lang);
      LocalizedNameType localName = new LocalizedNameType(langVal);
      localName.setValue(StaxParserUtil.getElementText(xmlEventReader));
      return localName;
   }

   private IndexedEndpointType parseAssertionConsumerService(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      IndexedEndpointType endpoint = parseIndexedEndpoint(xmlEventReader, startElement);

      EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
      StaxParserUtil.validate(endElement, JBossSAMLConstants.ASSERTION_CONSUMER_SERVICE.get());

      return endpoint;
   }

   private IndexedEndpointType parseArtifactResolutionService(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      IndexedEndpointType endpoint = parseIndexedEndpoint(xmlEventReader, startElement);

      EndElement endElement = StaxParserUtil.getNextEndElement(xmlEventReader);
      StaxParserUtil.validate(endElement, JBossSAMLConstants.ARTIFACT_RESOLUTION_SERVICE.get());

      return endpoint;
   }

   private IndexedEndpointType parseIndexedEndpoint(XMLEventReader xmlEventReader, StartElement startElement)
   {
      Attribute bindingAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.BINDING.get()));
      String binding = StaxParserUtil.getAttributeValue(bindingAttr);

      Attribute locationAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.LOCATION.get()));
      String location = StaxParserUtil.getAttributeValue(locationAttr);

      IndexedEndpointType endpoint = new IndexedEndpointType(URI.create(binding), URI.create(location));
      Attribute isDefault = startElement.getAttributeByName(new QName(JBossSAMLConstants.ISDEFAULT.get()));
      if (isDefault != null)
      {
         endpoint.setIsDefault(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(isDefault)));
      }
      Attribute index = startElement.getAttributeByName(new QName(JBossSAMLConstants.INDEX.get()));
      if (index != null)
      {
         endpoint.setIndex(Integer.parseInt(StaxParserUtil.getAttributeValue(index)));
      }
      return endpoint;
   }

   private AttributeConsumingServiceType parseAttributeConsumingService(XMLEventReader xmlEventReader,
         StartElement startElement) throws ParsingException
   {
      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);

      Attribute indexAttr = startElement.getAttributeByName(new QName(JBossSAMLConstants.INDEX.get()));
      if (indexAttr == null)
         throw new ParsingException(ErrorCodes.REQD_ATTRIBUTE + "index");

      AttributeConsumingServiceType attributeConsumer = new AttributeConsumingServiceType(
            Integer.parseInt(StaxParserUtil.getAttributeValue(indexAttr)));
      while (xmlEventReader.hasNext())
      {
         XMLEvent xmlEvent = StaxParserUtil.peek(xmlEventReader);
         if (xmlEvent instanceof EndElement)
         {
            EndElement end = StaxParserUtil.getNextEndElement(xmlEventReader);
            StaxParserUtil.validate(end, JBossSAMLConstants.ATTRIBUTE_CONSUMING_SERVICE.get());
            break;
         }

         startElement = (StartElement) xmlEvent;
         String localPart = startElement.getName().getLocalPart();

         if (JBossSAMLConstants.SERVICE_NAME.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            LocalizedNameType localName = getLocalizedName(xmlEventReader, startElement);
            attributeConsumer.addServiceName(localName);
         }
         else if (JBossSAMLConstants.SERVICE_DESCRIPTION.get().equals(localPart))
         {
            startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
            LocalizedNameType localName = getLocalizedName(xmlEventReader, startElement);
            attributeConsumer.addServiceDescription(localName);
         }
         else if (JBossSAMLConstants.REQUESTED_ATTRIBUTE.get().equals(localPart))
         {
            RequestedAttributeType attType = parseRequestedAttributeType(xmlEventReader, startElement);
            attributeConsumer.addRequestedAttribute(attType);
         }
         else
            throw new RuntimeException(ErrorCodes.UNKNOWN_TAG + localPart);
      }

      return attributeConsumer;
   }

   private RequestedAttributeType parseRequestedAttributeType(XMLEventReader xmlEventReader, StartElement startElement)
         throws ParsingException
   {
      startElement = StaxParserUtil.getNextStartElement(xmlEventReader);
      StaxParserUtil.validate(startElement, JBossSAMLConstants.REQUESTED_ATTRIBUTE.get());
      RequestedAttributeType attributeType = null;

      Attribute name = startElement.getAttributeByName(new QName(JBossSAMLConstants.NAME.get()));
      if (name == null)
         throw new RuntimeException(ErrorCodes.REQD_ATTRIBUTE + "Name");
      attributeType = new RequestedAttributeType(StaxParserUtil.getAttributeValue(name));

      Attribute isRequired = startElement.getAttributeByName(new QName(JBossSAMLConstants.IS_REQUIRED.get()));
      if (isRequired != null)
      {
         attributeType.setIsRequired(Boolean.parseBoolean(StaxParserUtil.getAttributeValue(isRequired)));
      }

      SAMLParserUtil.parseAttributeType(xmlEventReader, startElement, JBossSAMLConstants.REQUESTED_ATTRIBUTE.get(),
            attributeType);
      return attributeType;
   }

   private ExtensionsType parseExtensions(XMLEventReader xmlEventReader) throws ParsingException
   {
      ExtensionsType extensions = new ExtensionsType();
      Element extElement = StaxParserUtil.getDOMElement(xmlEventReader);
      extensions.setElement(extElement);
      return extensions;
   }
}