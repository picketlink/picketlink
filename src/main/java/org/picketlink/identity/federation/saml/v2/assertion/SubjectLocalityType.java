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
package org.picketlink.identity.federation.saml.v2.assertion;

import java.io.Serializable;

/**
 * <p>Java class for SubjectLocalityType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubjectLocalityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="Address" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="DNSName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class SubjectLocalityType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected String address;

   protected String dnsName;

   /**
    * Gets the value of the address property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getAddress()
   {
      return address;
   }

   /**
    * Sets the value of the address property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setAddress(String value)
   {
      this.address = value;
   }

   /**
    * Gets the value of the dnsName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getDNSName()
   {
      return dnsName;
   }

   /**
    * Sets the value of the dnsName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setDNSName(String value)
   {
      this.dnsName = value;
   }
}