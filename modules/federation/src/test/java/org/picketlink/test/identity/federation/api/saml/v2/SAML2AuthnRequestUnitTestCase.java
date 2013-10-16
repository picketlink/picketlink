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
package org.picketlink.test.identity.federation.api.saml.v2;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestedAuthnContextType;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test the SAML2 Authn Request Context constructs
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 8, 2008
 */
public class SAML2AuthnRequestUnitTestCase {

    /**
     * Test reading a saml2 authn request
     *
     * @throws Exception
     */
    @Test
    public void testAuthnRequestExample() throws Exception {
        String resourceName = "saml/v2/authnrequest/samlAuthnRequestExample.xml";

        SAML2Request request = new SAML2Request();

        AuthnRequestType authnRequestType = request.getAuthnRequestType(resourceName);

        assertEquals("http://www.example.com/", authnRequestType.getDestination().toString());
        assertEquals("urn:oasis:names:tc:SAML:2.0:consent:obtained", authnRequestType.getConsent());
        assertEquals("http://www.example.com/", authnRequestType.getAssertionConsumerServiceURL().toString());
        assertEquals(Integer.valueOf("0"), authnRequestType.getAttributeConsumingServiceIndex());

        SubjectType subjectType = authnRequestType.getSubject();
        assertNotNull(subjectType);

        STSubType subType = subjectType.getSubType();
        NameIDType nameIDType = (NameIDType) subType.getBaseID();

        assertEquals("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", nameIDType.getFormat().toString());
        assertEquals("j.doe@company.com", nameIDType.getValue());

        ConditionsType conditionsType = authnRequestType.getConditions();
        List<ConditionAbstractType> conditions = conditionsType.getConditions();
        assertTrue(conditions.size() == 1);

        ConditionAbstractType condition = conditions.get(0);
        assertTrue(condition instanceof AudienceRestrictionType);
        AudienceRestrictionType audienceRestrictionType = (AudienceRestrictionType) condition;
        List<URI> audiences = audienceRestrictionType.getAudience();
        assertTrue(audiences.size() == 1);
        assertEquals("urn:foo:sp.example.org", audiences.get(0).toASCIIString());

        RequestedAuthnContextType requestedAuthnContext = authnRequestType.getRequestedAuthnContext();
        assertEquals("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", requestedAuthnContext
                .getAuthnContextClassRef().get(0));

        // Let us marshall it back to an output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        request.marshall(authnRequestType, baos);
    }

    /**
     * Test reading a saml authn request from a file that contains a digital signature
     *
     * @throws Exception
     */
    @Test
    public void testAuthnRequestWithSignature() throws Exception {
        String resourceName = "saml/v2/authnrequest/samlAuthnRequestWithSignature.xml";

        SAML2Request request = new SAML2Request();

        AuthnRequestType authnRequestType = request.getAuthnRequestType(resourceName);
        assertNotNull(authnRequestType);

        Element signatureType = authnRequestType.getSignature();
        assertNotNull("Signature is not null", signatureType);

        // Let us marshall it back to an output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        request.marshall(authnRequestType, baos);
    }

    /**
     * Test the creation of AuthnRequestType
     *
     * @throws Exception
     */
    @Test
    public void testAuthnRequestCreation() throws Exception {
        String id = IDGenerator.create("ID_");

        SAML2Request request = new SAML2Request();
        AuthnRequestType authnRequest = request.createAuthnRequestType(id, "http://sp", "http://idp", "http://sp");

        // Verify whether NameIDPolicy exists
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertNotNull("NameIDPolicy is not null", nameIDPolicy);
        assertTrue(nameIDPolicy.isAllowCreate());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        request.marshall(authnRequest, baos);
    }
}