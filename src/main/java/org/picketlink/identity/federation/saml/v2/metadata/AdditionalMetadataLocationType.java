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
package org.picketlink.identity.federation.saml.v2.metadata;

import java.net.URI;

/**
 * <p>Java class for AdditionalMetadataLocationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AdditionalMetadataLocationType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute name="namespace" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class AdditionalMetadataLocationType
{
   protected URI value;

   protected URI namespace;

   /**
    * Gets the value of the value property.
    * 
    * @return
    *     possible object is
    *     {@link URI }
    *     
    */
   public URI getValue()
   {
      return value;
   }

   /**
    * Sets the value of the value property.
    * 
    * @param value
    *     allowed object is
    *     {@link URI }
    *     
    */
   public void setValue(URI value)
   {
      this.value = value;
   }

   /**
    * Gets the value of the namespace property.
    * 
    * @return
    *     possible object is
    *     {@link URI }
    *     
    */
   public URI getNamespace()
   {
      return namespace;
   }

   /**
    * Sets the value of the namespace property.
    * 
    * @param value
    *     allowed object is
    *     {@link URI }
    *     
    */
   public void setNamespace(URI value)
   {
      this.namespace = value;
   }
}