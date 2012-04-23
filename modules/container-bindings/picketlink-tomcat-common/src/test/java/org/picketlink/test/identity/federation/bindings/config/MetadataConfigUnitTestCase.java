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
package org.picketlink.test.identity.federation.bindings.config;

import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.config.IDPType;
import org.picketlink.identity.federation.core.config.KeyValueType;
import org.picketlink.identity.federation.core.config.MetadataProviderType;
import org.picketlink.identity.federation.core.config.TrustType;
import org.picketlink.identity.federation.core.parsers.config.SAMLConfigParser;


/**
 * Config for the SAMLv2 Metadata Profile
 * @author Anil.Saldhana@redhat.com
 * @since Apr 22, 2009
 */
public class MetadataConfigUnitTestCase extends TestCase
{
   String config = "config/test-metadata-config-";
    
   public void testMetadata() throws Exception
   {
      Object object = this.unmarshall(config + "1.xml");
      assertNotNull("IDP is not null", object); 
      IDPType idp =  (IDPType) object;
      assertEquals("20000", 20000L, idp.getAssertionValidity());
      assertEquals("somefqn", idp.getRoleGenerator());

      TrustType trust = idp.getTrust();
      assertNotNull("Trust is not null", trust);
      String domains = trust.getDomains();
      assertTrue("localhost trusted", domains.indexOf("localhost") > -1);
      assertTrue("jboss.com trusted", domains.indexOf("jboss.com") > -1);
      
      MetadataProviderType metaDataProvider = idp.getMetaDataProvider();
      assertNotNull("MetadataProvider is not null", metaDataProvider);
      assertEquals("org.jboss.test.somefqn", metaDataProvider.getClassName());
      
      List<KeyValueType> keyValues = metaDataProvider.getOption();
      assertTrue(1 == keyValues.size());
      KeyValueType kvt = keyValues.get(0);
      assertEquals("FileName", kvt.getKey());
      assertEquals("myfile", kvt.getValue());
   }
   
   private Object unmarshall(String configFile) throws Exception
   {
      //String schema = PicketLinkFederationConstants.SCHEMA_IDFED;

      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream(configFile);
      assertNotNull("Inputstream not null", is);

      return (new SAMLConfigParser()).parse( is );
   }
}