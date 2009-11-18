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

import java.security.Principal;

import org.apache.catalina.realm.RealmBase;

/**
 * Mock Tomcat Realm
 * @author Anil.Saldhana@redhat.com
 * @since Oct 21, 2009
 */
public class MockCatalinaRealm extends RealmBase
{
   private String name;
   private String pass;
   private Principal principal;

   public MockCatalinaRealm(String name, String pass, Principal p)
   {
      this.name = name;
      this.pass = pass;
      this.principal = p;
   }

   @Override
   protected String getName()
   { 
      return name;
   }

   @Override
   protected String getPassword(String arg0)
   { 
      return pass;
   }

   @Override
   protected Principal getPrincipal(String arg0)
   { 
      return principal;
   }

   @Override
   public Principal authenticate(String arg0, String arg1)
   {
      return principal;
   } 
}