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
package org.picketlink.identity.federation.saml.v2.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for AuthnAuthorityDescriptorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthnAuthorityDescriptorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:metadata}RoleDescriptorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AuthnQueryService" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}AssertionIDRequestService" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}NameIDFormat" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre> 
 */
public class AuthnAuthorityDescriptorType extends RoleDescriptorType
{
   protected List<EndpointType> authnQueryService = new ArrayList<EndpointType>();

   protected List<EndpointType> assertionIDRequestService = new ArrayList<EndpointType>();

   protected List<String> nameIDFormat = new ArrayList<String>();

   public AuthnAuthorityDescriptorType(List<String> protocolSupport)
   {
      super(protocolSupport);
   }

   /**
    * Add authn query service
    * @param endpoint
    */
   public void addAuthnQueryService(EndpointType endpoint)
   {
      this.authnQueryService.add(endpoint);
   }

   /**
    * Add assertion id request service
    * @param endpoint
    */
   public void addAssertionIDRequestService(EndpointType endpoint)
   {
      this.assertionIDRequestService.add(endpoint);
   }

   /**
    * Add name id format
    * @param str
    */
   public void addNameIDFormat(String str)
   {
      this.nameIDFormat.add(str);
   }

   /**
    * Remove authn query service
    * @param endpoint
    */
   public void removeAuthnQueryService(EndpointType endpoint)
   {
      this.authnQueryService.remove(endpoint);
   }

   /**
    * remove assertion id request service
    * @param endpoint
    */
   public void removeAssertionIDRequestService(EndpointType endpoint)
   {
      this.assertionIDRequestService.remove(endpoint);
   }

   /**
    * remove name id format
    * @param str
    */
   public void removeNameIDFormat(String str)
   {
      this.nameIDFormat.remove(str);
   }

   /**
    * Gets the value of the authnQueryService property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EndpointType }
    * 
    * 
    */
   public List<EndpointType> getAuthnQueryService()
   {
      return Collections.unmodifiableList(this.authnQueryService);
   }

   /**
    * Gets the value of the assertionIDRequestService property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EndpointType }
    * 
    * 
    */
   public List<EndpointType> getAssertionIDRequestService()
   {
      return Collections.unmodifiableList(this.assertionIDRequestService);
   }

   /**
    * Gets the value of the nameIDFormat property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    * 
    * 
    */
   public List<String> getNameIDFormat()
   {
      return Collections.unmodifiableList(this.nameIDFormat);
   }
}