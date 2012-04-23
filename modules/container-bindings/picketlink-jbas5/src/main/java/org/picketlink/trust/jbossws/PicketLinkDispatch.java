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
package org.picketlink.trust.jbossws;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.core.util.StringUtil;

/**
 * <p>
 * A concrete implementation of {@code Dispatch}
 * that can be used as an alternative to the 
 * underlying JAXWS implementation.
 * </p>
 * <p>
 * This is used by setting the module option "overrideDispatch"
 * to true in the {@code JBWSTokenIssuingLoginModule}
 * </p> 
 * @author Anil.Saldhana@redhat.com
 * @since May 10, 2011
 */
public class PicketLinkDispatch implements Dispatch<Source>
{
   @SuppressWarnings("rawtypes")
   private Dispatch parent;
   private String endpoint;
   
   private boolean useWSSE = false;
   private SSLSocketFactory sslSocketFactory;

   @SuppressWarnings("rawtypes")
   public PicketLinkDispatch(Dispatch parent, String endpoint)
   {
      this.parent = parent;
      this.endpoint = endpoint;
   } 
   
   public void setUseWSSE(boolean val)
   {
      this.useWSSE = val;
   }
   
   public void setSSLSocketFactory( SSLSocketFactory ssl)
   {
      this.sslSocketFactory = ssl;
   }
   
   public Map<String, Object> getRequestContext()
   {
      return parent.getRequestContext();
   }

   public Map<String, Object> getResponseContext()
   { 
      return parent.getResponseContext();
   }

   public Binding getBinding()
   { 
      return parent.getBinding();
   }

   public EndpointReference getEndpointReference()
   { 
      return parent.getEndpointReference();
   }

   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz)
   { 
      return parent.getEndpointReference(clazz);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   public Source invoke(Source requestMessage)
   { 
      PLMessageContext msgContext = new PLMessageContext();
      msgContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
      
      /** The JACC PolicyContext key for the current Subject */
      String WEB_REQUEST_KEY = "javax.servlet.http.HttpServletRequest";

      HttpServletRequest request = null;
      try
      {
         request = (HttpServletRequest) PolicyContext.getContext(WEB_REQUEST_KEY);
      }
      catch (PolicyContextException e1)
      { 
         throw new RuntimeException(e1);
      }
      msgContext.put(MessageContext.SERVLET_REQUEST, request); 
      
      SOAPMessage soapMessage = null;
      try
      {
         soapMessage = SOAPUtil.create();
      }
      catch (SOAPException e2)
      {
         throw new RuntimeException(e2);
      }
      String userName = (String) parent.getRequestContext().get(BindingProvider.USERNAME_PROPERTY);
      String passwd = (String) parent.getRequestContext().get(BindingProvider.PASSWORD_PROPERTY);
      if( StringUtil.isNotNull(userName))
      {  
         try
         {
            if(useWSSE)
            {
               SOAPElement security = create(userName, passwd);
               soapMessage.getSOAPHeader().appendChild(security); 
            }
            else
            {
               String authorization = Base64.encodeBytes((userName+":"+passwd).getBytes());
               soapMessage.getMimeHeaders().addHeader("Authorization", "Basic "+authorization);
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      
      try
      {
         SOAPUtil.addData(requestMessage, soapMessage);
      }
      catch (SOAPException e1)
      {
         throw new RuntimeException(e1);
      }
      msgContext.setMessage(soapMessage);
      
      List<Handler> handlers = getBinding().getHandlerChain();
      for( Handler handler: handlers)
      { 
         boolean result = handler.handleMessage(msgContext);
         if( !result)
            throw new WebServiceException(ErrorCodes.PROCESSING_EXCEPTION + "Handler "+ handler.getClass() + " returned false");
      }
      
      if(sslSocketFactory != null)
      {
         HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
      }
      
      SOAPMessage response = null;
      try
      {
         SOAPConnectionFactory connectFactory = SOAPConnectionFactory.newInstance();
         SOAPConnection connection = connectFactory.createConnection();
         //Send it across the wire
         URL url = new URL(endpoint);
         
         response = connection.call(soapMessage, url);  
      }
      catch (Exception e)
      { 
         throw new RuntimeException(e);
      }
      
      try
      {
         return new DOMSource(SOAPUtil.getSOAPData(response));
      }
      catch (SOAPException e)
      {
         throw new RuntimeException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public Response<Source> invokeAsync(Source msg)
   { 
      return parent.invokeAsync(msg);
   }

   @SuppressWarnings("unchecked")
   public Future<?> invokeAsync(Source msg, AsyncHandler<Source> handler)
   {
      return parent.invokeAsync(msg, handler);
   }

   @SuppressWarnings("unchecked")
   public void invokeOneWay(Source msg)
   {
      parent.invokeOneWay(msg);
   }
   
   public static class PLMessageContext implements MessageContext, SOAPMessageContext
   {
      private Map<String,Object> map = new HashMap<String,Object>();
      private Map<String,Scope> scopeMap = new HashMap<String, MessageContext.Scope>();
      
      private Map<String,Object> properties = new HashMap<String, Object>();
      
      private SOAPMessage message;

      public int size()
      { 
         return map.size();
      }

      public boolean isEmpty()
      { 
         return map.isEmpty();
      }

      public boolean containsKey(Object key)
      { 
         return map.containsKey(key);
      }

      public boolean containsValue(Object value)
      { 
         return map.containsValue(value);
      }

      public Object get(Object key)
      {
         return map.get(key);
      }

      public Object put(String key, Object value)
      {
         return map.put(key, value);
      }

      public Object remove(Object key)
      {
         return map.remove(key);
      }

      public void putAll(Map<? extends String, ? extends Object> m)
      {  
         map.putAll(m);
      }

      public void clear()
      {  
         map.clear();
      }

      public Set<String> keySet()
      {
         return map.keySet();
      }

      public Collection<Object> values()
      {
         return map.values();
      }

      public Set<java.util.Map.Entry<String, Object>> entrySet()
      { 
         return map.entrySet();
      }

      public void setScope(String name, Scope scope)
      {  
         this.scopeMap.put(name, scope);
      }

      public Scope getScope(String name)
      {
         return scopeMap.get(name);
      }

      public boolean containsProperty(String name)
      { 
         return properties.containsKey(name);
      }

      public Object getProperty(String name)
      { 
         return properties.get(name);
      }

      @SuppressWarnings("rawtypes")
      public Iterator getPropertyNames()
      {
         return properties.keySet().iterator();
      }

      public void removeProperty(String name)
      { 
         properties.remove(name);
      }

      public void setProperty(String name, Object value)
      {   
         properties.put(name, value);
      }

      public SOAPMessage getMessage()
      { 
         return message;
      }

      public void setMessage(SOAPMessage message)
      {
         this.message = message;
      }

      public Object[] getHeaders(QName header, JAXBContext context, boolean allRoles)
      { 
         throw new RuntimeException();
      }

      public Set<String> getRoles()
      { 
         throw new RuntimeException();
      }
   }
   
   
   /**
    * Given username and pass, create a {@link SOAPElement} for WSSE UsernameToken Profile
    * @param token
    * @return
    * @throws SOAPException
    */
   private SOAPElement create(String userName, String pass) throws SOAPException
   { 
      SOAPFactory factory = SOAPFactory.newInstance();
      SOAPElement security = factory.createElement(Constants.WSSE_LOCAL, Constants.WSSE_PREFIX, Constants.WSSE_NS); 
      security.addNamespaceDeclaration(Constants.WSU_PREFIX, Constants.WSU_NS);
       

      SOAPElement userNameToken = factory.createElement(Constants.WSSE_USERNAME_TOKEN, Constants.WSSE_PREFIX, Constants.WSSE_NS);
      userNameToken.addAttribute(new QName(Constants.WSU_NS,"Id",Constants.WSU_PREFIX), IDGenerator.create("token-"));
      
      SOAPElement un = factory.createElement(Constants.WSSE_USERNAME, Constants.WSSE_PREFIX, Constants.WSSE_NS);
      un.addTextNode(userName);
      userNameToken.addChildElement(un);
      
      SOAPElement passElement = factory.createElement(Constants.WSSE_PASSWORD, Constants.WSSE_PREFIX, Constants.WSSE_NS); 
      passElement.addAttribute(new QName("Type"), Constants.WSSE_PASSWORD_TEXT_NS);
      passElement.addTextNode(pass);
      userNameToken.addChildElement(passElement);
      
      
      security.addChildElement(userNameToken);
      return security; 
   }
}