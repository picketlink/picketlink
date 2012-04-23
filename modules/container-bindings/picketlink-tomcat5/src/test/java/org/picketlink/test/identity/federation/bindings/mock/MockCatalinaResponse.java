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
package org.picketlink.test.identity.federation.bindings.mock;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.connector.Response;

import javax.servlet.http.HttpServletResponse;

/**
 * Mock catalina response
 * @author Anil.Saldhana@redhat.com
 * @since Oct 20, 2009
 */
public class MockCatalinaResponse extends Response
{
   private Map<String, String> headers = new HashMap<String, String>();
   private int status;
   public String redirectString;
   private PrintWriter mywriter;

   @Override
   public void setCharacterEncoding(String charset)
   { 
   }

   @Override
   public void setHeader(String name, String value)
   {
      this.headers.put(name, value); 
   } 

   @Override
   public int getStatus()
   {
      return this.status;
   }

   @Override
   public void setStatus(int status)
   {
      this.status = status; 
   } 

   @Override
   public void sendRedirect(String arg0) throws IOException
   {
      this.redirectString = arg0;
   }

   @Override
   public boolean isCommitted()
   {
      return false;
   }

   @Override
   public boolean isAppCommitted()
   {
      boolean redirected = getStatus() == HttpServletResponse.SC_MOVED_TEMPORARILY;
      return redirected;
   }

   public void setWriter(Writer w)
   {
      this.mywriter = (PrintWriter) w;
   }
    

   @Override
   public PrintWriter getWriter() throws IOException
   {
      return this.mywriter;
   }

   @Override
   public void setContentLength(int length)
   { 
   }

   @Override
   public void setContentType(String arg0)
   { 
   }

   @Override
   public void recycle()
   { 
   }  
}