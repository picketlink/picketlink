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
package org.picketlink.identity.federation.saml.v1.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeDesignatorType;

/**
 * <complexType name="AttributeQueryType">
        <complexContent>
            <extension base="samlp:SubjectQueryAbstractType">
                <sequence>
                    <element ref="saml:AttributeDesignator" minOccurs="0" maxOccurs="unbounded"/>
                </sequence>

                <attribute name="Resource" type="anyURI" use="optional"/>
            </extension>
        </complexContent>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeQueryType extends SAML11SubjectQueryAbstractType
{
   private static final long serialVersionUID = 1L;

   protected List<SAML11AttributeDesignatorType> attributeDesignator = new ArrayList<SAML11AttributeDesignatorType>();

   protected URI resource;

   public URI getResource()
   {
      return resource;
   }

   public void setResource(URI resource)
   {
      this.resource = resource;
   }

   public void add(SAML11AttributeDesignatorType sadt)
   {
      this.attributeDesignator.add(sadt);
   }

   public boolean remove(SAML11AttributeDesignatorType sadt)
   {
      return this.attributeDesignator.remove(sadt);
   }

   public List<SAML11AttributeDesignatorType> get()
   {
      return Collections.unmodifiableList(attributeDesignator);
   }
}