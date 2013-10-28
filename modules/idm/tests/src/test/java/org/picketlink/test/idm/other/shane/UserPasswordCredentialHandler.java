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
package org.picketlink.test.idm.other.shane;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.test.idm.other.shane.model.scenario1.User;

import java.util.List;

/**
 * @author pedroigor
 */
public class UserPasswordCredentialHandler extends PasswordCredentialHandler {

    @Override
    public Account getAccount(final IdentityContext context, final UsernamePasswordCredentials credentials) {
        IdentityManager identityManager = getIdentityManager(context);
        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        query.setParameter(User.LOGIN_NAME, credentials.getUsername());

        List<User> result = query.getResultList();

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);
    }

}
