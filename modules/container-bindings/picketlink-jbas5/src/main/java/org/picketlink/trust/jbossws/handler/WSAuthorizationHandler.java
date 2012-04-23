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

import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.callbacks.SecurityContextCallbackHandler;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.trust.jbossws.util.JBossWSNativeStackUtil;
import org.picketlink.trust.jbossws.util.JBossWSSERoleExtractor;
import org.w3c.dom.Node;

/**
 * An authorization handler for the POJO Web services
 * Based on the Authorize Operation on the JBossWS Native stack
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class WSAuthorizationHandler extends AbstractPicketLinkTrustHandler
{   
   public static final String UNCHECKED = "unchecked";
   
   //A simple hashmap that reduces the reparsing of jboss-wsse.xml for the same keys
   protected Map<String, List<String>> cache = new HashMap<String,List<String>>();
   
   @Override
   protected boolean handleInbound(MessageContext msgContext)
   {   
      if(trace)
      {
         log.trace("Handling Inbound Message");
         trace(msgContext);
      }
      ServletContext context = (ServletContext) msgContext.get(MessageContext.SERVLET_CONTEXT);
      //Read the jboss-wsse.xml file
      InputStream is = getWSSE(context);
      if( is == null )
         throw new RuntimeException(ErrorCodes.RESOURCE_NOT_FOUND +  "unable to load jboss-wsse.xml");
      
      QName portName = (QName) msgContext.get(MessageContext.WSDL_PORT); 
      QName opName = (QName) msgContext.get(MessageContext.WSDL_OPERATION);

      if(portName == null)
         portName = JBossWSNativeStackUtil.getPortNameViaReflection(getClass(), msgContext);
      
      if(portName == null)
         throw new RuntimeException(ErrorCodes.NULL_VALUE + "Unable to determine port name from the message context");
      
      if(opName == null)
         opName = getOperationName(msgContext);
      
      if(opName == null)
         throw new RuntimeException(ErrorCodes.NULL_VALUE + "Unable to determine operation name from the message context");
      
      List<String> roles = null;
      
      String key = portName.getLocalPart()+ "_" + opName.toString();
      
      //First check in cache
      if( cache.containsKey(key))
      {
         roles = cache.get(key);
      }
      else
      {
         try
         {
            roles = JBossWSSERoleExtractor.getRoles(is, portName.getLocalPart(), opName.toString());
         }
         catch (ProcessingException e)
         {
            throw new RuntimeException(e); 
         } 
         cache.put(key, roles);
      }
      
      if( !roles.contains(UNCHECKED))
      {
         AuthorizationManager authorizationManager = getAuthorizationManager();

         SecurityAdaptor securityAdaptor = secAdapterfactory.newSecurityAdapter();
         Principal principal = securityAdaptor.getPrincipal();
         Subject subject = SecurityActions.getAuthenticatedSubject();
         
         Set<Principal> expectedRoles = rolesSet(roles);
         if(!authorizationManager.doesUserHaveRole(principal, expectedRoles ))
         {
            SecurityContext sc = SecurityActions.getSecurityContext();
            StringBuilder builder = new StringBuilder("Authorization Failed:Principal=");
            builder.append(principal).append(":Expected Roles=").append(expectedRoles);
            SecurityContextCallbackHandler scbh = new SecurityContextCallbackHandler(sc);
            builder.append("::Actual Roles=").append(authorizationManager.getSubjectRoles(subject,scbh));
            log.error(builder.toString() );
            
            throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Authorization Failed");
         }
      }
      return true;
   } 
   
   protected Set<Principal> rolesSet(List<String> roles)
   {
      Set<Principal> principals = new HashSet<Principal>();
      for( String role: roles)
      {
         principals.add(new SimplePrincipal(role));
      }
      return principals;
   }
   
   protected InputStream getWSSE(ServletContext context)
   {
      if( context == null )
         throw new RuntimeException(ErrorCodes.NULL_VALUE + "Servlet Context is null");
      
      InputStream is = context.getResourceAsStream("/WEB-INF/jboss-wsse.xml");
      return is;
   }
   
   protected InputStream load( ClassLoader cl)
   {
      InputStream is = null;
      is = cl.getResourceAsStream("WEB-INF/jboss-wsse.xml");
      if( is == null)
         is = cl.getResourceAsStream("/WEB-INF/jboss-wsse.xml");
      return is;
   }
   
   private QName getOperationName(MessageContext msgContext)
   {
      SOAPMessageContext soapMessageContext = (SOAPMessageContext) msgContext;
      SOAPMessage soapMessage = soapMessageContext.getMessage();
      SOAPBody soapBody;
      try
      {
         soapBody = soapMessage.getSOAPBody();
         Node child = soapBody.getFirstChild();
         String childNamespace = child.getNamespaceURI();
         String childName = child.getLocalName();
         return new QName(childNamespace, childName);
      }
      catch (SOAPException e)
      {         
         if(trace)
            log.trace("Exception using backup method to get op name=",e);
      }
      return null;
   }
}