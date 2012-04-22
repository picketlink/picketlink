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

package org.picketlink.identity.federation.saml.v2.assertion;

import java.io.Serializable;



/**
 * <p>Java class for SubjectConfirmationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubjectConfirmationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}BaseID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}NameID"/>
 *           &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}EncryptedID"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectConfirmationData" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Method" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class SubjectConfirmationType implements Serializable
{ 
   private static final long serialVersionUID = 1L;
   protected BaseIDAbstractType baseID; 
   protected NameIDType nameID; 
   protected EncryptedElementType encryptedID; 
   protected SubjectConfirmationDataType subjectConfirmationData; 
   protected String method;

   /**
    * Gets the value of the baseID property.
    * 
    * @return
    *     possible object is
    *     {@link BaseIDAbstractType }
    *     
    */
   public BaseIDAbstractType getBaseID() {
      return baseID;
   }

   /**
    * Sets the value of the baseID property.
    * 
    * @param value
    *     allowed object is
    *     {@link BaseIDAbstractType }
    *     
    */
   public void setBaseID(BaseIDAbstractType value) {
      this.baseID = value;
   }

   /**
    * Gets the value of the nameID property.
    * 
    * @return
    *     possible object is
    *     {@link NameIDType }
    *     
    */
   public NameIDType getNameID() {
      return nameID;
   }

   /**
    * Sets the value of the nameID property.
    * 
    * @param value
    *     allowed object is
    *     {@link NameIDType }
    *     
    */
   public void setNameID(NameIDType value) {
      this.nameID = value;
   }

   /**
    * Gets the value of the encryptedID property.
    * 
    * @return
    *     possible object is
    *     {@link EncryptedElementType }
    *     
    */
   public EncryptedElementType getEncryptedID() {
      return encryptedID;
   }

   /**
    * Sets the value of the encryptedID property.
    * 
    * @param value
    *     allowed object is
    *     {@link EncryptedElementType }
    *     
    */
   public void setEncryptedID(EncryptedElementType value) {
      this.encryptedID = value;
   }

   /**
    * Gets the value of the subjectConfirmationData property.
    * 
    * @return
    *     possible object is
    *     {@link SubjectConfirmationDataType }
    *     
    */
   public SubjectConfirmationDataType getSubjectConfirmationData() {
      return subjectConfirmationData;
   }

   /**
    * Sets the value of the subjectConfirmationData property.
    * 
    * @param value
    *     allowed object is
    *     {@link SubjectConfirmationDataType }
    *     
    */
   public void setSubjectConfirmationData(SubjectConfirmationDataType value) {
      this.subjectConfirmationData = value;
   }

   /**
    * Gets the value of the method property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getMethod() {
      return method;
   }

   /**
    * Sets the value of the method property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setMethod(String value) {
      this.method = value;
   }
}