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
 * <complexType name="EvidenceType">
        <choice maxOccurs="unbounded">
            <element ref="saml:AssertionIDReference"/>

            <element ref="saml:Assertion"/>
        </choice>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11EvidenceType
{
   protected List<String> assertionIDReference = new ArrayList<String>();

   protected List<SAML11AssertionType> assertions = new ArrayList<SAML11AssertionType>();

   public void add(String condition)
   {
      this.assertionIDReference.add(condition);
   }

   public void addAllAssertionIDReference(List<String> theassertionIDReference)
   {
      this.assertionIDReference.addAll(theassertionIDReference);
   }

   public boolean remove(String assertionIDReference)
   {
      return this.assertionIDReference.remove(assertionIDReference);
   }

   public List<String> getAssertionIDReference()
   {
      return Collections.unmodifiableList(assertionIDReference);
   }

   public void add(SAML11AssertionType condition)
   {
      this.assertions.add(condition);
   }

   public void addAllAssertionType(List<SAML11AssertionType> theassertions)
   {
      this.assertions.addAll(theassertions);
   }

   public boolean remove(SAML11AssertionType assertion)
   {
      return this.assertions.remove(assertionIDReference);
   }

   public List<SAML11AssertionType> getAssertions()
   {
      return Collections.unmodifiableList(assertions);
   }
}