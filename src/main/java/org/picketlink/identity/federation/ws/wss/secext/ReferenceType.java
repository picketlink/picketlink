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
package org.picketlink.identity.federation.ws.wss.secext;

import org.picketlink.identity.federation.ws.addressing.BaseAddressingType;

/**
 * This type represents a reference to an external security token.
 * 
 * <p>Java class for ReferenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReferenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="ValueType" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ReferenceType extends BaseAddressingType
{
   protected String uri;

   protected String valueType;

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
    * Gets the value of the valueType property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getValueType()
   {
      return valueType;
   }

   /**
    * Sets the value of the valueType property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setValueType(String value)
   {
      this.valueType = value;
   }
}