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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.parsers.sts.STSConfigParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTS;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.identity.federation.core.wstrust.StandardRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML11TokenProvider;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.BaseRequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AuthenticationStatementType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11NameIdentifierType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11StatementAbstractType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationDataType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.ComputedKeyType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.RequestedProofTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.RequestedSecurityTokenType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509CertificateType;
import org.picketlink.identity.xmlsec.w3.xmldsig.X509DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This {@code TestCase} tests the behavior of the {@code PicketLinkSTS} service.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class PicketLinkSTSUnitTestCase
{

   private TestSTS tokenService;

   /*
    * (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   @Before
   public void setUp() throws Exception
   {
      // for testing purposes we can instantiate the TestSTS as a regular POJO.
      this.tokenService = new TestSTS();
      TestContext context = new TestContext();
      context.setUserPrincipal(new TestPrincipal("jduke"));
      this.tokenService.setContext(context);
   }

   /**
    * <p>
    * This test verifies that the STS service can read and load all configuration parameters correctly. The
    * configuration file (picketlink-sts.xml) looks like the following:
    * 
    * <pre>
    *    &lt;PicketLinkSTS xmlns=&quot;urn:picketlink:identity-federation:config:1.0&quot;
    *     STSName=&quot;Test STS&quot; TokenTimeout=&quot;7200&quot; EncryptToken=&quot;true&quot;&gt;
    *     &lt;KeyProvider ClassName=&quot;org.jboss.identity.federation.bindings.tomcat.KeyStoreKeyManager&quot;&gt;
    *         &lt;Auth Key=&quot;KeyStoreURL&quot; Value=&quot;keystore/sts_keystore.jks&quot;/&gt; 
    *         &lt;Auth Key=&quot;KeyStorePass&quot; Value=&quot;testpass&quot;/&gt;
    *         &lt;Auth Key=&quot;SigningKeyAlias&quot; Value=&quot;sts&quot;/&gt;
    *         &lt;Auth Key=&quot;SigningKeyPass&quot; Value=&quot;keypass&quot;/&gt;
    *         &lt;ValidatingAlias Key=&quot;http://services.testcorp.org/provider1&quot; Value=&quot;service1&quot;/&gt;
    *         &lt;ValidatingAlias Key=&quot;http://services.testcorp.org/provider2&quot; Value=&quot;service2&quot;/&gt;
    *     &lt;/KeyProvider&gt;
    *     &lt;RequestHandler&gt;org.jboss.identity.federation.core.wstrust.StandardRequestHandler&lt;/RequestHandler&gt;
    *     &lt;TokenProviders&gt;
    *         &lt;TokenProvider ProviderClass=&quot;org.jboss.test.identity.federation.bindings.trust.SpecialTokenProvider&quot;
    *             TokenType=&quot;http://www.tokens.org/SpecialToken&quot;
    *             TokenElement=&quot;SpecialToken&quot;
    *             TokenElementNS=&quot;http://www.tokens.org&quot;&gt;
    *             &lt;Property Key=&quot;Property1&quot; Value=&quot;Value1&quot;/&gt;
    *             &lt;Property Key=&quot;Property2&quot; Value=&quot;Value2&quot;/&gt;
    *         &lt;/TokenProvider&gt;
    *         &lt;TokenProvider ProviderClass=&quot;org.jboss.identity.federation.core.wstrust.SAML11TokenProvider&quot;
    *             TokenType=&quot;http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1&quot;
    *             TokenElement=&quot;Assertion&quot;
    *             TokenElementNS=&quot;urn:oasis:names:tc:SAML:1.0:assertion&quot;/&gt;
    *         &lt;TokenProvider ProviderClass=&quot;org.jboss.identity.federation.core.wstrust.SAML20TokenProvider&quot;
    *             TokenType=&quot;http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0&quot;
    *             TokenElement=&quot;Assertion&quot;
    *             TokenElementNS=&quot;urn:oasis:names:tc:SAML:2.0:assertion&quot;/&gt;/&gt;
    *     &lt;/TokenProviders&gt;
    *     &lt;ServiceProviders&gt;
    *         &lt;ServiceProvider Endpoint=&quot;http://services.testcorp.org/provider1&quot; TokenType=&quot;http://www.tokens.org/SpecialToken&quot;
    *             TruststoreAlias=&quot;service1&quot;/&gt;
    *         &lt;ServiceProvider Endpoint=&quot;http://services.testcorp.org/provider2&quot; TokenType=&quot;http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0&quot;
    *             TruststoreAlias=&quot;service2&quot;/&gt;
    *     &lt;/ServiceProviders&gt;
    *    &lt;/PicketLinkSTS&gt;    *
    * </pre>
    * 
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testSTSConfiguration() throws Exception
   {
      // make the STS read the configuration file.
      STSConfiguration config = this.tokenService.getConfiguration();

      // check the values that have been configured.
      assertEquals("Unexpected service name", "Test STS", config.getSTSName());
      assertEquals("Unexpected token timeout value", 7200 * 1000, config.getIssuedTokenTimeout());
      assertFalse("Encrypt token should be true", config.encryptIssuedToken());
      WSTrustRequestHandler handler = config.getRequestHandler();
      assertNotNull("Unexpected null request handler found", handler);
      assertTrue("Unexpected request handler type", handler instanceof StandardRequestHandler);

      // check the token type -> token provider mapping.
      SecurityTokenProvider provider = config.getProviderForTokenType("http://www.tokens.org/SpecialToken");
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SpecialTokenProvider);
      Map<String, String> properties = ((SpecialTokenProvider) provider).getProperties();
      assertNotNull("Unexpected null properties map", properties);
      assertEquals("Unexpected number of properties", 2, properties.size());
      assertEquals("Invalid property found", "Value1", properties.get("Property1"));
      assertEquals("Invalid property found", "Value2", properties.get("Property2"));
      provider = config.getProviderForTokenType(SAMLUtil.SAML2_TOKEN_TYPE);
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML20TokenProvider);
      provider = config.getProviderForTokenType(SAMLUtil.SAML11_TOKEN_TYPE);
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML11TokenProvider);
      assertNull(config.getProviderForTokenType("unexistentType"));

      // check the service provider -> token provider mapping.
      provider = config.getProviderForService("http://services.testcorp.org/provider1");
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SpecialTokenProvider);
      provider = config.getProviderForService("http://services.testcorp.org/provider2");
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML20TokenProvider);
      assertNull(config.getProviderForService("http://invalid.service/service"));

      String family = SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();

      // check the token element and namespace -> token provider mapping.
      provider = config.getProviderForTokenElementNS(family, new QName("http://www.tokens.org", "SpecialToken"));
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SpecialTokenProvider);
      provider = config.getProviderForTokenElementNS(family, new QName(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
            JBossSAMLConstants.ASSERTION.get()));
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML20TokenProvider);
      provider = config.getProviderForTokenElementNS(family, new QName(SAML11Constants.ASSERTION_11_NSURI,
            JBossSAMLConstants.ASSERTION.get()));
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML11TokenProvider);
      assertNull(config.getProviderForTokenElementNS(family, new QName("InvalidNamespace", "SpecialToken")));

      // check the service provider -> token type mapping.
      assertEquals("Invalid token type for service provider 1", "http://www.tokens.org/SpecialToken",
            config.getTokenTypeForService("http://services.testcorp.org/provider1"));
      assertEquals("Invalid token type for service provider 2", SAMLUtil.SAML2_TOKEN_TYPE,
            config.getTokenTypeForService("http://services.testcorp.org/provider2"));
      assertNull(config.getTokenTypeForService("http://invalid.service/service"));

      // check the keystore configuration.
      assertNotNull("Invalid null STS key pair", config.getSTSKeyPair());
      assertNotNull("Invalid null STS public key", config.getSTSKeyPair().getPublic());
      assertNotNull("Invalid null STS private key", config.getSTSKeyPair().getPrivate());
      assertNotNull("Invalid null validating key for service provider 1",
            config.getServiceProviderPublicKey("http://services.testcorp.org/provider1"));
      assertNotNull("Invalid null validating key for service provider 2",
            config.getServiceProviderPublicKey("http://services.testcorp.org/provider2"));
   }

   /**
    * <p>
    * This tests sends a security token request to PicketLinkSTS custom {@code SpecialTokenProvider}. The returned
    * response is verified to make sure the expected tokens have been returned by the service. The token that is
    * generated in this test looks as follows:
    * 
    * <pre>
    *    &lt;token:SpecialToken xmlns:token=&quot;http://www.tokens.org&quot; TokenType=&quot;http://www.tokens.org/SpecialToken&quot;&gt;
    *       Principal:sguilhen
    *    &lt;/token:SpecialToken&gt;
    * </pre>
    * 
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeCustom() throws Exception
   {
      // create a simple token request, asking for a "special" test token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            "http://www.tokens.org/SpecialToken", null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));
      // validate the security token response.
      this.validateCustomTokenResponse(baseResponse);
   }

   /**
    * <p>
    * This tests sends a SAMLV2.0 security token request to PicketLinkSTS. This request should be handled by the {@code
    * SAML11TokenProvider} and should result in a SAMLV1.1 assertion.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML11() throws Exception
   {
      // create a simple token request, asking for a SAMLv1.1 token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML11_TOKEN_TYPE, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      InputStream is = DocumentUtil.getSourceAsStream(responseMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser().parse(is);
      // validate the security token response.
      this.validateSAML11AssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML11_BEARER_URI);
   }

   /**
    * <p>
    * This tests sends a SAMLV2.0 security token request to PicketLinkSTS. This request should be handled by the
    * standard {@code SAML20TokenProvider} and should result in a SAMLV2.0 assertion that looks like the following:
    * 
    * <pre>
    * &lt;saml2:Assertion xmlns:saml2=&quot;urn:oasis:names:tc:SAML:2.0:assertion&quot; 
    *                  xmlns:ds=&quot;http://www.w3.org/2000/09/xmldsig#&quot; 
    *                  xmlns:xenc=&quot;http://www.w3.org/2001/04/xmlenc#&quot; 
    *                  ID=&quot;ID-cc541137-74dc-4fc0-8bcc-7e9e3a4c899d&quot;
    *                  IssueInstant=&quot;2009-05-29T18:02:13.458Z&quot;&gt;
    *     &lt;saml2:Issuer&gt;
    *         PicketLinkSTS
    *     &lt;/saml2:Issuer&gt;
    *     &lt;saml2:Subject&gt;
    *         &lt;saml2:NameID NameQualifier=&quot;http://www.jboss.org&quot;&gt;
    *             sguilhen
    *         &lt;/saml2:NameID&gt;
    *         &lt;saml2:SubjectConfirmation Method=&quot;urn:oasis:names:tc:SAML:2.0:cm:bearer&quot;/&gt;
    *     &lt;/saml2:Subject&gt;
    *     &lt;saml2:Conditions NotBefore=&quot;2009-05-29T18:02:13.458Z&quot; NotOnOrAfter=&quot;2009-05-29T19:02:13.458Z&quot;&gt;
    *         &lt;saml2:AudienceRestriction&gt;
    *             &lt;saml2:Audience&gt;
    *                 http://services.testcorp.org/provider2
    *             &lt;/saml2:Audience&gt;
    *         &lt;/saml2:AudienceRestriction&gt;
    *     &lt;/saml2:Conditions&gt;
    *     &lt;ds:Signature&gt;
    *         ...
    *     &lt;/ds:Signature&gt;
    * &lt;/saml2:Assertion&gt;
    * </pre>
    * 
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20() throws Exception
   {
      // create a simple token request, asking for a SAMLv2.0 token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      InputStream is = DocumentUtil.getSourceAsStream(responseMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser().parse(is);
      // validate the security token response.
      this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML2_BEARER_URI);
   }

   /**
    * <p>
    * This test requests a token to the STS using the {@code AppliesTo} to identify the service provider. The STS must
    * be able to find out the type of the token that must be issued using the service provider URI. In this specific
    * case, the request should be handled by the custom {@code SpecialTokenProvider}.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeCustomAppliesTo() throws Exception
   {
      // create a simple token request, this time using the applies to get to the token type.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider1");
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response.
      this.validateCustomTokenResponse(baseResponse);
   }

   /**
    * <p>
    * This test requests a token to the STS using the {@code AppliesTo} to identify the service provider. The STS must
    * be able to find out the type of the token that must be issued using the service provider URI. In this specific
    * case, the request should be handled by the standard {@code SAML20TokenProvider}.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20AppliesTo() throws Exception
   {
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response.
      AssertionType assertion = this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke",
            SAMLUtil.SAML2_BEARER_URI);

      // in this scenario, the conditions section should have an audience restriction.
      ConditionsType conditions = assertion.getConditions();
      assertEquals("Unexpected restriction list size", 1, conditions.getConditions().size());
      ConditionAbstractType abstractType = conditions.getConditions().get(0);
      assertTrue("Unexpected restriction type", abstractType instanceof AudienceRestrictionType);
      AudienceRestrictionType audienceRestriction = (AudienceRestrictionType) abstractType;
      assertEquals("Unexpected audience restriction list size", 1, audienceRestriction.getAudience().size());
      assertEquals("Unexpected audience restriction item", "http://services.testcorp.org/provider2",
            audienceRestriction.getAudience().get(0).toString());
   }

   /**
    * <p>
    * This test requests a SAMLV2.0 assertion on behalf of another identity. The STS must issue an assertion for the
    * identity contained in the {@code OnBehalfOf} section of the WS-Trust request (and not for the identity that sent
    * the request).
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20OnBehalfOf() throws Exception
   {
      // create a simple token request, asking for a SAMLv2.0 token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);
      OnBehalfOfType onBehalfOf = WSTrustUtil.createOnBehalfOfWithUsername("anotherduke", "id");
      request.setOnBehalfOf(onBehalfOf);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response (assertion principal should be anotherduke as specified by OnBehalfOf).
      this.validateSAMLAssertionResponse(baseResponse, "testcontext", "anotherduke", SAMLUtil.SAML2_SENDER_VOUCHES_URI);
   }

   /**
    * <p>
    * This test requests a SAMLV2.0 assertion and requires a symmetric key to be used as a proof-of-possession token. As
    * the request doesn't contain any client-specified key, the STS is responsible for generating a random key and use
    * this key as the proof token. The WS-Trust response should contain the STS-generated key.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20WithSTSGeneratedSymmetricKey() throws Exception
   {
      // create a simple token request, asking for a SAMLv2.0 token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");

      // add a symmetric key type to the request, but don't supply any client key - STS should generate one.
      request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_SYMMETRIC));
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response.
      AssertionType assertion = this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke",
            SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
      // validate the holder of key contents.
      SubjectConfirmationType subjConfirmation = assertion.getSubject().getConfirmation().get(0);
      this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_SYMMETRIC, null, false);

      // check if the response contains the STS-generated key.
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      RequestedProofTokenType proofToken = response.getRequestedProofToken();
      assertNotNull("Unexpected null proof token", proofToken);
      assertTrue(proofToken.getAny().get(0) instanceof BinarySecretType);
      BinarySecretType serverBinarySecret = (BinarySecretType) proofToken.getAny().get(0);
      assertNotNull("Unexpected null secret", serverBinarySecret.getValue());
      // default key size is 128 bits (16 bytes).
      byte[] encodedSecret = serverBinarySecret.getValue();
      assertEquals("Unexpected secret size", 16, Base64.decode(encodedSecret, 0, encodedSecret.length).length);
   }

   /**
    * <p>
    * This test requests a SAMLV2.0 assertion and requires a symmetric key to be used as a proof-of-possession token. In
    * this case, the client supplies a secret key in the WS-Trust request, so the STS should combine the client-
    * specified key with the STS-generated key and use this combined key as the proof token. The WS-Trust response
    * should include the STS key to allow reconstruction of the combined key and the algorithm used to combine the keys.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20WithCombinedSymmetricKey() throws Exception
   {
      // create a 64-bit random client secret.
      byte[] clientSecret = WSTrustUtil.createRandomSecret(8);
      BinarySecretType clientBinarySecret = new BinarySecretType();
      clientBinarySecret.setType(WSTrustConstants.BS_TYPE_NONCE);
      clientBinarySecret.setValue(Base64.encodeBytes(clientSecret).getBytes());

      // set the client secret in the client entropy.
      EntropyType clientEntropy = new EntropyType();
      clientEntropy.addAny(clientBinarySecret);

      // create a token request specifying the key type, key size, and client entropy.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");
      request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_SYMMETRIC));
      request.setEntropy(clientEntropy);
      request.setKeySize(64);

      // invoke the token service.
      Source requestMessage = this.createSourceFromRequest(request);
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response.
      AssertionType assertion = this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke",
            SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
      // validate the holder of key contents.
      SubjectConfirmationType subjConfirmation = assertion.getSubject().getConfirmation().get(0);
      this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_SYMMETRIC, null, false);

      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      RequestedProofTokenType proofToken = response.getRequestedProofToken();
      assertNotNull("Unexpected null proof token", proofToken);
      assertTrue(proofToken.getAny().get(0) instanceof ComputedKeyType);
      ComputedKeyType computedKey = (ComputedKeyType) proofToken.getAny().get(0);
      assertEquals("Unexpected computed key algorithm", WSTrustConstants.CK_PSHA1, computedKey.getAlgorithm());

      // server entropy must have been included in the response to allow reconstruction of the computed key.
      EntropyType serverEntropy = response.getEntropy();
      assertNotNull("Unexpected null server entropy");
      assertEquals("Invalid number of elements in server entropy", 1, serverEntropy.getAny().size());
      BinarySecretType serverBinarySecret = (BinarySecretType) serverEntropy.getAny().get(0);
      assertEquals("Unexpected binary secret type", WSTrustConstants.BS_TYPE_NONCE, serverBinarySecret.getType());
      assertNotNull("Unexpected null secret value", serverBinarySecret.getValue());
      // get the base64 decoded
      byte[] encodedSecret = serverBinarySecret.getValue();
      assertEquals("Unexpected secret size", 8, Base64.decode(encodedSecret, 0, encodedSecret.length).length);
   }

   /**
    * <p>
    * This test requests a SAMLV2.0 assertion and sends a X.509 certificate to be used as the proof-of-possession token.
    * The STS must include the specified certificate in the SAML subject confirmation.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20WithCertificate() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");
      request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_PUBLIC));

      // include a UseKey section that specifies the certificate in the request.
      Certificate certificate = this.getCertificate("keystore/sts_keystore.jks", "testpass", "service1");
      UseKeyType useKey = new UseKeyType();
      useKey.add(Base64.encodeBytes(certificate.getEncoded()).getBytes());
      request.setUseKey(useKey);

      // invoke the token service.
      Source requestMessage = this.createSourceFromRequest(request);
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));
      // validate the security token response.
      AssertionType assertion = this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke",
            SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
      // validate the holder of key contents.
      SubjectConfirmationType subjConfirmation = assertion.getSubject().getConfirmation().get(0);
      this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_PUBLIC, certificate, false);
   }

   /**
    * <p>
    * This test requests a SAMLV2.0 assertion and sends a public key to be used as the proof-of-possession token. The
    * STS must include the specified public key in the SAML subject confirmation.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20WithPublicKey() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");
      request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_PUBLIC));

      // include a UseKey section that sets the public key in the request.
      Certificate certificate = this.getCertificate("keystore/sts_keystore.jks", "testpass", "service1");
      KeyValueType keyValue = WSTrustUtil.createKeyValue(certificate.getPublicKey());
      UseKeyType useKey = new UseKeyType();
      useKey.add(keyValue);
      request.setUseKey(useKey);

      // invoke the token service.
      Source requestMessage = this.createSourceFromRequest(request);
      Source responseMessage = this.tokenService.invoke(requestMessage);
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) new WSTrustParser()
            .parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the security token response.
      AssertionType assertion = this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke",
            SAMLUtil.SAML2_HOLDER_OF_KEY_URI);
      // validate the holder of key contents.
      SubjectConfirmationType subjConfirmation = assertion.getSubject().getConfirmation().get(0);
      this.validateHolderOfKeyContents(subjConfirmation, WSTrustConstants.KEY_TYPE_PUBLIC, certificate, true);
   }

   /**
    * <p>
    * This test case first generates a SAMLV1.1 assertion and then sends a WS-Trust validate message to the STS to get
    * the assertion validated, checking the validation results.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML11Validate() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML11_TOKEN_TYPE, null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAML11AssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML11_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertion = (Element) collection.getRequestSecurityTokenResponses().get(0).getRequestedSecurityToken()
            .getAny().get(0);

      // now construct a WS-Trust validate request with the generated assertion.
      request = this.createRequest("validatecontext", WSTrustConstants.VALIDATE_REQUEST, WSTrustConstants.STATUS_TYPE,
            null);
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(assertion);
      request.setValidateTarget(validateTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the response contents.
      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "validatecontext", response.getContext());
      assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      StatusType status = response.getStatus();
      assertNotNull("Unexpected null status", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_VALID, status.getCode());
      assertEquals("Unexpected status reason", "SAMLV1.1 Assertion successfuly validated", status.getReason());

      // now let's temper the SAML assertion and try to validate it again.
      // assertion.getAttributeNode("Issuer").setNodeValue("ABC");
      // request.getValidateTarget().add(assertion);
      // Source theRequest = this.createSourceFromRequest(request);
      // responseMessage = this.tokenService.invoke(theRequest);
      // collection = (RequestSecurityTokenResponseCollection) parser.parse(DocumentUtil
      // .getSourceAsStream(responseMessage));
      // assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      // response = collection.getRequestSecurityTokenResponses().get(0);
      // assertEquals("Unexpected response context", "validatecontext", response.getContext());
      // assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      // status = response.getStatus();
      // assertNotNull("Unexpected null status", status);
      // assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_INVALID, status.getCode());
      // assertEquals("Unexpected status reason", "Validation failure: digital signature is invalid",
      // status.getReason());
   }

   /**
    * <p>
    * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust validate message to the STS to get
    * the assertion validated, checking the validation results.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20Validate() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML2_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertion = (Element) collection.getRequestSecurityTokenResponses().get(0).getRequestedSecurityToken()
            .getAny().get(0);

      // now construct a WS-Trust validate request with the generated assertion.
      request = this.createRequest("validatecontext", WSTrustConstants.VALIDATE_REQUEST, WSTrustConstants.STATUS_TYPE,
            null);
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(assertion);
      request.setValidateTarget(validateTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the response contents.
      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "validatecontext", response.getContext());
      assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      StatusType status = response.getStatus();
      assertNotNull("Unexpected null status", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_VALID, status.getCode());
      assertEquals("Unexpected status reason", "SAMLV2.0 Assertion successfuly validated", status.getReason());

      // now let's temper the SAML assertion and try to validate it again.
      assertion.setAttribute("Version", "X");
      request.getValidateTarget().add(assertion);
      Source theRequest = this.createSourceFromRequest(request);
      responseMessage = this.tokenService.invoke(theRequest);
      collection = (RequestSecurityTokenResponseCollection) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "validatecontext", response.getContext());
      assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      status = response.getStatus();
      assertNotNull("Unexpected null status", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_INVALID, status.getCode());
      assertEquals("Unexpected status reason", "Validation failure: digital signature is invalid", status.getReason());
   }

   /**
    * <p>
    * This test case first generates a SAMLV1.1 assertion and then sends a WS-Trust renew message to the STS to get the
    * assertion renewed (i.e. get a new assertion with an updated lifetime).
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML11Renew() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML11_TOKEN_TYPE, null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAML11AssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML11_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertionElement = (Element) collection.getRequestSecurityTokenResponses().get(0)
            .getRequestedSecurityToken().getAny().get(0);

      // now construct a WS-Trust renew request with the generated assertion.
      request = this.createRequest("renewcontext", WSTrustConstants.RENEW_REQUEST, SAMLUtil.SAML11_TOKEN_TYPE, null);
      RenewTargetType renewTarget = new RenewTargetType();
      renewTarget.add(assertionElement);
      request.setRenewTarget(renewTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the renew response contents and get the renewed token.
      this.validateSAML11AssertionResponse(baseResponse, "renewcontext", "jduke", SAMLUtil.SAML11_BEARER_URI);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element renewedAssertionElement = (Element) collection.getRequestSecurityTokenResponses().get(0)
            .getRequestedSecurityToken().getAny().get(0);

      // compare the assertions, checking if the lifetime has been updated.
      SAML11AssertionType originalAssertion = SAMLUtil.saml11FromElement(assertionElement);
      SAML11AssertionType renewedAssertion = SAMLUtil.saml11FromElement(renewedAssertionElement);

      // assertions should have different ids and lifetimes.
      assertFalse("Renewed assertion should have a unique id",
            originalAssertion.getID().equals(renewedAssertion.getID()));
      assertEquals(DatatypeConstants.LESSER,
            originalAssertion.getConditions().getNotBefore().compare(renewedAssertion.getConditions().getNotBefore()));
      assertEquals(
            DatatypeConstants.LESSER,
            originalAssertion.getConditions().getNotOnOrAfter()
                  .compare(renewedAssertion.getConditions().getNotOnOrAfter()));
   }

   /**
    * <p>
    * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust renew message to the STS to get the
    * assertion renewed (i.e. get a new assertion with an updated lifetime).
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20Renew() throws Exception
   {
      // create a simple token request, using applies-to to identify the token type.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null,
            "http://services.testcorp.org/provider2");

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML2_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertionElement = (Element) collection.getRequestSecurityTokenResponses().get(0)
            .getRequestedSecurityToken().getAny().get(0);

      // now construct a WS-Trust renew request with the generated assertion.
      request = this.createRequest("renewcontext", WSTrustConstants.RENEW_REQUEST, SAMLUtil.SAML2_TOKEN_TYPE, null);
      RenewTargetType renewTarget = new RenewTargetType();
      renewTarget.add(assertionElement);
      request.setRenewTarget(renewTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the renew response contents and get the renewed token.
      this.validateSAMLAssertionResponse(baseResponse, "renewcontext", "jduke", SAMLUtil.SAML2_BEARER_URI);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element renewedAssertionElement = (Element) collection.getRequestSecurityTokenResponses().get(0)
            .getRequestedSecurityToken().getAny().get(0);

      // compare the assertions, checking if the lifetime has been updated.
      AssertionType originalAssertion = SAMLUtil.fromElement(assertionElement);
      AssertionType renewedAssertion = SAMLUtil.fromElement(renewedAssertionElement);

      // assertions should have different ids and lifetimes.
      assertFalse("Renewed assertion should have a unique id",
            originalAssertion.getID().equals(renewedAssertion.getID()));
      assertEquals(DatatypeConstants.LESSER,
            originalAssertion.getConditions().getNotBefore().compare(renewedAssertion.getConditions().getNotBefore()));
      assertEquals(
            DatatypeConstants.LESSER,
            originalAssertion.getConditions().getNotOnOrAfter()
                  .compare(renewedAssertion.getConditions().getNotOnOrAfter()));
   }

   /**
    * <p>
    * This test case first generates a SAMLV1.1 assertion and then sends a WS-Trust cancel message to the STS to cancel
    * the assertion. A canceled assertion cannot be renewed or considered valid anymore.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML11Cancel() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML11_TOKEN_TYPE, null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAML11AssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML11_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertion = (Element) collection.getRequestSecurityTokenResponses().get(0).getRequestedSecurityToken()
            .getAny().get(0);

      // now construct a WS-Trust cancel request with the generated assertion.
      request = this.createRequest("cancelcontext", WSTrustConstants.CANCEL_REQUEST, null, null);
      CancelTargetType cancelTarget = new CancelTargetType();
      cancelTarget.add(assertion);
      request.setCancelTarget(cancelTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the response contents.
      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "cancelcontext", response.getContext());
      assertNotNull("Cancel response should contain a RequestedTokenCancelled element",
            response.getRequestedTokenCancelled());

      // try to validate the canceled assertion.
      request = this.createRequest("validatecontext", WSTrustConstants.VALIDATE_REQUEST, null, null);
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(assertion);
      request.setValidateTarget(validateTarget);

      // the response should contain a status indicating that the token is not valid.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      collection = (RequestSecurityTokenResponseCollection) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "validatecontext", response.getContext());
      assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      StatusType status = response.getStatus();
      assertNotNull("Unexpected null status", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_INVALID, status.getCode());
      assertEquals("Unexpected status reason",
            "Validation failure: assertion with id " + assertion.getAttribute("AssertionID") + " has been canceled",
            status.getReason());

      // now try to renew the canceled assertion.
      request = this.createRequest("renewcontext", WSTrustConstants.RENEW_REQUEST, null, null);
      RenewTargetType renewTarget = new RenewTargetType();
      renewTarget.add(assertion);
      request.setRenewTarget(renewTarget);

      // we should receive an exception when renewing the token.
      try
      {
         this.tokenService.invoke(this.createSourceFromRequest(request));
         fail("Renewing a canceled token should result in an exception being thrown");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("has been canceled and cannot be renewed") == false)
            throw new RuntimeException("Unexpected exception message");
      }
   }

   /**
    * <p>
    * This test case first generates a SAMLV2.0 assertion and then sends a WS-Trust cancel message to the STS to cancel
    * the assertion. A canceled assertion cannot be renewed or considered valid anymore.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeSAML20Cancel() throws Exception
   {
      // create a simple token request.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      Source responseMessage = this.tokenService.invoke(requestMessage);
      WSTrustParser parser = new WSTrustParser();
      BaseRequestSecurityTokenResponse baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));

      // validate the response and get the SAML assertion from the request.
      this.validateSAMLAssertionResponse(baseResponse, "testcontext", "jduke", SAMLUtil.SAML2_BEARER_URI);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      Element assertion = (Element) collection.getRequestSecurityTokenResponses().get(0).getRequestedSecurityToken()
            .getAny().get(0);

      // now construct a WS-Trust cancel request with the generated assertion.
      request = this.createRequest("cancelcontext", WSTrustConstants.CANCEL_REQUEST, null, null);
      CancelTargetType cancelTarget = new CancelTargetType();
      cancelTarget.add(assertion);
      request.setCancelTarget(cancelTarget);

      // invoke the token service.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      baseResponse = (BaseRequestSecurityTokenResponse) parser.parse(DocumentUtil.getSourceAsStream(responseMessage));

      // validate the response contents.
      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "cancelcontext", response.getContext());
      assertNotNull("Cancel response should contain a RequestedTokenCancelled element",
            response.getRequestedTokenCancelled());

      // try to validate the canceled assertion.
      request = this.createRequest("validatecontext", WSTrustConstants.VALIDATE_REQUEST, null, null);
      ValidateTargetType validateTarget = new ValidateTargetType();
      validateTarget.add(assertion);
      request.setValidateTarget(validateTarget);

      // the response should contain a status indicating that the token is not valid.
      responseMessage = this.tokenService.invoke(this.createSourceFromRequest(request));
      collection = (RequestSecurityTokenResponseCollection) parser.parse(DocumentUtil
            .getSourceAsStream(responseMessage));
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "validatecontext", response.getContext());
      assertEquals("Unexpected token type", WSTrustConstants.STATUS_TYPE, response.getTokenType().toString());
      StatusType status = response.getStatus();
      assertNotNull("Unexpected null status", status);
      assertEquals("Unexpected status code", WSTrustConstants.STATUS_CODE_INVALID, status.getCode());
      assertEquals("Unexpected status reason", "Validation failure: assertion with id " + assertion.getAttribute("ID")
            + " has been canceled", status.getReason());

      // now try to renew the canceled assertion.
      request = this.createRequest("renewcontext", WSTrustConstants.RENEW_REQUEST, null, null);
      RenewTargetType renewTarget = new RenewTargetType();
      renewTarget.add(assertion);
      request.setRenewTarget(renewTarget);

      // we should receive an exception when renewing the token.
      try
      {
         this.tokenService.invoke(this.createSourceFromRequest(request));
         fail("Renewing a canceled token should result in an exception being thrown");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("has been canceled and cannot be renewed") == false)
            throw new RuntimeException("Unexpected exception message");
      }
   }

   /**
    * <p>
    * This test tries to request a token of an unknown type, checking if an exception is correctly thrown by the
    * security token service.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvokeUnknownTokenType() throws Exception
   {
      // create a simple token request, asking for an "unknown" test token.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST,
            "http://www.tokens.org/UnknownToken", null);

      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the security token service.
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         assertNotNull("Unexpected null cause", we.getCause());
         assertTrue("Unexpected cause type", we.getCause() instanceof WSTrustException);
      }
   }

   /**
    * <p>
    * This test verifies if the token service is correctly identifying invalid issue requests.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvalidIssueRequests() throws Exception
   {
      // lets create an issue request that container neither an applies-to nor a token type.
      RequestSecurityToken request = this.createRequest("testcontext", WSTrustConstants.ISSUE_REQUEST, null, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service. A WSTrustException should be raised.
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         assertNotNull("Unexpected null cause", we.getCause());
         assertTrue("Unexpected cause type", we.getCause() instanceof WSTrustException);
         /*
          * assertEquals("Either AppliesTo or TokenType must be present in a security token request", we.getCause()
          * .getMessage());
          */
      }

      // a request that asks for a public key to be used as proof key will fail if the public key is not available.
      request.setTokenType(URI.create(SAMLUtil.SAML2_TOKEN_TYPE));
      request.setKeyType(URI.create(WSTrustConstants.KEY_TYPE_PUBLIC));
      requestMessage = this.createSourceFromRequest(request);

      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("Unable to locate client public key") == false)
            throw new RuntimeException("Unexpected exception message");
      }
   }

   /**
    * <p>
    * This test verifies if the token service is correctly identifying invalid renew requests.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvalidRenewRequests() throws Exception
   {
      // first create a request that doesn't have a renew target element.
      RequestSecurityToken request = this.createRequest("renewcontext", WSTrustConstants.RENEW_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("Unable to renew token: request does not have a renew target") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request with an empty renew target should also result in a failure.
      request.setRenewTarget(new RenewTargetType());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof ParsingException);
         String msg = t.getMessage();
         if (msg.contains("Unable to parse token request: security token is null") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request to renew an unknown token (i.e. there's no provider can handle the token) should also fail.
      request.getRenewTarget().add(this.createUnknownToken());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains(ErrorCodes.INVALID_DIGITAL_SIGNATURE) == false)
            throw new RuntimeException("Unexpected error msg");
      }
   }

   /**
    * <p>
    * This test verifies if the token service is correctly identifying invalid validate requests.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvalidValidateRequests() throws Exception
   {
      // first create a request that doesn't have a validate target element.
      RequestSecurityToken request = this.createRequest("validatecontext", WSTrustConstants.VALIDATE_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("request does not have a validate target. Unable to validate token") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request with an empty validate target should also result in a failure.
      request.setValidateTarget(new ValidateTargetType());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof ParsingException);
         String msg = t.getMessage();
         if (msg.contains("Unable to parse token request: security token is null") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request to validate an unknown token (i.e. there's no provider can handle the token) should also fail.
      request.getValidateTarget().add(this.createUnknownToken());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         Source responseMessage = this.tokenService.invoke(requestMessage);
         RequestSecurityTokenResponseCollection baseResponseColl = (RequestSecurityTokenResponseCollection) new WSTrustParser()
               .parse(DocumentUtil.getSourceAsStream(responseMessage));

         RequestSecurityTokenResponse response = baseResponseColl.getRequestSecurityTokenResponses().get(0);
         StatusType status = response.getStatus();
         assertTrue(status.getCode().equals(WSTrustConstants.STATUS_CODE_INVALID));
         //fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         assertNotNull("Unexpected null cause", we.getCause());
         assertTrue("Unexpected cause type", we.getCause() instanceof WSTrustException);
         assertEquals("No SecurityTokenProvider configured for http://www.unknowntoken.org:UnknownToken", we.getCause()
               .getMessage());
      }
   }

   /**
    * <p>
    * This test verifies if the token service is correctly identifying invalid cancel requests.
    * </p>
    * 
    * @throws Exception
    *            if an error occurs while running the test.
    */
   @Test
   public void testInvalidCancelRequests() throws Exception
   {
      // first create a request that doesn't have a cancel target element.
      RequestSecurityToken request = this.createRequest("cancelcontext", WSTrustConstants.CANCEL_REQUEST,
            SAMLUtil.SAML2_TOKEN_TYPE, null);
      Source requestMessage = this.createSourceFromRequest(request);

      // invoke the token service.
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains("request does not have a cancel target. Unable to cancel token") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request with an empty cancel target should also result in a failure.
      request.setCancelTarget(new CancelTargetType());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof ParsingException);
         String msg = t.getMessage();
         if (msg.contains("Unable to parse token request: security token is null") == false)
            throw new RuntimeException("Unexpected exception message");
      }

      // a request to cancel an unknown token (i.e. there's no provider can handle the token) should also fail.
      request.getCancelTarget().add(this.createUnknownToken());
      requestMessage = this.createSourceFromRequest(request);
      try
      {
         this.tokenService.invoke(requestMessage);
         fail("An exception should have been raised by the security token service");
      }
      catch (WebServiceException we)
      {
         Throwable t = we.getCause();
         assertNotNull("Unexpected null cause", t);
         assertTrue("Unexpected cause type", t instanceof WSTrustException);
         String msg = t.getMessage();
         if (msg.contains(ErrorCodes.STS_NO_TOKEN_PROVIDER) == false)
            throw new RuntimeException("Unexpected exception message");
      }
   }

   /**
    * <p>
    * Validates the contents of a WS-Trust response message that contains a custom token issued by the test {@code
    * SpecialTokenProvider}.
    * </p>
    * 
    * @param baseResponse
    *           a reference to the WS-Trust response that was sent by the STS.
    * @throws Exception
    *            if one of the validation performed fail.
    */
   private void validateCustomTokenResponse(BaseRequestSecurityTokenResponse baseResponse) throws Exception
   {

      // =============================== WS-Trust Security Token Response Validation ===============================//

      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", "testcontext", response.getContext());
      assertEquals("Unexpected token type", "http://www.tokens.org/SpecialToken", response.getTokenType().toString());
      Lifetime lifetime = response.getLifetime();
      assertNotNull("Unexpected null token lifetime", lifetime);

      // ========================================= Custom Token Validation =========================================//

      RequestedSecurityTokenType requestedToken = response.getRequestedSecurityToken();
      assertNotNull("Unexpected null requested security token", requestedToken);
      Object token = requestedToken.getAny().get(0);
      assertNotNull("Unexpected null token", token);
      assertTrue("Unexpected token class", token instanceof Element);
      Element element = (Element) requestedToken.getAny().get(0);
      assertEquals("Unexpected root element name", "SpecialToken", element.getLocalName());
      assertEquals("Unexpected namespace value", "http://www.tokens.org", element.getNamespaceURI());
      assertEquals("Unexpected attribute value", "http://www.tokens.org/SpecialToken",
            element.getAttribute("TokenType"));
      element = (Element) element.getFirstChild();
      assertEquals("Unexpected child element name", "SpecialTokenValue", element.getLocalName());
      assertEquals("Unexpected token value", "Principal:jduke", element.getFirstChild().getNodeValue());
   }

   private SAML11AssertionType validateSAML11AssertionResponse(BaseRequestSecurityTokenResponse baseResponse,
         String context, String principal, String confirmationMethod) throws Exception
   {

      // =============================== WS-Trust Security Token Response Validation ===============================//

      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", context, response.getContext());
      assertEquals("Unexpected token type", SAMLUtil.SAML11_TOKEN_TYPE, response.getTokenType().toString());
      Lifetime lifetime = response.getLifetime();
      assertNotNull("Unexpected null token lifetime", lifetime);

      // validate the attached token reference.
      RequestedReferenceType reference = response.getRequestedAttachedReference();
      assertNotNull("Unexpected null attached reference", reference);
      SecurityTokenReferenceType securityRef = reference.getSecurityTokenReference();
      assertNotNull("Unexpected null security reference", securityRef);
      String tokenTypeAttr = securityRef.getOtherAttributes().get(new QName(WSTrustConstants.WSSE11_NS, "TokenType"));
      assertNotNull("Required attribute TokenType is missing", tokenTypeAttr);
      assertEquals("TokenType attribute has an unexpected value", SAMLUtil.SAML11_TOKEN_TYPE, tokenTypeAttr);
      KeyIdentifierType keyId = (KeyIdentifierType) securityRef.getAny().get(0);
      assertEquals("Unexpected key value type", SAMLUtil.SAML11_VALUE_TYPE, keyId.getValueType());
      assertNotNull("Unexpected null key identifier value", keyId.getValue());

      // ====================================== SAMLV1.1 Assertion Validation ======================================//

      RequestedSecurityTokenType requestedToken = response.getRequestedSecurityToken();
      assertNotNull("Unexpected null requested security token", requestedToken);

      // unmarshall the SAMLV1.1 assertion.
      Element assertionElement = (Element) requestedToken.getAny().get(0);
      SAML11AssertionType assertion = SAMLUtil.saml11FromElement(assertionElement);

      // verify the contents of the unmarshalled assertion.
      assertNotNull("Invalid null assertion ID", assertion.getID());
      assertEquals(keyId.getValue().substring(1), assertion.getID());
      assertEquals(lifetime.getCreated(), assertion.getIssueInstant());
      assertEquals(1, assertion.getMajorVersion());
      assertEquals(1, assertion.getMinorVersion());

      // validate the assertion issuer.
      assertNotNull("Unexpected null assertion issuer", assertion.getIssuer());
      assertEquals("Unexpected assertion issuer name", "Test STS", assertion.getIssuer());

      // validate the assertion authentication statement.
      List<SAML11StatementAbstractType> statements = assertion.getStatements();
      assertTrue("At least one statement is expected in a SAMLV1.1 assertion", statements.size() > 0);
      SAML11AuthenticationStatementType authStatement = null;
      for (SAML11StatementAbstractType statement : statements)
      {
         if (statement instanceof SAML11AuthenticationStatementType)
         {
            authStatement = (SAML11AuthenticationStatementType) statement;
            break;
         }
      }
      assertNotNull("SAMLV1.1 assertion is missing the authentication statement", authStatement);

      // validate the assertion subject.
      assertNotNull("Unexpected null subject", authStatement.getSubject());
      SAML11SubjectType subject = authStatement.getSubject();

      SAML11NameIdentifierType nameID = subject.getChoice().getNameID();
      assertEquals("Unexpected NameIdentifier format", SAML11Constants.FORMAT_UNSPECIFIED, nameID.getFormat()
            .toString());
      assertEquals("Unexpected NameIdentifier value", principal, nameID.getValue());

      SAML11SubjectConfirmationType subjType = subject.getSubjectConfirmation();
      assertEquals("Unexpected confirmation method", confirmationMethod, subjType.getConfirmationMethod().get(0)
            .toString());

      // validate the assertion conditions.
      assertNotNull("Unexpected null conditions", assertion.getConditions());
      assertEquals(lifetime.getCreated(), assertion.getConditions().getNotBefore());
      assertEquals(lifetime.getExpires(), assertion.getConditions().getNotOnOrAfter());

      assertNotNull("Assertion should have been signed", assertion.getSignature());

      return assertion;
   }

   /**
    * <p>
    * Validates the contents of a WS-Trust response message that contains a SAMLV2.0 assertion issued by the {@code
    * SAML20TokenProvider}.
    * </p>
    * 
    * @param baseResponse
    *           a reference to the WS-Trust response that was sent by the STS.
    * @param context
    *           the expected name of the response context.
    * @param principal
    *           the principal that is expected to be seen in the assertion subject.
    * @param confirmationMethod
    *           the confirmation method that is expected to be seen in the assertion subject.
    * @return the SAMLV2.0 assertion that has been extracted from the response. This object can be used by the test
    *         methods to perform extra validations depending on the scenario being tested.
    * @throws Exception
    *            if an error occurs while performing the validation.
    */
   private AssertionType validateSAMLAssertionResponse(BaseRequestSecurityTokenResponse baseResponse, String context,
         String principal, String confirmationMethod) throws Exception
   {

      // =============================== WS-Trust Security Token Response Validation ===============================//

      assertNotNull("Unexpected null response", baseResponse);
      assertTrue("Unexpected response type", baseResponse instanceof RequestSecurityTokenResponseCollection);
      RequestSecurityTokenResponseCollection collection = (RequestSecurityTokenResponseCollection) baseResponse;
      assertEquals("Unexpected number of responses", 1, collection.getRequestSecurityTokenResponses().size());
      RequestSecurityTokenResponse response = collection.getRequestSecurityTokenResponses().get(0);
      assertEquals("Unexpected response context", context, response.getContext());
      assertEquals("Unexpected token type", SAMLUtil.SAML2_TOKEN_TYPE, response.getTokenType().toString());
      Lifetime lifetime = response.getLifetime();
      assertNotNull("Unexpected null token lifetime", lifetime);

      // validate the attached token reference.
      RequestedReferenceType reference = response.getRequestedAttachedReference();
      assertNotNull("Unexpected null attached reference", reference);
      SecurityTokenReferenceType securityRef = reference.getSecurityTokenReference();
      assertNotNull("Unexpected null security reference", securityRef);
      String tokenTypeAttr = securityRef.getOtherAttributes().get(new QName(WSTrustConstants.WSSE11_NS, "TokenType"));
      assertNotNull("Required attribute TokenType is missing", tokenTypeAttr);
      assertEquals("TokenType attribute has an unexpected value", SAMLUtil.SAML2_TOKEN_TYPE, tokenTypeAttr);
      KeyIdentifierType keyId = (KeyIdentifierType) securityRef.getAny().get(0);
      assertEquals("Unexpected key value type", SAMLUtil.SAML2_VALUE_TYPE, keyId.getValueType());
      assertNotNull("Unexpected null key identifier value", keyId.getValue());

      // ====================================== SAMLV2.0 Assertion Validation ======================================//

      RequestedSecurityTokenType requestedToken = response.getRequestedSecurityToken();
      assertNotNull("Unexpected null requested security token", requestedToken);

      // unmarshall the SAMLV2.0 assertion.
      Element assertionElement = (Element) requestedToken.getAny().get(0);
      AssertionType assertion = SAMLUtil.fromElement(assertionElement);

      // verify the contents of the unmarshalled assertion.
      assertNotNull("Invalid null assertion ID", assertion.getID());
      assertEquals(keyId.getValue().substring(1), assertion.getID());
      assertEquals(lifetime.getCreated(), assertion.getIssueInstant());

      // validate the assertion issuer.
      assertNotNull("Unexpected null assertion issuer", assertion.getIssuer());
      assertEquals("Unexpected assertion issuer name", "Test STS", assertion.getIssuer().getValue());

      // validate the assertion subject.
      assertNotNull("Unexpected null subject", assertion.getSubject());
      SubjectType subject = assertion.getSubject();

      NameIDType nameID = (NameIDType) subject.getSubType().getBaseID();
      assertEquals("Unexpected name id qualifier", "urn:picketlink:identity-federation", nameID.getNameQualifier());
      assertEquals("Unexpected name id value", principal, nameID.getValue());

      SubjectConfirmationType subjType = subject.getConfirmation().get(0);
      assertEquals("Unexpected confirmation method", confirmationMethod, subjType.getMethod());

      // validate the assertion conditions.
      assertNotNull("Unexpected null conditions", assertion.getConditions());
      assertEquals(lifetime.getCreated(), assertion.getConditions().getNotBefore());
      assertEquals(lifetime.getExpires(), assertion.getConditions().getNotOnOrAfter());

      assertNotNull("Assertion should have been signed", assertion.getSignature());

      return assertion;
   }

   /**
    * <p>
    * Validates the contents of the specified {@code SubjectConfirmationType} when the {@code HOLDER_OF_KEY}
    * confirmation method has been used.
    * </p>
    * 
    * @param subjectConfirmation
    *           the {@code SubjectConfirmationType} to be validated.
    * @param keyType
    *           the type of the proof-of-possession key (Symmetric or Public).
    * @param certificate
    *           the certificate used in the Public Key scenarios.
    * @param usePublicKey
    *           {@code true} if the certificate's Public Key was used as the proof-of-possession token; {@code false}
    *           otherwise.
    * @throws Exception
    *            if an error occurs while performing the validation.
    */
   private void validateHolderOfKeyContents(SubjectConfirmationType subjectConfirmation, String keyType,
         Certificate certificate, boolean usePublicKey) throws Exception
   {
      SubjectConfirmationDataType subjConfirmationDataType = subjectConfirmation.getSubjectConfirmationData();
      assertNotNull("Unexpected null subject confirmation data", subjConfirmationDataType);
      KeyInfoType keyInfo = (KeyInfoType) subjConfirmationDataType.getAnyType();
      assertEquals("Unexpected key info content size", 1, keyInfo.getContent().size());

      // if the key is a symmetric key, the KeyInfo should contain an encrypted element.
      if (WSTrustConstants.KEY_TYPE_SYMMETRIC.equals(keyType))
      {
         Element encKeyElement = (Element) keyInfo.getContent().get(0);
         assertEquals("Unexpected key info content type", WSTrustConstants.XMLEnc.ENCRYPTED_KEY,
               encKeyElement.getLocalName());
      }
      // if the key is public, KeyInfo should either contain an encoded certificate or an encoded public key.
      else if (WSTrustConstants.KEY_TYPE_PUBLIC.equals(keyType))
      {
         // if the public key has been used as proof, we should be able to retrieve it from KeyValueType.
         if (usePublicKey == true)
         {
            KeyValueType keyValue = (KeyValueType) keyInfo.getContent().get(0);
            List<Object> keyValueContent = keyValue.getContent();
            assertEquals("Unexpected key value content size", 1, keyValueContent.size());
            assertEquals("Unexpected key value content type", RSAKeyValueType.class, keyValueContent.get(0).getClass());
            RSAKeyValueType rsaKeyValue = (RSAKeyValueType) keyValueContent.get(0);

            // reconstruct the public key and check if it matches the public key of the provided certificate.
            BigInteger modulus = new BigInteger(1, Base64.decode(new String(rsaKeyValue.getModulus())));
            BigInteger exponent = new BigInteger(1, Base64.decode(new String(rsaKeyValue.getExponent())));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            RSAPublicKey genKey = (RSAPublicKey) factory.generatePublic(spec);
            assertEquals("Invalid public key", certificate.getPublicKey(), genKey);
         }
         // if the whole certificate was used as proof, we should be able to retrieve it from X509DataType.
         else
         {
            X509DataType x509Data = (X509DataType) keyInfo.getContent().get(0);
            assertEquals("Unexpected X509 data content size", 1, x509Data.getDataObjects().size());
            Object content = x509Data.getDataObjects().get(0);
            assertTrue("Unexpected X509 data content type", content instanceof X509CertificateType);
            byte[] encodedCertificate = ((X509CertificateType) content).getEncodedCertificate();

            // reconstruct the certificate and check if it matches the provided certificate.
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(Base64.decode(encodedCertificate, 0,
                  encodedCertificate.length));
            assertEquals("Invalid certificate in key info", certificate, CertificateFactory.getInstance("X.509")
                  .generateCertificate(byteInputStream));
         }
      }
   }

   /**
    * <p>
    * Utility method that creates a simple WS-Trust request using the specified information.
    * </p>
    * 
    * @param context
    *           a {@code String} that represents the request context.
    * @param requestType
    *           a {@code String} that represents the WS-Trust request type.
    * @param tokenType
    *           a {@code String} that represents the requested token type.
    * @param appliesToString
    *           a {@code String} that represents the URL of a service provider.
    * @return the constructed {@code RequestSecurityToken} object.
    */
   private RequestSecurityToken createRequest(String context, String requestType, String tokenType,
         String appliesToString)
   {
      RequestSecurityToken request = new RequestSecurityToken();
      request.setContext(context);
      request.setRequestType(URI.create(requestType));
      if (tokenType != null)
         request.setTokenType(URI.create(tokenType));
      if (appliesToString != null)
         request.setAppliesTo(WSTrustUtil.createAppliesTo(appliesToString));
      return request;
   }

   /**
    * <p>
    * Creates a simple token that is not known to the STS for testing purposes.
    * </p>
    * 
    * @return an {@code Element} representing the unknown token.
    * @throws Exception
    *            if an error occurs while creating the token.
    */
   private Element createUnknownToken() throws Exception
   {
      Document doc = DocumentUtil.createDocument();
      String namespaceURI = "http://www.unknowntoken.org";
      Element root = doc.createElementNS(namespaceURI, "token:UnknownToken");
      Element child = doc.createElementNS(namespaceURI, "token:UnknownTokenValue");
      child.appendChild(doc.createTextNode("Unknown content"));
      root.appendChild(child);
      String id = IDGenerator.create("ID_");
      root.setAttributeNS(namespaceURI, "ID", id);
      root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:token", namespaceURI);
      return root;
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
      // write the request XML to a DOMResult
      DOMResult result = new DOMResult(DocumentUtil.createDocument());
      WSTrustRequestWriter writer = new WSTrustRequestWriter(result);
      writer.write(request);
      return new DOMSource(result.getNode());
   }

   /**
    * <p>
    * Helper class that exposes the PicketLinkSTS methods as public for the tests to work.
    * </p>
    * 
    * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
    */
   class TestSTS extends PicketLinkSTS
   {
      private String configFileName = "sts/picketlink-sts.xml";

      TestSTS()
      {
      }

      TestSTS(String configFileName)
      {
         this.configFileName = configFileName;
      }

      public Source invoke(Source source)
      {
         try
         {
            SOAPMessage request = SOAPUtil.create();
            SOAPUtil.addData(source, request);
            SOAPMessage response = super.invoke(request);
            return new DOMSource(SOAPUtil.getSOAPData(response));
         }
         catch (SOAPException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public STSConfiguration getConfiguration() throws ConfigurationException
      {
         InputStream stream;
         try
         {
            URL configURL = Thread.currentThread().getContextClassLoader().getResource(configFileName);
            stream = configURL.openStream();

            STSType stsConfig = (STSType) new STSConfigParser().parse(stream);
            return new PicketLinkSTSConfiguration(stsConfig);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }

      public void setContext(WebServiceContext context)
      {
         super.context = context;
      }
   }

   /**
    * <p>
    * Helper class that mocks a {@code WebServiceContext}. It is used in the PicketLink STS test cases.
    * </p>
    * 
    * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
    */
   class TestContext implements WebServiceContext
   {

      private Principal principal;

      /*
       * (non-Javadoc)
       * 
       * @see javax.xml.ws.WebServiceContext#getEndpointReference(java.lang.Class, org.w3c.dom.Element[])
       */
      public <T extends EndpointReference> T getEndpointReference(Class<T> arg0, Element... arg1)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see javax.xml.ws.WebServiceContext#getEndpointReference(org.w3c.dom.Element[])
       */
      public EndpointReference getEndpointReference(Element... arg0)
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see javax.xml.ws.WebServiceContext#getMessageContext()
       */
      public MessageContext getMessageContext()
      {
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see javax.xml.ws.WebServiceContext#getUserPrincipal()
       */
      public Principal getUserPrincipal()
      {
         return this.principal;
      }

      /**
       * <p>
       * Sets the principal to be used in the test case.
       * </p>
       * 
       * @param principal
       *           the {@code Principal} to be set.
       */
      public void setUserPrincipal(Principal principal)
      {
         this.principal = principal;
      }

      /*
       * (non-Javadoc)
       * 
       * @see javax.xml.ws.WebServiceContext#isUserInRole(java.lang.String)
       */
      public boolean isUserInRole(String arg0)
      {
         return false;
      }
   }
}
