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
package org.picketlink.test.identity.federation.core.saml.v2;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.X500SAMLProfileConstants;
import org.picketlink.identity.federation.core.saml.v2.factories.JBossSAMLAuthnResponseFactory;
import org.picketlink.identity.federation.core.saml.v2.holders.IDPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.IssuerInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.holders.SPInfoHolder;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit test the X500 Profile of SAML2
 * @author Anil.Saldhana@redhat.com
 * @since Sep 14, 2009
 */
public class X500AttributeUnitTestCase extends TestCase
{
   public void testX500Marshalling() throws Exception
   {
      Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put(X500SAMLProfileConstants.EMAIL_ADDRESS.getFriendlyName(), "test@a");
      attributes.put(X500SAMLProfileConstants.GIVEN_NAME.getFriendlyName(), "anil");

      AttributeStatementType attrStat = StatementUtil.createAttributeStatement(attributes);

      IssuerInfoHolder issuerHolder = new IssuerInfoHolder("http://idp");
      issuerHolder.setStatusCode(JBossSAMLURIConstants.STATUS_SUCCESS.get());

      IDPInfoHolder idp = new IDPInfoHolder();
      idp.setNameIDFormatValue(IDGenerator.create());

      ResponseType rt = JBossSAMLAuthnResponseFactory.createResponseType("response111", new SPInfoHolder(), idp,
            issuerHolder);
      assertNotNull(rt);

      AssertionType assertion = rt.getAssertions().get(0).getAssertion();
      assertion.addStatement(attrStat);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(rt);

      Document samlDom = DocumentUtil.getDocument(new String(baos.toByteArray()));

      NodeList nl = samlDom.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "Attribute");
      assertEquals("nodes = 2", 2, nl.getLength());

      String x500NS = JBossSAMLURIConstants.X500_NSURI.get();
      String encodingLocalName = "Encoding";

      Element attrib = (Element) nl.item(0);
      assertTrue("Has ldap encoding?", attrib.hasAttributeNS(x500NS, encodingLocalName));
      assertEquals("LDAP", attrib.getAttributeNodeNS(x500NS, encodingLocalName).getNodeValue());

      NodeList nla = attrib.getElementsByTagNameNS(JBossSAMLURIConstants.ASSERTION_NSURI.get(), "AttributeValue");

      Node attribNode = nla.item(0);
      String nodeValue = attribNode.getTextContent();
      assertTrue(nodeValue.equals("test@a") || nodeValue.equals("anil"));
   }
}