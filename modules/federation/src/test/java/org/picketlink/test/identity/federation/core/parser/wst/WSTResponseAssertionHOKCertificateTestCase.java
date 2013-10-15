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
package org.picketlink.test.identity.federation.core.parser.wst;

import org.junit.Test;
import org.picketlink.common.constants.WSTrustConstants;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Nov 11, 2010
 */
public class WSTResponseAssertionHOKCertificateTestCase {

    @Test
    public void testWST_RSTR_Assertion() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-response-assertion-hok-certificate.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityTokenResponseCollection coll = (RequestSecurityTokenResponseCollection) parser.parse(configStream);
        assertEquals(1, coll.getRequestSecurityTokenResponses().size());

        RequestSecurityTokenResponse rstr = coll.getRequestSecurityTokenResponses().get(0);

        assertEquals("testcontext", rstr.getContext());
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, rstr.getTokenType().toASCIIString());

        assertEquals(XMLTimeUtil.parse("2010-11-11T16:34:19.602Z"), rstr.getLifetime().getCreated());
        assertEquals(XMLTimeUtil.parse("2010-11-11T18:34:19.602Z"), rstr.getLifetime().getExpires());

        EndpointReferenceType endpoint = (EndpointReferenceType) rstr.getAppliesTo().getAny().get(0);
        assertEquals("http://services.testcorp.org/provider2", endpoint.getAddress().getValue());

        assertEquals(128, rstr.getKeySize());
        assertEquals(WSTrustConstants.KEY_TYPE_PUBLIC, rstr.getKeyType().toASCIIString());

        Element assertionElement = (Element) rstr.getRequestedSecurityToken().getAny().get(0);
        String id = assertionElement.getAttribute("ID");

        assertEquals("ID_5a15fc70-daa1-4808-b70e-9cbf6b8e4d4f", id);

        RequestedReferenceType ref = rstr.getRequestedAttachedReference();
        SecurityTokenReferenceType secRef = ref.getSecurityTokenReference();
        assertNotNull(secRef);
        Map<QName, String> map = secRef.getOtherAttributes();
        QName wsseTokenType = new QName(WSTrustConstants.WSSE11_NS, WSTrustConstants.TOKEN_TYPE,
                WSTrustConstants.WSSE.PREFIX_11);
        assertEquals(SAMLUtil.SAML2_TOKEN_TYPE, map.get(wsseTokenType));

        KeyIdentifierType keyId = (KeyIdentifierType) secRef.getAny().get(0);
        assertEquals("#ID_5a15fc70-daa1-4808-b70e-9cbf6b8e4d4f", keyId.getValue());
        assertEquals(WSTrustConstants.WSSE.KEY_IDENTIFIER_VALUETYPE_SAML, keyId.getValueType());
    }
}