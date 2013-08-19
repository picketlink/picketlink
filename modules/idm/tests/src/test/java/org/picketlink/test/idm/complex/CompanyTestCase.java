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

package org.picketlink.test.idm.complex;

import org.junit.Test;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.SecurityPolicy;
import org.picketlink.idm.model.Attribute;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.partition.AbstractPartitionTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for the {@link org.picketlink.idm.model.basic.Tier} management operations.
 * </p>
 *
 * @author Pedro Silva
 *
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class CompanyTestCase extends AbstractPartitionTestCase<Company> {

    public CompanyTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Company createPartition() {
        Company company = new Company("Acme");

        company.setDomain("acme.com");

        getPartitionManager().add(company);

        return company;
    }

    @Test
    public void testCreate() {
        Company company = createPartition();
        Company storedCompany = getPartitionManager().getPartition(Company.class, company.getName());

        assertEquals("acme.com", storedCompany.getDomain());
    }

    @Test
    public void testSecurityPolicy() {
        Company company = createPartition();

        SecurityPolicy securityPolicy = new SecurityPolicy();

        securityPolicy.setRequiredCredentials(new String [] {"USER_PASSWORD"});

        company.setAttribute(new Attribute("security.policy", securityPolicy));

        getPartitionManager().update(company);

        Company storedCompany = getPartition();

        assertNotNull(storedCompany.getAttribute("security.policy"));
        assertEquals("USER_PASSWORD", storedCompany.<SecurityPolicy>getAttribute("security.policy").getValue()
                .getRequiredCredentials()[0]);
    }

    @Override
    protected Company getPartition() {
        return getPartitionManager().getPartition(Company.class, "Acme");
    }

}
