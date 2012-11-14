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

package org.picketlink.test.identity.federation.web.saml.handlers;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.ProviderType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.factories.SAML2HandlerChainFactory;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler.HANDLER_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.TransformerUtil;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2EncryptionHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;
import org.picketlink.identity.federation.web.roles.DefaultRoleValidator;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class SAML2EncryptionHandlerUnitTestCase {

    private static final String SERVICE_PROVIDER_URL = "http://service-provider.picketlink.org";
    private static final String IDENTITY_PROVIDER_URL = "http://identity-provider.picketlink.org";
    
    private MockServletContext servletContext = new MockServletContext();
    private KeyPair keyPair;

    @Before
    public void onSetup() throws NoSuchAlgorithmException {
        // install the STS default configurations
        PicketLinkCoreSTS.instance().installDefaultConfiguration();
        
        // register a identity server into the ServletContext
        servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, new IdentityServer());
        
        // creates a random and temporary keypair for encryption and signing
        this.keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
    }

    /**
     * <p>
     * Try to issue an encrypted and signed SAML Assertion and to process it by the SP.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testEncryptAssertion() throws Exception {
        Document assertionDocument = issueSAMLAssertion();
        
        assertNotNull(assertionDocument);

        System.out.println(prettyPrintDocument(assertionDocument).getBuffer().toString());

        processSAMLAssertion(assertionDocument);
    }

    /**
     * <p>
     * This method asks the IDP for a new encrypted and signed SAML Assertion by sending an AuthnRequest.
     * </p>
     * 
     * @return
     * @throws ConfigurationException
     * @throws ProcessingException
     */
    private Document issueSAMLAssertion() throws ConfigurationException, ProcessingException {
        NameIDType issuerNameID = new NameIDType();

        issuerNameID.setValue(IDENTITY_PROVIDER_URL);

        SAML2Request samlRequest = new SAML2Request();

        AuthnRequestType authnRequestType = samlRequest.createAuthnRequestType("AuthnRequest_FAKE_ID",
                SERVICE_PROVIDER_URL, SERVICE_PROVIDER_URL,
                SERVICE_PROVIDER_URL);

        DefaultSAML2HandlerRequest handlerAuthnRequest = new DefaultSAML2HandlerRequest(new HTTPContext(
                new MockHttpServletRequest(new MockHttpSession(), "POST"), new MockHttpServletResponse(), servletContext),
                issuerNameID, new SAMLDocumentHolder(authnRequestType), HANDLER_TYPE.IDP);

        handlerAuthnRequest.addOption(GeneralConstants.SENDER_PUBLIC_KEY, getKeyPair().getPublic());
        
        DefaultSAML2HandlerResponse handlerAuthnResponse = new DefaultSAML2HandlerResponse();

        try {
            for (SAML2Handler handler : getIDPHandlerChain().handlers()) {
                handler.handleRequestType(handlerAuthnRequest, handlerAuthnResponse);
            }
        } catch (Exception e) { 
            e.printStackTrace();
            fail("Error while issuing encrypted and signed SAML Assertion.");
        }

        return handlerAuthnResponse.getResultingDocument();
    }

    /**
     * <p>
     * Given the SAML Assertion, process it by the SP and make sure it can be decrypted and have its signature validated.
     * </p>
     * 
     * @param assertionDocument
     * @throws ParsingException
     * @throws ConfigurationException
     * @throws ProcessingException
     * @throws NoSuchAlgorithmException
     */
    private void processSAMLAssertion(Document assertionDocument) throws ParsingException, ConfigurationException,
            ProcessingException, NoSuchAlgorithmException {
        NameIDType issuerSPNameID = new NameIDType();

        issuerSPNameID.setValue(IDENTITY_PROVIDER_URL);

        DefaultSAML2HandlerRequest handlerAssertionResponseRequest = new DefaultSAML2HandlerRequest(new HTTPContext(
                new MockHttpServletRequest(new MockHttpSession(), "POST"), new MockHttpServletResponse(), servletContext),
                issuerSPNameID, new SAMLDocumentHolder(new SAML2Response().getSAML2ObjectFromStream(DocumentUtil
                        .getNodeAsStream(assertionDocument)), assertionDocument), HANDLER_TYPE.SP);

        handlerAssertionResponseRequest.addOption(GeneralConstants.DECRYPTING_KEY, getKeyPair().getPrivate());
        handlerAssertionResponseRequest.addOption(GeneralConstants.SENDER_PUBLIC_KEY, getKeyPair().getPublic());

        DefaultSAML2HandlerResponse handlerAssertionRequestResponse = new DefaultSAML2HandlerResponse();

        try {
            for (SAML2Handler handler : getSPHandlerChain().handlers()) {
                handler.handleStatusResponseType(handlerAssertionResponseRequest, handlerAssertionRequestResponse);
            }
        } catch (Exception e) { 
            e.printStackTrace();
            fail("Error while processing the encrypted and signed SAML Assertion.");
        }

    }

    private SAML2HandlerChainConfig createHandlerChainConfig(ProviderType configuration) throws NoSuchAlgorithmException {
        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();

        chainOptions.put(GeneralConstants.CONFIGURATION, configuration);
        chainOptions.put(GeneralConstants.KEYPAIR, getKeyPair());
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR, new DefaultRoleValidator());

        chainConfig.set(chainOptions);

        return chainConfig;
    }

    /**
     * <p>
     * Creates an return a random RSA {@link KeyPair} for testing.
     * </p>
     * 
     * @return
     * @throws NoSuchAlgorithmException
     */
    private KeyPair getKeyPair() {
        return this.keyPair;
    }

    private SAML2HandlerChain getIDPHandlerChain() throws ConfigurationException {
        List<SAML2Handler> handlers = new ArrayList<SAML2Handler>();

        handlers.add(createAuthenticationHandler());
        handlers.add(createEncryptionHandler());

        IDPType idpType = new IDPType();

        idpType.setEncrypt(true);

        return getHandlerChain(idpType, handlers);
    }

    private SAML2HandlerChain getSPHandlerChain() throws ConfigurationException {
        List<SAML2Handler> handlers = new ArrayList<SAML2Handler>();

        handlers.add(createSignatureValidationHandler());
        handlers.add(createAuthenticationHandler());

        return getHandlerChain(new SPType(), handlers);
    }

    private SAML2HandlerChain getHandlerChain(ProviderType configuration, List<SAML2Handler> handlers) {
        SAML2HandlerChain handlerChain = null;

        try {
            SAML2HandlerChainConfig handlerChainConfig = createHandlerChainConfig(configuration);

            handlerChain = SAML2HandlerChainFactory.createChain();

            for (SAML2Handler saml2Handler : handlers) {
                saml2Handler.initChainConfig(handlerChainConfig);
                handlerChain.add(saml2Handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Handler chain configuration error.");
        }

        return handlerChain;
    }

    private SAML2EncryptionHandler createEncryptionHandler() throws ConfigurationException {
        SAML2EncryptionHandler handler = new SAML2EncryptionHandler();

        DefaultSAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        
        handler.initHandlerConfig(handlerConfig);

        return handler;
    }

    private SAML2SignatureValidationHandler createSignatureValidationHandler() throws ConfigurationException {
        SAML2SignatureValidationHandler handler = new SAML2SignatureValidationHandler();

        handler.initHandlerConfig(new DefaultSAML2HandlerConfig());

        return handler;
    }

    private SAML2AuthenticationHandler createAuthenticationHandler() throws ConfigurationException {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        handler.initHandlerConfig(new DefaultSAML2HandlerConfig());

        return handler;
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
