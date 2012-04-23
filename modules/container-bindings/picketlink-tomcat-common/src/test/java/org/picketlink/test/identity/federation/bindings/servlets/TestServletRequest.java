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
package org.picketlink.test.identity.federation.bindings.servlets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

 
/**
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
@SuppressWarnings({ "rawtypes"})
public class TestServletRequest implements HttpServletRequest
{
   private BufferedInputStream is = null;
   
   public TestServletRequest(InputStream is)
   {
      super();
      this.is = new BufferedInputStream(is); 
   }

   public String getAuthType()
   { 
      return null;
   }

   public String getContextPath()
   { 
      return null;
   }

   public Cookie[] getCookies()
   { 
      return null;
   }

   public long getDateHeader(String name)
   { 
      return 0;
   }

   public String getHeader(String name)
   { 
      return null;
   }

   public Enumeration getHeaderNames()
   { 
      return null;
   }

   public Enumeration getHeaders(String name)
   { 
      return null;
   }

   public int getIntHeader(String name)
   { 
      return 0;
   }

   public String getMethod()
   { 
      return null;
   }

   public String getPathInfo()
   { 
      return null;
   }

   public String getPathTranslated()
   { 
      return null;
   }

   public String getQueryString()
   { 
      return null;
   }

   public String getRemoteUser()
   {
      return null;
   }

   public String getRequestURI()
   {
      return null;
   }

   public StringBuffer getRequestURL()
   {
      return null;
   }

   public String getRequestedSessionId()
   {
      return null;
   }

   public String getServletPath()
   {
      return null;
   }

   public HttpSession getSession()
   {
      return null;
   }

   public HttpSession getSession(boolean create)
   {
      return null;
   }

   public Principal getUserPrincipal()
   {
      return null;
   }

   public boolean isRequestedSessionIdFromCookie()
   {
      return false;
   }

   public boolean isRequestedSessionIdFromURL()
   {
      return false;
   }

   public boolean isRequestedSessionIdFromUrl()
   {
      return false;
   }

   public boolean isRequestedSessionIdValid()
   {
      return false;
   }

   public boolean isUserInRole(String role)
   {
      return false;
   }

   public Object getAttribute(String name)
   {
      return null;
   }

   public Enumeration getAttributeNames()
   {
      return null;
   }

   public String getCharacterEncoding()
   {
      return null;
   }

   public int getContentLength()
   {
      return 0;
   }

   public String getContentType()
   {
      return null;
   }

   public ServletInputStream getInputStream() throws IOException
   { 
      return new ServletInputStream()
      {
         @Override
         public int read() throws IOException
         {
            return is.read();
         }
      };
   }

   public String getLocalAddr()
   {
      return null;
   }

   public String getLocalName()
   {
      return null;
   }

   public int getLocalPort()
   {
      return 0;
   }

   public Locale getLocale()
   {
      return null;
   }

   public Enumeration getLocales()
   {
      return null;
   }

   public String getParameter(String name)
   {
      return null;
   }

   public Map getParameterMap()
   {
      return null;
   }

   public Enumeration getParameterNames()
   {
      return null;
   }

   public String[] getParameterValues(String name)
   {
      return null;
   }

   public String getProtocol()
   {
      return null;
   }

   public BufferedReader getReader() throws IOException
   { 
      return null;
   }

   public String getRealPath(String path)
   {
      return null;
   }

   public String getRemoteAddr()
   {
      return null;
   }

   public String getRemoteHost()
   {
      return null;
   }

   public int getRemotePort()
   {
      return 0;
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      return null;
   }

   public String getScheme()
   {
      return null;
   }

   public String getServerName()
   {
      return null;
   }

   public int getServerPort()
   {
      return 0;
   }

   public boolean isSecure()
   {
      return false;
   }

   public void removeAttribute(String name)
   {
   }

   public void setAttribute(String name, Object o)
   {
   }

   public void setCharacterEncoding(String env) throws UnsupportedEncodingException
   { 
   }  
}