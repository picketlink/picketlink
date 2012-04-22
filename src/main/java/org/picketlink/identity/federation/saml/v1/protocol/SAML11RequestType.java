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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <complexType name="RequestType">
        <complexContent>
            <extension base="samlp:RequestAbstractType">
                <choice>
                    <element ref="samlp:Query"/>
                    <element ref="samlp:SubjectQuery"/>
                    <element ref="samlp:AuthenticationQuery"/>

                    <element ref="samlp:AttributeQuery"/>
                    <element ref="samlp:AuthorizationDecisionQuery"/>
                    <element ref="saml:AssertionIDReference" maxOccurs="unbounded"/>
                    <element ref="samlp:AssertionArtifact" maxOccurs="unbounded"/>
                </choice>
            </extension>
        </complexContent>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11RequestType extends SAML11RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected SAML11QueryAbstractType query;

   protected List<String> assertionIDRef = new ArrayList<String>();

   protected List<String> assertionArtifact = new ArrayList<String>();

   public SAML11RequestType(String id, XMLGregorianCalendar issueInstant)
   {
      super(id, issueInstant);
   }

   public void addAssertionIDRef(String sadt)
   {
      this.assertionIDRef.add(sadt);
   }

   public boolean removeAssertionIDRef(String sadt)
   {
      return this.assertionIDRef.remove(sadt);
   }

   public List<String> getAssertionIDRef()
   {
      return Collections.unmodifiableList(assertionIDRef);
   }

   public void addAssertionArtifact(String sadt)
   {
      this.assertionArtifact.add(sadt);
   }

   public boolean removeAssertionArtifact(String sadt)
   {
      return this.assertionArtifact.remove(sadt);
   }

   public List<String> getAssertionArtifact()
   {
      return Collections.unmodifiableList(assertionArtifact);
   }

   public SAML11QueryAbstractType getQuery()
   {
      return query;
   }

   public void setQuery(SAML11QueryAbstractType query)
   {
      this.query = query;
   }
}