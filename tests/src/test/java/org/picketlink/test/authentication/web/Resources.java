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

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;

/**
 * @author pedroigor
 */
@Singleton
@Startup
public class Resources {

    @Inject
    private PartitionManager partitionManager;

    @PostConstruct
    public void create() {
        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("john");

        identityManager.add(user);

        Password password = new Password("passwd");

        identityManager.updateCredential(user, password);

        Digest digestCredential = new Digest();

        digestCredential.setRealm("Test Realm");
        digestCredential.setUsername(user.getLoginName());
        digestCredential.setPassword("passwd");

        identityManager.updateCredential(user, digestCredential);

        user = new User("jbid test");

        identityManager.add(user);

    }

}
