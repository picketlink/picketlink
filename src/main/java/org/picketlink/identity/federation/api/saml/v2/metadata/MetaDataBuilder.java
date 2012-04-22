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
package org.picketlink.identity.federation.api.saml.v2.metadata;

import java.util.List;

import org.picketlink.identity.federation.core.saml.md.providers.MetaDataBuilderDelegate;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;


/**
 * SAML2 Metadata Builder API
 * @author Anil.Saldhana@redhat.com
 * @since Apr 19, 2009
 */
public class MetaDataBuilder
{ 
   /**
    * Create an Endpoint (SingleSignOnEndpoint or SingleLogoutEndpoint)
    * @param binding
    * @param location
    * @param responseLocation
    * @return
    */
   public static EndpointType createEndpoint(String binding, String location,
         String responseLocation)
   {
      return MetaDataBuilderDelegate.createEndpoint(binding, location, responseLocation); 
   }
   
   /**
    * Create an Organization
    * @param organizationName
    * @param organizationDisplayName
    * @param organizationURL
    * @param lang
    * @return
    */
   public static OrganizationType createOrganization(String organizationName,
         String organizationDisplayName, String organizationURL, String lang)
   {
      return MetaDataBuilderDelegate.createOrganization(organizationName, organizationDisplayName, organizationURL, lang);    
   }
   
   /**
    * Create an Entity Descriptor
    * @param idpOrSPDescriptor a descriptor for either the IDP or SSO
    * @return
    */
   public static EntityDescriptorType createEntityDescriptor(SSODescriptorType idpOrSPDescriptor)
   {
      return MetaDataBuilderDelegate.createEntityDescriptor(idpOrSPDescriptor); 
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
         KeyDescriptorType keyDescriptorType, 
         EndpointType ssoEndPoint, 
         EndpointType sloEndPoint,
         List<AttributeType> attributes,
         OrganizationType org)
   {
      return MetaDataBuilderDelegate.createIDPSSODescriptor(requestsSigned, 
                     keyDescriptorType, ssoEndPoint, sloEndPoint, attributes, org);       
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
   public static SPSSODescriptorType createSPSSODescriptor(boolean requestsSigned, 
         KeyDescriptorType keyDescriptorType,  
         EndpointType sloEndPoint,
         List<AttributeType> attributes,
         OrganizationType org)
   {
      return MetaDataBuilderDelegate.createSPSSODescriptor(requestsSigned, keyDescriptorType, 
            sloEndPoint, attributes, org);     
   } 
}