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
package org.picketlink.test.identity.federation.api.util;

import org.junit.Test;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Test the DocumentUtil
 *
 * @author Anil.Saldhana@redhat.com
 * @since Feb 6, 2009
 */
public class DocumentUtilUnitTestCase {

    String EncryptionSpecNS = "http://www.w3.org/2001/04/xmlenc#";
    String TAG_ENCRYPTEDDATA = "EncryptedData";
    String TAG_ENCRYPTEDKEY = "EncryptedKey";

    @Test
    public void testReadSAMLEncryptedAssertion() throws Exception {
        Document encDoc = getDocument("xml/dom/enc-sample.xml");
        Element encryptedDataElement = (Element) encDoc.getElementsByTagNameNS(EncryptionSpecNS, TAG_ENCRYPTEDDATA).item(0);
        Element encryptedKeyElement = (Element) encryptedDataElement.getElementsByTagNameNS(EncryptionSpecNS, TAG_ENCRYPTEDKEY)
                .item(0);
        assertNotNull(encryptedDataElement);
        assertNotNull(encryptedKeyElement);
    }

    /**
     * The SAML ResponseType has 2 assertions. We get the second one
     *
     * @throws Exception
     */
    @Test
    public void testReadingAnAssertionFromSAMLResponse() throws Exception {
        String id = "ID_976d8310-658a-450d-be39-f33c73c8afa6";
        Document responseDoc = getDocument("xml/dom/saml-response-2-assertions.xml");
        DocumentUtil.logNodes(responseDoc);
        Node n = DocumentUtil.getNodeWithAttribute(responseDoc, "urn:oasis:names:tc:SAML:2.0:assertion", "Assertion", "ID", id);
        assertNotNull(n);

        assertTrue("Assertion".equals(n.getNodeName()));
        NamedNodeMap nnm = n.getAttributes();
        assertEquals(3, nnm.getLength());
        Node att = nnm.getNamedItem("ID");
        assertEquals(id, att.getNodeValue());

        assertTrue(n.getParentNode() != null);
        assertTrue(n.getPreviousSibling() != null);
        assertTrue(n.getNextSibling() != null);

        // Let us get the first assertion
        Node firstAssertion = DocumentUtil.getNodeWithAttribute(responseDoc, "urn:oasis:names:tc:SAML:2.0:assertion",
                "Assertion", "ID", "ID_0be488d8-7089-4892-8aeb-83594c800706");
        Node prev = firstAssertion.getPreviousSibling();
        assertTrue(firstAssertion.getParentNode() != null);
        assertTrue(prev != null);
        Node next = firstAssertion.getNextSibling();
        assertTrue(next != null);

        // We have to check that the extracted node actually exists in the document
        assertTrue("Extracted Node is in doc", DocumentUtil.containsNode(responseDoc, firstAssertion));
    }

    @Test
    public void testReadSecurityDomain() throws Exception {
        String securityDomain = null;
        Document jbosswebDoc = getDocument("xml/dom/jboss-web.xml");
        assertNotNull(jbosswebDoc);
        Element rootNode = jbosswebDoc.getDocumentElement();
        NodeList nl = rootNode.getChildNodes();
        int length = nl.getLength();
        assertEquals(7, length);
        for (int i = 0; i < length; i++) {
            Node child = nl.item(i);
            if (child instanceof Element) {
                Element el = (Element) child;
                if ("security-domain".equals(el.getNodeName())) {
                    NodeList nl1 = el.getChildNodes();
                    int len = nl1.getLength();
                    for (int j = 0; j < len; j++) {
                        Node aChild = nl1.item(j);
                        if (aChild instanceof Text) {
                            securityDomain = ((Text) aChild).getNodeValue();
                            break;
                        }
                    }
                }
            }
        }
        assertEquals("idp", securityDomain);
    }

    private Document getDocument(String fileName) throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is == null)
            throw new RuntimeException("InputStream is null");
        return DocumentUtil.getDocument(is);
    }
}