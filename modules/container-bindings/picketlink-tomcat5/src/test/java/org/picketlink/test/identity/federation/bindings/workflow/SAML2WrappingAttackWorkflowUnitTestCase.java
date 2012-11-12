/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.bindings.workflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.ServletException;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.ServiceProviderAuthenticator;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
import org.picketlink.test.identity.federation.bindings.authenticators.AuthenticatorTestUtils;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaContext;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRealm;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaRequest;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaResponse;
import org.picketlink.test.identity.federation.bindings.mock.MockCatalinaSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * Tests if the XML Signature Wrapping Attack happens when performing a complete SAML POST Binding authentication workflow.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class SAML2WrappingAttackWorkflowUnitTestCase extends AbstractSAML2RedirectWithSignatureTestCase {

    /**
     * <p>
     * Performs a complete SAML authentication worklow trying to send to the service provider a SAML Response that was changed
     * to simulate a XML Signature Wrapping Attack.
     * </p>
     */
    @Test
    public void testAssertionWrappingAttackProtection() throws Exception {
        ServiceProviderAuthenticator spAuthenticator = createSPAuthenticator();

        // first interaction with the SP. We should receive from the SP a AuthnRequest type
        String authnRequest = invokeSPAndGetAuthnRequest(spAuthenticator);

        // let's invoke the IDP with the previous AuthnRequest. Now we should get a valid SAML Response and Assertion.
        String idpResponse = invokeIDPAndGetSAMLResponse(authnRequest);

        // let's wrap a bad assertion into the response doc. We are trying a XML Signature Wrapping Attack
        byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
        Document samlResponseDoc = DocumentUtil.getDocument(new ByteArrayInputStream(samlIDPResponse));
        samlResponseDoc = wrapBadSAMLAssertion(samlResponseDoc);

        // let's now send the bad SAML response and the assertion back to the SP.
        idpResponse = Base64.encodeBytes(DocumentUtil.asString(samlResponseDoc).getBytes());

        String forwardPage = invokeSPWithSAMLResponse(spAuthenticator, idpResponse);
        
        // the SP should not accept the bad response/assertion. The SP should redirect to the error page.
        Assert.assertEquals("/error.jsp", forwardPage);
    }

    private String invokeSPWithSAMLResponse(ServiceProviderAuthenticator spAuthenticator, String idpResponse)
            throws IOException {
        
        MockCatalinaRequest request = new MockCatalinaRequest();
        
        request.setRemoteAddr("http://localhost/idp");
        request.setSession(new MockCatalinaSession());
        request.setParameter("SAMLResponse", idpResponse);
        
        MockCatalinaRealm realm2 = new MockCatalinaRealm("anil", "test", new Principal() {
            public String getName() {
                return "anil";
            }
        });

        request.setUserPrincipal(new GenericPrincipal(realm2, "anil", "test"));
        
        request.setMethod("POST");
        
        MockCatalinaContext servletContext = new MockCatalinaContext();
        
        request.setContext(servletContext);

        MockCatalinaResponse response = new MockCatalinaResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.setWriter(new PrintWriter(baos));
        
        LoginConfig loginConfig2 = new LoginConfig();
        
        spAuthenticator.authenticate(request, response, loginConfig2);

        return servletContext.getForwardPage();
    }
    
    private String invokeSPAndGetAuthnRequest(ServiceProviderAuthenticator spAuthenticator) throws IOException, Exception {
        MockCatalinaRequest catalinaRequest = AuthenticatorTestUtils.createRequest("localhost", false);

        MockCatalinaResponse catalinaResponse = new MockCatalinaResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        catalinaResponse.setWriter(new PrintWriter(baos));

        LoginConfig loginConfig = new LoginConfig();

        spAuthenticator.authenticate(catalinaRequest, catalinaResponse, loginConfig);

        String authnRequest = getSAMLRequestOrResponse(baos);
        return authnRequest;
    }

    private String invokeIDPAndGetSAMLResponse(String authnRequest) throws ConfigurationException, ProcessingException,
            ParsingException, LifecycleException, IOException, ServletException, Exception {
        byte[] base64Decode = PostBindingUtil.base64Decode(authnRequest);

        AuthnRequestType art = new SAML2Request().getAuthnRequestType(new ByteArrayInputStream(base64Decode));

        IDPWebBrowserSSOValve idpAuthenticator = createIDPAuthenticator();

        // now let's send the previous AuthnRequest to the IDP and authenticate an user. The IDP should return a valid and
        // signed SAML Response.
        MockCatalinaResponse response = new MockCatalinaResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.setWriter(new PrintWriter(baos));

        String samlAuth = DocumentUtil.getDocumentAsString(new SAML2Request().convert(art));

        String samlMessage = Base64.encodeBytes(samlAuth.getBytes());

        MockCatalinaRealm realm = new MockCatalinaRealm("anil", "test", new Principal() {
            public String getName() {
                return "anil";
            }
        });

        MockCatalinaRequest request = new MockCatalinaRequest();

        request.setRemoteAddr("http://localhost/sp");
        request.setSession(new MockCatalinaSession());
        request.setParameter("SAMLRequest", samlMessage);
        request.setUserPrincipal(new GenericPrincipal(realm, "anil", "test"));
        request.setMethod("POST");

        idpAuthenticator.invoke(request, response);

        String idpResponse = getSAMLRequestOrResponse(baos);
        return idpResponse;
    }

    private String getSAMLRequestOrResponse(ByteArrayOutputStream baos) throws Exception {
        String spResponse = new String(baos.toByteArray());
        Document spHTMLResponse = DocumentUtil.getDocument(spResponse);
        NodeList nodes = spHTMLResponse.getElementsByTagName("INPUT");
        Element inputElement = (Element) nodes.item(0);
        return inputElement.getAttributeNode("VALUE").getValue();
    }

    /**
     * <p>
     * Changes the provided SAML Response document to wrap a bad SAML assertion.
     * </p>
     * 
     * @param samlIDPResponse
     * @return
     * @throws ConfigurationException
     * @throws ProcessingException
     * @throws ParsingException
     */
    private Document wrapBadSAMLAssertion(Document samlResponse) throws ConfigurationException, ProcessingException,
            ParsingException {
        // now let's change the response document and wrap a another SAML assertion

        // clone the whole document. The root element is the Response
        Document clonedResponse = (Document) samlResponse.cloneNode(true);

        // let's remove the Signature from the cloned response
        Element signature = (Element) clonedResponse.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature")
                .item(0);

        signature.getParentNode().removeChild(signature);

        // let's remove the original assertion. Later it will be replaced by a another one.
        Element originalAssertion = (Element) samlResponse.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion").item(0);

        originalAssertion.getParentNode().removeChild(originalAssertion);

        // let's load a forged assertion
        String fileName = "saml2-wrapping-attack.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        Document evilAssertion = DocumentUtil.getDocument(is);

        System.out.println(DocumentUtil.asString(evilAssertion));

        // let's wrap the forged assertion into the original document.
        Element element = evilAssertion.getDocumentElement();

        Node adoptNode = samlResponse.adoptNode(element);

        samlResponse.getDocumentElement().appendChild(adoptNode);

        // let's append the cloned response document as a child of the original Signature element
        Element signatureOriginal = (Element) samlResponse.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#",
                "Signature").item(0);

        Element importedClonedResponse = (Element) signatureOriginal.getOwnerDocument().adoptNode(
                clonedResponse.getDocumentElement());

        signatureOriginal.appendChild(importedClonedResponse);

        // let's change the original response ID attribute value
        samlResponse.getDocumentElement().setAttribute("ID", "evilAssertion");

        System.out.println(DocumentUtil.asString(samlResponse));

        return samlResponse;
    }

    private IDPWebBrowserSSOValve createIDPAuthenticator() throws LifecycleException {
        IDPWebBrowserSSOValve idpAuthenticator = new IDPWebBrowserSSOValve();

        IDPType idpType = new IDPType();

        idpType.setIdentityURL("http://localhost/idp");
        idpType.setSupportsSignature(true);

        idpAuthenticator.setConfigProvider(new MockSAMLConfiguratoinProvider(idpType));
        idpAuthenticator.setContainer(new MockCatalinaContext());

        idpAuthenticator.start();
        return idpAuthenticator;
    }

    private ServiceProviderAuthenticator createSPAuthenticator() throws LifecycleException {
        ServiceProviderAuthenticator spAuthenticator = new ServiceProviderAuthenticator();

        SPType spType = new SPType();

        spType.setBindingType("POST");
        spType.setIdentityURL("http://localhost/idp");
        spType.setSupportsSignature(true);

        spType.setServiceURL("http://localhost/sp");

        spAuthenticator.setConfigProvider(new MockSAMLConfiguratoinProvider(spType));
        
        MockCatalinaContext servletContext = new MockCatalinaContext();
        
        spAuthenticator.setContainer(servletContext);

        spAuthenticator.testStart();
        return spAuthenticator;
    }
}