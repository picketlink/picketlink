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
package org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion;

import java.util.ArrayList;
import java.util.List;

import org.jboss.security.xacml.core.model.policy.PolicySetType;
import org.jboss.security.xacml.core.model.policy.PolicyType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;

/**
 * <p>Java class for XACMLPolicyStatementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XACMLPolicyStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}Policy"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicySet"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class XACMLPolicyStatementType
extends StatementAbstractType
{
   private static final long serialVersionUID = 1L;

   public static class ChoiceType
   {
      private PolicyType policy;
      private PolicySetType policySet;
      public PolicyType getPolicy()
      {
         return policy;
      }
      public void setPolicy(PolicyType policy)
      {
         this.policy = policy;
      }
      public PolicySetType getPolicySet()
      {
         return policySet;
      }
      public void setPolicySet(PolicySetType policySet)
      {
         this.policySet = policySet;
      } 
   }

   protected List<ChoiceType> choiceTypeList = new ArrayList<ChoiceType>();

   public void add(ChoiceType choice )
   {
      choiceTypeList.add(choice);
   }

   /**
    * Gets the value of the choiceTypeList property. 
    */
   public List<ChoiceType> getChoiceType() 
   {
      return choiceTypeList;
   }

}