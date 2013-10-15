/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.identity.federation.core.config;

import org.junit.Test;
import org.picketlink.common.util.PBEUtils;
import org.picketlink.identity.federation.core.constants.PicketLinkFederationConstants;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import static org.junit.Assert.assertEquals;

/**
 * Test the masking of the password using {@code PBEUtils}
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 25, 2010
 */
public class PBEUtilsUnitTestCase {

    @Test
    public void testPBE() throws Exception {
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

        // Decode the stuff
        cipherKey = factory.generateSecret(keySpec);
        String decodedPass = PBEUtils.decode64(encodedPass, pbeAlgo, cipherKey, cipherSpec);

        assertEquals("Passwords match", pass, decodedPass);
    }
}