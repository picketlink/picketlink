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
package org.picketlink.identity.federation.core.config;

/**
 * 
 * 				The service provider type contains information about a specific service provider. In particular,
 * 				it specifies the type of the token that must be issued for the provider and the alias of the
 * 				provider's PKC in the truststore. This is used by the STS to locate the PKC when encrypting the
 * 				generated token. 
 * 			
 * 
 * <p>Java class for ServiceProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"> 
 *       &lt;attribute name="Endpoint" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TruststoreAlias" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
       &lt;attribute name="TokenType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="TruststoreAlias" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ServiceProviderType
{

   protected String endpoint;

   protected String tokenType;

   protected String truststoreAlias;

   /**
    * Gets the value of the endpoint property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getEndpoint()
   {
      return endpoint;
   }

   /**
    * Sets the value of the endpoint property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setEndpoint(String value)
   {
      this.endpoint = value;
   }

   /**
    * Gets the value of the tokenType property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getTokenType()
   {
      return tokenType;
   }

   /**
    * Sets the value of the tokenType property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setTokenType(String value)
   {
      this.tokenType = value;
   }

   /**
    * Gets the value of the truststoreAlias property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getTruststoreAlias()
   {
      return truststoreAlias;
   }

   /**
    * Sets the value of the truststoreAlias property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setTruststoreAlias(String value)
   {
      this.truststoreAlias = value;
   }

}
