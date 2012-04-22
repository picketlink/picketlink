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

import java.net.URI;


/**
 * <p>Java class for NameIDPolicyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NameIDPolicyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Format" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="SPNameQualifier" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="AllowCreate" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class NameIDPolicyType 
{
   protected URI format; 
   protected String spNameQualifier; 
   protected Boolean allowCreate;

   /**
    * Gets the value of the format property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public URI getFormat() {
      return format;
   }

   /**
    * Sets the value of the format property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setFormat( URI value) {
      this.format = value;
   }

   /**
    * Gets the value of the spNameQualifier property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getSPNameQualifier() {
      return spNameQualifier;
   }

   /**
    * Sets the value of the spNameQualifier property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setSPNameQualifier(String value) {
      this.spNameQualifier = value;
   }

   /**
    * Gets the value of the allowCreate property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public Boolean isAllowCreate() {
      return allowCreate;
   }

   /**
    * Sets the value of the allowCreate property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setAllowCreate(Boolean value) {
      this.allowCreate = value;
   }
}