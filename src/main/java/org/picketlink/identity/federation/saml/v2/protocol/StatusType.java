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



/**
 * <p>Java class for StatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}StatusCode"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}StatusMessage" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}StatusDetail" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class StatusType 
{ 
   protected String statusMessage;
   protected StatusCodeType statusCode; 
   protected StatusDetailType statusDetail;

   /**
    * Gets the value of the statusCode property.
    * 
    * @return
    *     possible object is
    *     {@link StatusCodeType }
    *     
    */
   public StatusCodeType getStatusCode() {
      return statusCode;
   }

   /**
    * Sets the value of the statusCode property.
    * 
    * @param value
    *     allowed object is
    *     {@link StatusCodeType }
    *     
    */
   public void setStatusCode(StatusCodeType value) {
      this.statusCode = value;
   }

   /**
    * Gets the value of the statusMessage property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getStatusMessage() {
      return statusMessage;
   }

   /**
    * Sets the value of the statusMessage property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setStatusMessage(String value) {
      this.statusMessage = value;
   }

   /**
    * Gets the value of the statusDetail property.
    * 
    * @return
    *     possible object is
    *     {@link StatusDetailType }
    *     
    */
   public StatusDetailType getStatusDetail() {
      return statusDetail;
   }

   /**
    * Sets the value of the statusDetail property.
    * 
    * @param value
    *     allowed object is
    *     {@link StatusDetailType }
    *     
    */
   public void setStatusDetail(StatusDetailType value) {
      this.statusDetail = value;
   }

}
