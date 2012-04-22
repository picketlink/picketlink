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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <complexType name="AttributeStatementType">
        <complexContent>
            <extension base="saml:SubjectStatementAbstractType">
                <sequence>
                    <element ref="saml:Attribute" maxOccurs="unbounded"/>

                </sequence>
            </extension>
        </complexContent>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AttributeStatementType extends SAML11SubjectStatementType
{
   private static final long serialVersionUID = 1L;

   protected List<SAML11AttributeType> attribute = new ArrayList<SAML11AttributeType>();

   public void add(SAML11AttributeType aAttribute)
   {
      this.attribute.add(aAttribute);
   }

   public void addAllAttributes(List<SAML11AttributeType> attribList)
   {
      this.attribute.addAll(attribList);
   }

   public boolean remove(SAML11AttributeType anAttrib)
   {
      return this.attribute.remove(anAttrib);
   }

   public List<SAML11AttributeType> get()
   {
      return Collections.unmodifiableList(attribute);
   }
}