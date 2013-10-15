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
package org.picketlink.test.identity.federation.core.parser.wst;

import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Validate the WST Cancel Target for SAML assertions
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSTrustCancelTargetSamlTestCase {

    @Test
    public void testWST_CancelTargetSaml() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-cancel-saml.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);
        assertEquals("cancelcontext", requestToken.getContext());
        assertEquals(WSTrustConstants.CANCEL_REQUEST, requestToken.getRequestType().toASCIIString());

        CancelTargetType cancelTarget = requestToken.getCancelTarget();

        Element assertionElement = (Element) cancelTarget.getAny().get(0);
        AssertionType assertion = SAMLUtil.fromElement(assertionElement);
        validateAssertion(assertion);

        // Now for the writing part
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

        rstWriter.write(requestToken);

        Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
    }

    private void validateAssertion(AssertionType assertion) throws Exception {
        DatatypeFactory dtf = DatatypeFactory.newInstance();

        assertNotNull(assertion);

        assertEquals("ID_cb1eadf5-50a6-4fdf-96bc-412514f52882", assertion.getID());
        assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T19:13:37.603Z"), assertion.getIssueInstant());
        // Issuer
        assertEquals("Test STS", assertion.getIssuer().getValue());

        // Subject
        SubjectType subject = assertion.getSubject();

        NameIDType subjectNameID = (NameIDType) subject.getSubType().getBaseID();

        assertEquals("jduke", subjectNameID.getValue());
        assertEquals("urn:picketlink:identity-federation", subjectNameID.getNameQualifier());

        SubjectConfirmationType subjectConfirmationType = subject.getConfirmation().get(0);
        assertEquals(JBossSAMLURIConstants.BEARER.get(), subjectConfirmationType.getMethod());

        /*
         * List<JAXBElement<?>> content = subject.getContent();
         *
         * int size = content.size();
         *
         * assertEquals( 2, size );
         *
         * for( int i = 0 ; i < size; i++ ) { JAXBElement<?> node = content.get(i); if( node.getDeclaredType().equals(
         * NameIDType.class )) { NameIDType subjectNameID = (NameIDType) node.getValue();
         *
         * assertEquals( "jduke", subjectNameID.getValue() ); assertEquals( "urn:picketlink:identity-federation",
         * subjectNameID.getNameQualifier() ); }
         *
         * if( node.getDeclaredType().equals( SubjectConfirmationType.class )) { SubjectConfirmationType subjectConfirmationType
         * = (SubjectConfirmationType) node.getValue(); assertEquals( JBossSAMLURIConstants.BEARER.get(),
         * subjectConfirmationType.getMethod() ); } }
         */

        // Conditions
        ConditionsType conditions = assertion.getConditions();
        assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T19:13:37.603Z"), conditions.getNotBefore());
        assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T21:13:37.603Z"), conditions.getNotOnOrAfter());
    }
}