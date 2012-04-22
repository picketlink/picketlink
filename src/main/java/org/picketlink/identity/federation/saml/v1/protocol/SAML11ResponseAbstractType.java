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

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.common.CommonResponseType;

/**
 * <complexType name="ResponseAbstractType" abstract="true">
        <sequence>

            <element ref="ds:Signature" minOccurs="0"/>
        </sequence>
        <attribute name="ResponseID" type="ID" use="required"/>
        <attribute name="InResponseTo" type="NCName" use="optional"/>
        <attribute name="MajorVersion" type="integer" use="required"/>
        <attribute name="MinorVersion" type="integer" use="required"/>
        <attribute name="IssueInstant" type="dateTime" use="required"/>
        <attribute name="Recipient" type="anyURI" use="optional"/>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public abstract class SAML11ResponseAbstractType extends CommonResponseType
{
   private static final long serialVersionUID = 1L;

   protected int majorVersion = 1;

   protected int minorVersion = 1;

   protected URI recipient;

   public SAML11ResponseAbstractType(String id, XMLGregorianCalendar issueInstant)
   {
      super(id, issueInstant);
   }

   public int getMajorVersion()
   {
      return majorVersion;
   }

   public int getMinorVersion()
   {
      return minorVersion;
   }

   public URI getRecipient()
   {
      return recipient;
   }

   public void setRecipient(URI recipient)
   {
      this.recipient = recipient;
   }
}