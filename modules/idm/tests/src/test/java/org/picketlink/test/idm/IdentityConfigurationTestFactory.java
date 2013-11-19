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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, List<IdentityConfigurationTester>> testConfigurations = new HashMap<String, List<IdentityConfigurationTester>>();

    static {
        testConfigurations.put("all", getAllTesters());
        testConfigurations.put("file", getFileStoreTesters());
        testConfigurations.put("jpa", getJPAStoreTesters());
        testConfigurations.put("ldap", getLDAPStoreTesters());
        testConfigurations.put("jdbc", getJDBCStoreTesters());
        testConfigurations.put("ldap_jpa", getLDAPAndJPAStoreTesters());
    }

    public static IdentityConfigurationTester[] getConfigurations() {
        String targetConfiguration = System.getProperty("test.idm.store", "all");
        List<IdentityConfigurationTester> testers = testConfigurations.get(targetConfiguration);

        if (testers == null) {
            throw new IllegalArgumentException("Invalid configuration: " + targetConfiguration);
        }

        String testerClassesProperty = System.getProperty("test.idm.configuration");

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

    private static List<IdentityConfigurationTester> getAllTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new FileStoreConfigurationTester());
        testers.add(new JPAStoreConfigurationTester());
        testers.add(new JPAStoreComplexSchemaConfigurationTester());
        testers.add(new SingleConfigLDAPJPAStoreConfigurationTester());
        testers.add(new LDAPStoreConfigurationTester());
        testers.add(new LDAPUserGroupJPARoleConfigurationTester());
        testers.add(new JDBCStoreConfigurationTester());

        return testers;
    }

    private static List<IdentityConfigurationTester> getFileStoreTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new FileStoreConfigurationTester());

        return testers;
    }

    private static List<IdentityConfigurationTester> getJPAStoreTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new JPAStoreConfigurationTester());
        testers.add(new JPAStoreComplexSchemaConfigurationTester());

        return testers;
    }

    private static List<IdentityConfigurationTester> getLDAPStoreTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new LDAPStoreConfigurationTester());

        return testers;
    }

    private static List<IdentityConfigurationTester> getJDBCStoreTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new JDBCStoreConfigurationTester());

        return testers;
    }

    private static List<IdentityConfigurationTester> getLDAPAndJPAStoreTesters() {
        List<IdentityConfigurationTester> testers = new ArrayList<IdentityConfigurationTester>();

        testers.add(new SingleConfigLDAPJPAStoreConfigurationTester());
        testers.add(new LDAPUserGroupJPARoleConfigurationTester());

        return testers;
    }

}