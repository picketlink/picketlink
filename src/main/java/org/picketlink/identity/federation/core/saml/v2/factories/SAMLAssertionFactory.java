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

import java.net.URI;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.KeyInfoConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;

/**
 * Deal with {@code AssertionType}
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
public class SAMLAssertionFactory
{
   /**
    * <p>
    * Creates an {@code AudienceRestrictionType} with the specified values.
    * </p>
    * 
    * @param values a {@code String[]} containing the restriction values.
    * @return the constructed {@code AudienceRestrictionType} instance.
    */
   public static AudienceRestrictionType createAudienceRestriction(String... values)
   {
      AudienceRestrictionType audienceRestriction = new AudienceRestrictionType();
      if (values != null)
      {
         for (String val : values)
         {
            audienceRestriction.addAudience(URI.create(val));
         }
      }
      return audienceRestriction;
   }

   /**
    * <p>
    * Creates a {@code NameIDType} instance with the specified values.
    * </p>
    * 
    * @param format a {@code String} representing the name format.
    * @param qualifier a {@code String} representing the name qualifier.
    * @param value a {@code String} representing the name value.
    * @return the constructed {@code NameIDType} instance.
    */
   public static NameIDType createNameID(String format, String qualifier, String value)
   {
      NameIDType nameID = new NameIDType();
      if (format != null)
         nameID.setFormat(URI.create(format));
      nameID.setNameQualifier(qualifier);
      nameID.setValue(value);
      return nameID;
   }

   /**
    * <p>
    * Creates a {@code Conditions} instance with the specified values.
    * </p>
    * 
    * @param notBefore a {@code XMLGregorianCalendar} representing the start of the token lifetime period.
    * @param notOnOrAfter a {@code XMLGregorianCalendar} representing the end of the token lifetime period.
    * @param restrictions an array containing the applicable restrictions.
    * @return the constructed {@code Conditions} instance.
    */
   public static ConditionsType createConditions(XMLGregorianCalendar notBefore, XMLGregorianCalendar notOnOrAfter,
         ConditionAbstractType... restrictions)
   {
      ConditionsType conditions = new ConditionsType();
      conditions.setNotBefore(notBefore);
      conditions.setNotOnOrAfter(notOnOrAfter);
      if (restrictions != null)
      {
         for (ConditionAbstractType condition : restrictions)
         {
            conditions.addCondition(condition);
         }

      }
      return conditions;
   }

   /**
    * <p>
    * Creates a {@code KeyInfoConfirmationDataType} with the specified {@code KeyInfoType}.
    * </p>
    * 
    * @param keyInfo the {@code KeyInfoType} object that wraps the proof-of-possession token.
    * @return the constructed {@code KeyInfoConfirmationDataType} instance.
    */
   public static KeyInfoConfirmationDataType createKeyInfoConfirmation(KeyInfoType keyInfo)
   {
      KeyInfoConfirmationDataType type = new KeyInfoConfirmationDataType();
      type.setAnyType(keyInfo);
      return type;
   }

   /**
    * <p>
    * Creates a {@code SubjectConfirmationType} object with the specified values.
    * </p>
    * 
    * @param nameID the identifier of the confirmation.
    * @param confirmationMethod a {@code String} representing the confirmation method.
    * @param keyInfoData the {@code KeyInfoConfirmationDataType} instance that contains the proof of possession key.
    * @return the constructed {@code SubjectConfirmationType} instance.
    */
   public static SubjectConfirmationType createSubjectConfirmation(NameIDType nameID, String confirmationMethod,
         KeyInfoConfirmationDataType keyInfoData)
   {
      SubjectConfirmationType subjectConfirmation = new SubjectConfirmationType();
      subjectConfirmation.setNameID(nameID);
      subjectConfirmation.setMethod(confirmationMethod);
      subjectConfirmation.setSubjectConfirmationData(keyInfoData);
      return subjectConfirmation;
   }

   /**
    * <p>
    * Creates a {@code SubjectType} object with the specified values.
    * </p>
    * 
    * @param nameID the identifier of the subject.
    * @param confirmation the {@code SubjectConfirmationType} that is used to establish the correspondence between the
    *            subject and claims of SAML statements.
    * @return the constructed {@code SubjectType} instance.
    */
   public static SubjectType createSubject(NameIDType nameID, SubjectConfirmationType confirmation)
   {
      SubjectType subject = new SubjectType();
      if (nameID != null)
      {
         SubjectType.STSubType subType = new SubjectType.STSubType();
         subType.addConfirmation(confirmation);
         subType.addBaseID(nameID);
         subject.setSubType(subType);
      }
      return subject;
   }

   /**
    * <p>
    * Creates a SAMLV2 {@code AssertionType} with the specified values.
    * </p>
    * 
    * @param id a {@code String} representing the assertion ID.
    * @param issuerID a {@code NameIDType} that identifies the assertion issuer.
    * @param issueInstant the assertion time of creation.
    * @param conditions the {@code ConditionsType} that specify the conditions under which the assertion is to be
    *            considered valid
    * @param subject the {@code SubjectType} that identifies the authenticated principal.
    * @param statements a list of statements associated with the authenticated principal.
    * @return
    */
   public static AssertionType createAssertion(String id, NameIDType issuerID, XMLGregorianCalendar issueInstant,
         ConditionsType conditions, SubjectType subject, List<StatementAbstractType> statements)
   {
      AssertionType assertion = new AssertionType(id, issueInstant);
      assertion.setIssuer(issuerID);
      if (conditions != null)
         assertion.setConditions(conditions);
      if (subject != null)
         assertion.setSubject(subject);

      if (statements != null)
      {
         for (StatementAbstractType statement : statements)
         {
            assertion.addStatement(statement);
         }
      }
      return assertion;
   }
}