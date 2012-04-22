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
package org.picketlink.test.identity.federation.core.saml.v2.metadata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.metadata.store.FileBasedMetadataConfigurationStore;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;

/**
 * Unit test the FileBasedMetadataConfigurationStore
 * @author Anil.Saldhana@redhat.com
 * @since Apr 28, 2009
 */
public class FileBasedMetadataConfigurationStoreUnitTestCase
{
   String pkgName = "org.picketlink.identity.federation.saml.v2.metadata";

   String id = "test";

   @Before
   public void setup() throws Exception
   {
      String userHome = System.getProperty("user.home");
      if (StringUtil.isNotNull(userHome) && "?".equals(userHome))
         System.setProperty("user.home", System.getProperty("user.dir"));
   }

   @Test
   public void testStore() throws Exception
   {
      SAMLParser parser = new SAMLParser();

      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream("saml2/metadata/idp-entitydescriptor.xml");
      assertNotNull("Inputstream not null", is);

      EntityDescriptorType edt = (EntityDescriptorType) parser.parse(is);
      assertNotNull(edt);
      FileBasedMetadataConfigurationStore fbd = new FileBasedMetadataConfigurationStore();
      fbd.persist(edt, id);

      EntityDescriptorType loaded = fbd.load(id);
      assertNotNull("loaded EntityDescriptorType not null", loaded);
      fbd.delete(id);

      try
      {
         fbd.load(id);
         fail("Did not delete the metadata persistent file");
      }
      catch (Exception t)
      {
         //pass
      }
   }

   @Test
   public void testTrustedProviders() throws Exception
   {
      FileBasedMetadataConfigurationStore fbd = new FileBasedMetadataConfigurationStore();
      Map<String, String> trustedProviders = new HashMap<String, String>();
      trustedProviders.put("idp1", "http://localhost:8080/idp1/metadata");
      trustedProviders.put("idp2", "http://localhost:8080/idp2/metadata");
      fbd.persistTrustedProviders(id, trustedProviders);

      //Lets get back
      Map<String, String> loadTP = fbd.loadTrustedProviders(id);
      assertNotNull("Loaded Trusted Providers not null", loadTP);

      assertTrue("idp1", loadTP.containsKey("idp1"));
      assertTrue("idp2", loadTP.containsKey("idp2"));
      assertTrue("size 2", loadTP.size() == 2);

      fbd.deleteTrustedProviders(id);
      try
      {
         fbd.loadTrustedProviders(id);
         fail("Did not delete the trusted providers file");
      }
      catch (Exception t)
      {
         //pass
      }
   }
}