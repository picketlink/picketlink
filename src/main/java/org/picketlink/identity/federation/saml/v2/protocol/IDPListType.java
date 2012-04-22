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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for IDPListType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IDPListType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}IDPEntry" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}GetComplete" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class IDPListType 
{
   protected List<IDPEntryType> idpEntry = new ArrayList<IDPEntryType>(); 
   protected URI getComplete;

   /**
    * Add an idp entry
    * @param entry
    */
   public void addIDPEntry( IDPEntryType entry )
   {
      this.idpEntry.add(entry);
   }
   
   /**
    * Remove an idp entry
    * @param entry
    */
   public void removeIDPEntry( IDPEntryType entry )
   {
      this.idpEntry.remove(entry);
   }

   /**
    * Gets the value of the idpEntry property. 
    * 
    */
   public List<IDPEntryType> getIDPEntry() 
   { 
      return Collections.unmodifiableList( this.idpEntry );
   }

   /**
    * Gets the value of the getComplete property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public URI getGetComplete() {
      return getComplete;
   }

   /**
    * Sets the value of the getComplete property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setGetComplete( URI value) {
      this.getComplete = value;
   }

}