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
package org.picketlink.test.identity.federation.web.integration;

import junit.framework.TestCase;
import org.picketlink.identity.federation.web.core.IdentityServer;
import org.picketlink.test.identity.federation.web.mock.MockHttpSession;
import org.picketlink.test.identity.federation.web.mock.MockServletContext;

import javax.servlet.http.HttpSessionEvent;

/**
 * Unit test the Identity Server
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class IdentityServerUnitTestCase extends TestCase {

    public void testActiveSessionCount() {
        IdentityServer server = new IdentityServer();
        assertEquals(0, server.getActiveSessionCount());

        MockHttpSession session = new MockHttpSession();
        session.setServletContext(new MockServletContext());
        HttpSessionEvent event = new HttpSessionEvent(session);
        server.sessionCreated(event);
        assertEquals(1, server.getActiveSessionCount());

        server.sessionDestroyed(event);
        assertEquals(0, server.getActiveSessionCount());
        // 6 sessions created and 1 destroyed
        server.sessionCreated(event);
        server.sessionCreated(event);
        server.sessionCreated(event);
        server.sessionCreated(event);
        server.sessionCreated(event);
        server.sessionCreated(event);

        server.sessionDestroyed(event);
        assertEquals(5, server.getActiveSessionCount());
    }
}