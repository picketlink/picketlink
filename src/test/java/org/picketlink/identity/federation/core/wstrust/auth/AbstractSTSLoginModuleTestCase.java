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

import static org.mockito.Mockito.mock;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.STSClient;
import org.picketlink.identity.federation.core.wstrust.STSClientConfig;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/***
 * Unit test for {@link AbstractSTSLoginModule}.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class AbstractSTSLoginModuleTestCase extends TestCase
{
    private STSClient stsClient;
    
    public void setUp()
    {
        stsClient = mock(STSClient.class);
    }
    
    public void testOptionsConfig()
    {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(null, stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        options.put(AbstractSTSLoginModule.OPTIONS_CREDENTIALS, "true");
        
        loginModule.initialize(new Subject(), null, sharedState, options);
        
        assertTrue(loginModule.isUseOptionsConfig());
        assertFalse(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());
    }
    
    public void testOptionsConfigWithPasswordStacking() throws Exception
    {
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
    
    public void testCallback()
    {
        final AbstractSTSLoginModule loginModule = new FakeSTSLoginModule(null, stsClient);
        final Map<String, String> sharedState = new HashMap<String, String>();
        final Map<String, String> options = Util.allOptions();
        TestCallbackHandler callbackHandler = new TestCallbackHandler("Mr.Poon", "rosen");
        
        loginModule.initialize(new Subject(), callbackHandler, sharedState, options);
        
        assertFalse(loginModule.isUseOptionsConfig());
        assertFalse(loginModule.isUsePasswordStacking());
        assertFalse(loginModule.isUseFirstPass());
    }
    
    public void testCallbackWithPasswordStacking() throws Exception
    {
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
    
    class FakeSTSLoginModule extends AbstractSTSLoginModule
    {
        private Element token;
        private final STSClient client;
        
        public FakeSTSLoginModule(final Element token, final STSClient stsClient)
        {
            this.token = token;
            client = stsClient;
        }
        
        
        @Override
        protected STSClient createWSTrustClient(STSClientConfig config)
        {
            return client;
        }

        @Override
        public Element invokeSTS(STSClient stsclient) throws WSTrustException, LoginException
        {
            return token;
        }
        
    }

}
