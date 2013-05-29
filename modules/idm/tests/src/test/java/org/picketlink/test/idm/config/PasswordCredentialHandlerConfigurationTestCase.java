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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
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
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.User;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.password.internal.SHAPasswordEncoder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.picketlink.idm.credential.internal.PasswordCredentialHandler.PASSWORD_ENCODER;

/**
 * <p>Some tests for the configuration of the encoding when using the {@link PasswordCredentialHandler}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class PasswordCredentialHandlerConfigurationTestCase {
    
    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Before
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @After
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }
    
    @Test
    public void testCustomSHAPasswordEncoder() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        builder
            .stores()
                .jpa()
                .setCredentialHandlerProperty(PASSWORD_ENCODER, 
                        new SHAPasswordEncoder(1) {
                            @Override
                            public String encode(String rawPassword) {
                                Assert.assertEquals(1, this.getStrength());
                                return super.encode(rawPassword);
                            }})
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .addRealm(Realm.DEFAULT_REALM)
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

        User user = new User("user");

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
    }
    
    @Test
    public void testCustomPasswordEncoder() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        final Map<String, String> assertionCheck = new HashMap<String, String>();

        builder
            .stores()
                .jpa()
                    .setCredentialHandlerProperty(PASSWORD_ENCODER, new PasswordEncoder() {
                        @Override
                        public String encode(String rawPassword) {
                            assertionCheck.put("WAS_INVOKED", "true");
                            return rawPassword;
                        }
                    })
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .addRealm(Realm.DEFAULT_REALM)
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

        User user = new User("user");

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        identityManager.updateCredential(user, password);
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
        assertEquals("true", assertionCheck.get("WAS_INVOKED"));
    }

    @Test (expected=IdentityManagementException.class)
    public void failInvalidEncodingAlgorithm() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        final Map<String, String> assertionCheck = new HashMap<String, String>();

        builder
            .stores()
                .jpa()
                    .setCredentialHandlerProperty(PASSWORD_ENCODER, new SHAPasswordEncoder(999) {
                        @Override
                        public String encode(String rawPassword) {
                            String encode = super.encode(rawPassword);
                            fail();
                            return encode;
                        }
                    })
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                    .addRealm(Realm.DEFAULT_REALM)
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

        User user = new User("user");

        identityManager.add(user);

        user = identityManager.getUser(user.getLoginName());
        
        assertNotNull(user);
        
        Password password = new Password("123");
        
        try {
            identityManager.updateCredential(user, password);            
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("PLIDM000072"));
            throw e;
        }
        
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials(user.getLoginName(), password);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.VALID, credential.getStatus());
    }

}
