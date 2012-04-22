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
package org.picketlink.identity.federation.web.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;

/**
 * Protocol Context based on HTTP
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public class HTTPContext implements ProtocolContext
{
   protected HttpServletRequest request;
   protected HttpServletResponse response; 
   
   protected ServletContext servletContext;
   
   public HTTPContext(HttpServletRequest httpReq, HttpServletResponse httpResp,
         ServletContext sctx)
   {
      this.request = httpReq;
      this.response = httpResp;
      this.servletContext = sctx;
   }

   public HttpServletRequest getRequest()
   {
      return request;
   }

   public HttpServletResponse getResponse()
   {
      return response;
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }
   
   //Setters
   
   public HTTPContext setRequest(HttpServletRequest req)
   {
      this.request = req;
      return this;
   }
   
   public HTTPContext setResponse(HttpServletResponse resp)
   {
      this.response = resp;
      return this;
   } 
   
   public HTTPContext setServletContext(ServletContext sctx)
   {
      this.servletContext = sctx;
      return this;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#serviceName()
    */
   public String serviceName()
   { 
      return null;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#tokenType()
    */
   public String tokenType()
   { 
      return null;
   }

   public QName getQName()
   { 
      return null;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#family()
    */
   public String family()
   { 
      return SecurityTokenProvider.FAMILY_TYPE.OPENID.toString();
   } 
}