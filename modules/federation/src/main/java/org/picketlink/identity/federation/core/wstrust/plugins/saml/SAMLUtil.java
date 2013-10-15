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
package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11AssertionWriter;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;

/**
 * <p>
 * This class contains utility methods and constants that are used by the SAML token providers.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SAMLUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    public static final String SAML11_BEARER_URI = "urn:oasis:names:tc:SAML:1.0:cm:bearer";

    public static final String SAML11_HOLDER_OF_KEY_URI = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";

    public static final String SAML11_SENDER_VOUCHES_URI = "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";

    public static final String SAML2_BEARER_URI = "urn:oasis:names:tc:SAML:2.0:cm:bearer";

    public static final String SAML2_HOLDER_OF_KEY_URI = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";

    public static final String SAML2_SENDER_VOUCHES_URI = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

    public static final String SAML11_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";

    public static final String SAML11_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID";

    public static final String SAML2_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";

    public static final String SAML2_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID";

    /**
     * <p>
     * Utility method that marshals the specified {@code AssertionType} object into an {@code Element} instance.
     * </p>
     *
     * @param assertion an {@code AssertionType} object representing the SAML assertion to be marshaled.
     *
     * @return a reference to the {@code Element} that contains the marshaled SAML assertion.
     *
     * @throws Exception if an error occurs while marshaling the assertion.
     */
    public static Element toElement(AssertionType assertion) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(assertion);

        byte[] assertionBytes = baos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(assertionBytes);
        Document document = DocumentUtil.getDocument(bis);

        if (logger.isTraceEnabled()) {
            logger.samlAssertion(DocumentUtil.asString(document));
        }

        return document.getDocumentElement();
    }

    /**
     * <p>
     * Utility method that marshals the specified {@code AssertionType} object into an {@code Element} instance.
     * </p>
     *
     * @param assertion an {@code AssertionType} object representing the SAML assertion to be marshaled.
     *
     * @return a reference to the {@code Element} that contains the marshaled SAML assertion.
     *
     * @throws Exception if an error occurs while marshaling the assertion.
     */
    public static Element toElement(SAML11AssertionType assertion) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
        writer.write(assertion);

        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        Document document = DocumentUtil.getDocument(bis);

        return document.getDocumentElement();
    }

    /**
     * <p>
     * Utility method that unmarshals the specified {@code Element} into an {@code AssertionType} instance.
     * </p>
     *
     * @param assertionElement the {@code Element} that contains the marshaled SAMLV2.0 assertion.
     *
     * @return a reference to the unmarshaled {@code AssertionType} instance.
     *
     * @throws ConfigurationException
     * @throws ProcessingException
     * @throws ParsingException
     */
    public static AssertionType fromElement(Element assertionElement) throws ProcessingException, ConfigurationException,
            ParsingException {
        SAMLParser samlParser = new SAMLParser();

        JAXPValidationUtil.checkSchemaValidation(assertionElement);
        AssertionType assertion = (AssertionType) samlParser.parse(DocumentUtil.getNodeAsStream(assertionElement));
        return assertion;
    }

    /**
     * Given a {@link Element} that represents a SAML 1.1 assertion, convert it into a {@link SAML11AssertionType}
     *
     * @param assertionElement
     *
     * @return
     *
     * @throws GeneralSecurityException
     */
    public static SAML11AssertionType saml11FromElement(Element assertionElement) throws GeneralSecurityException {
        SAMLParser samlParser = new SAMLParser();

        JAXPValidationUtil.checkSchemaValidation(assertionElement);
        return (SAML11AssertionType) samlParser.parse(DocumentUtil.getNodeAsStream(assertionElement));
    }
}