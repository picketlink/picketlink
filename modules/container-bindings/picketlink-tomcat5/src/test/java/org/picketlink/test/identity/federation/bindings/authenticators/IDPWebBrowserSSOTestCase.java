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

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.util.RedirectBindingSignatureUtil;
import org.picketlink.identity.federation.web.util.RedirectBindingUtil;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.w3c.dom.Document;

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

    private static final Logger logger = Logger.getLogger(IDPWebBrowserSSOTestCase.class.getName());

    private static final String IDENTITY_PROVIDER_HOST_ADDRESS = "192.168.1.1";
    private static final String SERVICE_PROVIDER_HOST_ADDRESS = "192.168.1.4";
    private static final String IDENTITY_PROVIDER_URL = "http://" + IDENTITY_PROVIDER_HOST_ADDRESS + ":8080/idp-sig/";
    private static final String SERVICE_PROVIDER_URL = "http://" + SERVICE_PROVIDER_HOST_ADDRESS + ":8080/fake-sp";

    private IDPWebBrowserSSOValve identityProvider;
    
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

        ResponseType responseType = getResponseType(response);

        Assert.assertNotNull(responseType);
        Assert.assertEquals(JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        Assert.assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }

    /**
     * <p>
     * Tests if the IDP respond with an ResponseType with a <code>JBossSAMLURIConstants.STATUS_AUTHNFAILED.get()</code> status
     * code. This test sends a {@link AuthnRequestType} with a invalid issuer. The issuer is not in the IDP ValidatingAlias list. 
     * </p>
     */
    @Test
    public void testRequestFromInvalidValidatingAlias() {
        logger.info("testRequestFromInvalidValidatingAlias");
        String notTrustedDomain = "123.123.123.123";
        String notTrustedServiceProviderURL = SERVICE_PROVIDER_URL.replace(SERVICE_PROVIDER_HOST_ADDRESS, notTrustedDomain);
        
        MockCatalinaRequest request = AuthenticatorTestUtils.createRequest(notTrustedDomain, true);
        MockCatalinaResponse response = new MockCatalinaResponse();
        
        sendAuthenticationRequest(request, response, notTrustedServiceProviderURL, true);

        ResponseType responseType = getResponseType(response);

        Assert.assertNotNull(responseType);
        Assert.assertEquals(JBossSAMLURIConstants.STATUS_AUTHNFAILED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        Assert.assertTrue("Expected a redirect to the SP.", response.redirectString.contains(notTrustedServiceProviderURL));
    }

    /**
     * <p>
     * Tests if the IDP respond with an ResponseType with a <code>JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get()</code> status
     * code. This test sends a {@link AuthnRequestType} with a invalid issuer. The issuer is not in the IDP trusted domain list. 
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

        ResponseType responseType = getResponseType(response);

        Assert.assertNotNull(responseType);
        Assert.assertEquals(JBossSAMLURIConstants.STATUS_REQUEST_DENIED.get(), responseType.getStatus().getStatusCode().getValue()
                .toString());

        // The response should redirect back to the caller SP
        Assert.assertTrue("Expected a redirect to the SP.", response.redirectString.contains(notTrustedServiceProviderURL));
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

        ResponseType responseType = getResponseType(response);

        Assert.assertNotNull(responseType);
        Assert.assertEquals(1, responseType.getAssertions().size());
        Assert.assertEquals(responseType.getAssertions().get(0).getAssertion().getIssuer().getValue(), IDENTITY_PROVIDER_URL);
        
        // The response should redirect back to the caller SP
        Assert.assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
    }
    
    /**
     * <p>
     * Tests if the the assertion issued by the IDP has the expected time conditions. This test asserts if the PicketLinkSTS.TokenTimeout
     * attribute is being considered when creating the assertion conditions.
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

        ResponseType responseType = getResponseType(response);

        Assert.assertNotNull(responseType);
        Assert.assertEquals(1, responseType.getAssertions().size());
        
        AssertionType issuedAssertion = responseType.getAssertions().get(0).getAssertion();
        
        Assert.assertEquals(issuedAssertion.getIssuer().getValue(), IDENTITY_PROVIDER_URL);
        
        // The response should redirect back to the caller SP
        Assert.assertTrue("Expected a redirect to the SP.", response.redirectString.contains(SERVICE_PROVIDER_URL));
        
        ConditionsType conditions = issuedAssertion.getConditions();
        
        Assert.assertEquals("The assertion timeout is invalid.", 3000, conditions.getNotOnOrAfter().toGregorianCalendar().getTimeInMillis() - conditions.getNotBefore().toGregorianCalendar().getTimeInMillis());
    }
    
    /**
     * <p>
     * Extracts the ${@link ResponseType} from the http response.
     * </p>
     * 
     * @param response
     * @return
     */
    private ResponseType getResponseType(MockCatalinaResponse response) {
        ResponseType responseType = null;

        try {
            SAML2Response samlResponse = new SAML2Response();

            MockCatalinaRequest requestTmp = new MockCatalinaRequest();

            AuthenticatorTestUtils.populateParametersWithQueryString(response.redirectString, requestTmp);

            responseType = (ResponseType) samlResponse.getSAML2ObjectFromStream(RedirectBindingUtil
                    .base64DeflateDecode(requestTmp.getParameter(GeneralConstants.SAML_RESPONSE_KEY)));

            Document convert = samlResponse.convert(responseType);

            System.out.println(prettyPrintDocument(convert));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error getting the ResponseType.");
        }

        return responseType;
    }

    /**
     * <p>
     * Sends an authentication request ({@link AuthnRequestType}) to the IDP.
     * </p>
     * 
     * @param request
     * @param response
     * @param issuer
     * @param signToken
     */
    private void sendAuthenticationRequest(MockCatalinaRequest request, MockCatalinaResponse response, String issuer,
            boolean signToken) {
        try {
            SAML2Request samlRequest = new SAML2Request();

            AuthnRequestType authnRequestType = samlRequest.createAuthnRequestType(IDGenerator.create("ID_"),
                    SERVICE_PROVIDER_URL, getAuthenticator().getConfiguration().getIdpOrSP().getIdentityURL(), issuer);

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
            Assert.fail("Error sending AuthnRequestType.");
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

    private StringWriter prettyPrintDocument(Document authnRequestDocument)  {
        StringWriter writer = new StringWriter();
        
        try {
            Transformer transformer = TransformerUtil.getTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            transformer.transform(DocumentUtil.getXMLSource(authnRequestDocument), new StreamResult(writer));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error printing the document.");
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