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

/**
 * <p>Java class for OrganizationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OrganizationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}Extensions" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}OrganizationName" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}OrganizationDisplayName" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:metadata}OrganizationURL" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

public class OrganizationType extends TypeWithOtherAttributes
{

   protected ExtensionsType extensions;

   protected List<LocalizedNameType> organizationName = new ArrayList<LocalizedNameType>();

   protected List<LocalizedNameType> organizationDisplayName = new ArrayList<LocalizedNameType>();

   protected List<LocalizedURIType> organizationURL = new ArrayList<LocalizedURIType>();

   /**
    * Add an organization name
    * @param name
    */
   public void addOrganizationName(LocalizedNameType name)
   {
      this.organizationName.add(name);
   }

   /**
    * Add organization display name
    * @param name
    */
   public void addOrganizationDisplayName(LocalizedNameType name)
   {
      this.organizationDisplayName.add(name);
   }

   /**
    * Add organization url
    * @param uri
    */
   public void addOrganizationURL(LocalizedURIType uri)
   {
      this.organizationURL.add(uri);
   }

   /**
    * remove an organization name
    * @param name
    */
   public void removeOrganizationName(LocalizedNameType name)
   {
      this.organizationName.remove(name);
   }

   /**
    * remove organization display name
    * @param name
    */
   public void removeOrganizationDisplayName(LocalizedNameType name)
   {
      this.organizationDisplayName.remove(name);
   }

   /**
    * remove organization url
    * @param uri
    */
   public void removeOrganizationURL(LocalizedURIType uri)
   {
      this.organizationURL.remove(uri);
   }

   /**
    * Gets the value of the extensions property.
    * 
    * @return
    *     possible object is
    *     {@link ExtensionsType }
    *     
    */
   public ExtensionsType getExtensions()
   {
      return extensions;
   }

   /**
    * Sets the value of the extensions property.
    * 
    * @param value
    *     allowed object is
    *     {@link ExtensionsType }
    *     
    */
   public void setExtensions(ExtensionsType value)
   {
      this.extensions = value;
   }

   /**
    * Gets the value of the organizationName property.
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    *  
    */
   public List<LocalizedNameType> getOrganizationName()
   {
      return Collections.unmodifiableList(this.organizationName);
   }

   /**
    * Gets the value of the organizationDisplayName property.
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link LocalizedNameType }
    * 
    * 
    */
   public List<LocalizedNameType> getOrganizationDisplayName()
   {
      return Collections.unmodifiableList(this.organizationDisplayName);
   }

   /**
    * Gets the value of the organizationURL property.
    * 

    */
   public List<LocalizedURIType> getOrganizationURL()
   {
      return Collections.unmodifiableList(this.organizationURL);
   }
}