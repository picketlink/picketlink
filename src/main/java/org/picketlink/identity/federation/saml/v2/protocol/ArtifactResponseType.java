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

import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

/**
 * <p>Java class for ArtifactResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArtifactResponseType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}StatusResponseType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class ArtifactResponseType extends StatusResponseType
{
   private static final long serialVersionUID = 1L;

   protected Object any;

   public ArtifactResponseType(String id, XMLGregorianCalendar issueInstant)
   {
      super(id, issueInstant);
   }

   public ArtifactResponseType(StatusResponseType srt)
   {
      super(srt);
   }

   /**
    * Gets the value of the any property.
    * 
    * @return
    *     possible object is
    *     {@link Element }
    *     {@link Object }
    *     
    */
   public Object getAny()
   {
      return any;
   }

   /**
    * Sets the value of the any property.
    * 
    * @param value
    *     allowed object is
    *     {@link Element }
    *     {@link Object }
    *     
    */
   public void setAny(Object value)
   {
      this.any = value;
   }

}
