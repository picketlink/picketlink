/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.ws.addressing;

/**
 * <p>Java class for RelatesToType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RelatesToType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute name="RelationshipType" type="{http://www.w3.org/2005/08/addressing}RelationshipTypeOpenEnum" default="http://www.w3.org/2005/08/addressing/reply" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class RelatesToType extends BaseAddressingType
{
   protected String value;

   protected String relationshipType;

   /**
    * Gets the value of the value property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getValue()
   {
      return value;
   }

   /**
    * Sets the value of the value property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setValue(String value)
   {
      this.value = value;
   }

   /**
    * Gets the value of the relationshipType property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getRelationshipType()
   {
      if (relationshipType == null)
      {
         return "http://www.w3.org/2005/08/addressing/reply";
      }
      else
      {
         return relationshipType;
      }
   }

   /**
    * Sets the value of the relationshipType property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setRelationshipType(String value)
   {
      this.relationshipType = value;
   }
}