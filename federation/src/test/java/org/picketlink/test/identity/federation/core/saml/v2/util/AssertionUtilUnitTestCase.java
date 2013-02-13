/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 3, 2009
 */
public class AssertionUtilUnitTestCase {
    @Test
    public void testValidAssertion() throws Exception {
        NameIDType nameIdType = new NameIDType();
        nameIdType.setValue("somename");

        AssertionType assertion = new AssertionType("SomeID", XMLTimeUtil.getIssueInstant());
        assertion.setIssuer(nameIdType);

        // Assertions with no conditions are everlasting
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
    public void testExpiredAssertion() throws Exception {
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
    public void testExpiredAssertionWithClockSkew() throws Exception {
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
    public void testRoleExtraction() throws Exception {
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