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
 * <p>Java class for SSODescriptorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SSODescriptorType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:metadata}RoleDescriptorType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ArtifactResolutionService" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}SingleLogoutService" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ManageNameIDService" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}NameIDFormat" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public abstract class SSODescriptorType extends RoleDescriptorType
{
   protected List<IndexedEndpointType> artifactResolutionService = new ArrayList<IndexedEndpointType>();

   protected List<EndpointType> singleLogoutService = new ArrayList<EndpointType>();

   protected List<EndpointType> manageNameIDService = new ArrayList<EndpointType>();

   protected List<String> nameIDFormat = new ArrayList<String>();

   public SSODescriptorType(List<String> protocolSupport)
   {
      super(protocolSupport);
   }

   /**
    * Add SLO Service
    * @param endpt
    */
   public void addSingleLogoutService(EndpointType endpt)
   {
      this.singleLogoutService.add(endpt);
   }

   /**
    * Add atrifact resolution service
    * @param i
    */
   public void addArtifactResolutionService(IndexedEndpointType i)
   {
      this.artifactResolutionService.add(i);
   }

   /**
    * Add manage name id service
    * @param end
    */
   public void addManageNameIDService(EndpointType end)
   {
      this.manageNameIDService.add(end);
   }

   /**
    * Add Name ID Format
    * @param s
    */
   public void addNameIDFormat(String s)
   {
      this.nameIDFormat.add(s);
   }

   /**
    * remove SLO Service
    * @param endpt
    */
   public void removeSingleLogoutService(EndpointType endpt)
   {
      this.singleLogoutService.remove(endpt);
   }

   /**
    * remove atrifact resolution service
    * @param i
    */
   public void removeArtifactResolutionService(IndexedEndpointType i)
   {
      this.artifactResolutionService.remove(i);
   }

   /**
    * remove manage name id service
    * @param end
    */
   public void removeManageNameIDService(EndpointType end)
   {
      this.manageNameIDService.remove(end);
   }

   /**
    * remove Name ID Format
    * @param s
    */
   public void removeNameIDFormat(String s)
   {
      this.nameIDFormat.remove(s);
   }

   /**
    * Gets the value of the artifactResolutionService property. 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link IndexedEndpointType }
    */
   public List<IndexedEndpointType> getArtifactResolutionService()
   {
      return Collections.unmodifiableList(this.artifactResolutionService);
   }

   /**
    * Gets the value of the singleLogoutService property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EndpointType }
    */
   public List<EndpointType> getSingleLogoutService()
   {
      return Collections.unmodifiableList(this.singleLogoutService);
   }

   /**
    * Gets the value of the manageNameIDService property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EndpointType }
    */
   public List<EndpointType> getManageNameIDService()
   {
      return Collections.unmodifiableList(this.manageNameIDService);
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