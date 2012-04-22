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

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>Java class for AuthnQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthnQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}SubjectQueryAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}RequestedAuthnContext" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="SessionIndex" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class AuthnQueryType extends SubjectQueryAbstractType
{
   private static final long serialVersionUID = 1L;

   protected RequestedAuthnContextType requestedAuthnContext;

   protected String sessionIndex;

   public AuthnQueryType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Gets the value of the requestedAuthnContext property.
    * 
    * @return
    *     possible object is
    *     {@link RequestedAuthnContextType }
    *     
    */
   public RequestedAuthnContextType getRequestedAuthnContext()
   {
      return requestedAuthnContext;
   }

   /**
    * Sets the value of the requestedAuthnContext property.
    * 
    * @param value
    *     allowed object is
    *     {@link RequestedAuthnContextType }
    *     
    */
   public void setRequestedAuthnContext(RequestedAuthnContextType value)
   {
      this.requestedAuthnContext = value;
   }

   /**
    * Gets the value of the sessionIndex property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getSessionIndex()
   {
      return sessionIndex;
   }

   /**
    * Sets the value of the sessionIndex property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setSessionIndex(String value)
   {
      this.sessionIndex = value;
   }
}