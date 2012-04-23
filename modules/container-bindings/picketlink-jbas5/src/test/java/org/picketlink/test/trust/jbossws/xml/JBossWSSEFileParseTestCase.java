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
package org.picketlink.test.trust.jbossws.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.trust.jbossws.util.JBossWSSERoleExtractor;

/**
 * Unit test the parsing of the jboss-wsse.xml for the roles
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class JBossWSSEFileParseTestCase
{
   @Test
   public void testUnchecked() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      String file = "jbossws/jboss-wsse/jboss-wsse-unchecked.xml";
      InputStream is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      
      List<String> roles = JBossWSSERoleExtractor.getRoles(is, null, null);
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertEquals( "unchecked", roles.get(0));
   }
   
   @Test
   public void testRoles() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      String file = "jbossws/jboss-wsse/jboss-wsse-roles.xml";
      InputStream is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      
      List<String> roles = JBossWSSERoleExtractor.getRoles(is, null, null);
      assertNotNull(roles);
      assertEquals( 2, roles.size());
      assertTrue( roles.contains("friend"));
      assertTrue( roles.contains("family")); 
   }
   
   @Test
   public void testRolesForPort() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      String file = "jbossws/jboss-wsse/jboss-wsse-port-role.xml";
      InputStream is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      
      List<String> roles = JBossWSSERoleExtractor.getRoles(is, "TestPort", null);
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("Trader"));
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      roles = JBossWSSERoleExtractor.getRoles(is, "MaxiPort", null);
      assertNotNull(roles);
      assertEquals( 3, roles.size());
      assertTrue( roles.contains("Trader"));
      assertTrue( roles.contains("friend"));
      assertTrue( roles.contains("family"));
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      roles = JBossWSSERoleExtractor.getRoles(is, "NonExistingPort", null);
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("Trader"));
   }
   
   @Test
   public void testRolesForPortOps() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      String file = "jbossws/jboss-wsse/jboss-wsse-port-ops.xml";
      InputStream is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      
      List<String> roles = JBossWSSERoleExtractor.getRoles(is, "POJOBeanPort", "{http://ws.trust.test.picketlink.org/}echoUnchecked");
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("unchecked"));
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      roles = JBossWSSERoleExtractor.getRoles(is, "POJOBeanPort", "{http://ws.trust.test.picketlink.org/}echo");
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("JBossAdmin"));
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      roles = JBossWSSERoleExtractor.getRoles(is, "NonExistingPort", null);
      assertNotNull(roles);
      assertEquals( 2, roles.size());
      assertTrue( roles.contains("friend"));
      assertTrue( roles.contains("family"));
   }
   
   @Test
   public void testInvalidXML() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      String file = "jbossws/jboss-wsse/invalid-jboss-wsse-port-role.xml";
      InputStream is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      
      List<String> roles = JBossWSSERoleExtractor.getRoles(is, "TestPort", null);
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("Trader"));
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      try
      {
         roles = JBossWSSERoleExtractor.getRoles(is, "MaxiPort", null);
         fail( "Should have thrown exception"); 
      }
      catch( ProcessingException pe)
      {
         //pass
      }
      
      is = tcl.getResourceAsStream(file);
      assertNotNull(is);
      roles = JBossWSSERoleExtractor.getRoles(is, "NonExistingPort", null);
      assertNotNull(roles);
      assertEquals( 1, roles.size());
      assertTrue( roles.contains("Trader"));
   }
}