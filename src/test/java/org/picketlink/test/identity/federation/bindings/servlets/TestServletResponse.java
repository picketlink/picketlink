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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
public class TestServletResponse implements HttpServletResponse
{
   private BufferedOutputStream bos = null;
   
   public TestServletResponse(OutputStream os)
   {
      super();
      bos = new BufferedOutputStream(os);
   }

   public void addCookie(Cookie cookie)
   { 
   }

   public void addDateHeader(String name, long date)
   { 
   }

   public void addHeader(String name, String value)
   {  
   }

   public void addIntHeader(String name, int value)
   {  
   }

   public boolean containsHeader(String name)
   {  
      return false;
   }

   public String encodeRedirectURL(String url)
   { 
      return null;
   }

   public String encodeRedirectUrl(String url)
   {
      return null;
   }

   public String encodeURL(String url)
   {
      return null;
   }

   public String encodeUrl(String url)
   {
      return null;
   }

   public void sendError(int sc) throws IOException
   {
   }

   public void sendError(int sc, String msg) throws IOException
   {
   }

   public void sendRedirect(String location) throws IOException
   {
   }

   public void setDateHeader(String name, long date)
   {
   }

   public void setHeader(String name, String value)
   {
   }

   public void setIntHeader(String name, int value)
   {
   }

   public void setStatus(int sc)
   {
   }

   public void setStatus(int sc, String sm)
   {
   }

   public void flushBuffer() throws IOException
   {
      this.bos.flush();
   }

   public int getBufferSize()
   {
      return 0;
   }

   public String getCharacterEncoding()
   {
      return null;
   }

   public String getContentType()
   {
      return null;
   }

   public Locale getLocale()
   {
      return null;
   }

   public ServletOutputStream getOutputStream() throws IOException
   { 
      bos.flush();
      return new ServletOutputStream()
      {
         @Override
         public void write(int b) throws IOException
         {
            bos.write(b);
         }
      };
   }

   public PrintWriter getWriter() throws IOException
   { 
      return null;
   }

   public boolean isCommitted()
   { 
      return false;
   }

   public void reset()
   {
   }

   public void resetBuffer()
   {
   }

   public void setBufferSize(int size)
   {
   }

   public void setCharacterEncoding(String charset)
   {
   }

   public void setContentLength(int len)
   {
   }

   public void setContentType(String type)
   {
   }

   public void setLocale(Locale loc)
   {
   } 
}
