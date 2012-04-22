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
package org.picketlink.identity.federation.core.saml.v2.util;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;

/**
 * Signature utility for signing content 
 * @author Anil.Saldhana@redhat.com
 * @since Dec 16, 2008
 */
public class SignatureUtil
{
   /**
    * Get the XML Signature URI for the algo (RSA, DSA)
    * @param algo
    * @return
    */
   public static String getXMLSignatureAlgorithmURI(String algo)
   {
      String xmlSignatureAlgo = null;

      if ("DSA".equalsIgnoreCase(algo))
      {
         xmlSignatureAlgo = JBossSAMLConstants.SIGNATURE_SHA1_WITH_DSA.get();
      }
      else if ("RSA".equalsIgnoreCase(algo))
      {
         xmlSignatureAlgo = JBossSAMLConstants.SIGNATURE_SHA1_WITH_RSA.get();
      }
      return xmlSignatureAlgo;
   }

   /**
    * Sign a string using the private key
    * @param stringToBeSigned
    * @param signingKey
    * @return 
    * @throws GeneralSecurityException 
    */
   public static byte[] sign(String stringToBeSigned, PrivateKey signingKey) throws GeneralSecurityException
   {
      if (stringToBeSigned == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "stringToBeSigned");
      if (signingKey == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signingKey");

      String algo = signingKey.getAlgorithm();
      Signature sig = getSignature(algo);
      sig.initSign(signingKey);
      sig.update(stringToBeSigned.getBytes());
      return sig.sign();
   }

   /**
    * Validate the signed content with the signature value
    * @param signedContent
    * @param signatureValue
    * @param validatingKey
    * @return 
    * @throws GeneralSecurityException 
    */
   public static boolean validate(byte[] signedContent, byte[] signatureValue, PublicKey validatingKey)
         throws GeneralSecurityException
   {
      if (signedContent == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signedContent");
      if (signatureValue == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signatureValue");
      if (validatingKey == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "validatingKey");

      //We assume that the sigatureValue has the same algorithm as the public key
      //If not, there will be an exception anyway
      String algo = validatingKey.getAlgorithm();
      Signature sig = getSignature(algo);

      sig.initVerify(validatingKey);
      sig.update(signedContent);
      return sig.verify(signatureValue);
   }

   /**
    * Validate the signature using a x509 certificate
    * @param signedContent
    * @param signatureValue
    * @param signatureAlgorithm
    * @param validatingCert
    * @return 
    * @throws GeneralSecurityException 
    */
   public static boolean validate(byte[] signedContent, byte[] signatureValue, String signatureAlgorithm,
         X509Certificate validatingCert) throws GeneralSecurityException
   {
      if (signedContent == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signedContent");
      if (signatureValue == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signatureValue");
      if (signatureAlgorithm == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "signatureAlgorithm");
      if (validatingCert == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "validatingCert");

      Signature sig = getSignature(signatureAlgorithm);

      sig.initVerify(validatingCert);
      sig.update(signedContent);
      return sig.verify(signatureValue);
   }

   private static Signature getSignature(String algo) throws GeneralSecurityException
   {
      Signature sig = null;

      if ("DSA".equalsIgnoreCase(algo))
      {
         sig = Signature.getInstance(PicketLinkFederationConstants.DSA_SIGNATURE_ALGORITHM);
      }
      else if ("RSA".equalsIgnoreCase(algo))
      {
         sig = Signature.getInstance(PicketLinkFederationConstants.RSA_SIGNATURE_ALGORITHM);
      }
      else
         throw new RuntimeException(ErrorCodes.UNKNOWN_SIG_ALGO + algo);
      return sig;
   }
}