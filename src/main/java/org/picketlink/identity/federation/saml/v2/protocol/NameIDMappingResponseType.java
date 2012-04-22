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

import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;

/**
 * <p>Java class for NameIDMappingResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NameIDMappingResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class NameIDMappingResponseType extends StatusResponseType
{
   private static final long serialVersionUID = 1L;

   protected NameIDType nameID;

   protected EncryptedElementType encryptedID;

   public NameIDMappingResponseType(String id, XMLGregorianCalendar issueInstant)
   {
      super(id, issueInstant);
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
}