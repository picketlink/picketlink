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

package org.picketlink.identity.federation.ws.policy;

import org.picketlink.identity.federation.ws.addressing.BaseAddressingType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="URI" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="Digest" type="{http://www.w3.org/2001/XMLSchema}base64Binary" />
 *       &lt;attribute name="DigestAlgorithm" type="{http://www.w3.org/2001/XMLSchema}anyURI" default="http://schemas.xmlsoap.org/ws/2004/09/policy/Sha1Exc" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class PolicyReference extends BaseAddressingType
{

   protected String uri;

   protected byte[] digest;

   protected String digestAlgorithm;

   /**
    * Gets the value of the uri property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getURI()
   {
      return uri;
   }

   /**
    * Sets the value of the uri property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setURI(String value)
   {
      this.uri = value;
   }

   /**
    * Gets the value of the digest property.
    * 
    * @return
    *     possible object is
    *     byte[]
    */
   public byte[] getDigest()
   {
      return digest;
   }

   /**
    * Sets the value of the digest property.
    * 
    * @param value
    *     allowed object is
    *     byte[]
    */
   public void setDigest(byte[] value)
   {
      this.digest = (value);
   }

   /**
    * Gets the value of the digestAlgorithm property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getDigestAlgorithm()
   {
      if (digestAlgorithm == null)
      {
         return "http://schemas.xmlsoap.org/ws/2004/09/policy/Sha1Exc";
      }
      else
      {
         return digestAlgorithm;
      }
   }

   /**
    * Sets the value of the digestAlgorithm property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setDigestAlgorithm(String value)
   {
      this.digestAlgorithm = value;
   }
}