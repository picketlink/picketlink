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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = createTokenA(user);

        identityManager.updateCredential(user, token);

        TokenCredential credential = new TokenCredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = createTokenA(user);

        identityManager.updateCredential(user, token);

        TokenCredential badUserName = new TokenCredential(new TokenA("bad_token"));

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());
    }

    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = createTokenA(user);

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, token, new Date(), expirationDate.getTime());

        TokenCredential credential = new TokenCredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        TokenA newToken = createTokenA(user);

        Thread.sleep(1000);

        identityManager.updateCredential(user, newToken);

        credential = new TokenCredential(newToken);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA firstToken = createTokenA(user);

        identityManager.updateCredential(user, firstToken);

        TokenCredential firstCredential = new TokenCredential(firstToken);

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.VALID, firstCredential.getStatus());

        TokenA secondToken = createTokenA(user);

        Thread.sleep(1000);

        identityManager.updateCredential(user, secondToken);

        TokenCredential secondCredential = new TokenCredential(secondToken);

        identityManager.validateCredentials(secondCredential);

        assertEquals(Status.VALID, secondCredential.getStatus());

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.INVALID, firstCredential.getStatus());
    }

    @Test
    public void testUserDeletion() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User john = createUser("john");
        TokenA johnToken = createTokenA(john);

        identityManager.updateCredential(john, johnToken);

        TokenCredential johnCredential = new TokenCredential(johnToken);

        identityManager.validateCredentials(johnCredential);

        assertEquals(Status.VALID, johnCredential.getStatus());

        User francesco = createUser("francesco");
        TokenA francescoToken = createTokenA(francesco);

        identityManager.updateCredential(francesco, francescoToken);

        TokenCredential francescoCredential = new TokenCredential(francescoToken);

        identityManager.validateCredentials(francescoCredential);

        assertEquals(Status.VALID, francescoCredential.getStatus());

        identityManager.remove(francesco);

        identityManager.validateCredentials(johnCredential);
    }

    @Test
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = createTokenA(user);

        identityManager.updateCredential(user, token);

        TokenCredential credential = new TokenCredential(token);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TokenA token = createTokenA(user);

        identityManager.updateCredential(user, token);

        TokenCredentialStorage currentStorage = identityManager.retrieveCurrentCredential(user, TokenCredentialStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getValue());
        assertEquals(token.getToken(), currentStorage.getValue());
        assertEquals(TokenA.class.getName(), currentStorage.getType());
    }

    @Test
    public void testMultipleTokenSupport() throws Exception {
        User user = createUser("mary");
        TokenA tokenA = createTokenA(user);
        IdentityManager identityManager = getIdentityManager();

        identityManager.updateCredential(user, tokenA);

        TokenCredential tokenACredential = new TokenCredential(tokenA);

        identityManager.validateCredentials(tokenACredential);

        assertEquals(Status.VALID, tokenACredential.getStatus());

        TokenB tokenB = createTokenB(user);

        TokenCredential tokenBCredential = new TokenCredential(tokenB);

        identityManager.validateCredentials(tokenBCredential);

        assertEquals(Status.INVALID, tokenBCredential.getStatus());

        identityManager.updateCredential(user, tokenB);
        identityManager.validateCredentials(tokenBCredential);

        assertEquals(Status.VALID, tokenBCredential.getStatus());
    }

    private TokenA createTokenA(User user) {
        return createToken(TokenA.class, user);
    }

    private TokenB createTokenB(User user) {
        return createToken(TokenB.class, user);
    }

    public static class TokenA extends Token {

        public TokenA(String token) {
            super(token);
        }
    }

    public static class TokenAProvider implements Token.Provider {

        private PartitionManager partitionManager;

        @Override
        public Account getAccount(Token token) {
            String[] claims = token.getToken().split(";");

            if (claims.length != 3) {
                return null;
            }

            String realmName = claims[2].substring("issuer".length() + 1);
            String subject = claims[1].substring("subject".length() + 1);
            Realm partition = this.partitionManager.getPartition(Realm.class, realmName);
            IdentityManager identityManager = this.partitionManager.createIdentityManager(partition);

            IdentityQuery<User> query = identityManager.createIdentityQuery(User.class)
                .setParameter(User.LOGIN_NAME, subject);

            List<User> result = query.getResultList();

            if (!result.isEmpty()) {
                return result.get(0);
            }

            return null;
        }

        @Override
        public Token create(Object value) {
            return new TokenA(value.toString());
        }

        @Override
        public Token issue(Account account) {
            User user = (User) account;
            Token token = createToken(TokenA.class, user);

            getIdentityManager(user).updateCredential(account, token);

            return token;
        }

        private IdentityManager getIdentityManager(User user) {
            return this.partitionManager.createIdentityManager(user.getPartition());
        }

        @Override
        public Token renew(Token currentToken) {
            return issue(getAccount(currentToken));
        }

        @Override
        public boolean validate(Token token) {
            User user = (User) getAccount(token);
            TokenCredentialStorage tokenStorage = getIdentityManager(user)
                .retrieveCurrentCredential(user, TokenCredentialStorage.class);

            return tokenStorage.getValue().equals(token.getToken());
        }

        @Override
        public void invalidate(Account account) {

        }

        @Override
        public boolean supports(Token token) {
            return TokenA.class.equals(token.getClass());
        }

        @Override
        public <T extends TokenCredentialStorage> T getTokenStorage(Account account, Token token) {
            return null;
        }

        public void setPartitionManager(DefaultPartitionManager partitionManager) {
            this.partitionManager = partitionManager;
        }
    }

    public static class TokenB extends Token {

        public TokenB(String token) {
            super(token);
        }
    }

    public static class TokenBProvider extends TokenAProvider {

        @Override
        public Token issue(Account account) {
            return new TokenB(super.issue(account).getToken());
        }

        @Override
        public boolean supports(Token token) {
            return TokenB.class.equals(token.getClass());
        }
    }

    public static <T extends Token> T createToken(Class<T> tokenType, User user) {
        StringBuilder builder = new StringBuilder();

        builder
            .append("id=").append(UUID.randomUUID().toString())
            .append(";")
            .append("subject=").append(user.getLoginName())
            .append(";")
            .append("issuer=").append(user.getPartition().getName());

        if (TokenA.class.equals(tokenType)) {
            return (T) new TokenA(builder.toString());
        } else {
            return (T) new TokenB(builder.toString());
        }
    }

}