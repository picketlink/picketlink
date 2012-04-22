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
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.saml.common.CommonRequestAbstractType;

/**
 * <complexType name="RequestAbstractType" abstract="true">

        <sequence>
            <element ref="samlp:RespondWith" minOccurs="0" maxOccurs="unbounded"/>
            <element ref="ds:Signature" minOccurs="0"/>
        </sequence>
        <attribute name="RequestID" type="ID" use="required"/>
        <attribute name="MajorVersion" type="integer" use="required"/>
        <attribute name="MinorVersion" type="integer" use="required"/>
        <attribute name="IssueInstant" type="dateTime" use="required"/>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public abstract class SAML11RequestAbstractType extends CommonRequestAbstractType
{
   private static final long serialVersionUID = 1L;

   protected int majorVersion = 1;

   protected int minorVersion = 1;

   protected List<QName> respondWith = new ArrayList<QName>();

   public SAML11RequestAbstractType(String id, XMLGregorianCalendar issueInstant)
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

   public void add(QName rw)
   {
      this.respondWith.add(rw);
   }

   public void addAllConditions(List<QName> rw)
   {
      this.respondWith.addAll(rw);
   }

   public boolean remove(QName rw)
   {
      return this.respondWith.remove(rw);
   }

   public List<QName> getRespondWith()
   {
      return Collections.unmodifiableList(respondWith);
   }
}