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

import java.util.ArrayList;
import java.util.List;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.trust.jbossws.Constants;
import org.picketlink.trust.jbossws.Util;

/**
 * <p>
 * Handler that looks for a binary data that exists
 * in the HTTP payload either as a header or a cookie
 * based on configuration.
 * </p>
 * <p>
 * <b>Configuration:</b>
 * <p>
 * <i>System Properties:</i>
 * <ul>
 *   <li>binary.http.header: http header name. Can be a comma separated set of names</li>
 *   <li>binary.http.cookie: http cookie name</li>
 *   <li>binary.http.encodingType: attribute value of the EncodingType attribute</li>
 *   <li>binary.http.valueType: attribute value of the ValueType attribute</li>
 *   <li>binary.http.valueType.namespace: namespace for the ValueType attribute</li>
 *   <li>binary.http.valueType.prefix: namespace for the ValueType attribute</li>
 *   <li>binary.http.cleanToken: true or false dependending on whether the binary token has to be cleaned</li>
 * </ul>
 * <i>Setters:</i>
 * <p> Please see the see also section. </p>
 * 
 * @see #setHttpHeaderName(String)
 * @see #setHttpCookieName(String)
 * @see #setEncodingType(String)
 * @see #setValueType(String)
 * @see #setValueTypeNamespace(String)
 * @see #setValueTypePrefix(String)
 * @see #setCleanToken(boolean)
 * </p>
 * </p>
 * @author Anil.Saldhana@redhat.com
 * @since Apr 5, 2011
 */
public class BinaryTokenHandler extends AbstractPicketLinkTrustHandler
{
   /**
    * The HTTP header name that this token looks for. Either this or the httpCookieName should be set.
    */
   private String httpHeaderName = SecurityActions.getSystemProperty("binary.http.header", null);
   
   /**
    * The HTTP cookie name that this token looks for. Either this or the httpHeaderName should be set.
    */
   private String httpCookieName = SecurityActions.getSystemProperty("binary.http.cookie", null);
    
   /**
    * Attribute value for the EncodingType attribute
    */
   private String encodingType = SecurityActions.getSystemProperty("binary.http.encodingType", 
           "http://docs.oasis-open.org/wss/2004/01/ oasis-200401-wss-soap-message-security-1.0#Base64Binary");
   
   /**
    * Attribute value for the ValueType attribute
    */
   private String valueType = SecurityActions.getSystemProperty("binary.http.valueType", null);
   
   /**
    * Namespace for the ValueType. Can be null. If null, then a separate namespace is not added.
    */
   private String valueTypeNamespace = SecurityActions.getSystemProperty("binary.http.valueType.namespace", null);
   
   /**
    * Prefix for the ValueType. Can be null. 
    */
   private String valueTypePrefix = SecurityActions.getSystemProperty("binary.http.valueType.prefix", null);
   
   /**
    * Some binary tokens need to be cleaned. This handler just cleans upto the first blank space and discards before that.
    */
   private boolean cleanToken = Boolean.parseBoolean(SecurityActions.getSystemProperty("binary.http.cleanToken", "false"));
   
   private SOAPFactory factory = null;
   
   /**
    * <p> Set the EncodingType value.</p>
    * <p> Alternatively, set the system property "binary.http.encodingType"</p>
    * 
    * @param binaryEncodingType
    */
   public void setEncodingType(String binaryEncodingType)
   {
      this.encodingType = binaryEncodingType;
   }

   /**
    * <p> Set the Value type</p>
    * <p> Alternatively, set the system property "binary.http.valueType"</p>
    * 
    * @param binaryValueType
    */
   public void setValueType(String binaryValueType)
   {
      this.valueType = binaryValueType;
   }

   /**
    * <p> Set the ValueType Namespace </p>
    * <p> Alternatively, set the system property "binary.http.valueType.namespace"</p>
    * 
    * @param binaryValueNamespace
    */
   public void setValueTypeNamespace(String binaryValueNamespace)
   {
      this.valueTypeNamespace = binaryValueNamespace;
   }

   /**
    * <p> Set the Value Type Prefix </p>
    * <p> Alternatively, set the system property "binary.http.valueType.prefix" </p>
    * 
    * @param binaryValuePrefix
    */
   public void setValueTypePrefix(String binaryValuePrefix)
   {
      this.valueTypePrefix = binaryValuePrefix;
   }

   /**
    * <p>
    * Set the Http Header Name
    * </p>
    * <p>
    * Alternatively, set the system property: "binary.http.header"
    * </p>
    * 
    * @param http
    */
   public void setHttpHeaderName(String http)
   {
      httpHeaderName = http;
   }

   /**
    * <p>
    * Set the Http Cookie Name
    * </p>
    * <p>
    * Alternatively, set the system property: ""binary.http.cookie"
    * </p>
    * 
    * @param http
    */
   public void setHttpCookieName(String http)
   {
      httpCookieName = http;
   }
   
   /**
    * <p>
    * Should we not clean the extracted binary token.
    * </p>
    * <p>
    * Alternatively, set the system property: "binary.http.cleanToken"
    * </p>
    * 
    * @param clean
    */
   public void setCleanToken( boolean clean)
   {
      this.cleanToken = clean;
   }
   
   @Override
   protected boolean handleOutbound(MessageContext msgContext)
   { 
      if(trace)
      {
         log.trace("Handling Outbound Message");
      }
      
      if( httpHeaderName == null && httpCookieName == null )
         throw new RuntimeException(ErrorCodes.INJECTED_VALUE_MISSING + "Either httpHeaderName or httpCookieName should be set" );
      
      HttpServletRequest servletRequest = getHttpRequest(msgContext);
      if( servletRequest == null )
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Unable to proceed as Http request is null"); 
       
      String token = getTokenValue(servletRequest);
      if(token==null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Null Token");
      SOAPElement security = null;
      try
      {
         security = create(token);
      }
      catch (SOAPException e)
      {  
         log.error("Unable to create binary token", e);
      }
      if( security == null)
      {
         log.warn("Was not able to create security token. Just sending message without binary token");
         return true;
      }
      SOAPMessage sm = ((SOAPMessageContext)msgContext).getMessage();
      SOAPEnvelope envelope;
      try
      {
         envelope = sm.getSOAPPart().getEnvelope();
         SOAPHeader header = (SOAPHeader)Util.
         findElement(envelope, new QName(envelope.getNamespaceURI(), "Header"));
         if (header == null)
         {
            header = (SOAPHeader)envelope.getOwnerDocument().createElementNS(
                  envelope.getNamespaceURI(), envelope.getPrefix() + ":Header");
            envelope.insertBefore(header, envelope.getFirstChild());
         }
         header.addChildElement(security);
      }
      catch (SOAPException e)
      {  
         log.error("Unable to create WSSE Binary Header::",e);
      }
      if( trace)
      {
         log.trace("SOAP Message=");
         try
         {
            sm.writeTo(System.out);
         }
         catch (Exception ignore)
         {    
            log.trace("Exception tracing out SOAP Message", ignore);
         }
      }
      return true; 
   } 
   
   /**
    * Get the {@link HttpServletRequest} from the {@link MessageContext}
    * @param msgContext
    * @return
    */
   private HttpServletRequest getHttpRequest(MessageContext msgContext)
   {
      HttpServletRequest request = (HttpServletRequest) msgContext.get(MessageContext.SERVLET_REQUEST);
      if( request == null)
      {
         try
         {
            request = (HttpServletRequest) PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
         }
         catch (PolicyContextException e)
         { 
            throw new RuntimeException(e);
         }
      }
      return request;
   }
   
   /**
    * Given the {@link HttpServletRequest}, look for the http header or
    * the cookie depending on the configuration
    * @param http
    * @return
    */
   private String getTokenValue(HttpServletRequest http)
   {
      if( httpHeaderName!= null && !httpHeaderName.isEmpty())
      {
         //Sometime the http header name can be a comma separated list of names
         if( httpHeaderName.contains(","))
         {
            List<String> headers = getHeaderNames(httpHeaderName);
            StringBuilder builder = new StringBuilder();
            for(String header: headers)
            {
               String value = getTokenValue(http, header);
               if(value != null)
                  builder.append(value);
            }
            String str = builder.toString();
            if(trace) log.trace("Header value has been identified:" + str );
            return clean(str);
         }
         else
         {
            String header = http.getHeader(httpHeaderName);
            if( header != null)
            {
               if(trace) log.trace("Header value has been identified:" + header);
               return clean(header);
            } 
         }
      } 
      if( httpCookieName != null && !httpCookieName.isEmpty())
      {
         Cookie[] cookies = http.getCookies();
         if( cookies != null )
         {
            for(Cookie cookie: cookies)
            {
               if(cookie.getName().equals(httpCookieName))
               {
                  if(trace) log.trace("Cookie value has been identified:" + cookie.getValue());
                  return clean(cookie.getValue());
               }
            }
         }
      }
      return null;
   }
   
   private String getTokenValue(HttpServletRequest http,String header)
   {
      String headerValue = http.getHeader(header);
      if( headerValue != null && !headerValue.isEmpty())
      {
         return clean(headerValue);
      }
      return null;
   }
   
   /**
    * Given a binary token, create a {@link SOAPElement}
    * @param token
    * @return
    * @throws SOAPException
    */
   private SOAPElement create(String token) throws SOAPException
   {
      if(factory == null)
         factory = SOAPFactory.newInstance();
      SOAPElement security = factory.createElement(Constants.WSSE_LOCAL, Constants.WSSE_PREFIX, Constants.WSSE_NS); 

      if (valueTypeNamespace != null)
      {
         security.addNamespaceDeclaration(valueTypePrefix, valueTypeNamespace);
      } 

      SOAPElement binarySecurityToken = factory.
      createElement(Constants.WSSE_BINARY_SECURITY_TOKEN, Constants.WSSE_PREFIX, Constants.WSSE_NS);
      binarySecurityToken.addTextNode(token);
      if( valueType != null && !valueType.isEmpty())
      {
         binarySecurityToken.setAttribute(Constants.WSSE_VALUE_TYPE, valueType);
      }
      if (encodingType != null)
      {
         binarySecurityToken.setAttribute(Constants.WSSE_ENCODING_TYPE, encodingType);
      }

      security.addChildElement(binarySecurityToken);
      return security; 
   }
   
   /**
    * Some 3rd party systems send in the binary token in the format Discardable<space>ValidToken
    * @param value
    * @return
    */
   private String clean(String value)
   {
      if( trace)
      {
         log.trace("Cleaning:"+value);
      }
      int i= -1;
      
      if( cleanToken)
      {
         value = value.trim();
         while((i = value.indexOf(' ')) != -1)
         {
             value = value.substring(i + 1);
         }        
      }
      if(trace)
      {
         log.trace("Cleaned:"+value);
      }
      return value;
   }
   
   private List<String> getHeaderNames(String str)
   {
      List<String> list = new ArrayList<String>();
      if(StringUtil.isNotNull(str))
      {
         list.addAll(StringUtil.tokenize(str));
      }
      return list;
   }
}