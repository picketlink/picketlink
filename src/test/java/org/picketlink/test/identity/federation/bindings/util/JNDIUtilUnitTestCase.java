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
package org.picketlink.test.identity.federation.bindings.util;

import junit.framework.TestCase;


/**
 * JNDI Util test case
 * @author Anil.Saldhana@redhat.com
 * @since Apr 27, 2009
 */
public class JNDIUtilUnitTestCase extends TestCase
{
//   @SuppressWarnings("unchecked")
   public void testJNDIConnection() throws Exception
   {
      /*Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, "ldap://localhost:389");
      env.put(Context.SECURITY_AUTHENTICATION,"simple");
      env.put(Context.SECURITY_PRINCIPAL,"cn=Manager,dc=jboss,dc=org"); 
      env.put(Context.SECURITY_CREDENTIALS,"test");     
      DirContext ctx = new InitialDirContext(env);
      
      //Read stuff
      Object obj = ctx.lookup("ou=identity,dc=jboss,dc=org");
      assertNotNull("Obj is not null", obj);
      assertTrue(obj instanceof LdapContext);
      
      obj = ctx.lookup("ou=idp,ou=identity,dc=jboss,dc=org");
      assertNotNull("Obj is not null", obj);
      assertTrue(obj instanceof LdapContext);
      
      SearchControls sc = new SearchControls();
      sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
      NamingEnumeration<SearchResult> ne = ctx.search("ou=idp,ou=identity,dc=jboss,dc=org", 
            "(ou=providers)", sc);
      
      while(ne.hasMore())
      {
         SearchResult sr = ne.next();
         System.out.println(sr.toString());
      }*/
   }
}