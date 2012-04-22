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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.test.identity.federation.api.util.KeyUtilUnitTestCase;
import org.w3c.dom.Document;

/**
 * Unit test the {@link SAML2Response} API
 * @author Anil.Saldhana@redhat.com
 * @since Jul 21, 2011
 */
public class SAML2ResponseUnitTestCase
{
   private final String keystoreLocation = "keystore/jbid_test_keystore.jks";

   private final String keystorePass = "store123";

   private final String keyPass = "test123";

   private final String alias = "servercert";

   /**
    * Parse a {@link ResponseType} that contains ADFS Claims
    * and then try to sign
    * @throws Exception
    */
   @Test
   public void parseADFSClaims() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("saml/v2/response/saml2-response-adfs-claims.xml");
      SAML2Response samlResponse = new SAML2Response();
      SAML2Object samlObject = samlResponse.getSAML2ObjectFromStream(configStream);
      assertNotNull(samlObject);

      SAML2Signature sig = new SAML2Signature();
      Document signedDoc = sig.sign((ResponseType) samlObject, getKeyPair());
      assertNotNull(signedDoc);

      System.out.println("Signed Response=" + DocumentUtil.asString(signedDoc));
   }

   /**
    * This test constructs the {@link ResponseType}. An {@link AssertionType}
    * is locally constructed and then passed to the construct method
    * @throws Exception
    */
   @Test
   public void constructAndSign() throws Exception
   {
      SAML2Response samlResponse = new SAML2Response();
      String ID = IDGenerator.create("ID_");

      IssuerInfoHolder issuerInfo = new IssuerInfoHolder("picketlink");

      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue("anil");

      //create the service provider(in this case BAS) holder object
      SPInfoHolder sp = new SPInfoHolder();
      sp.setResponseDestinationURI("http://sombody");

      Map<String, Object> attributes = new HashMap<String, Object>();

      attributes.put("TOKEN_USER_ID", String.valueOf(2));
      attributes.put("TOKEN_ORGANIZATION_DISPLAY_NAME", "Test Org");
      attributes.put("TOKEN_USER_DISPLAY_NAME", "Test User");

      AttributeStatementType attributeStatement = StatementUtil.createAttributeStatement(attributes);

      String assertionId = IDGenerator.create("ID_");

      AssertionType assertion = AssertionUtil.createAssertion(assertionId, issuerInfo.getIssuer());
      assertion.addStatement(attributeStatement);

      ResponseType responseType = samlResponse.createResponseType(ID, sp, idp, issuerInfo, assertion);
      SAML2Signature sig = new SAML2Signature();
      Document signedDoc = sig.sign(responseType, getKeyPair());
      assertNotNull(signedDoc);

      System.out.println("Signed Response=" + DocumentUtil.asString(signedDoc));

      Document convertedDoc = samlResponse.convert(responseType);
      assertNotNull(convertedDoc);

      //Now for the writing part
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      SAMLResponseWriter samlWriter = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      samlWriter.write(responseType);

      Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }

   /**
    * @see {@link KeyUtilUnitTestCase}
    * @return
    * @throws Exception
    */
   private KeyPair getKeyPair() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream ksStream = tcl.getResourceAsStream(keystoreLocation);
      assertNotNull("Input keystore stream is not null", ksStream);

      KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
      ks.load(ksStream, keystorePass.toCharArray());
      assertNotNull("KeyStore is not null", ks);

      Certificate cert = ks.getCertificate(alias);
      assertNotNull("Cert not null", cert);

      // Get private key 
      Key key = ks.getKey(alias, keyPass.toCharArray());
      return new KeyPair(cert.getPublicKey(), (PrivateKey) key);
   }
}