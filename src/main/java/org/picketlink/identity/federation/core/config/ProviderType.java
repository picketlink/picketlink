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

import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * Base Type for IDP and SP
 * 
 * <p>Java class for ProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IdentityURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Trust" type="{urn:picketlink:identity-federation:config:1.0}TrustType" minOccurs="0"/>
 *         &lt;element name="KeyProvider" type="{urn:picketlink:identity-federation:config:1.0}KeyProviderType" minOccurs="0"/>
 *         &lt;element name="MetaDataProvider" type="{urn:picketlink:identity-federation:config:1.0}MetadataProviderType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ServerEnvironment" default="picketlink">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="picketlink"/>
 *             &lt;enumeration value="TOMCAT"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       
        &lt;attribute name="CanonicalizationMethod" use="optional" default="http://www.w3.org/2001/10/xml-exc-c14n#WithComments"
                   type="string"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ProviderType
{
   protected String identityURL;

   protected TrustType trust;

   protected KeyProviderType keyProvider;

   protected MetadataProviderType metaDataProvider;

   protected String serverEnvironment;

   protected String canonicalizationMethod = CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS;

   protected Map<String, Object> additionalOptions = new HashMap<String, Object>();

   /**
    * Gets the value of the identityURL property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getIdentityURL()
   {
      return identityURL;
   }

   /**
    * Sets the value of the identityURL property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setIdentityURL(String value)
   {
      this.identityURL = value;
   }

   /**
    * Gets the value of the trust property.
    * 
    * @return
    *     possible object is
    *     {@link TrustType }
    *     
    */
   public TrustType getTrust()
   {
      return trust;
   }

   /**
    * Sets the value of the trust property.
    * 
    * @param value
    *     allowed object is
    *     {@link TrustType }
    *     
    */
   public void setTrust(TrustType value)
   {
      this.trust = value;
   }

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
    * Gets the value of the metaDataProvider property.
    * 
    * @return
    *     possible object is
    *     {@link MetadataProviderType }
    *     
    */
   public MetadataProviderType getMetaDataProvider()
   {
      return metaDataProvider;
   }

   /**
    * Sets the value of the metaDataProvider property.
    * 
    * @param value
    *     allowed object is
    *     {@link MetadataProviderType }
    *     
    */
   public void setMetaDataProvider(MetadataProviderType value)
   {
      this.metaDataProvider = value;
   }

   /**
    * Gets the value of the serverEnvironment property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getServerEnvironment()
   {
      if (serverEnvironment == null)
      {
         return "picketlink";
      }
      else
      {
         return serverEnvironment;
      }
   }

   /**
    * Sets the value of the serverEnvironment property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setServerEnvironment(String value)
   {
      this.serverEnvironment = value;
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

   /**
    * Add an option
    * @param key
    * @param value
    */
   public void addAdditionalOption(String key, Object value)
   {
      additionalOptions.put(key, value);
   }

   /**
    * Remove an option
    * @param key
    */
   public void removeAdditionalOption(String key)
   {
      additionalOptions.remove(key);
   }

   /**
    * Get option
    * @param key
    * @return
    */
   public Object getAdditionalOption(String key)
   {
      return additionalOptions.get(key);
   }

   /**
    * Import values from another {@link IDPType}
    * @param other
    */
   public void importFrom(ProviderType other)
   {
      KeyProviderType keyProvider = other.getKeyProvider();
      if (keyProvider != null)
      {
         setKeyProvider(keyProvider);
      }

      String can = other.getCanonicalizationMethod();
      if (StringUtil.isNotNull(can))
      {
         setCanonicalizationMethod(can);
      }

      trust = other.getTrust();

      additionalOptions.putAll(other.additionalOptions);
   }
}