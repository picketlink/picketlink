/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.core.wstrust;

import java.util.HashMap;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.identity.federation.core.wstrust.StandardRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustServiceFactory;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider;

/**
 * <p>
 * This {@code TestCase} tests the behavior of the {@code WSTrustServiceFactory} class.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustServiceFactoryUnitTestCase extends TestCase
{

   /**
    * <p>
    * Tests the creation of a {@code WSTrustRequestHandler} instance.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testCreateRequestHandler() throws Exception
   {
      STSConfiguration config = new PicketLinkSTSConfiguration();
      WSTrustServiceFactory factory = WSTrustServiceFactory.getInstance();

      // tests the creation of the request handler.
      WSTrustRequestHandler handler = factory.createRequestHandler(
            "org.picketlink.identity.federation.core.wstrust.StandardRequestHandler", config);
      assertNotNull("Unexpected null request handler", handler);
      assertTrue("Unexpected request handler type", handler instanceof StandardRequestHandler);

      // try to create an invalid instance of request handler.
      try
      {
         factory.createRequestHandler("InvalidHandler", config);
         fail("An exception should have been raised");
      }
      catch (RuntimeException re)
      {
         String msg = re.getCause().getMessage();
         assertTrue(msg.contains("Class Not Loaded"));
      }
   }

   /**
    * <p>
    * Tests the creation of {@code SecurityTokenProvider}s.
    * </p>
    * 
    * @throws Exception if an error occurs while running the test.
    */
   public void testCreateTokenProvider() throws Exception
   {
      WSTrustServiceFactory factory = WSTrustServiceFactory.getInstance();
      SecurityTokenProvider provider = factory.createTokenProvider(
            "org.picketlink.test.identity.federation.core.wstrust.SpecialTokenProvider", null);
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SpecialTokenProvider);
      provider = factory.createTokenProvider(
            "org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider",
            new HashMap<String, String>());
      assertNotNull("Unexpected null token provider", provider);
      assertTrue("Unexpected token provider type", provider instanceof SAML20TokenProvider);

      // try to create an invalid token provider.
      try
      {
         factory.createTokenProvider("InvalidTokenProvider", null);
         fail("An exception should have been raised");
      }
      catch (RuntimeException re)
      {
         String msg = re.getCause().getMessage();
         assertTrue(msg.contains("Class Not Loaded"));
      }
   }
}
