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
package org.picketlink.identity.federation.saml.v2.protocol;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.BaseIDAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * <p>Java class for NameIDMappingRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NameIDMappingRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}NameIDPolicy"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class NameIDMappingRequestType extends RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected BaseIDAbstractType baseID;

   protected NameIDType nameID;

   protected EncryptedElementType encryptedID;

   protected NameIDPolicyType nameIDPolicy;

   public NameIDMappingRequestType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Gets the value of the baseID property.
    * 
    * @return
    *     possible object is
    *     {@link BaseIDAbstractType }
    *     
    */
   public BaseIDAbstractType getBaseID()
   {
      return baseID;
   }

   /**
    * Sets the value of the baseID property.
    * 
    * @param value
    *     allowed object is
    *     {@link BaseIDAbstractType }
    *     
    */
   public void setBaseID(BaseIDAbstractType value)
   {
      this.baseID = value;
   }

   /**
    * Gets the value of the nameID property.
    * 
    * @return
    *     possible object is
    *     {@link NameIDType }
    *     
    */
   public NameIDType getNameID()
   {
      return nameID;
   }

   /**
    * Sets the value of the nameID property.
    * 
    * @param value
    *     allowed object is
    *     {@link NameIDType }
    *     
    */
   public void setNameID(NameIDType value)
   {
      this.nameID = value;
   }

   /**
    * Gets the value of the encryptedID property.
    * 
    * @return
    *     possible object is
    *     {@link EncryptedElementType }
    *     
    */
   public EncryptedElementType getEncryptedID()
   {
      return encryptedID;
   }

   /**
    * Sets the value of the encryptedID property.
    * 
    * @param value
    *     allowed object is
    *     {@link EncryptedElementType }
    *     
    */
   public void setEncryptedID(EncryptedElementType value)
   {
      this.encryptedID = value;
   }

   /**
    * Gets the value of the nameIDPolicy property.
    * 
    * @return
    *     possible object is
    *     {@link NameIDPolicyType }
    *     
    */
   public NameIDPolicyType getNameIDPolicy()
   {
      return nameIDPolicy;
   }

   /**
    * Sets the value of the nameIDPolicy property.
    * 
    * @param value
    *     allowed object is
    *     {@link NameIDPolicyType }
    *     
    */
   public void setNameIDPolicy(NameIDPolicyType value)
   {
      this.nameIDPolicy = value;
   }
}