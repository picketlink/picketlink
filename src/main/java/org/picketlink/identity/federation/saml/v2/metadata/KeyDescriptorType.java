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

import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmlenc.EncryptionMethodType;
import org.w3c.dom.Element;

/**
 * <p>Java class for KeyDescriptorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KeyDescriptorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}KeyInfo"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}EncryptionMethod" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="use" type="{urn:oasis:names:tc:SAML:2.0:metadata}KeyTypes" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class KeyDescriptorType
{
   protected Element keyInfo;

   protected List<EncryptionMethodType> encryptionMethod = new ArrayList<EncryptionMethodType>();

   protected KeyTypes use;

   /**
    * Gets the value of the keyInfo property.
    * 
    * @return
    *     possible object is
    *     {@link KeyInfoType }
    *     
    */
   public Element getKeyInfo()
   {
      return keyInfo;
   }

   /**
    * Sets the value of the keyInfo property.
    * 
    * @param value
    *     allowed object is
    *     {@link KeyInfoType }
    *     
    */
   public void setKeyInfo(Element value)
   {
      this.keyInfo = value;
   }

   /**
    * Add encryption method type
    * @param e
    */
   public void addEncryptionMethod(EncryptionMethodType e)
   {
      this.encryptionMethod.add(e);
   }

   /**
    * Remove encryption method type
    * @param e
    */
   public void removeEncryptionMethod(EncryptionMethodType e)
   {
      this.encryptionMethod.remove(e);
   }

   /**
    * Gets the value of the encryptionMethod property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link EncryptionMethodType }
    * 
    * 
    */
   public List<EncryptionMethodType> getEncryptionMethod()
   {
      return Collections.unmodifiableList(this.encryptionMethod);
   }

   /**
    * Gets the value of the use property.
    * 
    * @return
    *     possible object is
    *     {@link KeyTypes }
    *     
    */
   public KeyTypes getUse()
   {
      return use;
   }

   /**
    * Sets the value of the use property.
    * 
    * @param value
    *     allowed object is
    *     {@link KeyTypes }
    *     
    */
   public void setUse(KeyTypes value)
   {
      this.use = value;
   }
}