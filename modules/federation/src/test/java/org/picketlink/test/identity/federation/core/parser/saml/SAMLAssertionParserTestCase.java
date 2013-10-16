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
package org.picketlink.test.identity.federation.core.parser.saml;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
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

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the parsing of saml assertions
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2010
 */
public class SAMLAssertionParserTestCase extends AbstractParserTest {

    @Test
    public void testSAMLAssertionParsing() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion.xml");

        SAMLParser parser = new SAMLParser();
        AssertionType assertion = (AssertionType) parser.parse(configStream);
        assertNotNull(assertion);

        assertEquals("ID_ab0392ef-b557-4453-95a8-a7e168da8ac5", assertion.getID());
        assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.869Z"), assertion.getIssueInstant());
        // Issuer
        assertEquals("Test STS", assertion.getIssuer().getValue());

        // Subject
        SubjectType subject = assertion.getSubject();

        STSubType subType = subject.getSubType();
        NameIDType subjectNameID = (NameIDType) subType.getBaseID();
        assertEquals("jduke", subjectNameID.getValue());
        assertEquals("urn:picketlink:identity-federation", subjectNameID.getNameQualifier());

        ConditionsType conditions = assertion.getConditions();
        assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.869Z"), conditions.getNotBefore());
        assertEquals(XMLTimeUtil.parse("2010-09-30T21:13:37.869Z"), conditions.getNotOnOrAfter());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Lets do the writing
        SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(assertion);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAMLAssertionParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    /**
     * This test validates the parsing of audience restrictions inside the conditions
     *
     * @throws Exception
     */
    @Test
    public void testSAMLAssertionParsingWithAudienceRestriction() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-audiencerestriction.xml");

        SAMLParser parser = new SAMLParser();
        AssertionType assertion = (AssertionType) parser.parse(configStream);
        assertNotNull(assertion);

        assertEquals("ID_cf9efbf0-9d7f-4b4a-b77f-d83ecaafd374", assertion.getID());
        assertEquals(XMLTimeUtil.parse("2010-09-30T19:13:37.911Z"), assertion.getIssueInstant());
        assertEquals("2.0", assertion.getVersion());

        // Issuer
        assertEquals("Test STS", assertion.getIssuer().getValue());

        // Subject
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
        assertEquals("http://services.testcorp.org/provider2", audienceRestrictionType.getAudience().get(0).toASCIIString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Lets do the writing
        SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(assertion);
        String writtenString = new String(baos.toByteArray());
        Logger.getLogger(SAMLAssertionParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    @Test
    public void testAssertionWithX500Attribute() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-x500attrib.xml");

        SAMLParser parser = new SAMLParser();
        AssertionType assertion = (AssertionType) parser.parse(configStream);
        assertNotNull(assertion);

        assertEquals("ID_b07b804c-7c29-ea16-7300-4f3d6f7928ac", assertion.getID());
        assertEquals(XMLTimeUtil.parse("2004-12-05T09:22:05Z"), assertion.getIssueInstant());
        assertEquals("2.0", assertion.getVersion());

        // Issuer
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

        // Ensure that we have x500:encoding
        QName x500EncodingName = new QName(JBossSAMLURIConstants.X500_NSURI.get(), JBossSAMLConstants.ENCODING.get());
        String encodingValue = attribute.getOtherAttributes().get(x500EncodingName);
        assertEquals("LDAP", encodingValue);

        List<Object> attributeValues = attribute.getAttributeValue();
        assertEquals(2, attributeValues.size());

        String str = (String) attributeValues.get(0);
        if (!(str.equals("member") || str.equals("staff")))
            throw new RuntimeException("attrib value not found");

        // Subject
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
        DocumentUtil.getDocument(bis); // throws exceptions

        String writtenString = new String(bytes);
        Logger.getLogger(SAMLAssertionParserTestCase.class).debug(writtenString);
        validateSchema(writtenString);
    }

    /**
     * PLFED-251
     *
     * @throws Exception
     */
    @Test
    public void testSAML2AssertionWithSubjectConfirmationHavingNameID() throws Exception {
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
     *
     * @throws Exception
     */
    @Test
    public void testSAML2AssertionWithEncryptedID() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-encryptedID.xml");

        SAMLParser parser = new SAMLParser();
        AssertionType assertion = (AssertionType) parser.parse(configStream);
        assertNotNull(assertion);

        // Subject
        SubjectType subject = assertion.getSubject();
        STSubType subType = subject.getSubType();
        EncryptedElementType eet = subType.getEncryptedID();
        assertNotNull(eet);
    }

    /**
     * PLINK2-10 and PLINK-143 : SAML2 Attribute Value should support nested elements
     *
     * @throws Exception
     */
    @Test
    public void testSAMLAssertionWithTestShib() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/saml2/saml2-assertion-testshib.xml");

        SAMLParser parser = new SAMLParser();
        AssertionType assertion = (AssertionType) parser.parse(configStream);
        assertNotNull(assertion);
    }
}