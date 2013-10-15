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
package org.picketlink.test.identity.federation.core.wstrust;

import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.DocumentUtil;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.wstrust.SecurityToken;
import org.picketlink.identity.federation.core.wstrust.StandardSecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * <p>
 * Mock {@code SecurityTokenProvider} used in the test scenarios.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SpecialTokenProvider implements SecurityTokenProvider {

    private Map<String, String> properties;

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#initialize(java.util.Map)
     */
    public void initialize(Map<String, String> properties) {
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#cancelToken(org.picketlink.identity.federation.
     * core.wstrust.WSTrustRequestContext)
     */
    public void cancelToken(ProtocolContext protoContext) throws ProcessingException {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#issueToken(org.picketlink.identity.federation.core
     * .wstrust.WSTrustRequestContext)
     */
    public void issueToken(ProtocolContext protoContext) throws ProcessingException {
        WSTrustRequestContext context = (WSTrustRequestContext) protoContext;

        // create a simple sample token using the info from the request.
        String caller = context.getCallerPrincipal() == null ? "anonymous" : context.getCallerPrincipal().getName();
        URI tokenType = context.getRequestSecurityToken().getTokenType();
        if (tokenType == null) {
            try {
                tokenType = new URI("http://www.tokens.org/SpecialToken");
            } catch (URISyntaxException ignore) {
            }
        }

        // we will use DOM to create the token.
        try {
            Document doc = DocumentUtil.createDocument();

            String namespaceURI = "http://www.tokens.org";
            Element root = doc.createElementNS(namespaceURI, "token:SpecialToken");
            Element child = doc.createElementNS(namespaceURI, "token:SpecialTokenValue");
            child.appendChild(doc.createTextNode("Principal:" + caller));
            root.appendChild(child);
            String id = IDGenerator.create("ID_");
            root.setAttributeNS(namespaceURI, "ID", id);
            root.setAttributeNS(namespaceURI, "TokenType", tokenType.toString());
            root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:token", namespaceURI);

            doc.appendChild(root);

            SecurityToken token = new StandardSecurityToken(tokenType.toString(), root, id);
            context.setSecurityToken(token);
        } catch (ConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#renewToken(org.picketlink.identity.federation.core
     * .wstrust.WSTrustRequestContext)
     */
    public void renewToken(ProtocolContext protoContext) throws ProcessingException {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#validateToken(org.picketlink.identity.federation
     * .core.wstrust.WSTrustRequestContext)
     */
    public void validateToken(ProtocolContext protoContext) throws ProcessingException {
    }

    /**
     * <p>
     * Just returns a reference to the properties that have been configured for testing purposes.
     * </p>
     *
     * @return a reference to the properties map.
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#supports(java.lang.String)
     */
    public boolean supports(String namespace) {
        return WSTrustConstants.BASE_NAMESPACE.equals(namespace);
    }

    /**
     * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#tokenType()
     */
    public String tokenType() {
        return WSTrustConstants.BASE_NAMESPACE;
    }

    public QName getSupportedQName() {
        return new QName(tokenType(), "SpecialToken");
    }

    public String family() {
        return SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();
    }
}