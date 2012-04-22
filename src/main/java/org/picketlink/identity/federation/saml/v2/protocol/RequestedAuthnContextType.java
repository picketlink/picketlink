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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for RequestedAuthnContextType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestedAuthnContextType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AuthnContextClassRef" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AuthnContextDeclRef" maxOccurs="unbounded"/>
 *       &lt;/choice>
 *       &lt;attribute name="Comparison" type="{urn:oasis:names:tc:SAML:2.0:protocol}AuthnContextComparisonType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class RequestedAuthnContextType 
{
   protected List<String> authnContextClassRef = new ArrayList<String>();
   protected List<String> authnContextDeclRef = new ArrayList<String>();
   protected AuthnContextComparisonType comparison;

   /**
    * Add an authn Context class ref
    * @param str
    */
   public void addAuthnContextClassRef( String str )
   {
      this.authnContextClassRef.add(str);
   }

   /**
    * Add authn context decl ref
    * @param str
    */
   public void addAuthnContextDeclRef( String str )
   {
      this.authnContextDeclRef.add(str);
   }
   
   /**
    * Remove an authn Context class ref
    * @param str
    */
   public void removeAuthnContextClassRef( String str )
   {
      this.authnContextClassRef.remove(str);
   }

   /**
    * remove authn context decl ref
    * @param str
    */
   public void removeAuthnContextDeclRef( String str )
   {
      this.authnContextDeclRef.remove(str);
   }

   /**
    * Gets the value of the authnContextClassRef property. 
    * 
    */
   public List<String> getAuthnContextClassRef() 
   {
      return Collections.unmodifiableList( this.authnContextClassRef );
   }

   /**
    * Gets the value of the authnContextDeclRef property.
    * 
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the authnContextDeclRef property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAuthnContextDeclRef().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    * 
    * 
    */
   public List<String> getAuthnContextDeclRef() 
   { 
      return Collections.unmodifiableList( this.authnContextDeclRef );
   }

   /**
    * Gets the value of the comparison property.
    * 
    * @return
    *     possible object is
    *     {@link AuthnContextComparisonType }
    *     
    */
   public AuthnContextComparisonType getComparison() 
   {
      return comparison;
   }

   /**
    * Sets the value of the comparison property.
    * 
    * @param value
    *     allowed object is
    *     {@link AuthnContextComparisonType }
    *     
    */
   public void setComparison(AuthnContextComparisonType value) 
   {
      this.comparison = value;
   }
}