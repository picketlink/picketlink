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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for ProxyRestrictionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProxyRestrictionType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}ConditionAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Audience" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Count" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ProxyRestrictionType extends ConditionAbstractType
{
   private static final long serialVersionUID = 1L;

   protected List<URI> audience = new ArrayList<URI>();

   protected BigInteger count;

   /**
    * Add an audience
    * @param a
    */
   public void addAudience(URI a)
   {
      this.audience.add(a);
   }

   /**
    * Gets the value of the audience property.
    *  
    */
   public List<URI> getAudience()
   {
      return Collections.unmodifiableList(audience);
   }

   /**
    * Remove an audience
    * @param a
    */
   public void removeAudience(URI a)
   {
      this.audience.remove(a);
   }

   /**
    * Gets the value of the count property.
    * 
    * @return
    *     possible object is
    *     {@link BigInteger }
    *     
    */
   public BigInteger getCount()
   {
      return count;
   }

   /**
    * Sets the value of the count property.
    * 
    * @param value
    *     allowed object is
    *     {@link BigInteger }
    *     
    */
   public void setCount(BigInteger value)
   {
      this.count = value;
   }
}