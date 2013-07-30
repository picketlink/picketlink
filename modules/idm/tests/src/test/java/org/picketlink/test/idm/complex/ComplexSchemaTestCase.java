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

import javax.persistence.EntityManager;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.jpa.model.sample.complex.Company;
import org.picketlink.idm.jpa.model.sample.complex.User;
import org.picketlink.idm.jpa.model.sample.complex.entity.Country;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
import org.picketlink.idm.jpa.model.sample.complex.entity.PersonAddress;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreComplexSchemaConfigurationTester;

/**
 * @author pedroigor
 */
@Ignore
public class ComplexSchemaTestCase extends AbstractPartitionManagerTestCase {

    private Country country;
    private EntityManager entityManager;
    private Company acmeCompany;
    private Company wayneCompany;
    private Company umbrellaCompany;

    public ComplexSchemaTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Override
    public void onBefore() {
        super.onBefore();

        if (JPAStoreComplexSchemaConfigurationTester.class.isInstance(getVisitor())) {
            JPAStoreComplexSchemaConfigurationTester visitor = (JPAStoreComplexSchemaConfigurationTester) getVisitor();

            this.entityManager = visitor.getEntityManager();

            this.country = new Country("Brazil");
            this.entityManager.persist(this.country);

            this.acmeCompany = createCompany("Acme");
            this.wayneCompany = createCompany("Wayne Enterprise");
            this.umbrellaCompany = createCompany("Umbrella Inc.");
        }
    }

    @Test
    public void testUser() {
        Employee person = new Employee();

        person.setFirstName("John");
        person.setLastName("Smith");

        PersonAddress address = new PersonAddress();

        address.setCity("Barbacena");
        address.setCountry(this.country);
        address.setStreetName("D. Pedro I");
        address.setType("Home");
        address.setUnitNumber("1501");
        address.setZip("70000000");
        address.setPrimaryAddress(true);

        person.addAddress(address);

        this.entityManager.persist(person);

        User user = new User("john");

        user.setPerson(person);

        IdentityManager identityManager = getPartitionManager().createIdentityManager(this.acmeCompany);

        identityManager.add(user);

        IdentityQuery<User> query = identityManager.createIdentityQuery(User.class);

        user = query.getResultList().get(0);

        System.out.println(user);
    }

    public Company createCompany(String name) {
        Company company = new Company(name);

        getPartitionManager().add(company);

        return company;
    }
}
