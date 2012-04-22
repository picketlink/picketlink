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

import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AuthnStatementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthnStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}SubjectLocality" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AuthnContext"/>
 *       &lt;/sequence>
 *       &lt;attribute name="AuthnInstant" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="SessionIndex" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="SessionNotOnOrAfter" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class AuthnStatementType
extends StatementAbstractType
{ 
   private static final long serialVersionUID = 1L;
   
   protected SubjectLocalityType subjectLocality; 
   protected AuthnContextType authnContext; 
   protected XMLGregorianCalendar authnInstant;  
   protected XMLGregorianCalendar sessionNotOnOrAfter;

   protected String sessionIndex;

   public AuthnStatementType( XMLGregorianCalendar instant )
   {
      this.authnInstant = instant;
   }

   /**
    * Gets the value of the subjectLocality property.
    * 
    * @return
    *     possible object is
    *     {@link SubjectLocalityType }
    *     
    */
   public SubjectLocalityType getSubjectLocality() {
      return subjectLocality;
   }

   /**
    * Sets the value of the subjectLocality property.
    * 
    * @param value
    *     allowed object is
    *     {@link SubjectLocalityType }
    *     
    */
   public void setSubjectLocality(SubjectLocalityType value) {
      this.subjectLocality = value;
   }

   /**
    * Gets the value of the authnContext property.
    * 
    * @return
    *     possible object is
    *     {@link AuthnContextType }
    *     
    */
   public AuthnContextType getAuthnContext() {
      return authnContext;
   }

   /**
    * Sets the value of the authnContext property.
    * 
    * @param value
    *     allowed object is
    *     {@link AuthnContextType }
    *     
    */
   public void setAuthnContext(AuthnContextType value) {
      this.authnContext = value;
   }

   /**
    * Gets the value of the authnInstant property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getAuthnInstant() {
      return authnInstant;
   } 

   /**
    * Gets the value of the sessionIndex property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getSessionIndex() {
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
   public void setSessionIndex(String value) {
      this.sessionIndex = value;
   }

   /**
    * Gets the value of the sessionNotOnOrAfter property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getSessionNotOnOrAfter() {
      return sessionNotOnOrAfter;
   }

   /**
    * Sets the value of the sessionNotOnOrAfter property.
    * 
    * @param value
    *     allowed object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public void setSessionNotOnOrAfter(XMLGregorianCalendar value) {
      this.sessionNotOnOrAfter = value;
   } 
}