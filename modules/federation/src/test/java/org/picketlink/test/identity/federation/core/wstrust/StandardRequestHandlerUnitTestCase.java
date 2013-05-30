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
import org.picketlink.config.federation.STSType;
import org.picketlink.config.federation.parsers.STSConfigParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.identity.federation.core.wstrust.StandardRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustServiceFactory;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;

import java.io.InputStream;
import java.security.Principal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test the {@link StandardRequestHandler}
 *
 * @author anil saldhana
 */
public class StandardRequestHandlerUnitTestCase {

    @Test
    public void testUseKeyViaSecondaryParameters() throws Exception {
        ClassLoader cl = getClass().getClassLoader();

        InputStream configStream = cl.getResourceAsStream("sts/picketlink-sts.xml");
        STSConfigParser configParser = new STSConfigParser();
        STSType stsType = (STSType) configParser.parse(configStream);

        STSConfiguration config = new PicketLinkSTSConfiguration(stsType);
        WSTrustServiceFactory factory = WSTrustServiceFactory.getInstance();

        // tests the creation of the request handler.
        WSTrustRequestHandler handler = factory.createRequestHandler(
                "org.picketlink.identity.federation.core.wstrust.StandardRequestHandler", config);
        assertNotNull("Unexpected null request handler", handler);
        assertTrue("Unexpected request handler type", handler instanceof StandardRequestHandler);

        InputStream is = getClass().getClassLoader().getResourceAsStream("wstrust/wstrust-rst-usekey.xml");
        assertNotNull(is);

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityToken token = (RequestSecurityToken) parser.parse(is);

        RequestSecurityTokenResponse response = handler.issue(token, new Principal() {
            @Override
            public String getName() {
                return "testuser";
            }
        });
        assertNotNull(response);
    }
}