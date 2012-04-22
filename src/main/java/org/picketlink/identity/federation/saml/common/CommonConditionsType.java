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
package org.picketlink.identity.federation.saml.common;

import java.io.Serializable;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class CommonConditionsType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected XMLGregorianCalendar notBefore;

   protected XMLGregorianCalendar notOnOrAfter;

   /**
    * Gets the value of the notBefore property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getNotBefore()
   {
      return notBefore;
   }

   /**
    * Sets the value of the notBefore property.
    * 
    * @param value
    *     allowed object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public void setNotBefore(XMLGregorianCalendar value)
   {
      this.notBefore = value;
   }

   /**
    * Gets the value of the notOnOrAfter property.
    * 
    * @return
    *     possible object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public XMLGregorianCalendar getNotOnOrAfter()
   {
      return notOnOrAfter;
   }

   /**
    * Sets the value of the notOnOrAfter property.
    * 
    * @param value
    *     allowed object is
    *     {@link XMLGregorianCalendar }
    *     
    */
   public void setNotOnOrAfter(XMLGregorianCalendar value)
   {
      this.notOnOrAfter = value;
   }
}