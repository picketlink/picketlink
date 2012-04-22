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
package org.picketlink.identity.federation.core.saml.v2.holders;

import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
 

/**
 * Holds essential information about an IDP for creating
 * saml messages.
 * @author Anil.Saldhana@redhat.com
 * @since Dec 10, 2008
 */
public class IDPInfoHolder
{ 
   private String subjectConfirmationMethod = JBossSAMLURIConstants.SUBJECT_CONFIRMATION_BEARER.get();
   private String nameIDFormat = JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get();
   private String nameIDFormatValue;
   
   private AssertionType assertion;
   
   private int assertionValidityDuration = 5; //5 Minutes 
   
   public int getAssertionValidityDuration()
   {
      return assertionValidityDuration;
   }

   public void setAssertionValidityDuration(int assertionValidityDuration)
   {
      this.assertionValidityDuration = assertionValidityDuration;
   }

   public String getSubjectConfirmationMethod()
   {
      return subjectConfirmationMethod;
   }

   public void setSubjectConfirmationMethod(String subjectConfirmationMethod)
   {
      this.subjectConfirmationMethod = subjectConfirmationMethod;
   }

   public String getNameIDFormat()
   {
      return nameIDFormat;
   }

   public void setNameIDFormat(String nameIDFormat)
   {
      this.nameIDFormat = nameIDFormat;
   }

   public String getNameIDFormatValue()
   {
      return nameIDFormatValue;
   }

   public void setNameIDFormatValue(String nameIDFormatValue)
   {
      this.nameIDFormatValue = nameIDFormatValue;
   }

   public AssertionType getAssertion()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission( PicketLinkCoreSTS.rte );
      return assertion;
   }

   public void setAssertion(AssertionType assertion)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission( PicketLinkCoreSTS.rte );
      this.assertion = assertion;
   }  
}