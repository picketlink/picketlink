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
package org.picketlink.identity.federation.api.saml.v2.sig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathException;

import org.picketlink.identity.federation.api.saml.v2.request.SAML2Request;
import org.picketlink.identity.federation.api.saml.v2.response.SAML2Response;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Class that deals with SAML2 Signature
 * @author Anil.Saldhana@redhat.com
 * @since May 26, 2009
 */
public class SAML2Signature
{
   private String signatureMethod = SignatureMethod.RSA_SHA1;

   private String digestMethod = DigestMethod.SHA1;

   public String getSignatureMethod()
   {
      return signatureMethod;
   }

   public void setSignatureMethod(String signatureMethod)
   {
      this.signatureMethod = signatureMethod;
   }

   public String getDigestMethod()
   {
      return digestMethod;
   }

   public void setDigestMethod(String digestMethod)
   {
      this.digestMethod = digestMethod;
   }

   /**
    * Set to false, if you do not want to include keyinfo
    * in the signature
    * @param val
    * @since v2.0.1
    */
   public void setSignatureIncludeKeyInfo(boolean val)
   {
      if (!val)
      {
         XMLSignatureUtil.setIncludeKeyInfoInSignature(false);
      }
   }

   /**
    * Sign an RequestType at the root
    * @param request
    * @param keypair Key Pair 
    * @param digestMethod (Example: DigestMethod.SHA1)
    * @param signatureMethod (Example: SignatureMethod.DSA_SHA1)
    * @return 
    * @throws ParserConfigurationException 
    * @throws IOException 
    * @throws SAXException 
    * @throws XMLSignatureException 
    * @throws MarshalException 
    * @throws GeneralSecurityException 
    */
   public Document sign(RequestAbstractType request, KeyPair keypair) throws SAXException, IOException,
         ParserConfigurationException, GeneralSecurityException, MarshalException, XMLSignatureException
   {
      SAML2Request saml2Request = new SAML2Request();
      Document doc = saml2Request.convert(request);
      doc.normalize();

      String referenceURI = "#" + request.getID();

      return XMLSignatureUtil.sign(doc, keypair, digestMethod, signatureMethod, referenceURI);
   }

   /**
    * Sign an ResponseType at the root
    * @param response
    * @param keypair Key Pair 
    * @param digestMethod (Example: DigestMethod.SHA1)
    * @param signatureMethod (Example: SignatureMethod.DSA_SHA1)
    * @return 
    * @throws ParserConfigurationException  
    * @throws XMLSignatureException 
    * @throws MarshalException 
    * @throws GeneralSecurityException 
    */
   public Document sign(ResponseType response, KeyPair keypair) throws ParserConfigurationException,
         GeneralSecurityException, MarshalException, XMLSignatureException
   {
      SAML2Response saml2Request = new SAML2Response();
      Document doc = saml2Request.convert(response);
      doc.normalize();

      return sign(doc, response.getID(), keypair);
   }

   /**
    * Sign an Document at the root
    * @param response
    * @param keypair Key Pair 
    * @param digestMethod (Example: DigestMethod.SHA1)
    * @param signatureMethod (Example: SignatureMethod.DSA_SHA1)
    * @return 
    * @throws ParserConfigurationException  
    * @throws XMLSignatureException 
    * @throws MarshalException 
    * @throws GeneralSecurityException 
    */
   public Document sign(Document doc, String referenceID, KeyPair keypair) throws ParserConfigurationException,
         GeneralSecurityException, MarshalException, XMLSignatureException
   {
      String referenceURI = "#" + referenceID;

      return XMLSignatureUtil.sign(doc, keypair, digestMethod, signatureMethod, referenceURI);
   }

   /**
    * Sign an assertion whose id value is provided in the response type
    * @param response
    * @param idValueOfAssertion
    * @param keypair
    * @param referenceURI
    * @return 
    * @throws ParserConfigurationException  
    * @throws TransformerException 
    * @throws TransformerFactoryConfigurationError 
    * @throws XPathException 
    * @throws XMLSignatureException 
    * @throws MarshalException 
    * @throws GeneralSecurityException 
    */
   public Document sign(ResponseType response, String idValueOfAssertion, KeyPair keypair, String referenceURI)
         throws ParserConfigurationException, XPathException, TransformerFactoryConfigurationError,
         TransformerException, GeneralSecurityException, MarshalException, XMLSignatureException
   {
      SAML2Response saml2Response = new SAML2Response();
      Document doc = saml2Response.convert(response);

      return sign(doc, idValueOfAssertion, keypair, referenceURI);
   }

   /**
    * Sign a document
    * @param doc
    * @param idValueOfAssertion
    * @param keypair
    * @param referenceURI
    * @return
    * @throws ParserConfigurationException
    * @throws XPathException
    * @throws TransformerFactoryConfigurationError
    * @throws TransformerException
    * @throws GeneralSecurityException
    * @throws MarshalException
    * @throws XMLSignatureException
    */
   public Document sign(Document doc, String idValueOfAssertion, KeyPair keypair, String referenceURI)
         throws ParserConfigurationException, XPathException, TransformerFactoryConfigurationError,
         TransformerException, GeneralSecurityException, MarshalException, XMLSignatureException
   {

      Node assertionNode = DocumentUtil.getNodeWithAttribute(doc, JBossSAMLURIConstants.ASSERTION_NSURI.get(),
            "Assertion", "ID", idValueOfAssertion);

      return XMLSignatureUtil.sign(doc, assertionNode, keypair, digestMethod, signatureMethod, referenceURI);
   }

   /**
    * Sign a SAML Document
    * @param samlDocument
    * @param keypair
    * @throws ProcessingException
    */
   public void signSAMLDocument(Document samlDocument, KeyPair keypair) throws ProcessingException
   {
      //Get the ID from the root
      String id = samlDocument.getDocumentElement().getAttribute("ID");
      try
      {
         sign(samlDocument, id, keypair);
      }
      catch (Exception e)
      {
         throw new ProcessingException(e);
      }
   }

   /**
    * Validate the SAML2 Document
    * @param signedDocument
    * @param publicKey
    * @return
    * @throws ProcessingException
    */
   public boolean validate(Document signedDocument, PublicKey publicKey) throws ProcessingException
   {
      try
      {
         return XMLSignatureUtil.validate(signedDocument, publicKey);
      }
      catch (MarshalException me)
      {
         throw new ProcessingException(me.getLocalizedMessage());
      }
      catch (XMLSignatureException xse)
      {
         throw new ProcessingException(xse.getLocalizedMessage());
      }
   }
}