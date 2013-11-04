/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.picketlink.test.idm.credential;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.idm.credential.handler.annotations.SupportsCredentials.NO_CREDENTIAL_STORAGE;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * <p>Some tests for the configuration of custom {@link CredentialHandler}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class CredentialHandlerConfigurationTestCase {
    
    @Before
    public void onInit() {
        CustomCredentialHandler.wasSetupCalled = false;
        CustomCredentialHandler.wasValidateCalled = false;
        CustomCredentialHandler.wasUpdateCalled = false;
    }

    @Test
    public void testCredentialHandlerLifeCycle() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .addCredentialHandler(CustomCredentialHandler.class)
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());
        
        assertNotNull(user);
        
        CustomCredentialHandler.Token token = new CustomCredentialHandler.Token("123");
        
        identityManager.updateCredential(user, token);
        
        assertTrue(CustomCredentialHandler.wasSetupCalled);
        assertTrue(CustomCredentialHandler.wasUpdateCalled);
        assertFalse(CustomCredentialHandler.wasValidateCalled);
        
        CustomCredentialHandler.TokenCredential credential = new CustomCredentialHandler.TokenCredential(token);
        
        identityManager.validateCredentials(credential);
        
        assertTrue(CustomCredentialHandler.wasValidateCalled);
    }
    
    @Test
    public void testCustomCredentialHandler() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .addCredentialHandler(CustomCredentialHandler.class)
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());
        
        assertNotNull(user);
        
        CustomCredentialHandler.Token token = new CustomCredentialHandler.Token("123");
        
        identityManager.updateCredential(user, token);
        
        CustomCredentialHandler.TokenCredential credential = new CustomCredentialHandler.TokenCredential(token);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
        
        CustomCredentialHandler.TokenCredential badCredential = new CustomCredentialHandler.TokenCredential(new CustomCredentialHandler.Token("bad_token"));
        
        identityManager.validateCredentials(badCredential);
        
        assertEquals(Status.INVALID, badCredential.getStatus());
    }

    @SupportsCredentials(
            credentialClass = { CustomCredentialHandler.TokenCredential.class, CustomCredentialHandler.Token.class },
            credentialStorage = NO_CREDENTIAL_STORAGE.class
    )
    public static class CustomCredentialHandler<S,V,U> 
        implements CredentialHandler<IdentityStore<?>, CustomCredentialHandler.TokenCredential, Object> {

        public static boolean wasValidateCalled;
        public static boolean wasUpdateCalled;
        public static boolean wasSetupCalled;
        
        @Override
        public void validate(IdentityContext context, CustomCredentialHandler.TokenCredential credentials, 
                IdentityStore<?> identityStore) {
            credentials.setStatus(Status.INVALID);
            
            if (credentials.getToken().getValue().equals("123")) {
                credentials.setStatus(Status.VALID);
            }
            
            wasValidateCalled = true;
        }

        @Override
        public void update(IdentityContext context, Account agent, Object credential, IdentityStore<?> identityStore,
                Date effectiveDate, Date expiryDate) {
            wasUpdateCalled = true;   
        }

        @Override
        public void setup(IdentityStore<?> identityStore) {
            wasSetupCalled = true;
        }
        
        public static class Token {
            private String value;
            
            public Token(String value) {
                this.value = value;
            }

            public String getValue() {
                return this.value;
            }
            
            public void setValue(String value) {
                this.value = value;
            }
        }
        
        public static class TokenCredential extends AbstractBaseCredentials {

            private Token token;
            
            public TokenCredential(Token token) {
                this.token = token;
            }
            
            @Override
            public void invalidate() {
                this.token = null;
            }
            
            public Token getToken() {
                return token;
            }
        }
    }
}