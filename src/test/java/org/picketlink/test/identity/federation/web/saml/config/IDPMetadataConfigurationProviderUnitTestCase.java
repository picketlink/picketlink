/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.test.identity.federation.web.saml.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.web.config.IDPMetadataConfigurationProvider;

/**
 * Unit test the {@link IDPMetadataConfigurationProvider}
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class IDPMetadataConfigurationProviderUnitTestCase
{
   @Test
   public void testIDPType() throws ProcessingException
   {
      IDPMetadataConfigurationProvider provider = new IDPMetadataConfigurationProvider();
      IDPType idp = provider.getIDPConfiguration();
      assertNotNull(idp);
      assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SSO", idp.getIdentityURL());
   }

   @Test
   public void testIDPTypeWithConfig() throws Exception
   {
      IDPMetadataConfigurationProvider provider = new IDPMetadataConfigurationProvider();
      InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("saml2/logout/idp/WEB-INF/picketlink-idfed.xml");
      assertNotNull(is);
      provider.setConfigFile(is);

      IDPType idp = provider.getIDPConfiguration();
      assertNotNull(idp);
      assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SSO", idp.getIdentityURL());

      TrustType trust = idp.getTrust();
      assertNotNull(trust);
      assertEquals("localhost,jboss.com,jboss.org", trust.getDomains());

      assertEquals("org.picketlink.identity.federation.core.impl.EmptyAttributeManager", idp.getAttributeManager());
   }
}