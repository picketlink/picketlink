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
package org.picketlink.identity.federation.core.saml.md.providers;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Privileged Blocks
 * @author Anil.Saldhana@redhat.com
 * @since Mar 17, 2009
 */
class SecurityActions
{
   static void setSystemProperty(final String key, final String value)
   {
      AccessController.doPrivileged(new PrivilegedAction<Object>()
      {
         public Object run()
         {
            System.setProperty(key, value);
            return null;
         }
      });
   }

   static Class<?> loadClass(final Class<?> theClass, final String fqn)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Class<?>>()
      {
         public Class<?> run()
         {
            ClassLoader classLoader = theClass.getClassLoader();

            Class<?> clazz = loadClass(classLoader, fqn);
            if (clazz == null)
            {
               classLoader = Thread.currentThread().getContextClassLoader();
               clazz = loadClass(classLoader, fqn);
            }
            return clazz;
         }
      });
   }

   static Class<?> loadClass(final ClassLoader cl, final String fqn)
   {
      return AccessController.doPrivileged(new PrivilegedAction<Class<?>>()
      {
         public Class<?> run()
         {
            try
            {
               return cl.loadClass(fqn);
            }
            catch (ClassNotFoundException e)
            {
            }
            return null;
         }
      });
   }
}