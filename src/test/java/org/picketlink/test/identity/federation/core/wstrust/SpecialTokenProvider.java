/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.core.wstrust;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.wstrust.SecurityToken;
import org.picketlink.identity.federation.core.wstrust.StandardSecurityToken;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * Mock {@code SecurityTokenProvider} used in the test scenarios.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SpecialTokenProvider implements SecurityTokenProvider
{
   
   private Map<String, String> properties;
   
   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#initialize(java.util.Map)
    */
   public void initialize(Map<String, String> properties)
   {
      this.properties = properties;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#cancelToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
    */
   public void cancelToken( ProtocolContext protoContext ) throws ProcessingException
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#issueToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
    */
   public void issueToken( ProtocolContext protoContext) throws ProcessingException
   {
      WSTrustRequestContext context = (WSTrustRequestContext) protoContext;
      
      // create a simple sample token using the info from the request.
      String caller = context.getCallerPrincipal() == null ? "anonymous" : context.getCallerPrincipal().getName();
      URI tokenType = context.getRequestSecurityToken().getTokenType();
      if (tokenType == null)
      {
         try
         {
            tokenType = new URI("http://www.tokens.org/SpecialToken");
         }
         catch (URISyntaxException ignore)
         {
         }
      }

      // we will use DOM to create the token.
      try
      {
         Document doc = DocumentUtil.createDocument();

         String namespaceURI = "http://www.tokens.org";
         Element root = doc.createElementNS(namespaceURI, "token:SpecialToken");
         Element child = doc.createElementNS(namespaceURI, "token:SpecialTokenValue");
         child.appendChild(doc.createTextNode("Principal:" + caller));
         root.appendChild(child);
         String id = IDGenerator.create("ID_");
         root.setAttributeNS(namespaceURI, "ID", id);
         root.setAttributeNS(namespaceURI, "TokenType", tokenType.toString());
         root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:token", namespaceURI);
         
         doc.appendChild(root);

         SecurityToken token = new StandardSecurityToken(tokenType.toString(), root, id);
         context.setSecurityToken(token);
      }
      catch (ConfigurationException pce)
      {
         pce.printStackTrace();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#renewToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
    */
   public void renewToken( ProtocolContext protoContext ) throws ProcessingException
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityTokenProvider#validateToken(org.picketlink.identity.federation.core.wstrust.WSTrustRequestContext)
    */
   public void validateToken( ProtocolContext protoContext ) throws ProcessingException
   {
   }
   
   /**
    * <p>
    * Just returns a reference to the properties that have been configured for testing purposes.
    * </p>
    * 
    * @return a reference to the properties map.
    */
   public Map<String, String> getProperties()
   {
      return this.properties;
   }

   /**
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#supports(java.lang.String)
    */
   public boolean supports(String namespace)
   { 
      return WSTrustConstants.BASE_NAMESPACE.equals(namespace);
   }

   /**
    * 
    * @see org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider#tokenType()
    */
   public String tokenType()
   {
      return WSTrustConstants.BASE_NAMESPACE;
   }

   public QName getSupportedQName()
   { 
      return new QName( tokenType(), "SpecialToken" );
   }

   public String family()
   { 
      return SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();
   }
}