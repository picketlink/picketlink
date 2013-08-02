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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.jpa.model.sample.complex.Application;
import org.picketlink.idm.jpa.model.sample.complex.ApplicationAuthorization;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.EmployeeUser;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for custom {@link org.picketlink.idm.model.Relationship} types.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = JPAStoreComplexSchemaConfigurationTester.class)
public class ApplicationAuthorizationTestCase extends AbstractPartitionManagerTestCase {

    private ComplexSchemaHelper helper;

    public ApplicationAuthorizationTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        this.helper = new ComplexSchemaHelper("Acme", "acme.com", getVisitor());
    }

    @Test
    public void testCreate() throws Exception {
        EmployeeUser user = this.helper.createEmployeeUser("Chuck", "Norris", "chuck", this.helper.getSecurityOrgUnit());

        IdentityManager identityManager = getIdentityManager();

        Application application = new Application("Employee Manager");

        identityManager.add(application);

        ApplicationAuthorization authorization = new ApplicationAuthorization();

        authorization.setAccount(user);
        authorization.setApplication(application);

        Company company = (Company) user.getPartition();

        authorization.setProfileUrl("http://app@" + company.getDomain() + "/" + user.getUserName());

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(authorization);

        RelationshipQuery<ApplicationAuthorization> query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        List<ApplicationAuthorization> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(authorization.getAccount().getId(), result.get(0).getAccount().getId());
        assertEquals(authorization.getApplication().getId(), result.get(0).getApplication().getId());
        assertEquals(authorization.getApplication().getCreatedDate(), result.get(0).getApplication().getCreatedDate());
        assertEquals("http://app@acme.com/chuck", result.get(0).getProfileUrl());
    }

    @Test
    public void testFindByIdentity() throws Exception {
        EmployeeUser chuck = this.helper.createEmployeeUser("Chuck", "Norris", "chuck", this.helper.getSecurityOrgUnit());

        IdentityManager identityManager = getIdentityManager();

        Application application = new Application("Employee Manager");

        identityManager.add(application);

        ApplicationAuthorization chuckAuthorization = new ApplicationAuthorization();

        chuckAuthorization.setAccount(chuck);
        chuckAuthorization.setApplication(application);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(chuckAuthorization);

        RelationshipQuery<ApplicationAuthorization> query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        List<ApplicationAuthorization> result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(chuck.getId(), result.get(0).getAccount().getId());
        assertEquals(chuckAuthorization.getApplication().getId(), result.get(0).getApplication().getId());

        EmployeeUser mary = this.helper.createEmployeeUser("Mary", "Anne", "mary",
                this.helper.getSecurityOrgUnit());

        ApplicationAuthorization maryAuthorization = new ApplicationAuthorization();

        maryAuthorization.setAccount(mary);
        maryAuthorization.setApplication(application);

        relationshipManager.add(maryAuthorization);

        query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        result = query.getResultList();

        assertEquals(2, result.size());

        query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        query.setParameter(ApplicationAuthorization.ACCOUNT, chuck);

        result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(chuck.getId(), result.get(0).getAccount().getId());
    }

    @Test
    public void testExpiration() throws Exception {
        EmployeeUser user = this.helper.createEmployeeUser("Chuck", "Norris", "chuck", this.helper.getSecurityOrgUnit());

        IdentityManager identityManager = getIdentityManager();

        Application application = new Application("Employee Manager");

        identityManager.add(application);

        ApplicationAuthorization authorization = new ApplicationAuthorization();

        authorization.setAccount(user);
        authorization.setApplication(application);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(authorization);

        RelationshipQuery<ApplicationAuthorization> query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        List<ApplicationAuthorization> result = query.getResultList();

        assertEquals(1, result.size());
        assertNull(authorization.getApplication().getExpirationDate());

        authorization.setExpirationDate(new Date());

        relationshipManager.update(authorization);

        query = relationshipManager.createRelationshipQuery(ApplicationAuthorization.class);

        result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(authorization.getExpirationDate(), result.get(0).getExpirationDate());
    }

    @Override
    public IdentityManager getIdentityManager() {
        return this.helper.getIdentityManager();
    }
}
