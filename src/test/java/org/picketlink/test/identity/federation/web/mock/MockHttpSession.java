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
package org.picketlink.test.identity.federation.web.mock;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * Mock HttpSession
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */ 
@SuppressWarnings({"deprecation", "unchecked", "rawtypes"}) 
public class MockHttpSession implements HttpSession
{
   private boolean valid = true;
   
   private Map<String,Object> attribs = new HashMap<String,Object>();

   private String id = UUID.randomUUID().toString();

   private ServletContext context;
   
   public boolean isInvalidated()
   {
      return valid == false;
   }
   
   public Object getAttribute(String arg0)
   {
      return attribs.get(arg0);
   }

   public Enumeration getAttributeNames()
   {
      return new Enumeration() 
      {
         private Iterator iter = attribs.entrySet().iterator();
         
         public boolean hasMoreElements()
         {
            return iter.hasNext();
         }

         public Object nextElement()
         {
            Entry<String,Object> entry =  (Entry<String, Object>) iter.next();
            return entry.getValue();
         }
      };
   }

   public long getCreationTime()
   {  
      return 0;
   }

   public String getId()
   {   
      return id;
   }

   public long getLastAccessedTime()
   {      
      return 0;
   }

   public int getMaxInactiveInterval()
   {      
      return 0;
   }

   public void setServletContext(ServletContext servletContext)
   {
      this.context = servletContext;
   }
   
   public ServletContext getServletContext()
   {     
      return this.context;
   }

   public HttpSessionContext getSessionContext()
   {
      
      throw new RuntimeException("NYI");
   }

   public Object getValue(String arg0)
   { 
      throw new RuntimeException("NYI");
   }

   public String[] getValueNames()
   {  
      throw new RuntimeException("NYI");
   }

   public void invalidate()
   {
      this.valid = false;
   }

   public boolean isNew()
   {  
      if(this.valid == false)
         throw new IllegalStateException("Session already invalidated");
      
      return false;
   }

   public void putValue(String arg0, Object arg1)
   {
      if(this.valid == false)
         throw new IllegalStateException("Session already invalidated");
   }

   public void removeAttribute(String arg0)
   {
      if(this.valid == false)
         throw new IllegalStateException("Session already invalidated");
      
      this.attribs.remove(arg0);
   }

   public void removeValue(String arg0)
   {
      if(this.valid == false)
         throw new IllegalStateException("Session already invalidated");      
   }

   public void setAttribute(String arg0, Object arg1)
   {
      if(this.valid == false)
      throw new IllegalStateException("Session already invalidated");
   
      this.attribs.put(arg0, arg1); 
   }

   public void setMaxInactiveInterval(int arg0)
   { 
   }
}