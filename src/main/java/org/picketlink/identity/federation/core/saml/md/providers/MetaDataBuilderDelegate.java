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
package org.picketlink.identity.federation.core.saml.md.providers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedNameType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedURIType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;

/**
 * SAML2 Metadata Builder API
 * @author Anil.Saldhana@redhat.com
 * @since Apr 19, 2009
 */
public class MetaDataBuilderDelegate
{
   /**
    * Create an Endpoint (SingleSignOnEndpoint or SingleLogoutEndpoint)
    * @param binding
    * @param location
    * @param responseLocation
    * @return
    */
   public static EndpointType createEndpoint(String binding, String location, String responseLocation)
   {
      EndpointType endpoint = new EndpointType(URI.create(binding), URI.create(location));
      endpoint.setResponseLocation(URI.create(responseLocation));
      return endpoint;
   }

   /**
    * Create an Organization
    * @param organizationName
    * @param organizationDisplayName
    * @param organizationURL
    * @param lang
    * @return
    */
   public static OrganizationType createOrganization(String organizationName, String organizationDisplayName,
         String organizationURL, String lang)
   {
      if (organizationName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "organizationName");
      if (organizationDisplayName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "organizationDisplayName");
      if (organizationURL == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "organizationURL");
      if (lang == null)
         lang = JBossSAMLConstants.LANG_EN.get();

      //orgName
      LocalizedNameType orgName = new LocalizedNameType(lang);
      orgName.setValue(organizationName);

      //orgDisplayName
      LocalizedNameType orgDisplayName = new LocalizedNameType(lang);
      orgDisplayName.setValue(organizationDisplayName);

      //orgURL
      LocalizedURIType orgURL = new LocalizedURIType(lang);
      orgURL.setValue(URI.create(organizationURL));

      OrganizationType orgType = new OrganizationType();
      orgType.addOrganizationName(orgName);
      orgType.addOrganizationDisplayName(orgDisplayName);
      orgType.addOrganizationURL(orgURL);
      return orgType;
   }

   /**
    * Create an Entity Descriptor
    * @param idpOrSPDescriptor a descriptor for either the IDP or SSO
    * @return
    */
   public static EntityDescriptorType createEntityDescriptor(SSODescriptorType idpOrSPDescriptor)
   {
      EDTDescriptorChoiceType edtDescriptorChoiceType = new EDTDescriptorChoiceType(idpOrSPDescriptor);

      List<EDTDescriptorChoiceType> edtList = new ArrayList<EntityDescriptorType.EDTDescriptorChoiceType>();
      edtList.add(edtDescriptorChoiceType);

      EDTChoiceType choiceType = new EDTChoiceType(edtList);

      EntityDescriptorType entity = new EntityDescriptorType(" ");
      entity.addChoiceType(choiceType);
      return entity;
   }

   /**
    * Create a IDP SSO metadata descriptor
    * @param requestsSigned
    * @param keyDescriptorType
    * @param ssoEndPoint
    * @param sloEndPoint
    * @param attributes
    * @param org
    * @return
    */
   public static IDPSSODescriptorType createIDPSSODescriptor(boolean requestsSigned,
         KeyDescriptorType keyDescriptorType, EndpointType ssoEndPoint, EndpointType sloEndPoint,
         List<AttributeType> attributes, OrganizationType org)
   {
      List<String> emptyList = new ArrayList<String>();
      IDPSSODescriptorType idp = new IDPSSODescriptorType(emptyList);
      idp.addSingleSignOnService(ssoEndPoint);
      idp.addSingleLogoutService(sloEndPoint);

      for (AttributeType attr : attributes)
      {
         idp.addAttribute(attr);
      }
      idp.addKeyDescriptor(keyDescriptorType);
      idp.setWantAuthnRequestsSigned(requestsSigned);
      idp.setOrganization(org);
      return idp;
   }

   /**
    * Create a IDP SSO metadata descriptor
    * @param requestsSigned
    * @param keyDescriptorType
    * @param ssoEndPoint
    * @param sloEndPoint
    * @param attributes
    * @param org
    * @return
    */
   public static SPSSODescriptorType createSPSSODescriptor(boolean requestsSigned, KeyDescriptorType keyDescriptorType,
         EndpointType sloEndPoint, List<AttributeType> attributes, OrganizationType org)
   {
      List<String> protocolEnumList = new ArrayList<String>();
      protocolEnumList.add(JBossSAMLURIConstants.PROTOCOL_NSURI.get());

      SPSSODescriptorType sp = new SPSSODescriptorType(protocolEnumList);
      sp.addSingleLogoutService(sloEndPoint);
      sp.addKeyDescriptor(keyDescriptorType);
      sp.setAuthnRequestsSigned(requestsSigned);
      sp.setOrganization(org);
      return sp;
   }
}