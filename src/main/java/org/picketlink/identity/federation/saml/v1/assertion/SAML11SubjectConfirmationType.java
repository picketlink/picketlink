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

import org.w3c.dom.Element;

/**
 * <complexType name="SubjectConfirmationType">
        <sequence>
            <element ref="saml:ConfirmationMethod" maxOccurs="unbounded"/>
            <element ref="saml:SubjectConfirmationData" minOccurs="0"/>

            <element ref="ds:KeyInfo" minOccurs="0"/>
        </sequence>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectConfirmationType
{
   protected List<URI> confirmationMethod = new ArrayList<URI>();

   protected Object subjectConfirmationData;

   protected Element keyInfo;

   public void addConfirmationMethod(URI confirmation)
   {
      this.confirmationMethod.add(confirmation);
   }

   public void addAllConfirmationMethod(List<URI> confirmation)
   {
      this.confirmationMethod.addAll(confirmation);
   }

   public boolean removeConfirmationMethod(URI confirmation)
   {
      return this.confirmationMethod.remove(confirmation);
   }

   public List<URI> getConfirmationMethod()
   {
      return Collections.unmodifiableList(confirmationMethod);
   }

   public void setSubjectConfirmationData(Object subjectConfirmation)
   {
      this.subjectConfirmationData = subjectConfirmation;
   }

   public Element getKeyInfo()
   {
      return keyInfo;
   }

   public void setKeyInfo(Element keyInfo)
   {
      this.keyInfo = keyInfo;
   }

   public Object getSubjectConfirmationData()
   {
      return subjectConfirmationData;
   }
}