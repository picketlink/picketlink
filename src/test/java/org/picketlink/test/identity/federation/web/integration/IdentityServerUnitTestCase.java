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
package org.picketlink.test.identity.federation.web.integration;

import javax.servlet.http.HttpSessionEvent;

import junit.framework.TestCase;

import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

/**
 * Unit test the Identity Server
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */ 
public class IdentityServerUnitTestCase extends TestCase
{
   public void testActiveSessionCount()
   {
      IdentityServer server = new IdentityServer();
      assertEquals(0,server.getActiveSessionCount());

      MockHttpSession session = new MockHttpSession();
      session.setServletContext(new MockServletContext());
      HttpSessionEvent event = new HttpSessionEvent(session); 
      server.sessionCreated(event);
      assertEquals(1,server.getActiveSessionCount());
      
      server.sessionDestroyed(event);
      assertEquals(0,server.getActiveSessionCount());
      //6 sessions created and 1 destroyed
      server.sessionCreated(event);
      server.sessionCreated(event);
      server.sessionCreated(event);
      server.sessionCreated(event);
      server.sessionCreated(event);
      server.sessionCreated(event);
      
      server.sessionDestroyed(event);
      assertEquals(5,server.getActiveSessionCount());
   } 
}