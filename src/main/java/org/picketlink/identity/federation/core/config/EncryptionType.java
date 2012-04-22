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
 * <p>Java class for EncryptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncryptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="EncAlgo" type="{urn:picketlink:identity-federation:config:1.0}EncAlgoType"/>
 *         &lt;element name="KeySize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class EncryptionType
{

   protected EncAlgoType encAlgo;

   protected int keySize;

   /**
    * Gets the value of the encAlgo property.
    * 
    * @return
    *     possible object is
    *     {@link EncAlgoType }
    *     
    */
   public EncAlgoType getEncAlgo()
   {
      return encAlgo;
   }

   /**
    * Sets the value of the encAlgo property.
    * 
    * @param value
    *     allowed object is
    *     {@link EncAlgoType }
    *     
    */
   public void setEncAlgo(EncAlgoType value)
   {
      this.encAlgo = value;
   }

   /**
    * Gets the value of the keySize property.
    * 
    */
   public int getKeySize()
   {
      return keySize;
   }

   /**
    * Sets the value of the keySize property.
    * 
    */
   public void setKeySize(int value)
   {
      this.keySize = value;
   }

}
