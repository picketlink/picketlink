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

import javax.xml.crypto.dsig.CanonicalizationMethod;

/**
 * <p>Java class for STSType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="STSType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KeyProvider" type="{urn:picketlink:identity-federation:config:1.0}KeyProviderType" minOccurs="0"/>
 *         &lt;element name="RequestHandler" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ClaimsProcessors" type="{urn:picketlink:identity-federation:config:1.0}ClaimsProcessorsType" minOccurs="0"/>
 *         &lt;element name="TokenProviders" type="{urn:picketlink:identity-federation:config:1.0}TokenProvidersType" minOccurs="0"/>
 *         &lt;element name="ServiceProviders" type="{urn:picketlink:identity-federation:config:1.0}ServiceProvidersType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="STSName" type="{http://www.w3.org/2001/XMLSchema}string" default="PicketLinkSTS" />
 *       &lt;attribute name="TokenTimeout" type="{http://www.w3.org/2001/XMLSchema}int" default="3600" />
 *       &lt;attribute name="SignToken" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="EncryptToken" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="CanonicalizationMethod" default="http://www.w3.org/2001/10/xml-exc-c14n#WithComments" 
 *             type="string" use="optional"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class STSType
{

   protected KeyProviderType keyProvider;

   protected String requestHandler;

   protected ClaimsProcessorsType claimsProcessors;

   protected TokenProvidersType tokenProviders;

   protected ServiceProvidersType serviceProviders;

   protected String stsName;

   protected Integer tokenTimeout;

   protected Boolean signToken;

   protected Boolean encryptToken;

   protected String canonicalizationMethod;

   /**
    * Gets the value of the keyProvider property.
    * 
    * @return
    *     possible object is
    *     {@link KeyProviderType }
    *     
    */
   public KeyProviderType getKeyProvider()
   {
      return keyProvider;
   }

   /**
    * Sets the value of the keyProvider property.
    * 
    * @param value
    *     allowed object is
    *     {@link KeyProviderType }
    *     
    */
   public void setKeyProvider(KeyProviderType value)
   {
      this.keyProvider = value;
   }

   /**
    * Gets the value of the requestHandler property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getRequestHandler()
   {
      return requestHandler;
   }

   /**
    * Sets the value of the requestHandler property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setRequestHandler(String value)
   {
      this.requestHandler = value;
   }

   /**
    * Gets the value of the claimsProcessors property.
    * 
    * @return
    *     possible object is
    *     {@link ClaimsProcessorsType }
    *     
    */
   public ClaimsProcessorsType getClaimsProcessors()
   {
      return claimsProcessors;
   }

   /**
    * Sets the value of the claimsProcessors property.
    * 
    * @param value
    *     allowed object is
    *     {@link ClaimsProcessorsType }
    *     
    */
   public void setClaimsProcessors(ClaimsProcessorsType value)
   {
      this.claimsProcessors = value;
   }

   /**
    * Gets the value of the tokenProviders property.
    * 
    * @return
    *     possible object is
    *     {@link TokenProvidersType }
    *     
    */
   public TokenProvidersType getTokenProviders()
   {
      return tokenProviders;
   }

   /**
    * Sets the value of the tokenProviders property.
    * 
    * @param value
    *     allowed object is
    *     {@link TokenProvidersType }
    *     
    */
   public void setTokenProviders(TokenProvidersType value)
   {
      this.tokenProviders = value;
   }

   /**
    * Gets the value of the serviceProviders property.
    * 
    * @return
    *     possible object is
    *     {@link ServiceProvidersType }
    *     
    */
   public ServiceProvidersType getServiceProviders()
   {
      return serviceProviders;
   }

   /**
    * Sets the value of the serviceProviders property.
    * 
    * @param value
    *     allowed object is
    *     {@link ServiceProvidersType }
    *     
    */
   public void setServiceProviders(ServiceProvidersType value)
   {
      this.serviceProviders = value;
   }

   /**
    * Gets the value of the stsName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getSTSName()
   {
      if (stsName == null)
      {
         return "PicketLinkSTS";
      }
      else
      {
         return stsName;
      }
   }

   /**
    * Sets the value of the stsName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setSTSName(String value)
   {
      this.stsName = value;
   }

   /**
    * Gets the value of the tokenTimeout property.
    * 
    * @return
    *     possible object is
    *     {@link Integer }
    *     
    */
   public int getTokenTimeout()
   {
      if (tokenTimeout == null)
      {
         return 3600;
      }
      else
      {
         return tokenTimeout;
      }
   }

   /**
    * Sets the value of the tokenTimeout property.
    * 
    * @param value
    *     allowed object is
    *     {@link Integer }
    *     
    */
   public void setTokenTimeout(Integer value)
   {
      this.tokenTimeout = value;
   }

   /**
    * Gets the value of the signToken property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public boolean isSignToken()
   {
      if (signToken == null)
      {
         return true;
      }
      else
      {
         return signToken;
      }
   }

   /**
    * Sets the value of the signToken property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setSignToken(Boolean value)
   {
      this.signToken = value;
   }

   /**
    * Gets the value of the encryptToken property.
    * 
    * @return
    *     possible object is
    *     {@link Boolean }
    *     
    */
   public boolean isEncryptToken()
   {
      if (encryptToken == null)
      {
         return false;
      }
      else
      {
         return encryptToken;
      }
   }

   /**
    * Sets the value of the encryptToken property.
    * 
    * @param value
    *     allowed object is
    *     {@link Boolean }
    *     
    */
   public void setEncryptToken(Boolean value)
   {
      this.encryptToken = value;
   }

   /**
    * Gets the value of the canonicalizationMethod property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getCanonicalizationMethod()
   {
      if (canonicalizationMethod == null)
         canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

      return canonicalizationMethod;
   }

   /**
    * Sets the value of the canonicalizationMethod property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setCanonicalizationMethod(String canonicalizationMethod)
   {
      this.canonicalizationMethod = canonicalizationMethod;
   }
}