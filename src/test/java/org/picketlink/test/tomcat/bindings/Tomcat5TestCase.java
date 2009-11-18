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
package org.picketlink.test.tomcat.bindings;

import junit.framework.TestCase;

import org.picketlink.test.tomcat.helpers.Tomcat5Embedded;

/**
 * Tomcat5 Embedded 
 * @author Anil.Saldhana@redhat.com
 * @since Nov 1, 2008
 */
public class Tomcat5TestCase extends TestCase
{ 
   boolean enable = false;
   
   public void testTomcat5() throws Exception
   {
      if(enable)
      {
         Tomcat5Embedded emb = new Tomcat5Embedded();
         emb.setHomePath("target/tomcat");
         emb.startServer();
         Thread.sleep(2000);
         assertTrue("Tomcat5 started", emb.hasStarted());

         // emb.createContext("target/../identity-samples/samples/employee/target/employee.war");

         emb.stopServer();
         Thread.sleep(1000);
         assertTrue(emb.hasStopped());
      }  
   }
}