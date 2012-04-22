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
 * <p>Java class for IDPEntryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IDPEntryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="ProviderID" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Loc" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class IDPEntryType 
{
   protected URI providerID; 
   protected String name; 
   protected String loc;

   /**
    * Gets the value of the providerID property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public URI getProviderID() 
   {
      return providerID;
   }

   /**
    * Sets the value of the providerID property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setProviderID( URI value) 
   {
      this.providerID = value;
   }

   /**
    * Gets the value of the name property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getName() {
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
   public void setName(String value) {
      this.name = value;
   }

   /**
    * Gets the value of the loc property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getLoc() {
      return loc;
   }

   /**
    * Sets the value of the loc property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setLoc(String value) {
      this.loc = value;
   }

}