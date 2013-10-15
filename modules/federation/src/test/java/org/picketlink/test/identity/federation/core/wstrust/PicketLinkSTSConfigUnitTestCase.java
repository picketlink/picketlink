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
package org.picketlink.test.identity.federation.core.wstrust;

import org.junit.Test;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.test.identity.federation.core.wstrust.PicketLinkSTSUnitTestCase.TestSTS;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import java.security.cert.Certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test various aspects of the sts configuration
 *
 * @author Anil.Saldhana@redhat.com
 * @since May 25, 2010
 */
public class PicketLinkSTSConfigUnitTestCase {

    /**
     * Test the masking of passwords
     *
     * @throws Exception
     */
    @Test
    public void testMaskedPassword() throws Exception {
        PicketLinkSTSUnitTestCase plstsTest = new PicketLinkSTSUnitTestCase();
        TestSTS sts = plstsTest.new TestSTS("sts/picketlink-sts-maskedpasswd.xml");

        STSConfiguration stsConfiguration = sts.getConfiguration();
        Certificate cert = stsConfiguration.getCertificate("service1");
        assertNotNull("cert is not null", cert);

        cert = stsConfiguration.getCertificate("service2");
        assertNotNull("cert is not null", cert);
    }

    /**
     * Test the introduction of the CanonicalizationMethod attribute on the STSType
     *
     * @throws Exception
     */
    @Test
    public void testXMLDSigCanonicalization() throws Exception {
        PicketLinkSTSUnitTestCase plstsTest = new PicketLinkSTSUnitTestCase();
        TestSTS sts = plstsTest.new TestSTS("sts/picketlink-sts-xmldsig-Canonicalization.xml");

        STSConfiguration stsConfiguration = sts.getConfiguration();
        assertNotNull("STS Configuration is not null", stsConfiguration);
        assertEquals(CanonicalizationMethod.EXCLUSIVE, stsConfiguration.getXMLDSigCanonicalizationMethod());
    }
}