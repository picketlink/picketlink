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
package org.picketlink.test.identity.federation.api.util;

import java.io.InputStream;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Unit Test the DocumentUtil
 * @author Anil.Saldhana@redhat.com
 * @since Feb 6, 2009
 */
public class DocumentUtilUnitTestCase extends TestCase
{
   String EncryptionSpecNS = "http://www.w3.org/2001/04/xmlenc#";
   String TAG_ENCRYPTEDDATA = "EncryptedData";
   String TAG_ENCRYPTEDKEY = "EncryptedKey";
   
   public void testReadSAMLEncryptedAssertion() throws Exception
   {
      Document encDoc = getDocument("xml/dom/enc-sample.xml");
      Element encryptedDataElement =
         (Element) encDoc.getElementsByTagNameNS(
             EncryptionSpecNS,
             TAG_ENCRYPTEDDATA).item(0);
      Element encryptedKeyElement =
         (Element) encryptedDataElement.getElementsByTagNameNS(
               EncryptionSpecNS,
               TAG_ENCRYPTEDKEY).item(0);
      assertNotNull(encryptedDataElement);
      assertNotNull(encryptedKeyElement);
   }  
   
   /**
    * The SAML ResponseType has 2 assertions. We get the second one
    * @throws Exception
    */
   public void testReadingAnAssertionFromSAMLResponse() throws Exception
   {
      String id = "ID_976d8310-658a-450d-be39-f33c73c8afa6";
      Document responseDoc = getDocument("xml/dom/saml-response-2-assertions.xml"); 
      DocumentUtil.logNodes(responseDoc);
      Node n = DocumentUtil.getNodeWithAttribute(responseDoc, "urn:oasis:names:tc:SAML:2.0:assertion",
            "Assertion",
            "ID", id );
      assertNotNull(n); 
      
      assertTrue("Assertion".equals(n.getNodeName()));
      NamedNodeMap nnm = n.getAttributes();
      assertEquals(3, nnm.getLength() );
      Node att = nnm.getNamedItem("ID");
      assertEquals(id, att.getNodeValue());
      
      assertTrue(n.getParentNode() != null);
      assertTrue(n.getPreviousSibling() != null);
      assertTrue(n.getNextSibling() != null);
      
      //Let us get the first assertion
      Node firstAssertion = DocumentUtil.getNodeWithAttribute(responseDoc, 
            "urn:oasis:names:tc:SAML:2.0:assertion",
            "Assertion",
            "ID", "ID_0be488d8-7089-4892-8aeb-83594c800706" );
      Node prev = firstAssertion.getPreviousSibling();
      assertTrue(firstAssertion.getParentNode() != null);
      assertTrue( prev!= null);
      Node next = firstAssertion.getNextSibling();
      assertTrue( next != null);
       
      //We have to check that the extracted node actually exists in the document
      assertTrue("Extracted Node is in doc",DocumentUtil.containsNode(responseDoc, firstAssertion)); 
   }
   
   private  Document getDocument(String fileName) throws Exception
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
      if(is == null)
         throw new RuntimeException("InputStream is null");
      return DocumentUtil.getDocument(is);
   } 
}