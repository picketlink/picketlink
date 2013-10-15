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
import org.picketlink.common.exceptions.fed.WSTrustException;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.w3c.dom.Element;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link AbstractSTSLoginModule}.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class AbstractSTSLoginModuleTestCase extends TestCase {

    private STSClient stsClient;

    public void setUp() {
        stsClient = mock(STSClient.class);
    }

    public void testOptionsConfig() {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(null, stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        options.put(AbstractSTSLoginModule.OPTIONS_CREDENTIALS, "true");

        loginModule.initialize(new Subject(), null, sharedState, options);

        assertTrue(loginModule.isUseOptionsConfig());
        assertFalse(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());
    }

    public void testOptionsConfigWithPasswordStacking() throws Exception {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(Util.createSamlToken(), stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        options.put(AbstractSTSLoginModule.OPTIONS_CREDENTIALS, "true");
        options.put(AbstractSTSLoginModule.OPTIONS_PW_STACKING, "true");

        loginModule.initialize(new Subject(), null, sharedState, options);

        assertTrue(loginModule.isUseOptionsConfig());
        assertTrue(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());

        loginModule.login();

        assertEquals("user1", loginModule.getSharedUsername());
        assertEquals("pass1", new String(loginModule.getSharedPassword()));
    }

    public void testCallback() {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(null, stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        TestCallbackHandler callbackHandler = new TestCallbackHandler("Mr.Poon", "rosen");

        loginModule.initialize(new Subject(), callbackHandler, sharedState, options);

        assertFalse(loginModule.isUseOptionsConfig());
        assertFalse(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());
    }

    public void testCallbackWithPasswordStacking() throws Exception {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(Util.createSamlToken(), stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        options.put(AbstractSTSLoginModule.OPTIONS_PW_STACKING, "true");
        final String username = "Mr.Poon";
        final String password = "rosen";

        TestCallbackHandler callbackHandler = new TestCallbackHandler(username, password);

        loginModule.initialize(new Subject(), callbackHandler, sharedState, options);

        assertFalse(loginModule.isUseOptionsConfig());
        assertTrue(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());

        loginModule.login();

        assertEquals(username, loginModule.getSharedUsername());
        assertEquals(password, new String(loginModule.getSharedPassword()));
    }

    class FakeSTSLoginModule extends AbstractSTSLoginModule {

        private Element token;
        private final STSClient client;

        public FakeSTSLoginModule(final Element token, final STSClient stsClient) {
            this.token = token;
            client = stsClient;
        }

        @Override
        protected STSClient createWSTrustClient(STSClientConfig config) {
            return client;
        }

        @Override
        public Element invokeSTS(STSClient stsclient) throws WSTrustException, LoginException {
            return token;
        }

    }

}
