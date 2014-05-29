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
package org.picketlink.test.idm.testers;

import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.TokenCredentialHandler;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.test.idm.credential.TokenCredentialTestCase;
import org.picketlink.test.idm.model.MyCustomAccount;

/**
 * @author pedroigor
 */
public class FileStoreConfigurationTester implements IdentityConfigurationTester {

    public static final String SIMPLE_FILE_STORE_CONFIG = "SIMPLE_FILE_STORE_CONFIG";

    @Override
    public DefaultPartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        TokenCredentialTestCase.TokenAProvider tokenAProvider = new TokenCredentialTestCase.TokenAProvider();
        TokenCredentialTestCase.TokenBProvider tokenBProvider = new TokenCredentialTestCase.TokenBProvider();

        builder
            .named(SIMPLE_FILE_STORE_CONFIG)
                .stores()
                    .file()
                    .preserveState(false)
                    .setCredentialHandlerProperty(CredentialHandler.SUPPORTED_ACCOUNT_TYPES_PROPERTY, new Class[]{MyCustomAccount.class})
                    .setCredentialHandlerProperty(CredentialHandler.LOGIN_NAME_PROPERTY, "userName")
                    .setCredentialHandlerProperty(TokenCredentialHandler.TOKEN_PROVIDER, new Token.Provider[]{tokenAProvider, tokenBProvider})
                    .supportAllFeatures();

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        tokenAProvider.setPartitionManager(partitionManager);
        tokenBProvider.setPartitionManager(partitionManager);

        return partitionManager;
    }

    @Override
    public void beforeTest() {
    }

    @Override
    public void afterTest() {
    }

}
