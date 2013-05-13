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

package org.picketlink.test.idm.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;

/**
 * <p>Some tests for the configuration of custom {@link CredentialHandler}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class CredentialHandlerConfigurationTestCase {
    
    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Before
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
        CustomCredentialHandler.wasSetupCalled = false;
        CustomCredentialHandler.wasValidateCalled = false;
        CustomCredentialHandler.wasUpdateCalled = false;
    }

    @After
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }
    
    @Test
    public void testCredentialHandlerLifeCycle() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .stores()
                .jpa()
                    .addCredentialHandler(CustomCredentialHandler.class)
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .supportAllFeatures()
                    .identityClass(IdentityObject.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .relationshipClass(RelationshipObject.class)
                    .relationshipIdentityClass(RelationshipIdentityObject.class)
                    .relationshipAttributeClass(RelationshipObjectAttribute.class)
                    .credentialClass(CredentialObject.class)
                    .credentialAttributeClass(CredentialObjectAttribute.class)
                    .partitionClass(PartitionObject.class);

        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());

        IdentityManager identityManager = identityManagerFactory.createIdentityManager();

        User user = new SimpleUser("user");

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());
        
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
            .stores()
                .jpa()
                    .addCredentialHandler(CustomCredentialHandler.class)
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .supportAllFeatures()
                    .identityClass(IdentityObject.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .relationshipClass(RelationshipObject.class)
                    .relationshipIdentityClass(RelationshipIdentityObject.class)
                    .relationshipAttributeClass(RelationshipObjectAttribute.class)
                    .credentialClass(CredentialObject.class)
                    .credentialAttributeClass(CredentialObjectAttribute.class)
                    .partitionClass(PartitionObject.class);

        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());

        IdentityManager identityManager = identityManagerFactory.createIdentityManager();

        User user = new SimpleUser("user");

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());
        
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

    @SupportsCredentials({ CustomCredentialHandler.TokenCredential.class, CustomCredentialHandler.Token.class })
    public static class CustomCredentialHandler implements CredentialHandler {

        public static boolean wasValidateCalled;
        public static boolean wasUpdateCalled;
        public static boolean wasSetupCalled;
        
        @Override
        public void validate(SecurityContext context, Credentials credentials, IdentityStore<?> identityStore) {
            TokenCredential credential = (TokenCredential) credentials;
            
            credential.setStatus(Status.INVALID);
            
            if (credential.getToken().getValue().equals("123")) {
                credential.setStatus(Status.VALID);
            }
            
            wasValidateCalled = true;
        }

        @Override
        public void update(SecurityContext context, Agent agent, Object credential, IdentityStore<?> identityStore,
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