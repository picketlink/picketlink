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

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock TCL
 * @author Anil.Saldhana@redhat.com
 * @since Oct 7, 2009
 */
public class MockCatalinaContextClassLoader extends URLClassLoader
{
   private String profile;

   private ClassLoader delegate;

   private final Map<String, InputStream> streams = new HashMap<String, InputStream>();

   public MockCatalinaContextClassLoader(URL[] urls)
   {
      super(urls);
   }

   public void setDelegate(ClassLoader tcl)
   {
      this.delegate = tcl;
   }

   public void setProfile(String profile)
   {
      this.profile = profile;
   }

   public void associate(String name, InputStream is)
   {
      this.streams.put(name, is);
   }

   @Override
   public InputStream getResourceAsStream(String name)
   {
      if (streams.containsKey(name))
         return streams.get(name);

      if (profile == null)
         throw new RuntimeException("null profile when seeking resource:" + name);
      InputStream is = delegate.getResourceAsStream(profile + "/" + name);
      if (is == null)
         is = super.getResourceAsStream(name);
      return is;
   }
}