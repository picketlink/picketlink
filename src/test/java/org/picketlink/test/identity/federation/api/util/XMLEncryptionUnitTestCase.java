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
package org.picketlink.test.identity.federation.api.util;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedAssertionType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit Test the XML Encryption Utility
 * @author Anil.Saldhana@redhat.com
 * @since Feb 5, 2009
 */
public class XMLEncryptionUnitTestCase extends TestCase
{
   SAML2Response sr = new SAML2Response();

   public void testEncryptAssertion() throws Exception
   {
      PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
      sts.installDefaultConfiguration();

      KeyPair kp = this.getKeyPair("RSA");
      SecretKey sk = this.getSecretKey();

      ResponseType rt = createResponse();
      Document responseDoc = sr.convert(rt);

      String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();

      QName assertionQName = new QName(assertionNS, "EncryptedAssertion", "saml");

      Element docElement = XMLEncryptionUtil.encryptElementInDocument(responseDoc, kp.getPublic(), sk, 128,
            assertionQName, true);

      InputStream is = DocumentUtil.getNodeAsStream(docElement);
      EncryptedAssertionType eet = sr.getEncryptedAssertion(is);
      rt.addAssertion(new RTChoiceType(eet));

      RTChoiceType choiceType = rt.getAssertions().get(1);
      EncryptedAssertionType encryptedAssertionType = choiceType.getEncryptedAssertion();

      Document eetDoc = sr.convert(encryptedAssertionType);

      Element decryptedDocumentElement = XMLEncryptionUtil.decryptElementInDocument(eetDoc, kp.getPrivate());

      //Let us use the encrypted doc element to decrypt it

      ResponseType newRT = sr.getResponseType(DocumentUtil.getNodeAsStream(decryptedDocumentElement));

      AssertionType assertion = newRT.getAssertions().get(0).getAssertion();
      assertEquals("http://identityurl", assertion.getIssuer().getValue());
   }

   public void testArbitraryXML() throws Exception
   {
      String myXML = "<somexml><a><b></b></a></somexml>";

      KeyPair kp = this.getKeyPair("RSA");
      SecretKey sk = this.getSecretKey();

      PublicKey publicKey = kp.getPublic();

      Document doc = DocumentUtil.getDocument(myXML);
      QName qname = new QName("urn:test", "encryptedA", "someprefix");

      QName elementAQname = new QName("a");

      XMLEncryptionUtil.encryptElement(elementAQname, doc, publicKey, sk, 256, qname, true);

      //Let us verify the document: The original document that has been passed has been updated
      NodeList nl = doc.getElementsByTagNameNS(XMLEncryptionUtil.XMLENC_NS, XMLEncryptionUtil.ENCRYPTED_KEY_LOCALNAME);
      assertTrue(nl != null && nl.getLength() == 1);

      Node wrappedNode = doc.getDocumentElement().getFirstChild();
      assertEquals(wrappedNode.getLocalName(), qname.getLocalPart());
      assertEquals(wrappedNode.getNamespaceURI(), qname.getNamespaceURI());
      assertEquals(wrappedNode.getPrefix(), qname.getPrefix());

      //Let us decrypt the document
      PrivateKey privateKey = kp.getPrivate();
      Element decryptedElement = XMLEncryptionUtil.decryptElementInDocument(doc, privateKey);
      assertEquals("a", decryptedElement.getLocalName());
   }

   public void testArbitraryXMLWithOuterKeyInfo() throws Exception
   {
      String myXML = "<somexml><a><b></b></a></somexml>";

      KeyPair kp = this.getKeyPair("RSA");
      SecretKey sk = this.getSecretKey();

      PublicKey publicKey = kp.getPublic();

      Document doc = DocumentUtil.getDocument(myXML);
      QName qname = new QName("urn:test", "encryptedA", "someprefix");

      QName elementAQname = new QName("a");

      XMLEncryptionUtil.encryptElement(elementAQname, doc, publicKey, sk, 256, qname, false);

      //Let us verify the document: The original document that has been passed has been updated
      NodeList nl = doc.getElementsByTagNameNS(XMLEncryptionUtil.XMLENC_NS, XMLEncryptionUtil.ENCRYPTED_KEY_LOCALNAME);
      assertTrue(nl != null && nl.getLength() == 1);
   }

   private ResponseType createResponse() throws Exception
   {
      List<String> roles = new ArrayList<String>();
      roles.add("roleA");
      roles.add("roleB");

      ResponseType responseType = null;

      SAML2Response saml2Response = new SAML2Response();

      //Create a response type
      String id = IDGenerator.create("ID_");

      IssuerInfoHolder issuerHolder = new IssuerInfoHolder("http://identityurl");
      issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());

      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue("testPrincipal");
      idp.setNameIDFormat(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

      SPInfoHolder sp = new SPInfoHolder();
      sp.setResponseDestinationURI("http://service");
      responseType = saml2Response.createResponseType(id, sp, idp, issuerHolder);
      AssertionType assertion = responseType.getAssertions().get(0).getAssertion();

      AttributeStatementType attrStatement = StatementUtil.createAttributeStatement(roles);
      assertion.addStatement(attrStatement);

      //Add timed conditions
      saml2Response.createTimedConditions(assertion, 5000L);

      return responseType;
   }

   private KeyPair getKeyPair(String algo) throws Exception
   {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(algo);
      return kpg.genKeyPair();
   }

   private SecretKey getSecretKey() throws Exception
   {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      return keyGenerator.generateKey();
   }
}
