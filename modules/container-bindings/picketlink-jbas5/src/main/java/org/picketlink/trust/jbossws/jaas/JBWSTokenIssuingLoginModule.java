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
package org.picketlink.trust.jbossws.jaas;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;
import javax.security.auth.login.LoginException;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.Dispatch;
import javax.xml.ws.handler.Handler;

import org.jboss.logging.Logger;
import org.picketlink.identity.federation.bindings.jboss.subject.PicketLinkPrincipal;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig.Builder;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule;
import org.picketlink.trust.jbossws.PicketLinkDispatch;
import org.picketlink.trust.jbossws.handler.BinaryTokenHandler;
import org.picketlink.trust.jbossws.handler.SAML2Handler;

/**
 * A subclass of {@link STSIssuingLoginModule} that adds in JBoss WS specific
 * details
 * @author Anil.Saldhana@redhat.com
 * @since Apr 22, 2011
 */
public class JBWSTokenIssuingLoginModule extends STSIssuingLoginModule
{  
   /**
    * Key in the options to customize the WS-Addressing Issuer in the WS-T Call
    */
   public static final String WSA_ISSUER = "wsaIssuer";
   
   /**
    * Key in the options to customize the WS-Policy Applies To in the WS-T Call
    */
   public static final String WSP_APPIESTO = "wspAppliesTo";
   
   @Override
   protected Builder createBuilder()
   {  
      Builder builder = super.createBuilder();
      builder.wsaIssuer((String) options.get(WSA_ISSUER));
      builder.wspAppliesTo((String) options.get(WSP_APPIESTO));
      return builder;
   }

   @Override
   protected STSClient createWSTrustClient(STSClientConfig config)
   { 
      return new JBWSTokenClient(config,options);
   }  
   
   @SuppressWarnings("unchecked")
   @Override
   public boolean commit() throws LoginException
   {
      boolean result =  super.commit();
      if( result )
      {
         SamlCredential samlCredential = null;
         Set<Object> creds = subject.getPublicCredentials();
         for(Object cred: creds)
         {
            if( cred instanceof SamlCredential)
            {
               samlCredential = (SamlCredential) cred;
               break;
            } 
         }
         if(samlCredential == null)
            throw new LoginException(ErrorCodes.NULL_VALUE + "SamlCredential is not available in subject");
         Principal principal = new PicketLinkPrincipal("");
         if (super.isUseFirstPass())
         {
            this.sharedState.put("javax.security.auth.login.name", principal);
            super.sharedState.put("javax.security.auth.login.password", samlCredential);
         }

      }
      return result;
   } 

   public class JBWSTokenClient extends STSClient
   { 
      private Logger log = Logger.getLogger(JBWSTokenClient.class);
      private boolean trace = log.isTraceEnabled();
      
      public JBWSTokenClient()
      {
         super(); 
      }

      public JBWSTokenClient(STSClientConfig config)
      {
         super(config);  
      }
      
      @SuppressWarnings("rawtypes")
      public JBWSTokenClient(STSClientConfig config, Map<String,?> options)
      {
         super(config); 
         
         //Get pre-constructed Dispatch from super
         Dispatch<Source> dispatch = super.getDispatch();
          
         String overrideDispatchStr = (String) options.get("overrideDispatch");
         if( StringUtil.isNotNull(overrideDispatchStr))
         {
            boolean bool = Boolean.valueOf(overrideDispatchStr);
            if( bool )
            {
               dispatch = new PicketLinkDispatch(dispatch, (String) options.get("endpointAddress"));
               String useWSSE = (String) options.get("useWSSE");
               if( StringUtil.isNotNull(useWSSE) && useWSSE.equalsIgnoreCase("true"))
               {
                  ((PicketLinkDispatch)dispatch).setUseWSSE(true);
               }
            }
         }
         
         Binding binding = dispatch.getBinding();
         
         List<Handler> handlers = binding.getHandlerChain();
         
         String handlerStr = (String) options.get("handlerChain");
         
         if(StringUtil.isNotNull(handlerStr))
         {
            List<String> tokens = StringUtil.tokenize(handlerStr);
            for(String token: tokens)
            {
               if(token.equalsIgnoreCase("binary"))
               {
                  BinaryTokenHandler binaryTokenHandler = new BinaryTokenHandler();
                  handlers.add(binaryTokenHandler);
               }
               else if(token.equalsIgnoreCase("saml2"))
               {
                  SAML2Handler samlHandler = new SAML2Handler();
                  handlers.add(samlHandler);
               } 
               else
               {
                  ClassLoader cl = SecurityActions.getClassLoader(getClass());
                  try
                  {
                     handlers.add((Handler) cl.loadClass(token).newInstance());
                  }
                  catch (Exception e)
                  {
                     throw new RuntimeException(ErrorCodes.CANNOT_CREATE_INSTANCE + "Unable to instantiate handler:"+token, e);
                  }
               }
            }
         }

         binding.setHandlerChain(handlers);
         
         setDispatch(dispatch);
         
         String securityDomainForFactory = (String) options.get("securityDomainForFactory");
         if( StringUtil.isNotNull(securityDomainForFactory))
         {
            if(trace)
            {
               log.trace("We got security domain for domain ssl factory = " + securityDomainForFactory);
               log.trace("Setting it on the system property org.jboss.security.ssl.domain.name");   
            }
            String sslFactoryName = "org.jboss.security.ssl.JaasSecurityDomainSocketFactory";
            SecurityActions.setSystemProperty("org.jboss.security.ssl.domain.name", securityDomainForFactory);
            //StubExt.PROPERTY_SOCKET_FACTORY
            dispatch.getRequestContext().put( "org.jboss.ws.socketFactory", sslFactoryName); 
            
            //If we are using PL Dispatch. Then we need to set the SSL Socket Factory
            if( dispatch instanceof PicketLinkDispatch)
            { 
               ClassLoader cl = SecurityActions.getClassLoader(getClass());
               SSLSocketFactory socketFactory = null;
               if(cl != null)
               {
                  try
                  {
                     Class<?> clazz = cl.loadClass(sslFactoryName);
                     socketFactory = (SSLSocketFactory) clazz.newInstance(); 
                  }
                  catch(Exception e)
                  {
                     cl = SecurityActions.getContextClassLoader();
                     try
                     {
                        Class<?> clazz = cl.loadClass(sslFactoryName);
                        socketFactory = (SSLSocketFactory) clazz.newInstance();
                     }
                     catch (Exception e1)
                     {
                        throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION + "Unable to create SSL Socket Factory:",e1);
                     }
                  }
                  finally
                  {
                     if(socketFactory != null)
                     {
                        ((PicketLinkDispatch)dispatch).setSSLSocketFactory(socketFactory);
                     }
                     else
                        throw new RuntimeException(" We did not find SSL Socket Factory");
                  }
               }
               else
               {
                  if(trace)
                  {
                     log.trace("Classloader is null. Unable to set the SSLSocketFactory on PicketLinkDispatch");
                  }
               }
            }
         }
      }
   }
}