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
package org.picketlink.identity.federation.core.util;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.xml.namespace.QName;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility for XML Encryption
 * <b>Note: </b> This utility is currently using Apache XML Security
 * library API. JSR-106 is not yet final. Until that happens,we
 * rely on the non-standard API.
 * 
 * @author Anil.Saldhana@redhat.com
 * @since May 4, 2009
 */
public class XMLEncryptionUtil
{
   static
   {
      //Initialize the Apache XML Security Library
      org.apache.xml.security.Init.init();
   }

   public static final String CIPHER_DATA_LOCALNAME = "CipherData";

   public static final String ENCRYPTED_KEY_LOCALNAME = "EncryptedKey";

   public static final String DS_KEY_INFO = "ds:KeyInfo";

   public static final String XMLNS = "http://www.w3.org/2000/xmlns/";

   public static String XMLSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

   public static String XMLENC_NS = "http://www.w3.org/2001/04/xmlenc#";

   private static HashMap<String, EncryptionAlgorithm> algorithms = new HashMap<String, EncryptionAlgorithm>(4);

   private static class EncryptionAlgorithm
   {
      EncryptionAlgorithm(String jceName, String xmlSecName, int size)
      {
         this.jceName = jceName;
         this.xmlSecName = xmlSecName;
         this.size = size;
      }

      @SuppressWarnings("unused")
      public String jceName;

      public String xmlSecName;

      public int size;
   }

   static
   {
      algorithms.put("aes-128", new EncryptionAlgorithm("AES", XMLCipher.AES_128, 128));
      algorithms.put("aes-192", new EncryptionAlgorithm("AES", XMLCipher.AES_192, 192));
      algorithms.put("aes-256", new EncryptionAlgorithm("AES", XMLCipher.AES_256, 256));
      algorithms.put("aes", new EncryptionAlgorithm("AES", XMLCipher.AES_256, 256));

      algorithms.put("tripledes", new EncryptionAlgorithm("TripleDes", XMLCipher.TRIPLEDES, 168));
   }

   /**
    * Given the JCE algorithm, get the XML Encryption URL
    * @param certAlgo
    * @return
    */
   public static String getEncryptionURL(String certAlgo)
   {
      EncryptionAlgorithm ea = algorithms.get(certAlgo);
      if (ea == null)
         throw new RuntimeException(ErrorCodes.UNKNOWN_ENC_ALGO + certAlgo);
      return ea.xmlSecName;
   }

   /**
    * Given the JCE algorithm, get the XML Encryption KeySize
    * @param certAlgo
    * @return
    */
   public static int getEncryptionKeySize(String certAlgo)
   {
      EncryptionAlgorithm ea = algorithms.get(certAlgo);
      if (ea == null)
         throw new RuntimeException(ErrorCodes.UNKNOWN_ENC_ALGO + certAlgo);
      return ea.size;
   }

   /**
    * <p>
    * Encrypt the Key to be transported
    * </p>
    * <p>
    * Data is encrypted with a SecretKey. Then the key needs to be
    * transported to the other end where it is needed for decryption.
    * For the Key transport, the SecretKey is encrypted with the
    * recipient's public key. At the receiving end, the receiver
    * can decrypt the Secret Key using his private key.s
    * </p>
    * @param document
    * @param keyToBeEncrypted Symmetric Key (SecretKey)
    * @param keyUsedToEncryptSecretKey Asymmetric Key (Public Key)
    * @param keySize Length of the key
    * @return 
    * @throws ProcessingException  
    */
   public static EncryptedKey encryptKey(Document document, SecretKey keyToBeEncrypted,
         PublicKey keyUsedToEncryptSecretKey, int keySize) throws ProcessingException
   {
      XMLCipher keyCipher = null;
      String pubKeyAlg = keyUsedToEncryptSecretKey.getAlgorithm();

      try
      {
         String keyWrapAlgo = getXMLEncryptionURLForKeyUnwrap(pubKeyAlg, keySize);
         keyCipher = XMLCipher.getInstance(keyWrapAlgo);

         keyCipher.init(XMLCipher.WRAP_MODE, keyUsedToEncryptSecretKey);
         return keyCipher.encryptKey(document, keyToBeEncrypted);
      }
      catch (XMLEncryptionException e)
      {
         throw new ProcessingException(e);
      }
   }

   /**
    * Given an element in a Document, encrypt the element and replace
    * the element in the document with the encrypted data
    * @param elementQName QName of the element that we like to encrypt
    * @param publicKey
    * @param secretKey
    * @param keySize
    * @param wrappingElementQName A QName of an element that will wrap the encrypted element
    * @param addEncryptedKeyInKeyInfo Need for the EncryptedKey to be placed in ds:KeyInfo
    * @return 
    * @throws ProcessingException 
    */
   public static void encryptElement(QName elementQName, Document document, PublicKey publicKey, SecretKey secretKey,
         int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo) throws ProcessingException
   {
      if (elementQName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "elementQName");
      if (document == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "document");
      String wrappingElementPrefix = wrappingElementQName.getPrefix();
      if (wrappingElementPrefix == null || wrappingElementPrefix == "")
         throw new IllegalArgumentException(ErrorCodes.WRONG_TYPE + "Wrapping element prefix invalid");

      Element documentElement = DocumentUtil.getElement(document, elementQName);

      if (documentElement == null)
         throw new IllegalStateException(ErrorCodes.DOM_MISSING_DOC_ELEMENT + elementQName.toString());

      XMLCipher cipher = null;
      EncryptedKey encryptedKey = encryptKey(document, secretKey, publicKey, keySize);

      String encryptionAlgorithm = getXMLEncryptionURL(secretKey.getAlgorithm(), keySize);
      //Encrypt the Document 
      try
      {
         cipher = XMLCipher.getInstance(encryptionAlgorithm);
         cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
      }
      catch (XMLEncryptionException e1)
      {
         throw new ProcessingException(e1);
      }

      Document encryptedDoc;
      try
      {
         encryptedDoc = cipher.doFinal(document, documentElement);
      }
      catch (Exception e)
      {
         throw new ProcessingException(e);
      }

      // The EncryptedKey element is added
      Element encryptedKeyElement = cipher.martial(document, encryptedKey);

      String wrappingElementName = wrappingElementPrefix + ":" + wrappingElementQName.getLocalPart();

      //Create the wrapping element and set its attribute NS
      Element wrappingElement = encryptedDoc.createElementNS(wrappingElementQName.getNamespaceURI(),
            wrappingElementName);

      if (wrappingElementPrefix == null || wrappingElementPrefix == "")
      {
         wrappingElementName = wrappingElementQName.getLocalPart();
      }
      wrappingElement.setAttributeNS(XMLNS, "xmlns:" + wrappingElementPrefix, wrappingElementQName.getNamespaceURI());

      //Get Hold of the Cipher Data
      NodeList cipherElements = encryptedDoc.getElementsByTagNameNS(XMLENC_NS, "EncryptedData");
      if (cipherElements == null || cipherElements.getLength() == 0)
         throw new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT + "xenc:EncryptedData");
      Element encryptedDataElement = (Element) cipherElements.item(0);

      Node parentOfEncNode = encryptedDataElement.getParentNode();
      parentOfEncNode.replaceChild(wrappingElement, encryptedDataElement);

      wrappingElement.appendChild(encryptedDataElement);

      if (addEncryptedKeyInKeyInfo)
      {
         // Outer ds:KeyInfo Element to hold the EncryptionKey
         Element sigElement = encryptedDoc.createElementNS(XMLSIG_NS, DS_KEY_INFO);
         sigElement.setAttributeNS(XMLNS, "xmlns:ds", XMLSIG_NS);
         sigElement.appendChild(encryptedKeyElement);

         //Insert the Encrypted key before the CipherData element 
         NodeList nodeList = encryptedDoc.getElementsByTagNameNS(XMLENC_NS, CIPHER_DATA_LOCALNAME);
         if (nodeList == null || nodeList.getLength() == 0)
            throw new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT + "xenc:CipherData");
         Element cipherDataElement = (Element) nodeList.item(0);
         Node cipherParent = cipherDataElement.getParentNode();
         cipherParent.insertBefore(sigElement, cipherDataElement);
      }
      else
      {
         //Add the encrypted key as a child of the wrapping element
         wrappingElement.appendChild(encryptedKeyElement);
      }
   }

   /**
    * <p>
    * Encrypts an element in a XML document using the specified public key, secret key, and key size. This method
    * doesn't wrap the encrypted element in a new element. Instead, it replaces the element with its encrypted version. 
    * </p>
    * <p>
    * For example, calling this method to encrypt the <tt><b>inner</b></tt> element in the following XML document
    * <pre>
    *    &lt;root&gt;
    *       &lt;outer&gt;
    *          &lt;inner&gt;
    *             ...
    *          &lt;/inner&gt;
    *       &lt;/outer&gt;
    *    &lt;/root&gt;
    * </pre>
    * 
    * would result in a document similar to
    * <pre>
    *    &lt;root&gt;
    *       &lt;outer&gt;
    *          &lt;xenc:EncryptedData xmlns:xenc="..."&gt;
    *             ...
    *          &lt;/xenc:EncryptedData&gt;
    *       &lt;/outer&gt;
    *    &lt;/root&gt;
    * </pre>
    * </p>
    * 
    * @param document the {@code Document} that contains the element to be encrypted.
    * @param element the {@code Element} to be encrypted.
    * @param publicKey the {@code PublicKey} that must be used to encrypt the secret key.
    * @param secretKey the {@code SecretKey} used to encrypt the specified element.
    * @param keySize the size (in bits) of the secret key.
    * @throws ProcessingException if an error occurs while encrypting the element with the specified params.
    */
   public static void encryptElement(Document document, Element element, PublicKey publicKey, SecretKey secretKey,
         int keySize) throws ProcessingException
   {
      if (element == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "element");
      if (document == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "document");

      XMLCipher cipher = null;
      EncryptedKey encryptedKey = encryptKey(document, secretKey, publicKey, keySize);
      String encryptionAlgorithm = getXMLEncryptionURL(secretKey.getAlgorithm(), keySize);

      //Encrypt the Document 
      try
      {
         cipher = XMLCipher.getInstance(encryptionAlgorithm);
         cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
      }
      catch (XMLEncryptionException e1)
      {
         throw new ProcessingException(e1);
      }

      Document encryptedDoc;
      try
      {
         encryptedDoc = cipher.doFinal(document, element);
      }
      catch (Exception e)
      {
         throw new ProcessingException(e);
      }

      // The EncryptedKey element is added
      Element encryptedKeyElement = cipher.martial(document, encryptedKey);

      // Outer ds:KeyInfo Element to hold the EncryptionKey
      Element sigElement = encryptedDoc.createElementNS(XMLSIG_NS, DS_KEY_INFO);
      sigElement.setAttributeNS(XMLNS, "xmlns:ds", XMLSIG_NS);
      sigElement.appendChild(encryptedKeyElement);

      //Insert the Encrypted key before the CipherData element 
      NodeList nodeList = encryptedDoc.getElementsByTagNameNS(XMLENC_NS, CIPHER_DATA_LOCALNAME);
      if (nodeList == null || nodeList.getLength() == 0)
         throw new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT + "xenc:CipherData");
      Element cipherDataElement = (Element) nodeList.item(0);
      Node cipherParent = cipherDataElement.getParentNode();
      cipherParent.insertBefore(sigElement, cipherDataElement);
   }

   /**
    * Encrypt the root document element inside a Document.
    * <b>NOTE:</> The document root element will be replaced 
    * by the wrapping element.
    * 
    * @param document Document that contains an element to encrypt
    * @param publicKey The Public Key used to encrypt the secret encryption key
    * @param secretKey The secret encryption key
    * @param keySize Length of key
    * @param wrappingElementQName QName of the element to be used to wrap around
    *        the cipher data.
    * @param addEncryptedKeyInKeyInfo Should the encrypted key be inside a KeyInfo
    *        or added as a peer of Cipher Data
    * @return An element that has the wrappingElementQName  
    * @throws ProcessingException 
    * @throws ConfigurationException 
    */
   public static Element encryptElementInDocument(Document document, PublicKey publicKey, SecretKey secretKey,
         int keySize, QName wrappingElementQName, boolean addEncryptedKeyInKeyInfo) throws ProcessingException,
         ConfigurationException
   {
      String wrappingElementPrefix = wrappingElementQName.getPrefix();
      if (wrappingElementPrefix == null || wrappingElementPrefix == "")
         throw new IllegalArgumentException(ErrorCodes.WRONG_TYPE + "Wrapping element prefix invalid");

      XMLCipher cipher = null;
      EncryptedKey encryptedKey = encryptKey(document, secretKey, publicKey, keySize);

      String encryptionAlgorithm = getXMLEncryptionURL(secretKey.getAlgorithm(), keySize);
      //Encrypt the Document 
      try
      {
         cipher = XMLCipher.getInstance(encryptionAlgorithm);
         cipher.init(XMLCipher.ENCRYPT_MODE, secretKey);
      }
      catch (XMLEncryptionException e1)
      {
         throw new ConfigurationException(e1);
      }

      Document encryptedDoc;
      try
      {
         encryptedDoc = cipher.doFinal(document, document.getDocumentElement());
      }
      catch (Exception e)
      {
         throw new ProcessingException(e);
      }

      // The EncryptedKey element is added
      Element encryptedKeyElement = cipher.martial(document, encryptedKey);

      String wrappingElementName = wrappingElementPrefix + ":" + wrappingElementQName.getLocalPart();

      //Create the wrapping element and set its attribute NS
      Element wrappingElement = encryptedDoc.createElementNS(wrappingElementQName.getNamespaceURI(),
            wrappingElementName);

      if (wrappingElementPrefix == null || wrappingElementPrefix == "")
      {
         wrappingElementName = wrappingElementQName.getLocalPart();
      }
      wrappingElement.setAttributeNS(XMLNS, "xmlns:" + wrappingElementPrefix, wrappingElementQName.getNamespaceURI());

      Element encryptedDocRootElement = encryptedDoc.getDocumentElement();
      //Bring in the encrypted wrapping element to wrap the root node
      encryptedDoc.replaceChild(wrappingElement, encryptedDocRootElement);

      wrappingElement.appendChild(encryptedDocRootElement);

      if (addEncryptedKeyInKeyInfo)
      {
         // Outer ds:KeyInfo Element to hold the EncryptionKey
         Element sigElement = encryptedDoc.createElementNS(XMLSIG_NS, DS_KEY_INFO);
         sigElement.setAttributeNS(XMLNS, "xmlns:ds", XMLSIG_NS);
         sigElement.appendChild(encryptedKeyElement);

         //Insert the Encrypted key before the CipherData element 
         NodeList nodeList = encryptedDocRootElement.getElementsByTagNameNS(XMLENC_NS, CIPHER_DATA_LOCALNAME);
         if (nodeList == null || nodeList.getLength() == 0)
            throw new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT + "xenc:CipherData");

         Element cipherDataElement = (Element) nodeList.item(0);
         encryptedDocRootElement.insertBefore(sigElement, cipherDataElement);
      }
      else
      {
         //Add the encrypted key as a child of the wrapping element
         wrappingElement.appendChild(encryptedKeyElement);
      }

      return encryptedDoc.getDocumentElement();
   }

   /**
    * Decrypt an encrypted element inside a document
    * @param documentWithEncryptedElement 
    * @param privateKey key need to unwrap the encryption key
    * @return the document with the encrypted element replaced by the data element  
    * @throws XMLEncryptionException 
    * @throws ProcessingException 
    */
   public static Element decryptElementInDocument(Document documentWithEncryptedElement, PrivateKey privateKey)
         throws ProcessingException
   {
      if (documentWithEncryptedElement == null)
         throw new IllegalArgumentException("Input document is null");

      //Look for encrypted data element
      Element documentRoot = documentWithEncryptedElement.getDocumentElement();
      Element encDataElement = getNextElementNode(documentRoot.getFirstChild());
      if (encDataElement == null)
         throw new IllegalStateException(ErrorCodes.DOM_MISSING_ELEMENT
               + "No element representing the encrypted data found");

      //Look at siblings for the key
      Element encKeyElement = getNextElementNode(encDataElement.getNextSibling());
      if (encKeyElement == null)
      {
         //Search the enc data element for enc key
         NodeList nodeList = encDataElement.getElementsByTagNameNS(XMLENC_NS, ENCRYPTED_KEY_LOCALNAME);

         if (nodeList == null || nodeList.getLength() == 0)
            throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Encrypted Key not found in the enc data");

         encKeyElement = (Element) nodeList.item(0);
      }

      XMLCipher cipher;
      EncryptedData encryptedData;
      EncryptedKey encryptedKey;
      try
      {
         cipher = XMLCipher.getInstance();
         cipher.init(XMLCipher.DECRYPT_MODE, null);
         encryptedData = cipher.loadEncryptedData(documentWithEncryptedElement, encDataElement);
         encryptedKey = cipher.loadEncryptedKey(documentWithEncryptedElement, encKeyElement);
      }
      catch (XMLEncryptionException e1)
      {
         throw new ProcessingException(e1);
      }

      Document decryptedDoc = null;

      if (encryptedData != null && encryptedKey != null)
      {
         try
         {
            String encAlgoURL = encryptedData.getEncryptionMethod().getAlgorithm();
            XMLCipher keyCipher = XMLCipher.getInstance();
            keyCipher.init(XMLCipher.UNWRAP_MODE, privateKey);
            Key encryptionKey = keyCipher.decryptKey(encryptedKey, encAlgoURL);
            cipher = XMLCipher.getInstance();
            cipher.init(XMLCipher.DECRYPT_MODE, encryptionKey);

            decryptedDoc = cipher.doFinal(documentWithEncryptedElement, encDataElement);
         }
         catch (Exception e)
         {
            throw new ProcessingException(e);
         }
      }

      Element decryptedRoot = decryptedDoc.getDocumentElement();
      Element dataElement = getNextElementNode(decryptedRoot.getFirstChild());
      if (dataElement == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Data Element after encryption is null");

      decryptedRoot.removeChild(dataElement);
      decryptedDoc.replaceChild(dataElement, decryptedRoot);

      return decryptedDoc.getDocumentElement();
   }

   /**
    * From the secret key, get the W3C XML Encryption URL
    * @param publicKeyAlgo
    * @param keySize
    * @return
    */
   private static String getXMLEncryptionURLForKeyUnwrap(String publicKeyAlgo, int keySize)
   {
      if ("AES".equals(publicKeyAlgo))
      {
         switch (keySize)
         {
            case 192 :
               return XMLCipher.AES_192_KeyWrap;
            case 256 :
               return XMLCipher.AES_256_KeyWrap;
            default :
               return XMLCipher.AES_128_KeyWrap;
         }
      }
      if (publicKeyAlgo.contains("RSA"))
         return XMLCipher.RSA_v1dot5;
      if (publicKeyAlgo.contains("DES"))
         return XMLCipher.TRIPLEDES_KeyWrap;
      throw new IllegalArgumentException(ErrorCodes.UNSUPPORTED_TYPE + "unsupported publicKey Algo:" + publicKeyAlgo);
   }

   /**
    * From the secret key, get the W3C XML Encryption URL
    * @param secretKey
    * @param keySize
    * @return
    */
   private static String getXMLEncryptionURL(String algo, int keySize)
   {
      if ("AES".equals(algo))
      {
         switch (keySize)
         {
            case 192 :
               return XMLCipher.AES_192;
            case 256 :
               return XMLCipher.AES_256;
            default :
               return XMLCipher.AES_128;
         }
      }
      if (algo.contains("RSA"))
         return XMLCipher.RSA_v1dot5;
      if (algo.contains("DES"))
         return XMLCipher.TRIPLEDES_KeyWrap;
      throw new IllegalArgumentException(ErrorCodes.UNSUPPORTED_TYPE + "Secret Key with unsupported algo:" + algo);
   }

   /**
    * Returns the next Element node.
    */
   private static Element getNextElementNode(Node node)
   {
      while (node != null)
      {
         if (Node.ELEMENT_NODE == node.getNodeType())
            return (Element) node;
         node = node.getNextSibling();
      }
      return null;
   }
}