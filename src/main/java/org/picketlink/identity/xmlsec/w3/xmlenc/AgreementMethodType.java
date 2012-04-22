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
package org.picketlink.identity.xmlsec.w3.xmlenc;

import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;

/**
 * <p>Java class for AgreementMethodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AgreementMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KA-Nonce" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;any/>
 *         &lt;element name="OriginatorKeyInfo" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType" minOccurs="0"/>
 *         &lt;element name="RecipientKeyInfo" type="{http://www.w3.org/2000/09/xmldsig#}KeyInfoType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class AgreementMethodType 
{
   protected String algorithm;

   public static class AggrementMethod
   {
      protected byte[] kANonce;
      protected KeyInfoType originatorKeyInfo;
      protected KeyInfoType recipientKeyInfo;
      public AggrementMethod(byte[] kANonce, KeyInfoType originatorKeyInfo, KeyInfoType recipientKeyInfo)
      { 
         this.kANonce = kANonce;
         this.originatorKeyInfo = originatorKeyInfo;
         this.recipientKeyInfo = recipientKeyInfo;
      }
      public byte[] getkANonce()
      {
         return kANonce;
      }
      public KeyInfoType getOriginatorKeyInfo()
      {
         return originatorKeyInfo;
      }
      public KeyInfoType getRecipientKeyInfo()
      {
         return recipientKeyInfo;
      } 
   }
   
   public AgreementMethodType( String algo )
   {
      this.algorithm = algo;
   }

   /**
    * Gets the value of the algorithm property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getAlgorithm() {
      return algorithm;
   }

}
