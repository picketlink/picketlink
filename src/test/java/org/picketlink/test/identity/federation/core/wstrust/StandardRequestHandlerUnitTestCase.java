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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.security.Principal;

import org.junit.Test;
import org.picketlink.identity.federation.core.config.STSType;
import org.picketlink.identity.federation.core.parsers.sts.STSConfigParser;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.wstrust.PicketLinkSTSConfiguration;
import org.picketlink.identity.federation.core.wstrust.STSConfiguration;
import org.picketlink.identity.federation.core.wstrust.StandardRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustRequestHandler;
import org.picketlink.identity.federation.core.wstrust.WSTrustServiceFactory;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;

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