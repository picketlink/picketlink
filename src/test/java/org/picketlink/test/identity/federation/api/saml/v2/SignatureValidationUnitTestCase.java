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
package org.picketlink.test.identity.federation.api.saml.v2;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.xml.crypto.dsig.SignatureMethod;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SignatureUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Signatures related unit test cases
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Dec 15, 2008
 */
public class SignatureValidationUnitTestCase
{
   /**
    * Test the creation of AuthnRequestType with signature creation with a private key and then validate the signature
    * with a public key
    * 
    * @throws Exception
    */
   @Test
   public void testAuthnRequestCreationWithSignature() throws Exception
   {
      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      String assertionConsumerURL = "http://sp";
      String destination = "http://idp";
      String issuerValue = "http://sp";
      AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination,
            issuerValue);

      KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
      KeyPair kp = kpg.genKeyPair();

      SAML2Signature ss = new SAML2Signature();
      ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
      Document signedDoc = ss.sign(authnRequest, kp);

      System.out.println("Signed Doc:" + DocumentUtil.asString(signedDoc));

      // Validate the signature
      boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
      assertTrue(isValid);
   }

   /**
    * Test the creation of AuthnRequestType with signature creation with a private key and then validate the signature
    * with a public key. We test that the signature does not contain the keyinfo
    * 
    * @throws Exception
    */
   @Test
   public void testNoKeyInfo() throws Exception
   {
      SAML2Request saml2Request = new SAML2Request();
      String id = IDGenerator.create("ID_");
      String assertionConsumerURL = "http://sp";
      String destination = "http://idp";
      String issuerValue = "http://sp";
      AuthnRequestType authnRequest = saml2Request.createAuthnRequestType(id, assertionConsumerURL, destination,
            issuerValue);

      KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
      KeyPair kp = kpg.genKeyPair();

      SAML2Signature ss = new SAML2Signature();
      ss.setSignatureIncludeKeyInfo(false);

      ss.setSignatureMethod(SignatureMethod.DSA_SHA1);
      Document signedDoc = ss.sign(authnRequest, kp);

      System.out.println("Signed Doc:" + DocumentUtil.asString(signedDoc));

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
   public void testSigningResponse() throws Exception
   {
      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("testIssuer");
      String id = IDGenerator.create("ID_");

      SAML2Response response = new SAML2Response();

      String authnContextDeclRef = JBossSAMLURIConstants.AC_PASSWORD_PROTECTED_TRANSPORT.get();

      AuthnStatementType authnStatement = response.createAuthnStatement(authnContextDeclRef,
            XMLTimeUtil.getIssueInstant());

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

      // Validate the signature
      boolean isValid = XMLSignatureUtil.validate(signedDoc, kp.getPublic());
      assertTrue(isValid);
   }

   @Test
   public void testSigningAnAssertionWithinResponse() throws Exception
   {
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
      Node assert2 = DocumentUtil.getNodeWithAttribute(doc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "ID",
            id);

      String referenceURI = "#" + id;

      assertNotNull("Found assertion?", assert2);
      SAML2Signature ss = new SAML2Signature();
      Document signedDoc = ss.sign(responseType, id, kp, referenceURI);

      Node signedNode = DocumentUtil.getNodeWithAttribute(signedDoc, "urn:oasis:names:tc:SAML:2.0:assertion",
            "Assertion", "ID", id);

      // Let us just validate the signature of the assertion
      Document validatingDoc = DocumentUtil.createDocument();
      Node importedSignedNode = validatingDoc.importNode(signedNode, true);
      validatingDoc.appendChild(importedSignedNode);

      // Validate the signature 
      boolean isValid = XMLSignatureUtil.validate(validatingDoc, kp.getPublic());
      assertTrue("Signature is valid:", isValid);

      /**
       * Now the signed document is marshalled across the wire using dom
       * write
       */
      //Binder<Node> binder = response.getBinder();
      //We have to parse the dom coming from the stream and feed to binder
      Document readDoc = DocumentUtil.getDocument(DocumentUtil.getNodeAsStream(signedDoc));

      signedNode = DocumentUtil.getNodeWithAttribute(readDoc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion",
            "ID", id);

      // The client creates a validating document, importing the signed assertion.
      validatingDoc = DocumentUtil.createDocument();
      importedSignedNode = validatingDoc.importNode(signedNode, true);
      validatingDoc.appendChild(importedSignedNode);

      // The client re-validates the signature.  
      assertTrue("Signature is valid:", XMLSignatureUtil.validate(validatingDoc, kp.getPublic()));

      /*JAXBElement<ResponseType> jaxbresponseType = (JAXBElement<ResponseType>) binder.unmarshal(readDoc);
      responseType = jaxbresponseType.getValue();
      assertNotNull(responseType); */
   }

   /**
    * Test signing a string
    * 
    * @throws Exception
    */
   @Test
   public void testStringContentSignature() throws Exception
   {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
      KeyPair kp = kpg.genKeyPair();

      String arbitContent = "I am A String";

      byte[] sigVal = SignatureUtil.sign(arbitContent, kp.getPrivate());

      boolean valid = SignatureUtil.validate(arbitContent.getBytes(), sigVal, kp.getPublic());
      assertTrue(valid);
   }
}