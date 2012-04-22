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

import org.w3c.dom.Element;

/**
 * Represents an element that is encrypted
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class EncryptedElementType implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    <complexType name="EncryptedElementType">
        <sequence>
            <element ref="xenc:EncryptedData"/>
            <element ref="xenc:EncryptedKey" minOccurs="0" maxOccurs="unbounded"/>
        </sequence>
    </complexType>
    */

   protected Element encryptedElement;

   public EncryptedElementType()
   {
   }

   public EncryptedElementType(Element el)
   {
      this.encryptedElement = el;
   }

   public Element getEncryptedElement()
   {
      return encryptedElement;
   }

   public void setEncryptedElement(Element encryptedElement)
   {
      this.encryptedElement = encryptedElement;
   }
}