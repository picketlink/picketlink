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

import junit.framework.TestCase;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.identity.federation.core.wstrust.StandardRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustServiceFactory;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider;

import java.util.HashMap;

/**
 * <p>
 * This {@code TestCase} tests the behavior of the {@code WSTrustServiceFactory} class.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustServiceFactoryUnitTestCase extends TestCase {

    /**
     * <p>
     * Tests the creation of a {@code WSTrustRequestHandler} instance.
     * </p>
     *
     * @throws Exception if an error occurs while running the test.
     */
    public void testCreateRequestHandler() throws Exception {
        STSConfiguration config = new PicketLinkSTSConfiguration();
        WSTrustServiceFactory factory = WSTrustServiceFactory.getInstance();

        // tests the creation of the request handler.
        WSTrustRequestHandler handler = factory.createRequestHandler(
                "org.picketlink.identity.federation.core.wstrust.StandardRequestHandler", config);
        assertNotNull("Unexpected null request handler", handler);
        assertTrue("Unexpected request handler type", handler instanceof StandardRequestHandler);

        // try to create an invalid instance of request handler.
        try {
            factory.createRequestHandler("InvalidHandler", config);
            fail("An exception should have been raised");
        } catch (RuntimeException re) {
            String msg = re.getCause().getMessage();
            assertTrue(msg.contains("Class Not Loaded"));
        }
    }

    /**
     * <p>
     * Tests the creation of {@code SecurityTokenProvider}s.
     * </p>
     *
     * @throws Exception if an error occurs while running the test.
     */
    public void testCreateTokenProvider() throws Exception {
        WSTrustServiceFactory factory = WSTrustServiceFactory.getInstance();
        SecurityTokenProvider provider = factory.createTokenProvider(
                "org.picketlink.test.identity.federation.core.wstrust.SpecialTokenProvider", null);
        assertNotNull("Unexpected null token provider", provider);
        assertTrue("Unexpected token provider type", provider instanceof SpecialTokenProvider);
        provider = factory.createTokenProvider(
                "org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider",
                new HashMap<String, String>());
        assertNotNull("Unexpected null token provider", provider);
        assertTrue("Unexpected token provider type", provider instanceof SAML20TokenProvider);

        // try to create an invalid token provider.
        try {
            factory.createTokenProvider("InvalidTokenProvider", null);
            fail("An exception should have been raised");
        } catch (RuntimeException re) {
            String msg = re.getCause().getMessage();
            assertTrue(msg.contains("Cannot create instance"));
        }
    }
}
