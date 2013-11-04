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

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.common.random.DefaultSecureRandomProvider;
import org.picketlink.common.random.SecureRandomProvider;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.encoder.BCryptPasswordEncoder;
import org.picketlink.idm.credential.encoder.PBKDF2PasswordEncoder;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.encoder.SHAPasswordEncoder;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.picketlink.idm.credential.handler.PasswordCredentialHandler.*;
import static org.picketlink.idm.credential.handler.annotations.SupportsCredentials.NO_CREDENTIAL_STORAGE;
import static org.picketlink.idm.model.basic.BasicModel.*;

/**
 * <p>Some tests for the configuration of the encoding when using the {@link PasswordCredentialHandler}.</p>
 * 
 * @author Pedro Silva
 * @author Anil Saldhana
 */
public class PasswordCredentialHandlerConfigurationTestCase {
    
    @Test
    public void testBCryptPasswordEncoder() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .setCredentialHandlerProperty(PASSWORD_ENCODER, new BCryptPasswordEncoder(4))
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());

        assertNotNull(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testPBKDF2PasswordEncoder() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .setCredentialHandlerProperty(PASSWORD_ENCODER, new PBKDF2PasswordEncoder("salty".getBytes(), 1000, 128))
                    .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());

        assertNotNull(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testCustomSHAPasswordEncoder() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .setCredentialHandlerProperty(PASSWORD_ENCODER,
                            new SHAPasswordEncoder(1) {
                                @Override
                                public String encode(String rawPassword) {
                                    Assert.assertEquals(1, this.getStrength());
                                    return super.encode(rawPassword);
                                }

                                @Override
                                public boolean verify(String rawPassword, String encodedPassword) {
                                    return super.verify(rawPassword, encodedPassword);
                                }
                            })
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
    }
    
    @Test
    public void testCustomPasswordEncoder() throws Exception {
        final Map<String, String> assertionCheck = new HashMap<String, String>();

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .setCredentialHandlerProperty(PASSWORD_ENCODER, new PasswordEncoder() {
                            @Override
                            public String encode(String rawPassword) {
                                assertionCheck.put("WAS_INVOKED", "true");
                                return rawPassword;
                            }

                            @Override
                            public boolean verify(String rawPassword, String encodedPassword) {
                                return true;
                            }
                        })
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
        assertEquals("true", assertionCheck.get("WAS_INVOKED"));
    }

    @Test
    public void testKeyLengthRandomNumber() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .addCredentialHandler(MockPasswordCredentialHandler.class)
                        .setCredentialHandlerProperty(PasswordCredentialHandler.KEY_LENGTH_RANDOM_NUMBER, 8)
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());

        assertNotNull(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());

        assertEquals(8, ((DefaultSecureRandomProvider) MockPasswordCredentialHandler.secureRandomProvider).getKeyLength());
    }

    @Test
    public void testSaltAlgorithm() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .file()
                .addCredentialHandler(MockPasswordCredentialHandler.class)
                .setCredentialHandlerProperty(PasswordCredentialHandler.ALGORITHM_RANDOM_NUMBER, "SHA1PRNG")
                .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());

        assertNotNull(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());

        assertEquals("SHA1PRNG", ((DefaultSecureRandomProvider) MockPasswordCredentialHandler.secureRandomProvider).getAlgorithm());
    }

    @Test (expected=IdentityManagementException.class)
    public void failInvalidEncodingAlgorithm() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .setCredentialHandlerProperty(PASSWORD_ENCODER, new SHAPasswordEncoder(999) {
                            @Override
                            public String encode(String rawPassword) {
                                String encode = super.encode(rawPassword);
                                fail();
                                return encode;
                            }
                        })
                    .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        user = getUser(identityManager, user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        try {
            identityManager.updateCredential(user, password);            
        } catch (IdentityManagementException e) {
            assertTrue(e.getMessage().contains("PLIDM000201"));
            throw e;
        }
    }

    @SupportsCredentials(
            credentialClass = {UsernamePasswordCredentials.class, Password.class},
            credentialStorage = NO_CREDENTIAL_STORAGE.class)
    public static class MockPasswordCredentialHandler extends PasswordCredentialHandler {

        public static SecureRandomProvider secureRandomProvider;

        @Override
        public SecureRandomProvider getSecureRandomProvider() {
            secureRandomProvider = super.getSecureRandomProvider();
            return secureRandomProvider;
        }
    }
}
