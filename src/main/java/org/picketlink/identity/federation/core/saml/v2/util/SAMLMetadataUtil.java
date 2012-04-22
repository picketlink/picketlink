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

import java.security.cert.X509Certificate;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Deals with SAML2 Metadata
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLMetadataUtil
{
   /**
    * Get the {@link X509Certificate} from the KeyInfo
    * @param keyDescriptor
    * @return
    * @throws ProcessingException 
    * @throws ConfigurationException 
    */
   public static X509Certificate getCertificate( KeyDescriptorType keyDescriptor ) throws ConfigurationException, ProcessingException
   {
      X509Certificate cert = null;
      Element keyInfo = keyDescriptor.getKeyInfo();
      if( keyInfo != null )
      {
         NodeList x509DataNodes = keyInfo.getElementsByTagName( "X509Data" );
         if( x509DataNodes == null || x509DataNodes.getLength() == 0 )
         {
            x509DataNodes = keyInfo.getElementsByTagNameNS( JBossSAMLURIConstants.XMLDSIG_NSURI.get(), "X509Data" );
         }
         
         if( x509DataNodes == null || x509DataNodes.getLength() == 0 )
         {
            x509DataNodes = keyInfo.getElementsByTagName("ds:X509Data" );
         }
         
         if( x509DataNodes != null && x509DataNodes.getLength() > 0 )
         {
            //Choose the first one
            Node x509DataNode = x509DataNodes.item(0);
            NodeList children = x509DataNode.getChildNodes();
            int len = children != null ? children.getLength() : 0 ;
            for( int i = 0 ; i < len ; i++ )
            {
               Node nl = children.item(i);
               if( nl.getNodeName().contains( "X509Certificate" ) )
               {
                  Node certNode = nl.getFirstChild();
                  String certNodeValue = certNode.getNodeValue();
                  cert = XMLSignatureUtil.getX509CertificateFromKeyInfoString( certNodeValue.replaceAll("\\s", ""));
                  break;
               }
            }
         }
      }
      return cert;
   }
}