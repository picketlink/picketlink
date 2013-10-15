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

import org.jboss.logging.Logger;
import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.SignatureMethod;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Signatures related unit test cases
 *
 * @author Anil.Saldhana@redhat.com
 * @since Dec 15, 2008
 */
public class SignatureValidationUnitTestCase {

    /**
     * Test the creation of AuthnRequestType with signature creation with a private key and then validate the signature
     * with a
     * public key
     *
     * @throws Exception
     */
    @Test
    public void testAuthnRequestCreationWithSignature() throws Exception {
        SAML2Request saml2Request = new SAML2Request();
        String id = IDGenerator.create("ID_");
        String assertionConsumerURL = "http://sp";
        String destination = "http://idp";
        String issuerValue = "http://sp";
        AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination, issuerValue);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        KeyPair kp = kpg.genKeyPair();

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
        Document signedDoc = ss.sign(authnRequest, kp);

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug("Signed Doc:" + DocumentUtil.asString(signedDoc));

        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
        assertTrue(isValid);
    }

    /**
     * Test the creation of AuthnRequestType with signature creation with a private key and then validate the signature
     * with a
     * public key. We test that the signature does not contain the keyinfo
     *
     * @throws Exception
     */
    @Test
    public void testNoKeyInfo() throws Exception {
        SAML2Request saml2Request = new SAML2Request();
        String id = IDGenerator.create("ID_");
        String assertionConsumerURL = "http://sp";
        String destination = "http://idp";
        String issuerValue = "http://sp";
        AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination, issuerValue);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        KeyPair kp = kpg.genKeyPair();

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureIncludeKeyInfo(false);

        ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
        Document signedDoc = ss.sign(authnRequest, kp);

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug("Signed Doc:" + DocumentUtil.asString(signedDoc));

        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
        assertTrue(isValid);
        XMLSignatureUtil.setIncludeKeyInfoInSignature(true);
    }

    /**
     * Test the signature for ResponseType
     *
     * @throws Exception
     */
    @Test
    public void testSigningResponse() throws Exception {
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("testIssuer");
        String id = IDGenerator.create("ID_");

        SAML2Response response = new SAML2Response();

        String authnContextDeclRef = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();

        AuthnStatementType authnStatement = response.createAuthnStatement(authnContextDeclRef, XMLTimeUtil.getIssueInstant());

        // Create an assertion
        AssertionType assertion = response.createAssertion(id, issuerInfo.getIssuer());
        assertion.addStatement(authnStatement);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        KeyPair kp = kpg.genKeyPair();

        id = IDGenerator.create("ID_"); // regenerate
        ResponseType responseType = response.createResponseType(id, issuerInfo, assertion);

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
        Document signedDoc = ss.sign(responseType, kp);

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug(DocumentUtil.asString(signedDoc));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
        assertTrue(isValid);
    }

    @Test
    public void testSigningAnAssertionWithinResponse() throws Exception {
        SAML2Response response = new SAML2Response();
        String fileName = "xml/dom/saml-response-2-assertions.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");

        ResponseType responseType = response.getResponseType(is);

        Document doc = response.convert(responseType);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair kp = kpg.genKeyPair();

        // String id = "ID_0be488d8-7089-4892-8aeb-83594c800706";
        String id = "ID_976d8310-658a-450d-be39-f33c73c8afa6";

        // Get the second assertion
        Node assert2 = DocumentUtil.getNodeWithAttribute(doc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "ID", id);

        String referenceURI = "#" + id;

        assertNotNull("Found assertion?", assert2);
        SAML2Signature ss = new SAML2Signature();
        Document signedDoc = ss.sign(responseType, id, kp, referenceURI);

        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        Node signedNode = DocumentUtil.getNodeWithAttribute(signedDoc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion",
                "ID", id);

        // Let us just validate the signature of the assertion
        Document validatingDoc = DocumentUtil.createDocument();
        Node importedSignedNode = validatingDoc.importNode(signedNode.getOwnerDocument().getFirstChild(), true);
        validatingDoc.appendChild(importedSignedNode);

        // set IDness in validating document
        Element assertionEl = (Element) DocumentUtil.getNodeWithAttribute(validatingDoc,
                "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "ID", id);
        assertionEl.setIdAttribute("ID", true);

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(validatingDoc, kp.getPublic());
        assertTrue("Signature is valid:", isValid);

        /**
         * Now the signed document is marshalled across the wire using dom write
         */
        // Binder<Node> binder = response.getBinder();
        // We have to parse the dom coming from the stream and feed to binder
        Document readDoc = DocumentUtil.getDocument(DocumentUtil.getNodeAsStream(signedDoc));

        signedNode = DocumentUtil.getNodeWithAttribute(readDoc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "ID", id);

        // The client creates a validating document, importing the signed assertion.
        validatingDoc = DocumentUtil.createDocument();
        importedSignedNode = validatingDoc.importNode(signedNode.getOwnerDocument().getFirstChild(), true);
        validatingDoc.appendChild(importedSignedNode);

        // set IDness in validating document
        assertionEl = (Element) DocumentUtil.getNodeWithAttribute(validatingDoc, "urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion", "ID", id);
        assertionEl.setIdAttribute("ID", true);

        // The client re-validates the signature.
        assertTrue("Signature is valid:", XMLSignatureUtil.validate(validatingDoc, kp.getPublic()));
    }

    /**
     * Test signing a string
     *
     * @throws Exception
     */
    @Test
    public void testStringContentSignature() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        KeyPair kp = kpg.genKeyPair();

        String arbitContent = "I am A String";

        byte[] sigVal = SignatureUtil.sign(arbitContent, kp.getPrivate());

        boolean valid = SignatureUtil.validate(arbitContent.getBytes(), sigVal, kp.getPublic());
        assertTrue(valid);
    }

    @Test
    public void testX509DataInSignedInfo() throws Exception {
        SAML2Request saml2Request = new SAML2Request();
        String id = IDGenerator.create("ID_");
        String assertionConsumerURL = "http://sp";
        String destination = "http://idp";
        String issuerValue = "http://sp";
        AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination, issuerValue);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        KeyPair kp = kpg.genKeyPair();
        PublicKey publicKey = kp.getPublic();

        X509Certificate x509 = getCertificate();

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
        ss.setX509Certificate(x509);
        Document signedDoc = ss.sign(authnRequest, kp);

        //Ensure that we have the certificate in key info
        NodeList x509DataList = signedDoc.getElementsByTagName("dsig:X509Data");
        assertEquals(1, x509DataList.getLength());

        Element x509Data = (Element) x509DataList.item(0);

        assertTrue(x509Data != null);

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug("Signed Doc:" + DocumentUtil.asString(signedDoc));

        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
        assertTrue(isValid);
    }

    private X509Certificate getCertificate() throws Exception {
        String certificateString = "MIICQDCCAakCBEeNB0swDQYJKoZIhvcNAQEEBQAwZzELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNh\n"
                + "bGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYXJhMQwwCgYDVQQKEwNTdW4xEDAOBgNVBAsTB09w\n"
                + "ZW5TU08xDTALBgNVBAMTBHRlc3QwHhcNMDgwMTE1MTkxOTM5WhcNMTgwMTEyMTkxOTM5WjBnMQsw\n"
                + "CQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxMLU2FudGEgQ2xhcmExDDAK\n"
                + "BgNVBAoTA1N1bjEQMA4GA1UECxMHT3BlblNTTzENMAsGA1UEAxMEdGVzdDCBnzANBgkqhkiG9w0B\n"
                + "AQEFAAOBjQAwgYkCgYEArSQc/U75GB2AtKhbGS5piiLkmJzqEsp64rDxbMJ+xDrye0EN/q1U5Of+\n"
                + "RkDsaN/igkAvV1cuXEgTL6RlafFPcUX7QxDhZBhsYF9pbwtMzi4A4su9hnxIhURebGEmxKW9qJNY\n"
                + "Js0Vo5+IgjxuEWnjnnVgHTs1+mq5QYTA7E6ZyL8CAwEAATANBgkqhkiG9w0BAQQFAAOBgQB3Pw/U\n"
                + "QzPKTPTYi9upbFXlrAKMwtFf2OW4yvGWWvlcwcNSZJmTJ8ARvVYOMEVNbsT4OFcfu2/PeYoAdiDA\n"
                + "cGy/F2Zuj8XJJpuQRSE6PtQqBuDEHjjmOQJ0rV/r8mO1ZCtHRhpZ5zYRjhRC9eCbjx9VrFax0JDC\n" + "/FfwWigmrW0Y0Q==";
        X509Certificate cert = null;
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN CERTIFICATE-----\n").append(certificateString).append("\n-----END CERTIFICATE-----");

        String derFormattedString = builder.toString();

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bais = new ByteArrayInputStream(derFormattedString.getBytes());

        while (bais.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bais);
        }

        return cert;
    }
}