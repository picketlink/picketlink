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

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.junit.Test;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;
import org.picketlink.identity.federation.core.util.PBEUtils;

/**
 * Test the masking of the password using {@code PBEUtils}
 * @author Anil.Saldhana@redhat.com
 * @since May 25, 2010
 */
public class PBEUtilsUnitTestCase
{
   @Test
   public void testPBE() throws Exception
   {
      String pass = "testpass";

      String salt = "18273645";
      int iterationCount = 56;

      String pbeAlgo = PicketLinkFederationConstants.PBE_ALGORITHM;
      SecretKeyFactory factory = SecretKeyFactory.getInstance(pbeAlgo);

      char[] password = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();
      PBEParameterSpec cipherSpec = new PBEParameterSpec(salt.getBytes(), iterationCount);
      PBEKeySpec keySpec = new PBEKeySpec(password);
      SecretKey cipherKey = factory.generateSecret(keySpec);

      String encodedPass = PBEUtils.encode64(pass.getBytes(), pbeAlgo, cipherKey, cipherSpec);

      //Decode the stuff
      cipherKey = factory.generateSecret(keySpec);
      String decodedPass = PBEUtils.decode64(encodedPass, pbeAlgo, cipherKey, cipherSpec);

      assertEquals("Passwords match", pass, decodedPass);
   }
}