/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust.auth;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.w3c.dom.Element;

/**
 * Unit test for {@link STSIssuingLoginModule}
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSIssuingLoginModuleTestCase extends TestCase
{
    private STSClient stsClient;
    
    public void setUp()
    {
        stsClient = mock(STSClient.class);
    }
    
    public void testLoginWithValidToken() throws Exception
    {
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
    
    public void testUseFirstPass()
    {
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
    
    private Map<String, String> allOptions()
    {
        final Map<String, String> options = Util.allOptions();
        options.put(STSIssuingLoginModule.ENDPOINT_OPTION, "someUrl");
        options.put(STSIssuingLoginModule.TOKEN_TYPE_OPTION, "someTokenType");
        return options;
    }

    private class FakeSTSIssuingLoginModule extends STSIssuingLoginModule
    {
        private STSClient client;

        public FakeSTSIssuingLoginModule(final STSClient client) 
        {
            this.client = client;
        }

        @Override
        protected STSClient createWSTrustClient(final STSClientConfig config)
        {
            return client;
        }
    }
}
