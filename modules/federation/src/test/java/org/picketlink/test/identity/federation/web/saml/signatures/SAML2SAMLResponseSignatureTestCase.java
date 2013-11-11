package org.picketlink.test.identity.federation.web.saml.signatures;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.config.federation.IDPType;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public class SAML2SAMLResponseSignatureTestCase {

    private SAML2SignatureGenerationHandler generationHandler;
    private SAML2SignatureValidationHandler validationHandler;
    private KeyPair keypair;

    @Before
    public void onSetup() throws Exception {
        // create a temporary RSA key pair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        this.keypair = kpg.genKeyPair();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        IDPType idpType = new IDPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
        chainOptions.put(GeneralConstants.KEYPAIR, keypair);
        chainConfig.set(chainOptions);

        this.generationHandler = new SAML2SignatureGenerationHandler();

        // Initialize the generationHandler
        this.generationHandler.initChainConfig(chainConfig);
        this.generationHandler.initHandlerConfig(handlerConfig);

        this.validationHandler = new SAML2SignatureValidationHandler();

        // Initialize the validationHandler
        this.validationHandler.initChainConfig(chainConfig);
        this.validationHandler.initHandlerConfig(handlerConfig);
    }

    @Test
    public void testSignatureWithHttpPostBinding() throws Exception {
        String assertingPartyUrl = "http://post-assertingparty.com";
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder(assertingPartyUrl);
        ResponseType responseType = new SAML2Response().createResponseType(
                "response",
                issuerInfo,
                AssertionUtil.createAssertion("assertion", issuerInfo.getIssuer()));

        DefaultSAML2HandlerRequest handlerRequest = createHandlerRequest("POST", assertingPartyUrl, responseType);
        DefaultSAML2HandlerResponse handlerResponse = createHandlerResponse("POST", responseType);

        this.generationHandler.handleRequestType(handlerRequest, handlerResponse);

        Document resultingDocument = handlerResponse.getResultingDocument();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[local-name()='Signature']");

        NodeList signatureElement = (NodeList) expr.evaluate(resultingDocument, XPathConstants.NODESET);

        assertNotNull(signatureElement);
        assertEquals(1, signatureElement.getLength());

        assertEquals(resultingDocument.getDocumentElement(), signatureElement.item(0).getParentNode());

        DefaultSAML2HandlerRequest handlerValidationRequest = createHandlerRequest(
                "POST",
                assertingPartyUrl,
                new SAML2Response().getResponseType(DocumentUtil.getNodeAsStream(resultingDocument)));

        handlerValidationRequest.addOption(GeneralConstants.SENDER_PUBLIC_KEY, this.keypair.getPublic());

        DefaultSAML2HandlerResponse handlerValidationResponse = createHandlerResponse("POST", responseType);

        this.validationHandler.handleStatusResponseType(handlerValidationRequest, handlerValidationResponse);
    }

    @Test
    public void testSignatureWithHttpRedirectBinding() throws Exception {
        String assertingPartyUrl = "http://post-assertingparty.com";
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder(assertingPartyUrl);
        ResponseType responseType = new SAML2Response().createResponseType(
                "response",
                issuerInfo,
                AssertionUtil.createAssertion("assertion", issuerInfo.getIssuer()));

        DefaultSAML2HandlerRequest handlerRequest = createHandlerRequest("GET", assertingPartyUrl, responseType);
        DefaultSAML2HandlerResponse handlerResponse = createHandlerResponse("GET", responseType);

        this.generationHandler.handleRequestType(handlerRequest, handlerResponse);

        Document resultingDocument = handlerResponse.getResultingDocument();

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*[local-name()='Signature']");

        Node signatureElement = (Node) expr.evaluate(resultingDocument, XPathConstants.NODE);

        assertNotNull(signatureElement);

        String queryStringWithSignature = handlerResponse.getDestinationQueryStringWithSignature();

        assertNotNull(queryStringWithSignature);
        assertTrue(queryStringWithSignature.contains("&" + GeneralConstants.SAML_SIG_ALG_REQUEST_KEY + "="));
        assertTrue(queryStringWithSignature.contains("&" + GeneralConstants.SAML_SIGNATURE_REQUEST_KEY + "="));

        DefaultSAML2HandlerRequest handlerValidationRequest = createHandlerRequest("GET", assertingPartyUrl, responseType);

        handlerValidationRequest.addOption(GeneralConstants.SENDER_PUBLIC_KEY, this.keypair.getPublic());

        HTTPContext httpContext = (HTTPContext) handlerValidationRequest.getContext();
        MockHttpServletRequest mockRequest = (MockHttpServletRequest) httpContext.getRequest();

        mockRequest.setQueryString(queryStringWithSignature);

        DefaultSAML2HandlerResponse handlerValidationResponse = createHandlerResponse("GET", responseType);

        this.validationHandler.handleStatusResponseType(handlerValidationRequest, handlerValidationResponse);
    }

    private DefaultSAML2HandlerResponse createHandlerResponse(final String httpMethod, final ResponseType responseType) throws Exception {
        DefaultSAML2HandlerResponse handlerResponse = new DefaultSAML2HandlerResponse();

        handlerResponse.setResultingDocument(new SAML2Request().convert(responseType));
        handlerResponse.setPostBindingForResponse(httpMethod.equals("POST"));

        return handlerResponse;
    }

    private DefaultSAML2HandlerRequest createHandlerRequest(String httpMethod, final String relayingPartyUrl, final ResponseType responseType) throws Exception {
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder(relayingPartyUrl);
        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(responseType, new SAML2Request().convert(responseType));

        return new DefaultSAML2HandlerRequest(createProtocolContext(httpMethod), issuerInfo.getIssuer(), docHolder, SAML2Handler.HANDLER_TYPE.IDP);
    }

    private ProtocolContext createProtocolContext(final String httpMethod) {
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, httpMethod);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        return new HTTPContext(servletRequest, servletResponse, servletContext);
    }
}
