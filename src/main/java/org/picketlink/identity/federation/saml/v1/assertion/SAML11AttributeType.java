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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <complexType name="AttributeType">
        <complexContent>
            <extension base="saml:AttributeDesignatorType">
                <sequence>
                    <element ref="saml:AttributeValue" maxOccurs="unbounded"/>
                </sequence>
            </extension>
        </complexContent>

    </complexType>
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeType extends SAML11AttributeDesignatorType
{
   protected List<Object> attributeValues = new ArrayList<Object>();

   public SAML11AttributeType(String attributeName, URI attributeNamespace)
   {
      super(attributeName, attributeNamespace);
   }

   public void add(Object attribValue)
   {
      this.attributeValues.add(attribValue);
   }

   public void addAll(List<Object> attribValueList)
   {
      this.attributeValues.addAll(attribValueList);
   }

   public boolean remove(Object attribVal)
   {
      return this.attributeValues.remove(attribVal);
   }

   public List<Object> get()
   {
      return Collections.unmodifiableList(attributeValues);
   }
}