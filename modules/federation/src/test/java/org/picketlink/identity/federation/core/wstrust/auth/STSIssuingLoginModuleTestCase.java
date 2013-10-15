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
import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link STSIssuingLoginModule}
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSIssuingLoginModuleTestCase extends TestCase {

    private STSClient stsClient;

    public void setUp() {
        stsClient = mock(STSClient.class);
    }

    public void testLoginWithValidToken() throws Exception {
        // Make the issueToken() method return a token.
        when(stsClient.issueToken(any(String.class), any(String.class))).thenReturn(Util.createSamlToken());

        final STSIssuingLoginModule loginModule = new FakeSTSIssuingLoginModule(stsClient);
        final CallbackHandler callbackHandler = new TestCallbackHandler("admin", "admin");
        final Subject subject = new Subject();
        final HashMap<String, Object> sharedState = new HashMap<String, Object>();

        loginModule.initialize(subject, callbackHandler, sharedState, allOptions());

        // Simulate Phase 1
        assertTrue(loginModule.login());

        final Object token = loginModule.getSharedToken();
        assertNotNull(token);
        assertTrue(token instanceof Element);

        // Simulate Phase 2
        assertTrue(loginModule.commit());

        final Set<SamlCredential> samlCredentials = subject.<SamlCredential>getPublicCredentials(SamlCredential.class);
        assertEquals(1, samlCredentials.size());
    }

    public void testUseFirstPass() {
        final String username = "Fletch";
        final String password = "letMeIn";
        final STSIssuingLoginModule loginModule = new STSIssuingLoginModule();
        final Subject subject = new Subject();

        final HashMap<String, Object> sharedState = new HashMap<String, Object>();
        sharedState.put("javax.security.auth.login.name", username);
        sharedState.put("javax.security.auth.login.password", password.toCharArray());

        final Map<String, String> options = allOptions();
        options.put(AbstractSTSLoginModule.OPTIONS_PW_STACKING, "useFirstPass");

        loginModule.initialize(subject, null, sharedState, options);

        assertTrue(loginModule.isUseFirstPass());
        assertEquals(username, loginModule.getSharedUsername());
        assertEquals(password, new String(loginModule.getSharedPassword()));
    }

    private Map<String, String> allOptions() {
        final Map<String, String> options = Util.allOptions();
        options.put(STSIssuingLoginModule.ENDPOINT_OPTION, "someUrl");
        options.put(STSIssuingLoginModule.TOKEN_TYPE_OPTION, "someTokenType");
        return options;
    }

    private class FakeSTSIssuingLoginModule extends STSIssuingLoginModule {

        private STSClient client;

        public FakeSTSIssuingLoginModule(final STSClient client) {
            this.client = client;
        }

        @Override
        protected STSClient createWSTrustClient(final STSClientConfig config) {
            return client;
        }
    }
}
