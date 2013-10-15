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
package org.picketlink.test.identity.federation.core.saml.v2;

import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.test.identity.federation.api.saml.v2.SignatureValidationUnitTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignatureException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * This test case demonstrates how PicketLink behaves when a XML Signature Wrapping Attack is performed using a SAML
 * Response
 * document. It also forces a successful attack.
 * </p>
 * <p>
 * What is protecting PicketLink to the XML Signature Wrapping Attack is how the idness of attributes is configured for
 * XML elements. PicketLink
 * expects to manually set the idness of attributes after Apache Santuario version update.
 * </p>
 * <p>
 * It is strongly recommended to use signatures when configuring IDPs and SPs.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class SAMLAssertionWrappingAttackTestCase {

    private String keystoreLocation = "keystore/jbid_test_keystore.jks";
    private String keystorePass = "store123";
    private String alias = "servercert";
    private String keyPass = "test123";
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Before
    public void onSetup() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
        assertNotNull("Input keystore stream is not null", ksStream);

        KeyStore ks = KeyStoreUtil.getKeyStore(ksStream, keystorePass.toCharArray());
        assertNotNull("KeyStore is not null", ks);

        // Check that there are aliases in the keystore
        Enumeration<String> aliases = ks.aliases();
        assertTrue("Aliases are not empty", aliases.hasMoreElements());

        this.publicKey = KeyStoreUtil.getPublicKey(ks, alias, keyPass.toCharArray());
        assertNotNull("Public Key is not null", publicKey);

        this.privateKey = (PrivateKey) ks.getKey(alias, keyPass.toCharArray());
    }

    /**
     * <p>
     * Tests if PicketLink is blinded for XML Signature Wrapping Attacks when using a SAML Response. In this case an
     * exception
     * should be throw because the ID used to reference the signed Response will not be found. This tests shows how
     * PicketLink
     * reacts when a XML Signature Wrapping Attack is performed.
     * </p>
     *
     * @throws Exception
     */
    @Test(expected = XMLSignatureException.class)
    public void testWrappingAttack() throws Exception {
        ResponseType responseType = createSignedResponse();

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureMethod(SignatureMethod.RSA_SHA1);
        Document signedDoc = ss.sign(responseType, new KeyPair(publicKey, privateKey));

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug(DocumentUtil.asString(signedDoc));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, publicKey);

        assertTrue(isValid);

        // now let's change the response document and wrap a another SAML assertion

        // clone the whole document. The root element is the Response
        Document clonedResponse = (Document) signedDoc.cloneNode(true);

        // let's remove the Signature from the cloned response
        Element signature = (Element) clonedResponse.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature")
                .item(0);

        signature.getParentNode().removeChild(signature);

        // let's remove the original assertion. Later it will be replaced by a another one.
        Element originalAssertion = (Element) signedDoc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion").item(0);

        originalAssertion.getParentNode().removeChild(originalAssertion);

        // let's load a forged assertion
        String fileName = "saml2-wrapping-attack.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        Document evilAssertion = DocumentUtil.getDocument(is);

        // let's wrap the forged assertion into the original document.
        Element element = evilAssertion.getDocumentElement();

        Node adoptNode = signedDoc.adoptNode(element);

        signedDoc.getDocumentElement().appendChild(adoptNode);

        // let's append the cloned response document as a child of the original Signature element
        Element signatureOriginal = (Element) signedDoc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#",
                "Signature").item(0);

        Element importedClonedResponse = (Element) signatureOriginal.getOwnerDocument().adoptNode(
                clonedResponse.getDocumentElement());

        signatureOriginal.appendChild(importedClonedResponse);

        // let's change the original response ID attribute value
        signedDoc.getDocumentElement().setAttribute("ID", "evilAssertion");

        // validate the original response with the wrapped assertion
        isValid = XMLSignatureUtil.validate(signedDoc, publicKey);

        assertTrue(false);
    }

    /**
     * <p>
     * Forces the XML Signature Wrapping Attack. This test creates a valid SAML Response properly signed.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testForceWrappingAttack() throws Exception {
        ResponseType responseType = createSignedResponse();

        SAML2Signature ss = new SAML2Signature();
        ss.setSignatureMethod(SignatureMethod.RSA_SHA1);
        Document signedDoc = ss.sign(responseType, new KeyPair(publicKey, privateKey));

        Logger.getLogger(SignatureValidationUnitTestCase.class).debug(DocumentUtil.asString(signedDoc));
        JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(signedDoc));

        // Validate the signature
        boolean isValid = XMLSignatureUtil.validate(signedDoc, publicKey);

        assertTrue(isValid);

        // now let's change the response document and wrap a another SAML assertion

        // clone the whole document. The root element is the Response
        Document clonedResponse = (Document) signedDoc.cloneNode(true);

        // let's remove the Signature from the cloned response
        Element signature = (Element) clonedResponse.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature")
                .item(0);

        signature.getParentNode().removeChild(signature);

        // let's remove the original assertion. Later it will be replaced by a another one.
        Element originalAssertion = (Element) signedDoc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion").item(0);

        originalAssertion.getParentNode().removeChild(originalAssertion);

        // let's load a forged assertion
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        String fileName = "saml2-wrapping-attack.xml";
        InputStream is = tcl.getResourceAsStream(fileName);
        Document evilAssertion = DocumentUtil.getDocument(is);

        // let's wrap the forged assertion into the original document.
        Element element = evilAssertion.getDocumentElement();

        Node adoptNode = signedDoc.adoptNode(element);

        signedDoc.getDocumentElement().appendChild(adoptNode);

        // let's append the cloned response document as a child of the original Signature element
        Element signatureOriginal = (Element) signedDoc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#",
                "Signature").item(0);

        Element importedClonedResponse = (Element) signatureOriginal.getOwnerDocument().adoptNode(
                clonedResponse.getDocumentElement());

        signatureOriginal.appendChild(importedClonedResponse);

        // let's change the original response ID attribute value
        signedDoc.getDocumentElement().setAttribute("ID", "evilAssertion");

        // let's set the idness of the ID attribute. This should force the signature validation to be successful. The two lines
        // bellow are responsible to allow the attack.
        importedClonedResponse = (Element) signatureOriginal.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:protocol",
                "Response").item(0);
        importedClonedResponse.setIdAttribute("ID", true);

        // validate the original response with the wrapped assertion
        isValid = XMLSignatureUtil.validate(signedDoc, publicKey);

        assertTrue(isValid);
    }

    private ResponseType createSignedResponse() throws ConfigurationException {
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("testIssuer");
        String id = IDGenerator.create("ID_");

        SAML2Response response = new SAML2Response();

        String authnContextDeclRef = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();

        AuthnStatementType authnStatement = response.createAuthnStatement(authnContextDeclRef, XMLTimeUtil.getIssueInstant());

        // Create an assertion
        AssertionType assertion = response.createAssertion(id, issuerInfo.getIssuer());

        SubjectType subject = new SubjectType();

        subject.setSubType(new STSubType());
        NameIDType nameId = new NameIDType();
        nameId.setValue("jduke");
        subject.getSubType().addBaseID(nameId);

        assertion.setSubject(subject);
        assertion.addStatement(authnStatement);

        AttributeStatementType attributes = new AttributeStatementType();

        AttributeType attribute = new AttributeType("Role");

        attribute.addAttributeValue("Manager");

        attributes.addAttribute(new ASTChoiceType(attribute));

        assertion.addStatement(attributes);

        id = IDGenerator.create("ID_"); // regenerate

        return response.createResponseType(id, issuerInfo, assertion);
    }

}