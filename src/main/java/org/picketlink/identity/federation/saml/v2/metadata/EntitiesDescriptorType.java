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

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

/**
 * <p>Java class for EntitiesDescriptorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EntitiesDescriptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Signature" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}Extensions" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}EntityDescriptor"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}EntitiesDescriptor"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="validUntil" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="cacheDuration" type="{http://www.w3.org/2001/XMLSchema}duration" />
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class EntitiesDescriptorType
{
   protected Element signature;

   protected ExtensionsType extensions;

   protected List<Object> entityDescriptor = new ArrayList<Object>();

   protected XMLGregorianCalendar validUntil;

   protected Duration cacheDuration;

   protected String id;

   protected String name;

   /**
    * Gets the value of the signature property.
    * 
    * @return
    *     possible object is
    *     {@link Element }
    *     
    */
   public Element getSignature()
   {
      return signature;
   }

   /**
    * Sets the value of the signature property.
    * 
    * @param value
    *     allowed object is
    *     {@link Element }
    *     
    */
   public void setSignature(Element value)
   {
      this.signature = value;
   }

   /**
    * Gets the value of the extensions property.
    * 
    * @return
    *     possible object is
    *     {@link ExtensionsType }
    *     
    */
   public ExtensionsType getExtensions()
   {
      return extensions;
   }

   /**
    * Sets the value of the extensions property.
    * 
    * @param value
    *     allowed object is
    *     {@link ExtensionsType }
    *     
    */
   public void setExtensions(ExtensionsType value)
   {
      this.extensions = value;
   }

   /**
    * Add an entity descriptor
    * @param obj
    */
   public void addEntityDescriptor(Object obj)
   {
      this.entityDescriptor.add(obj);
   }

   /**
    * Remove an entity descriptor
    * @param obj
    */
   public void removeEntityDescriptor(Object obj)
   {
      this.entityDescriptor.remove(obj);
   }

   /**
    * Gets the value of the entityDescriptorOrEntitiesDescriptor property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EntitiesDescriptorType }
    * {@link EntityDescriptorType }
    * 
    * 
    */
   public List<Object> getEntityDescriptor()
   {
      return Collections.unmodifiableList(this.entityDescriptor);
   }

   /**
    * Gets the value of the validUntil property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getValidUntil()
   {
      return validUntil;
   }

   /**
    * Sets the value of the validUntil property.
    * 
    * @param value
    *     allowed object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public void setValidUntil(XMLGregorianCalendar value)
   {
      this.validUntil = value;
   }

   /**
    * Gets the value of the cacheDuration property.
    * 
    * @return
    *     possible object is
    *     {@link Duration }
    *     
    */
   public Duration getCacheDuration()
   {
      return cacheDuration;
   }

   /**
    * Sets the value of the cacheDuration property.
    * 
    * @param value
    *     allowed object is
    *     {@link Duration }
    *     
    */
   public void setCacheDuration(Duration value)
   {
      this.cacheDuration = value;
   }

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getID()
   {
      return id;
   }

   /**
    * Sets the value of the id property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setID(String value)
   {
      this.id = value;
   }

   /**
    * Gets the value of the name property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getName()
   {
      return name;
   }

   /**
    * Sets the value of the name property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setName(String value)
   {
      this.name = value;
   }
}