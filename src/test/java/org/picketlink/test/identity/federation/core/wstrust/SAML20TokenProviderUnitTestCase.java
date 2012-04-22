/*
 * JBoss, Home of Professional Open Source. Copyright 2009, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.test.identity.federation.core.wstrust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLAssertionParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.wstrust.SecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code TestCase} tests the functionalities of the {@code SAML20TokenProvider} class.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SAML20TokenProviderUnitTestCase
{

   private SAML20TokenProvider provider;

   /**
    * <p>
    * Tests the issuance of a SAMLV2.0 Assertion.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testIssueSAMLV20Token() throws Exception
   {
      this.provider = new SAML20TokenProvider();
      provider.initialize(new HashMap<String, String>());

      SAMLAssertionParser assertionParser = new SAMLAssertionParser();

      // create a WSTrustRequestContext with a simple WS-Trust request.
      RequestSecurityToken request = new RequestSecurityToken();
      request.setLifetime(WSTrustUtil.createDefaultLifetime(3600000));
      request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider2"));
      request.setTokenType(URI.create(SAMLUtil.SAML2_TOKEN_TYPE));

      WSTrustRequestContext context = new WSTrustRequestContext(request, new TestPrincipal("sguilhen"));
      context.setTokenIssuer("PicketLinkSTS");

      // call the SAML token provider and check the generated token.
      this.provider.issueToken(context);
      assertNotNull("Unexpected null security token", context.getSecurityToken());

      SecurityToken securityToken = context.getSecurityToken();

      AssertionType assertion = assertionParser.fromElement((Element) securityToken.getTokenValue());
      /*
       * JAXBContext jaxbContext = JAXBContext.newInstance("org.picketlink.identity.federation.saml.v2.assertion");
       * Unmarshaller unmarshaller = jaxbContext.createUnmarshaller(); JAXBElement<?> parsedElement = (JAXBElement<?>)
       * unmarshaller.unmarshal((Element) context.getSecurityToken() .getTokenValue());
       * assertNotNull("Unexpected null element", parsedElement); assertEquals("Unexpected element type",
       * AssertionType.class, parsedElement.getDeclaredType());
       * 
       * AssertionType assertion = (AssertionType) parsedElement.getValue(); StandardSecurityToken securityToken =
       * (StandardSecurityToken) context.getSecurityToken();
       */
      assertEquals("Unexpected token id", securityToken.getTokenID(), assertion.getID());
      assertEquals("Unexpected token issuer", "PicketLinkSTS", assertion.getIssuer().getValue());

      // check the contents of the assertion conditions.
      ConditionsType conditions = assertion.getConditions();
      assertNotNull("Unexpected null conditions", conditions);
      assertNotNull("Unexpected null value for NotBefore attribute", conditions.getNotBefore());
      assertNotNull("Unexpected null value for NotOnOrAfter attribute", conditions.getNotOnOrAfter());
      assertEquals("Unexpected number of conditions", 1, conditions.getConditions().size());

      AudienceRestrictionType restrictionType = (AudienceRestrictionType) conditions.getConditions().get(0);
      assertNotNull("Unexpected null audience list", restrictionType.getAudience());
      assertEquals("Unexpected number of audience elements", 1, restrictionType.getAudience().size());
      assertEquals("Unexpected audience value", "http://services.testcorp.org/provider2", restrictionType.getAudience()
            .get(0).toString());

      // check the contents of the assertion subject.
      SubjectType subject = assertion.getSubject();
      assertNotNull("Unexpected null subject", subject);

      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID.getNameQualifier());
      assertEquals("Unexpected name id", "sguilhen", nameID.getValue());

      SubjectConfirmationType confirmation = subject.getConfirmation().get(0);
      assertEquals("Unexpected confirmation method", SAMLUtil.SAML2_BEARER_URI, confirmation.getMethod());

      // validate the attached token reference created by the SAML provider.
      RequestedReferenceType reference = context.getAttachedReference();
      assertNotNull("Unexpected null attached reference", reference);
      SecurityTokenReferenceType securityRef = reference.getSecurityTokenReference();
      assertNotNull("Unexpected null security reference", securityRef);
      String tokenTypeAttr = securityRef.getOtherAttributes().get(new QName(WSTrustConstants.WSSE11_NS, "TokenType"));
      assertNotNull("Required attribute TokenType is missing", tokenTypeAttr);
      assertEquals("TokenType attribute has an unexpected value", SAMLUtil.SAML2_TOKEN_TYPE, tokenTypeAttr);
      KeyIdentifierType keyId = (KeyIdentifierType) securityRef.getAny().get(0);
      assertEquals("Unexpected key value type", SAMLUtil.SAML2_VALUE_TYPE, keyId.getValueType());
      assertNotNull("Unexpected null key identifier value", keyId.getValue());
      assertEquals(assertion.getID(), keyId.getValue().substring(1));
   }

   /**
    * <p>
    * This method tests the creation of SAMLV.20 assertions that contain a proof-of-possession token - that is,
    * assertions that use the Holder Of Key confirmation method.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testIssueSAMLV20HolderOfKeyToken() throws Exception
   {

      this.provider = new SAML20TokenProvider();
      provider.initialize(new HashMap<String, String>());
      // create a WSTrustRequestContext with a simple WS-Trust request.
      RequestSecurityToken request = new RequestSecurityToken();
      request.setLifetime(WSTrustUtil.createDefaultLifetime(3600000));
      request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider2"));
      request.setTokenType(URI.create(SAMLUtil.SAML2_TOKEN_TYPE));

      WSTrustRequestContext context = new WSTrustRequestContext(request, new TestPrincipal("sguilhen"));
      context.setTokenIssuer("PicketLinkSTS");

      // let's set a symmetric key proof-of-possession token in the context.
      byte[] secret = WSTrustUtil.createRandomSecret(32);
      PublicKey serviceKey = this.getCertificate("keystore/sts_keystore.jks", "testpass", "service2").getPublicKey();
      context.setProofTokenInfo(WSTrustUtil.createKeyInfo(secret, serviceKey, null));

      // call the SAML token provider and check the generated token.
      this.provider.issueToken(context);
      assertNotNull("Unexpected null security token", context.getSecurityToken());

      // check if the assertion has a subject confirmation that contains the encrypted symmetric key.
      AssertionType assertion = SAMLUtil.fromElement((Element) context.getSecurityToken().getTokenValue());
      SubjectType subject = assertion.getSubject();
      assertNotNull("Unexpected null subject", subject);

      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID.getNameQualifier());
      assertEquals("Unexpected name id", "sguilhen", nameID.getValue());

      SubjectConfirmationType confirmation = subject.getConfirmation().get(0);
      assertEquals("Unexpected confirmation method", SAMLUtil.SAML2_HOLDER_OF_KEY_URI, confirmation.getMethod());

      SubjectConfirmationDataType confirmData = confirmation.getSubjectConfirmationData();
      KeyInfoType keyInfo = (KeyInfoType) confirmData.getAnyType();
      assertEquals("Unexpected key info content size", 1, keyInfo.getContent().size());
      Element encKeyElement = (Element) keyInfo.getContent().get(0);
      assertEquals("Unexpected key info content type", WSTrustConstants.XMLEnc.ENCRYPTED_KEY,
            encKeyElement.getLocalName());

      // Now let's set an asymmetric proof of possession token in the context.
      Certificate certificate = this.getCertificate("keystore/sts_keystore.jks", "testpass", "service1");
      context.setProofTokenInfo(WSTrustUtil.createKeyInfo(certificate));

      // call the SAML token provider and check the generated token.
      this.provider.issueToken(context);
      assertNotNull("Unexpected null security token", context.getSecurityToken());

      // check if the assertion has a subject confirmation that contains the encoded certificate.
      assertion = SAMLUtil.fromElement((Element) context.getSecurityToken().getTokenValue());
      subject = assertion.getSubject();
      nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID.getNameQualifier());
      assertEquals("Unexpected name id", "sguilhen", nameID.getValue());
      confirmation = subject.getConfirmation().get(0);
      assertEquals("Unexpected confirmation method", SAMLUtil.SAML2_HOLDER_OF_KEY_URI, confirmation.getMethod());

      /*
       * confirmationContent = confirmation.getSubjectConfirmationData().getContent();
       * assertEquals("Unexpected subject confirmation content size", 1, confirmationContent.size()); keyInfoElement =
       * (JAXBElement<?>) confirmationContent.get(0); assertEquals("Unexpected subject confirmation context type",
       * KeyInfoType.class, keyInfoElement.getDeclaredType());
       */
      keyInfo = (KeyInfoType) confirmation.getSubjectConfirmationData().getAnyType();
      assertEquals("Unexpected key info content size", 1, keyInfo.getContent().size());

      // key info should contain a X509Data section with the encoded certificate.
      X509DataType x509Data = (X509DataType) keyInfo.getContent().get(0);
      assertEquals("Unexpected X509 data content size", 1, x509Data.getDataObjects().size());
      X509CertificateType cert = (X509CertificateType) x509Data.getDataObjects().get(0);

      // certificate should have been encoded to Base64, so we need to decode it first.
      byte[] encodedCert = Base64.decode(new String(cert.getEncodedCertificate()));
      assertTrue("Invalid encoded certificate found", Arrays.equals(certificate.getEncoded(), encodedCert));
   }

   /**
    * <p>
    * Tests the validation of a SAMLV2.0 Assertion.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testValidateSAMLV20Token() throws Exception
   {
      this.provider = new SAML20TokenProvider();
      provider.initialize(new HashMap<String, String>());

      // issue a SAMLV2.0 assertion.
      WSTrustRequestContext context = this.createIssuingContext(WSTrustUtil.createDefaultLifetime(3600000));
      this.provider.issueToken(context);

      // get the issued SAMLV2.0 assertion.
      Element assertion = (Element) context.getSecurityToken().getTokenValue();

      // now create a WS-Trust validate context.
      context = this.createValidatingContext(assertion);

      // validate the SAMLV2.0 assertion.
      this.provider.validateToken(context);
      StatusType status = context.getStatus();
      assertNotNull("Unexpected null status type", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_VALID, status.getCode());
      assertEquals("Unexpected status reason", "SAMLV2.0 Assertion successfuly validated", status.getReason());

      // now let's create a new SAMLV2.0 assertion with an expired lifetime.
      long currentTimeMillis = System.currentTimeMillis();
      GregorianCalendar created = new GregorianCalendar();
      created.setTimeInMillis(currentTimeMillis - 3600000);
      GregorianCalendar expires = new GregorianCalendar();
      expires.setTimeInMillis(currentTimeMillis - 1800000);
      context = this.createIssuingContext(new Lifetime(created, expires));

      provider.issueToken(context);
      assertion = (Element) context.getSecurityToken().getTokenValue();

      // try to validate the expired token.
      context = this.createValidatingContext(assertion);
      provider.validateToken(context);
      status = context.getStatus();
      assertNotNull("Unexpected null status type", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_INVALID, status.getCode());
      assertEquals("Unexpected status reason",
            "Validation failure: assertion expired or used before its lifetime period", status.getReason());
   }

   /**
    * <p>
    * Creates a {@code WSTrustRequestContext} using the specified lifetime. The created context is used in the issuing
    * test scenarios.
    * </p>
    * 
    * @param lifetime
    *           the {@code Lifetime} of the assertion to be issued.
    * @return the constructed {@code WSTrustRequestHandler} instance.
    * @throws Exception
    *            if an error occurs while creating the context.
    */
   private WSTrustRequestContext createIssuingContext(Lifetime lifetime) throws Exception
   {
      // create a WSTrustRequestContext with a simple WS-Trust issue request.
      RequestSecurityToken request = new RequestSecurityToken();
      request.setLifetime(lifetime);
      request.setAppliesTo(WSTrustUtil.createAppliesTo("http://services.testcorp.org/provider2"));
      request.setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
      request.setTokenType(URI.create(SAMLUtil.SAML2_TOKEN_TYPE));

      WSTrustRequestContext context = new WSTrustRequestContext(request, new TestPrincipal("sguilhen"));
      context.setTokenIssuer("PicketLinkSTS");

      return context;
   }

   /**
    * <p>
    * Creates a {@code WSTrustRequestContext} for validating the specified assertion.
    * </p>
    * 
    * @param assertion
    *           an {@code Element} representing the SAMLV2.0 assertion to be validated.
    * @return the constructed {@code WSTrustRequestContext} instance.
    * @throws Exception
    *            if an error occurs while creating the validating context.
    */
   private WSTrustRequestContext createValidatingContext(Element assertion) throws Exception
   {
      RequestSecurityToken request = new RequestSecurityToken();
      request.setRequestType(URI.create(WSTrustConstants.VALIDATE_REQUEST));
      request.setTokenType(URI.create(WSTrustConstants.STATUS_TYPE));
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(assertion);
      request.setValidateTarget(validateTarget);
      // we need to set the request document in the request object for the test.
      DOMSource requestSource = (DOMSource) this.createSourceFromRequest(request);
      request.setRSTDocument((Document) requestSource.getNode());

      WSTrustRequestContext context = new WSTrustRequestContext(request, new TestPrincipal("sguilhen"));
      return context;
   }

   /**
    * <p>
    * Obtains the {@code Certificate} stored under the specified alias in the specified keystore.
    * </p>
    * 
    * @param keyStoreFile
    *           the name of the file that contains a JKS keystore.
    * @param passwd
    *           the keystore password.
    * @param certificateAlias
    *           the alias of a certificate in the keystore.
    * @return a reference to the {@code Certificate} stored under the given alias.
    * @throws Exception
    *            if an error occurs while handling the keystore.
    */
   private Certificate getCertificate(String keyStoreFile, String passwd, String certificateAlias) throws Exception
   {
      InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFile);
      KeyStore keyStore = KeyStore.getInstance("JKS");
      keyStore.load(stream, passwd.toCharArray());

      Certificate certificate = keyStore.getCertificate(certificateAlias);
      return certificate;
   }

   private Source createSourceFromRequest(RequestSecurityToken request) throws Exception
   {
      DOMResult result = new DOMResult(DocumentUtil.createDocument());
      WSTrustRequestWriter writer = new WSTrustRequestWriter(result);
      writer.write(request);
      return new DOMSource(result.getNode());
   }
}
