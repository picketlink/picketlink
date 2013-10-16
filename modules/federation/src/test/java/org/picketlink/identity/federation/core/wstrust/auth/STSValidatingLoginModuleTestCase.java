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
package org.picketlink.identity.federation.core.wstrust.auth;

import junit.framework.TestCase;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.w3c.dom.Element;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link STSIssuingLoginModule}.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSValidatingLoginModuleTestCase extends TestCase {

    private STSClient stsClient;

    public void setUp() {
        stsClient = mock(STSClient.class);
    }

    public void testLoginWithValidToken() throws Exception {
        // Make the validateToken() method return true.
        when(stsClient.validateToken(any(Element.class))).thenReturn(true);

        final STSValidatingLoginModule loginModule = new FakeSTSValidatingLoginModule(stsClient);
        final CallbackHandler callbackHandler = new TestCallbackHandler(Util.createSamlToken());
        final Subject subject = new Subject();

        loginModule.initialize(subject, callbackHandler, null, getAllOptions());

        // Simulate Phase 1
        assertTrue(loginModule.login());

        // Simulate Phase 2
        assertTrue(loginModule.commit());

        final Set<SamlCredential> samlCredentials = subject.<SamlCredential>getPublicCredentials(SamlCredential.class);
        assertEquals(1, samlCredentials.size());
    }

    public void testLoginWithInValidToken() throws Exception {
        // Make the validateToken() method return false.
        when(stsClient.validateToken(any(Element.class))).thenReturn(false);

        final STSValidatingLoginModule loginModule = new FakeSTSValidatingLoginModule(stsClient);
        final CallbackHandler callbackHandler = new TestCallbackHandler(Util.createSamlToken());

        loginModule.initialize(new Subject(), callbackHandler, null, getAllOptions());

        try {
            // Simulate Phase 1
            loginModule.login();
            fail("login should have thrown a LoginException!");
        } catch (final Exception e) {
            assertTrue(e instanceof LoginException);
        }
    }

    public void testStackedModules() throws Exception {
        // Make the validateToken() method return true.
        when(stsClient.validateToken(any(Element.class))).thenReturn(true);

        final STSValidatingLoginModule loginModule = new FakeSTSValidatingLoginModule(stsClient);
        final Element token = Util.createSamlToken();

        final Subject subject = new Subject();

        final Map<String, Object> sharedState = new HashMap<String, Object>();

        loginModule.initialize(subject, null, sharedState, getAllOptions());
        // Simlulate that a previous LM stored a security token in the shared state.
        loginModule.setSharedToken(token);

        // Simulate Phase 1
        assertTrue(loginModule.login());

        // Simulate Phase 2
        assertTrue(loginModule.commit());

        final Set<SamlCredential> samlCredentials = subject.<SamlCredential>getPublicCredentials(SamlCredential.class);
        assertEquals(1, samlCredentials.size());
    }

    private Map<String, String> getAllOptions() {
        Map<String, String> options = Util.allOptions();
        options.put("useOptionsCredentials", "true");
        return options;
    }

    private class TestCallbackHandler implements CallbackHandler {

        private final Object token;

        public TestCallbackHandler(final Object token) {
            this.token = token;
        }

        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof TokenCallback) {
                    ((TokenCallback) callback).setToken(token);
                }
            }
        }
    }

    private class FakeSTSValidatingLoginModule extends STSValidatingLoginModule {

        private STSClient client;

        public FakeSTSValidatingLoginModule(final STSClient client) {
            this.client = client;
        }

        @Override
        protected STSClient createWSTrustClient(final STSClientConfig config) {
            return client;
        }
    }

}
