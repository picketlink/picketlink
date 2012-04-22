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
package org.picketlink.identity.federation.core.saml.v2.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;

/**
 * <p>
 * A SAML2 specification based instance of {@code ProtocolContext}
 * </p>
 * <p>
 * This instance is used to pass information from the IDP to the Core STS.
 * </p>
 * @author Anil.Saldhana@redhat.com
 * @since Dec 30, 2010
 */
public class SAMLProtocolContext implements ProtocolContext
{
   protected NameIDType issuerID; 
   
   protected SubjectType subjectType;
   
   protected ConditionsType conditions;
   
   protected List<StatementAbstractType> statements = new ArrayList<StatementAbstractType>();
   
   protected AssertionType issuedAssertion;
   
   /**
    * Get the Issuer ID
    * @return instance of {@link NameIDType}
    */
   public NameIDType getIssuerID()
   {
      return issuerID;
   }

   /**
    * Set the Issuer ID
    * @param issuerID {@link NameIDType}
    */
   public void setIssuerID( NameIDType issuerID)
   {
      this.issuerID = issuerID;
   }

   /**
    * Get the subject
    * @return {@link SubjectType}
    */
   public SubjectType getSubjectType()
   {
      return subjectType;
   }

   /**
    * Set the subject
    * @param subjectType {@link SubjectType}
    */
   public void setSubjectType(SubjectType subjectType)
   {
      this.subjectType = subjectType;
   }

   /**
    * Get the conditions
    * @return {@link ConditionsType}
    */
   public ConditionsType getConditions()
   {
      return conditions;
   }

   /**
    * Set the conditions
    * @param conditions {@link ConditionsType}
    */
   public void setConditions(ConditionsType conditions)
   {
      this.conditions = conditions;
   }

   /**
    * Get the statements as a read-only list
    * @return {@link StatementAbstractType}
    */
   public List<StatementAbstractType> getStatements()
   {
      return Collections.unmodifiableList( statements );
   }

   /**
    * Add a list of statements 
    * @param statements {@link List}
    */
   public void setStatements(List<StatementAbstractType> statements)
   {
      this.statements = statements;
   } 

   /**
    * Get the previously issued assertion by the STS
    * @return {@link AssertionType}
    */
   public AssertionType getIssuedAssertion()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission( PicketLinkCoreSTS.rte ); 
      
      return issuedAssertion;
   }

   /**
    * Set the assertion issued by the STS
    * @param issuedAssertion {@link AssertionType}
    */
   public void setIssuedAssertion(AssertionType issuedAssertion)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission( PicketLinkCoreSTS.rte ); 
      
      this.issuedAssertion = issuedAssertion;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#serviceName()
    */
   public String serviceName()
   { 
      return null;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#tokenType()
    */
   public String tokenType()
   {
      return JBossSAMLURIConstants.ASSERTION_NSURI.get();
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#getQName()
    */
   public QName getQName()
   {
      String localPart = JBossSAMLConstants.ASSERTION.get();
      String ns = tokenType();
      return new QName( ns, localPart );
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#family()
    */
   public String family()
   { 
      return SecurityTokenProvider.FAMILY_TYPE.SAML2.toString();
   } 
}