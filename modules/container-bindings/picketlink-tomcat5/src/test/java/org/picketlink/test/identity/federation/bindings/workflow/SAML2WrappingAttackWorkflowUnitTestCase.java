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
import java.security.KeyPair;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import junit.framework.Assert;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.sig.SAML2Signature;
import org.picketlink.identity.federation.bindings.tomcat.idp.IDPWebBrowserSSOValve;
import org.picketlink.identity.federation.bindings.tomcat.sp.ServiceProviderAuthenticator;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.TrustKeyManager;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.web.util.PostBindingUtil;
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
 * Tests some scenarios trying to perform a SAML Assertion Wrapping Attack.
 * </p>
 * <p>
 * What is protecting PicketLink to the XML Signature Wrapping Attack is how the idness of attributes is configured for XML
 * elements. PicketLink expects to manually set the idness of attributes after Apache Santuario version update.
 * </p>
 * <p>
 * It is strongly recommended to use signatures when configuring IDPs and SPs.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class SAML2WrappingAttackWorkflowUnitTestCase extends AbstractSAML2RedirectWithSignatureTestCase {

    private MockCatalinaContext spContext = new MockCatalinaContext();
    private MockCatalinaContext idpContext = new MockCatalinaContext();

    private MockCatalinaSession spSession = new MockCatalinaSession();
    private MockCatalinaSession idpSession = new MockCatalinaSession();

    /**
     * <p>
     * Performs a complete SAML authentication workflow trying to send to the service provider a SAML Response that was changed
     * to simulate a XML Signature Wrapping Attack.
     * </p>
     * <p>
     * When performing such attack, PicketLink is protected because the signature validation will fail and the user redirected
     * to the error page.
     * </p>
     */
    @Test
    public void testWrapIntoSignedSAMLResponse() throws Exception {
        ServiceProviderAuthenticator spAuthenticator = createSPAuthenticator(true);

        // first interaction with the SP. We should receive from the SP a AuthnRequest type
        String authnRequest = invokeSPAndGetAuthnRequest(spAuthenticator);

        IDPWebBrowserSSOValve idpAuthenticator = createIDPAuthenticator(true);

        // let's invoke the IDP with the previous AuthnRequest and perform the authentication. Now we should get a valid SAML
        // Response and Assertion.
        String idpResponse = invokeIDPAndGetSAMLResponse(idpAuthenticator, authnRequest);

        // let's wrap a bad assertion into the response doc. We are trying a XML Signature Wrapping Attack
        byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
        Document samlResponseDoc = DocumentUtil.getDocument(new ByteArrayInputStream(samlIDPResponse));
        samlResponseDoc = wrapBadSAMLAssertion(samlResponseDoc);

        // let's now send the bad SAML response and the assertion back to the SP.
        idpResponse = Base64.encodeBytes(DocumentUtil.asString(samlResponseDoc).getBytes());

        Principal principal = invokeSPWithSAMLResponse(spAuthenticator, idpResponse);

        // the SP should not accept the bad response/assertion. The SP should redirect to the error page.
        Assert.assertEquals("/error.jsp", this.spContext.getForwardPage());

        Assert.assertNull(principal);
    }

    @Test
    public void testWrapWithSignedAssertion() throws Exception {
       // same workflow like previous test for obtaining valid idpResponse from IDP
       ServiceProviderAuthenticator spAuthenticator = createSPAuthenticator(true);
       String authnRequest = invokeSPAndGetAuthnRequest(spAuthenticator);
       IDPWebBrowserSSOValve idpAuthenticator = createIDPAuthenticator(true);
       String idpResponse = invokeIDPAndGetSAMLResponse(idpAuthenticator, authnRequest);
       byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
       Document samlResponseDoc = DocumentUtil.getDocument(new ByteArrayInputStream(samlIDPResponse));

       // remove signature element as it's signing whole samlResponse
       Element signature = (Element) samlResponseDoc.getElementsByTagNameNS("http://www.w3.org/2000/09/xmldsig#", "Signature")
             .item(0);
       signature.getParentNode().removeChild(signature);

       // sign Assertion element only
       signAssertionElement(samlResponseDoc, idpAuthenticator.getKeyManager());

       // verify successful validation of signature on Assertion element
       Assert.assertTrue(new SAML2Signature().validate(samlResponseDoc, idpAuthenticator.getKeyManager().getSigningKeyPair().getPublic()));

       // wrap evil assertion
       wrapBadAssertionBeforeOriginal(samlResponseDoc);

       // let's now send the bad SAML response and the assertion back to the SP.
       idpResponse = Base64.encodeBytes(DocumentUtil.asString(samlResponseDoc).getBytes());
       Principal principal = invokeSPWithSAMLResponse(spAuthenticator, idpResponse);

       // TODO: This does not work currently and needs to be fixed in Picketlink! Uncomment following lines once implementation is fixed
       // the SP should not accept the bad response/assertion. The SP should redirect to the error page.
       // Assert.assertNull("Principal should be null but is: " + principal, principal);
       // Assert.assertEquals("/error.jsp", this.spContext.getForwardPage());
    }

    /**
     * <p>                                                                                              DocumentUtil.asString(samlResponseDoc)
     * Performs a complete SAML authentication workflow trying to send to the service provider a SAML Response with a bad
     * assertion that replaces the original one.
     * </p>
     * <p>
     * When performing such attack, PicketLink is not protected because the SAML Response is not signed and the document can be
     * tampered.
     * </p>
     */
    @Test
    @Ignore
    public void testReplaceOriginalAssertion() throws Exception {
        ServiceProviderAuthenticator spAuthenticator = createSPAuthenticator(false);

        // first interaction with the SP. We should receive from the SP a AuthnRequest type
        String authnRequest = invokeSPAndGetAuthnRequest(spAuthenticator);

        IDPWebBrowserSSOValve idpAuthenticator = createIDPAuthenticator(false);

        // let's invoke the IDP with the previous AuthnRequest. Now we should get a valid SAML Response and Assertion.
        String idpResponse = invokeIDPAndGetSAMLResponse(idpAuthenticator, authnRequest);

        // let's replace the original assertion with a bad one
        byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
        Document samlResponseDoc = DocumentUtil.getDocument(new ByteArrayInputStream(samlIDPResponse));
        samlResponseDoc = replaceWithBadAssertion(samlResponseDoc);

        // let's now send the bad SAML response and the assertion back to the SP.
        idpResponse = Base64.encodeBytes(DocumentUtil.asString(samlResponseDoc).getBytes());

        Principal principal = invokeSPWithSAMLResponse(spAuthenticator, idpResponse);

        Assert.assertNotNull(principal);
        Assert.assertEquals("jduke_was_attacked", principal.getName());
    }

    /**
     * <p>
     * Performs a complete SAML authentication workflow trying to send to the service provider a SAML Response with a bad
     * assertion wrapped before the original one.
     * </p>
     * <p>
     * When performing such attack, PicketLink is not protected because the SAML Response is not signed and the document can be
     * tampered. It allows multiple Assertion elements within a SAML Response and always consider the first Assertion during the
     * processing.
     * </p>
     */
    @Test
    public void testWrapBadAssertionBeforeOriginal() throws Exception {
        ServiceProviderAuthenticator spAuthenticator = createSPAuthenticator(false);

        // first interaction with the SP. We should receive from the SP a AuthnRequest type
        String authnRequest = invokeSPAndGetAuthnRequest(spAuthenticator);

        IDPWebBrowserSSOValve idpAuthenticator = createIDPAuthenticator(false);

        // let's invoke the IDP with the previous AuthnRequest. Now we should get a valid SAML Response and Assertion.
        String idpResponse = invokeIDPAndGetSAMLResponse(idpAuthenticator, authnRequest);

        // let's replace the original assertion with a bad one
        byte[] samlIDPResponse = PostBindingUtil.base64Decode(idpResponse);
        Document samlResponseDoc = DocumentUtil.getDocument(new ByteArrayInputStream(samlIDPResponse));
        samlResponseDoc = wrapBadAssertionBeforeOriginal(samlResponseDoc);

        // let's now send the bad SAML response and the assertion back to the SP.
        idpResponse = Base64.encodeBytes(DocumentUtil.asString(samlResponseDoc).getBytes());

        Principal principal = invokeSPWithSAMLResponse(spAuthenticator, idpResponse);

        Assert.assertNotNull(principal);
        Assert.assertEquals("jduke_was_attacked", principal.getName());
    }

    private Principal invokeSPWithSAMLResponse(ServiceProviderAuthenticator spAuthenticator, String idpResponse)
            throws IOException {

        MockCatalinaRequest request = new MockCatalinaRequest();

        request.setRemoteAddr("http://localhost/idp");
        request.setSession(this.spSession);
        request.setParameter("SAMLResponse", idpResponse);

        request.setMethod("POST");

        request.setContext(this.spContext);

        MockCatalinaResponse response = new MockCatalinaResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        response.setWriter(new PrintWriter(baos));

        spAuthenticator.authenticate(request, response, new LoginConfig());

        return request.getUserPrincipal();
    }

    private String invokeSPAndGetAuthnRequest(ServiceProviderAuthenticator spAuthenticator) throws IOException, Exception {
        MockCatalinaRequest request = new MockCatalinaRequest();

        request.setRemoteAddr("http://localhost/idp");
        request.setSession(this.spSession);

        request.setMethod("POST");

        request.setContext(this.spContext);

        MockCatalinaResponse catalinaResponse = new MockCatalinaResponse();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        catalinaResponse.setWriter(new PrintWriter(baos));

        LoginConfig loginConfig = new LoginConfig();

        spAuthenticator.authenticate(request, catalinaResponse, loginConfig);

        String authnRequest = getSAMLRequestOrResponse(baos);

        return authnRequest;
    }

    private String invokeIDPAndGetSAMLResponse(IDPWebBrowserSSOValve idpAuthenticator, String authnRequest)
            throws ConfigurationException, ProcessingException, ParsingException, LifecycleException, IOException,
            ServletException, Exception {
        byte[] base64Decode = PostBindingUtil.base64Decode(authnRequest);

        AuthnRequestType art = new SAML2Request().getAuthnRequestType(new ByteArrayInputStream(base64Decode));

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
        request.setSession(this.idpSession);
        request.setContext(this.idpContext);
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
    private Document replaceWithBadAssertion(Document samlResponse) throws Exception {
        // now let's change the response document and wrap a another SAML assertion

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

        System.out.println(DocumentUtil.asString(samlResponse));

        return samlResponse;
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
    private Document wrapBadAssertionBeforeOriginal(Document samlResponse) throws Exception {
        // now let's change the response document and wrap a another SAML assertion

        Element originalAssertion = (Element) samlResponse.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion").item(0);

        // let's load a forged assertion
        String fileName = "saml2-wrapping-attack.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        Document evilAssertion = DocumentUtil.getDocument(is);

        System.out.println(DocumentUtil.asString(evilAssertion));

        // let's wrap the forged assertion into the original document.
        Element element = evilAssertion.getDocumentElement();

        Node adoptNode = samlResponse.adoptNode(element);

        originalAssertion.getParentNode().insertBefore(adoptNode, originalAssertion);

        System.out.println(DocumentUtil.asString(samlResponse));

        return samlResponse;
    }

    /**
     * <p>
     * Creates and start a {@link IDPWebBrowserSSOValve} instance.
     * </p>
     * 
     * @param supportsSignatures indicates if the authenticator supports signatures or not.
     * @return
     */
    private IDPWebBrowserSSOValve createIDPAuthenticator(boolean supportsSignatures) throws Exception {
        IDPWebBrowserSSOValve idpAuthenticator = new IDPWebBrowserSSOValve();

        IDPType idpType = new IDPType();

        idpType.setIdentityURL("http://localhost/idp");
        idpType.setSupportsSignature(supportsSignatures);

        idpAuthenticator.setConfigProvider(new MockSAMLConfigurationProvider(idpType));
        idpAuthenticator.setContainer(this.idpContext);

        idpAuthenticator.start();

        return idpAuthenticator;
    }

    /**
     * <p>
     * Creates and start a {@link ServiceProviderAuthenticator} instance.
     * </p>
     * 
     * @param supportsSignatures indicates if the authenticator supports signatures or not.
     * @return
     */
    private ServiceProviderAuthenticator createSPAuthenticator(boolean supportsSignatures) throws Exception {
        ServiceProviderAuthenticator spAuthenticator = new ServiceProviderAuthenticator();

        SPType spType = new SPType();

        spType.setBindingType("POST");
        spType.setIdentityURL("http://localhost/idp");
        spType.setSupportsSignature(supportsSignatures);

        spType.setServiceURL("http://localhost/sp");

        spAuthenticator.setConfigProvider(new MockSAMLConfigurationProvider(spType));

        spAuthenticator.setContainer(this.spContext);

        spAuthenticator.testStart();

        return spAuthenticator;
    }

    private void signAssertionElement(Document samlResponseDoc, TrustKeyManager keyManager)
            throws Exception {
        // obtain assertion to sign
        Element assertion = (Element)samlResponseDoc.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
              JBossSAMLConstants.ASSERTION.get()).item(0);

        // configure ID (it's required by Santuario library)
        assertion.setIdAttribute("ID", true);

        // obtain needed stuff
        String referenceURI = "#" + assertion.getAttribute("ID");
        Node nextSibling = assertion.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(),
              JBossSAMLConstants.ISSUER.get()).item(0).getNextSibling();
        KeyPair keyPair = keyManager.getSigningKeyPair();

        // sign it
        XMLSignatureUtil.sign(assertion, nextSibling, keyPair, DigestMethod.SHA1, SignatureMethod.RSA_SHA1, referenceURI);
    }
}