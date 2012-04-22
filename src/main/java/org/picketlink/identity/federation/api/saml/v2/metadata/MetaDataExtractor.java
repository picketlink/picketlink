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

import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.List;
 
import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.SAMLMetadataUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;
 

/**
 * Extract useful information out of metadata
 * @author Anil.Saldhana@redhat.com
 * @since Apr 29, 2009
 */
public class MetaDataExtractor
{
   public static String LINE_SEPARATOR = SecurityActions.getSystemProperty("line.separator", 
         "\n");
   
   /**
    * Get the {@link X509Certificate} from the KeyInfo
    * @param keyDescriptor
    * @return
    */
   public static X509Certificate getCertificate( KeyDescriptorType keyDescriptor )
   {
      try
      {
         return SAMLMetadataUtil.getCertificate(keyDescriptor);
      }
      catch ( Exception e)
      { 
         throw new RuntimeException( e );
      }  
   }
   
   /**
    * Generate a string from the information in the metadata
    * @param edt
    * @return
    */
   public static String toString(EntityDescriptorType edt)
   {
      StringWriter sw = new StringWriter();
      try
      {
         XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(sw );
         
         SAMLMetadataWriter metaWriter = new SAMLMetadataWriter(writer);
         metaWriter.writeEntityDescriptor(edt);
      }
      catch (ProcessingException e)
      { 
         throw new RuntimeException( e );
      }
      
      return sw.toString();
      
      /*StringBuilder builder = new StringBuilder();
       List<RoleDescriptorType> rolesD = edt.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor();
      
      for(RoleDescriptorType rdt: rolesD)
      {
         builder.append("ID=").append(rdt.getID());
         builder.append(LINE_SEPARATOR);
         
         if(rdt instanceof IDPSSODescriptorType)
         {
            IDPSSODescriptorType idp = (IDPSSODescriptorType) rdt;
            builder.append(toString(idp));
         }
         if(rdt instanceof SPSSODescriptorType)
         {
            SPSSODescriptorType sp = (SPSSODescriptorType) rdt;
            builder.append(toString(sp));
         } 
      }
      
      return builder.toString();*/
   }
   
   /**
    * Information from the IDP SSO Descriptor
    * @param idp
    * @return
    */
   public static String toString(IDPSSODescriptorType idp)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(LINE_SEPARATOR);
      
      //Get the SSODescriptor tags
      SSODescriptorType sdt = idp;
      builder.append(toString(sdt));
      
      List<EndpointType> ssoServices = idp.getSingleSignOnService();
      if(ssoServices != null)
      {
         builder.append("Single Sign On Services are:[");
         
         for(EndpointType edt: ssoServices)
         {
            builder.append(toString(edt));
         }
         builder.append("]");
         builder.append(LINE_SEPARATOR);
      }
      return builder.toString();
   }
   
   /**
    * Information from the SP SSO Descriptor
    * @param sp
    * @return
    */
   public static String toString(SPSSODescriptorType sp)
   {
      StringBuilder builder = new StringBuilder();
      builder.append(LINE_SEPARATOR);
      
      //Get the SSODescriptor tags
      SSODescriptorType sdt = sp;
      builder.append(toString(sdt));
      
      List<IndexedEndpointType> assertionConsumerServices = sp.getAssertionConsumerService();
      if(assertionConsumerServices != null)
      {
         builder.append("AssertionConsumer Services are:[");
         
         for(IndexedEndpointType edt: assertionConsumerServices)
         {
            builder.append(toString(edt));
         }
         builder.append("]");
         builder.append(LINE_SEPARATOR);
      }
      
      builder.append("AuthnRequests Signed=").append(sp.isAuthnRequestsSigned());
      builder.append(LINE_SEPARATOR);
      builder.append("Requires Assertions Signed=").append(sp.isWantAssertionsSigned());
      builder.append(LINE_SEPARATOR);
      
      return builder.toString();
   }
   
   /**
    * Information from the general SSO descriptor
    * @param sso
    * @return
    */
   public static String toString(SSODescriptorType sso)
   {
      StringBuilder builder = new StringBuilder();
      List<String> nameIDs = sso.getNameIDFormat();
      if(nameIDs != null)
      {
         for(String nameID: nameIDs)
         {
            builder.append("NameID=").append(nameID);
            builder.append(LINE_SEPARATOR);
         }
      }
      
      List<IndexedEndpointType> attrResServices = sso.getArtifactResolutionService();
      if(attrResServices != null)
      {
         builder.append("AttributeResolutionServices are:[");
         builder.append(LINE_SEPARATOR);
         for(IndexedEndpointType iet : attrResServices)
         {
            builder.append(toString(iet)); 
         }
         builder.append("]");
      }
       
      List<EndpointType> sloServices = sso.getSingleLogoutService();
      if(sloServices != null)
      {
         builder.append("Single Logout Services are:[");
         builder.append(LINE_SEPARATOR);
         
         for(EndpointType edt: sloServices)
         {
            builder.append(toString(edt));
         }
         builder.append("]");
         builder.append(LINE_SEPARATOR);
      }
      return builder.toString();
   }
   
   /**
    * Information from an endpoint
    * @param ept
    * @return
    */
   public static String toString(EndpointType ept)
   {
      StringBuilder builder = new StringBuilder();
      builder.append("[Location=").append(ept.getLocation());
      
      builder.append(",ResponseLocation=").append(ept.getResponseLocation());
      builder.append("]");
      builder.append(LINE_SEPARATOR);
      return builder.toString();
   } 
}