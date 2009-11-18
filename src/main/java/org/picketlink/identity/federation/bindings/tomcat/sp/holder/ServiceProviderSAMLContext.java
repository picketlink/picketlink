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
package org.picketlink.identity.federation.bindings.tomcat.sp.holder;

import java.util.List;

/**
 * A context of username/roles to be used by login modules
 * @author Anil.Saldhana@redhat.com
 * @since Feb 13, 2009
 */
public class ServiceProviderSAMLContext
{
   public static final String EMPTY_PASSWORD = "EMPTY_STR";
   
   private static ThreadLocal<String> username = new ThreadLocal<String>();
   private static ThreadLocal<List<String>> userRoles = new ThreadLocal<List<String>>();
   
   public static void push(String user, List<String> roles)
   {
      username.set(user);
      userRoles.set(roles);
   }
   
   public static void clear()
   {
      username.remove();
      userRoles.remove();
   }

   public static String getUserName()
   {
      return username.get();
   }
   
   public static List<String> getRoles()
   {
      return userRoles.get();
   }
}