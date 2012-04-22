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
package org.picketlink.identity.xmlsec.w3.xmldsig;


/**
 * <p>Java class for ReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transforms" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestMethod"/>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}DigestValue"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class ReferenceType 
{
   protected TransformsType transforms;
   protected DigestMethodType digestMethod;
   protected byte[] digestValue;
   protected String id;
   protected String uri;
   protected String type;

   /**
    * Gets the value of the transforms property.
    * 
    * @return
    *     possible object is
    *     {@link TransformsType }
    *     
    */
   public TransformsType getTransforms() {
      return transforms;
   }

   /**
    * Sets the value of the transforms property.
    * 
    * @param value
    *     allowed object is
    *     {@link TransformsType }
    *     
    */
   public void setTransforms(TransformsType value) {
      this.transforms = value;
   }

   /**
    * Gets the value of the digestMethod property.
    * 
    * @return
    *     possible object is
    *     {@link DigestMethodType }
    *     
    */
   public DigestMethodType getDigestMethod() {
      return digestMethod;
   }

   /**
    * Sets the value of the digestMethod property.
    * 
    * @param value
    *     allowed object is
    *     {@link DigestMethodType }
    *     
    */
   public void setDigestMethod(DigestMethodType value) {
      this.digestMethod = value;
   }

   /**
    * Gets the value of the digestValue property.
    * 
    * @return
    *     possible object is
    *     byte[]
    */
   public byte[] getDigestValue() {
      return digestValue;
   }

   /**
    * Sets the value of the digestValue property.
    * 
    * @param value
    *     allowed object is
    *     byte[]
    */
   public void setDigestValue(byte[] value) {
      this.digestValue = ((byte[]) value);
   }

   /**
    * Gets the value of the id property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getId() {
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
   public void setId(String value) {
      this.id = value;
   }

   /**
    * Gets the value of the uri property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getURI() {
      return uri;
   }

   /**
    * Sets the value of the uri property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setURI(String value) {
      this.uri = value;
   }

   /**
    * Gets the value of the type property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getType() {
      return type;
   }

   /**
    * Sets the value of the type property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setType(String value) {
      this.type = value;
   }
}