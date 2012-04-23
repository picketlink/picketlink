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
package org.picketlink.trust.jbossws.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.AuthorizationManager;
import org.jboss.wsf.common.handler.GenericSOAPHandler;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.Util;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for the PicketLink Trust Handlers
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPicketLinkTrustHandler extends GenericSOAPHandler
{
   protected Logger log = Logger.getLogger(this.getClass());
   protected boolean trace = log.isTraceEnabled();
   
   protected static Set<QName> headers;
   
   protected static final String SEC_MGR_LOOKUP = "java:comp/env/security/securityMgr";
   protected static final String AUTHZ_MGR_LOOKUP = "java:comp/env/security/authorizationMgr";
   
   protected SecurityAdaptorFactory secAdapterfactory;

   static
   {
      HashSet<QName> set = new HashSet<QName>();
      set.add(Constants.WSSE_HEADER_QNAME);
      headers = Collections.unmodifiableSet(set);
   }
   
   public Set<QName> getHeaders()
   {
      //return a collection with just the wsse:Security header to pass the MustUnderstand check on it
      return headers;
   }
   
   /**
    * Get the JBoss Authentication Manager {@link AuthenticationManager} from JNDI
    * @return
    * @throws NamingException
    */
   protected AuthenticationManager getAuthenticationManager()
   { 
      if( secAdapterfactory == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         secAdapterfactory = spiProvider.getSPI(SecurityAdaptorFactory.class);
      }
      return (AuthenticationManager) lookupJNDI(SEC_MGR_LOOKUP);
   }
   
   /**
    * Get the JBoss Authorization Manager {@link AuthorizationManager} from JNDI
    * @return
    * @throws NamingException
    */
   protected AuthorizationManager getAuthorizationManager()
   { 
      if( secAdapterfactory == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         secAdapterfactory = spiProvider.getSPI(SecurityAdaptorFactory.class);
      }
      return (AuthorizationManager)lookupJNDI(AUTHZ_MGR_LOOKUP);
   }
   
   /**
    * Given a {@link Document}, create the WSSE element
    * @param document
    * @return
    */
   protected Element getSecurityHeaderElement(Document document)
   {
      Element element = document.createElementNS(Constants.WSSE_NS, Constants.WSSE_HEADER);
      Util.addNamespace(element, Constants.WSSE_PREFIX, Constants.WSSE_NS);
      Util.addNamespace(element, Constants.WSU_PREFIX, Constants.WSU_NS);
      Util.addNamespace(element, Constants.XML_ENCRYPTION_PREFIX, Constants.XML_SIGNATURE_NS);
      return element;
   }
   
   protected void trace(MessageContext msgContext)
   {
      if(trace)
      {
         if(msgContext instanceof SOAPMessageContext)
         {
            SOAPMessageContext soapMessageContext = (SOAPMessageContext) msgContext;
            log.trace("WSDL_PORT="+soapMessageContext.get(SOAPMessageContext.WSDL_PORT));
            log.trace("WSDL_OPERATION="+soapMessageContext.get(SOAPMessageContext.WSDL_OPERATION));
            log.trace("WSDL_INTERFACE="+soapMessageContext.get(SOAPMessageContext.WSDL_INTERFACE)); 
            log.trace("WSDL_SERVICE="+soapMessageContext.get(SOAPMessageContext.WSDL_SERVICE));     
         }
      }
   }

   /**
    * Given the NameID {@link Element}, return the user name
    * @param nameID
    * @return
    */
   protected String getUsername(final Element nameID) 
   {
      String username = nameID.getNodeValue();
      if (username == null) {
         final NodeList childNodes = nameID.getChildNodes();
         final int size = childNodes.getLength();
         for (int i = 0; i < size; i++) {
            final Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.TEXT_NODE) {
               username = childNode.getNodeValue();
            }
         }
      }
      return username;
   }
    
    /**
     * Get the SAML Assertion from the subject
     * @return
     */
    protected Element getAssertionFromSubject()
    {
       Element assertion = null;
       Subject subject =  SecurityActions.getAuthenticatedSubject();

       if(subject == null)
       {
          log.error("null subject, cannot extract SAML token required for WS-TRUST");
          return assertion;
       }

       Set<Object> creds = subject.getPublicCredentials();
       if( creds != null )
       {
          for( Object cred: creds)
          {
             if( cred instanceof SamlCredential)
             {
                SamlCredential samlCredential = (SamlCredential) cred;
                try
                {
                   assertion = samlCredential.getAssertionAsElement();
                }
                catch (ProcessingException e)
                {
                   log.error("failed to process SAML credential", e);
                }
                break;
             }
          } 
       }
       return assertion;
    }
    
    private Object lookupJNDI( String str)
    {
       try
      {
         Context context = new InitialContext();
          return context.lookup(str);
      }
      catch (NamingException e)
      { 
         throw new RuntimeException(e);
      }
    }
}