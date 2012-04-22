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
package org.picketlink.identity.federation.saml.v2.protocol;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.policy.IdReferenceType;
import org.jboss.security.xacml.core.model.policy.TargetType;

/**
 * <p>Java class for XACMLPolicyQueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XACMLPolicyQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}Target"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicySetIdReference"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:policy:schema:os}PolicyIdReference"/>
 *       &lt;/choice>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class XACMLPolicyQueryType extends RequestAbstractType
{
   private static final long serialVersionUID = 1L;

   public static class ChoiceType
   {
      private RequestType request;

      private TargetType target;

      private IdReferenceType policySetIDReference;

      private IdReferenceType policyIdReference;

      public RequestType getRequest()
      {
         return request;
      }

      public void setRequest(RequestType request)
      {
         this.request = request;
      }

      public TargetType getTarget()
      {
         return target;
      }

      public void setTarget(TargetType target)
      {
         this.target = target;
      }

      public IdReferenceType getPolicySetIDReference()
      {
         return policySetIDReference;
      }

      public void setPolicySetIDReference(IdReferenceType policySetIDReference)
      {
         this.policySetIDReference = policySetIDReference;
      }

      public IdReferenceType getPolicyIdReference()
      {
         return policyIdReference;
      }

      public void setPolicyIdReference(IdReferenceType policyIdReference)
      {
         this.policyIdReference = policyIdReference;
      }
   }

   protected ChoiceType choiceType;

   public XACMLPolicyQueryType(String id, XMLGregorianCalendar instant)
   {
      super(id, instant);
   }

   public ChoiceType getChoiceType()
   {
      return choiceType;
   }

   public void setChoiceType(ChoiceType choiceType)
   {
      this.choiceType = choiceType;
   }
}