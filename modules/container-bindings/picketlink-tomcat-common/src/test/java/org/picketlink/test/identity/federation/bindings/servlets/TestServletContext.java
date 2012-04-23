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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

 
/**
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
@SuppressWarnings({ "rawtypes"})
public class TestServletContext implements ServletContext
{
   private HashMap<String,String> params = new HashMap<String,String>();

   public TestServletContext(HashMap<String,String> map)
   {
      this.params = map;
   }
   
   public Object getAttribute(String name)
   { 
      return null;
   }

   public Enumeration getAttributeNames()
   {   
      return null;
   }

   public ServletContext getContext(String uripath)
   {   
      return null;
   }

   public String getContextPath()
   {   
      return null;
   }

   public String getInitParameter(String name)
   {   
      return this.params.get(name);
   }

   public Enumeration getInitParameterNames()
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 0;
   }

   public String getMimeType(String file)
   {
      return null;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public RequestDispatcher getNamedDispatcher(String name)
   {
      return null;
   }

   public String getRealPath(String path)
   {
      return null;
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      return null;
   }

   public URL getResource(String path) throws MalformedURLException
   {
      return null;
   }

   public InputStream getResourceAsStream(String path)
   {
      return null;
   }

   public Set getResourcePaths(String path)
   {
      return null;
   }

   public String getServerInfo()
   {
      return null;
   }

   public Servlet getServlet(String name) throws ServletException
   {
      return null;
   }

   public String getServletContextName()
   {
      return null;
   }
 
   public Enumeration getServletNames()
   {
      return null;
   }
 
   public Enumeration getServlets()
   {
      return null;
   }

   public void log(String msg)
   {
   }

   public void log(Exception exception, String msg)
   {
   }

   public void log(String message, Throwable throwable)
   {
   }

   public void removeAttribute(String name)
   {
   }

   public void setAttribute(String name, Object object)
   {
   }
}