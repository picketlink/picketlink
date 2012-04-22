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
package org.picketlink.test.identity.federation.core.parser.saml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11AssertionWriter;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AttributeType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AudienceRestrictionCondition;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11ConditionsType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType.SAML11SubjectTypeChoice;
import org.w3c.dom.Element;

/**
 * Unit Test the parsing of SAML 1.1 assertion
 * @author Anil.Saldhana@redhat.com
 * @since Jun 21, 2011
 */
public class SAML11AssertionParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAML11Assertion() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-assertion.xml");

      SAMLParser parser = new SAMLParser();
      SAML11AssertionType assertion = (SAML11AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Validate assertion
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());
      assertEquals("buGxcG4gILg5NlocyLccDz6iXrUa", assertion.getID());
      assertEquals("https://idp.example.org/saml", assertion.getIssuer());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), assertion.getIssueInstant());

      SAML11ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:00:37.795Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:10:37.795Z"), conditions.getNotOnOrAfter());

      SAML11AuthenticationStatementType stat = (SAML11AuthenticationStatementType) assertion.getStatements().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:am:password", stat.getAuthenticationMethod().toString());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:17.706Z"), stat.getAuthenticationInstant());

      SAML11SubjectType subject = stat.getSubject();
      SAML11SubjectType.SAML11SubjectTypeChoice choice = subject.getChoice();
      assertEquals("user@idp.example.org", choice.getNameID().getValue());
      assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", choice.getNameID().getFormat().toString());

      SAML11SubjectConfirmationType subjectConfirm = subject.getSubjectConfirmation();
      URI confirmationMethod = subjectConfirm.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:bearer", confirmationMethod.toString());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testSAML11AssertionWithAttributeStatements() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-assertion-attribstat.xml");

      SAMLParser parser = new SAMLParser();
      SAML11AssertionType assertion = (SAML11AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Validate assertion
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());
      assertEquals("buGxcG4gILg5NlocyLccDz6iXrUb", assertion.getID());
      assertEquals("https://idp.example.org/saml", assertion.getIssuer());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), assertion.getIssueInstant());

      SAML11ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:15:37.795Z"), conditions.getNotOnOrAfter());

      SAML11AuthenticationStatementType stat = (SAML11AuthenticationStatementType) assertion.getStatements().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:am:password", stat.getAuthenticationMethod().toString());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:08:37.795Z"), stat.getAuthenticationInstant());

      SAML11SubjectType subject = stat.getSubject();
      SAML11SubjectType.SAML11SubjectTypeChoice choice = subject.getChoice();
      assertEquals("user@idp.example.org", choice.getNameID().getValue());
      assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", choice.getNameID().getFormat().toString());

      SAML11SubjectConfirmationType subjectConfirm = subject.getSubjectConfirmation();
      URI confirmationMethod = subjectConfirm.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:bearer", confirmationMethod.toString());

      SAML11AttributeStatementType attribStat = (SAML11AttributeStatementType) assertion.getStatements().get(1);
      assertNotNull(attribStat);
      subject = attribStat.getSubject();

      choice = subject.getChoice();
      assertEquals("user@idp.example.org", choice.getNameID().getValue());
      assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", choice.getNameID().getFormat().toString());

      subjectConfirm = subject.getSubjectConfirmation();
      confirmationMethod = subjectConfirm.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:bearer", confirmationMethod.toString());

      List<SAML11AttributeType> attribs = attribStat.get();
      assertEquals(1, attribs.size());
      SAML11AttributeType attrib = attribs.get(0);
      assertEquals("urn:mace:dir:attribute-def:eduPersonAffiliation", attrib.getAttributeName());
      assertEquals("urn:mace:shibboleth:1.0:attributeNamespace:uri", attrib.getAttributeNamespace().toString());

      List<Object> attribValues = attrib.get();
      assertTrue(attribValues.contains("member"));
      assertTrue(attribValues.contains("student"));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testSAML11AssertionWithAuthzDecisionStatement() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-assertion-authzdecision.xml");

      SAMLParser parser = new SAMLParser();
      SAML11AssertionType assertion = (SAML11AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Validate assertion
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());
      assertEquals("buGxcG4gILg5NlocyLccDz6iXrUb", assertion.getID());
      assertEquals("https://idp.example.org/saml", assertion.getIssuer());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), assertion.getIssueInstant());

      SAML11ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:05:37.795Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2002-06-19T17:15:37.795Z"), conditions.getNotOnOrAfter());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testSAML11AssertionWithAuthAndAuthz() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-assertion-auth-authz.xml");

      SAMLParser parser = new SAMLParser();
      SAML11AssertionType assertion = (SAML11AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Validate assertion
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());
      assertEquals("_e5c23ff7a3889e12fa01802a47331653", assertion.getID());
      assertEquals("localhost", assertion.getIssuer());
      assertEquals(XMLTimeUtil.parse("2008-12-10T14:12:14.817Z"), assertion.getIssueInstant());

      SAML11ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2008-12-10T14:12:14.817Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2008-12-10T14:12:44.817Z"), conditions.getNotOnOrAfter());
      List<SAML11ConditionAbstractType> theConditions = conditions.get();
      assertEquals(1, theConditions.size());
      SAML11AudienceRestrictionCondition restrictCond = (SAML11AudienceRestrictionCondition) theConditions.get(0);
      assertEquals("https://some-service.example.com/app/", restrictCond.get().get(0).toString());

      List<SAML11StatementAbstractType> statements = assertion.getStatements();
      assertEquals(2, statements.size());

      SAML11AttributeStatementType attrStat = (SAML11AttributeStatementType) statements.get(0);
      SAML11SubjectType subject = attrStat.getSubject();
      SAML11SubjectTypeChoice choice = subject.getChoice();
      SAML11NameIdentifierType nameID = choice.getNameID();
      assertEquals("johnq", nameID.getValue());
      SAML11SubjectConfirmationType subjConf = subject.getSubjectConfirmation();
      URI confirmationMethod = subjConf.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:artifact", confirmationMethod.toString());

      List<SAML11AttributeType> attributes = attrStat.get();
      assertEquals(4, attributes.size());
      SAML11AttributeType attr = attributes.get(0);
      assertEquals("uid", attr.getAttributeName());
      assertEquals("http://jboss.org/test", attr.getAttributeNamespace().toString());
      assertEquals("12345", attr.get().get(0));

      attr = attributes.get(1);
      assertEquals("groupMembership", attr.getAttributeName());
      assertEquals("http://jboss.org/test", attr.getAttributeNamespace().toString());
      assertEquals("uugid=middleware.staff,ou=Groups,dc=vt,dc=edu", attr.get().get(0));

      attr = attributes.get(2);
      assertEquals("eduPersonAffiliation", attr.getAttributeName());
      assertEquals("http://jboss.org/test", attr.getAttributeNamespace().toString());
      assertEquals("staff", attr.get().get(0));

      attr = attributes.get(3);
      assertEquals("accountState", attr.getAttributeName());
      assertEquals("http://jboss.org/test", attr.getAttributeNamespace().toString());
      assertEquals("ACTIVE", attr.get().get(0));

      SAML11AuthenticationStatementType authStat = (SAML11AuthenticationStatementType) statements.get(1);
      assertEquals(XMLTimeUtil.parse("2008-12-10T14:12:14.741Z"), authStat.getAuthenticationInstant());
      assertEquals("urn:oasis:names:tc:SAML:1.0:am:password", authStat.getAuthenticationMethod().toString());
      subject = authStat.getSubject();
      choice = subject.getChoice();
      nameID = choice.getNameID();
      assertEquals("johnq", nameID.getValue());
      subjConf = subject.getSubjectConfirmation();
      confirmationMethod = subjConf.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:artifact", confirmationMethod.toString());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testSAML11AssertionWithKeyInfo() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml1/saml1-assertion-keyinfo.xml");

      SAMLParser parser = new SAMLParser();
      SAML11AssertionType assertion = (SAML11AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Validate assertion
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());
      assertEquals("s69f7e2599d4eb0c548782432bf", assertion.getID());
      assertEquals("http://jboss.org/test", assertion.getIssuer());
      assertEquals(XMLTimeUtil.parse("2006-05-24T05:52:32Z"), assertion.getIssueInstant());

      List<SAML11StatementAbstractType> statements = assertion.getStatements();
      assertEquals(1, statements.size());
      SAML11AuthenticationStatementType authStat = (SAML11AuthenticationStatementType) statements.get(0);
      assertEquals(XMLTimeUtil.parse("2006-05-24T05:52:30Z"), authStat.getAuthenticationInstant());
      assertEquals("urn:picketlink:auth", authStat.getAuthenticationMethod().toString());
      SAML11SubjectType subject = authStat.getSubject();
      SAML11SubjectTypeChoice choice = subject.getChoice();
      SAML11NameIdentifierType nameID = choice.getNameID();
      assertEquals("anil", nameID.getValue());
      SAML11SubjectConfirmationType subjConf = subject.getSubjectConfirmation();
      URI confirmationMethod = subjConf.getConfirmationMethod().get(0);
      assertEquals("urn:oasis:names:tc:SAML:1.0:cm:holder-of-key", confirmationMethod.toString());
      assertNotNull(subjConf.getKeyInfo());

      Element sig = assertion.getSignature();
      assertNotNull(sig);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }
}