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

package org.picketlink.test.idm.usecases;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import static org.junit.Assert.*;

/**
 * <p>Test case of {@link org.picketlink.idm.credential.random.AutoReseedSecureRandomProvider} used as SecureRandomProvider for
 * {@link org.picketlink.idm.credential.handler.PasswordCredentialHandler}.</p>
 *
 * <p>Other unit tests are using simpleSecureRandomProvider to not block the execution of unit tests</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RealSecureRandomProviderTestCase {

    @Test
    public void testSecureRandomProvider() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        // Don't add simpleSecureRandomProvider, which means that PasswordCredentialHandler will use AutoReseedSecureRandomProvider
        builder
            .named("simple-file-store")
                .stores()
                    .file()
                    .preserveState(false)
                    .supportAllFeatures();

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        User john = new User("john");
        User mary = new User("mary");
        IdentityManager identityManager = partitionManager.createIdentityManager();
        identityManager.add(john);
        identityManager.add(mary);
        identityManager.updateCredential(john, new Password("johnpass"));
        identityManager.updateCredential(mary, new Password("marypass"));

        UsernamePasswordCredentials valid1 = new UsernamePasswordCredentials("john", new Password("johnpass"));
        UsernamePasswordCredentials valid2 = new UsernamePasswordCredentials("mary", new Password("marypass"));
        UsernamePasswordCredentials invalid1 = new UsernamePasswordCredentials("mary", new Password("johnpass"));
        UsernamePasswordCredentials invalid2 = new UsernamePasswordCredentials("john", new Password("marypass"));
        identityManager.validateCredentials(valid1);
        identityManager.validateCredentials(valid2);
        identityManager.validateCredentials(invalid1);
        identityManager.validateCredentials(invalid2);
        assertEquals(Credentials.Status.VALID, valid1.getStatus());
        assertEquals(Credentials.Status.VALID, valid2.getStatus());
        assertEquals(Credentials.Status.INVALID, invalid1.getStatus());
        assertEquals(Credentials.Status.INVALID, invalid2.getStatus());
    }
}
