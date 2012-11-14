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
package org.picketlink.test.identity.federation.core.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.KeyStoreUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.xmlsec.w3.xmldsig.DSAKeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit test the {@link XMLSignatureUtil}
 *
 * @author Anil Saldhana
 * @since Feb 24, 2012
 */
public class XMLSignatureUtilUnitTestCase {
    @Test
    public void testSigningWSTRequestCollection() throws Exception {
        String fileName = "signatures/wstRequestCollection.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");

        Document rstrDocument = DocumentUtil.getDocument(is);
        assertNotNull(rstrDocument);

        Node rst = rstrDocument.getElementsByTagNameNS(WSTrustConstants.BASE_NAMESPACE, "RequestedSecurityToken").item(0);
        Element tokenElement = null;
        NodeList childNodes = rst.getChildNodes();

        int len = childNodes.getLength();
        for (int i = 0; i < len; i++) {
            Node theNode = childNodes.item(i);
            if (theNode instanceof Element) {
                tokenElement = (Element) theNode;
                tokenElement.setIdAttribute("AssertionID", true);
                break;
            }
        }

        String signatureMethod = SignatureMethod.RSA_SHA1;
        KeyPair keyPair = KeyStoreUtil.generateKeyPair("RSA");

        rstrDocument = XMLSignatureUtil.sign(rstrDocument, tokenElement, keyPair, DigestMethod.SHA1, signatureMethod, "#"
                + tokenElement.getAttribute("AssertionID"));

        assertNotNull(rstrDocument);

        Logger.getLogger(XMLSignatureUtilUnitTestCase.class).debug(DocumentUtil.asString(rstrDocument));

        assertTrue(XMLSignatureUtil.validate(rstrDocument, keyPair.getPublic()));
    }

    @Test
    public void testSAML2Assertion() throws Exception {
        String fileName = "signatures/saml11assertion.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");

        Document rstrDocument = DocumentUtil.getDocument(is);
        assertNotNull(rstrDocument);

        String signatureMethod = SignatureMethod.RSA_SHA1;
        KeyPair keyPair = KeyStoreUtil.generateKeyPair("RSA");

        Element tokenElement = (Element) rstrDocument.getFirstChild();
        rstrDocument = XMLSignatureUtil.sign(rstrDocument, tokenElement, keyPair, DigestMethod.SHA1, signatureMethod, "");

        assertNotNull(rstrDocument);

        Logger.getLogger(XMLSignatureUtilUnitTestCase.class).debug(DocumentUtil.asString(rstrDocument));

        assertTrue(XMLSignatureUtil.validate(rstrDocument, keyPair.getPublic()));
    }
    
    @Test
    public void testDSAKeyValueParsing() throws Exception {
        String fileName = "signatures/dsakeyvalue.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");

        Document doc = DocumentUtil.getDocument(is);
        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());
        
        Element dsaEl = (Element) doc.getElementsByTagName("ds:DSAKeyValue").item(0);
        assertNotNull(dsaEl);
        
        DSAKeyValueType dsa = XMLSignatureUtil.getDSAKeyValue(dsaEl);
        assertNotNull(dsa);
        assertNotNull(dsa.getP());
        assertNotNull(dsa.getQ());
        assertNotNull(dsa.getG());
        assertNotNull(dsa.getY());
        
        System.out.println(dsa);

        DSAPublicKey publicKey = dsa.convertToPublicKey();
        assertNotNull(publicKey);
    }
    
    @Test
    public void testRSAKeyValueParsing() throws Exception {
        String fileName = "signatures/rsakeyvalue.xml";
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream is = tcl.getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");

        Document doc = DocumentUtil.getDocument(is);
        assertNotNull(doc);
        assertNotNull(doc.getDocumentElement());
        
        Element rsaEl = (Element) doc.getElementsByTagName("ds:RSAKeyValue").item(0);
        assertNotNull(rsaEl);
        
        RSAKeyValueType rsa = XMLSignatureUtil.getRSAKeyValue(rsaEl);
        assertNotNull(rsa);
        assertNotNull(rsa.getModulus());
        assertNotNull(rsa.getExponent()); 
        
        System.out.println(rsa);
        
        RSAPublicKey publicKey = rsa.convertToPublicKey();
        assertNotNull(publicKey);
    }
}