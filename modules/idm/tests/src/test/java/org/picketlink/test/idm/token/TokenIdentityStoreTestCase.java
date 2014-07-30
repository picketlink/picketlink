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
package org.picketlink.test.idm.token;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import static org.junit.Assert.assertEquals;

/**
 * @author Pedro Igor
 */
public class TokenIdentityStoreTestCase {

    private PartitionManager serviceProviderPartitionManager;
    private PartitionManager identityProviderPartitionManager;

    private CredentialsContextInitializer credentialsContextInitializer = new CredentialsContextInitializer();

    @Before
    public void onBefore() {
        IdentityConfigurationBuilder identityProviderConfigBuilder = new IdentityConfigurationBuilder();

        identityProviderConfigBuilder
            .named("idp.config")
            .stores()
            .file()
            .supportAllFeatures();

        this.identityProviderPartitionManager = new DefaultPartitionManager(identityProviderConfigBuilder.buildAll());

        this.identityProviderPartitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityConfigurationBuilder serviceProviderConfigBuilder = new IdentityConfigurationBuilder();

        serviceProviderConfigBuilder
            .named("sp.config")
                .stores()
                    .token()
                        .tokenConsumer(new TokenAConsumer())
                        .addContextInitializer(this.credentialsContextInitializer)
                        .supportAllFeatures();

        this.serviceProviderPartitionManager = new DefaultPartitionManager(serviceProviderConfigBuilder.buildAll());
    }

    @Test
    public void testValidateCredential() {
        User account = createAccount();
        TokenAProvider tokenAProvider = new TokenAProvider(this.identityProviderPartitionManager);
        TokenA token = tokenAProvider.issue(account);

        IdentityManager identityManager = this.serviceProviderPartitionManager.createIdentityManager();

        TokenACredential credentials = new TokenACredential(token);

        this.credentialsContextInitializer.setCredentials(credentials);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());
    }

    private User createAccount() {
        IdentityManager identityManager = this.identityProviderPartitionManager.createIdentityManager();
        User account = new User("john");

        identityManager.add(account);

        return account;
    }

    public static class CredentialsContextInitializer implements ContextInitializer {

        private Credentials credentials;

        @Override
        public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
            context.setParameter(IdentityContext.CREDENTIALS, this.credentials);
        }

        public void setCredentials(Credentials credentials) {
            this.credentials = credentials;
        }
    }
}
