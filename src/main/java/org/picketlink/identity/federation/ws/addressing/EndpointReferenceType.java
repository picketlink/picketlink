/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.ws.addressing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

/**
 * <p>Java class for EndpointReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EndpointReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Address" type="{http://www.w3.org/2005/08/addressing}AttributedURIType"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}ReferenceParameters" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/08/addressing}Metadata" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class EndpointReferenceType extends BaseAddressingType
{
   protected AttributedURIType address;

   protected ReferenceParametersType referenceParameters;

   protected MetadataType metadata;

   protected List<Object> any = new ArrayList<Object>();

   /**
    * Gets the value of the address property.
    * 
    * @return
    *     possible object is
    *     {@link AttributedURIType }
    *     
    */
   public AttributedURIType getAddress()
   {
      return address;
   }

   /**
    * Sets the value of the address property.
    * 
    * @param value
    *     allowed object is
    *     {@link AttributedURIType }
    *     
    */
   public void setAddress(AttributedURIType value)
   {
      this.address = value;
   }

   /**
    * Gets the value of the referenceParameters property.
    * 
    * @return
    *     possible object is
    *     {@link ReferenceParametersType }
    *     
    */
   public ReferenceParametersType getReferenceParameters()
   {
      return referenceParameters;
   }

   /**
    * Sets the value of the referenceParameters property.
    * 
    * @param value
    *     allowed object is
    *     {@link ReferenceParametersType }
    *     
    */
   public void setReferenceParameters(ReferenceParametersType value)
   {
      this.referenceParameters = value;
   }

   /**
    * Gets the value of the metadata property.
    * 
    * @return
    *     possible object is
    *     {@link MetadataType }
    *     
    */
   public MetadataType getMetadata()
   {
      return metadata;
   }

   /**
    * Sets the value of the metadata property.
    * 
    * @param value
    *     allowed object is
    *     {@link MetadataType }
    *     
    */
   public void setMetadata(MetadataType value)
   {
      this.metadata = value;
   }

   /**
    * Gets the value of the any property. 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Object }
    * {@link Element }
    * 
    * 
    */
   public List<Object> getAny()
   {
      return Collections.unmodifiableList(this.any);
   }

   /**
    * Add an any
    * @param obj
    */
   public void addAny(Object obj)
   {
      this.any.add(obj);
   }
}