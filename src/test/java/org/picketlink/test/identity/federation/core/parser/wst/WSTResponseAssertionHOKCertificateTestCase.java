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
package org.picketlink.test.identity.federation.core.parser.wst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Map;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.KeyIdentifierType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;
import org.w3c.dom.Element;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Nov 11, 2010
 */
public class WSTResponseAssertionHOKCertificateTestCase
{
   @Test
   public void testWST_RSTR_Assertion() throws Exception
   {
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