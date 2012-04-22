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

/**
 * <complexType name="SubjectType">
        <choice>
            <sequence>
                <element ref="saml:NameIdentifier"/>
                <element ref="saml:SubjectConfirmation" minOccurs="0"/>

            </sequence>
            <element ref="saml:SubjectConfirmation"/>
        </choice>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11SubjectType
{
   public static class SAML11SubjectTypeChoice
   {
      protected SAML11NameIdentifierType nameID;

      protected SAML11SubjectConfirmationType subjectConfirmation;

      public SAML11SubjectTypeChoice(SAML11NameIdentifierType nameID)
      {
         this.nameID = nameID;
      }

      public SAML11SubjectTypeChoice(SAML11SubjectConfirmationType subConfirms)
      {
         this.subjectConfirmation = subConfirms;
      }

      public SAML11NameIdentifierType getNameID()
      {
         return nameID;
      }

      public SAML11SubjectConfirmationType getSubjectConfirmation()
      {
         return subjectConfirmation;
      }
   }

   protected SAML11SubjectConfirmationType subjectConfirmation;

   protected SAML11SubjectTypeChoice choice;

   public SAML11SubjectConfirmationType getSubjectConfirmation()
   {
      return subjectConfirmation;
   }

   public void setSubjectConfirmation(SAML11SubjectConfirmationType subjectConfirmation)
   {
      this.subjectConfirmation = subjectConfirmation;
   }

   public SAML11SubjectTypeChoice getChoice()
   {
      return choice;
   }

   public void setChoice(SAML11SubjectTypeChoice choice)
   {
      this.choice = choice;
   }
}