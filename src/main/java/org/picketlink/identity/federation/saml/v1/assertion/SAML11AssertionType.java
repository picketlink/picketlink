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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.common.CommonAssertionType;
import org.w3c.dom.Element;

/**
 * <complexType name="AssertionType">
        <sequence>
            <element ref="saml:Conditions" minOccurs="0"/>
            <element ref="saml:Advice" minOccurs="0"/>
            <choice maxOccurs="unbounded">
                <element ref="saml:Statement"/>
                <element ref="saml:SubjectStatement"/>
                <element ref="saml:AuthenticationStatement"/>
                <element ref="saml:AuthorizationDecisionStatement"/>
                <element ref="saml:AttributeStatement"/>
            </choice>

            <element ref="ds:Signature" minOccurs="0"/>
        </sequence>
        <attribute name="MajorVersion" type="integer" use="required"/>
        <attribute name="MinorVersion" type="integer" use="required"/>
        <attribute name="AssertionID" type="ID" use="required"/>
        <attribute name="Issuer" type="string" use="required"/>
        <attribute name="IssueInstant" type="dateTime" use="required"/>
    </complexType>

 * @author Anil.Saldhana@redhat.com
 * @since Jun 21, 2011
 */
public class SAML11AssertionType extends CommonAssertionType
{
   private static final long serialVersionUID = 1L;

   protected int majorVersion = 1;

   protected int minorVersion = 1;

   protected SAML11ConditionsType conditions;

   protected SAML11AdviceType advice;

   protected List<SAML11StatementAbstractType> statements = new ArrayList<SAML11StatementAbstractType>();

   protected Element signature;

   protected String issuer;

   public SAML11AssertionType(String iD, XMLGregorianCalendar issueInstant)
   {
      super(iD, issueInstant);
   }

   public int getMajorVersion()
   {
      return majorVersion;
   }

   public int getMinorVersion()
   {
      return minorVersion;
   }

   public void add(SAML11StatementAbstractType statement)
   {
      this.statements.add(statement);
   }

   public void addAllStatements(List<SAML11StatementAbstractType> statement)
   {
      this.statements.addAll(statement);
   }

   public boolean remove(SAML11StatementAbstractType statement)
   {
      return this.statements.remove(statement);
   }

   public List<SAML11StatementAbstractType> getStatements()
   {
      return Collections.unmodifiableList(statements);
   }

   public SAML11ConditionsType getConditions()
   {
      return conditions;
   }

   public void setConditions(SAML11ConditionsType conditions)
   {
      this.conditions = conditions;
   }

   public SAML11AdviceType getAdvice()
   {
      return advice;
   }

   public void setAdvice(SAML11AdviceType advice)
   {
      this.advice = advice;
   }

   public Element getSignature()
   {
      return signature;
   }

   public void setSignature(Element signature)
   {
      this.signature = signature;
   }

   public String getIssuer()
   {
      return issuer;
   }

   public void setIssuer(String issuer)
   {
      this.issuer = issuer;
   }
}