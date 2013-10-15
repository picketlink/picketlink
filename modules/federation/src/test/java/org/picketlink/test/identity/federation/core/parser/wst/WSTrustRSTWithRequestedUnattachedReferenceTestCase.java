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
package org.picketlink.test.identity.federation.core.parser.wst;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.wss.secext.SecurityTokenReferenceType;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * PLINK2-36: PicketLink STS chokes on WS-Policy 1.5 tags
 *
 * @author anil saldhana
 * @since May 20, 2013
 */
public class WSTrustRSTWithRequestedUnattachedReferenceTestCase {

    @Test
    public void parseRSTWithWSP_15() throws Exception {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-response-unnatachedreference.xml");

        WSTrustParser parser = new WSTrustParser();
        RequestSecurityTokenResponseCollection requestTokenResponseCollection = (RequestSecurityTokenResponseCollection) parser.parse(configStream);

        RequestSecurityTokenResponse requestSecurityTokenResponse = requestTokenResponseCollection.getRequestSecurityTokenResponses().get(0);

        RequestedReferenceType requestedUnattachedReference = requestSecurityTokenResponse.getRequestedUnattachedReference();

        assertNotNull(requestedUnattachedReference);

        SecurityTokenReferenceType securityTokenReference = requestedUnattachedReference.getSecurityTokenReference();

        assertNotNull(securityTokenReference);
    }
}