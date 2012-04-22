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
package org.picketlink.test.identity.federation.core.wstrust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.cert.Certificate;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import org.junit.Test;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.test.identity.federation.core.wstrust.PicketLinkSTSUnitTestCase.TestSTS;

/**
 * Unit test various aspects of the sts configuration
 * @author Anil.Saldhana@redhat.com
 * @since May 25, 2010
 */
public class PicketLinkSTSConfigUnitTestCase
{
   /**
    * Test the masking of passwords
    * @throws Exception
    */
   @Test
   public void testMaskedPassword() throws Exception
   {
      PicketLinkSTSUnitTestCase plstsTest = new PicketLinkSTSUnitTestCase();
      TestSTS sts = plstsTest.new TestSTS("sts/picketlink-sts-maskedpasswd.xml");

      STSConfiguration stsConfiguration = sts.getConfiguration();
      Certificate cert = stsConfiguration.getCertificate( "service1" );
      assertNotNull( "cert is not null", cert );

      cert =  stsConfiguration.getCertificate( "service2" );
      assertNotNull( "cert is not null", cert );
   }
   
   /**
    * Test the introduction of the CanonicalizationMethod attribute
    * on the STSType
    * @throws Exception
    */
   @Test
   public void testXMLDSigCanonicalization() throws Exception
   {
      PicketLinkSTSUnitTestCase plstsTest = new PicketLinkSTSUnitTestCase();
      TestSTS sts = plstsTest.new TestSTS("sts/picketlink-sts-xmldsig-Canonicalization.xml");

      STSConfiguration stsConfiguration = sts.getConfiguration();
      assertNotNull( "STS Configuration is not null", stsConfiguration ); 
      assertEquals( CanonicalizationMethod.EXCLUSIVE, stsConfiguration.getXMLDSigCanonicalizationMethod() );
   }
}