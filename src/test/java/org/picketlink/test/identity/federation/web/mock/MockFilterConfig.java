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
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Mock Filter Config
 * @author Anil.Saldhana@redhat.com
 * @since Oct 8, 2009
 */
public class MockFilterConfig implements FilterConfig
{
   private Map<String,String> params = new HashMap<String,String>();
   private ServletContext context = null;
   
   public MockFilterConfig(ServletContext ctx)
   {
      this.context = ctx;
   }
   
   public void addInitParameter(String key, String val)
   {
      params.put(key, val);
   }
   
   public String getFilterName()
   {
      throw new RuntimeException("NYI");
   }

   public String getInitParameter(String arg0)
   {
      return params.get(arg0);
   }
 
   @SuppressWarnings("rawtypes")
   public Enumeration getInitParameterNames()
   {
      throw new RuntimeException("NYI");
   }

   public ServletContext getServletContext()
   {
      return context;
   }
}