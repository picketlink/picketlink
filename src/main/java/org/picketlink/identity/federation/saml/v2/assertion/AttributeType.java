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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName; 

/**
 * <p>Java class for AttributeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AttributeValue" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="NameFormat" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="FriendlyName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */ 
public class AttributeType implements Serializable
{
   private static final long serialVersionUID = 1L;
   
   protected List<Object> attributeValue = new ArrayList<Object>(); 
   protected String name;  
   protected String nameFormat; 
   protected String friendlyName; 
   private Map<QName, String> otherAttributes = new HashMap<QName, String>();
   
   public AttributeType( String name )
   {
      this.name = name;
   }

   /**
    * Add an attribute value to the attribute
    * @param value {@link Object}
    */
   public void addAttributeValue( Object value )
   {
      attributeValue.add(value);
   }
   
   /**
    * Remove an attribute value to the attribute
    * @param value {@link Object}
    */
   public void removeAttributeValue( Object value )
   {
      attributeValue.remove(value);
   }

   /**
    * Gets the value of the attributeValue property.
    *  
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getAttributeValue().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Object }
    * 
    * 
    */
   public List<Object> getAttributeValue() 
   { 
      return Collections.unmodifiableList( this.attributeValue );
   }

   /**
    * Gets the value of the name property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getName() 
   {
      return name;
   }

   /**
    * Sets the value of the name property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setName(String value) 
   {
      this.name = value;
   }

   /**
    * Gets the value of the nameFormat property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getNameFormat() 
   {
      return nameFormat;
   }

   /**
    * Sets the value of the nameFormat property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setNameFormat(String value) 
   {
      this.nameFormat = value;
   }

   /**
    * Gets the value of the friendlyName property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getFriendlyName() 
   {
      return friendlyName;
   }

   /**
    * Sets the value of the friendlyName property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setFriendlyName(String value) 
   {
      this.friendlyName = value;
   }

   /**
    * Gets a map that contains attributes that aren't bound to any typed property on this class.
    * 
    * <p>
    * the map is keyed by the name of the attribute and 
    * the value is the string value of the attribute.
    * 
    * the map returned by this method is live, and you can add new attribute
    * by updating the map directly. Because of this design, there's no setter.
    * 
    * 
    * @return
    *     always non-null
    */
   public Map<QName, String> getOtherAttributes() 
   {
      return otherAttributes;
   }
}