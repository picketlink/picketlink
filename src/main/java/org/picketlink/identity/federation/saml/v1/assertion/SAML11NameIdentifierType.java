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
package org.picketlink.identity.federation.saml.v1.assertion;

import java.io.Serializable;
import java.net.URI;

/**
 * <complexType name="NameIdentifierType">
        <simpleContent>
            <extension base="string">
                <attribute name="NameQualifier" type="string" use="optional"/> 
                <attribute name="Format" type="anyURI" use="optional"/>
            </extension>
        </simpleContent>
    </complexType>
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11NameIdentifierType implements Serializable
{
   private static final long serialVersionUID = 1L;

   protected String nameQualifier;

   protected URI format;

   protected String value;

   public SAML11NameIdentifierType(String val)
   {
      this.value = val;
   }

   public String getNameQualifier()
   {
      return nameQualifier;
   }

   public void setNameQualifier(String nameQualifier)
   {
      this.nameQualifier = nameQualifier;
   }

   public URI getFormat()
   {
      return format;
   }

   public void setFormat(URI format)
   {
      this.format = format;
   }

   public String getValue()
   {
      return value;
   }
}