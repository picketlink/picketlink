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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 *     			Source of the Signing and Validating Key
 *     		
 * 
 * <p>Java class for KeyProviderType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KeyProviderType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Auth" type="{urn:picketlink:identity-federation:config:1.0}AuthPropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ValidatingAlias" type="{urn:picketlink:identity-federation:config:1.0}KeyValueType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SigningAlias" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ClassName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class KeyProviderType
{

   protected List<AuthPropertyType> auth = new ArrayList<AuthPropertyType>();

   protected List<KeyValueType> validatingAlias = new ArrayList<KeyValueType>();

   protected String signingAlias;

   protected String className;

   public void add(AuthPropertyType kv)
   {
      this.auth.add(kv);
   }

   public void remove(AuthPropertyType kv)
   {
      this.auth.remove(kv);
   }

   /**
    * Gets the value of the auth property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link AuthPropertyType }
    * 
    * 
    */
   public List<AuthPropertyType> getAuth()
   {
      return Collections.unmodifiableList(this.auth);
   }

   public void add(KeyValueType kv)
   {
      this.validatingAlias.add(kv);
   }

   public void remove(KeyValueType kv)
   {
      this.validatingAlias.remove(kv);
   }

   /**
    * Gets the value of the validatingAlias property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link KeyValueType }
    * 
    * 
    */
   public List<KeyValueType> getValidatingAlias()
   {
      return Collections.unmodifiableList(this.validatingAlias);
   }

   /**
    * Gets the value of the signingAlias property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getSigningAlias()
   {
      return signingAlias;
   }

   /**
    * Sets the value of the signingAlias property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setSigningAlias(String value)
   {
      this.signingAlias = value;
   }

   /**
    * Gets the value of the className property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getClassName()
   {
      return className;
   }

   /**
    * Sets the value of the className property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setClassName(String value)
   {
      this.className = value;
   }

}
