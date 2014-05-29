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
package org.picketlink.identity.federation.core.wstrust.handlers;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.common.ErrorCodes;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

import static javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link STSSaml20Handler}. </p>
 *
 * When running this unit test 'java.endorsed.dirs' must be set. For example, if you are using Eclipse then you can set
 * this in
 * the run configuration as a VM argument: -Djava.endorsed.dirs=${project_loc}/src/test/resources/endorsed
 *
 * This is not required when running the test through maven as this same setting exists in pom.xml.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSSaml20HandlerTestCase {

    private SOAPMessageContext soapMessageContext;

    private SOAPMessage soapMessage;

    private STSClient wsTrustClient;

    private STSSaml20Handler samlHandler;

    @Test
    public void handleMessageValidToken() throws Exception {
        when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(true);

        final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
        addSecurityAssertionElement(securityHeader);

        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(soapMessageContext.getMessage()).thenReturn(soapMessage);

        boolean result = samlHandler.handleMessage(soapMessageContext);
        assertTrue(result);
    }

    @Test
    public void handleMessageInValidToken() throws Exception {
        when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(false);

        final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
        addSecurityAssertionElement(securityHeader);

        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(soapMessageContext.getMessage()).thenReturn(soapMessage);
        try {
            samlHandler.handleMessage(soapMessageContext);
            fail("handleMessage should have thrown an exception");
        } catch (final Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            assertSoapFaultString(e, "The security token could not be authenticated or authorized");
        }
    }

    @Test
    public void handleMessageMissingSecurityToken() throws Exception {
        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(soapMessageContext.getMessage()).thenReturn(soapMessage);
        try {
            samlHandler.handleMessage(soapMessageContext);
            fail("handleMessage should have thrown a exception!");
        } catch (final Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            assertSoapFaultString(e, ErrorCodes.NULL_VALUE + "No security token could be found in the SOAP Header");
        }
    }

    @Test
    public void handleMessageInvalidSecurityToken() throws Exception {
        when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(false);

        final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
        addSecurityAssertionElement(securityHeader);

        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(soapMessageContext.getMessage()).thenReturn(soapMessage);
        try {
            samlHandler.handleMessage(soapMessageContext);
            fail("handleMessage should have thrown a exception!");
        } catch (final Exception e) {
            assertTrue(e instanceof SOAPFaultException);
            assertSoapFaultString(e, "The security token could not be authenticated or authorized");
        }
    }

    @Test
    public void usernamePasswordFromSOAPMessageContext() throws Exception {
        when(wsTrustClient.validateToken((any(Element.class)))).thenReturn(true);

        final SOAPHeaderElement securityHeader = addSecurityHeader(soapMessage.getSOAPHeader());
        addSecurityAssertionElement(securityHeader);

        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(false);
        when(soapMessageContext.getMessage()).thenReturn(soapMessage);

        when(soapMessageContext.get(STSSecurityHandler.USERNAME_MSG_CONTEXT_PROPERTY)).thenReturn("Fletch");
        when(soapMessageContext.get(STSSecurityHandler.PASSWORD_MSG_CONTEXT_PROPERTY)).thenReturn("letmein");

        samlHandler.handleMessage(soapMessageContext);

        assertEquals("Fletch", samlHandler.getConfigBuilder().getUsername());
        assertEquals("letmein", samlHandler.getConfigBuilder().getPassword());
    }

    @Test
    public void handleMessageOutbound() {
        when(soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY)).thenReturn(true);
        assertTrue(new STSSaml20Handler().handleMessage(soapMessageContext));
    }

    @Before
    public void setUp() {
        // Create a Mock for WSTrustClient.
        wsTrustClient = mock(STSClient.class);

        samlHandler = new FakeSamlHandler(wsTrustClient);
        samlHandler.setConfigFile("wstrust/auth/jboss-sts-client.properties");
        // Simulate the WS Engine calling @PostConstruct.
        samlHandler.parseSTSConfig();

        soapMessageContext = mock(SOAPMessageContext.class);

        try {
            soapMessage = SOAPUtil.create();
        } catch (SOAPException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private SOAPHeaderElement addSecurityHeader(final SOAPHeader soapHeader) throws SOAPException {
        final QName securityQName = samlHandler.getSecurityElementQName();
        final SOAPHeaderElement securityHeader = soapHeader.addHeaderElement(new QName(securityQName.getNamespaceURI(),
                securityQName.getLocalPart(), "wsse"));
        soapHeader.addChildElement(securityHeader);
        return securityHeader;
    }

    private SOAPElement addSecurityAssertionElement(final SOAPHeaderElement securityHeader) throws SOAPException {
        final QName tokenElementQName = this.samlHandler.getTokenElementQName();
        final SOAPElement tokenElement = securityHeader.addChildElement(new QName(tokenElementQName.getNamespaceURI(),
                tokenElementQName.getLocalPart(), "saml"));
        return securityHeader.addChildElement(tokenElement);
    }

    private void assertSoapFaultString(final Exception e, final String str) {
        SOAPFaultException soapFaultException = (SOAPFaultException) e;
        SOAPFault fault = soapFaultException.getFault();
        assertEquals(str, fault.getFaultString());
    }

    private class FakeSamlHandler extends STSSaml20Handler {

        private final STSClient stsClient;

        public FakeSamlHandler(final STSClient stsClient) {
            this.stsClient = stsClient;
        }

        @Override
        protected STSClient createSTSClient(STSClientConfig config) throws ParsingException {
            return stsClient;
        }
    }
}
