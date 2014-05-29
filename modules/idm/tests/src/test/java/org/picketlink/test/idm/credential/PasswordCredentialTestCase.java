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

import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

/**
 * <p>
 * Test case for {@link UsernamePasswordCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include = {SingleConfigLDAPJPAStoreConfigurationTester.class, JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
public class PasswordCredentialTestCase extends AbstractPasswordCredentialTestCase {

    public PasswordCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Account createAccount(String accountName) {
        return createUser(accountName);
    }
}