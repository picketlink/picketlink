/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.identity.federation.web.saml.handlers;

import org.junit.Test;
import org.picketlink.common.constants.GeneralConstants;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.ProviderType;
import org.picketlink.config.federation.SPType;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.X500SAMLProfileConstants;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.impl.DefaultSAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChainConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerConfig;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.web.core.HTTPContext;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AttributeHandler;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletRequest;
import org.picketlink.test.identity.federation.web.mock.MockHttpServletResponse;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test the {@code SAML2AttributeHandler}
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2AttributeHandlerUnitTestCase {

    private static String name = "anil";

    private static String email = "anil@test";

    @SuppressWarnings("unchecked")
    @Test
    public void testAttributes() throws Exception {
        SAML2AttributeHandler handler = new SAML2AttributeHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        IDPType idpType = new IDPType();
        idpType.setAttributeManager(TestAttributeManager.class.getName());
        chainOptions.put(GeneralConstants.CONFIGURATION, idpType);
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        SAML2Object saml2Object = new SAML2Object() {
        };

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.IDP);
        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal() {
            public String getName() {
                return name;
            }
        });
        handler.handleRequestType(request, response);

        Map<String, Object> attribs = (Map<String, Object>) session.getAttribute(GeneralConstants.ATTRIBUTES);
        assertNotNull("Attributes are not null", attribs);
        assertEquals(email, attribs.get(X500SAMLProfileConstants.EMAIL.getFriendlyName()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAttribsOnSP() throws Exception {
        SAML2AttributeHandler handler = new SAML2AttributeHandler();

        SAML2HandlerChainConfig chainConfig = new DefaultSAML2HandlerChainConfig();
        SAML2HandlerConfig handlerConfig = new DefaultSAML2HandlerConfig();

        Map<String, Object> chainOptions = new HashMap<String, Object>();
        ProviderType spType = new SPType();
        chainOptions.put(GeneralConstants.CONFIGURATION, spType);
        chainConfig.set(chainOptions);

        // Initialize the handler
        handler.initChainConfig(chainConfig);
        handler.initHandlerConfig(handlerConfig);

        // Create a Protocol Context
        MockHttpSession session = new MockHttpSession();
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(session, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        HTTPContext httpContext = new HTTPContext(servletRequest, servletResponse, servletContext);

        ResponseType saml2Object = new SAML2Response().createResponseType("fake_id");

        SAMLDocumentHolder docHolder = new SAMLDocumentHolder(saml2Object, null);
        IssuerInfoHolder issuerInfo = new IssuerInfoHolder("http://localhost:8080/idp/");
        SAML2HandlerRequest request = new DefaultSAML2HandlerRequest(httpContext, issuerInfo.getIssuer(), docHolder,
                SAML2Handler.HANDLER_TYPE.IDP);
        SAML2HandlerResponse response = new DefaultSAML2HandlerResponse();

        AssertionType assertion = new AssertionType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());

        Map<String, Object> myattr = new HashMap<String, Object>();
        myattr.put("testKey", "hello");
        AttributeStatementType attState = StatementUtil.createAttributeStatement(myattr);
        assertion.addStatement(attState);

        request.addOption(GeneralConstants.ASSERTION, assertion);
        handler.handleStatusResponseType(request, response);

        Map<String, List<Object>> sessionMap = (Map<String, List<Object>>) session
                .getAttribute(GeneralConstants.SESSION_ATTRIBUTE_MAP);
        assertNotNull(sessionMap);
        List<Object> values = sessionMap.get("testKey");
        assertEquals("hello", values.get(0));
    }

    public static class TestAttributeManager implements AttributeManager {

        public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
            Map<String, Object> attribs = new HashMap<String, Object>();

            if (name.equals(userPrincipal.getName())) {
                attribs.put(X500SAMLProfileConstants.EMAIL.getFriendlyName(), email);
            }
            return attribs;
        }
    }
}