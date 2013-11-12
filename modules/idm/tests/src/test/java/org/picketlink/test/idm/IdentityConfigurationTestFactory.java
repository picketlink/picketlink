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
import org.picketlink.test.idm.testers.JDBCStoreConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This factory is responsible to create all {@link IdentityConfigurationTester} that will be used to run the test cases
 * that extends {@link AbstractPartitionManagerTestCase}.</p>
 *
 * <p>It is possible to restrict which testers will run using the <code>testerClasses</code> system property.
 * This property accepts a comma separated list which the names of the test classes.</p>
 *
 * @author pedroigor
 */
public class IdentityConfigurationTestFactory {

    public static IdentityConfigurationTester[] getConfigurations() {
        List<IdentityConfigurationTester> testers = getIdentityConfigurationTesters();

        String testerClassesProperty = System.getProperty("testerClasses");

        if (testerClassesProperty != null) {
            List<String> testerClasses = Arrays.asList(testerClassesProperty.split(","));

            for (IdentityConfigurationTester tester : new ArrayList<IdentityConfigurationTester>(testers)) {
                if (!testerClasses.contains(tester.getClass().getSimpleName())) {
                    testers.remove(tester);
                }
            }
        }

        return testers.toArray(new IdentityConfigurationTester[testers.size()]);
    }

    private static List<IdentityConfigurationTester> getIdentityConfigurationTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

//        testers.add(new FileStoreConfigurationTester());
        testers.add(new JPAStoreConfigurationTester());
//        testers.add(new JPAStoreComplexSchemaConfigurationTester());
//        testers.add(new SingleConfigLDAPJPAStoreConfigurationTester());
//        testers.add(new LDAPStoreConfigurationTester());
//        testers.add(new LDAPUserGroupJPARoleConfigurationTester());
//        testers.add(new LDAPJPAPerformanceConfigurationTester());
//        testers.add(new JDBCStoreConfigurationTester());

        return testers;
    }

}