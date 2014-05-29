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
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.storage.OTPCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.TimeBasedOTP;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;
import static org.picketlink.idm.credential.Credentials.*;

/**
 * <p>
 * Test case for {@link org.picketlink.idm.credential.DigestCredentials} type.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class TOTPCredentialTestCase extends AbstractTOTPCredentialTestCase {

    public TOTPCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Account createAccount(String accountName) {
        return createUser(accountName);
    }
}