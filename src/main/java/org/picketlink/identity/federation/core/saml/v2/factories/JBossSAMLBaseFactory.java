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
package org.picketlink.identity.federation.core.saml.v2.factories;


import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;

/**
 * Base methods for the factories
 * @author Anil.Saldhana@redhat.com
 * @since Dec 9, 2008
 */
public class JBossSAMLBaseFactory
{   
   /**
    * Create an empty attribute statement
    * @return
    */
   public static AttributeStatementType createAttributeStatement()
   {
      return new AttributeStatementType(); 
   }
   
   /**
    * Create an attribute type given a role name
    * @param roleName
    * @return
    */
   public static AttributeType createAttributeForRole(String roleName)
   {
      AttributeType att = new AttributeType( "role" );
      att.setFriendlyName("role"); 
      att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());
      
      //rolename 
      att.addAttributeValue( roleName );
      
      return att;
   }
   
   /**
    * Create an AttributeStatement given an attribute
    * @param attributeValue
    * @return
    */
   public static AttributeStatementType createAttributeStatement(String attributeValue)
   {
      AttributeStatementType attribStatement = new AttributeStatementType();
      AttributeType att = new AttributeType( attributeValue );
      att.addAttributeValue(attributeValue);
      
      attribStatement.addAttribute( new ASTChoiceType( att ));
      return attribStatement;
   }
   
   /**
    * Create a Subject confirmation type given the method
    * @param method
    * @return
    */
   public static SubjectConfirmationType createSubjectConfirmation(String method)
   {
      SubjectConfirmationType sct = new SubjectConfirmationType();
      sct.setMethod(method);
      return sct;
   }
   
   /**
    * Create a Subject Confirmation
    * @param inResponseTo
    * @param destinationURI
    * @param issueInstant
    * @return
    */
   public static SubjectConfirmationDataType createSubjectConfirmationData(String inResponseTo, 
         String destinationURI, XMLGregorianCalendar issueInstant)
   {
      SubjectConfirmationDataType subjectConfirmationData = new SubjectConfirmationDataType();
      subjectConfirmationData.setInResponseTo(inResponseTo);
      subjectConfirmationData.setRecipient(destinationURI);
      subjectConfirmationData.setNotBefore(issueInstant);
      subjectConfirmationData.setNotOnOrAfter(issueInstant);
      
      return subjectConfirmationData;
   }
   
   /**
    * Get a UUID String
    * @return
    */
   public static String createUUID()
   {
      return java.util.UUID.randomUUID().toString(); 
   }
    
   
   /**
    * Return the NameIDType for the issuer
    * @param issuerID
    * @return
    */
   public static NameIDType getIssuer(String issuerID)
   {
      NameIDType nid = new NameIDType();
      nid.setValue(issuerID);
      return nid;
   }
}