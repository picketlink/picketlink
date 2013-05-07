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
package org.picketlink.test.trust.jbossws.handler;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.handler.SamlRequestSecurityTokenHandler;
import org.w3c.dom.NodeList;

/**
 * Unit test the {@link SamlRequestSecurityTokenHandler}
 *
 * @author pskopek@redhat.com
 * @since Aug 15, 2012
 */
public class SamlRstHandlerUnitTestCase {
    
    public static final String ADMIN = "test-admin";
    
    @Test
    public void testIn() throws Exception {
        DelegatingHandler handler = new DelegatingHandler();
        MsgHandlerUnitTestCaseMessageContext msgContext = new MsgHandlerUnitTestCaseMessageContext();
        SOAPMessage soapMessage = get();

        msgContext.setMessage(soapMessage);
        handler.handleInbound(msgContext);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if (prefix == null) throw new NullPointerException("Null prefix");
                if (prefix.equals(WSTrustConstants.PREFIX)) {
                    return WSTrustConstants.BASE_NAMESPACE;
                }
                else if (prefix.equals(Constants.WSSE_PREFIX)) {
                    return Constants.WSSE_NS;
                }
                else if (prefix.equals("wsu")) {
                    return WSTrustConstants.WSU_NS;
                }
                else if (prefix.equals("wsa")) {
                    return WSTrustConstants.WSA_NS;
                }
                else if (prefix.equals(SOAPConstants.SOAP_ENV_PREFIX)) {
                    return SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE;
                }
                return null;
            }
            
            public Iterator getPrefixes(String namespaceURI) {
                // not necessary for XPath
                return null;
            }
           
            public String getPrefix(String namespaceURI) {
                return null;
            }
        });
        
        XPathExpression expr = xpath.compile("//env:Envelope/env:Body/wst:RequestSecurityToken/wsse:UsernameToken/wsse:Username/text()");

        NodeList nl = (NodeList) expr.evaluate(soapMessage.getSOAPPart().getDocumentElement(), XPathConstants.NODESET);
        String result = (nl.getLength() == 1 ? nl.item(0).getTextContent() : null);
        assertEquals("Admin user has to be added", ADMIN, result);
    }

    private SOAPMessage get() throws SOAPException, IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ws-trust/samples/wst-rst-with-binary-token.xml");
        SOAPMessage soapMessage = SOAPUtil.getSOAPMessage(is);
        return soapMessage;
    }

    private static class DelegatingHandler extends SamlRequestSecurityTokenHandler {
        @Override
        protected String getUserPrincipalName(MessageContext msgContext) {
            return ADMIN;
        }

        @Override
        protected boolean handleInbound(MessageContext msgContext) {
            return super.handleInbound(msgContext);
        }
        
    }

    private static class MsgHandlerUnitTestCaseMessageContext implements SOAPMessageContext {
        private Map<String, Object> map = new HashMap<String, Object>();

        private Map<String, Scope> scopes = new HashMap<String, MessageContext.Scope>();

        private SOAPMessage msg;

        public int size() {
            return 0;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        public Object get(Object key) {
            return map.get(key);
        }

        public Object put(String key, Object value) {
            return map.put(key, value);
        }

        public Object remove(Object key) {
            return map.remove(key);
        }

        public void putAll(Map<? extends String, ? extends Object> m) {
            map.putAll(m);
        }

        public void clear() {
            map.clear();
        }

        public Set<String> keySet() {
            return map.keySet();
        }

        public Collection<Object> values() {
            return map.values();
        }

        public Set<java.util.Map.Entry<String, Object>> entrySet() {
            return map.entrySet();
        }
        

        public Scope getScope(String arg0) {
            return scopes.get(arg0);
        }

        public void setScope(String arg0, Scope arg1) {
            scopes.put(arg0, arg1);
        }

        public Object[] getHeaders(QName arg0, JAXBContext arg1, boolean arg2) {
            throw new RuntimeException("NYI");
        }

        public SOAPMessage getMessage() {
            return msg;
        }

        public Set<String> getRoles() {
            throw new RuntimeException("NYI");
        }

        public void setMessage(SOAPMessage arg0) {
            msg = arg0;
        }
    }
}