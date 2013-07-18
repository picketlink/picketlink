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
package org.picketlink.test.integration.authentication.credential;

import java.util.Date;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.internal.AbstractCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.test.integration.AbstractJPADeploymentTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.Credentials.Status.INVALID;
import static org.picketlink.idm.credential.Credentials.Status.VALID;
import static org.picketlink.test.integration.ArchiveUtils.addDependency;
import static org.picketlink.test.integration.ArchiveUtils.getCurrentProjectVersion;

/**
 * @author pedroigor
 */
public class CustomIDMCredentialTestCase extends AbstractJPADeploymentTestCase {

    public static final String USER_NAME = "john";
    @Inject
    private IdentityManager identityManager;

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = createDeployment(CustomIDMCredentialTestCase.class);

        addDependency(archive, "org.picketlink:picketlink-idm-schema:" + getCurrentProjectVersion());

        return archive;
    }

    @Override
    @Before
    public void onBefore() throws Exception {
        super.onBefore();

        User john = new User("john");

        this.identityManager.add(john);

        MyCredential credential = new MyCredential(john.getLoginName(), "valid_token");

        this.identityManager.updateCredential(john, credential);
    }

    @Override
    public void onAfter() {
        super.onAfter();
        this.identity.logout();
    }

    @Test
    public void testSuccessfullAuthentication() {
        MyCredential credential = new MyCredential(USER_NAME, "valid_token");

        this.credentials.setCredential(credential);
        this.identity.login();

        assertTrue(this.identity.isLoggedIn());
        assertNotNull(this.identity.getAgent());
        assertEquals(USER_NAME, this.identity.getAgent().getLoginName());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        MyCredential credential = new MyCredential(USER_NAME, "bad_token");

        this.credentials.setCredential(credential);
        this.identity.login();

        assertFalse(this.identity.isLoggedIn());
        assertNull(this.identity.getAgent());
    }

    @ApplicationScoped
    public static class IDMConfiguration {

        @Produces
        public IdentityConfiguration buildIDMConfiguration() {
            IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

            builder
                .named("default")
                    .stores()
                        .jpa()
//                            .addCredentialHandler(MyCredentialHandler.class)
//                            .identityClass(IdentityObject.class)
//                            .attributeClass(IdentityObjectAttribute.class)
//                            .relationshipClass(RelationshipObject.class)
//                            .relationshipIdentityClass(RelationshipIdentityObject.class)
//                            .relationshipAttributeClass(RelationshipObjectAttribute.class)
//                            .credentialClass(CredentialObject.class)
//                            .credentialAttributeClass(CredentialObjectAttribute.class)
//                            .partitionClass(PartitionObject.class)
//                            .supportAllFeatures();
            ;

            return builder.build();
        }

    }

    @SupportsCredentials(MyCredential.class)
    public static class MyCredentialHandler extends AbstractCredentialHandler<CredentialStore<?>, MyCredential, MyCredential> {
        @Override
        public void setup(CredentialStore<?> store) {
            // handler initialization
        }

        @Override
        public void validate(IdentityContext context, MyCredential credentials, CredentialStore<?> store) {
            if (!MyCredential.class.isInstance(credentials)) {
                throw MESSAGES.credentialUnsupportedType(credentials.getClass(), this);
            }

            MyCredential credential = (MyCredential) credentials;

            credential.setStatus(INVALID);

            Agent agent = getAgent(context, credential.getUserName());

            if (agent != null) {
                MyCredentialStorage storage = store.retrieveCurrentCredential(context, agent, MyCredentialStorage.class);

                String token = storage.getToken();

                if ("valid_token".equals(credential.getToken())) {
                    credential.setStatus(VALID);
                    credential.setValidatedAgent(agent);
                }
            }
        }

        @Override
        public void update(IdentityContext context, Account agent, MyCredential credential, CredentialStore<?> store, Date effectiveDate, Date expiryDate) {
            MyCredentialStorage storage = new MyCredentialStorage();

            storage.setEffectiveDate(effectiveDate);
            storage.setExpiryDate(expiryDate);
            storage.setToken(credential.getToken());

            store.storeCredential(context, agent, storage);
        }
    }

    public static class MyCredential extends AbstractBaseCredentials {

        private String userName;
        private String token;

        public MyCredential(String userName, String token) {
            this.userName = userName;
            this.token = token;
        }

        @Override
        public void invalidate() {
        }

        public String getUserName() {
            return this.userName;
        }

        public String getToken() {
            return this.token;
        }
    }
    
    public static class MyCredentialStorage implements CredentialStorage {

        private Date effectiveDate;
        private Date expiryDate;
        private String token;

        @Override
        @Stored
        public Date getEffectiveDate() {
            return this.effectiveDate;
        }

        public void setEffectiveDate(Date effectiveDate) {
            this.effectiveDate = effectiveDate;
        }

        @Override
        @Stored
        public Date getExpiryDate() {
            return this.expiryDate;
        }

        public void setExpiryDate(Date expiryDate) {
            this.expiryDate = expiryDate;
        }

        @Stored
        public String getToken() {
            return this.token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
