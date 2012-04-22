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

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;

/**
 * <p>Java class for AttributeQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AttributeQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}SubjectQueryAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Attribute" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class AttributeQueryType extends SubjectQueryAbstractType
{
   private static final long serialVersionUID = 1L;

   protected List<AttributeType> attribute = new ArrayList<AttributeType>();

   public AttributeQueryType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Add an attribute
    * @param att
    */
   public void add(AttributeType att)
   {
      this.attribute.add(att);
   }

   /**
    * Remove an attribute
    * @param att
    */
   public void remove(AttributeType att)
   {
      this.attribute.remove(att);
   }

   /**
    * Gets the value of the attribute property. 
    */
   public List<AttributeType> getAttribute()
   {
      return Collections.unmodifiableList(this.attribute);
   }
}