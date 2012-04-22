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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedElementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;

/**
 * Test the parsing of saml assertions
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAMLAssertionParserTestCase extends AbstractParserTest
{
   @Test
   public void testSAMLAssertionParsing() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion.xml");

      SAMLParser parser = new SAMLParser();
      AssertionType assertion = (AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      assertEquals("ID_ab0392ef-b557-4453-95a8-a7e168da8ac5", assertion.getID());
      assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.869Z"), assertion.getIssueInstant());
      //Issuer
      assertEquals("Test STS", assertion.getIssuer().getValue());

      //Subject
      SubjectType subject = assertion.getSubject();

      STSubType subType = subject.getSubType();
      NameIDType subjectNameID = (NameIDType) subType.getBaseID();
      assertEquals("jduke", subjectNameID.getValue());
      assertEquals("urn:picketlink:identity-federation", subjectNameID.getNameQualifier());

      ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.869Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2010-09-30T21:13:37.869Z"), conditions.getNotOnOrAfter());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   /**
    * This test validates the parsing of audience restrictions inside the conditions
    * @throws Exception
    */
   @Test
   public void testSAMLAssertionParsingWithAudienceRestriction() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-audiencerestriction.xml");

      SAMLParser parser = new SAMLParser();
      AssertionType assertion = (AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      assertEquals("ID_cf9efbf0-9d7f-4b4a-b77f-d83ecaafd374", assertion.getID());
      assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.911Z"), assertion.getIssueInstant());
      assertEquals("2.0", assertion.getVersion());

      //Issuer
      assertEquals("Test STS", assertion.getIssuer().getValue());

      //Subject
      SubjectType subject = assertion.getSubject();

      STSubType subType = subject.getSubType();
      NameIDType subjectNameID = (NameIDType) subType.getBaseID();
      assertEquals("jduke", subjectNameID.getValue());
      assertEquals("urn:picketlink:identity-federation", subjectNameID.getNameQualifier());

      SubjectConfirmationType subjectConfirmation = subject.getConfirmation().get(0);
      assertEquals("urn:oasis:names:tc:SAML:2.0:cm:bearer", subjectConfirmation.getMethod());

      ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.911Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2010-09-30T21:13:37.911Z"), conditions.getNotOnOrAfter());

      AudienceRestrictionType audienceRestrictionType = (AudienceRestrictionType) conditions.getConditions().get(0);
      assertEquals(1, audienceRestrictionType.getAudience().size());
      assertEquals("http://services.testcorp.org/provider2", audienceRestrictionType.getAudience().get(0)
            .toASCIIString());
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      //Lets do the writing
      SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);
      String writtenString = new String(baos.toByteArray());
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   @Test
   public void testAssertionWithX500Attribute() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-x500attrib.xml");

      SAMLParser parser = new SAMLParser();
      AssertionType assertion = (AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      assertEquals("ID_b07b804c-7c29-ea16-7300-4f3d6f7928ac", assertion.getID());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:22:05Z"), assertion.getIssueInstant());
      assertEquals("2.0", assertion.getVersion());

      //Issuer
      assertEquals("https://idp.example.org/SAML2", assertion.getIssuer().getValue());

      Set<StatementAbstractType> statements = assertion.getStatements();
      assertEquals(2, statements.size());

      Iterator<StatementAbstractType> iter = statements.iterator();
      AuthnStatementType authnStatement = (AuthnStatementType) iter.next();
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:22:00Z"), authnStatement.getAuthnInstant());
      assertEquals("b07b804c-7c29-ea16-7300-4f3d6f7928ac", authnStatement.getSessionIndex());

      AttributeStatementType attributeStatement = (AttributeStatementType) iter.next();
      List<ASTChoiceType> attributes = attributeStatement.getAttributes();
      assertEquals(1, attributes.size());
      AttributeType attribute = attributes.get(0).getAttribute();
      assertEquals("eduPersonAffiliation", attribute.getFriendlyName());
      assertEquals("urn:oid:1.3.6.1.4.1.5923.1.1.1.1", attribute.getName());
      assertEquals("urn:oasis:names:tc:SAML:2.0:attrname-format:uri", attribute.getNameFormat());

      //Ensure that we have x500:encoding
      QName x500EncodingName = new QName(JBossSAMLURIConstants.X500_NSURI.get(), JBossSAMLConstants.ENCODING.get());
      String encodingValue = attribute.getOtherAttributes().get(x500EncodingName);
      assertEquals("LDAP", encodingValue);

      List<Object> attributeValues = attribute.getAttributeValue();
      assertEquals(2, attributeValues.size());

      String str = (String) attributeValues.get(0);
      if (!(str.equals("member") || str.equals("staff")))
         throw new RuntimeException("attrib value not found");

      //Subject
      SubjectType subject = assertion.getSubject();
      STSubType subType = subject.getSubType();
      NameIDType subjectNameID = (NameIDType) subType.getBaseID();
      assertEquals("3f7b3dcf-1674-4ecd-92c8-1544f346baf8", subjectNameID.getValue());
      assertEquals("urn:oasis:names:tc:SAML:2.0:nameid-format:transient", subjectNameID.getFormat().toString());

      SubjectConfirmationType subjectConfirmation = subject.getConfirmation().get(0);
      assertEquals("urn:oasis:names:tc:SAML:2.0:cm:bearer", subjectConfirmation.getMethod());

      SubjectConfirmationDataType subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();
      assertEquals("ID_aaf23196-1773-2113-474a-fe114412ab72", subjectConfirmationData.getInResponseTo());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:27:05Z"), subjectConfirmationData.getNotOnOrAfter());
      assertEquals("https://sp.example.com/SAML2/SSO/POST", subjectConfirmationData.getRecipient());

      ConditionsType conditions = assertion.getConditions();
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:17:05Z"), conditions.getNotBefore());
      assertEquals(XMLTimeUtil.parse("2004-12-05T09:27:05Z"), conditions.getNotOnOrAfter());

      AudienceRestrictionType audienceRestrictionType = (AudienceRestrictionType) conditions.getConditions().get(0);
      assertEquals(1, audienceRestrictionType.getAudience().size());
      assertEquals("https://sp.example.com/SAML2", audienceRestrictionType.getAudience().get(0).toString());

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);

      byte[] bytes = baos.toByteArray();
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
      DocumentUtil.getDocument(bis); //throws exceptions

      String writtenString = new String(bytes);
      System.out.println(writtenString);
      validateSchema(writtenString);
   }

   /**
    * PLFED-251
    * @throws Exception
    */
   @Test
   public void testSAML2AssertionWithSubjectConfirmationHavingNameID() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-subjectconfirmation.xml");

      SAMLParser parser = new SAMLParser();
      AssertionType assertion = (AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      SubjectType subjectType = assertion.getSubject();
      STSubType stType = subjectType.getSubType();
      assertEquals("A_DUDE", ((NameIDType) stType.getBaseID()).getValue());

      List<SubjectConfirmationType> subjectConfirmationTypes = subjectType.getConfirmation();
      assertNotNull(subjectConfirmationTypes);
      assertEquals(1, subjectConfirmationTypes.size());
      SubjectConfirmationType sct = subjectConfirmationTypes.get(0);
      assertEquals("urn:oasis:names:tc:SAML:2.0:cm:sender-vouches", sct.getMethod());
      NameIDType nameID = sct.getNameID();
      assertNotNull(nameID);
      assertEquals("CN=theDUDE", nameID.getValue());

   }

   /**
    * PLFED-252
    * @throws Exception
    */
   @Test
   public void testSAML2AssertionWithEncryptedID() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-encryptedID.xml");

      SAMLParser parser = new SAMLParser();
      AssertionType assertion = (AssertionType) parser.parse(configStream);
      assertNotNull(assertion);

      //Subject
      SubjectType subject = assertion.getSubject();
      STSubType subType = subject.getSubType();
      EncryptedElementType eet = subType.getEncryptedID();
      assertNotNull(eet);
   }
}