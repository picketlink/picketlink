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
package org.picketlink.test.authentication.web;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

/**
 * @author pedroigor
 */
@Singleton
@Startup
public class Resources {

    public static final String DEFAULT_USERNAME = "john";
    public static final String DEFAULT_USER_PASSWD = "passwd";
    public static final String DEFAULT_DISABLED_USERNAME = "disabledUser";

    @Inject
    protected PartitionManager partitionManager;

    @PostConstruct
    public void create() {
        User john = new User(DEFAULT_USERNAME);

        addUser(john);

        User disabledUser = new User(DEFAULT_DISABLED_USERNAME);

        disabledUser.setEnabled(false);

        addUser(disabledUser);

        User httpCertUser = new User("jbid test");

        addUser(httpCertUser);
    }

    private void addUser(User user) {
        IdentityManager identityManager = partitionManager.createIdentityManager();

        identityManager.add(user);

        Password password = new Password("passwd");

        identityManager.updateCredential(user, password);

        Digest digestCredential = new Digest();

        digestCredential.setRealm("Test Realm");
        digestCredential.setUsername(user.getLoginName());
        digestCredential.setPassword(DEFAULT_USER_PASSWD);

        identityManager.updateCredential(user, digestCredential);
    }
}
