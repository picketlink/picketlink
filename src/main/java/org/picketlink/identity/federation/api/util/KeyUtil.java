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
package org.picketlink.identity.federation.api.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.Base64;
import org.w3c.dom.Element;

/**
 * Utility dealing with PublicKey/Certificates and xml-dsig KeyInfoType
 * @author Anil.Saldhana@redhat.com
 * @since Apr 29, 2009
 */
public class KeyUtil
{
   private static String EOL = getSystemProperty("line.separator", "\n");

   /**
    * Base64 encode the certificate
    * @param certificate
    * @return
    * @throws CertificateEncodingException
    */
   public static String encodeAsString(Certificate certificate) throws CertificateEncodingException
   {
      return Base64.encodeBytes(certificate.getEncoded());
   }

   /**
    * Given a certificate, build a keyinfo type
    * @param certificate
    * @return 
    * @throws CertificateException 
    * @throws ProcessingException 
    * @throws ParsingException 
    * @throws ConfigurationException 
    */
   public static Element getKeyInfo(Certificate certificate) throws CertificateException, ConfigurationException,
         ParsingException, ProcessingException
   {
      if (certificate == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "certificate is null");

      StringBuilder builder = new StringBuilder();

      if (certificate instanceof X509Certificate)
      {
         X509Certificate x509 = (X509Certificate) certificate;

         //Add the binary encoded x509 cert
         String certStr = Base64.encodeBytes(x509.getEncoded(), 76);

         builder.append("<KeyInfo xmlns=\'http://www.w3.org/2000/09/xmldsig#\'>").append(EOL).append("<X509Data>")
               .append(EOL).append("<X509Certificate>").append(EOL).append(certStr).append(EOL)
               .append("</X509Certificate>").append("</X509Data>").append("</KeyInfo>");
      }
      else
         throw new RuntimeException(ErrorCodes.NOT_IMPLEMENTED_YET);

      return DocumentUtil.getDocument(builder.toString()).getDocumentElement();
   }

   /**
    * Get the system property
    * @param key
    * @param defaultValue
    * @return
    */
   static String getSystemProperty(final String key, final String defaultValue)
   {
      return AccessController.doPrivileged(new PrivilegedAction<String>()
      {
         public String run()
         {
            return System.getProperty(key, defaultValue);
         }
      });
   }
}
