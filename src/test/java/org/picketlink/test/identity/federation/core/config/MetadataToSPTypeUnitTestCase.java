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
package org.picketlink.test.identity.federation.core.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.SPType;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.util.CoreConfigUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

/**
 * Given an IDP metadata, construct {@link SPType}
 * @author Anil.Saldhana@redhat.com
 * @since Feb 28, 2011
 */
public class MetadataToSPTypeUnitTestCase
{
   private final String idpMetadata = "saml2/metadata/testshib.org.idp-metadata.xml";

   @Test
   public void testMetadataToSP() throws Exception
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(idpMetadata);
      assertNotNull(is);
      SAMLParser parser = new SAMLParser();
      EntitiesDescriptorType entities = (EntitiesDescriptorType) parser.parse(is);
      assertNotNull(entities);

      SPType sp = CoreConfigUtil.getSPConfiguration((EntityDescriptorType) entities.getEntityDescriptor().get(0),
            JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get());
      assertNotNull(sp);
      assertEquals("https://idp.testshib.org/idp/profile/SAML2/POST/SSO", sp.getIdentityURL());
   }
}