/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.identity.federation.bindings.authenticators;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.IdentityParticipantStack;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.authenticators.idp.TestIdentityParticipantStack;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Test class for the IDP authenticator {@link IDPWebBrowserSSOValve}.
 * </p>
 * <p>
 * This tests simulates a scenario with the following characteristics: <br/>
 * <ul>
 * <li>Identity Provider is deployed in a host with this address: <code>IDENTITY_PROVIDER_HOST_ADDRESS</code>. The URL is
 * <code>IDENTITY_PROVIDER_URL</code></li>
 * <li>Service Provider is deployed in a host with this address: <code>SERVICE_PROVIDER_HOST_ADDRESS</code>. The URL is
 * <code>SERVICE_PROVIDER_URL</code></li>
 * </ul>
 * </p>
 * 
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class IDPWebBrowserSSOTestCase {

    private static final String CERTIFICATE_ALIAS = "servercert";

    private static final Logger logger = Logger.getLogger(IDPWebBrowserSSOTestCase.class.getName());

    private static final String IDENTITY_PROVIDER_HOST_ADDRESS = "192.168.1.1";
    private static final String SERVICE_PROVIDER_HOST_ADDRESS = "192.168.1.4";
    private static final String IDENTITY_PROVIDER_URL = "http://" + IDENTITY_PROVIDER_HOST_ADDRESS + ":8080/idp-sig/";
    private static final String SERVICE_PROVIDER_URL = "http://" + SERVICE_PROVIDER_HOST_ADDRESS + ":8080/fake-sp";

    private IDPWebBrowserSSOValve identityProvider;

    @Before
    public void onSetup() {
        TestIdentityParticipantStack.reset();
    }
    
    /**
     * <p>
     * Tests the configuration of a custom {@link IdentityParticipantStack}.
     * </p>
     */
    @Test
    public void testRoleGeneratorConfiguration() {
        logger.info("testRoleGeneratorConfiguration");

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());
        
        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();
        
        assertEquals(assertion.getIssuer().getValue(), IDENTITY_PROVIDER_URL);
        
        List<String> expectedRoles = new ArrayList<String>();
        
        expectedRoles.add("test-role1");
        expectedRoles.add("test-role2");
        expectedRoles.add("test-role3");
        
        Set<StatementAbstractType> statements = assertion.getStatements();
        
        for (StatementAbstractType statementType : statements) {
            if (statementType instanceof AttributeStatementType) {
                AttributeStatementType attributeType = (AttributeStatementType) statementType;
                List<ASTChoiceType> attributes = attributeType.getAttributes();
                
                for (ASTChoiceType astChoiceType : attributes) {
                    if (astChoiceType.getAttribute().getName().equals("Role")) {
                        expectedRoles.remove(astChoiceType.getAttribute().getAttributeValue().get(0));
                    }
                }
            }
        }
        
        assertTrue(expectedRoles.isEmpty());
        
        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }
    
    /**
     * <p>
     * Tests the configuration of a custom {@link IdentityParticipantStack}.
     * </p>
     */
    @Test
    public void testIdentityParticipantStackConfiguration() {
        logger.info("testIdentityParticipantStackConfiguration");

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, true);

        IdentityParticipantStack testIdentityParticipantStack = TestIdentityParticipantStack.getDelegate();

        assertEquals("Unexpected total created sessions.", 1, testIdentityParticipantStack.totalSessions());

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());
        assertEquals(responseType.getAssertions().get(0).getAssertion().getIssuer().getValue(), IDENTITY_PROVIDER_URL);

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));

        String currentSessionID = request.getSession().getId();

        // asserts if there is a participant for the current session ID
        assertEquals(1, testIdentityParticipantStack.getParticipants(currentSessionID));

        // asserts if the last participant in the stack is the last caller SP
        assertEquals(SERVICE_PROVIDER_URL, testIdentityParticipantStack.peek(currentSessionID));
    }

    /**
     * <p>
     * Tests the StrictPostBinding configuration.
     * </p>
     * 
     * @throws ProcessingException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    @Test
    public void testStrictPostBindingConfiguration() throws ConfigurationException, ParsingException, ProcessingException {
        logger.info("testStrictPostBindingConfiguration");

        ((IDPType) getAuthenticator().getConfiguration().getIdpOrSP()).setStrictPostBinding(true);

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        StringWriter responseWriter = new StringWriter();

        response.setWriter(new PrintWriter(responseWriter));

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, responseWriter);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());
        AssertionType assertion = responseType.getAssertions().get(0).getAssertion(); 
        assertEquals(assertion.getIssuer().getValue(), IDENTITY_PROVIDER_URL);
        
        ConditionsType conditions = assertion.getConditions();
        assertNotNull(conditions);
        List<ConditionAbstractType> conditionList = conditions.getConditions();
        assertEquals(1, conditionList.size());
        AudienceRestrictionType audience = (AudienceRestrictionType) conditionList.get(0);
        assertEquals(SERVICE_PROVIDER_URL, audience.getAudience().get(0).toString());
    }

    /**
     * <p>
     * Tests if the IDP respond with an ResponseType with a <code>JBossSAMLURIConstants.STATUS_AUTHNFAILED.get()</code> status
     * code. This test sends a request without any signature information. Because the IDP is configured with signatures an error
     * response is expected.
     * </p>
     */
    @Test
    public void testInvalidRequestWithoutSignature() {
        logger.info("testInvalidRequestWithoutSignature");

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, false);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }

    /**
     * <p>
     * Tests if the IDP respond with an ResponseType with a <code>JBossSAMLURIConstants.STATUS_AUTHNFAILED.get()</code> status
     * code. This test sends a {@link AuthnRequestType} with a invalid issuer. The issuer is not in the IDP ValidatingAlias
     * list.
     * </p>
     */
    @Test
    public void testRequestFromInvalidValidatingAlias() {
        logger.info("testRequestFromInvalidValidatingAlias");
        String notTrustedDomain = "123.123.123.123";
        String notTrustedDomainForIssuer = "145.145.145.145";
        String notTrustedServiceProviderURL = SERVICE_PROVIDER_URL.replace(SERVICE_PROVIDER_HOST_ADDRESS, notTrustedDomain);
        String notTrustedIssuerURL = SERVICE_PROVIDER_URL.replace(SERVICE_PROVIDER_HOST_ADDRESS, notTrustedDomainForIssuer);

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(notTrustedDomain, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        // We will use different URL for assertionConsumerServiceURL and for issuerURL to ensure that error response
        // will be redirected to assertionConsumerServiceURL
        sendAuthenticationRequest(request, response, notTrustedIssuerURL, notTrustedServiceProviderURL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(notTrustedServiceProviderURL));
    }

    /**
     * <p>
     * Tests if the IDP respond with an ResponseType with a <code>JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get()</code>
     * status code. This test sends a {@link AuthnRequestType} with a invalid issuer. The issuer is not in the IDP trusted
     * domain list.
     * </p>
     */
    @Test
    public void testRequestFromUntrustedDOmain() {
        logger.info("testRequestFromUntrustedDOmain");
        String notTrustedDomain = "192.168.1.5";
        String notTrustedServiceProviderURL = SERVICE_PROVIDER_URL.replace(SERVICE_PROVIDER_HOST_ADDRESS, notTrustedDomain);

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(notTrustedDomain, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, notTrustedServiceProviderURL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(notTrustedServiceProviderURL));
    }

    /**
     * <p>
     * Tests if the IDP respond with a valid {@link AssertionType} given a valid {@link AuthnRequestType}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleAuthenticationRequest() throws Exception {
        logger.info("testSimpleAuthenticationRequest");
        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());
        assertEquals(responseType.getAssertions().get(0).getAssertion().getIssuer().getValue(), IDENTITY_PROVIDER_URL);

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }

    /**
     * <p>
     * Tests if the IDP respond with a valid {@link AssertionType} given a valid {@link AuthnRequestType}. This test disables
     * signature support on the IDP and try to get an assertion without signatures.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleAuthenticationRequestWithoutSignature() throws Exception {
        logger.info("testSimpleAuthenticationRequest");

        getAuthenticator().getConfiguration().getIdpOrSP().setSupportsSignature(false);

        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, false);

        ResponseType responseType = getResponseType(response, null);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());
        assertEquals(responseType.getAssertions().get(0).getAssertion().getIssuer().getValue(), IDENTITY_PROVIDER_URL);

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }

    /**
     * <p>
     * Tests if the the assertion issued by the IDP has the expected time conditions. This test asserts if the
     * PicketLinkSTS.TokenTimeout attribute is being considered when creating the assertion conditions.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testAssertionTokenTimeoutAndClockSkew() throws Exception {
        logger.info("testAssertionTokenTimeoutAndClockSkew");
        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(SERVICE_PROVIDER_HOST_ADDRESS, true);
        MockCatalinaResponse response = new MockCatalinaResponse();

        sendAuthenticationRequest(request, response, SERVICE_PROVIDER_URL, true);

        ResponseType responseType = getResponseTypeAndCheckSignature(response, null);

        assertNotNull(responseType);
        assertEquals(1, responseType.getAssertions().size());

        AssertionType issuedAssertion = responseType.getAssertions().get(0).getAssertion();

        assertEquals(issuedAssertion.getIssuer().getValue(), IDENTITY_PROVIDER_URL);

        // The response should redirect back to the caller SP
        assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));

        ConditionsType conditions = issuedAssertion.getConditions();

        assertEquals("The assertion timeout is invalid.", 3000, conditions.getNotOnOrAfter().toGregorianCalendar()
                .getTimeInMillis()
                - conditions.getNotBefore().toGregorianCalendar().getTimeInMillis());
    }

    /**
     * <p>
     * Extracts the {@link ResponseType} from the http response. This methos allows to extract the {@link ResponseType} from a
     * {@link StringWriter} or direct from the response.redirectString. If using HTTP Redirect Binding you should pass null. to
     * the writer param.
     * </p>
     * 
     * @param response
     * @param responseWriter if not null, try to get the {@link ResponseType} from the this writer. Otherwise try to get from
     *        the response querystring.
     * @return
     */
    private ResponseType getResponseType(MockCatalinaResponse response, StringWriter responseWriter) {
        ResponseType responseType = null;

        try {
            SAML2Response samlResponse = new SAML2Response();

            if (responseWriter == null) {
                MockCatalinaRequest requestTmp = new MockCatalinaRequest();

                AuthenticatorTestUtils.populateParametersWithQueryString(response.redirectString, requestTmp);

                responseType = (ResponseType) samlResponse.getSAML2ObjectFromStream(RedirectBindingUtil
                        .base64DeflateDecode(requestTmp.getParameter(GeneralConstants.SAML_RESPONSE_KEY)));
            } else {
                Document postBindingForm = DocumentUtil.getDocument(responseWriter.toString());

                logger.info("POST Binding response from the IDP:");
                logger.info(prettyPrintDocument(postBindingForm).toString());

                NodeList nodes = postBindingForm.getElementsByTagName("INPUT");
                Element inputElement = (Element) nodes.item(0);
                String idpResponse = inputElement.getAttributeNode("VALUE").getValue();

                responseType = (ResponseType) samlResponse.getSAML2ObjectFromStream(PostBindingUtil
                        .base64DecodeAsStream(idpResponse));
            }

            Document convert = samlResponse.convert(responseType);

            logger.info("ResponseType returned from the IDP:");
            System.out.println(prettyPrintDocument(convert));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error getting the ResponseType.");
        }

        return responseType;
    }

    private ResponseType getResponseTypeAndCheckSignature(MockCatalinaResponse response, StringWriter responseWriter) {
        ResponseType responseType = getResponseType(response, responseWriter);

        try {
            if (responseWriter == null) {
                assertTrue(RedirectBindingSignatureUtil.validateSignature(response.redirectString, getAuthenticator()
                        .getKeyManager().getPublicKey(CERTIFICATE_ALIAS), RedirectBindingSignatureUtil
                        .getSignatureValueFromSignedURL(response.redirectString)));
            } else {
                assertTrue("No Signature element found.", responseType.getSignature() != null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error checking response signature.");
        }

        return responseType;
    }

    // We use same URL for assertionConsumerServiceURL and for issuer in this case
    private void sendAuthenticationRequest(MockCatalinaRequest request, MockCatalinaResponse response, String issuer,
                                           boolean signToken) {
       sendAuthenticationRequest(request, response, issuer, issuer, signToken);
    }

    /**
     * <p>
     * Sends an authentication request ({@link AuthnRequestType}) to the IDP.
     * </p>
     * 
     * @param request
     * @param response
     * @param issuer
     * @param assertionConsumerURL
     * @param signToken
     */
    private void sendAuthenticationRequest(MockCatalinaRequest request, MockCatalinaResponse response, String issuer,
            String assertionConsumerURL, boolean signToken) {
        try {
            SAML2Request samlRequest = new SAML2Request();

            AuthnRequestType authnRequestType = samlRequest.createAuthnRequestType(IDGenerator.create("ID_"),
                  assertionConsumerURL, getAuthenticator().getConfiguration().getIdpOrSP().getIdentityURL(), issuer);

            Document authnRequestDocument = samlRequest.convert(authnRequestType);

            logger.info("AuthRequestType:" + prettyPrintDocument(authnRequestDocument).toString());

            if (signToken) {
                request.setQueryString(RedirectBindingSignatureUtil.getSAMLRequestURLWithSignature(authnRequestType, null,
                        getAuthenticator().getKeyManager().getSigningKey()));
                AuthenticatorTestUtils.populateParametersWithQueryString(request.getQueryString(), request);
            } else {
                String deflateBase64URLEncode = RedirectBindingUtil.deflateBase64Encode(DocumentUtil.asString(
                        authnRequestDocument).getBytes("UTF-8"));
                request.setQueryString("SAMLRequest=" + deflateBase64URLEncode);
                request.setParameter("SAMLRequest", deflateBase64URLEncode);
            }

            getAuthenticator().invoke(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error sending AuthnRequestType.");
        }
    }

    /**
     * <p>
     * Creates and returns a instance of {@link IDPWebBrowserSSOValve}.
     * </p>
     * 
     * @return
     */
    private IDPWebBrowserSSOValve getAuthenticator() {
        if (this.identityProvider == null) {
            this.identityProvider = AuthenticatorTestUtils.createIdentityProvider("saml2/redirect/idp-sig");
        }

        return this.identityProvider;
    }

    private StringWriter prettyPrintDocument(Document authnRequestDocument) {
        StringWriter writer = new StringWriter();

        try {
            Transformer transformer = TransformerUtil.getTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(DocumentUtil.getXMLSource(authnRequestDocument), new StreamResult(writer));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error printing the document.");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writer;
    }

}