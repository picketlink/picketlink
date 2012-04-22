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
package org.picketlink.test.identity.federation.api.saml.v2.metadata;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.picketlink.identity.federation.api.saml.v2.metadata.KeyDescriptorMetaDataBuilder;
import org.picketlink.identity.federation.api.w3.xmldsig.KeyInfoBuilder;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.w3c.dom.Element;


/**
 * Unit Test the KeyDescriptorMetaDataBuilder
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class KeyDescriptorMetaDataBuilderUnitTestCase
{
   @Test
   public void testCreateKeyDescriptor()
   {
      Element keyInfo = KeyInfoBuilder.createKeyInfo("testKey");
      
      String algorithm = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
      
      KeyDescriptorType keyDescriptor = KeyDescriptorMetaDataBuilder.createKeyDescriptor(
            keyInfo, algorithm, 256, false, true);
      assertNotNull("Key Descriptor not null", keyDescriptor);
   }
   
}