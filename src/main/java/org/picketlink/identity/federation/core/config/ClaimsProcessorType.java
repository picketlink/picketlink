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
package org.picketlink.identity.federation.core.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Java class for ClaimsProcessorType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ClaimsProcessorType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Property" type="{urn:picketlink:identity-federation:config:1.0}KeyValueType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ProcessorClass" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Dialect" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ClaimsProcessorType
{

   protected List<KeyValueType> property = new ArrayList<KeyValueType>();

   protected String processorClass;

   protected String dialect;

   public void add(KeyValueType kv)
   {
      this.property.add(kv);
   }

   public void remove(KeyValueType kv)
   {
      this.property.remove(kv);
   }

   /**
    * Gets the value of the property property.
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link KeyValueType }
    * 
    * 
    */
   public List<KeyValueType> getProperty()
   {
      return Collections.unmodifiableList(this.property);
   }

   /**
    * Gets the value of the processorClass property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getProcessorClass()
   {
      return processorClass;
   }

   /**
    * Sets the value of the processorClass property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setProcessorClass(String value)
   {
      this.processorClass = value;
   }

   /**
    * Gets the value of the dialect property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getDialect()
   {
      return dialect;
   }

   /**
    * Sets the value of the dialect property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setDialect(String value)
   {
      this.dialect = value;
   }

}
