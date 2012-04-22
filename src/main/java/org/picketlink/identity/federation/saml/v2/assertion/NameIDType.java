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

import java.net.URI;

/**
 * Represents a NameIDType
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class NameIDType extends BaseIDAbstractType
{
   /*
    <complexType name="NameIDType">
        <simpleContent>
            <extension base="string">
                <attributeGroup ref="saml:IDNameQualifiers"/>
                <attribute name="Format" type="anyURI" use="optional"/>
                <attribute name="SPProvidedID" type="string" use="optional"/>
            </extension>
        </simpleContent>
    </complexType>

    <attributeGroup name="IDNameQualifiers">
        <attribute name="NameQualifier" type="string" use="optional"/>
        <attribute name="SPNameQualifier" type="string" use="optional"/>
    </attributeGroup>
    */

   private static final long serialVersionUID = 1L;
   private String value;
   private URI format;
   private String sPProvidedID; 

   public String getValue()
   {
      return value;
   }
   public void setValue(String value)
   {
      this.value = value;
   }

   public String getsPProvidedID()
   {
      return sPProvidedID;
   }
   public void setsPProvidedID(String sPProvidedID)
   {
      this.sPProvidedID = sPProvidedID;
   }
   public URI getFormat()
   {
      return format;
   }
   public void setFormat(URI format)
   {
      this.format = format;
   }
   public String getSPProvidedID()
   {
      return sPProvidedID;
   }
   public void setSPProvidedID(String sPProvidedID)
   {
      this.sPProvidedID = sPProvidedID;
   } 
}