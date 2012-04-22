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

import java.math.BigInteger;



/**
 * <p>Java class for EncryptionMethodType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EncryptionMethodType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="KeySize" type="{http://www.w3.org/2001/04/xmlenc#}KeySizeType" minOccurs="0"/>
 *         &lt;element name="OAEPparams" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Algorithm" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class EncryptionMethodType 
{
   protected String algorithm; 

   protected EncryptionMethod encryptionMethod;
   
   public static class EncryptionMethod
   {
      protected BigInteger keySize;
      protected byte[] OAEPparams;
      public EncryptionMethod(BigInteger bigInteger, byte[] oAEPparams)
      { 
         this.keySize = bigInteger;
         OAEPparams = oAEPparams;
      }
      public BigInteger getKeySize()
      {
         return keySize;
      }
      public byte[] getOAEPparams()
      {
         return OAEPparams;
      } 
   }

   public EncryptionMethodType( String algo )
   {
      this.algorithm = algo;
   }

   

   public EncryptionMethod getEncryptionMethod()
   {
      return encryptionMethod;
   }



   public void setEncryptionMethod(EncryptionMethod encryptionMethod)
   {
      this.encryptionMethod = encryptionMethod;
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