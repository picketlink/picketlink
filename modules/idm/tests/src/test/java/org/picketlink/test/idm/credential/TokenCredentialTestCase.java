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

package org.picketlink.test.idm.credential;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.token.TokenA;
import org.picketlink.test.idm.token.TokenACredential;
import org.picketlink.test.idm.token.TokenACredentialHandler;
import org.picketlink.test.idm.token.TokenAProvider;
import org.picketlink.test.idm.token.TokenB;
import org.picketlink.test.idm.token.TokenBCredential;
import org.picketlink.test.idm.token.TokenBProvider;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for {@link org.picketlink.idm.credential.UsernamePasswordCredentials} type.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class TokenCredentialTestCase extends AbstractPartitionManagerTestCase {

    public TokenCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Before
    public void onBefore() {
        super.onBefore();
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA token = tokenAProvider.issue(user);

        TokenACredential credential = new TokenACredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertEquals(user, credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA token = tokenAProvider.issue(user);

        User invalidUser = new User("bad_user");

        invalidUser.setPartition(user.getPartition());

        invalidUser.setId("invalid_id");

        TokenACredential badUserName = new TokenACredential(TokenAProvider.createToken(TokenA.class, invalidUser));

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());

        TokenACredential credential = new TokenACredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertEquals(user, credential.getValidatedAccount());
    }

    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager(), expirationDate.getTime());
        TokenA token = tokenAProvider.issue(user);

        TokenACredential credential = new TokenACredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA newToken = tokenAProvider.issue(user);

        Thread.sleep(1000);

        identityManager.updateCredential(user, newToken);

        credential = new TokenACredential(newToken);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertEquals(user, credential.getValidatedAccount());
    }

    @Test
    public void testUpdateToken() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA firstToken = tokenAProvider.issue(user);

        TokenACredential firstCredential = new TokenACredential(firstToken);

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.VALID, firstCredential.getStatus());
        assertEquals(user, firstCredential.getValidatedAccount());

        TokenA secondToken = tokenAProvider.issue(user);

        identityManager.updateCredential(user, secondToken);

        TokenACredential secondCredential = new TokenACredential(secondToken);

        identityManager.validateCredentials(secondCredential);

        assertEquals(Status.VALID, secondCredential.getStatus());
        assertEquals(user, secondCredential.getValidatedAccount());

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.INVALID, firstCredential.getStatus());
        assertNull(firstCredential.getValidatedAccount());
    }

    @Test
    public void testUserDeletion() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User john = createUser("john");
        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA johnToken = tokenAProvider.issue(john);

        TokenACredential johnCredential = new TokenACredential(johnToken);

        identityManager.validateCredentials(johnCredential);

        assertEquals(Status.VALID, johnCredential.getStatus());
        assertEquals(john, johnCredential.getValidatedAccount());

        User francesco = createUser("francesco");
        TokenA francescoToken = tokenAProvider.issue(francesco);

        identityManager.updateCredential(francesco, francescoToken);

        TokenACredential francescoCredential = new TokenACredential(francescoToken);

        identityManager.validateCredentials(francescoCredential);

        assertEquals(Status.VALID, francescoCredential.getStatus());
        assertEquals(francesco, francescoCredential.getValidatedAccount());

        identityManager.remove(francesco);

        identityManager.validateCredentials(johnCredential);

        assertEquals(Status.VALID, johnCredential.getStatus());
        assertEquals(john, johnCredential.getValidatedAccount());
    }

    @Test
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = new TokenAProvider(getPartitionManager()).issue(user);

        TokenACredential credential = new TokenACredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertEquals(user, credential.getValidatedAccount());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Status.ACCOUNT_DISABLED, credential.getStatus());
        assertNull(credential.getValidatedAccount());
    }

    @Test
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = new TokenAProvider(getPartitionManager()).issue(user);

        identityManager.updateCredential(user, token);

        TokenACredentialHandler.TokenACredentialStorage currentStorage = identityManager.retrieveCurrentCredential(user, TokenACredentialHandler.TokenACredentialStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getToken());
        assertEquals(token.getToken(), currentStorage.getToken());
        assertEquals(TokenA.class.getName(), currentStorage.getType());
    }

    @Test
    public void testMultipleTokenSupport() throws Exception {
        User user = createUser("mary");
        TokenAProvider tokenAProvider = new TokenAProvider(getPartitionManager());
        TokenA tokenA = tokenAProvider.issue(user);
        IdentityManager identityManager = getIdentityManager();

        TokenACredential tokenACredential = new TokenACredential(tokenA);

        identityManager.validateCredentials(tokenACredential);

        assertEquals(Status.VALID, tokenACredential.getStatus());
        assertEquals(user, tokenACredential.getValidatedAccount());

        TokenBProvider tokenBProvider = new TokenBProvider(getPartitionManager());
        TokenB tokenB = tokenBProvider.issue(user);

        TokenBCredential tokenBCredential = new TokenBCredential(tokenB);

        identityManager.validateCredentials(tokenBCredential);
        assertEquals(Status.VALID, tokenBCredential.getStatus());
        assertEquals(user, tokenBCredential.getValidatedAccount());

        identityManager.validateCredentials(tokenACredential);
        assertEquals(Status.VALID, tokenACredential.getStatus());
        assertEquals(user, tokenACredential.getValidatedAccount());

        tokenAProvider.invalidate(user);

        identityManager.validateCredentials(tokenACredential);
        assertEquals(Status.INVALID, tokenACredential.getStatus());
        assertNull(tokenACredential.getValidatedAccount());

        identityManager.validateCredentials(tokenBCredential);
        assertEquals(Status.VALID, tokenBCredential.getStatus());
        assertEquals(user, tokenBCredential.getValidatedAccount());
    }
}