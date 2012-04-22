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
package org.picketlink.identity.federation.saml.v2.assertion;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.common.CommonAssertionType;
import org.w3c.dom.Element;

/**
 <complexType name="AssertionType">
        <sequence>
            <element ref="saml:Issuer"/>
            <element ref="ds:Signature" minOccurs="0"/>
            <element ref="saml:Subject" minOccurs="0"/>
            <element ref="saml:Conditions" minOccurs="0"/>
            <element ref="saml:Advice" minOccurs="0"/>
            <choice minOccurs="0" maxOccurs="unbounded">
                <element ref="saml:Statement"/>
                <element ref="saml:AuthnStatement"/>
                <element ref="saml:AuthzDecisionStatement"/>
                <element ref="saml:AttributeStatement"/>
            </choice>
        </sequence>
        <attribute name="Version" type="string" use="required"/>
        <attribute name="ID" type="ID" use="required"/>
        <attribute name="IssueInstant" type="dateTime" use="required"/>
    </complexType>
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class AssertionType extends CommonAssertionType
{
   private static final long serialVersionUID = 1L;

   private Element signature;

   private final String version = "2.0";

   private AdviceType advice;

   private NameIDType issuer;

   private SubjectType subject;

   private ConditionsType conditions;

   private final Set<StatementAbstractType> statements = new LinkedHashSet<StatementAbstractType>();

   /**
    * Create an assertion
    * @param iD ID of the assertion (Required)
    * @param issueInstant {@link XMLGregorianCalendar} issue instant (required)
    * @param version
    */
   public AssertionType(String iD, XMLGregorianCalendar issueInstant)
   {
      super(iD, issueInstant);
   }

   /**
    * Get the subject
    * @return {@link SubjectType}
    */
   public SubjectType getSubject()
   {
      checkSTSPermission();
      return subject;
   }

   /**
    * Set the subject
    * @param subject
    */
   public void setSubject(SubjectType subject)
   {
      checkSTSPermission();
      this.subject = subject;
   }

   /**
    * Get the version of SAML
    * @return {@link String}
    */
   public String getVersion()
   {
      return version;
   }

   /**
    * Get the advice
    * @return {@link AdviceType}
    */
   public AdviceType getAdvice()
   {
      return advice;
   }

   /**
    * Set the advice
    * @param advice {@link advice}
    */
   public void setAdvice(AdviceType advice)
   {
      checkSTSPermission();

      this.advice = advice;
   }

   /**
    * Get the conditions
    * @return {@link ConditionsType}
    */
   public ConditionsType getConditions()
   {
      checkSTSPermission();

      return conditions;
   }

   /**
    * Set the conditions 
    * @param conditions {@link ConditionsType}
    */
   public void setConditions(ConditionsType conditions)
   {
      checkSTSPermission();

      this.conditions = conditions;
   }

   /**
    * Get the issuer
    * @return {@link NameIDType}
    */
   public NameIDType getIssuer()
   {
      return issuer;
   }

   /**
    * Set the issuer
    * @param issuer {@link NameIDType}
    */
   public void setIssuer(NameIDType issuer)
   {
      checkSTSPermission();

      this.issuer = issuer;
   }

   /**
    * Add a statement
    * @param statement {@link StatementAbstractType}
    */
   public void addStatement(StatementAbstractType statement)
   {
      checkSTSPermission();

      this.statements.add(statement);
   }

   /**
    * Add a collection of statements
    * @param statement {@link Collection}
    */
   public void addStatements(Collection<StatementAbstractType> statement)
   {
      checkSTSPermission();

      this.statements.addAll(statement);
   }

   /**
    * Add a set of statements
    * @param statement {@link Collection}
    */
   public void addStatements(Set<StatementAbstractType> statement)
   {
      checkSTSPermission();

      this.statements.addAll(statement);
   }

   /**
    * Get a read only set of statements
    * @return {@link Set}
    */
   public Set<StatementAbstractType> getStatements()
   {
      checkSTSPermission();

      return Collections.unmodifiableSet(statements);
   }

   /**
    * Get the signature as a DOM element
    * @return {@link Element}
    */
   public Element getSignature()
   {
      return signature;
   }

   /**
    * Set the signature DOM element
    * @param signature
    */
   public void setSignature(Element signature)
   {
      this.signature = signature;
   }

   /**
    * Update the issue instant
    * @param xg
    */
   public void updateIssueInstant(XMLGregorianCalendar xg)
   {
      checkSTSPermission();

      this.issueInstant = xg;
   }

   protected void checkSTSPermission()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(new RuntimePermission("org.picketlink.sts"));
   }
}