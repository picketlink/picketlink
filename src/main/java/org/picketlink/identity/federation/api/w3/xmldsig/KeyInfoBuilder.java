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
package org.picketlink.identity.federation.api.w3.xmldsig;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil; 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
 

/**
 * Builder for the W3C xml-dsig KeyInfoType
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class KeyInfoBuilder
{ 
   
   /**
    * Create a KeyInfoType
    * @return
    */
   public static Element createKeyInfo( String id )
   {
      Document doc = null;
      try
      {
         doc = DocumentUtil.createDocument();
      }
      catch (ConfigurationException e)
      {
         throw new RuntimeException( e );
      }
      Element keyInfoEl = doc.createElementNS( JBossSAMLURIConstants.XMLDSIG_NSURI.get(), JBossSAMLConstants.KEY_INFO.get() );
      keyInfoEl.setAttribute( "Id", id );
      return keyInfoEl;
   } 
}