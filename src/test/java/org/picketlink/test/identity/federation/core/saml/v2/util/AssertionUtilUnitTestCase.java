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
package org.picketlink.test.identity.federation.core.saml.v2.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;

/**
 * Unit test the AssertionUtil
 * @author Anil.Saldhana@redhat.com
 * @since Jun 3, 2009
 */
public class AssertionUtilUnitTestCase
{
   @Test
   public void testValidAssertion() throws Exception
   {
      NameIDType nameIdType = new NameIDType();
      nameIdType.setValue("somename");

      AssertionType assertion = new AssertionType("SomeID", XMLTimeUtil.getIssueInstant());
      assertion.setIssuer(nameIdType);

      //Assertions with no conditions are everlasting
      assertTrue(AssertionUtil.hasExpired(assertion) == false);

      XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();

      XMLGregorianCalendar sometimeLater = XMLTimeUtil.add(now, 5555);

      ConditionsType conditions = new ConditionsType();
      conditions.setNotBefore(now);
      conditions.setNotOnOrAfter(sometimeLater);
      assertion.setConditions(conditions);
      assertTrue(AssertionUtil.hasExpired(assertion) == false);
   }

   @Test
   public void testExpiredAssertion() throws Exception
   {
      NameIDType nameIdType = new NameIDType();
      nameIdType.setValue("somename");

      AssertionType assertion = new AssertionType("SomeID", XMLTimeUtil.getIssueInstant());
      assertion.setIssuer(nameIdType);

      XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();

      XMLGregorianCalendar sometimeAgo = XMLTimeUtil.subtract(now, 55555);

      ConditionsType conditions = new ConditionsType();
      conditions.setNotBefore(XMLTimeUtil.subtract(now, 55575));
      conditions.setNotOnOrAfter(sometimeAgo);
      assertion.setConditions(conditions);
      assertTrue(AssertionUtil.hasExpired(assertion));
   }

   @Test
   public void testExpiredAssertionWithClockSkew() throws Exception
   {
      NameIDType nameIdType = new NameIDType();
      nameIdType.setValue("somename");

      AssertionType assertion = new AssertionType("SomeID", XMLTimeUtil.getIssueInstant());
      assertion.setIssuer(nameIdType);

      XMLGregorianCalendar now = XMLTimeUtil.getIssueInstant();

      XMLGregorianCalendar sometimeAgo = XMLTimeUtil.subtract(now, 55555);

      ConditionsType conditions = new ConditionsType();
      conditions.setNotBefore(XMLTimeUtil.subtract(now, 55575));
      conditions.setNotOnOrAfter(sometimeAgo);
      assertion.setConditions(conditions);

      assertFalse(AssertionUtil.hasExpired(assertion, 60000));
      assertTrue(AssertionUtil.hasExpired(assertion, 600));
   }

   @Test
   public void testRoleExtraction() throws Exception
   {
      String file = "parser/saml2/saml2-response-assertion-subject.xml";
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
      assertNotNull(is);
      SAMLParser parser = new SAMLParser();
      ResponseType response = (ResponseType) parser.parse(is);
      List<RTChoiceType> assertionList = response.getAssertions();
      assertEquals(1, assertionList.size());
      RTChoiceType rtc = assertionList.get(0);
      AssertionType assertion = rtc.getAssertion();
      List<String> roles = AssertionUtil.getRoles(assertion, null);
      assertEquals(2, roles.size());
      assertTrue(roles.contains("manager"));
      assertTrue(roles.contains("employee"));
   }
}