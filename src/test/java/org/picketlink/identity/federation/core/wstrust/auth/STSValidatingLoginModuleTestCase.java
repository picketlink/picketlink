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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule;
import org.picketlink.identity.federation.core.wstrust.auth.STSValidatingLoginModule;
import org.picketlink.identity.federation.core.wstrust.auth.TokenCallback;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * Unit test for {@link STSIssuingLoginModule}.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSValidatingLoginModuleTestCase extends TestCase
{
    private STSClient stsClient;
    
    public void setUp()
    {
	    stsClient = mock(STSClient.class);
    }
    
    public void testLoginWithValidToken() throws Exception
    {
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
    
    public void testLoginWithInValidToken() throws Exception
    {
        // Make the validateToken() method return false.
        when(stsClient.validateToken(any(Element.class))).thenReturn(false);

        final STSValidatingLoginModule loginModule = new FakeSTSValidatingLoginModule(stsClient);
        final CallbackHandler callbackHandler = new TestCallbackHandler(Util.createSamlToken());

        loginModule.initialize(new Subject(), callbackHandler, null, getAllOptions());

        try
        {
	        // Simulate Phase 1
	        loginModule.login();
	        fail("login should have thrown a LoginException!");
        }
        catch (final Exception e)
        {
            assertTrue(e instanceof LoginException);
        }
    }
    
    public void testStackedModules() throws Exception
    {
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
    
    private Map<String, String> getAllOptions()
    {
        Map<String, String> options = Util.allOptions();
        options.put("useOptionsCredentials", "true");
        return options;
    }
    
    private class TestCallbackHandler implements CallbackHandler
    {
        private final Object token;
    
        public TestCallbackHandler(final Object token)
        {
            this.token = token;
        }
        
        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException
        {
            for (Callback callback : callbacks)
            {
                if (callback instanceof TokenCallback)
                {
                    ((TokenCallback)callback).setToken(token);
                }
            }
        }
    }

    private class FakeSTSValidatingLoginModule extends STSValidatingLoginModule
    {
        private STSClient client;

        public FakeSTSValidatingLoginModule(final STSClient client) 
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
