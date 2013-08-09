/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.idm;

import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.MixedLDAPJPAStoreConfigurationTester;

/**
 * @author pedroigor
 */
public class IdentityConfigurationTestFactory {

    public static IdentityConfigurationTester[] getConfigurations() {
        return new IdentityConfigurationTester[] {
                new FileStoreConfigurationTester(),
                new JPAStoreConfigurationTester(),
                new JPAStoreComplexSchemaConfigurationTester(),
                new MixedLDAPJPAStoreConfigurationTester(),
                new LDAPStoreConfigurationTester()
        };
    }

}
