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
package org.picketlink.test.identity.federation.web.saml.handlers;

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.constants.SAMLAuthenticationContextClass;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.exceptions.fed.AssertionExpiredException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.constants.AttributeConstants;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest.GENERATE_REQUEST_TYPE;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.AssertionUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.picketlink.identity.federation.core.util.XMLEncryptionUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.AudienceRestrictionType;
import org.picketlink.identity.federation.saml.v2.assertion.AuthnStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType.STSubType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnContextComparisonType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestedAuthnContextType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.identity.federation.web.core.SessionManager;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;
import org.w3c.dom.Document;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyPair;
import java.security.Principal;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picketlink.identity.federation.core.saml.v2.util.StatementUtil.createAttributeStatement;

/**
 * Unit test the {@link SAML2AuthenticationHandler}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 17, 2011
 */
public class SAML2AuthenticationHandlerUnitTestCase {

    @Test
    public void handleNameIDCustomization() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.generateSAMLRequest(request, response);

        Document samlReq = response.getResultingDocument();
        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertEquals(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get(), nameIDPolicy.getFormat().toString());
    }

    @Ignore
    @Test
    public void handleEncryptedAssertion() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = createServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        KeyPair keypair = KeyStoreUtil.generateKeyPair("RSA");

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        String assertionNS = JBossSAMLURIConstants.ASSERTION_NSURI.get();

        QName assertionQName = new QName(assertionNS, "EncryptedAssertion", "saml");
        Document responseDoc = saml2Response.convert(responseType);

        byte[] secret = WSTrustUtil.createRandomSecret(128 / 8);
        SecretKey secretKey = new SecretKeySpec(secret, "AES");

        PublicKey publicKey = keypair.getPublic();
        XMLEncryptionUtil.encryptElement(new QName(assertionNS, "Assertion", "saml"), responseDoc, publicKey, secretKey, 128,
                assertionQName, true);

        SAMLParser parser = new SAMLParser();
        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        request.addOption(GeneralConstants.DECRYPTING_KEY, keypair.getPrivate());

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal() {
            public String getName() {
                return "Hi";
            }
        });

        handler.handleStatusResponseType(request, response);
    }

    @Test
    public void testRoleAttributeMultipleValues() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(SAML2Handler.USE_MULTI_VALUED_ROLES, "true");


        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = createServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.generateSAMLRequest(request, response);

        Document samlReq = response.getResultingDocument();
        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertEquals(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get(), nameIDPolicy.getFormat().toString());

        ProviderType idpType = new IDPType();
        chainOptions = new HashMap<String, Object>();
        chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        IdentityServer identityServer = new IdentityServer();
        servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);

        //Add roles to session to be picked up by the handler
        List<String> roles = new ArrayList<String>();
        roles.add("role1");
        roles.add("role2");
        session.setAttribute(GeneralConstants.ROLES_ID, roles);

        httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);
        docHolder = new SAMLDocumentHolder(authnRequest, null);
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.IDP);

        PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
        sts.installDefaultConfiguration(null);

        handler.handleRequestType(request, response);
        samlReq = response.getResultingDocument();
        parser = new SAMLParser();
        ResponseType responseType = (ResponseType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();
        assertNotNull(assertion);

        Set<StatementAbstractType> statements = assertion.getStatements();
        Iterator<StatementAbstractType> iter = statements.iterator();
        boolean processedAttributeStatement = false;
        while (iter.hasNext()) {
            StatementAbstractType statement = iter.next();
            if (statement instanceof AuthnStatementType) {
                continue;
            }
            if (statement instanceof AttributeStatementType) {
                AttributeStatementType attributeStatementType = (AttributeStatementType) statement;
                assertNotNull(attributeStatementType);
                assertEquals(1, attributeStatementType.getAttributes().size());
                AttributeType attributeType = attributeStatementType.getAttributes().get(0).getAttribute();
                assertEquals(AttributeConstants.ROLE_IDENTIFIER_ASSERTION, attributeType.getName());
                List<Object> values = attributeType.getAttributeValue();
                assertEquals(2, values.size()); //2 Roles
                processedAttributeStatement = true;
            }
        }

        assertTrue(processedAttributeStatement);
    }

    @Test
    public void testSingleAttributeStatement() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(SAML2AuthenticationHandler.SINGLE_ATTRIBUTE_STATEMENT, true);

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = createServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.generateSAMLRequest(request, response);

        Document samlReq = response.getResultingDocument();
        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertEquals(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get(), nameIDPolicy.getFormat().toString());

        ProviderType idpType = new IDPType();
        chainOptions = new HashMap<String, Object>();
        chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        IdentityServer identityServer = new IdentityServer();
        servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, identityServer);

        //Add roles to session to be picked up by the handler
        List<String> roles = new ArrayList<String>();
        roles.add("role1");
        roles.add("role2");
        session.setAttribute(GeneralConstants.ROLES_ID, roles);

        httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);
        docHolder = new SAMLDocumentHolder(authnRequest, null);
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.IDP);

        Set<AttributeStatementType> attributeStatementTypes = new HashSet<AttributeStatementType>();

        attributeStatementTypes.add(createAttributeStatement("attribute1", "attributeValue1"));
        attributeStatementTypes.add(createAttributeStatement("attribute2", "attributeValue2"));

        request.addOption(GeneralConstants.ATTRIBUTES, attributeStatementTypes);

        PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
        sts.installDefaultConfiguration(null);

        handler.handleRequestType(request, response);
        samlReq = response.getResultingDocument();
        parser = new SAMLParser();
        ResponseType responseType = (ResponseType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();
        assertNotNull(assertion);

        Set<StatementAbstractType> statements = assertion.getStatements();
        Iterator<StatementAbstractType> iter = statements.iterator();
        int countAttributeStatement = 0;
        AttributeStatementType attributeStatementType = null;

        while (iter.hasNext()) {
            StatementAbstractType statement = iter.next();

            if (statement instanceof AuthnStatementType) {
                continue;
            }

            if (statement instanceof AttributeStatementType) {
                countAttributeStatement++;
                attributeStatementType = (AttributeStatementType) statement;
            }
        }

        assertEquals(1, countAttributeStatement);
        assertNotNull(attributeStatementType);

        Map<String, Object> attributes = new HashMap<String, Object>();

        for (AttributeStatementType.ASTChoiceType attribute : attributeStatementType.getAttributes()) {
            attributes.put(attribute.getAttribute().getName(), attribute.getAttribute().getAttributeValue());
        }

        assertTrue(attributes.containsKey("Role"));
        assertTrue(attributes.containsKey("attribute1"));
        assertTrue(attributes.containsKey("attribute2"));
    }

    @Test
    public void testPublishAssertionInHttpSession() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();

        spType.setServiceURL("http://sp.com");

        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        Document responseDoc = saml2Response.convert(responseType);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal() {
            public String getName() {
                return "Hi";
            }
        });

        handler.handleStatusResponseType(request, response);

        assertNotNull(session.getAttribute("org.picketlink.sp.SAML_ASSERTION"));
    }

    @Test
    public void testNullNotBeforeNotOnOrAfterCondition() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();

        spType.setServiceURL("http://sp.com");

        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);
        assertion.setConditions(new ConditionsType());

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        Document responseDoc = saml2Response.convert(responseType);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        handler.handleStatusResponseType(request, response);

        ConditionsType conditions = new ConditionsType();

        conditions.setNotBefore(XMLTimeUtil.getIssueInstant());

        assertion.setConditions(conditions);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseDoc = saml2Response.convert(responseType);

        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        response = new DefaultSAML2HandlerResponse();

        handler.handleStatusResponseType(request, response);

        conditions = new ConditionsType();

        conditions.setNotBefore(XMLTimeUtil.add(XMLTimeUtil.getIssueInstant(), 10000));

        assertion.setConditions(conditions);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseDoc = saml2Response.convert(responseType);

        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        response = new DefaultSAML2HandlerResponse();

        try {
            handler.handleStatusResponseType(request, response);
            fail();
        } catch (ProcessingException e) {
            assertTrue(AssertionExpiredException.class.isInstance(e.getCause()));
        }

        conditions = new ConditionsType();

        conditions.setNotOnOrAfter(XMLTimeUtil.add(XMLTimeUtil.getIssueInstant(), -10000));

        assertion.setConditions(conditions);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseDoc = saml2Response.convert(responseType);

        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        response = new DefaultSAML2HandlerResponse();

        try {
            handler.handleStatusResponseType(request, response);
            fail();
        } catch (ProcessingException e) {
            assertTrue(AssertionExpiredException.class.isInstance(e.getCause()));
        }
    }

    @Test
    public void testAssertionAudience() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();
        spType.setServiceURL("http://sales.com");
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);
        ConditionsType conditions = new ConditionsType();
        AudienceRestrictionType audienceRestrictionType = new AudienceRestrictionType();

        audienceRestrictionType.addAudience(URI.create(spType.getServiceURL()));

        conditions.addCondition(audienceRestrictionType);

        assertion.setConditions(conditions);

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        Document responseDoc = saml2Response.convert(responseType);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        handler.handleStatusResponseType(request, response);

        conditions = new ConditionsType();

        audienceRestrictionType = new AudienceRestrictionType();

        audienceRestrictionType.addAudience(URI.create("http://employee.com"));

        conditions.addCondition(audienceRestrictionType);

        assertion.setConditions(conditions);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseDoc = saml2Response.convert(responseType);

        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        response = new DefaultSAML2HandlerResponse();

        try {
            handler.handleStatusResponseType(request, response);
            fail();
        } catch (ProcessingException e) {
            assertTrue(e.getMessage().contains("Wrong audience"));
        }
    }

    @Test
    public void testAssertionAudienceWithEntityID() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();
        spType.setEntityId("urn:samltest:picketlink-wildfly8");
        spType.setServiceURL("http://sales.com");
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);
        ConditionsType conditions = new ConditionsType();
        AudienceRestrictionType audienceRestrictionType = new AudienceRestrictionType();

        audienceRestrictionType.addAudience(URI.create(spType.getEntityId()));

        conditions.addCondition(audienceRestrictionType);

        assertion.setConditions(conditions);

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        Document responseDoc = saml2Response.convert(responseType);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        handler.handleStatusResponseType(request, response);

        conditions = new ConditionsType();

        audienceRestrictionType = new AudienceRestrictionType();

        audienceRestrictionType.addAudience(URI.create("urn:samltest:picketlink-eap"));

        conditions.addCondition(audienceRestrictionType);

        assertion.setConditions(conditions);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseDoc = saml2Response.convert(responseType);

        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        response = new DefaultSAML2HandlerResponse();

        try {
            handler.handleStatusResponseType(request, response);
            fail();
        } catch (ProcessingException e) {
            assertTrue(e.getMessage().contains("Wrong audience"));
        }
    }

    @Test
    public void testDestinationValidation() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();
        spType.setServiceURL("http://sales.com");
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Response saml2Response = new SAML2Response();
        IssuerInfoHolder issuerInfoholder = new IssuerInfoHolder("testIssuer");

        AssertionType assertion = AssertionUtil.createAssertion(IDGenerator.create("ID_"), new NameIDType());
        SubjectType assertionSubject = new SubjectType();
        STSubType subType = new STSubType();
        NameIDType anil = new NameIDType();
        anil.setValue("anil");
        subType.addBaseID(anil);
        assertionSubject.setSubType(subType);
        assertion.setSubject(assertionSubject);

        ResponseType responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseType.setDestination("http://sales.com");

        Document responseDoc = saml2Response.convert(responseType);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        handler.handleStatusResponseType(request, response);

        responseType = saml2Response.createResponseType(IDGenerator.create("ID_"), issuerInfoholder, assertion);

        responseType.setDestination("http://employee.com");

        responseDoc = saml2Response.convert(responseType);

        parser = new SAMLParser();
        saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        response = new DefaultSAML2HandlerResponse();

        try {
            handler.handleStatusResponseType(request, response);
            fail();
        } catch (ProcessingException e) {
            assertTrue(e.getMessage().contains("Invalid destination"));
        }
    }

    @Test
    public void testResponseWithNamespacesInRootElementOnly() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        handlerConfig.addParameter(GeneralConstants.ASSERTION_SESSION_ATTRIBUTE_NAME, "org.picketlink.sp.SAML_ASSERTION");

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        SPType spType = new SPType();

        spType.setServiceURL("http://localhost:8080/sales-post-sig");

        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream("parser/saml2/saml2-response-namespace-root-.xml");
        Document responseDoc = DocumentUtil.getDocument(is);

        SAMLParser parser = new SAMLParser();
        SAML2Object saml2Object = (SAML2Object) parser.parse(DocumentUtil.getNodeAsStream(responseDoc));

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, responseDoc);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal() {
            public String getName() {
                return "Hi";
            }
        });

        handler.handleStatusResponseType(request, response);

        assertNotNull(session.getAttribute("org.picketlink.sp.SAML_ASSERTION"));
    }

    private MockServletContext createServletContext() {
        MockServletContext mockServletContext = new MockServletContext();

        new SessionManager(mockServletContext, new SessionManager.InitializationCallback() {
            @Override
            public void registerSessionListener(Class<? extends HttpSessionListener> listener) {

            }
        });

        return mockServletContext;
    }

    @Test
    public void handleRequestedAuthnContextCustomization() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        String contextClasses = "password,X509, internetProtocol";
        handlerConfig.addParameter(GeneralConstants.AUTHN_CONTEXT_CLASSES, contextClasses);
        handlerConfig.addParameter(GeneralConstants.REQUESTED_AUTHN_CONTEXT_COMPARISON, AuthnContextComparisonType.MINIMUM.value());

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = createServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.generateSAMLRequest(request, response);

        Document samlReq = response.getResultingDocument();

        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        RequestedAuthnContextType requestedAuthnContextType = authnRequest.getRequestedAuthnContext();

        assertNotNull(requestedAuthnContextType.getAuthnContextClassRef());
        assertFalse(requestedAuthnContextType.getAuthnContextClassRef().isEmpty());

        for (String aliasClasses : contextClasses.split(",")) {
            SAMLAuthenticationContextClass contextClass = SAMLAuthenticationContextClass.forAlias(aliasClasses);
            if (!requestedAuthnContextType.getAuthnContextClassRef().contains(contextClass.getFqn())) {
                fail("Expected authentication context class not found.");
            }
        }

        assertEquals(AuthnContextComparisonType.MINIMUM, requestedAuthnContextType.getComparison());
    }

    @Test
    public void handleAssertionCustomization() throws Exception {
        SAML2AuthenticationHandler handler = new SAML2AuthenticationHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();
        handlerConfig.addParameter(GeneralConstants.NAMEID_FORMAT, JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainOptions.put(GeneralConstants.ROLE_VALIDATOR_IGNORE, "true");
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockServletContext servletContext = createServletContext();
        MockHttpSession session = new MockHttpSession();

        session.setServletContext(servletContext);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");

        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
            SAML2Handler.HANDLER_TYPE.SP);
        request.setTypeOfRequestToBeGenerated(GENERATE_REQUEST_TYPE.AUTH);

        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();
        handler.generateSAMLRequest(request, response);

        Document samlReq = response.getResultingDocument();
        SAMLParser parser = new SAMLParser();
        AuthnRequestType authnRequest = (AuthnRequestType) parser.parse(DocumentUtil.getNodeAsStream(samlReq));
        NameIDPolicyType nameIDPolicy = authnRequest.getNameIDPolicy();
        assertEquals(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get(), nameIDPolicy.getFormat().toString());

        request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), new SAMLDocumentHolder(authnRequest),
            SAML2Handler.HANDLER_TYPE.IDP);

        handler = new CustomSAML2Authenticationhandler();

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        PicketLinkCoreSTS.instance().installDefaultConfiguration();

        servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, new IdentityServer());

        handler.handleRequestType(request, response);

        Document resultingDocument = response.getResultingDocument();

        assertNotNull(resultingDocument);

        ResponseType responseType = (ResponseType) parser.parse(DocumentUtil.getNodeAsStream(resultingDocument));
        AssertionType assertion = responseType.getAssertions().get(0).getAssertion();

        assertNotNull(assertion);

        SubjectType subject = assertion.getSubject();
        STSubType subType = subject.getSubType();
        NameIDType nameIDType = (NameIDType) subType.getBaseID();

        assertEquals("changedNamedId", nameIDType.getValue());
    }

    private class CustomSAML2Authenticationhandler extends SAML2AuthenticationHandler {

        @Override
        protected void onAssertionCreated(SAML2HandlerRequest request, AssertionType assertion) {
            SubjectType subject = assertion.getSubject();
            STSubType subType = subject.getSubType();
            NameIDType nameIDType = (NameIDType) subType.getBaseID();

            nameIDType.setValue("changedNamedId");
        }

        @Override
        public HANDLER_TYPE getType() {
            return HANDLER_TYPE.IDP;
        }
    }
}