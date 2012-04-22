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
 * <p>Java class for AttributeConsumingServiceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeConsumingServiceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ServiceName" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}ServiceDescription" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}RequestedAttribute" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="index" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="isDefault" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class AttributeConsumingServiceType
{
   protected List<LocalizedNameType> serviceName = new ArrayList<LocalizedNameType>();

   protected List<LocalizedNameType> serviceDescription = new ArrayList<LocalizedNameType>();

   protected List<RequestedAttributeType> requestedAttribute = new ArrayList<RequestedAttributeType>();

   protected int index;

   protected Boolean isDefault;

   public AttributeConsumingServiceType(int index)
   {
      this.index = index;
   }

   /**
    * Add serviceName  
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public void addServiceName(LocalizedNameType service)
   {
      this.serviceName.add(service);
   }

   /**
    * Add serviceDescription.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public void addServiceDescription(LocalizedNameType desc)
   {
      this.serviceDescription.add(desc);
   }

   /**
    * Add requestedAttribute 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link RequestedAttributeType }
    * 
    */
   public void addRequestedAttribute(RequestedAttributeType req)
   {
      this.requestedAttribute.add(req);
   }

   /**
    * remove serviceName  
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public void removeServiceName(LocalizedNameType service)
   {
      this.serviceName.remove(service);
   }

   /**
    * remove serviceDescription.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public void removeServiceDescription(LocalizedNameType desc)
   {
      this.serviceDescription.remove(desc);
   }

   /**
    * remove requestedAttribute 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link RequestedAttributeType }
    * 
    */
   public void removeRequestedAttribute(RequestedAttributeType req)
   {
      this.requestedAttribute.remove(req);
   }

   /**
    * Gets the value of the serviceName property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public List<LocalizedNameType> getServiceName()
   {
      return Collections.unmodifiableList(this.serviceName);
   }

   /**
    * Gets the value of the serviceDescription property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public List<LocalizedNameType> getServiceDescription()
   {
      return Collections.unmodifiableList(this.serviceDescription);
   }

   /**
    * Gets the value of the requestedAttribute property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link RequestedAttributeType }
    * 
    * 
    */
   public List<RequestedAttributeType> getRequestedAttribute()
   {
      return Collections.unmodifiableList(this.requestedAttribute);
   }

   /**
    * Gets the value of the index property.
    * 
    */
   public int getIndex()
   {
      return index;
   }

   /**
    * Gets the value of the isDefault property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean isIsDefault()
   {
      return isDefault;
   }

   /**
    * Sets the value of the isDefault property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setIsDefault(Boolean value)
   {
      this.isDefault = value;
   }
}