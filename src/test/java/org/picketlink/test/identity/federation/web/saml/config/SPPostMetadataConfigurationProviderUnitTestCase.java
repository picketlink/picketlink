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
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.web.config.SPPostMetadataConfigurationProvider;

/**
 * Unit test the {@link SPPostMetadataConfigurationProvider}
 * @author Anil Saldhana
 * @since Feb 15, 2012
 */
public class SPPostMetadataConfigurationProviderUnitTestCase
{
   @Test
   public void testSPType() throws ProcessingException
   {
      SPPostMetadataConfigurationProvider provider = new SPPostMetadataConfigurationProvider();
      SPType sp = provider.getSPConfiguration();
      assertNotNull(sp);
      assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", sp.getServiceURL());
   }

   @Test
   public void testSPTypeWithConfig() throws Exception
   {
      SPPostMetadataConfigurationProvider provider = new SPPostMetadataConfigurationProvider();
      InputStream is = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("saml2/logout/sp/sales/WEB-INF/picketlink-idfed.xml");
      assertNotNull(is);
      provider.setConfigFile(is);

      SPType sp = provider.getSPConfiguration();
      assertNotNull(sp);
      assertEquals("https://sp.testshib.org/Shibboleth.sso/SAML2/POST", sp.getServiceURL());
   }

}