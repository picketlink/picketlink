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

/**
 * <p>Java class for AssertionIDRequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssertionIDRequestType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}AssertionIDRef" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class AssertionIDRequestType extends RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected List<String> assertionIDRef = new ArrayList<String>();

   public AssertionIDRequestType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   /**
    * Add assertion id reference
    * @param id
    */
   public void addAssertionIDRef(String id)
   {
      assertionIDRef.add(id);
   }

   /**
    * remove assertion id reference
    * @param id
    */
   public void removeAssertionIDRef(String id)
   {
      assertionIDRef.remove(id);
   }

   /**
    * Gets the value of the assertionIDRef property.
    *  
    */
   public List<String> getAssertionIDRef()
   {
      return Collections.unmodifiableList(this.assertionIDRef);
   }
}