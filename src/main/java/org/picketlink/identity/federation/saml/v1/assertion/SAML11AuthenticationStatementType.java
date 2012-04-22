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

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <complexType name="AuthenticationStatementType">
        <complexContent>
            <extension base="saml:SubjectStatementAbstractType">

                <sequence>
                    <element ref="saml:SubjectLocality" minOccurs="0"/>
                    <element ref="saml:AuthorityBinding" minOccurs="0" maxOccurs="unbounded"/>
                </sequence>
                <attribute name="AuthenticationMethod" type="anyURI" use="required"/>
                <attribute name="AuthenticationInstant" type="dateTime" use="required"/>
            </extension>
        </complexContent>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthenticationStatementType extends SAML11SubjectStatementType
{
   private static final long serialVersionUID = 1L;

   protected URI authenticationMethod;

   protected XMLGregorianCalendar authenticationInstant;

   protected SAML11SubjectLocalityType subjectLocality;

   protected List<SAML11AuthorityBindingType> authorityBinding = new ArrayList<SAML11AuthorityBindingType>();

   public SAML11AuthenticationStatementType(URI authenticationMethod, XMLGregorianCalendar authenticationInstant)
   {
      this.authenticationMethod = authenticationMethod;
      this.authenticationInstant = authenticationInstant;
   }

   public URI getAuthenticationMethod()
   {
      return authenticationMethod;
   }

   public XMLGregorianCalendar getAuthenticationInstant()
   {
      return authenticationInstant;
   }

   public SAML11SubjectLocalityType getSubjectLocality()
   {
      return subjectLocality;
   }

   public void setSubjectLocality(SAML11SubjectLocalityType subjectLocality)
   {
      this.subjectLocality = subjectLocality;
   }

   public void add(SAML11AuthorityBindingType advice)
   {
      this.authorityBinding.add(advice);
   }

   public void addAllAuthorityBindingType(List<SAML11AuthorityBindingType> advice)
   {
      this.authorityBinding.addAll(advice);
   }

   public boolean remove(SAML11AuthorityBindingType advice)
   {
      return this.authorityBinding.remove(advice);
   }

   public List<SAML11AuthorityBindingType> getAuthorityBindingType()
   {
      return Collections.unmodifiableList(authorityBinding);
   }
}